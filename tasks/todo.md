# 中危缺陷修复（2026-08-21）

用户决定：`and(Where)`/`or(Where)` 不加括号是设计如此，不改。以下 8 条中危问题全部修复。

- [x] M1 `AbstractCriteria.limit(offset, size)` 参数校验：`limit(10, 0)` 现在生成 `LIMIT ?/[10]`（语义变成"取前 10 行"），`limit(-1, 10)` 生成 `LIMIT ?/[10]`。两参版本要求 `offset >= 0 && size > 0`，否则抛 `GyjdbcException` 并提示改用单参 `limit(n)`。
- [x] M2 `EntityDaoImpl.countWithCriteria` 前置校验：`groupBy`/`having` 会让 `queryForObject` 抛 `IncorrectResultSizeDataAccessException`，`limit` 带 offset 会让结果集为空抛 `EmptyResultDataAccessException`。三者一律 fail fast；`orderBy` 改为**静默忽略**（见 Review 补充）。
- [x] M3 分页把 `ORDER BY` 留在派生表内、`LIMIT` 放外层（`EntityDaoImpl.pageQueryWithCriteria`/`pageQueryWithSql`、`Result.pageQuery`）。MySQL 合并派生表时会丢弃其中无 LIMIT 的 `ORDER BY` → 翻页顺序不稳定。改为数据查询不再包派生表，直接在基础 SQL 末尾追加 `LIMIT ?,?`；`COUNT(*)` 仍包派生表。
- [x] M4 `Where.exists/notExists` 忽略 key，`or("b").exists(sub)` 输出 `AND EXISTS(...)`，OR 丢失且无法表达 `OR EXISTS`。给 `AbstractCriteria` 补 `orExists/orNotExists`，`Where.exists/notExists` 识别 key 上的 OR 前缀，并补显式 `Where.orExists/orNotExists`。
- [x] M5 `MixUtils.getSqlType` 未覆盖 `BigInteger`/枚举/`byte[]`/`UUID`/`OffsetDateTime` 等，统一落 `Types.OTHER`（Connector/J 会序列化写 BLOB 或直接报错）。补全映射分支。
- [x] M6 `EntityDaoImpl.createWithSql` 内 `insertWithSql(sql)` 是自调用，`@Transactional` 代理不生效 → 建表后的分批插入无事务保护。给 `createWithSql` 加 `@Transactional`。
- [x] M7 `ResultSetExractorFactory.getColumVal`：`Date.class.isAssignableFrom` 走 `rs.getDate()` 丢时分秒；包装类型遇 SQL NULL 返回 `0`/`false` 而非 null。按 `java.sql.Date`/`Time`/`Timestamp`/`java.util.Date` 分别取值，包装类型用 `rs.wasNull()` 回 null。
- [x] M8 `pom.xml`：删除完全未被引用的 `commons-beanutils 1.9.4`（CVE-2025-48734）；`aspectjweaver` 改 `optional`（只有 `@BindPoint` 需要，不应强制传递给使用者）。

## 验证

每组改动后 `mvn -o -B -fn -Dmaven.test.failure.ignore=true clean test`，看 `target/surefire-reports/*.txt` 计数。基线 503 全绿。

最终：**524 全绿**（CSqlTest 420、CriteriaTest 14、EntityDaoImplIT 86、JdbcRoutingDataSourceIT 4，无 failure/error）。新增 21 个测试。

## Review

- M2 的实施与计划有一处偏差：原计划"`orderBy` 无害，保留"是错的。写集成测试时 H2 直接报
  `Column "age" must be in the GROUP BY list`，MySQL 5.7+ 默认开 `ONLY_FULL_GROUP_BY` 同样会拒绝
  `SELECT COUNT(*) ... ORDER BY age`。而"同一个 Criteria 既查分页又统计总数"是最常见用法，
  抛异常会把正常代码打断，因此改为静默忽略排序：`SqlMakeTools.doCriteria` 新增
  `doCriteria(criteria, sql, withSort)` 重载（旧两参签名委托 `withSort=true`，不破坏兼容），
  `countWithCriteria` 传 `false`。
- 唯一的行为破坏点是 M1：原先 `limit(10, 0)`/`limit(-1, 10)` 会静默生成语义不同的 SQL，现在抛
  `GyjdbcException`。仓库内无任何测试依赖旧行为，但属于 public API 行为变化，发版需写进 release note。
- 测试覆盖：M1/M4/M5 用 `CSqlTest` 纯 SQL 断言（11 个）；M2/M3/M7 用 H2 集成测试（10 个）。
  M6 只是加注解，靠既有 `createWithSql` 集成测试保证不回归。M8 靠编译期 + 全量测试保证无引用。
- 两处 H2 无法证伪的限制：H2 不复现 MySQL 派生表 `ORDER BY` 消除，所以 M3 的测试是回归护栏而非
  「改前失败、改后通过」的证明；H2 也不复现 MySQL 反斜杠转义语义。

