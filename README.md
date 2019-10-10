### 让天下没有难写的sql
像使用mongotemplate一样拼接sql。使用jdbcTemplate不想写sql?写XXXDao和XXXDaoImpl很麻烦?sql拼错查找问题浪费时间?通过使用GyJdbc这些问题将迎刃而解。

### 参与项目
作者水平有限，很多出代码写的较烂，欢迎提PR优化代码或者对BUG进行修改

#### 运行环境
- jdk1.8+
- mysql

### 如何使用
Demo: https://github.com/SpringStudent/GyJdbcTest

#### 条件查询
```
@Test
    public void testQueryWithCriteria() throws Exception {
         ApplicationContext ac = new  ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        //根据用户名查询:SELECT * FROM tb_user where name = 'zhouning'
        TbUser tbUser = tbUserDao.queryOne(new Criteria().where(TbUser::getName, "zhouning").andIfAbsent(TbUser::getName, null));
        System.out.println("queryOne:" + tbUser);
        //根据用户名批量查询:SELECT * FROM tb_user where name in('zhouning','yinhw');
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().in(TbUser::getName, Arrays.asList("zhouning", "yinhw")));
        System.out.println("queryWithCriteria:" + tbUsers);
        //根据关键字和年龄模糊查询:SELECT * FROM tb_user where age > 26 and (realName like '%l%' or name like '%l%')
        String searchKey = "l";
        List<TbUser> tbUsers2 = tbUserDao.queryWithCriteria(new Criteria().gt(TbUser::getAge, 26).andCriteria(new Criteria().like(TbUser::getRealName, searchKey).or(TbUser::getName, "like", "%" + searchKey + "%")));
        System.out.println("queryWithCriteria:" + tbUsers2);
        //根据关键字搜索，关键字空或者null则不传:SELECT * FROM tb_user
        searchKey = "";
        List<TbUser> tbUsers3 = tbUserDao.queryWithCriteria(new Criteria().likeIfAbsent(TbUser::getName, searchKey));
        System.out.println("queryWithCriteria:" + tbUsers3);
        //分页查询:SELECT * FROM tb_user LIMIT 0,2
        PageResult<TbUser> pageResult = tbUserDao.pageQuery(new Page(1, 2));
        System.out.println("pageQuery:" + pageResult);
        //分页条件查询:SELECT * FROM tb_user WHERE age < 28 LIMIT 0,2
        PageResult<TbUser> pageResult2 = tbUserDao.pageQueryWithCriteria(new Page(1, 2), new Criteria().lt(TbUser::getAge, 28));
        System.out.println("pageQueryWithCriteria:" + pageResult2);
        //按年龄降序查询用户:SELECT * FROM tb_user ORDER BY age DESC
        List<TbUser> tbUsers4 = tbUserDao.queryWithCriteria(new Criteria().orderBy(new Sort(TbUser::getAge)));
        System.out.println("queryWithCriteria:" + tbUsers4);
    }
```

