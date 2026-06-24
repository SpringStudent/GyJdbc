# GyJdbc

> 基于 Spring JdbcTemplate 的轻量级持久层框架：保留 SQL 的表达力，减少 DAO 层样板代码，让 Java 项目更快写出清晰、可维护的数据访问逻辑。

GyJdbc 适合那些不想引入重型 ORM、又不想反复手写 DAO 和 SQL 拼接代码的项目。它在 JdbcTemplate 之上提供了类 JPA 的实体 DAO、链式 SQL 构建器、Lambda 字段引用、Criteria 条件拼装，以及多数据源绑定和负载均衡能力。

## 为什么选择 GyJdbc

- **DAO 层更轻**：通用增删改查、分页、批量操作、SQL 查询等能力由 `EntityDao` 提供，业务 DAO 不再堆重复代码。
- **SQL 仍然可控**：不是把 SQL 藏起来，而是用链式 API 把 SQL 写得更安全、更清楚。
- **接近原生 SQL 的表达力**：支持 `select`、`insert`、`update`、`delete`、`join`、`union`、子查询、分组、排序、分页、聚合函数等常见 SQL 场景。
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
    <version>3.0.0.RELEASE</version>
</dependency>
```

当前版本基于 Java 8 和 Spring JDBC 4.3.x。

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
int save(T entity) throws Exception;
void batchSave(List<T> list) throws Exception;
void saveOrUpdate(T entity) throws Exception;
int saveAll(List<T> list) throws Exception;

int update(T entity) throws Exception;
void batchUpdate(List<T> list) throws Exception;
int updateWithSql(SQL sql) throws Exception;

int delete(Id id) throws Exception;
int batchDelete(List<Id> ids) throws Exception;
int deleteWithCriteria(Criteria criteria) throws Exception;
int deleteWithSql(SQL sql) throws Exception;

T queryOne(Id id) throws Exception;
T queryOne(Criteria criteria) throws Exception;
List<T> queryAll() throws Exception;
List<T> queryWithCriteria(Criteria criteria) throws Exception;
PageResult<T> pageQuery(Page page) throws Exception;
PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception;

<E> Result<E> queryWithSql(Class<E> type, SQL sql) throws Exception;
List<Map<String, Object>> queryMapsWithSql(SQL sql) throws Exception;
Integer queryIntegerWithSql(SQL sql) throws Exception;
boolean existsWithCriteria(Criteria criteria) throws Exception;
boolean existsWithSql(SQL sql) throws Exception;
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
import static com.gysoft.jdbc.bean.FuncBuilder.countAs;

new SQL()
        .select("age", countAs("age").as("num"))
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

### 子查询

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

### MySQL 常用函数

```java
import static com.gysoft.jdbc.bean.FuncBuilder.*;

new SQL()
        .select(
                countAs("id").as("total"),
                maxAs("age").as("maxAge"),
                jsonExtractAs("extra", "$.name").as("nameJson")
        )
        .from("tb_user")
        .where("is_active", 1);
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

```java
import com.gysoft.jdbc.multi.balance.RoundRobinLoadBalance;

// 指定 slave 数据源执行查询
List<TbUser> users = tbUserDao
        .bindKey("secondary")
        .queryWithCriteria(new Criteria().in(TbUser::getName, Arrays.asList("zhouning", "yinhw")));

// 在 master 组中使用轮询策略选择数据源执行更新
tbUserDao
        .bindGroup("master", RoundRobinLoadBalance.class)
        .updateWithSql(
                new SQL()
                        .update(TbUser.class)
                        .set(TbUser::getRealName, "元林")
                        .where(TbUser::getName, "Smith")
        );
```

数据源选择优先级：

```text
EntityDao.bindXxx > 方法上的 @BindPoint > 类上的 @BindPoint > JdbcRoutingDataSource.defaultLookUpKey
```

## 更多示例

- SQL 语法测试：[CSqlTest.java](https://github.com/hope-for/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/CSqlTest.java)
- 集成测试项目：[GyJdbcTest](https://github.com/SpringStudent/GyJdbcTest)
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
