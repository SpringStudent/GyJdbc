package com.gysoft.jdbc;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class CriteriaTest {

    private String baseSql = "SELECT * FROM tb_test";

    @Test
    public void testQueryWithCriteria() {
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
    }

    @Test
    public void testJoinWithCriteria() {
        String sd = null;
        Criteria criteria = new Criteria().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Token.class).as("t2"))
                .in("t1.password", Arrays.asList("1234567890", "111111"))
                .andIfAbsent("k1", sd);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(org.apache.commons.lang.ArrayUtils.toString(pair.getSecond()));
    }

    @Test
    public void testBuildCriteriaTree() {
        Criteria sub1 = new Criteria().select("t1.a,t1.b").from(Token.class).as("t1").where("t1.tid", 1);
        Criteria sub2 = new Criteria().select("t2.f1").from(new Criteria().select("*").from(Role.class).as("t2'").where("f1","v1")
        .innerJoin(new Joins().with(Role.class).as("t2''").on("t2'.id","t2''.id"))).as("t2").where("t2.roleId", 2);
        Criteria criteria = new Criteria().select("*").from(sub1, sub2).as("t3").rightJoin(new Joins()
        .with(Token.class).as("t4").on("t4.f2","t3.f3")).where("t3.id","vv");
        CriteriaTree criteriaTree = new CriteriaTree();
        Pair<String,Object[]> pair = SqlMakeTools.doCriteria(criteria,null);
        criteriaTree.setId("0");
        criteriaTree.setParams(pair.getSecond());
        criteriaTree.setSql(pair.getFirst());
        criteriaTree.setChildCriteriaTree(new ArrayList<>());
        SqlMakeTools.buildCriteriaTree(criteria,criteriaTree);
        System.out.println(criteriaTree);
        System.out.println(SqlMakeTools.doSubCriteriaSql(criteriaTree,""));
        System.out.println(Arrays.toString(SqlMakeTools.doSubCriteriaParam(criteriaTree,new Object[]{})));

    }
}