#### 自定义sql查询
```
 @Test
    public void testUseSQL() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        //SELECT name, email, realName, mobile FROM tb_user WHERE isActive = 1
        SQL sql = new SQL().select(TbUser::getName, TbUser::getEmail, TbUser::getRealName, TbUser::getMobile)
                .from(TbUser.class).where(TbUser::getIsActive, 1);
        List<SimpleUser> simpleUsers = tbUserDao.queryWithSql(SimpleUser.class, sql).queryList();
        System.out.println("queryWithSql:" + simpleUsers);
        //SELECT name, email, realName, mobile FROM tb_user WHERE isActive = 1 limit 0,2
        SQL sql2 = new SQL().select(TbUser::getName, TbUser::getEmail, TbUser::getRealName, TbUser::getMobile)
                .from(TbUser.class).where(TbUser::getIsActive, 1);
        PageResult<SimpleUser> simpleUsers2 = tbUserDao.queryWithSql(SimpleUser.class, sql2).pageQuery(new Page(1, 2));
        System.out.println("queryWithSql:" + simpleUsers2);
        //SELECT count(id) from tb_user
        SQL sql3 = new SQL().select(count(TbUser::getId)).from(TbUser.class);
        Integer count = tbUserDao.queryIntegerWithSql(sql3);
        Integer count2 = tbUserDao.queryWithSql(Integer.class, sql3).queryObject();
        System.out.println("queryIntegerWithSql:" + count);
        System.out.println("queryWithSql:" + count2);
        //SELECT age, COUNT(age) AS num FROM tb_user GROUP BY age ORDER BY age DESC
        SQL sql4 = new SQL().select("age", countAs("age").as("num")).from(TbUser.class).orderBy(new Sort(TbUser::getAge)).groupBy(TbUser::getAge);
        Map<Integer, Integer> map = tbUserDao.queryMapWithSql(sql4, CustomResultSetExractorFactory.createDoubleColumnValueResultSetExractor());
        System.out.println("queryMapWithSql:" + map);
        //SELECT DISTINCT(career) FROM tb_user
        SQL sql5 = new SQL().select(distinct(TbUser::getCareer)).from(TbUser.class);
        List<String> careers = tbUserDao.queryWithSql(String.class, sql5).queryForList();
        System.out.println("queryWithSql" + careers);
        //SELECT t1.name,t1.realName,t2.id,t2.roleName FROM tb_user t1 LEFT JOIN tb_role t2  ON t1.roleId = t2.id  WHERE t1.age > ?
        SQL sql6 = new SQL().select("t1.name,t1.realName,t2.id as roleId,t2.roleName").from(TbUser.class)
                .as("t1").leftJoin(new Joins().with(TbRole.class).as("t2").on("t1.roleId", "t2.id"))
                .where("t1.age", ">", 24);
        List<UserRole> userRoles = tbUserDao.queryWithSql(UserRole.class, sql6).queryList();
        System.out.println("queryWithSql:" + userRoles);
        //以下SQL仅仅用来演示SQL功能
        //SELECT roleId FROM( (SELECT DISTINCT(t.roleId) AS roleId FROM tb_user t) UNION ALL (SELECT DISTINCT(t1.roleId) AS roleId FROM tb_user t1))  t2
        SQL sql7 = new SQL().select("roleId").from(new SQL().select(distinctAs("t.roleId").as("roleId")).from(TbUser.class).as("t")
                , new SQL().select(distinctAs("t1.roleId").as("roleId")).from(TbUser.class).as("t1")).as("t2");
        List<Integer> inUseRoleId = tbUserDao.queryWithSql(Integer.class, sql7).queryForList();
        System.out.println("queryWithSql:" + inUseRoleId);
        //(SELECT t1.name,t1.realName,t2.id,t2.roleName FROM tb_user t1 LEFT JOIN tb_role t2  ON t1.roleId = t2.id  WHERE t1.age > 24)
        // UNION
        //(SELECT t3.name,t3.realName,t4.id,t4.roleName FROM tb_user t3 LEFT JOIN tb_role t4  ON t3.roleId = t4.id  WHERE t3.career IN('JAVA'))
        SQL sql8 = new SQL().select("t1.name,t1.realName,t2.id as roleId,t2.roleName").from(TbUser.class)
                .as("t1").leftJoin(new Joins().with(TbRole.class).as("t2").on("t1.roleId", "t2.id"))
                .where("t1.age", ">", 24).union().select("t3.name,t3.realName,t4.id,t4.roleName").from(TbUser.class)
                .as("t3").leftJoin(new Joins().with(TbRole.class).as("t4").on("t3.roleId", "t4.id"))
                .in("t3.career", Arrays.asList("JAVA"));
        List<UserRole> userRoles2 = tbUserDao.queryWithSql(UserRole.class, sql8).queryList();
        System.out.println(userRoles2);
        //more ...............................
    }
```

