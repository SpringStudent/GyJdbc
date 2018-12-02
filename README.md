### GyJdbc是什么?
像使用mongotemplate一样拼接sql。使用jdbcTemplate不想写sql?写XXXDao和XXXDaoImpl很麻烦?sql拼错查找问题浪费时间?通过使用GyJdbc这些问题将迎刃而解。
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
    <version>1.2.0</version>
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
**更变态的sql还有?**

```
        Criteria criteria = new Criteria();
        criteria.in("password", Arrays.asList("1234567890", "111111"));
        criteria.andCriteria(new Criteria().and("realName", "like", "%" + "周宁" + "%").or("userName", "in", Arrays.asList("zhou", "he")));
        criteria.orCriteria(new Criteria().where("ppid", "12305").and("special", "TJ"));
        criteria.or("userName", "like", "%" + "zhouning" + "%")
                .andCriteria(new Criteria().and("realName", "like", "%" + "周宁" + "%").or("userName", "in", Arrays.asList("zhou", "he")))
                .notEqual("epid", 90001000).let("score", 60).isNotNull("constructId");
        criteria.andCriteria(new Criteria().lt("createTime", new Date()).in("productId", Arrays.asList(1, 2, 3, 4, 5, 6)))
                .andCriteria(new Criteria().lt("createTime", new Date()).or("createTime", new Date()).andCriteria(new Criteria().where("key", 12).in("name", Arrays.asList(1, 2, 3)))
                        .orCriteria(new Criteria().where("iinnerji", "我CA")));
        criteria.notIn("productNum", Arrays.asList("GY-008", "GY-009"));
        criteria.orderBy(new Sort("userName"));
        criteria.orderBy(new Sort("createTime", "ASC"));
        criteria.groupBy("userName", "id");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
```
**控制台输出:**

```
SELECT * FROM tb_test WHERE password IN(?,?) AND (realName like ? OR userName in(?,?)) OR(ppid = ? AND special = ?) OR userName like ? AND (realName like ? OR userName in(?,?)) AND epid <> ? AND score <= ? AND constructId IS NOT NULL AND (createTime < ? AND productId IN(?,?,?,?,?,?)) AND (createTime < ? OR createTime = ? AND (key = ? AND name IN(?,?,?)) OR(iinnerji = ?)) AND productNum NOT IN(?,?) GROUP BY userName,id ORDER BY userName DESC,createTime ASC
{1234567890,111111,%周宁%,zhou,he,12305,TJ,%zhouning%,%周宁%,zhou,he,90001000,60,Tue Sep 25 20:11:40 CST 2018,1,2,3,4,5,6,Tue Sep 25 20:11:40 CST 2018,Tue Sep 25 20:11:40 CST 2018,12,1,2,3,我CA,GY-008,GY-009}
```

**使用案例**:https://github.com/SpringStudent/GyJdbcTest

#### 项目中真实应用:


```
List<DocTag> docTags = docTagDao.queryWithCriteria(new Criteria().where("tagPath", "like", parentPath + "%").and("projectId", projectId)
                .and("stageNum", stageNum).and("type", type).and("tagName", tagName)
                .and("LENGTH(tagPath)", parentPath.length() == 0 ? PathUtils.PATH_LENGTH : parentPath.length() + PathUtils.PATH_LENGTH + 1));
```

```
Criteria criteria = new Criteria().where("projectId",projectId).and("type",type).in("tagId",tagIds).and("deleteFlag",0);
        //搜索条件不为空
        if (EmptyUtils.isNotEmpty(searchkey)) {
            searchkey = RegexUtils.replaceEspStr(searchkey);
            criteria.like("fileName", searchkey);
            if(EmptyUtils.isNotEmpty(likeTagIds)){
                criteria.orCriteria(new Criteria().where("projectId",projectId).and("type",type).in("tagId",likeTagIds).and("deleteFlag",0));
            }
        }
        criteria.orderBy(new Sort("updateTime")).orderBy(new Sort("id"));
        PageResult<DataDocument> temp = dataDocumentDao.pageQueryWithCriteria(page,criteria);
```


```
Map<String, Integer> result = new HashMap<>();
        Map<String,Object> temp = dataDocumentDao.queryMapWithCriteria(new Criteria().select("tagId","count(1) as docNum")
                .in("tagId",tagIds).and("deleteFlag",0).groupBy("tagId"),CustomResultSetExractorFactory.createDoubleColumnValueResultSetExractor());
        tagIds.forEach(id -> {
            if (!temp.containsKey(id)) {
                result.put(id, 0);
            }else{
                result.put(id,Integer.parseInt(temp.get(id).toString()));
            }
        });
```


## 版本更新
### v1.1
支持使用Lambda表达式拼接sql

```
criteria.where("epid",1000)->criteria.where(UserBasicInfo::getEpid,1000);
```

### v1.2
支持按条件更新部分字段


```
tbUserDao.updateWithCriteria(new Criteria().update(SimpleUser::getEmail,"33@qq.com").update(SimpleUser::getBirth,new Date()).where(SimpleUser::getName,"zhouning"));
```

### V2.0
支持自定义sql和实体对象查询

```
Integer count = tbUserDao.useSql(Integer.class,"select count(*) from tb_user").queryObject();
List<Map<String,Object>> mapList = tbUserDao.useSql("select name,email from tb_user").queryMaps();
PageResult<SimpleUser> tbUserPageResult = tbUserDao
.useSql(SimpleUser.class,"select name,email,birth from tb_user where name = ?","zhouning").pageQuery(new Page(1,1));
```
