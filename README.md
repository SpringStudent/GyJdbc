### 让天下没有难写的sql
像使用mongotemplate一样拼接sql。使用jdbcTemplate不想写sql?写XXXDao和XXXDaoImpl很麻烦?sql拼错查找问题浪费时间?通过使用GyJdbc这些问题将迎刃而解。

#### 运行环境
- jdk1.8+
- mysql

#### 如何使用

下面以tb_user的增删改查进行演示
```
CREATE TABLE `tb_user` (
  `id` varchar(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `pwd` varchar(32) NOT NULL,
  `email` varchar(20) NOT NULL,
  `birth` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
```

**step1.添加pom.xml**
```
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>GyJdbc</artifactId>
    <version>最新RLEASE版本</version>
</dependency>
```

**step2.创建tb_user对应pojo类**

```
/**
 * @author 周宁
 * @Date 2018-09-21 15:11
 */
@Table(name = "tb_user",pk = "id")
@Data
public class TbUser {
    private Integer id;
    private String name;
    private String pwd;
    private String email;
    private Date birth;
}
```

**step3.编写TbUserDao和TbUserDaoImpl分别继承EntityDao和EntityDaoImpl**

```
/**
 * @author 周宁
 * @Date 2018-09-21 15:13
 */

public interface TbUserDao extends EntityDao<TbUser,Integer> {
}
```


```
/**
 * @author 周宁
 * @Date 2018-09-21 15:14
 */
@Repository
public class TbUserDaoImpl extends EntityDaoImpl<TbUser,Integer> implements TbUserDao {
}
```
**step4.使用criteria优雅的编写sql**

```
        //查询所有用户
        //select * from tb_user
        List<TbUser> tbUsers = tbUserDao.queryAll();
        //根据主键查询一个用户
        //select * from tb_user where id = id
        TbUser tbUser = tbUserDao.queryOne(id);
        //根据用户名查询一个用户 
        //select * from tb_user where name = name
        TbUser tbUser = tbUserDao.queryOne(new Criteria().where("name",name));
        //根据用户名批量查询用户
        //select * from tb_user where name in(names)
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().in("name",names));
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().where("name","in",names));
        //根据用户名和邮箱查询 
        //select * from tb_user where name in(names) and email = email
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().in("name",names).and("email",email));
        //根据用户名模糊匹配
        //select * from tb_user where name like %name%
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().like("name",name));
        //select * from tb_user where name like %name
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().where("name","like","%"+name));
        //用户名或者email满足条件
        //select * from tb_user where name = name or email = email
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().and("name",name).or("email",email));
        //id在ids列表+用户名和email同时满足条件结果集
        //select * from tb_user where id in (ids) and (name = name and email = email)
        List<TbUser> tbUsers = tbUserDao.queryWithCriteria(new Criteria().in("id",ids).andCriteria(new Criteria().and("name",name).and("email",email)));
        //查询总人数
        //select count(*) from tb_user
        Integer userCount = tbUserDao.queryIntegerWithCriteria(new Criteria().select("count(*)"));
        //查询每个邮箱的注册的人数
        //select count(*) as emailNum email from tb_user group by email
        Map<String,Object> emailNumMap = tbUserDao.queryMapsWithCriteria(new Criteria().select("count(*) as emailNum","email").groupBy("email"),CustomResultSetExractorFactory.createDoubleColumnValueResultSetExractor());
        //分页查询
        //select * from tb_user limit ?,?
        PageResult<TbUser> tbUserPageResult = tbUserDao.pageQuery(page);
        //select * from tb_user where birth < birth limit ?,?
        PageResult<TbUser> tbUserPageResult = tbUserDao.pageQueryWithCriteria(page,new Criteria().lt("birth",birth));
        //删除用户
        //delete from tb_user where id = id
        tbUserDao.delete(id);
        //条件删除用户
        //delete from tb_user where name in(names)
        tbUserDao.deleteWithCriteria(new Criteria().where("name","in",names));
        //更新一个用户
        //update tb_user set name = tbUser.name,email = tbUser.email,pwd = tbUser.pwd,birth=tbUser.birth where id = tbUser.id
        tbUserDao.update(tbUser);
        //批量更新用户
        tbUserDao.batchUpdate(tbUsers);
```

## 版本更新
### v1.1.0
支持使用Lambda表达式拼接sql

```
criteria.where("epid",1000)->criteria.where(UserBasicInfo::getEpid,1000);
```

### v1.2.0
支持按条件更新部分字段


```
tbUserDao.updateWithCriteria(new Criteria().update(SimpleUser::getEmail,"33@qq.com").update(SimpleUser::getBirth,new Date()).where(SimpleUser::getName,"zhouning"));
```

### V3.0.0
支持join查询