#### sql插入
```
@Test
    public void testInsertWithSql() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbAccountDao tbAccountDao = (TbAccountDao) ac.getBean("tbAccountDao");
        SQL sql = new SQL().insert_into(TbAccount.class, "userName", "realName")
                .values("test", "测试")
                .values("test2", "测试2");
        SQL sql2 = new SQL().insert_into(TbAccount.class, "userName", "realName")
                .select("name", "realName").from(TbUser.class);
        SQL sql3 = new SQL().insert_into(TbAccount.class, TbAccount::getUserName, TbAccount::getRealName)
                .select("name", "realName").from(TbUser.class).gt(TbUser::getIsActive, 0);
        tbAccountDao.insertWithSql(sql);
        tbAccountDao.insertWithSql(sql2);
        tbAccountDao.insertWithSql(sql3);
    }
```
#### 更新数据
```
 @Test
    public void testUpdate() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        //更新一个用户
        TbUser tbUser = tbUserDao.queryOne(new Criteria().and(TbUser::getName, "zhouning"));
        tbUser.setRealName("周宁宁");
        tbUser.setEmail("2267431887@qq.com");
        tbUserDao.update(tbUser);
        //更新全部用户
        List<TbUser> tbUsers = tbUserDao.queryAll();
        for (TbUser tUser : tbUsers) {
            tUser.setIsActive(1);
        }
        tbUserDao.batchUpdate(tbUsers);
        //SQL更新某个用户:UPDATE tb_user SET realName = '李森',email='1388888888@163.com' where name = 'Smith'
        tbUserDao.updateWithSql(new SQL().update(TbUser.class).set(TbUser::getRealName, "元林").set(TbUser::getEmail, "13888888888@163.com").where(TbUser::getName, "Smith"));
        //SQL关联更新:
        TbAccountDao tbAccountDao = (TbAccountDao) ac.getBean("tbAccountDao");
        tbAccountDao.updateWithSql(new SQL().update(TbAccount.class).as("t1").innerJoin(new Joins().with(TbUser.class).as("t2")
                .on("t1.userName", "t2.name")).set("t1.realName", new FieldReference("t2.realName")));
    }
```
#### 删除数据
```
@After
    public void testDelete() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbAccountDao tbAccountDao = (TbAccountDao) ac.getBean("tbAccountDao");
        tbAccountDao.delete(1);
        tbAccountDao.deleteWithCriteria(new Criteria().in("userName", Arrays.asList("test2")));
        tbAccountDao.deleteWithSql(new SQL().delete().from(TbAccount.class).where("userName","Smith"));
        tbAccountDao.deleteWithSql(new SQL().delete("t1").from(TbAccount.class).innerJoin(
                new Joins().with(TbUser.class).as("t2").on("t1.userName", "t2.name")
        ));
    }
```

#### 创建表并插入数据
```
@Test
    public void testCreateTable() throws Exception {
        SQL sql = new SQL().createTable()
                .addColumn().name("id").integer().notNull().autoIncrement().primary().comment("主键").commit()
                .addColumn().name("userName").varchar(50).notNull().comment("账号").commit()
                .addColumn().name("realName").varchar(50).defaultNull().comment("真实名称").commit()
                .engine(TableEngine.MyISAM).comment("账号表2").commit()
                .values(0, "zhouning", "周宁")
                .values(0, "laoning", "老宁")
                .values(0, "daning", "大宁");
//                .select("0,userName,realName").from(TbAccount.class);//支持select语句的插入方法但是会和values的插入方法冲突
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbAccountDao tbAccountDao = (TbAccountDao) ac.getBean("tbAccountDao");
        String tbName = tbAccountDao.createWithSql(sql);
        System.out.println(tbAccountDao.queryWithSql(TbAccount.class, new SQL().select("*").from(tbName)).queryList());
    }
```

