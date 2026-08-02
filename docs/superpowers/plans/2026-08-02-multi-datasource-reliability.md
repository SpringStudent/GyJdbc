# Multi-Data-Source Reliability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add scoped data-source routing and make nested annotations, load balancing, configuration isolation, validation, and cleanup reliable while preserving the existing one-shot DAO binding API.

**Architecture:** Keep one-shot DAO bindings in a dedicated thread-local slot and store scoped or annotation bindings in a thread-local stack. Let each `JdbcRoutingDataSource` own its group definitions, strategy instances, round-robin sequences, and active-scope counters; the routing data source resolves bindings without mutating scoped context.

**Tech Stack:** Java 8, Spring JDBC 4.3, Spring AOP/AspectJ, JUnit 4, Maven.

---

### Task 1: Add Focused Failing Tests

**Files:**
- Create: `src/test/java/com/gysoft/jdbc/MultiDataSourceTest.java`

- [ ] **Step 1: Add a test routing data source and test fixtures**

```java
private static class TestRoutingDataSource extends JdbcRoutingDataSource {
    String currentKey() {
        return (String) determineCurrentLookupKey();
    }
}

public static class SelectLastStrategy implements LoadBalance {
    @Override
    public String select(DataSourceBind bind) {
        List<String> candidates = bind.getCandidateKeys();
        return candidates.get(candidates.size() - 1);
    }
}
```

- [ ] **Step 2: Add scoped-routing tests**

Test repeated lookup stability, nested restoration, exception cleanup, group scope stability, one-shot priority, and one-shot consumption with assertions against `currentKey()`.

- [ ] **Step 3: Add aspect and isolation tests**

Create small proxied services with `AspectJProxyFactory`; assert nested `@BindPoint` restoration, exception restoration, method-over-class precedence, custom load balancing, invalid groups, two routing instances, and independent registrar calls.

- [ ] **Step 4: Run the focused test and verify failure**

Run: `mvn test -Dtest=MultiDataSourceTest`

Expected: test compilation fails because `DataSourceContext` and the new candidate/stack APIs do not exist.

### Task 2: Implement Scoped Routing Context

**Files:**
- Create: `src/main/java/com/gysoft/jdbc/multi/DataSourceContext.java`
- Modify: `src/main/java/com/gysoft/jdbc/multi/DataSourceBindHolder.java`
- Modify: `src/main/java/com/gysoft/jdbc/multi/DataSourceBind.java`

- [ ] **Step 1: Separate one-shot and scoped state**

Implement one `ThreadLocal<DataSourceBind>` for the next lookup and one `ThreadLocal<Deque<DataSourceBind>>` for scoped bindings. `setDataSource` writes the one-shot slot; `pushDataSource`, `popDataSource`, and `currentDataSource` manage the stack. Popping releases the selected scoped binding and removes an empty thread local.

- [ ] **Step 2: Add scoped callback APIs**

```java
public static <T> T withDataSource(String key, Supplier<T> callback) {
    return execute(DataSourceBind.bindScopedKey(key), callback);
}

public static void withDataSource(String key, Runnable callback) {
    execute(DataSourceBind.bindScopedKey(key), () -> {
        callback.run();
        return null;
    });
}
```

Add matching `withDataSourceGroup` overloads. Validate callbacks and binding arguments before pushing, and always pop in `finally`.

- [ ] **Step 3: Make bindings scope-aware**

Store key, group, strategy type, selected key, candidate keys, active-count lookup, and a one-time release action in `DataSourceBind`. Add factories for one-shot key/group, scoped key/group, and annotations. Reject empty or conflicting annotation declarations.

- [ ] **Step 4: Run scoped tests**

Run: `mvn test -Dtest=MultiDataSourceTest`

Expected: context tests progress; routing and aspect tests remain failing until Tasks 3 and 4.

### Task 3: Isolate Routing And Load-Balancing State

