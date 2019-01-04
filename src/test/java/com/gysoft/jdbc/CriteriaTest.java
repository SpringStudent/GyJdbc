package com.gysoft.jdbc;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

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
    public void testJoinWithCriteria(){
        Criteria criteria = new Criteria().select("t1.name","t2.username").from(Role.class).as("t1")
                .join(new Joins().with(JoinType.LeftJoin,Role.class).as("t2").on("userName","userName")
                        .on("pwd","pwd").and("userName","=","sess")
                .and("email","in",Arrays.asList("1@qq.com","2@qq.com","3@qq.com")).on("prd","prd"))
                .join(new Joins().with(JoinType.NatureJoin,Token.class).as("t3").on("userName","userName"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }
}
