package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;
import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.tools.SqlMakeTools;
import lombok.Data;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.sql.JDBCType;
import java.util.*;

import static com.gysoft.jdbc.bean.FuncBuilder.*;

/**
 * Unit test for simple App.
 */
public class CriteriaTest {
    private String baseSql = "SELECT * FROM tb_test";
    @Test
    public void testCriteria() {
        Criteria criteria = new Criteria();
        criteria.in("set", new HashSet(Arrays.asList("1234567890", "111111")));
        criteria.orLike("okd", "s123").orLike(Token::getTk, "sd");
        criteria.orLikeIfAbsent("dsa", "").orLikeIfAbsent(Token::getTk, "111");
        criteria.in("password", Arrays.asList("1234567890", "111111"));
        criteria.andCriteria(new Criteria().betweenAnd("stdate", "2019-01-02", "2019-04-09").and("realName", "like", "%" + "周宁" + "%").or("userName", "in", Arrays.asList("zhou", "he")));
        criteria.orCriteria(new Criteria().where("ppid", "12305").orBetweenAnd("birt", "2019-12-02", "2020-12-11").and("special", "TJ").andCriteria(new Criteria().where("roleId", 123).and("pid", 1119).andCriteria(new Criteria().where("key", 123).orCriteria(new Criteria().where("hh", 3).or("mm", 4231)))));
        criteria.or("userName", "like", "%" + "zhouning" + "%")
                .andCriteria(new Criteria().and("realName", "like", "%" + "周宁" + "%").or("userName", "in", Arrays.asList("zhou", "he")))
                .notEqual("epid", 90001000).let("score", 60).isNotNull("constructId");
        criteria.andCriteria(new Criteria().lt("createTime", new Date()).in("productId", Arrays.asList(1, 2, 3, 4, 5, 6)))
                .andCriteria(new Criteria().lt("createTime", new Date()).or("createTime", new Date()).andCriteria(new Criteria().where("key", 12).in("name", Arrays.asList(1, 2, 3)))
                        .orCriteria(new Criteria().where("iinnerji", "我CA")));
        criteria.notIn("productNum", Arrays.asList("GY-008", "GY-009"));
        criteria.betweenAnd("stdate", "2019-01-02", "2019-04-09").betweenAnd("eddate", "2019-01-02", "2020-01-02");
        criteria.orBetweenAnd("sss", "orsdsd", "sda1231").orBetweenAnd("sedTime", "2018-12-02", "2019-11-22");
        criteria.orderBy(new Sort("userName"), new Sort("createTime", "ASC"));
        criteria.groupBy("userName", "id");
        criteria.having(count("asd"), "in", Arrays.asList(1, 2, 3)).limit(1);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }

    @Test
    public void testJoinSql() {
        SQL criteria = new SQL().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .natureJoin(new Joins().with(Book.class).as("t3"))
                .leftJoin(new Joins().with(Book.class).as("t4").on("t4.id", "t2.id"))
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .union().select("un.ke","un.ke2").from(Book.class).where("un.ke",1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(criteria);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }

    @Test
    public void testSubSql() {
        SQL criteria4 = new SQL().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .natureJoin(new Joins().with(Book.class).as("t3"))
                .leftJoin(new Joins().with(Book.class).as("t4").on("t4.id", "t2.id"))
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .union().select("un.ke","un.ke2").from(Book.class).where("un.ke",1);
        SQL criteria = new SQL().select("*").from(criteria4,
                new SQL().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name")),
                new SQL().select("t2.*").from(Book.class).as("t2").where("t2.kd","ssd").groupBy("t2.uploader").having("t2.count1",">",123),
                new SQL().select("t3.*").from(new SQL().select("t33.*").from(Book.class).as("t33").andIfAbsent("t33.name", "name33").orderBy(new Sort("t33.id")).union().select("t33U.*").from(Book.class).where("t33U",111111))
        ).as("res").rightJoin(new Joins().with(Book.class).as("t4").on("res.name", "t4.name")).where("res.name", "book1").orderBy(new Sort("res.name"));
        Pair<String,Object[]> p = SqlMakeTools.useSql(criteria);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));
    }

    @Test
    public void testUnionSql(){
        SQL s1 =new SQL().select("u1.*").from(Test.class).where("u1.id",123).union().select("u2.*").from(Test.class)
                .unionAll().select("u3.*").from(Book.class).where("u3",123).leftJoin(new Joins().with(Test.class)
                        .as("u31").on("u31.id","u3.id").and("u31.nmm","=","nmmm"));
        SQL s2 = new SQL().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name"));
        SQL s = new SQL().select("res.*").from(s1,s2).where("res.name", "book1").orderBy(new Sort("res.name")).groupBy("res.name").having("res.name",">",1);
        Pair<String, Object[]> sqlParamPair = SqlMakeTools.useSql(s);
        System.out.println(sqlParamPair.getFirst());
        System.out.println(ArrayUtils.toString(sqlParamPair.getSecond()));
    }

    @Data
    @Table(name = "tb_book")
    private class Book {
        private String id;
        private String name;
        private String num;
    }

    private interface BookDao extends EntityDao<Book,String>{

    }

    private class BookDaoImpl extends EntityDaoImpl<Book,String> implements BookDao{

    }

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

    @Test
    public void testCreate() throws Exception {
        SQL sql = new SQL().createTable().name("halou").temporary()
                .addColumn().name("id").integer().notNull().primary().autoIncrement().comment("主键").commit()
                .addColumn().name("name").varchar(5).notNull().comment("名称").defaults("").commit()
                .addColumn().name("age").tinyint().notNull().commit()
                .addColumn().name("email").jdbcType(JDBCType.LONGVARCHAR).defaultNull().commit()
                .addColumn().name("birthday").datetime().notNull().defaultCurrentTimestamp().commit()
                .index().unique().column("name").column("age").name("ix_name_age").commit()
                .index().name("ix_name").column("name").commit()
                .engine(TableEngine.InnoDB).comment("用户").commit()
                .values(1,"zhou",23)
                .values(2,"peng",24)
                .values(3,"wei",25);
        String tbName = new BookDaoImpl().createWithSql(sql);
        System.out.println(tbName);
    }

}