#### 使用临时表优化in查询
```
//before
@Override
    public Map<String, String> getProjUserNameCareerMap(List<String> projIds, List<String> userNames) throws Exception {
        List<Object[]> tmpValues = new ArrayList<>();
        if (EmptyUtils.isEmpty(projIds) || EmptyUtils.isEmpty(userNames)) {
            return new HashMap<>();
        }
        return projectUserDao.queryMapWithSql(
                new SQL().select(concat("a.projectId", "a.userName"), "a.career").from(ProjectUser.class).as("a")
                        .in("a.projectId", projIds).in("a.userName", userNames).groupBy("a.projectId","a.userName"), CustomResultSetExractorFactory.createDoubleColumnValueResultSetExractor())
    }
//after
@Override
    public Map<String, String> getProjUserNameCareerMap(List<String> projIds, List<String> userNames) throws Exception {
        List<Object[]> tmpValues = new ArrayList<>();
        if(EmptyUtils.isEmpty(projIds)||EmptyUtils.isEmpty(userNames)){
            return new HashMap<>();
        }
        projIds.forEach(pid -> userNames.forEach(unm -> tmpValues.add(new Object[]{0,pid,unm})));
        return projectUserDao.queryMapWithSql(
                new SQL().select(concat("a.projectId","a.userName"),"a.career").from(ProjectUser.class).as("a")
                        .innerJoin(new Joins().with(
                                projectUserDao.createWithSql(new SQL().createTable().temporary()
                                        .addColumn().name("id").primary().integer().notNull().autoIncrement().commit()
                                        .addColumn().name("projectId").varchar(32).notNull().commit()
                                        .addColumn().name("userName").varchar(50).notNull().commit()
                                        .engine(TableEngine.MyISAM).commit().values(tmpValues))
                        ).as("b").on("a.projectId","b.projectId").on("a.userName","b.userName"))
                        .groupBy("a.projectId","a.userName")
                ,CustomResultSetExractorFactory.createDoubleColumnValueResultSetExractor()
        );
    }    
```

#### 丰富的函数支持,参照FuncBuilder.java
```
@Test
    public void testFunc(){
        //支持mysql函数拼接
        //聚集函数
        SQL s = new SQL().select(count("*"),avg(Token::getSize),max(Token::getSize),min(Token::getSize),sum(Token::getSize)).from(Token.class);
        //字符串处理函数
        SQL s2 = new SQL().select(concat(Token::getTk,Token::getSize),length(Token::getTk),charLength(Token::getTk),upper(Token::getTk),lower(Token::getTk)).from(Token.class);
        //数值处理函数
        SQL s3 = new SQL().select(abs(Token::getSize),ceil(Token::getSize),floor(Token::getSize)).from(Token.class);
        //时间处理函数
        SQL s4 = new SQL().select(curdate(),curtime(),now(),month(curdate()),week(curdate()),minute(curtime()));

        SQL s5 = new SQL().select(formatAs("10000","2").as("a")).from(Book.class);

        Pair<String, Object[]> pair = SqlMakeTools.useSql(s);
        System.out.println(pair.getFirst());
        Pair<String, Object[]> pair2 = SqlMakeTools.useSql(s2);
        System.out.println(pair2.getFirst());
        Pair<String, Object[]> pair3 = SqlMakeTools.useSql(s3);
        System.out.println(pair3.getFirst());
        Pair<String, Object[]> pair4 = SqlMakeTools.useSql(s4);
        System.out.println(pair4.getFirst());
        //...more 等着你完善和探索...
        Pair<String, Object[]> pair5 = SqlMakeTools.useSql(s5);
        System.out.println(pair5.getFirst());
    }
```


