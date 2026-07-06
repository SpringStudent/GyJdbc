[English](README.md) | [中文](README_zh.md)

# GyJdbc
 
 > A lightweight persistence framework based on Spring JdbcTemplate: preserves SQL expressiveness while reducing DAO boilerplate, helping Java projects write clean, maintainable data access logic faster.
 
 GyJdbc is for projects that don't want a heavy ORM but are tired of writing DAO classes and SQL concatenation by hand. Built on top of JdbcTemplate, it provides JPA-like entity DAOs, a fluent SQL builder, Lambda field references, Criteria-based condition assembly, and multi-data-source binding with load balancing.
 
 ## Why GyJdbc
 
 - **Lighter DAO layer**: Generic CRUD, pagination, batch operations, and SQL queries are provided by `EntityDao` — no more repetitive code in business DAOs.
 - **SQL stays under control**: SQL isn't hidden; it's written more safely and clearly via a fluent API.
 - **Near-native SQL expressiveness**: Supports `select`, `insert`, `update`, `delete`, `join`, `union`, subqueries, grouping, sorting, pagination, aggregate functions, and most other SQL scenarios.
 - **Stabler field references**: Use Lambda references like `TbUser::getName` to avoid typos in string field names.
 - **Dynamic conditions made easy**: `Criteria` supports `where`, `and`, `or`, `in`, `like`, `between`, nested conditions, `xxxIfAbsent`, and other common condition assembly patterns.
 - **Built-in multi-data-source support**: Bind data sources via annotations or DAO methods, and use load-balancing strategies within data-source groups.
 - **Low learning curve**: The API follows SQL semantics, so developers familiar with SQL and Spring JdbcTemplate can pick it up quickly.
 
 ## When to Use GyJdbc
 
 GyJdbc is a great fit for:
 
 - Spring / Spring Boot projects that need a quick data access layer;
 - SQL-centric business logic that shouldn't be constrained by complex ORM mapping rules;
 - Scenarios requiring dynamic query conditions, pagination, or batch operations;
 - Switching between primary/replica, read/write, or multi-tenant databases;
 - Keeping JdbcTemplate's simplicity while cutting down repetitive DAO code.
 
 If your project needs full object-relational management, complex entity state tracking, first-level caching, or automatic dirty checking, Hibernate / JPA may be a better choice. GyJdbc's philosophy is more direct: write less code, produce clearer SQL, and build a maintainable data access layer.
 
 ## Installation
 
 ```xml
 <dependency>
     <groupId>io.github.springstudent</groupId>
     <artifactId>GyJdbc</artifactId>
     <version>6.0.0.RELEASE</version>
 </dependency>
 ```
 
 Current version is based on Java 8 and Spring JDBC 4.3.x.
 
 ## Quick Start
 
 ### 1. Define an Entity
 
 Use `@Table` to declare the entity-to-table mapping, and `pk` to specify the primary key field.
 
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
 
 ### 2. Define a DAO
 
 Business DAOs extend `EntityDao`, and their implementations extend `EntityDaoImpl`.
 
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
 
 ### 3. Use in a Service
 
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
 
 ## EntityDao Common Capabilities
 
 `EntityDao<T, Id>` covers most common data-access operations:
 
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
 
 ## Criteria: Build Dynamic Conditions More Comfortably
 
 `Criteria` is ideal when query conditions come from page filters, API parameters, permission rules, or other dynamic sources.
 
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
 
 // Automatically skip the condition when the parameter is null — great for search forms
 new Criteria()
         .where(TbUser::getIsActive, 1)
         .likeIfAbsent(TbUser::getName, keyword);
 ```
 
 ### Lambda Conditions and Nested Conditions
 
 When fields come from entity getters, use Lambda references directly to avoid string field name typos. `andCriteria` / `orCriteria` are for grouping a set of conditions inside parentheses.
 
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
 
 ### Where and WhereParam
 
 `Where` lets you combine a set of local conditions in one chained expression. `WhereParam` is for passing arrays or lists of conditions to `Opt.AND` / `Opt.OR` for bulk assembly.
 
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
 
 ## SQL: Compose Complex Statements Like Writing SQL
 
 The `SQL` builder is for scenarios where you need explicit control over query fields, table joins, aggregations, subqueries, and update/insert/delete statements.
 
 ### Select
 
 ```java
 new SQL()
         .select(TbUser::getName, TbUser::getEmail, TbUser::getMobile)
         .from(TbUser.class)
         .where(TbUser::getIsActive, 1);
 ```
 
 ### Aggregate, Group, Order
 
 ```java
 import static com.gysoft.jdbc.bean.FuncBuilder.countAs;
 
 new SQL()
         .select("age", countAs("age").as("num"))
         .from(TbUser.class)
         .groupBy(TbUser::getAge)
         .orderBy(new Sort(TbUser::getAge));
 ```
 
 ### Update
 
 ```java
 new SQL()
         .update(TbUser.class)
         .set(TbUser::getRealName, "Yuanlin")
         .set(TbUser::getEmail, "13888888888@163.com")
         .where(TbUser::getName, "Smith");
 ```
 
 ### Insert
 
 ```java
 new SQL()
         .insertInto(TbAccount.class, "userName", "realName")
         .values("test", "TestUser1")
         .values("test2", "TestUser2");
 ```
 
 ### Delete
 
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
 
 Join conditions can also be expressed via a callback, which supports Lambda field references and dynamic conditions.
 
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
 
 ### Where / WhereParam with SQL
 
 Complex filter conditions can be attached directly to the `SQL` builder — useful for report queries, list filtering, permission conditions, and similar scenarios.
 
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
 
 ### Subquery in Conditions
 
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
 
 ### Nested Subqueries
 
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
 
 ### Common MySQL Functions
 
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
 
 ### Complex UPDATE
 
 `SQL` supports updating aliased tables, join updates, field-reference assignments, and subquery assignments. Use `FieldReference` when the right-hand side should be treated as a column or expression rather than a parameter value.
 
 ```java
 import com.gysoft.jdbc.bean.FieldReference;
 
 // UPDATE tb_user u SET u.email = ?, u.real_name = ? WHERE u.name = ?
 new SQL()
         .update("tb_user", "u")
         .set("u.email", "13888888888@163.com")
         .set("u.real_name", "Yuanlin")
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
 
 ### Complex DELETE
 
 Delete statements also support aliases, multi-table deletes, delete with joins, field-reference comparisons, and subquery conditions.
 
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
 
 ## Multi-DataSource Support
 
 GyJdbc provides `JdbcRoutingDataSource`, which selects a data source by key or group. Groups support load-balancing strategies — useful for read/write splitting, multiple replicas, tenant databases, etc.
 
 ### Spring Boot Configuration Example
 
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
 
 ### Enable Annotation Binding
 
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
 
 ### Using `@BindPoint`
 
 ```java
 import com.gysoft.jdbc.multi.BindPoint;
 import com.gysoft.jdbc.multi.balance.RandomLoadBalance;
 
 // Randomly select a data source from the slave group
 @BindPoint(group = "slave", loadBalance = RandomLoadBalance.class)
 public List<TbUser> queryFromSlave() throws Exception {
     return tbUserDao.queryAll();
 }
 
 // Bind to a specific data source
 @BindPoint(key = "secondary")
 public int updateSecondary(TbUser user) throws Exception {
     return tbUserDao.update(user);
 }
 ```
 
 ### Binding at the DAO Call Level
 
 ```java
 import com.gysoft.jdbc.multi.balance.RoundRobinLoadBalance;
 
 // Execute query against a data source in the slave group
 List<TbUser> users = tbUserDao
         .bindKey("secondary")
         .queryWithCriteria(new Criteria().in(TbUser::getName, Arrays.asList("zhouning", "yinhw")));
 
 // Use round-robin strategy from the master group for an update
 tbUserDao
         .bindGroup("master", RoundRobinLoadBalance.class)
         .updateWithSql(
                 new SQL()
                         .update(TbUser.class)
                         .set(TbUser::getRealName, "Yuanlin")
                         .where(TbUser::getName, "Smith")
         );
 ```
 
 Data source resolution priority:
 
 ```text
 EntityDao.bindXxx > @BindPoint on method > @BindPoint on class > JdbcRoutingDataSource.defaultLookUpKey
 ```
 
 ## More Examples
 
 - SQL syntax tests: [CSqlTest.java](https://github.com/hope-for/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/CSqlTest.java)
 - Sample projects:
   - [remote-desktop-control](https://github.com/SpringStudent/remote-desktop-control)
   - [webrtc-meetings](https://github.com/SpringStudent/webrtc-meetings)
 
 ## Project Philosophy
 
 GyJdbc isn't meant to replace all ORMs, nor is it about hiding SQL. It's a practical SQL assistant:
 
 - Simple CRUD → `EntityDao`;
 - Dynamic queries → `Criteria`;
 - Complex SQL → the `SQL` builder;
 - Multi-data-source routing → `JdbcRoutingDataSource` and `@BindPoint`.
 
 You stay in control of your SQL — but you no longer waste time on repetitive DAO code, string concatenation, or scattered data-source-switching logic.
 
 ## License
 
 GyJdbc is open source under the Apache License 2.0.
