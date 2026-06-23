package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;
import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.FieldReference;
import com.gysoft.jdbc.bean.Pair;
import com.gysoft.jdbc.bean.SQL;
import com.gysoft.jdbc.bean.Sort;
import com.gysoft.jdbc.bean.ValueReference;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gysoft.jdbc.bean.*;

import static com.gysoft.jdbc.bean.FuncBuilder.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import static com.gysoft.jdbc.bean.FuncBuilder.avg;
import static com.gysoft.jdbc.bean.FuncBuilder.count;
import static com.gysoft.jdbc.bean.FuncBuilder.max;
import static com.gysoft.jdbc.bean.FuncBuilder.sum;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CSqlTest {

    @Test
    public void createWithSqlShouldRestoreSqlStateAfterInsert() throws Exception {
        SQL sql = new SQL().create().table("halou")
                .column().name("id").integer().primary().commit()
                .column().name("name").varchar(16).commit()
                .commit()
                .values(1, "zhou");

        new TestDao().createWithSql(sql);

        assertEquals("create", sql.getSqlType());
        assertEquals("`halou`", sql.getTableMeta().getName());
        assertNull(sql.getInsert().getFirst());
        assertNull(sql.getInsert().getSecond());
    }

    @Test
    public void createWithSqlShouldRestoreSqlStateWhenCreateFails() throws Exception {
        SQL sql = new SQL().create().table("halou")
                .column().name("id").integer().primary().commit()
                .commit()
                .values(1);

        try {
            new FailingCreateDao().createWithSql(sql);
            fail("createWithSql should fail");
        } catch (IllegalStateException expected) {
            assertEquals("create failed", expected.getMessage());
        }

        assertEquals("create", sql.getSqlType());
        assertEquals("`halou`", sql.getTableMeta().getName());
        assertNull(sql.getInsert().getFirst());
        assertNull(sql.getInsert().getSecond());
    }

    @Test
    public void testSimpleWhere() {
        Criteria criteria = new Criteria().where("name", "zhouning");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void testMultipleConditions() {
        Criteria criteria = new Criteria()
                .where("name", "zhouning")
                .and("age", 28)
                .gt("score", 60);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("name = ?"));
        assertTrue(pair.getFirst().contains("AND age = ?"));
        assertTrue(pair.getFirst().contains("AND score > ?"));
    }

    @Test
    public void testInClause() {
        Criteria criteria = new Criteria().in("password", Arrays.asList("1234567890", "111111"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE password IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{"1234567890", "111111"}, pair.getSecond());
    }

    @Test
    public void testNotInClause() {
        Criteria criteria = new Criteria().notIn("productNum", Arrays.asList("GY-008", "GY-009"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("NOT IN"));
    }

    @Test
    public void testLikeClause() {
        Criteria criteria = new Criteria().like("key", "test");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("LIKE"));
        assertArrayEquals(new Object[]{"%test%"}, pair.getSecond());
    }

    @Test
    public void testLikeIfAbsentWithNullSkips() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .likeIfAbsent("optionalField", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
    }

    @Test
    public void testBetweenAnd() {
        Criteria criteria = new Criteria().betweenAnd("stdate", "2019-01-02", "2019-04-09");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("BETWEEN ? AND ?"));
        assertArrayEquals(new Object[]{"2019-01-02", "2019-04-09"}, pair.getSecond());
    }

    @Test
    public void testIsNull() {
        Criteria criteria = new Criteria().isNull("field");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("IS NULL"));
    }

    @Test
    public void testIsNotNull() {
        Criteria criteria = new Criteria().isNotNull("field");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("IS NOT NULL"));
    }

    // ==================== AND/OR 组合条件测试 ====================

    @Test
    public void testOrCondition() {
        Criteria criteria = new Criteria().where("name", "a").or("name", "b");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("OR name = ?"));
    }

    @Test
    public void testAndCriteriaNesting() {
        Criteria criteria = new Criteria()
                .where("flag", 1)
                .andCriteria(new Criteria().where("type", "A").or("type", "B"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("AND (type = ? OR type = ?)"));
        assertArrayEquals(new Object[]{1, "A", "B"}, pair.getSecond());
    }

    @Test
    public void testOrCriteriaNesting() {
        Criteria criteria = new Criteria()
                .where("flag", 1)
                .orCriteria(new Criteria().where("type", "A"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue("OR criteria missing: " + pair.getFirst(), pair.getFirst().contains("OR (type = ?)"));
    }

    @Test
    public void testWhereBuilderPattern() {
        Criteria criteria = new Criteria().where("1", 1)
                .andWhere(Where.where("a").equal(1).and("b").equal(2))
                .orWhere(Where.where("c").equal(3));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue("AND group missing", pair.getFirst().contains("AND (a = ? AND b = ?)"));
        assertTrue("OR group missing", pair.getFirst().contains(" OR (c = ?)"));
    }

    // ==================== GROUP BY / ORDER BY / HAVING / LIMIT 测试 ====================

    @Test
    public void testGroupBy() {
        Criteria criteria = new Criteria().groupBy("userName", "id");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("GROUP BY userName,id"));
    }

    @Test
    public void testOrderBy() {
        Criteria criteria = new Criteria().orderBy(new Sort("userName"), new Sort("createTime", "ASC"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue("ORDER BY missing: " + pair.getFirst(), pair.getFirst().contains("ORDER BY userName"));
    }

    @Test
    public void testHavingClause() {
        Criteria criteria = new Criteria().having(count("asd"), "in", Arrays.asList(1, 2, 3)).groupBy("tbs");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue("HAVING clause missing: " + pair.getFirst(), pair.getFirst().contains("HAVING COUNT(asd) in(?,?,?)"));
    }

    @Test
    public void testLimit() {
        Criteria criteria = new Criteria().limit(1, 5);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue("LIMIT missing: " + pair.getFirst(), pair.getFirst().contains("LIMIT"));
    }

    // ==================== SQL 对象 — 基础操作测试 ====================

    @Test
    public void testSqlSelect() {
        SQL sql = new SQL().select("id", "name").from("tb_test");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("SELECT id, name FROM tb_test"));
    }

    @Test
    public void testSqlSelectAll() {
        SQL sql = new SQL().select("*").from("table_1123")
                .and(Where.where("a").equal(1).and("b").equal(2));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM table_1123 WHERE a = ? AND b = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 2}, pair.getSecond());
    }

    @Test
    public void sqlSelectShouldSupportForUpdate() {
        SQL sql = new SQL().select("*").from("tb_order").where("id", 1).forUpdate();
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT * FROM tb_order WHERE id = ? FOR UPDATE", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void sqlSelectShouldSupportLockInShareMode() {
        SQL sql = new SQL().select("*").from("tb_order").where("id", 1).lockInShareMode();
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT * FROM tb_order WHERE id = ? LOCK IN SHARE MODE", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void testSqlDelete() {
        SQL sql = new SQL().delete().from("test").where("id", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("DELETE FROM test WHERE id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void testSqlUpdate() {
        SQL sql = new SQL().update("test").set("name", "asd").set("id", 1).where("pid", 15);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("UPDATE test SET name = ?, id = ? WHERE pid = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"asd", 1, 15}, pair.getSecond());
    }

    @Test
    public void testSqlInsertValues() {
        SQL sql = new SQL().insertInto("tb_test", "id", "name").values(1, "test");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue("Should start with INSERT: " + pair.getFirst(), pair.getFirst().startsWith("INSERT INTO tb_test"));
        assertTrue("Should contain columns: " + pair.getFirst(), pair.getFirst().contains("(id,name)"));
    }

    // ==================== SQL JOIN 测试 ====================

    @Test
    public void testInnerJoin() {
        SQL sql = new SQL().select("*").from("a")
                .innerJoin("b").on("a.id", "b.id");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue("INNER JOIN missing", pair.getFirst().contains("INNER JOIN b"));
    }

    @Test
    public void testLeftJoin() {
        SQL sql = new SQL().select("*").from("a")
                .leftJoin("b").on("a.id", "b.id");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue("LEFT JOIN missing", pair.getFirst().contains("LEFT JOIN b"));
    }

    @Test
    public void testRightJoin() {
        SQL sql = new SQL().select("*").from("a")
                .rightJoin("b", "aliasB").on("a.id", "aliasB.id");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("RIGHT JOIN b aliasB  ON a.id = aliasB.id"));
    }

    @Test
    public void testMultipleJoins() {
        SQL sql = new SQL().select("*").from("a")
                .innerJoin("b", "bAlias", c -> c.on("a.id", "bAlias.id").and("bAlias.name", "=", "test"))
                .leftJoin("c", "cAlias", c -> c.on("a.id", "cAlias.id"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("INNER JOIN b bAlias"));
        assertTrue(pair.getFirst().contains("LEFT JOIN c cAlias"));
    }

    // ==================== UNION 测试 ====================

    @Test
    public void testUnion() {
        SQL sql = new SQL().select("*").from("a").union().select("*").from("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("UNION"));
    }

    @Test
    public void testUnionAll() {
        SQL sql = new SQL().select("*").from("a").unionAll().select("*").from("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("UNION ALL"));
    }

    // ==================== 子查询测试 ====================

    @Test
    public void testSubQueryInWhere() {
        SQL sql = new SQL().select("*").from("BOOK")
                .notIn("id", new SQL().select("id").from("author").where("f1", 123).in("f2", Arrays.asList("g", "l")))
                .andCriteria(new Criteria().where("name", "name1").or("num", "asdsd"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("NOT IN"));
        assertTrue(pair.getFirst().contains("SELECT id FROM author"));
    }

    @Test
    public void testSubQueryInFrom() {
        SQL sql = new SQL().select("*").from(
                new SQL().select("d").from("t_d").union().select("c").from("t_c"), "cd");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("UNION"));
        assertTrue(pair.getFirst().contains("cd"));
    }

    // ==================== 聚合函数测试 ====================

    @Test
    public void testAggregateFunctionSql() {
        SQL sql = new SQL().select(
                countAs("id").as("cid"),
                avgAs("size").as("avgSize"),
                maxAs("size").as("maxSize"),
                minAs("size").as("minSize"),
                sumAs("size").as("sumSize")
        ).from("tb_test");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue("COUNT missing: " + pair.getFirst(), pair.getFirst().contains("COUNT"));
        assertTrue("AVG missing: " + pair.getFirst(), pair.getFirst().contains("AVG"));
        assertTrue("MAX missing: " + pair.getFirst(), pair.getFirst().contains("MAX"));
        assertTrue("MIN missing: " + pair.getFirst(), pair.getFirst().contains("MIN"));
        assertTrue("SUM missing: " + pair.getFirst(), pair.getFirst().contains("SUM"));
    }

    @Test
    public void testFuncBuilderDateFormat() {
        String result = dateFormat("create_time", "%Y-%m-%d");
        assertEquals("DATE_FORMAT(create_time,'%Y-%m-%d')", result);
    }

    @Test
    public void testFuncBuilderIfNull() {
        String result = ifNull("name", "'default'");
        assertEquals("IFNULL(name,'default')", result);
    }

    @Test
    public void testFuncBuilderConcat() {
        String result = concat("a", "b", "c");
        assertEquals("CONCAT(a,b,c)", result);
    }

    @Test
    public void testFuncBuilderGroupConcat() {
        assertEquals("GROUP_CONCAT(name)", groupConcat("name"));
        assertEquals("GROUP_CONCAT(DISTINCT name)", groupConcatDistinct("name"));
        assertEquals("GROUP_CONCAT(name SEPARATOR ',')", groupConcat("name", "','"));
        assertEquals("GROUP_CONCAT(`name`)", groupConcat(Role::getName));
        assertEquals("GROUP_CONCAT(name) AS names", groupConcatAs("name").as("names"));
    }

    @Test
    public void testFuncBuilderCaseWhen() {
        assertEquals("CASE WHEN score >= 60 THEN 'pass' ELSE 'fail' END", caseWhen("score >= 60", "'pass'", "'fail'"));
        assertEquals("CASE WHEN score >= 90 THEN 'A' WHEN score >= 60 THEN 'B' ELSE 'C' END",
                caseWhen("score >= 90", "'A'").when("score >= 60", "'B'").elseThen("'C'").end());
        assertEquals("CASE WHEN status = 1 THEN 'enabled' ELSE 'disabled' END AS statusName",
                caseWhen("status = 1", "'enabled'").elseThen("'disabled'").asBuilder().as("statusName"));
    }

    @Test
    public void testFuncBuilderJsonFunctions() {
        assertEquals("JSON_EXTRACT(extra,'$.name')", jsonExtract("extra", "$.name"));
        assertEquals("JSON_EXTRACT(`auths`,'$.name')", jsonExtract(Role::getAuths, "$.name"));
        assertEquals("JSON_UNQUOTE(JSON_EXTRACT(extra,'$.name'))", jsonUnquote(jsonExtract("extra", "$.name")));
        assertEquals("JSON_CONTAINS(extra,'1')", jsonContains("extra", "'1'"));
        assertEquals("JSON_CONTAINS(extra,'1','$.ids')", jsonContains("extra", "'1'", "$.ids"));
        assertEquals("JSON_SET(extra,'$.name','Tom')", jsonSet("extra", "$.name", "'Tom'"));
        assertEquals("JSON_REMOVE(extra,'$.temp','$.debug')", jsonRemove("extra", "$.temp", "$.debug"));
        assertEquals("JSON_OBJECT('id',id,'name',name)", jsonObject("'id'", "id", "'name'", "name"));
        assertEquals("JSON_ARRAY(id,name)", jsonArray("id", "name"));
        assertEquals("JSON_ARRAY(`name`,`auths`)", jsonArray(Role::getName, Role::getAuths));
        assertEquals("JSON_EXTRACT(extra,'$.name') AS nameJson", jsonExtractAs("extra", "$.name").as("nameJson"));
    }

    @Test
    public void criteriaShouldBuildSqlAndParams() {
        Criteria criteria = new Criteria()
                .where(Token::getTk, "abc")
                .gt(Token::getSize, 10)
                .in("status", Arrays.asList(1, 2))
                .orderBy(new Sort(Token::getSize, "ASC"))
                .limit(5, 10);

        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));

        assertEquals("SELECT * FROM tb_token WHERE ddd = ? AND `size` > ? AND status IN(?,?) ORDER BY `size` ASC LIMIT ?, ?", pair.getFirst());
        assertArrayEquals(new Object[]{"abc", 10, 1, 2, 5, 10}, pair.getSecond());
    }

    @Test
    public void criteriaShouldBuildNestedAndOrGroups() {
        Criteria criteria = new Criteria()
                .where("type", "A")
                .andCriteria(new Criteria().where("status", 1).or("status", 2))
                .orCriteria(new Criteria().where("owner", "root").and("enabled", true));

        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM account"));

        assertEquals("SELECT * FROM account WHERE type = ? AND (status = ? OR status = ?) OR (owner = ? AND enabled = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{"A", 1, 2, "root", true}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildJoinSqlAndParams() {
        SQL sql = new SQL()
                .select("r.name", "t.ddd")
                .from(Role.class).as("r")
                .leftJoin(Token.class, "t")
                .on("r.name", "t.ddd")
                .and("t.size", ">", 3)
                .where("r.auths", "admin")
                .or("t.id", 7);

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT r.name, t.ddd FROM tb_role r LEFT JOIN tb_token t  ON r.name = t.ddd  WHERE t.size > ? AND r.auths = ? OR t.id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{3, "admin", 7}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildAggregateSelectWithGroupAndHaving() {
        SQL sql = new SQL()
                .select("type", count("*"), max(Token::getSize), sum(Token::getSize))
                .from(Token.class)
                .where("status", 1)
                .groupBy("type")
                .having(count("*"), ">", 2)
                .orderBy(new Sort("type", "ASC"));

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT type, COUNT(*), MAX(`size`), SUM(`size`) FROM tb_token WHERE status = ? GROUP BY type HAVING COUNT(*) > ? ORDER BY type ASC", pair.getFirst());
        assertArrayEquals(new Object[]{1, 2}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildSubQueryConditionAndValueReference() {
        SQL sql = new SQL()
                .select("number", new ValueReference("fixed"))
                .from("student_score")
                .where("subject", "math")
                .gt("score", new SQL()
                        .select(avg("score"))
                        .from("student_score")
                        .where("subject", "math"));

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT number, ? FROM student_score WHERE subject = ? AND score >(SELECT AVG(score) FROM student_score WHERE subject = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{"fixed", "math", "math"}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildUnionAllWithParams() {
        SQL sql = new SQL()
                .select("a.*")
                .from("a_tb")
                .as("a")
                .where("a.id", 1)
                .unionAll()
                .select("b.*")
                .from("b_tb")
                .as("b")
                .where("b.id", 2);

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("(SELECT a.* FROM a_tb a WHERE a.id = ?) UNION ALL (SELECT b.* FROM b_tb b WHERE b.id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, 2}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildSelectFromUnionSubQuery() {
        SQL sql = new SQL()
                .select("*")
                .from(new SQL()
                        .select("a.*")
                        .from("a_tb")
                        .as("a")
                        .where("a.flag", 1)
                        .unionAll()
                        .select("b.*")
                        .from("b_tb")
                        .as("b")
                        .where("b.flag", 2), "res")
                .where("res.id", "id1");

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT * FROM( ( (SELECT a.* FROM a_tb a WHERE a.flag = ?) UNION ALL (SELECT b.* FROM b_tb b WHERE b.flag = ?)) res )  WHERE res.id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 2, "id1"}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildInsertValuesAndDuplicateKeyUpdate() {
        SQL sql = new SQL()
                .insertInto(Role.class, Role::getName, Role::getAuths)
                .values("admin", "all")
                .values("guest", "read")
                .onDuplicateKeyUpdate(Role::getAuths, "updated");

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("INSERT INTO tb_role (`name`,`auths`)", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
        assertEquals(2, sql.getInsertValues().size());
        assertArrayEquals(new Object[]{"admin", "all"}, sql.getInsertValues().get(0));
        assertArrayEquals(new Object[]{"guest", "read"}, sql.getInsertValues().get(1));
        assertEquals(1, sql.getKvs().size());
        assertEquals("`auths`", sql.getKvs().get(0).getFirst());
        assertEquals("updated", sql.getKvs().get(0).getSecond());
    }

    @Test
    public void sqlShouldBuildDuplicateKeyUpdateWithInsertValues() {
        SQL sql = new SQL()
                .insertInto("tb_account", "userName", "realName")
                .values("admin", "all")
                .onDuplicateKeyUpdateValues("userName", "realName");

        assertEquals(2, sql.getKvs().size());
        assertEquals("userName", sql.getKvs().get(0).getFirst());
        assertEquals("VALUES(userName)", ((FieldReference) sql.getKvs().get(0).getSecond()).getField());
        assertEquals("realName", sql.getKvs().get(1).getFirst());
        assertEquals("VALUES(realName)", ((FieldReference) sql.getKvs().get(1).getSecond()).getField());
    }

    @Test
    public void sqlShouldBuildDuplicateKeyUpdateWithLambdaInsertValues() {
        SQL sql = new SQL()
                .insertInto(Role.class, Role::getName, Role::getAuths)
                .values("admin", "all")
                .onDuplicateKeyUpdateValues(Role::getName, Role::getAuths);

        assertEquals(2, sql.getKvs().size());
        assertEquals("`name`", sql.getKvs().get(0).getFirst());
        assertEquals("VALUES(`name`)", ((FieldReference) sql.getKvs().get(0).getSecond()).getField());
        assertEquals("`auths`", sql.getKvs().get(1).getFirst());
        assertEquals("VALUES(`auths`)", ((FieldReference) sql.getKvs().get(1).getSecond()).getField());
    }

    @Test
    public void sqlShouldBuildUpdateWithFieldReferenceAndWhereParams() {
        SQL sql = new SQL()
                .update("test")
                .as("t")
                .set("t.name", new FieldReference("src.name"))
                .set("t.size", 10)
                .where("t.id", "id1");

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("UPDATE test t SET t.name = src.name, t.size = ? WHERE t.id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{10, "id1"}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildDeleteWithJoin() {
        SQL sql = new SQL()
                .delete("a", "b")
                .from("flow_instance")
                .as("a")
                .innerJoin("flow_action", "b")
                .on("a.id", "b.flowInstanceId")
                .where("b.bizId", "id123456");

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("DELETE a,b FROM flow_instance a INNER JOIN flow_action b  ON a.id = b.flowInstanceId  WHERE b.bizId = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"id123456"}, pair.getSecond());
    }

    @Table
    private class Book {
        private String id;
        private String name;
        private String num;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }
    }

    @Test
    public void sqlShouldBuildDeleteWithJoinAndChild() {
        SQL s1 = new SQL().select("u1.*").from(Test.class).where("u1.id", 123).union().select("u2.*").from(Test.class).and("xd22", 1).or(Opt.OR, WhereParam.where("zx").isNull(), WhereParam.where("had").equal(2335))
                .unionAll().select("u3.*").from(Book.class).where("u3", 123).leftJoin(new Joins().with(Test.class)
                        .as("u31").on("u31.id", "u3.id").and("u31.nmm", "=", "nmmm")).limit(10000);
        SQL s2 = new SQL().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name"));
        SQL sql = new SQL().select("res.*").from(s1, s2).where("res.name", "book1").orderBy(new Sort("res.name")).limit(100);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT res.* FROM( (SELECT u1.* FROM TEST WHERE u1.id = ?) UNION (SELECT u2.* FROM TEST WHERE xd22 = ? OR zx IS NULL OR had = ?) UNION ALL (SELECT u3.* FROM BOOK LEFT JOIN TEST u31  ON u31.id = u3.id  AND u31.nmm = ? WHERE u3 = ? LIMIT ?) , (SELECT t1.* FROM BOOK t1 LEFT JOIN BOOK j1  ON j1.id = t1.id  AND j1.name = ? WHERE (t1.id IN(?,?,?) AND t1.name LIKE ?)))  WHERE res.name = ? ORDER BY res.name DESC LIMIT ?", pair.getFirst());
        assertArrayEquals(new Object[]{123, 1, 2335, "nmmm", 123, 10000, "j1name", 1, 2, 3, "%name1%", "book1", 100}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildJoinWith() {
        SQL sql = new SQL().select("m.status mkStatus").from("inspection", "m")
                .leftJoin(new SQL().select("m.inspId,max(m.modelObjId)modelObjId").from("inspection_model").as("m")
                        .and("m.projectId", "pid").groupBy("m.projectId"), "g1").on("g1.inspId", "m.id")
                .inIfAbsent("m.status", Arrays.asList(1, 2, 3));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT m.status mkStatus FROM inspection m LEFT JOIN (SELECT m.inspId,max(m.modelObjId)modelObjId FROM inspection_model m WHERE m.projectId = ? GROUP BY m.projectId) g1  ON g1.inspId = m.id  WHERE m.status IN(?,?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{"pid", 1, 2, 3,}, pair.getSecond());
    }

    private static class TestDao extends EntityDaoImpl<TestEntity, String> {
        TestDao() {
            this.jdbcTemplate = new JdbcTemplate() {
                @Override
                public void execute(String sql) {
                }

                @Override
                public int update(String sql, Object... args) {
                    return 1;
                }

                @Override
                public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) {
                    return requiredType.cast(1);
                }
            };
        }
    }

    private static class FailingCreateDao extends EntityDaoImpl<TestEntity, String> {
        FailingCreateDao() {
            this.jdbcTemplate = new JdbcTemplate() {
                @Override
                public void execute(String sql) {
                    throw new IllegalStateException("create failed");
                }
            };
        }
    }

    private static class TestEntity {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