**Files:**
- Modify: `src/main/java/com/gysoft/jdbc/multi/JdbcRoutingDataSource.java`
- Modify: `src/main/java/com/gysoft/jdbc/multi/balance/AbstractLoadBalance.java`
- Modify: `src/main/java/com/gysoft/jdbc/multi/balance/LeastActiveLoadBalance.java`

- [ ] **Step 1: Move group configuration into the routing instance**

Parse `Map<String, String>` into an instance-owned `Map<String, List<String>>`. Trim names, reject blank groups or members, and capture target keys from `setTargetDataSources` for explicit-binding validation.

- [ ] **Step 2: Resolve bindings in `JdbcRoutingDataSource`**

```java
DataSourceBind bind = DataSourceBindHolder.currentDataSource();
if (bind == null) {
    return defaultLookUpKey;
}
String key = bind.resolve(loadBalance(bind.getLoadBalance()), groupKeys(bind));
validateTargetKey(key);
registerActiveScope(bind, key);
return key;
```

The lookup consumes a one-shot binding but only reads scoped bindings. Invalid explicit bindings throw `GyjdbcException` instead of returning the default key.

- [ ] **Step 3: Instantiate strategies per routing data source**

Use an instance `ConcurrentMap<Class<? extends LoadBalance>, LoadBalance>` and `putIfAbsent`. Instantiate custom strategies through an accessible no-argument constructor and wrap reflective failures in a descriptive `GyjdbcException`.

- [ ] **Step 4: Use binding candidates and active-scope counts**

Change `AbstractLoadBalance` to read `DataSourceBind.getCandidateKeys()`. Change `LeastActiveLoadBalance` to call `DataSourceBind.getActiveCount(key)`. Increment a selected scoped key once and install a release action that decrements/removes the instance counter when the scope exits.

- [ ] **Step 5: Run routing tests**

Run: `mvn test -Dtest=MultiDataSourceTest`

Expected: scoped, validation, strategy, and instance-isolation tests pass; aspect tests remain.

### Task 4: Fix Annotation Routing And Registration

**Files:**
- Modify: `src/main/java/com/gysoft/jdbc/multi/BindPointAspect.java`
- Modify: `src/main/java/com/gysoft/jdbc/multi/BindPointAspectRegistar.java`

- [ ] **Step 1: Resolve one effective annotation**

Use `AopUtils.getMostSpecificMethod`, `ClassUtils.getUserClass`, and `AnnotatedElementUtils.findMergedAnnotation`. Select the method annotation first, otherwise the class annotation, and resolve everything before pushing context.

- [ ] **Step 2: Apply scoped cleanup and transaction ordering**

Annotate the aspect with `@Order(Ordered.HIGHEST_PRECEDENCE)`. Push the effective binding, call `proceed`, and pop in `finally`; proceed directly when no binding is resolved.

- [ ] **Step 3: Remove the global registrar flag**

Register `BindPointAspect` when the supplied `BeanDefinitionRegistry` does not already contain its bean name. Do not use JVM-wide state.

- [ ] **Step 4: Run all focused tests**

Run: `mvn test -Dtest=MultiDataSourceTest`

Expected: all tests in `MultiDataSourceTest` pass.

### Task 5: Document And Verify

**Files:**
- Modify: `README_zh.md`
- Modify: `README.md`

- [ ] **Step 1: Add scoped API examples**

Document `DataSourceContext.withDataSource` and `withDataSourceGroup` for paging and batch operations. State that one-shot DAO bindings affect the next lookup and remain supported.

- [ ] **Step 2: Document transaction boundaries**

State that the scope or `@BindPoint` must wrap the transactional entry point because an already-bound transaction connection cannot be switched.

- [ ] **Step 3: Run formatting checks**

Run: `git diff --check`

Expected: no whitespace errors.

- [ ] **Step 4: Run the complete suite**

Run: `mvn test`

Expected: `MultiDataSourceTest` and all existing tests pass with zero failures and errors.

- [ ] **Step 5: Inspect the final worktree**

Run: `git status --short` and `git diff --stat`.

Expected: only the requested implementation, tests, documentation, and the pre-existing user-owned staged deletion are present. Do not create a commit unless the user explicitly requests one.
