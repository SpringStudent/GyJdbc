## 是什么

GyJdbc基于jdbctemplate的类似JPA的持久层框架封装，使用优势：

1. **Dao层0代码，再也不需要为Dao层的方法名称命名掉头发。**
2. **链式SQL配合lambda表达式，既装B又简洁。**
3. **强悍的SQL拼接，支持作者已知的所有SQL语法。**
4. **学习成本极低，靠近SQL语法，开发者使用起来会像平时一样写SQL一样简单。**
5. **提供类JPA语法，类MongoTemplate的SQL拼接语法**
6. **支持多数据源，多数据源的负载均衡，仅需一个注解或者一个方法调用。**

#### 快速开始

**step1.添加maven坐标**

```xml

<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>GyJdbc</artifactId>
    <version>1.4.2.RELEASE</version>
</dependency>
```

**step2.定义Pojo类，对应数据库中的一张表。**

`@Table`注解，`name`定义pojo类与数据库表的关系，`pk`指定表的主键

```java

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
}    
```

**step3.定义Dao与DaoImpl，分别继承自EntityDao和EntityDaoImpl**

```java
public interface TbUserDao extends EntityDao<TbUser, String> {

}

@Repository
public class TbUserDaoImpl extends EntityDaoImpl<TbUser, String> implements TbUserDao {

}
```

**step4.在Service层注入Dao，使用EntityDao提供的方法完成增、删、改、查**

<u>增</u>

```java
void save(T t)throws Exception;
void batchSave(List<T> list)throws Exception;
int insertWithSql(SQL sql)throws Exception;
```

<u>删</u>

```java
void delete(Id id)throws Exception;
void deleteWithCriteria(Criteria criteria)throws Exception;
void batchDelete(List<Id> ids)throws Exception;
int deleteWithSql(SQL sql)throws Exception;
void truncate()throws Exception;
void drop()throws Exception;
void drunk(SQL sql)throws Exception;
```

<u>改</u>

```java
void update(T t)throws Exception;
void batchUpdate(List<T> list)throws Exception;
int updateWithSql(SQL sql)throws Exception;
```

<u>查</u>

```java
T queryOne(Id id)throws Exception;
List<T> queryAll()throws Exception;
PageResult<T> pageQuery(Page page)throws Exception;
PageResult<T> pageQueryWithCriteria(Page page,Criteria criteria)throws Exception;
List<T> queryWithCriteria(Criteria criteria)throws Exception;
<E> Result<E> queryWithSql(Class<E> clss,SQL sql)throws Exception;
List<Map<String, Object>>queryMapsWithSql(SQL sql)throws Exception;
<K, V> Map<K, V> queryMapWithSql(SQL sql,ResultSetExtractor<Map<K, V>>resultSetExtractor)throws Exception;

```

`Criteria语法示例:`

```java
//where name = 'zhouning'
new Criteria().where(TbUser::getName,"zhouning").andIfAbsent(TbUser::getName,null);
//where name in ('zhouning','yinhw')
new Criteria().in(TbUser::getName,Arrays.asList("zhouning","yinhw"));
//where age < 28 order by age desc
new Criteria().lt(TbUser::getAge,28).orderBy(new Sort(TbUser::getAge);
//where age < 20 and (name like '%zhou%' or realName like 'zhouning')
new Criteria().lt(TbUser::getAge,20).andCriteria(new Criteria().like(TbUser::getName,"zhou").orLike(TbUser::getRealName,"周"));
```

`SQL语法示例:`

```java
new SQL().select(TbUser::getName,TbUser::getEmail,TbUser::getRealName,TbUser::getMobile).from(TbUser.class).where(TbUser::getIsActive,1);
new SQL().select("age",countAs("age").as("num")).from(TbUser.class).orderBy(new Sort(TbUser::getAge)).groupBy(TbUser::getAge);
new SQL().update(TbUser.class).set(TbUser::getRealName,"元林").set(TbUser::getEmail,"13888888888@163.com").where(TbUser::getName,"Smith");
new SQL().insertInto(TbAccount.class,"userName","realName").values("test","测试").values("test2","测试2");
new SQL().delete().from(TbUser.class).gt(TbUser::getAge,20);
//类似MongoTemplate的Criteria拼接
new SQL().select("*").from("table1").where("f1",1).and(Where.where("f2").like("a").or("f3").gte(1).and("f4").in(Arrays.asList(2,3,4)))
//类似JPA的Predict使用
List<WhereParam> params=new ArrayList<>();
params.add(WhereParam.where("f1").like("v1"));
params.add(WhereParam.where("f2").in(Arrays.asList(1,2,3)));
sql=new SQL().select("*").from("table2").and(Opt.AND,params);
```

