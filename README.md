### 让天下没有难写的sql
像使用mongotemplate一样拼接sql。使用jdbcTemplate不想写sql?写XXXDao和XXXDaoImpl很麻烦?sql拼错查找问题浪费时间?通过使用GyJdbc这些问题将迎刃而解。

### 参与项目
作者水平有限，很多出代码写的较烂，欢迎提PR优化代码或者对BUG进行修改

#### 运行环境
- jdk1.8+
- mysql
- lombok

### 如何使用
Demo: https://github.com/SpringStudent/GyJdbcTest


```
@Test
    public void testQuery() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        List<TbUser> result1 = tbUserDao.queryAll();
        TbUser result2 = tbUserDao.queryOne(result1.get(0).getId());
        System.out.println("queryAll():" + result1);
        System.out.println("queryOne:" + result2);
    }
```


```
@Test
    public void testQueryWithCriteria() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        //根据用户名查询:SELECT * FROM tb_user where name = 'zhouning'
        TbUser tbUser = tbUserDao.queryOne(new Criteria().where(TbUser::getName, "zhouning"));
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
        System.out.println("queryWithCriteria:"+tbUsers4);
    }
```


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
        //根据SQL更新某个用户:UPDATE tb_user SET realName = '李森',email='1388888888@163.com' where name = 'Smith'
        tbUserDao.updateWithSql(new SQL().update(TbUser::getRealName, "李森").update(TbUser::getEmail, "13888888888@163.com").where(TbUser::getName, "Smith"));
    }
```


```
 @Test
    public void testUseSQL() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        //SELECT name, email, realName, mobile FROM tb_user WHERE isActive = 1
        SQL sql = new SQL().select(TbUser::getName, TbUser::getEmail, TbUser::getRealName, TbUser::getMobile)
                .from(TbUser.class).where(TbUser::getIsActive, 1);
        List<SimpleUser> simpleUsers = tbUserDao.queryWithSql(SimpleUser.class,sql).queryList();
        System.out.println("queryWithSql:"+ simpleUsers);
        //SELECT name, email, realName, mobile FROM tb_user WHERE isActive = 1 limit 0,2
        SQL sql2 = new SQL().select(TbUser::getName, TbUser::getEmail, TbUser::getRealName, TbUser::getMobile)
                .from(TbUser.class).where(TbUser::getIsActive, 1);
        PageResult<SimpleUser> simpleUsers2 = tbUserDao.queryWithSql(SimpleUser.class,sql2).pageQuery(new Page(1,2));
        System.out.println("queryWithSql:"+ simpleUsers2);
        //SELECT count(id) from tb_user
        SQL sql3 = new SQL().select(count(TbUser::getId)).from(TbUser.class);
        Integer count = tbUserDao.queryIntegerWithSql(sql3);
        Integer count2 = tbUserDao.queryWithSql(Integer.class,sql3).queryObject();
        System.out.println("queryIntegerWithSql:"+count);
        System.out.println("queryWithSql:"+count2);
        //SELECT age, COUNT(age) AS num FROM tb_user GROUP BY age ORDER BY age DESC
        SQL sql4 = new SQL().select("age",countAs("age").as("num")).from(TbUser.class).orderBy(new Sort(TbUser::getAge)).groupBy(TbUser::getAge);
        Map<Integer,Integer> map = tbUserDao.queryMapWithSql(sql4,CustomResultSetExractorFactory.createDoubleColumnValueResultSetExractor());
        System.out.println("queryMapWithSql:"+map);
        //SELECT DISTINCT(career) FROM tb_user
        SQL sql5 = new SQL().select(distinct(TbUser::getCareer)).from(TbUser.class);
        List<String> careers = tbUserDao.queryWithSql(String.class,sql5).queryForList();
        System.out.println("queryWithSql"+careers);
        //SELECT t1.name,t1.realName,t2.id,t2.roleName FROM tb_user AS t1 LEFT JOIN tb_role AS t2  ON t1.roleId = t2.id  WHERE t1.age > ?
        SQL sql6 = new SQL().select("t1.name,t1.realName,t2.id,t2.roleName").from(TbUser.class)
                .as("t1").leftJoin(new Joins().with(TbRole.class).as("t2").on("t1.roleId","t2.id"))
                .where("t1.age",">",24);
        List<UserRole> userRoles = tbUserDao.queryWithSql(UserRole.class,sql6).queryList();
        System.out.println("queryWithSql:"+userRoles);
        //以下SQL仅仅用来演示SQL功能，工作中切勿这么些
        //SELECT roleId FROM( SELECT DISTINCT(t.roleId) AS roleId FROM tb_user AS t UNION ALL SELECT DISTINCT(t1.roleId) AS roleId FROM tb_user AS t1)  AS t2
        SQL sql7 = new SQL().select("roleId").from(new SQL().select(distinctAs("t.roleId").as("roleId")).from(TbUser.class).as("t")
                                                  ,new SQL().select(distinctAs("t1.roleId").as("roleId")).from(TbUser.class).as("t1")).as("t2");
        List<Integer> inUseRoleId = tbUserDao.queryWithSql(Integer.class,sql7).queryForList();
        System.out.println("queryWithSql:"+inUseRoleId);
        //SELECT t1.name,t1.realName,t2.id,t2.roleName FROM tb_user AS t1 LEFT JOIN tb_role AS t2  ON t1.roleId = t2.id  WHERE t1.age > 24
        // UNION
        // SELECT t3.name,t3.realName,t4.id,t4.roleName FROM tb_user AS t3 LEFT JOIN tb_role AS t4  ON t3.roleId = t4.id  WHERE t3.career IN('JAVA')
        SQL sql8 =new SQL().select("t1.name,t1.realName,t2.id,t2.roleName").from(TbUser.class)
                .as("t1").leftJoin(new Joins().with(TbRole.class).as("t2").on("t1.roleId","t2.id"))
                .where("t1.age",">",24).union().select("t3.name,t3.realName,t4.id,t4.roleName").from(TbUser.class)
                .as("t3").leftJoin(new Joins().with(TbRole.class).as("t4").on("t3.roleId","t4.id"))
                .in("t3.career",Arrays.asList("JAVA"));
        List<UserRole> userRoles2 = tbUserDao.queryWithSql(UserRole.class,sql8).queryList();
        System.out.println(userRoles2);
        //more ...............................
    }
