# FROM Subquery Rendering Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve placeholder parameter order around FROM subqueries and stop mistaking expression-internal `FROM` tokens for the main clause boundary.

**Architecture:** Record the main FROM character boundary and the number of parameters rendered before that boundary on each `SQLTree` node. Keep `useSql()` unchanged while an internal render result transports this metadata from single-node rendering into recursive tree rendering.

**Tech Stack:** Java 8, Maven, JUnit 4, existing GyJdbc SQL DSL

---

### Task 1: Add failing regression tests

**Files:**
- Modify: `src/test/java/com/gysoft/jdbc/CSqlTest.java`

- [ ] **Step 1: Add a parent/child parameter-order test**

Add a test that builds a parent SELECT containing `ValueReference.newValueRef("parent-select")`, a FROM child with `where("child_id", "child-where")`, and a parent `where("parent_id", "parent-where")`. Assert the complete SQL and parameter order:

```java
assertArrayEquals(
        new Object[]{"parent-select", "child-where", "parent-where"},
        pair.getSecond());
```

- [ ] **Step 2: Add an expression-internal FROM test**

Build a parent query selecting `EXTRACT(YEAR FROM created_at)` from a parameterized child query. Assert that the expression remains before the generated main FROM subquery and that the child parameter is preserved.

- [ ] **Step 3: Add multiple-child and nested-boundary tests**

Add one test with two parameterized FROM children and parent parameters, plus one test with two nested FROM-subquery levels. Assert exact SQL text and the parameters in placeholder order.

- [ ] **Step 4: Run the focused tests and verify the old implementation fails**

Run:

```text
mvn test -Dtest=CSqlTest#fromSubqueryShouldPreserveParentAndChildParameterOrder,CSqlTest#fromSubqueryShouldNotSplitExpressionFrom,CSqlTest#multipleFromSubqueriesShouldPreserveParameterOrder,CSqlTest#nestedFromSubqueriesShouldPreserveParameterOrder
```

Expected: at least the parameter-order test and expression-internal FROM test fail on the old implementation.

### Task 2: Carry exact render boundaries through SQLTree

**Files:**
- Modify: `src/main/java/com/gysoft/jdbc/bean/SQLTree.java`
- Modify: `src/main/java/com/gysoft/jdbc/tools/SqlMakeTools.java`

- [ ] **Step 1: Add additive SQLTree metadata**

Add fields with accessors while preserving existing constructors:

```java
private int fromIndex = -1;
private int paramsBeforeFrom;
```

- [ ] **Step 2: Introduce a private node-render result**

Inside `SqlMakeTools`, add a private static result type containing `sql`, `params`, `fromIndex`, and `paramsBeforeFrom`. Change the internal single-node renderer to return this type. Keep a small adapter where callers still require `Pair<String, Object[]>`.

- [ ] **Step 3: Record the SELECT boundary at its source**

Immediately before appending the generated ` FROM ` clause, record:

```java
fromIndex = sql.length();
paramsBeforeFrom = params.size();
sql.append(" FROM ");
```

Non-SELECT render results retain `fromIndex == -1` and `paramsBeforeFrom == 0`.

- [ ] **Step 4: Store metadata when building tree nodes**

Populate each child `SQLTree` from the internal render result, including both new boundary fields. Configure the virtual root with no boundary.

### Task 3: Render SQL and parameters by structural boundary

**Files:**
- Modify: `src/main/java/com/gysoft/jdbc/tools/SqlMakeTools.java`

- [ ] **Step 1: Remove textual FROM discovery**

Replace `parentSql.indexOf("FROM")` with `sqlTree.getFromIndex()`. Split using the exact length of the generated ` FROM ` delimiter.

- [ ] **Step 2: Merge parameters around child rendering**

For a node with children, append parent parameters `[0, paramsBeforeFrom)` before rendering children. Append child parameters in child order. Append parent parameters `[paramsBeforeFrom, length)` after all children. Preserve existing leaf behavior.

- [ ] **Step 3: Run the four focused tests**

Run the command from Task 1. Expected: 4 tests pass with zero failures and errors.

- [ ] **Step 4: Run the SQL-builder test class**

Run:

```text
mvn test -Dtest=CSqlTest
```

Expected: all `CSqlTest` tests pass.

- [ ] **Step 5: Run the complete suite**

Run:

```text
mvn test
```

Expected: all project tests pass with zero failures and errors.

### Task 4: Review scope and repository state

**Files:**
- Review: `src/main/java/com/gysoft/jdbc/bean/SQLTree.java`
- Review: `src/main/java/com/gysoft/jdbc/tools/SqlMakeTools.java`
- Review: `src/test/java/com/gysoft/jdbc/CSqlTest.java`

- [ ] **Step 1: Inspect the final diff**

Confirm every production change directly supports boundary capture or ordered recursive rendering. Confirm no public method signature changed and `CriteriaTest.java` remains outside the implementation diff.

- [ ] **Step 2: Verify the final repository state**

Use `git status --short` and report the pre-existing `CriteriaTest.java` modification separately from files changed by this fix.