#### SQLInterceptor.java
最终拦截的方法的签名为entityDao.xxxSql，即方法参数传入的是SQL，beforeBuild在构建sql和参数之前执行，而afterSql在构建sql之后执行。可以通过实现该接口方便的给sql批量添加一些通用的查询字段、更新字段sql审计的逻辑，以下是一个具体点的demo
```java
@Component
public class SQLInterceptorImpl implements SQLInterceptor {

    @Override
    public void beforeBuild(SQLType sqlType, SqlModifier sqlModifier) throws Exception {
        //粗粒度 通过sql类型统一添加需要更新和插入的字段
        if (sqlType.equals(SQLType.Update)) {
            if (sqlModifier.tableName().startsWith("sys_tb_")) {
                sqlModifier.addUpdate("updateTime", new Date());
                sqlModifier.addUpdate("updateUser", "admin");
            }
        } else if (sqlType.equals(SQLType.Insert)) {
            if (sqlModifier.tableName().startsWith("sys_tb_")) {
                sqlModifier.addInsert("createTime", new Date());
                sqlModifier.addInsert("createUser", "admin");
                sqlModifier.addInsert("updateTime", new Date());
                sqlModifier.addInsert("updateUser", "admin");
            }
        }

        //精细粒度  根据sqlId添加相应的更新字段和查询条件
        if (sqlModifier.sqlId().equals("updateBirthAuto")) {
            sqlModifier.addUpdate("isActive", 0);
        } else if (sqlModifier.sqlId().equals("isDelete1")) {
            sqlModifier.addAnd(Where.where("isActive").equal(1));
        }
    }

    @Override
    public void afterBuild(String sql, Object[] args) throws Exception {
        //sql审计
        System.out.println("sql:" + sql + " args:" + ArrayUtils.toString(args));
    }
}
```

#### sql语法

#### https://github.com/hope-for/GyJdbc/blob/master/src/test/java/com/gysoft/jdbc/CriteriaTest.java

#### 集成测试

#### https://github.com/SpringStudent/GyJdbcTest

#### 多数据源支持

开启AOP和引入切面增强类

```java
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(BindPointAspectRegistar.class)
public class SystemApp {

	public static void main(String[] args) {
		SpringApplication.run(SystemApp.class, args);
	}

}
```

通过spring的xml配置

```xml

<bean id="sourceDs" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
    ...省略
</bean>

<bean id="targetDs" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
...省略
</bean>

<bean id="nightDs" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
...省略
</bean>

<bean id="dataSource" class="com.gysoft.jdbc.multi.JdbcRoutingDataSource">
<property name="targetDataSources">
    <map>
        <entry key="master" value-ref="sourceDs"/>
        <entry key="slave" value-ref="targetDs"/>
        <entry key="slave2" value-ref="nightDs"/>
    </map>
</property>
<property name="dataSourceKeysGroup">
    <map>
        <entry key="masterGroup" value="master"/>
        <entry key="slaveGroup" value="slave,slave2"/>
    </map>
</property>
<property name="defaultLookUpKey" value="master"/>
</bean>

<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
<property name="dataSource" ref="dataSource"/>
</bean>
```
通过spring的bean配置

```java
@Configuration
public class DatasourceConf {
    @Bean(name = "primary")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public HikariDataSource primary() {
        return new HikariDataSource();
    }

    @Bean(name = "secondry")
    @ConfigurationProperties(prefix = "spring.datasource.secondry")
    public HikariDataSource secondry() {
        return new HikariDataSource();
    }

    @Bean(name = "thrid")
    @ConfigurationProperties(prefix = "spring.datasource.thrid")
    public HikariDataSource thrid() {
        return new HikariDataSource();
    }

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        JdbcRoutingDataSource jdbcRoutingDataSource = new JdbcRoutingDataSource();
        jdbcRoutingDataSource.setDefaultLookUpKey("primary");

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("primary", primary());
        targetDataSources.put("secondry", secondry());
        targetDataSources.put("third", thrid());
        jdbcRoutingDataSource.setTargetDataSources(targetDataSources);
        //配置分组用于负载均衡，如果无需负载均衡则可忽略
        Map<String, String> dataSourceKeysGroup = new HashMap<>();
        dataSourceKeysGroup.put("master","primary");
        dataSourceKeysGroup.put("slave","secondry,thrid");
        jdbcRoutingDataSource.setDataSourceKeysGroup(dataSourceKeysGroup);
        return jdbcRoutingDataSource;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}
```
**@Bindpoint注解绑定数据源**

<u>绑定方法或者类级别的数据源,依赖于Spring Aop</u>

```java
//绑定数据源slaveGroup组，采用RandomLoadBalance策略（随机）的负载均衡策略选取数据源
@BindPoint(group = "slaveGroup", loadBalance = RandomLoadBalance.class)
//绑定指定slave2数据源
@BindPoint(key = "slave2")
```

**EntityDao.binxxx方法绑定数据源**

<u>绑定Sql级别的数据源</u>

```java
//SELECT * FROM tb_user where name in('zhouning','yinhw')将会在slave数据源上执行
 List<TbUser> tbUsers=tbUserDao.bindKey("slave").queryWithCriteria(new Criteria().in(TbUser::getName,Arrays.asList("zhouning","yinhw")));
//UPDATE tb_user set realName = "元林",email = "13888888888@163.com" WHERE name = "Smith"
//采用轮询负载均衡策略在masterGroup组中选择一个数据源执行update操作
tbUserDao.bindGroup("masterGroup",RoundbinLoadBalance.class).updateWithSql(new SQL().update(TbUser.class).set(TbUser::getRealName,"元林").set(TbUser::getEmail,"13888888888@163.com").where(TbUser::getName,"Smith"));
```

**FAQ**

<u>数据源选择的优先级顺序：</u>

*entityDao.bindXxx* > *方法上@BindPoint* > *类上@BindPoint* > *JdbcRoutingDataSource.defaultLookUpKey*