#### 其他sql的组装
```
    @Test
    public void testOtherSql() {
        //UPDATE test SET id = ?, name = ? WHERE pid = ?
        SQL sql = new SQL().update("test").set("id", 1).set("name", "asd").where("pid", 15);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //UPDATE test t1
        // INNER JOIN
        // tb_test t2  ON t1.id = t2.id  AND t1.id = ? SET t1.id = ?, t1.id = ? WHERE t1.id IN(?,?)
        sql = new SQL().update("test").as("t1").innerJoin(new Joins().with("tb_test").as("t2")
                .on("t1.id", "t2.id").and("t1.id", "=", 123)).set("t1.id", "t2.pid")
                .set("t1.id", 123).in("t1.id", Arrays.asList("id1", "id2"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE FROM test WHERE id = ?
        sql = new SQL().delete().from("test").where("id", 1);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE t1 FROM tb_table t1
        // INNER JOIN
        //tmp_table t2  ON t2.moduletype  = t1.moduletype  AND t2.unitqdkey = t1.unitqdkey  WHERE t1.unid = ? AND t1.epid = ?
        sql = new SQL().delete("t1").from("tb_table")
                .innerJoin(new Joins().with("tmp_table").as("t2").on("t2.moduletype ", "t1.moduletype")
                        .on("t2.unitqdkey", "t1.unitqdkey")).where("t1.unid", "iiods").and("t1.epid", 9192);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE FROM test WHERE id = ?
        sql = new SQL().delete().from("test").where("id", 1);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //UPDATE test t1 INNER JOIN tb_test t2  ON t1.id = t2.id  AND t1.id = ?
        //INNER JOIN
        //test_tb t3  ON t1.id = t3.id  AND t1.id = ? SET t1.id = t2.pid, t1.id = t3.cid, t1.id = ? WHERE t1.id IN(?,?)
        sql = new SQL().update("test").as("t1")
                .innerJoin(new Joins().with("tb_test").as("t2").on("t1.id", "t2.id").and("t1.id", "=", "id1"))
                .innerJoin(new Joins().with("test_tb").as("t3").on("t1.id", "t3.id").and("t1.id", "=", "id1"))
                .set("t1.id", new FieldReference("t2.pid")).set("t1.id", new FieldReference("t3.cid")).set("t1.id", "id2").in("t1.id", Arrays.asList("id3", "id4"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //UPDATE student s , class c  SET s.class_name = ?, c.stu_name = ? WHERE s.class_id = c.id
        sql = new SQL().update("student").as("s")
                .natureJoin(new Joins().with("class").as("c"))
                .set("s.class_name","test00").set("c.stu_name","test00")
                .where("s.class_id",new FieldReference("c.id"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE orders,items FROM orders,items
        //WHERE orders.userid = items.userid  AND orders.orderid = items.orderid AND orders.date <= ?
        sql = new SQL().delete("orders,items")
                .where("orders.userid",new FieldReference("items.userid "))
                .and("orders.orderid",new FieldReference("items.orderid"))
                .let("orders.date","2000/03/01");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE FROM orders,items
        // WHERE orders.userid = items.userid  AND orders.orderid = items.orderid AND orders.date <= ?
        sql = new SQL().delete().from("orders,items")
                .where("orders.userid",new FieldReference("items.userid "))
                .and("orders.orderid",new FieldReference("items.orderid"))
                .let("orders.date","2000/03/01");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }
```
#### 动态数据源切换
##### 使用须知 方法选择数据源的优先级
```
   entityDao.bindXXX()>@BindPoint()>GyJdbcRoutingDataSource.defaultLookUpKey
```
1. applicationContext的配置

