# Private Render Node Refactor Design

## Objective

Improve the internal structure of the FROM-subquery fix without changing SQL output, parameter order, or any public SQL builder API.

The refactor must remove rendering-only metadata from the public `SQLTree` bean and replace the current two-stage `SqlRenderResult` to `SQLTree` data transfer with one private rendering model owned by `SqlMakeTools`.

## Current Problem

The first fix is behaviorally correct, but its implementation has unnecessary structure:

- `SQLTree` exposes `fromIndex` and `paramsBeforeFrom`, although these values are meaningful only during one rendering operation.
- `SqlRenderResult` duplicates SQL text and parameter metadata before those values are copied into `SQLTree`.
- `buildSQLTree()` creates UUID node identifiers even though the renderer only needs to distinguish the virtual root.
- The result object requires getter methods solely for data transfer inside one class.

## Design

`SqlMakeTools` will define one private static `RenderNode`. It will contain the complete state required by recursive rendering:

- Rendered SQL text and parameters.
- Exact main `FROM` index and the number of parameters before it.
- Child render nodes.
- Union type and alias metadata.
- A boolean virtual-root marker.

Because `RenderNode` is private to `SqlMakeTools`, the enclosing class will access its fields directly. No getter/setter layer is needed.

`renderSql(SQL)` will create and return a fully populated `RenderNode`. For a SELECT node it records the main FROM boundary while rendering the SELECT list. For non-SELECT statements it leaves the boundary unset and retains the existing SQL and parameter behavior.

`buildRenderTree(SQL, RenderNode)` will recursively create child nodes directly from each source `SQL`. It will replace `buildSQLTree()` and will not create UUID identifiers.

`recurSql(RenderNode, Pair<String, Object[]>)` will keep the verified merge order:

1. Parent parameters before the main FROM.
2. Child-node parameters in SQL order.
3. Parent parameters after the main FROM.

The virtual root will be represented by `virtualRoot == true`, replacing the special string ID `"0"`.

For non-SELECT statements, `useSql()` will convert the private node back to the existing `Pair<String, Object[]>` return type. No public signature changes.

## SQLTree Compatibility

The two fields introduced by the first fix, plus their accessors, will be removed from `SQLTree`. All fields, constructors, and methods that existed before that fix remain unchanged. `SQLTree` itself will not be deleted or otherwise refactored.

## Workspace Preservation

The working tree currently contains user changes in `SqlMakeTools.java` and `CriteriaTest.java`. The implementation must preserve those changes. In particular, existing line-ending and formatting changes in `SqlMakeTools.java` must be used as the working baseline rather than overwritten.

The lesson recorded in `tasks/lessons.md` is separate from production implementation and must remain intact.

## Verification

- Keep the four FROM-subquery regression tests unchanged.
- Run `CSqlTest` and confirm all SQL text and parameter assertions pass.
- Run the complete Maven test suite.
- Confirm the final production diff removes the `SQLTree` rendering metadata and `SqlRenderResult`.
- Confirm `SQLTree` has no new public API compared with its state before the original fix.
- Report user-owned uncommitted files separately.

## Out Of Scope

- Changing SQL formatting.
- Changing the FROM-subquery parameter-order algorithm.
- Replacing the broader SQL tree algorithm with a parser.
- Refactoring unrelated sections of `SqlMakeTools`.
- Modifying or cleaning user changes in `CriteriaTest.java`.
