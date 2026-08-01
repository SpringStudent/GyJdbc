# Nested BindPoint Restoration Design

## Goal

Fix nested `@BindPoint` calls so that leaving an inner annotated method restores the outer annotated data source. Preserve the existing one-shot behavior of `bindKey` and `bindGroup`.

## Existing Semantics To Preserve

- A method-level `@BindPoint` overrides a class-level `@BindPoint`.
- `bindKey` and `bindGroup` affect the next routed database operation only.
- Consecutive method bindings use the last binding and do not restore earlier method bindings.
- A method binding used inside an annotated scope restores the nearest annotation binding after it is consumed.
- `clearDataSource` continues to remove the current binding completely.

## Design

### Annotation Binding Stack

`DataSourceBind.setPrev` will save the current binding directly when the new binding is `byAnno`. This gives nested annotation bindings a parent to restore.

The existing `byMethod` branch remains unchanged: it skips prior method bindings and retains only the nearest annotation binding. This preserves the current one-shot and last-method-binding-wins behavior.

### Scoped Restoration

`DataSourceBindHolder` will gain `restoreDataSource`. It will:

1. Decrease the active count accumulated by the current binding.
2. Restore `current.getPrev()` when present.
3. Remove the thread-local value when no previous binding exists.

The existing `clearDataSource` method will not change. The routing path for `byMethod` bindings will therefore keep its current behavior.

### Effective Aspect Binding

`BindPointAspect` will resolve class and method annotations first, select the method annotation when both exist, and push only that effective binding. Its `finally` block will call `restoreDataSource` only when it pushed a binding.

This avoids the current class-plus-method double push followed by a single cleanup.

## Required Behavior

```text
outer @BindPoint(A)
  route -> A
  inner @BindPoint(B)
    route -> B
  inner returns or throws
  route -> A
outer returns or throws
route -> default
```

Inside an annotated `A` scope:

```text
bindKey(B).query() -> B
next query         -> A
```

Without an annotated scope:

```text
bindKey(A).bindKey(B).query() -> B
next query                    -> default
```

## Error Handling

Restoration must run from the aspect `finally` block so that normal returns and exceptions have identical cleanup behavior. This change does not alter validation of empty `@BindPoint` declarations.

## Verification

Add focused tests for:

- Nested annotation binding restores the outer binding.
- An exception in the inner annotated scope still restores the outer binding.
- A method annotation overrides a class annotation without leaving the class binding behind.
- A one-shot `bindKey` inside an annotation scope restores the annotation binding.
- Consecutive method bindings still use only the last binding.

Run the focused tests, then the complete Maven test suite.
