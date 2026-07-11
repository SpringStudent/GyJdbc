# Lightweight SQLTree Metadata Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove `SqlRenderResult` while preserving the original `doSql(SQL)` contract and the verified FROM-subquery behavior.

**Architecture:** `buildSQLTree()` continues to construct nodes from `doSql()`'s existing `Pair<String,Object[]>`. A second SELECT-list pass returns `Pair<Integer,Integer>` containing the exact main FROM index and the number of parameters before it.

**Tech Stack:** Java 8, Maven, JUnit 4, existing GyJdbc SQL DSL

---

### Task 1: Restore doSql and add lightweight metadata analysis

**Files:**
- Modify: `src/main/java/com/gysoft/jdbc/tools/SqlMakeTools.java`

- [ ] **Step 1: Restore the original doSql contract**

Rename `renderSql(SQL)` back to `doSql(SQL)`, return `Pair<String,Object[]>` from every branch, and remove the `SqlRenderResult` class. Restore the non-SELECT `useSql()` path to:

```java
return doSql(sqlObj);
```

- [ ] **Step 2: Add SELECT metadata analysis**

Add a private helper:

```java
private static Pair<Integer, Integer> getFromMetadata(SQL sqlObj)
```

It starts with the rendered `SELECT ` or `SELECT DISTINCT ` prefix length, traverses `selectFields` using the same output lengths as `doSql()`, counts `ValueReference` and nested-SQL parameters, removes the final `, ` when fields exist, and returns:

```java
return new Pair<>(selectPrefixLength + 1, paramsBeforeFrom);
```

The `+ 1` identifies the `F` in the generated ` FROM ` delimiter, preserving existing recursive formatting.

- [ ] **Step 3: Populate SQLTree in buildSQLTree**

Keep the original pair-based construction and add only:

```java
Pair<Integer, Integer> metadata = getFromMetadata(subSql);
cTree.setFromIndex(metadata.getFirst());
cTree.setParamsBeforeFrom(metadata.getSecond());
```

Call the helper only for SELECT nodes.

### Task 2: Verify behavior and scope

**Files:**
- Test unchanged: `src/test/java/com/gysoft/jdbc/CSqlTest.java`
- Review: `src/main/java/com/gysoft/jdbc/tools/SqlMakeTools.java`

- [ ] **Step 1: Run SQL builder tests**

Run `mvn test -Dtest=CSqlTest`. Expected: all 290 tests pass.

- [ ] **Step 2: Run complete tests**

Run `mvn test`. Expected: all project tests pass with zero failures and errors.

- [ ] **Step 3: Inspect the final diff**

Confirm `SqlRenderResult` and `renderSql` no longer exist, `doSql()` again returns `Pair<String,Object[]>`, no private render node was introduced, and user changes in `CriteriaTest.java` remain untouched.
