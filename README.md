## GyJdbc

基于jdbctemplate的类似JPA的ORM框架，使用Gyjdbc的优势：

1. **Dao层0代码，再也不需要为Dao层的方法名称命名掉头发。**
2. **链式SQL拼接与查询，配合lambda表达式，既装X又简洁。**
3. **强悍的SQL拼接，支持作者已知的所有SQL语法。**
4. **学习成本极低，靠近SQL语法，开发者使用起来会像平时一样写SQL一样简单。**
5. **支持多数据源，多数据源的负载均衡，仅需一个注解或者一个方法调用。**

### 谁在用
一家不知名的创业公司
#### 快速开始

**step1.添加maven坐标**

```xml
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>GyJdbc</artifactId>
    <version>{最新版本}</version>
</dependency>
```

**step2.定义Pojo类，对应数据库中的一张表。**

`@Table`注解，`name`定义pojo类与数据库表的关系，`pk`指定表的主键

```java
@Table(name = "tb_user",pk = "id")
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
public interface TbUserDao extends EntityDao<TbUser,String> {
    
}
@Repository
public class TbUserDaoImpl extends EntityDaoImpl<TbUser,String> implements TbUserDao {
    
}
```

**step4.在Service层注入Dao，使用EntityDao提供的方法完成增、删、改、查**

<u>增</u>

```java
void save(T t) throws Exception ;
void batchSave(List<T> list) throws Exception ;
int insertWithSql(SQL sql)throws Exception;
```

<u>删</u>

```java
void delete(Id id) throws Exception ;
void deleteWithCriteria(Criteria criteria) throws Exception;
void batchDelete(List<Id> ids) throws Exception ;
int deleteWithSql(SQL sql)throws Exception;
void truncate()throws Exception;
void drop()throws Exception;
void drunk(SQL sql)throws Exception;
```

<u>改</u>

```java
void update(T t) throws Exception ;
void batchUpdate(List<T> list) throws Exception ;
int updateWithSql(SQL sql)throws Exception;
```

<u>查</u>

```java
T queryOne(Id id) throws Exception ;
List<T> queryAll() throws Exception ;
PageResult<T> pageQuery(Page page) throws Exception;
PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception;
List<T> queryWithCriteria(Criteria criteria) throws Exception;
<E> Result<E> queryWithSql(Class<E> clss,SQL sql)throws Exception;
List<Map<String,Object>> queryMapsWithSql(SQL sql)throws Exception;
<K,V> Map<K,V> queryMapWithSql(SQL sql,ResultSetExtractor<Map<K,V>> resultSetExtractor)throws Exception;

```

`Criteria语法示例:`

```java
new Criteria().where(TbUser::getName, "zhouning").andIfAbsent(TbUser::getName, null);
new Criteria().in(TbUser::getName, Arrays.asList("zhouning", "yinhw"));
new Criteria().lt(TbUser::getAge, 28).orderBy(new Sort(TbUser::getAge)
```

`SQL语法示例:`

```java
new SQL().select(TbUser::getName, TbUser::getEmail, TbUser::getRealName,TbUser::getMobile).from(TbUser.class).where(TbUser::getIsActive, 1);
new SQL().select("age", countAs("age").as("num")).from(TbUser.class).orderBy(new Sort(TbUser::getAge)).groupBy(TbUser::getAge);
new SQL().update(TbUser.class).set(TbUser::getRealName, "元林").set(TbUser::getEmail, "13888888888@163.com").where(TbUser::getName, "Smith");
new SQL().delete("t1").from(TbAccount.class).innerJoin(
new Joins().with(TbUser.class).as("t2").on("t1.userName", "t2.name");
new SQL().insert_into(TbAccount.class, "userName", "realName").values("test", "测试")
.values("test2", "测试2")
```

#### 更多用法见

#### https://github.com/SpringStudent/GyJdbcTest 

#### 多数据源支持

**数据源配置文件**

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
    <property name="defaultLookUpKey" value="master"/></bean>

<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">    
    <property name="dataSource" ref="dataSource"/>
</bean>
```

**@Bindpoint注解绑定数据源**

<u>绑定方法或者类级别的数据源</u>

```java
//绑定数据源slaveGroup组，采用RandomLoadBalance策略（随机）的负载均衡策略选取数据源
@BindPoint(group = "slaveGroup",loadBalance = RandomLoadBalance.class)
//绑定指定slave2数据源
@BindPoint(key = "slave2")
```

**EntityDao.binxxx方法绑定数据源**

<u>绑定Sql级别的数据源</u>

```java
//SELECT * FROM tb_user where name in('zhouning','yinhw')将会在slave数据源上执行
 List<TbUser> tbUsers = tbUserDao.bindKey("slave").queryWithCriteria(new Criteria().in(TbUser::getName, Arrays.asList("zhouning", "yinhw")));

//UPDATE tb_user set realName = "元林",email = "13888888888@163.com" WHERE name = "Smith"
//采用轮询负载均衡策略在masterGroup组中选择一个数据源执行update操作
tbUserDao.bindGroup("masterGroup",RoundbinLoadBalance.class).updateWithSql(new SQL().update(TbUser.class).set(TbUser::getRealName, "元林").set(TbUser::getEmail, "13888888888@163.com").where(TbUser::getName, "Smith"));
```

**FAQ**

<u>数据源选择的优先级顺序：</u>

*entityDao.bindXxx* > *方法上@BindPoint* > *类上@BindPoint* > *JdbcRoutingDataSource.defaultLookUpKey*