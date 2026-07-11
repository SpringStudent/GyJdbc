# Lightweight SQLTree Metadata Design

## Objective

Improve the internal structure of the FROM-subquery fix without changing SQL output, parameter order, or any public SQL builder API.

The refactor must remove `SqlRenderResult` while preserving the original `doSql(SQL)` method and its `Pair<String, Object[]>` return contract.

## Current Problem

The first fix is behaviorally correct, but its implementation has unnecessary structure:

- `SqlRenderResult` duplicates SQL text and parameter metadata before those values are copied into `SQLTree`.
- The result object requires getter methods solely for data transfer inside one class.

## Design

The original `doSql(SQL)` method will be restored unchanged. It will continue returning only the SQL text and JDBC parameters as `Pair<String, Object[]>`.

`buildSQLTree()` will continue to call `doSql()` and use that pair to construct each `SQLTree`. For SELECT nodes it will make one additional lightweight metadata pass over the source `SQL` and obtain:

- The exact index of the main `FROM` keyword in the rendered SQL.
- The number of rendered parameters that occur before that main `FROM`.

The metadata helper will return `Pair<Integer, Integer>`, where the first value is `fromIndex` and the second is `paramsBeforeFrom`. It will derive both values from the SELECT-list structure rather than searching the final SQL text for the first `FROM` token. This preserves correct handling of expressions such as `EXTRACT(YEAR FROM created_at)`.

The helper is called only from `buildSQLTree()` for SELECT nodes. Non-SELECT behavior is untouched.

`recurSql(SQLTree, Pair<String, Object[]>)` will keep the verified merge order:

1. Parent parameters before the main FROM.
2. Child-node parameters in SQL order.
3. Parent parameters after the main FROM.

`SQLTree` retains `fromIndex` and `paramsBeforeFrom` because it is the object consumed by `recurSql()`. No additional model or result class is introduced.

## Workspace Preservation

The working tree currently contains user changes in `SqlMakeTools.java` and `CriteriaTest.java`. The implementation must preserve those changes. In particular, existing line-ending and formatting changes in `SqlMakeTools.java` must be used as the working baseline rather than overwritten.

The lesson recorded in `tasks/lessons.md` is separate from production implementation and must remain intact.

## Verification

- Keep the four FROM-subquery regression tests unchanged.
- Run `CSqlTest` and confirm all SQL text and parameter assertions pass.
- Run the complete Maven test suite.
- Confirm the final production diff removes `SqlRenderResult` and restores the original `doSql()` contract.
- Confirm no private `RenderNode` or other transfer object is introduced.
- Report user-owned uncommitted files separately.

## Out Of Scope

- Changing SQL formatting.
- Removing the two rendering metadata fields from `SQLTree`.
- Changing the FROM-subquery parameter-order algorithm.
- Replacing the broader SQL tree algorithm with a parser.
- Refactoring unrelated sections of `SqlMakeTools`.
- Modifying or cleaning user changes in `CriteriaTest.java`.
