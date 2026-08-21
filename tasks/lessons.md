# Lessons（GyJdbc）

## L1 不要把"设计选择"当缺陷改掉（2026-08-21）

- 现象：我把 `and(Where)`/`or(Where)` 不给内层条件加括号列为高危缺陷并准备修复。
- 用户纠正："and(Where)是我设计如此"。`ConnectorType.WHERE_AND/WHERE_OR` 的 `wrapInParens=false` 是刻意的。
- 规则：审计报告里对**语义可辩护**的行为，先标为"待确认设计意图"，不要直接归类为 bug；只有明确违背 SQL 语义或会静默产生错数据的才算缺陷。

## L2 语义相近的 API 不要共用底层枚举（2026-08-21）

- 现象：`crossJoin` 复用 `JoinType.NatureJoin`，生成的是逗号连接而不是 `CROSS JOIN`。
- 用户要求："cross join 生成的 sql 就是 cross join"。
- 规则：只要用户能从方法名读出具体 SQL 关键字，生成的 SQL 就必须包含那个关键字，不能靠"MySQL 里等价"来省一个枚举值。补枚举值时追加在末尾，避免 ordinal 位移破坏兼容。

## L3 H2 不等于 MySQL，写集成测试前先想方言差异（2026-08-21）

- 现象：`countWithCriteria(criteria.orderBy(...))` 在 H2 报 `Column "age" must be in the GROUP BY list`，
  暴露出"COUNT(\*) 里拼 ORDER BY"这个原计划判定为"无害"的行为其实在 MySQL 5.7+（`ONLY_FULL_GROUP_BY`）也会报错。
- 规则：H2(MODE=MySQL) 能验证的是"SQL 能不能跑"，验证不了 MySQL 特有的优化器行为
  （派生表 `ORDER BY` 消除、反斜杠转义）。这类测试要在注释里写明是回归护栏而非等价证明。
- 附带规则：计划里写"某条件无害、保留"之前，先真跑一次带该条件的 SQL。