```


```
@After
    public void testDelete() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        tbUserDao.delete("0");
        tbUserDao.batchDelete(tbUserDao.queryAll().stream().map(TbUser::getId).collect(Collectors.toList()));
    }
```

```
@Test
    public void testInsertWithSql() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        TbUserDao tbUserDao = (TbUserDao) ac.getBean("tbUserDao");
        SQL sql = new SQL().insertInto(TbUser.class, "id", "name", "realName", "pwd", "email", "mobile", "age", "birth", "roleId", "career", "isActive")
                .values(1, "ins1", "插入1", "123456", "345@qq.com", "12345678901", 25, new Date(), 1, "测试", 1)
                .values(2, "ins2", "插入2", "123456", "456@qq.com", "12345678901", 26, new Date(), 1, "测试", 1)
                .values(3, "ins3", "插入3", "123456", "567@qq.com", "12345678901", 27, new Date(), 1, "测试", 0);
        SQL sql2 = new SQL().insertInto(TbAccount.class, "userName", "realName")
                .select("name", "realName").from(TbUser.class);
        SQL sql3 = new SQL().insertInto(TbAccount.class, TbAccount::getUserName, TbAccount::getRealName)
                        .select("name", "realName").from(TbUser.class);
        tbUserDao.insertWithSql(sql);
        tbUserDao.insertWithSql(sql2);
        tbUserDao.insertWithSql(sql3);
    }
```
### 版本更新
- 10.1.0 修复union查询和子查询的sql无大括号导致报错bug
- 10.2.0 修复无selectFields sql拼接的一处BUG
- 11.0.0 自定义sql插入语句支持
- 11.1.1 自定义sql支持lambda表达式
- 12.0.0 自定义sql插入语句支持调整
### 当前版本12.0.0
