# Multi-Data-Source Reliability Design

## Goal

Add an explicit scoped routing API and fix the existing multi-data-source routing defects without removing or deprecating `EntityDao.bindKey` and `EntityDao.bindGroup`.

## Compatibility

- `bindKey` and `bindGroup` keep their existing one-shot behavior: the binding affects the next routing lookup.
- Neither existing method is marked `@Deprecated`.
- `@BindPoint` keeps method-over-class precedence.
- Existing built-in load-balancing classes remain available.
- The new scoped API is the recommended choice for DAO methods that perform multiple JDBC operations, including paging and batched work.

## Scoped API

Add a public `DataSourceContext` utility with value-returning and void overloads:

```java
PageResult<User> result = DataSourceContext.withDataSource(
        "secondary",
        () -> userDao.pageQuery(page)
);

DataSourceContext.withDataSource(
        "secondary",
        () -> userDao.batchSave(users)
);

PageResult<User> result = DataSourceContext.withDataSourceGroup(
        "slave",
        RoundRobinLoadBalance.class,
        () -> userDao.pageQuery(page)
);
```

The API uses `Supplier<T>` for values and `Runnable` for void operations. Cleanup always runs from `finally`.

## Routing Context

Replace annotation restoration through `DataSourceBind.prev` with an explicit stack stored in `ThreadLocal<Deque<DataSourceBind>>`.

- Scoped bindings and annotation bindings are pushed on entry and popped on exit.
- `determineCurrentLookupKey` reads the current binding without consuming it.
- Nested scopes restore their immediate parent.
- Empty stacks remove their thread-local value.
- A separate one-shot slot preserves existing `bindKey` and `bindGroup` behavior.
- A one-shot binding has priority over the current scoped or annotation binding and is consumed by the next lookup.

Expected behavior:

```text
withDataSource(A)
  lookup -> A
  lookup -> A
  withDataSource(B)
    lookup -> B
  lookup -> A
lookup -> default
```

Inside an annotation scope:

```text
@BindPoint(A)
  bindKey(B); lookup -> B
  lookup -> A
```

## Annotation Aspect

`BindPointAspect` resolves the most specific target method and its target class before changing context.

- The method annotation wins when both method and class annotations exist.
- Only the effective binding is pushed.
- The binding is popped only when this advice pushed it.
- Cleanup runs for normal returns and exceptions.
- The aspect runs before Spring transaction advice so routing is established before a transaction obtains its connection.

Spring proxy self-invocation remains outside the supported behavior of `@BindPoint`; callers should use `DataSourceContext` when proxy interception is not available.

## Routing Configuration And Load Balancing

Routing configuration belongs to each `JdbcRoutingDataSource` instance.

- Data-source groups are copied and normalized when configured.
- Group members are trimmed and empty members are rejected.
- Load-balancing strategy instances are cached per routing data source.
- Built-in strategies are created through the same registry used for custom strategies.
- A custom strategy must have an accessible no-argument constructor; creation failures produce a descriptive exception.
- Round-robin sequences and least-active counters are isolated per routing data source.
- The registrar checks the target registry instead of using a JVM-wide static initialization flag.

Least-active counts represent active scoped or annotation routing operations. A binding is registered once after its concrete key is selected and released once when its scope exits. One-shot bindings are not long-lived scopes and therefore do not contribute an active duration.

## Validation And Errors

Fail before executing user code when:

- a binding contains neither key nor group;
- a binding contains both key and group;
- the key is blank;
- the group is blank or unknown;
- a group contains no usable keys;
- a requested strategy cannot be created;
- a group resolves to an unknown target data-source key.

No binding continues to use `defaultLookUpKey`. Invalid explicit bindings never silently fall back to the default data source.

## Transactions

The routing scope must begin before the transaction interceptor obtains a connection:

```java
DataSourceContext.withDataSource("secondary", transactionalService::execute);
```

Changing a routing key after a transaction has already bound a connection cannot switch that physical connection. The implementation orders `@BindPoint` before transaction advice and documents that programmatic scopes must wrap the transactional entry point. Cross-data-source atomic transactions require a dedicated distributed transaction solution and are outside this change.

## Tests

Add `MultiDataSourceTest` with focused unit tests for:

- repeated lookups within `withDataSource`;
- nested scoped restoration;
- cleanup after a callback exception;
- repeated lookups within a group scope;
- nested annotation restoration on return and exception;
- method annotation precedence over a class annotation;
- one-shot binding priority and consumption;
- restoration of an annotation scope after a one-shot binding;
- round-robin selection;
- custom load-balancing strategy creation;
- invalid binding and invalid group errors;
- isolation between two routing data-source instances;
- registrar behavior across independent bean registries.

Run the focused test class first, then the complete Maven test suite.

## Success Criteria

- A scoped callback can perform any number of routing lookups without changing its selected key.
- Nested scopes and annotations restore the correct parent after success or failure.
- Existing one-shot DAO binding behavior remains intact.
- Invalid explicit routing never falls back silently.
- Routing instances do not share group, strategy, sequence, counter, or registrar state.
- The new focused tests and the existing test suite pass.
