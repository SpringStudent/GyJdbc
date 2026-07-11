# FROM Subquery Rendering Design

## Objective

Fix two defects in SELECT statements whose `FROM` clause contains subqueries:

1. Parameters from the parent SELECT list can be placed after child-query parameters.
2. The main `FROM` boundary is found with `indexOf("FROM")`, which can mistake an expression such as `EXTRACT(YEAR FROM created_at)` for the clause boundary.

The public `SQL` builder and `SqlMakeTools.useSql()` API must remain unchanged.

## Root Cause

`doSql()` currently flattens each node into one SQL string and one parameter array. `recurSql()` later tries to reconstruct the parent/child layout by searching that flattened string for `FROM`. At that point it no longer knows which parent parameters occur before the `FROM` subquery and which occur after it, so it appends all child parameters before all parent parameters.

## Design

Each `SQLTree` SELECT node will retain two pieces of render metadata:

- `fromIndex`: the exact character position at which `doSql()` appended the node's main ` FROM ` clause.
- `paramsBeforeFrom`: the number of parameters already collected when that clause was appended.

`SqlMakeTools` will use a private render-result type to carry the SQL text, parameters, and these boundaries from the single-node renderer into `buildSQLTree()`. `useSql()` will continue returning `Pair<String, Object[]>`; the metadata is internal only.

`recurSql()` will split SQL using the recorded index instead of scanning SQL text. It will merge parameters in this order:

1. Parent parameters before the main `FROM`.
2. Child-query parameters in child SQL order.
3. Remaining parent parameters, including joins, WHERE, GROUP/HAVING, ORDER, and LIMIT.

Leaf nodes and non-SELECT statements keep their existing behavior. The virtual root node has no real `FROM` boundary and carries no parameters.

## Compatibility

- No public method signatures change.
- Existing SQL formatting is preserved except where it was corrupted by matching an expression-internal `FROM`.
- `SQLTree` is a public class but is used as an internal rendering structure. New fields are additive; its existing constructor and accessors remain available.
- No SQL parser or new dependency is introduced.

## Verification

Add regression tests to `CSqlTest` for:

- A parent SELECT `ValueReference`, a parameterized FROM subquery, and a parent WHERE condition, asserting exact parameter order.
- A parent SELECT expression containing `EXTRACT(YEAR FROM ...)` with a FROM subquery, asserting exact SQL output.
- Multiple parameterized FROM subqueries with parent parameters.
- Two nested FROM-subquery levels with parameters before and after each boundary.

Run each new test against the old implementation and confirm failure. After implementation, run the focused tests and the complete Maven test suite.

## Out Of Scope

- General SQL parsing or dialect abstraction.
- Validation of empty SELECT, UPDATE, or table names.
- DDL escaping and raw identifier/operator validation.
- Changes to the public `Pair` return contract.