```
unitDao.joinQuery(UnitInfoBrief.class, new Criteria().in("a.id", unitIds).select("a.id AS unitId,a.unitName,b.id AS unitAttrId,b.`name`").as("a").leftJoin(new Joins().with(UnitAttr.class).as("b")
                .on("a.unitAttrId", "b.id"))).queryList()
```

### V3.1.0
支持lambda条件判断

```
 if(EmptyUtils.isEmpty(searchKey)){criteria.and(User::getName,searchkey)}->criteria.andIfAbsent(User::getName,searchkey)
 
```

### V3.2.0
支持子查询(同级子查询使用UNION ALL连接)

```
Criteria criteria = new Criteria();
Criteria criteria1 = new Criteria().select("id,-1 as type,tagName as folderName,createTime,updateTime,createUser,updateUser")
.from(DataDocTag.class).where(DataDocTag::getProjectId, projectId).and(DataDocTag::getParentId, folderId)
.likeIfAbsent(DataDocTag::getTagName, searchKey).gtIfAbsent(DataDocTag::getUpdateTime, startDate)
.letIfAbsent(DataDocTag::getUpdateTime, endDate);
Criteria criteria2 = new Criteria().select("id,type,fileName as folderName,createTime,updateTime,createUser,updateUser")
        .from(DataDocument.class).where(DataDocument::getTagId, folderId).and(DataDocument::getProjectId, projectId)
        .likeIfAbsent(DataDocument::getFileName, searchKey).gtIfAbsent(DataDocument::getUpdateTime, startDate)
        .letIfAbsent(DataDocument::getUpdateTime, endDate).and(DataDocument::getType, type);
criteria.select("*").from(criteria1, criteria2).as("result").orderBy(new Sort("result.updateTime"));
return dataDocTagDao.subQuery(AppFolderInfo.class, criteria).pageQuery(page);
```

### V4.0.0
支持函数拼接


```
//支持mysql函数拼接
//聚集函数
Criteria criteria = new Criteria().select(count("*"),avg(Token::getSize),max(Token::getSize),min(Token::getSize),sum(Token::getSize)).from(Token.class);
//字符串处理函数
Criteria criteria2 = new Criteria().select(concat(Token::getTk,Token::getSize),length(Token::getTk),charLength(Token::getTk),upper(Token::getTk),lower(Token::getTk)).from(Token.class);
//数值处理函数
Criteria criteria3 = new Criteria().select(abs(Token::getSize),ceil(Token::getSize),floor(Token::getSize)).from(Token.class);
//时间处理函数
Criteria criteria4 = new Criteria().select(curdate(),curtime(),now(),month(curdate()),week(curdate()),minute(curtime()));
Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder());
System.out.println(pair.getFirst());
Pair<String, Object[]> pair2 = SqlMakeTools.doCriteria(criteria2, new StringBuilder());
System.out.println(pair2.getFirst());
Pair<String, Object[]> pair3 = SqlMakeTools.doCriteria(criteria3, new StringBuilder());
System.out.println(pair3.getFirst());
Pair<String, Object[]> pair4 = SqlMakeTools.doCriteria(criteria4, new StringBuilder());
System.out.println(pair4.getFirst());
//...more 等着你完善和探索...

SELECT COUNT(*), AVG(size), MAX(size), MIN(size), SUM(size) FROM tb_token
SELECT CONCAT(tk,size), LENGTH(tk), CHAR_LENGTH(tk), UPPER(tk), LOWER(tk) FROM tb_token
SELECT ABS(size), CEIL(size), FLOOR(size) FROM tb_token
SELECT CURDATE(), CURTIME(), NOW(), MONTH(CURDATE()), WEEK(CURDATE()), MINUTE(CURTIME()) FROM 
```

### V4.1.0
- 修复了一些BUG
- Result新增方法queryForList,查询基本类型的List列表

### V4.2.0
- 子查询递归优化

### v4.3.0
- 添加having语句支持
- 添加betweenAnd支持
- 添加orBetweenAnd支持
- 添加日期处理函数DATE_FORMAT、FORMAT函数支持

### v4.4.0
- 添加更多的常用函数支持

### v5.0.0
- 添加union和unionAll的支持
```
 Criteria criteria =new Criteria().select("u1.*").from(Test.class).where("u1.id",123).union().select("u2.*").from(Test.class)
        .unionAll().select("u3.*").from(Book.class).where("u3",123).leftJoin(new Joins().with(Test.class)
                .as("u31").on("u31.id","u3.id").and("u31.nmm","=","nmmm"));
 Criteria criteria1 = new Criteria().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name"));
 Criteria subCriteria = new Criteria().select("res.*").from(criteria,criteria1).where("res.name", "book1").orderBy(new Sort("res.name"));
```    