```
 <bean id="sourceDs" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- 数据库基本信息配置 -->
        <property name="driverClassName" value="${mysql.driver}"/>
        <property name="url" value="${source.mysql.url}"/>
        <property name="username" value="${source.mysql.username}"/>
        <property name="password" value="${source.mysql.password}"/>
    </bean>

    <bean id="targetDs" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- 数据库基本信息配置 -->
        <property name="driverClassName" value="${mysql.driver}"/>
        <property name="url" value="${target.mysql.url}"/>
        <property name="username" value="${target.mysql.username}"/>
        <property name="password" value="${target.mysql.password}"/>
    </bean>
    
    <bean id="thirdDs" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- 数据库基本信息配置 -->
        <property name="driverClassName" value="${mysql.driver}"/>
        <property name="url" value="${third.mysql.url}"/>
        <property name="username" value="${third.mysql.username}"/>
        <property name="password" value="${third.mysql.password}"/>
    </bean>
    
    <!-- 配置多数据源的支持对象-->
    <bean id="dataSource" class="com.gysoft.jdbc.multi.GyJdbcRoutingDataSource">
        <property name="targetDataSources">
            <map>
                //此处targetDataSources的entry key如果不是配置的master、
                //slave那么在下文使用dao的bindMaster()、bindSlave()
                //方法会获取不到数据源，这时候可以通过bindPoint(String ds)
                //方法去获取配置的数据源
                <entry key="master" value-ref="sourceDs"/>
                <entry key="slave" value-ref="targetDs"/>
                <entry key="slave2" value-ref="thirdDs"/>
            </map>
        </property>
        //在没有调用tbAccountDao.bindxxx()方法时并且对应的service类的方法
        //上没有@BindPoint注解 此时指定一个默认的数据源的key
        <property name="defaultLookUpKey" value="slave"/>
    </bean>

    <!-- 数据目标jdbcTemplate -->
    <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
        <property name="dataSource" ref="dataSource"/>
    </bean>
```
2.sql级别的数据源切换：在调用方法的时候指定数据源master、slave

```
 @Test
    public void testMasterSlave() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbAccountDao tbAccountDao = (TbAccountDao) ac.getBean("tbAccountDao");
        //没有调用bindxxx()方法数据源为defaultLookUpKey配置的slave
        System.out.println("common query"+tbAccountDao.queryAll());
        //使用master数据源
        System.out.println("Master query"+tbAccountDao.bindMaster().queryAll());
        //使用slave数据源
        System.out.println("Slave query"+tbAccountDao.bindSlave().queryAll());
        //使用slave2数据源
        System.out.println("Slave2 query"+tbAccountDao.bindPoint("slave2").queryAll());
        //使用slave
        System.out.println("common query"+tbAccountDao.queryAll());
    }
```

3.方法级别的数据源切换：通过使用@BindPoint注解
```
@Override
    @BindPoint("master")
    public void bindDataSource() throws Exception {
        System.out.println("common query"+tbAccountDao.queryAll());
        System.out.println("Master query"+tbAccountDao.bindMaster().queryAll());
        System.out.println("Slave query"+tbAccountDao.bindSlave().queryAll());
        System.out.println("Slave2 query"+tbAccountDao.bindPoint("slave2").queryAll());
        System.out.println("common query"+tbAccountDao.queryAll());
    }
```


### 版本更新
- 10.1.0 修复union查询和子查询的sql无大括号导致报错bug
- 10.2.0 修复无selectFields sql拼接的一处BUG
- 11.0.0 自定义sql插入语句支持
- 11.1.1 自定义sql支持lambda表达式
- 12.0.0 自定义sql插入语句支持调整
- 12.1.1 支持拼接limit
- 12.2.0 支持orLike拼接，修复相同WhereParam的条件丢失问题
- 13.0.0 支持创建表并插入数据,配合临时表使用美滋滋
- 13.0.1 修复createWithSql不插入数据的一处bug
- 13.0.2 创建表添加defaultNull
- 13.0.3 join查询忘了支持表名称字符串
- 13.0.4 修复limit查询的bug
- 13.0.5 修复使用lambda表达式Mysql特殊字符未添加``导致的错误
- 13.0.6 修复创建表并插入数据column为mysql特殊字符未添加``导致的报错
- 14.0.0 insertWithSql方法改为分页插入
- 15.0.1 添加了切换数据源的支持
- 15.0.2 解决多数据源方法类型转换丢失
- 15.1.0 使用@BindPoint注解绑定数据源
- 16.0.0 删除需要lombok的支持
- 17.0.0 SQL插入、更新方法重构使其更加符合sql拼写习惯，实现了DELETE、UPDATE操作的连接拼接
- 17.1.0 其他sql拼接的支持
- 17.2.0 join语句支持传递SQL
- 18.0.0 修复union()和unionAll()方法中子查询的bug
- 18.1.0 支持in(key,sql)语法
- 18.2.0 having(Criteria criteria)语法支持
### 当前版本18.2.0
