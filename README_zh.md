[English](README.md) | [**中文**](README_zh.md)

# GyJdbc

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.springstudent/GyJdbc"><img src="https://img.shields.io/badge/Maven%20Central-13.0.0.RELEASE-blue" alt="Maven Central" /></a>
  <img src="https://img.shields.io/badge/Java-8%2B-orange" alt="Java 8+" />
  <img src="https://img.shields.io/badge/Spring%20JDBC-5.3.x-brightgreen" alt="Spring JDBC 5.3.x" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="Apache 2.0" />
</p>

> 基于 Spring JdbcTemplate 的轻量级持久层框架：保留 SQL 的表达力，减少 DAO 层样板代码，让 Java 项目更快写出清晰、可维护的数据访问逻辑。

GyJdbc 适合那些不想引入重型 ORM、又不想反复手写 DAO 和 SQL 拼接代码的项目。它在 JdbcTemplate 之上提供了类 JPA 的实体 DAO、链式 SQL 构建器、Lambda 字段引用、Criteria 条件拼装，以及多数据源绑定和负载均衡能力。

## 目录

- [为什么选择 GyJdbc](#为什么选择-gyjdbc)
- [适用场景](#适用场景)
- [安装](#安装)
- [快速开始](#快速开始)
- [EntityDao 常用能力](#entitydao-常用能力)
- [Criteria：更舒服地拼动态条件](#criteria更舒服地拼动态条件)
- [SQL：像写 SQL 一样组合复杂语句](#sql像写-sql-一样组合复杂语句)
- [多数据源支持](#多数据源支持)
- [测试](#测试)
- [重要提示](#重要提示)
- [更多示例](#更多示例)
- [项目定位](#项目定位)
- [License](#license)

## 为什么选择 GyJdbc

- **DAO 层更轻**：通用增删改查、分页、批量操作、SQL 查询等能力由 `EntityDao` 提供，业务 DAO 不再堆重复代码。
- **SQL 仍然可控**：不是把 SQL 藏起来，而是用链式 API 把 SQL 写得更安全、更清楚。
- **接近原生 SQL 的表达力**：支持 `select`、`insert`、`update`、`delete`、`join`、`union`、子查询、分组、排序、分页、聚合函数等大部分 SQL 场景。
- **字段引用更稳**：支持 `TbUser::getName` 这类 Lambda 字段引用，减少字符串字段名带来的拼写风险。
- **动态条件友好**：`Criteria` 支持 `where`、`and`、`or`、`in`、`like`、`between`、嵌套条件、`xxxIfAbsent` 等常用条件拼装。
- **多数据源内置支持**：可以通过注解、DAO 方法绑定指定数据源，也可以按数据源组使用负载均衡策略。
- **学习成本低**：API 贴近 SQL 语义，熟悉 SQL 和 Spring JdbcTemplate 的开发者可以很快上手。

## 适用场景

GyJdbc 很适合：

- Spring / Spring Boot 项目中需要快速实现数据访问层；
- 业务以 SQL 为中心，不希望被复杂 ORM 映射规则束缚；
- 需要动态拼接查询条件、分页、批量操作；
- 需要在主从库、读写库、多个业务库之间灵活切换；
- 希望保留 JdbcTemplate 的简单直接，同时减少重复 DAO 代码。

如果你的项目需要完整的对象关系管理、复杂实体状态跟踪、一级缓存或自动脏检查，Hibernate / JPA 可能更适合。GyJdbc 的定位更直接：让你用更少代码写出更清晰的 SQL 数据访问层。

## 安装

```xml
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>GyJdbc</artifactId>
    <version>13.0.0.RELEASE</version>
</dependency>
```

当前版本基于 Java 8+ 和 Spring JDBC 5.3.x。

## 快速开始

### 1. 定义实体

使用 `@Table` 声明实体与数据库表的关系，`pk` 指定主键字段。

```java
import com.gysoft.jdbc.annotation.Table;

import java.util.Date;

@Table(name = "tb_user", pk = "id")
public class TbUser {
    private String id;
    private String name;
    private String realName;
    private String pwd;
    private String email;
    private String mobile;
    private Date birth;
    private Integer age;
    private String career;
    private Integer isActive = 0;
    private Integer roleId;

    // getter / setter
}
```

### 2. 定义 DAO

业务 DAO 继承 `EntityDao`，实现类继承 `EntityDaoImpl`。

```java
import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import org.springframework.stereotype.Repository;

public interface TbUserDao extends EntityDao<TbUser, String> {
}

@Repository
public class TbUserDaoImpl extends EntityDaoImpl<TbUser, String> implements TbUserDao {
}
```

### 3. 在 Service 中使用

```java
import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.Page;
import com.gysoft.jdbc.bean.PageResult;
import com.gysoft.jdbc.bean.SQL;

import java.util.Arrays;
import java.util.List;

public class UserService {

    private TbUserDao tbUserDao;

    public int createUser(TbUser user) throws Exception {
        return tbUserDao.save(user);
    }

    public List<TbUser> queryActiveUsers() throws Exception {
        return tbUserDao.queryWithCriteria(
                new Criteria()
                        .where(TbUser::getIsActive, 1)
                        .in(TbUser::getName, Arrays.asList("zhouning", "yinhw"))
        );
    }

    public PageResult<TbUser> pageUsers(int pageNo, int pageSize) throws Exception {
        return tbUserDao.pageQueryWithCriteria(
                new Page(pageNo, pageSize),
                new Criteria().where(TbUser::getIsActive, 1)
        );
    }

    public int updateEmail(String name, String email) throws Exception {
        return tbUserDao.updateWithSql(
                new SQL()
                        .update(TbUser.class)
                        .set(TbUser::getEmail, email)
                        .where(TbUser::getName, name)
        );
    }
}
```

## EntityDao 常用能力

`EntityDao<T, Id>` 覆盖了多数常见数据访问操作：

```java
// 新增 / 更新 / 删除
int save(T entity);
void batchSave(List<T> list);
void saveOrUpdate(T entity);
int saveAll(List<T> list);
int update(T entity);
void batchUpdate(List<T> list);
int updateWithSql(SQL sql);
int delete(Id id);
int batchDelete(List<Id> ids);
int deleteWithCriteria(Criteria criteria);
int deleteWithSql(SQL sql);

// 主键查询
T queryOne(Id id);
Optional<T> queryOneOpt(Id id);

// 条件查询
List<T> queryAll();
List<T> queryWithCriteria(Criteria criteria);
T queryOne(Criteria criteria);
Optional<T> queryOneOpt(Criteria criteria);
PageResult<T> pageQuery(Page page);
PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria);
long countWithCriteria(Criteria criteria);
boolean existsWithCriteria(Criteria criteria);
List<Id> queryIds(Criteria criteria);

// SQL 入参查询
<E> Result<E> queryWithSql(Class<E> type, SQL sql);
<E> List<E> queryListWithSql(Class<E> type, SQL sql);
<E> E queryOneWithSql(Class<E> type, SQL sql);
<E> Optional<E> queryOneWithSqlOpt(Class<E> type, SQL sql);
<E> PageResult<E> pageQueryWithSql(Page page, Class<E> type, SQL sql);
List<Map<String, Object>> queryMapsWithSql(SQL sql);
<K, V> Map<K, V> queryMapWithSql(SQL sql, ResultSetExtractor<Map<K, V>> extractor);
Integer queryIntegerWithSql(SQL sql);
long countWithSql(SQL sql);
boolean existsWithSql(SQL sql);

// DDL 与特殊操作
int insertWithSql(SQL sql);
String createWithSql(SQL sql);
void drop();
void truncate();
void drunk(SQL sql);
```

## Criteria：更舒服地拼动态条件

`Criteria` 适合查询条件来自页面筛选、接口参数、权限规则等动态场景。

```java
// WHERE name = ?
new Criteria().where(TbUser::getName, "zhouning");

// WHERE name IN (?,?)
new Criteria().in(TbUser::getName, Arrays.asList("zhouning", "yinhw"));

// WHERE age < ? ORDER BY age DESC
new Criteria()
        .lt(TbUser::getAge, 28)
        .orderBy(new Sort(TbUser::getAge));

// WHERE age < ? AND (name LIKE ? OR realName LIKE ?)
new Criteria()
        .lt(TbUser::getAge, 20)
        .andCriteria(
                new Criteria()
                        .like(TbUser::getName, "zhou")
                        .orLike(TbUser::getRealName, "周")
        );

// 参数为空时自动跳过，适合搜索表单
new Criteria()
        .where(TbUser::getIsActive, 1)
        .likeIfAbsent(TbUser::getName, keyword);
```

### Lambda 条件与嵌套条件

当字段来自实体 getter 时，可以直接使用 Lambda 引用字段，减少字符串字段名写错的风险。`andCriteria` / `orCriteria` 适合表达括号内的一组条件。

```java
// WHERE is_active = ? AND age >= ? AND (name LIKE ? OR real_name LIKE ?)
new Criteria()
        .where(TbUser::getIsActive, 1)
        .gte(TbUser::getAge, 18)
        .andCriteria(c -> c
                .like(TbUser::getName, "zhou")
                .orLike(TbUser::getRealName, "周"));

// WHERE role_id IN(?,?,?) OR (email IS NULL AND mobile IS NOT NULL)
new Criteria()
        .in(TbUser::getRoleId, Arrays.asList(1, 2, 3))
        .orCriteria(c -> c
                .isNull(TbUser::getEmail)
                .isNotNull(TbUser::getMobile));
```

### Where 与 WhereParam

`Where` 适合在一行里组合一组局部条件，`WhereParam` 适合把条件数组或列表交给 `Opt.AND` / `Opt.OR` 批量拼装。

```java
import com.gysoft.jdbc.bean.Opt;
import com.gysoft.jdbc.bean.Where;
import com.gysoft.jdbc.bean.WhereParam;

// WHERE name LIKE ? OR email LIKE ?
new Criteria()
        .and(
                Where.where(TbUser::getName).like("zhou")
                                .or(TbUser::getEmail).like("@example.com")
        );

// WHERE is_active = ? AND (name LIKE ? OR email LIKE ?)
new Criteria()
        .where(TbUser::getIsActive, 1)
        .andWhere(
                Where.where(TbUser::getName).like("zhou")
                        .or(TbUser::getEmail).like("@example.com")
        );

// WHERE role_id IN(?,?,?) AND age >= ? AND mobile IS NOT NULL
new Criteria()
        .and(
                Opt.AND,
                WhereParam.where(TbUser::getRoleId).in(Arrays.asList(1, 2, 3)),
                WhereParam.where(TbUser::getAge).gte(18),
                WhereParam.where(TbUser::getMobile).isNotNull()
        );

// WHERE is_active = ? AND (role_id IN(?,?,?) OR age >= ? OR mobile IS NOT NULL)
new Criteria()
        .where(TbUser::getIsActive, 1)
        .andWhere(
                Opt.OR,
                WhereParam.where(TbUser::getRoleId).in(Arrays.asList(1, 2, 3)),
                WhereParam.where(TbUser::getAge).gte(18),
                WhereParam.where(TbUser::getMobile).isNotNull()
        );

// WHERE is_active = ? AND (EXISTS(SELECT ...) AND age BETWEEN ? AND ?)
new Criteria()
        .where(TbUser::getIsActive, 1)
        .andWhere(
                Where.where("ignored").exists(
                        new SQL().select("*").from("tb_role").where("tb_role.id", 1)
                ).and(TbUser::getAge).betweenAnd(18, 35)
        );
```

## SQL：像写 SQL 一样组合复杂语句

`SQL` 构建器适合需要明确控制查询字段、表连接、聚合、子查询、更新语句的场景。

### 查询

```java
new SQL()
        .select(TbUser::getName, TbUser::getEmail, TbUser::getMobile)
        .from(TbUser.class)
        .where(TbUser::getIsActive, 1);
```

### 聚合、分组、排序

```java
import static com.gysoft.jdbc.bean.FuncBuilder.count;

new SQL()
        .select("age", count("age").as("num"))
        .from(TbUser.class)
        .groupBy(TbUser::getAge)
        .orderBy(new Sort(TbUser::getAge));
```

### 更新

```java
new SQL()
        .update(TbUser.class)
        .set(TbUser::getRealName, "元林")
        .set(TbUser::getEmail, "13888888888@163.com")
        .where(TbUser::getName, "Smith");
```

### 插入

```java
new SQL()
        .insertInto(TbAccount.class, "userName", "realName")
        .values("test", "测试")
        .values("test2", "测试2");
```

### 删除

```java
new SQL()
        .delete()
        .from(TbUser.class)
        .gt(TbUser::getAge, 20);
```

### JOIN

```java
new SQL()
        .select("u.name", "r.role_name")
        .from("tb_user", "u")
        .leftJoin("tb_role", "r")
        .on("u.role_id", "r.id")
        .where("u.is_active", 1);
```

也可以使用回调方式写连接条件，连接条件里同样支持 Lambda 字段引用和动态条件。

```java
// SELECT u.name, r.role_name, d.dept_name FROM tb_user u 
// INNER JOIN tb_role r  ON u.role_id = r.id  AND r.status = ? 
// LEFT JOIN tb_department d  ON u.dept_id = d.id  AND d.type = ? 
// WHERE u.is_active = ? AND (u.name LIKE ? OR u.real_name like ?)
new SQL()
        .select("u.name", "r.role_name", "d.dept_name")
        .from("tb_user", "u")
        .innerJoin("tb_role", "r", on -> on
                .on("u.role_id", "r.id")
                .and("r.status", "=", 1))
        .leftJoin("tb_department", "d", on -> on
                .on("u.dept_id", "d.id")
                .andIfAbsent("d.type", "=", deptType))
        .where("u.is_active", 1)
        .andCriteria(c -> c
                .like("u.name", keyword)
                .orLike("u.real_name", keyword));

new SQL()
        .select(Role::getName, Token::getTk)
        .from(Role.class, "r")
        .leftJoin(Token.class, "t", on -> on
                .on(Role::getName, Token::getTk)
                .and("t.status", "=", "active"));
```

### Where / WhereParam 与 SQL 组合

复杂筛选条件可以直接挂在 `SQL` 上，适合报表查询、列表筛选、权限条件等场景。

```java
new SQL()
        .select("*")
        .from("tb_user")
        .and(
                Where.where("is_active").equal(1)
                        .and("age").gte(18)
                        .or("name").like("zhou")
        );

new SQL()
        .select("*")
        .from("tb_user")
        .where("tenant_id", tenantId)
        .andWhere(
                Opt.OR,
                WhereParam.where("role_id").in(Arrays.asList(1, 2, 3)),
                WhereParam.where("email").like("@example.com"),
                WhereParam.where("mobile").isNotNull()
        );
```

### UNION / UNION ALL

```java
new SQL()
        .select("*")
        .from("tb_a")
        .where("status", 1)
        .unionAll()
        .select("*")
        .from("tb_b")
        .where("status", 1);
```

### 条件子查询

```java
new SQL()
        .select("*")
        .from("BOOK")
        .notIn(
                "id",
                new SQL()
                        .select("id")
                        .from("author")
                        .where("status", 1)
        );
```

### 嵌套子查询

```java
// complex nest select sql is ok 
new SQL().select("*").from(
        new SQL().select("a.*").from(
                new SQL().select("b.*").from(
                        new SQL().select("c.*").from(
                                new SQL().select("d.*").from(
                                        new SQL().select("e.*").from("nestTable")
                                ).where("key", "k1")
                        )
                ).like("keyLike","Lie").unionAll().select("f.*").from("f").isNotNull("notNull")
        ).where("condition", "1")
);
```

### 通过 FuncBuilder 使用 SQL 函数

`FuncBuilder` 用于构建 SQL 函数表达式，返回不可变的 `FuncExpr` 对象，支持嵌套组合与链式别名（`.as(alias)`）。使用前务必理解它的**参数约定**：

- `String` → **裸 SQL**（列名 / 表达式 / 变量），原样拼接，**不加引号**；
- `TypeFunction`（如 `TbUser::getAge`）→ 解析为对应的列名（兼容 `@Column` 改名）；
- `FuncExpr` → 已组合的表达式（`col()` / `lit()` / 其它函数的结果）；
- 值参数（`Object`）→ **值字面量**：`String` 自动加引号并转义（`'` → `''`），`Number` / `Boolean` 原样输出，`null` 输出 `NULL`。

由此列与字面量在类型层面被区分开——列用 `col("...")` / 裸 `String`，字符串常量用 `lit("...")`，数字直接传值。这正是框架避免手写引号、杜绝 SQL 注入的设计。

```java
import static com.gysoft.jdbc.bean.FuncBuilder.*;

// 聚合 + 别名：SELECT COUNT(id) AS total, MAX(age) AS maxAge FROM tb_user
new SQL().select(count("id").as("total"), max("age").as("maxAge"))
        .from("tb_user");

// Lambda 列引用 + lit() 字面量：CONCAT(realName, ', ', name) AS display
new SQL().select(concat(TbUser::getRealName, lit(", "), TbUser::getName).as("display"))
        .from(TbUser.class);

// String 为裸列名，Object 值为字面量（自动加引号转义）：IFNULL(remark, 'no remark')
new SQL().select(ifNull("remark", "no remark").as("remark"))
        .from("tb_order");

// 条件 + 日期函数
new SQL().select(
                caseWhen("salary > 10000", "high", "normal").as("level"),
                dateFormat("create_time", "%Y-%m-%d").as("day"),
                year(now()).as("current_year")
        ).from("tb_user");
```

可用函数分组：聚合（`count`、`countDistinct`、`sum`、`avg`、`max`、`min`、`groupConcat`、`groupConcatDistinct`）；字符串（`concat`、`concat_ws`、`length`、`charLength`、`substring`、`upper`、`lower`、`ltrim`、`rtrim`、`trim`、`left`、`right`、`replace`、`findInSet`、`locate`、`position`、`instr`、`elt`、`insert`）；数值（`abs`、`ceil`、`floor`、`round`、`truncate`、`mod`、`rand`）；日期（`now`、`curdate`、`curtime`、`dateFormat`、`format`、`dateAdd`、`dateSub`、`strToDate`、`month`、`monthname`、`week`、`year`、`hour`、`minute`、`weekday`、`dayname`、`date`）；条件（`ifNull`、`_if`、`caseWhen`）；JSON（`jsonExtract`、`jsonUnquote`、`jsonContains`、`jsonSet`、`jsonRemove`、`jsonObject`、`jsonArray`）；其它（`distinct`、`convertUsingGbk`、`unixTimeStamp`、`fromUnixTime`）。

### 复杂 UPDATE

`SQL` 支持更新别名表、连接更新、字段引用赋值以及子查询赋值。需要把右侧当作字段或表达式时，可以使用 `FieldReference`。

```java
import com.gysoft.jdbc.bean.FieldReference;

// UPDATE tb_user u SET u.email = ?, u.real_name = ? WHERE u.name = ?
new SQL()
        .update("tb_user", "u")
        .set("u.email", "13888888888@163.com")
        .set("u.real_name", "元林")
        .where("u.name", "Smith");

// UPDATE tb_user u INNER JOIN tb_account a ON u.id = a.user_id
// SET u.email = a.email, u.mobile = a.mobile WHERE a.status = ?
new SQL()
        .update("tb_user", "u")
        .innerJoin("tb_account", "a")
        .on("u.id", "a.user_id")
        .set("u.email", new FieldReference("a.email"))
        .set("u.mobile", new FieldReference("a.mobile"))
        .where("a.status", 1);

// UPDATE tb_score SET (avg_score,max_score) = (SELECT ...)
new SQL()
        .update("tb_score")
        .set(
                "(avg_score,max_score)",
                new SQL()
                        .select("AVG(score)", "MAX(score)")
                        .from("tb_score_detail")
                        .where("student_id", studentId)
        )
        .where("student_id", studentId);
```

### 复杂 DELETE

删除语句同样支持别名、多表删除、连接删除、字段引用比较和子查询条件。

```java
import com.gysoft.jdbc.bean.FieldReference;

// DELETE FROM tb_user WHERE age > ?
new SQL()
        .delete()
        .from("tb_user")
        .gt("age", 60);

// DELETE u FROM tb_user u INNER JOIN tb_account a ON u.id = a.user_id
// WHERE a.status = ? AND u.is_active = ?
new SQL()
        .delete("u")
        .from("tb_user")
        .as("u")
        .innerJoin("tb_account", "a")
        .on("u.id", "a.user_id")
        .where("a.status", 0)
        .and("u.is_active", 0);

// DELETE orders,items FROM orders,items
// WHERE orders.userid = items.userid AND orders.orderid = items.orderid AND orders.date <= ?
new SQL()
        .delete("orders,items")
        .from("orders,items")
        .where("orders.userid", new FieldReference("items.userid"))
        .and("orders.orderid", new FieldReference("items.orderid"))
        .let("orders.date", "2000/03/01");

// DELETE FROM tb_user WHERE id NOT IN(SELECT ...)
new SQL()
        .delete()
        .from("tb_user")
        .notIn(
                "id",
                new SQL()
                        .select("user_id")
                        .from("tb_order")
                        .where("status", "PAID")
        );
```

## 多数据源支持

GyJdbc 提供 `JdbcRoutingDataSource`，可以按 key 或 group 选择数据源。group 支持负载均衡策略，适合读写分离、多从库、租户库等场景。

### Spring Boot 配置示例

```java
import com.gysoft.jdbc.multi.JdbcRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatasourceConf {

    @Bean(name = "primary")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public HikariDataSource primary() {
        return new HikariDataSource();
    }

    @Bean(name = "secondary")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public HikariDataSource secondary() {
        return new HikariDataSource();
    }

    @Bean(name = "third")
    @ConfigurationProperties(prefix = "spring.datasource.third")
    public HikariDataSource third() {
        return new HikariDataSource();
    }

    @Bean(name = "dataSource")
    public DataSource dataSource(
            @Qualifier("primary") DataSource primary,
            @Qualifier("secondary") DataSource secondary,
            @Qualifier("third") DataSource third) {

        JdbcRoutingDataSource routingDataSource = new JdbcRoutingDataSource();
        routingDataSource.setDefaultLookUpKey("primary");

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("primary", primary);
        targetDataSources.put("secondary", secondary);
        targetDataSources.put("third", third);
        routingDataSource.setTargetDataSources(targetDataSources);

        Map<String, String> dataSourceKeysGroup = new HashMap<>();
        dataSourceKeysGroup.put("master", "primary");
        dataSourceKeysGroup.put("slave", "secondary,third");
        routingDataSource.setDataSourceKeysGroup(dataSourceKeysGroup);

        return routingDataSource;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

### 开启注解绑定

```java
import com.gysoft.jdbc.multi.BindPointAspectRegistar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(BindPointAspectRegistar.class)
public class SystemApp {

    public static void main(String[] args) {
        SpringApplication.run(SystemApp.class, args);
    }
}
```

### 使用 `@BindPoint`

```java
import com.gysoft.jdbc.multi.BindPoint;
import com.gysoft.jdbc.multi.balance.RandomLoadBalance;

// 从 slave 数据源组中随机选择一个数据源
@BindPoint(group = "slave", loadBalance = RandomLoadBalance.class)
public List<TbUser> queryFromSlave() throws Exception {
    return tbUserDao.queryAll();
}

// 绑定指定数据源
@BindPoint(key = "secondary")
public int updateSecondary(TbUser user) throws Exception {
    return tbUserDao.update(user);
}
```

### 在 DAO 调用级别绑定

使用 `DataSourceContext.withDataSource()` 将数据源绑定作用到整个 DAO 调用：

```java

PageResult<TbUser> result = DataSourceContext.withDataSource(
        "secondary",
        () -> tbUserDao.pageQuery(page)
);

DataSourceContext.withDataSourceGroup(
        "slave",
        RoundRobinLoadBalance.class,
        () -> tbUserDao.batchSave(users)
);
```

作用域支持嵌套，内层作用域结束后会自动恢复外层数据源；回调抛出异常时也会清理绑定。

如果操作带有 Spring 事务，作用域必须包裹事务入口，使数据源在事务获取连接之前确定：

```java
DataSourceContext.withDataSource("secondary", transactionalService::execute);
```

事务已经获取连接后不能通过切换路由 key 改变该事务使用的物理数据源。`@BindPoint` 切面会优先于事务切面执行，但跨数据源原子事务仍需要独立的分布式事务方案。

> **注意：路由绑定只对当前线程生效**。`@BindPoint` / `DataSourceContext.withDataSource` 的绑定存放在线程局部变量中，**不会传播到子线程**。在 `@Async`、`CompletableFuture`、线程池任务等异步场景里，子线程无法感知外层绑定，其中的数据库操作会回退到默认数据源（`defaultLookUpKey`）。如需在异步任务中指定数据源，请在子线程内部显式绑定——把 key/group 作为参数传入后，在子线程内调用 `DataSourceContext.withDataSource(...)`。

数据源选择优先级：

```text
ThreadLocal 绑定栈顶（后 push 优先，含 @BindPoint 切面与 DataSourceContext 编程式绑定）> JdbcRoutingDataSource.defaultLookUpKey
```

`@BindPoint` 内部：方法注解优先于类注解。

## 测试

GyJdbc 自带两层测试，用普通的 `mvn test` 即可运行，无需 Docker 或外部数据库。

### SQL 生成单测（无数据库依赖）

验证 SQL 构建器生成的 SQL 语句和参数是否正确。

- [`CSqlTest.java`](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/sqltest/CSqlTest.java) — SQL 构建器语法测试：select/insert/update/delete、join、union、子查询、聚合函数、create/truncate/drop 等。
- [`CriteriaTest.java`](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/sqltest/CriteriaTest.java) — `Criteria` 动态条件拼装测试。

### H2 集成测试（内存库，MySQL 模式）

用 H2 的 MySQL 兼容模式在真实内存库上运行 DAO，覆盖只有真实数据库才能暴露的行为。

- [`AbstractJdbcIT.java`](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/jdbctest/AbstractJdbcIT.java) — 测试基类：H2 连接（`MODE=MySQL`）、建表 DDL、反射注入 `jdbcTemplate`。
- [`EntityDaoImplIT.java`](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/jdbctest/EntityDaoImplIT.java) — DAO 全量行为：实体映射、增删改查、批量操作、分页、条件查询、join/union/子查询、`xxxIfAbsent`、DDL 操作。
- [`JdbcRoutingDataSourceIT.java`](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/jdbctest/JdbcRoutingDataSourceIT.java) — 编程式多数据源路由与主从库数据隔离验证。

### 运行测试

```bash
mvn test                        # 运行全部测试（含 *IT 集成测试）
mvn -Dtest=CSqlTest test        # 只跑 SQL 生成单测
mvn -Dtest=EntityDaoImplIT test # 只跑 H2 集成测试
```

## 重要提示

### 多数据源绑定是线程隔离的

`@BindPoint` / `DataSourceContext.withDataSource` 的绑定存放在 `ThreadLocal` 中，**不会传播到子线程**。在 `@Async`、线程池等异步场景中，子线程无法感知外层绑定，其数据库操作会回退到默认数据源。如需在异步任务内指定数据源，请在子线程内部显式绑定。

## 更多示例

- SQL 生成测试：[CSqlTest.java](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/sqltest/CSqlTest.java)、[CriteriaTest.java](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/sqltest/CriteriaTest.java)
- H2 集成测试：[EntityDaoImplIT.java](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/jdbctest/EntityDaoImplIT.java)、[JdbcRoutingDataSourceIT.java](https://github.com/SpringStudent/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/jdbctest/JdbcRoutingDataSourceIT.java)
- 使用示例项目：
  - [remote-desktop-control](https://github.com/SpringStudent/remote-desktop-control)
  - [webrtc-meetings](https://github.com/SpringStudent/webrtc-meetings)

## 项目定位

GyJdbc 不是为了取代所有 ORM，也不是为了隐藏 SQL。它更像是一个面向实战的 SQL 助手：

- 简单 CRUD 交给 `EntityDao`；
- 动态查询交给 `Criteria`；
- 复杂 SQL 交给 `SQL` 构建器；
- 多数据源选择交给 `JdbcRoutingDataSource` 和 `@BindPoint`。

你仍然掌控 SQL，但不用再把时间浪费在重复 DAO、字符串拼接和散落各处的数据源切换代码上。

## License

GyJdbc 使用 Apache License 2.0 开源协议。
