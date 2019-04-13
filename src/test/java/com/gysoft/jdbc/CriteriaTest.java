package com.gysoft.jdbc;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.springframework.util.IdGenerator;

import java.awt.print.Book;
import java.util.*;

import static com.gysoft.jdbc.bean.FuncBuilder.*;

/**
 * Unit test for simple App.
 */
public class CriteriaTest {

    private String baseSql = "SELECT * FROM tb_test";

    @Test
    public void testQueryWithCriteria() {
        Criteria criteria = new Criteria();
        criteria.select("distinct(zhouning)");
//        criteria.in("set", new HashSet(Arrays.asList("1234567890","111111")));
//        criteria.in("password", Arrays.asList("1234567890","111111"));
        criteria.andCriteria(new Criteria().betweenAnd("stdate","2019-01-02","2019-04-09").and("realName", "like", "%" + "周宁" + "%").or("userName", "in", Arrays.asList("zhou", "he")));
        criteria.orCriteria(new Criteria().where("ppid", "12305").orBetweenAnd("birt","2019-12-02","2020-12-11").and("special", "TJ").andCriteria(new Criteria().where("roleId", 123).and("pid", 1119).andCriteria(new Criteria().where("key", 123).orCriteria(new Criteria().where("hh", 3).or("mm", 4231)))));
        criteria.or("userName", "like", "%" + "zhouning" + "%")
                .andCriteria(new Criteria().and("realName", "like", "%" + "周宁" + "%").or("userName", "in", Arrays.asList("zhou", "he")))
                .notEqual("epid", 90001000).let("score", 60).isNotNull("constructId");
        criteria.andCriteria(new Criteria().lt("createTime", new Date()).in("productId", Arrays.asList(1, 2, 3, 4, 5, 6)))
                .andCriteria(new Criteria().lt("createTime", new Date()).or("createTime", new Date()).andCriteria(new Criteria().where("key", 12).in("name", Arrays.asList(1, 2, 3)))
                        .orCriteria(new Criteria().where("iinnerji", "我CA")));
        criteria.notIn("productNum", Arrays.asList("GY-008", "GY-009"));
        criteria.betweenAnd("stdate","2019-01-02","2019-04-09").betweenAnd("eddate","2019-01-02","2020-01-02");
        criteria.orBetweenAnd("sss","orsdsd","sda1231").orBetweenAnd("sedTime","2018-12-02","2019-11-22");
        criteria.orderBy(new Sort("userName"), new Sort("createTime", "ASC"));
        criteria.groupBy("userName", "id");
        criteria.having(count("asd"),"in",Arrays.asList(1,2,3));
        Pair<String, Object[]> pair = SqlMakeTools.doPielineCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }

    @Test
    public void testJoinWithCriteria() {
        Criteria criteria = new Criteria().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .natureJoin(new Joins().with(Book.class).as("t3"))
                .leftJoin(new Joins().with(Book.class).as("t4").on("t4.id", "t2.id"))
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .union().select("un.ke","un.ke2").from(Book.class).where("un.ke",1);
        Pair<String, Object[]> pair = SqlMakeTools.doPielineCriteria(criteria, null);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }

    @Test
    public void testSubQuery() {
        Criteria criteria4 = new Criteria().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .natureJoin(new Joins().with(Book.class).as("t3"))
                .leftJoin(new Joins().with(Book.class).as("t4").on("t4.id", "t2.id"))
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .union().select("un.ke","un.ke2").from(Book.class).where("un.ke",1);
        Criteria criteria = new Criteria().select("*").from(criteria4,
                new Criteria().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name")),
                new Criteria().select("t2.*").from(Book.class).as("t2").where("t2.kd","ssd").groupBy("t2.uploader").having("t2.count1",">",123),
                new Criteria().select("t3.*").from(new Criteria().select("t33.*").from(Book.class).as("t33").andIfAbsent("t33.name", "name33").orderBy(new Sort("t33.id")))
        ).as("res").rightJoin(new Joins().with(Book.class).as("t4").on("res.name", "t4.name")).where("res.name", "book1").orderBy(new Sort("res.name"));
        CriteriaTree criteriaTree = new CriteriaTree();
        Pair<String, Object[]> pair = SqlMakeTools.doPielineCriteria(criteria, null);
        criteriaTree.setId("0");
        criteriaTree.setParams(pair.getSecond());
        criteriaTree.setSql(pair.getFirst());
        criteriaTree.setChildCriteriaTree(new ArrayList<>());
        SqlMakeTools.buildCriteriaTree(criteria, criteriaTree);
        Pair<String,Object[]> p = SqlMakeTools.doSubCriteria(criteriaTree,new Pair<>("",new Object[]{}));
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));

    }

    @Test
    public void testFunc(){
        //支持mysql函数拼接
        //聚集函数
        Criteria criteria = new Criteria().select(count("*"),avg(Token::getSize),max(Token::getSize),min(Token::getSize),sum(Token::getSize)).from(Token.class);
        //字符串处理函数
        Criteria criteria2 = new Criteria().select(concat(Token::getTk,Token::getSize),length(Token::getTk),charLength(Token::getTk),upper(Token::getTk),lower(Token::getTk)).from(Token.class);
        //数值处理函数
        Criteria criteria3 = new Criteria().select(abs(Token::getSize),ceil(Token::getSize),floor(Token::getSize)).from(Token.class);
        //时间处理函数
        Criteria criteria4 = new Criteria().select(curdate(),curtime(),now(),month(curdate()),week(curdate()),minute(curtime()));

        Criteria criteria5 = new Criteria().select(formatAs("10000","2").as("a")).from(Book.class);

        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder());
        System.out.println(pair.getFirst());
        Pair<String, Object[]> pair2 = SqlMakeTools.doCriteria(criteria2, new StringBuilder());
        System.out.println(pair2.getFirst());
        Pair<String, Object[]> pair3 = SqlMakeTools.doCriteria(criteria3, new StringBuilder());
        System.out.println(pair3.getFirst());
        Pair<String, Object[]> pair4 = SqlMakeTools.doCriteria(criteria4, new StringBuilder());
        System.out.println(pair4.getFirst());
        //...more 等着你完善和探索...
        Pair<String, Object[]> pair5 = SqlMakeTools.doCriteria(criteria5, null);
        System.out.println(pair5.getFirst());
    }

    @Test
    public void testUnion(){
        Criteria criteria =new Criteria().select("u1.*").from(Test.class).where("u1.id",123).union().select("u2.*").from(Test.class)
                .unionAll().select("u3.*").from(Book.class).where("u3",123).leftJoin(new Joins().with(Test.class)
                        .as("u31").on("u31.id","u3.id").and("u31.nmm","=","nmmm"));
        Criteria criteria1 = new Criteria().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name"));
        Criteria subCriteria = new Criteria().select("res.*").from(criteria,criteria1).where("res.name", "book1").orderBy(new Sort("res.name"));

        CriteriaTree criteriaTree = new CriteriaTree();
        Pair<String, Object[]> pair = SqlMakeTools.doPielineCriteria(subCriteria, null);
        criteriaTree.setId("0");
        criteriaTree.setParams(pair.getSecond());
        criteriaTree.setSql(pair.getFirst());
        criteriaTree.setChildCriteriaTree(new ArrayList<>());
        SqlMakeTools.buildCriteriaTree(subCriteria, criteriaTree);
        Pair<String, Object[]> sqlParamPair = SqlMakeTools.doSubCriteria(criteriaTree, new Pair<>("", new Object[]{}));
        System.out.println(sqlParamPair.getFirst());
        System.out.println(ArrayUtils.toString(sqlParamPair.getSecond()));
    }
}
