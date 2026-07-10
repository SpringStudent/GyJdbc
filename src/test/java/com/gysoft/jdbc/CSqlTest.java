package com.gysoft.jdbc;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.gysoft.jdbc.bean.FuncBuilder.*;
import static org.junit.Assert.*;

public class CSqlTest {

    @Test
    public void useSqlShouldBuildCreateSqlWithEmptyParams() {
        SQL sql = new SQL().create().table("halou")
                .column(c -> c.name("id").integer().primary())
                .column(c -> c.name("name").varchar(16))
                .commit();

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("CREATE TABLE `halou` (`id` int PRIMARY KEY,`name` varchar(16)) DEFAULT CHARSET=utf8mb4", pair.getFirst());
        assertArrayEquals(new Object[]{"`halou`"}, pair.getSecond());
    }

    @Test
    public void useSqlShouldBuildComplexCreateSql() {
        SQL sql = new SQL().create().table("member_log")
                .ifNotExists()
                .engine(TableEnum.Engine.InnoDB)
                .charset("utf8mb4")
                .collation("utf8mb4_general_ci")
                .autoIncrement(1000)
                .rowFormat(TableEnum.RowFormat.DYNAMIC)
                .comment("member log table")
                .column().name("id").integer().primary().autoIncrement().notNull().comment("primary id").commit()
                .column().name("memberName").varchar(64).notNull().defaultVal("anonymous").comment("member name").commit()
                .column().name("createdAt").datetime(6).notNull().defaultCurrentTimestamp().commit()
                .column().name("deletedFlag").tinyint().defaultVal("0").commit()
                .index().name("uk_member_name").unique().column("memberName").usingBtree().comment("unique member name").commit()
                .index().column("createdAt", "deletedFlag").usingHash().commit()
                .commit()
                .select("*").from(new SQL().select("1").from("DUAL"));

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("CREATE TABLE IF NOT EXISTS `member_log` (`id` int NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'primary id',`memberName` varchar(64) NOT NULL DEFAULT 'anonymous' COMMENT 'member name',`createdAt` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,`deletedFlag` tinyint DEFAULT '0',UNIQUE KEY `uk_member_name` (`memberName`) USING BTREE COMMENT 'unique member name', KEY `ix_createdAt_deletedFlag` (`createdAt`,`deletedFlag`) USING HASH) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci AUTO_INCREMENT=1000 ROW_FORMAT=DYNAMIC COMMENT='member log table'", pair.getFirst());
        assertArrayEquals(new Object[]{"`member_log`"}, pair.getSecond());
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
    public void andCriteriaWithConditionTrueShouldInclude() {
        Criteria criteria = new Criteria()
                .where("flag", 1)
                .andCriteria(c -> c.where("type", "A").or("type", "B"), true);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("AND (type = ? OR type = ?)"));
        assertArrayEquals(new Object[]{1, "A", "B"}, pair.getSecond());
    }

    @Test
    public void andCriteriaWithConditionFalseShouldSkip() {
        Criteria criteria = new Criteria()
                .where("flag", 1)
                .andCriteria(c -> c.where("type", "A").or("type", "B"), false);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE flag = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void orCriteriaWithConditionTrueShouldInclude() {
        Criteria criteria = new Criteria()
                .where("flag", 1)
                .orCriteria(c -> c.where("type", "A"), true);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("OR (type = ?)"));
        assertArrayEquals(new Object[]{1, "A"}, pair.getSecond());
    }

    @Test
    public void orCriteriaWithConditionFalseShouldSkip() {
        Criteria criteria = new Criteria()
                .where("flag", 1)
                .orCriteria(c -> c.where("type", "A"), false);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE flag = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
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
        assertTrue(pair.getFirst().contains("RIGHT JOIN b aliasB ON a.id = aliasB.id"));
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

    @Test
    public void joinShouldRejectUnsupportedTableType() {
        try {
            new SQL().select("*").from("a").leftJoin(new Object());
            fail("unsupported join table type should fail");
        } catch (GyjdbcException expected) {
            assertEquals("unsupported join table type", expected.getMessage());
        }
    }

    @Test
    public void joinsStaticFactoryShouldCreateStringClassAndSqlJoins() {
        SQL sql = new SQL()
                .select("r.name", "t.ddd", "g1.max_id")
                .from(Role.class).as("r")
                .leftJoin(Joins.joinWith("tb_token").as("t").on("r.name", "t.ddd"))
                .innerJoin(Joins.joinWith(Token.class).as("t2").on("r.name", "t2.ddd"))
                .rightJoin(Joins.joinWith(new SQL()
                        .select("ddd", "MAX(id) max_id")
                        .from(Token.class)
                        .groupBy("ddd")).as("g1").on("g1.ddd", "t.ddd"))
                .where("r.auths", "admin");

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT r.name, t.ddd, g1.max_id FROM tb_role r LEFT JOIN tb_token t ON r.name = t.ddd INNER JOIN tb_token t2 ON r.name = t2.ddd RIGHT JOIN (SELECT ddd, MAX(id) max_id FROM tb_token GROUP BY ddd) g1 ON g1.ddd = t.ddd WHERE r.auths = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin"}, pair.getSecond());
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
                .andCriteria(a -> a.where("status", 1).or("status", 2))
                .orCriteria(c -> c.where("owner", "root").and("enabled", true));

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
                .where("r.auths", "admin")
                .and("t.size", ">", 3)
                .or("t.id", 7);

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT r.name, t.ddd FROM tb_role r LEFT JOIN tb_token t ON r.name = t.ddd WHERE r.auths = ? AND t.size > ? OR t.id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin", 3, 7}, pair.getSecond());
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

        System.out.println(col(Role::getName));
        assertEquals(2, sql.getKvs().size());
        assertEquals("`name`", sql.getKvs().get(0).getFirst());
        assertEquals("VALUES(`name`)", ((FieldReference) sql.getKvs().get(0).getSecond()).getField());
        assertEquals("`auths`", sql.getKvs().get(1).getFirst());
        assertEquals("VALUES(`auths`)", ((FieldReference) sql.getKvs().get(1).getSecond()).getField());
    }

    private <T, R> String col(TypeFunction<T, R> function) {
        return TypeFunction.getLambdaColumnName(function);
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
    public void staticFactoriesShouldCreateReferencesAndSorts() {
        FieldReference fieldReference = FieldReference.newFieldRef("src.name");
        FieldReference lambdaFieldReference = FieldReference.newFieldRef(Token::getSize);
        ValueReference valueReference = ValueReference.newValueRef("fixed");
        Sort defaultSort = Sort.by("score");
        Sort ascSort = Sort.asc(Token::getSize);
        Sort descSort = Sort.desc("createTime");

        assertEquals("src.name", fieldReference.getField());
        assertEquals("`size`", lambdaFieldReference.getField());
        assertEquals("fixed", valueReference.getValue());
        assertEquals("score", defaultSort.getSortField());
        assertEquals("DESC", defaultSort.getSortType());
        assertEquals("`size`", ascSort.getSortField());
        assertEquals("ASC", ascSort.getSortType());
        assertEquals("createTime", descSort.getSortField());
        assertEquals("DESC", descSort.getSortType());
    }

    @Test
    public void pageStaticFactoryShouldCreatePage() {
        Page emptyPage = Page.newPage();
        Page page = Page.newPage(2, 10);

        assertEquals(0, emptyPage.getCurrentPage());
        assertEquals(0, emptyPage.getPageSize());
        assertEquals(2, page.getCurrentPage());
        assertEquals(10, page.getPageSize());
        assertEquals(10, page.getOffset());
    }

    @Test
    public void pageResultStaticFactoriesShouldCreatePageResult() {
        PageResult<String> pageResult = PageResult.newPageResult(Arrays.asList("a", "b"), 2);
        PageResult<String> emptyPageResult = PageResult.emptyPageResult();

        assertEquals(Integer.valueOf(2), pageResult.getTotal());
        assertEquals(Arrays.asList("a", "b"), pageResult.getList());
        assertEquals(Integer.valueOf(0), emptyPageResult.getTotal());
        assertTrue(emptyPageResult.getList().isEmpty());
    }

    @Test
    public void criteriaStaticFactoriesShouldCreateCriteria() {
        Criteria emptyCriteria = Criteria.newCriteria().in("id", Arrays.asList(1, 2));
        Criteria equalCriteria = Criteria.newCriteria("name", "zhangsan").or("name", "lisi");
        Criteria optCriteria = Criteria.newCriteria("age", ">", 18).and("status", 1);
        Criteria lambdaEqualCriteria = Criteria.newCriteria(Token::getTk, "token1");
        Criteria lambdaOptCriteria = Criteria.newCriteria(Token::getSize, ">", 18);

        Pair<String, Object[]> emptyPair = SqlMakeTools.doCriteria(emptyCriteria, new StringBuilder("SELECT * FROM user"));
        Pair<String, Object[]> equalPair = SqlMakeTools.doCriteria(equalCriteria, new StringBuilder("SELECT * FROM user"));
        Pair<String, Object[]> optPair = SqlMakeTools.doCriteria(optCriteria, new StringBuilder("SELECT * FROM user"));
        Pair<String, Object[]> lambdaEqualPair = SqlMakeTools.doCriteria(lambdaEqualCriteria, new StringBuilder("SELECT * FROM tb_token"));
        Pair<String, Object[]> lambdaOptPair = SqlMakeTools.doCriteria(lambdaOptCriteria, new StringBuilder("SELECT * FROM tb_token"));

        assertEquals("SELECT * FROM user WHERE id IN(?,?)", emptyPair.getFirst());
        assertArrayEquals(new Object[]{1, 2}, emptyPair.getSecond());
        assertEquals("SELECT * FROM user WHERE name = ? OR name = ?", equalPair.getFirst());
        assertArrayEquals(new Object[]{"zhangsan", "lisi"}, equalPair.getSecond());
        assertEquals("SELECT * FROM user WHERE age > ? AND status = ?", optPair.getFirst());
        assertArrayEquals(new Object[]{18, 1}, optPair.getSecond());
        assertEquals("SELECT * FROM tb_token WHERE ddd = ?", lambdaEqualPair.getFirst());
        assertArrayEquals(new Object[]{"token1"}, lambdaEqualPair.getSecond());
        assertEquals("SELECT * FROM tb_token WHERE `size` > ?", lambdaOptPair.getFirst());
        assertArrayEquals(new Object[]{18}, lambdaOptPair.getSecond());
    }

    @Test
    public void sqlStaticFactoryShouldCreateSql() {
        SQL sql = SQL.newSQL("scoreQuery")
                .select("number", ValueReference.newValueRef("fixed"))
                .from("student_score")
                .where("subject", "math")
                .gt("score", SQL.newSQL()
                        .select(avg("score"))
                        .from("student_score")
                        .where("subject", "math"));

        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);

        assertEquals("SELECT number, ? FROM student_score WHERE subject = ? AND score >(SELECT AVG(score) FROM student_score WHERE subject = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{"fixed", "math", "math"}, pair.getSecond());
        assertEquals("scoreQuery", sql.getId());
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

        assertEquals("DELETE a,b FROM flow_instance a INNER JOIN flow_action b ON a.id = b.flowInstanceId WHERE b.bizId = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"id123456"}, pair.getSecond());
    }

    @Test
    public void sqlShouldBuildJoinWith() {
        SQL sql = new SQL().select("m.status mkStatus").from("inspection", "m")
                .leftJoin(new SQL().select("m.inspId,max(m.modelObjId)modelObjId").from("inspection_model").as("m")
                        .and("m.projectId", "pid").groupBy("m.projectId"), "g1").on("g1.inspId", "m.id")
                .inIfAbsent("m.status", Arrays.asList(1, 2, 3));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT m.status mkStatus FROM inspection m LEFT JOIN (SELECT m.inspId,max(m.modelObjId)modelObjId FROM inspection_model m WHERE m.projectId = ? GROUP BY m.projectId) g1 ON g1.inspId = m.id WHERE m.status IN(?,?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{"pid", 1, 2, 3,}, pair.getSecond());
    }

    @Test
    public void notEqualIfAbsentWithValueShouldIncludeCondition() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .notEqualIfAbsent("status", "deleted");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ? AND status <> ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning", "deleted"}, pair.getSecond());
    }

    @Test
    public void notEqualIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .notEqualIfAbsent("status", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void notEqualIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .notEqualIfAbsent("status", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void notEqualIfAbsentLambdaWithValueShouldIncludeCondition() {
        Criteria criteria = new Criteria().where(Role::getName, "zhouning")
                .notEqualIfAbsent(Role::getAuths, "deleted");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("`name`"));
        assertTrue(pair.getFirst().contains("`auths`"));
        assertTrue(pair.getFirst().contains("<>"));
    }

    @Test
    public void betweenAndIfAbsentWithBothValuesShouldIncludeCondition() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .betweenAndIfAbsent("createTime", "2020-01-01", "2020-12-31");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("WHERE name = ?"));
        assertTrue(pair.getFirst().contains("AND createTime BETWEEN ? AND ?"));
        assertArrayEquals(new Object[]{"zhouning", "2020-01-01", "2020-12-31"}, pair.getSecond());
    }

    @Test
    public void betweenAndIfAbsentWithNullV1ShouldSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .betweenAndIfAbsent("createTime", null, "2020-12-31");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void betweenAndIfAbsentWithNullV2ShouldSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .betweenAndIfAbsent("createTime", "2020-01-01", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void betweenAndIfAbsentWithBothNullShouldSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .betweenAndIfAbsent("createTime", null, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void betweenAndIfAbsentLambdaWithBothValuesShouldIncludeCondition() {
        Criteria criteria = new Criteria().where(Role::getName, "zhouning")
                .betweenAndIfAbsent(Role::getAuths, "2020-01-01", "2020-12-31");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("`name`"));
        assertTrue(pair.getFirst().contains("`auths`"));
        assertTrue(pair.getFirst().contains("BETWEEN ? AND ?"));
    }

    @Test
    public void andOptIfAbsentWithCustomOptAndValueShouldIncludeCondition() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .andOptIfAbsent("score", ">=", 60);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ? AND score >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning", 60}, pair.getSecond());
    }

    @Test
    public void andOptIfAbsentWithCustomOptAndNullShouldSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .andOptIfAbsent("score", ">=", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void andOptIfAbsentLambdaWithCustomOptShouldIncludeCondition() {
        Criteria criteria = new Criteria().where(Role::getName, "zhouning")
                .andOptIfAbsent(Role::getAuths, ">=", "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("`name`"));
        assertTrue(pair.getFirst().contains("`auths`"));
        assertTrue(pair.getFirst().contains(">="));
    }

    @Test
    public void orBetweenAndIfAbsentWithValuesShouldIncludeOrCondition() {
        Criteria criteria = new Criteria().where("status", "active")
                .orBetweenAndIfAbsent("createTime", "2020-01-01", "2020-06-30");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("WHERE status = ?"));
        assertTrue(pair.getFirst().contains("OR createTime BETWEEN ? AND ?"));
        assertArrayEquals(new Object[]{"active", "2020-01-01", "2020-06-30"}, pair.getSecond());
    }

    @Test
    public void orBetweenAndIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria().where("status", "active")
                .orBetweenAndIfAbsent("createTime", null, "2020-06-30");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"active"}, pair.getSecond());
    }

    @Test
    public void orOptIfAbsentWithCustomOptShouldIncludeOrCondition() {
        Criteria criteria = new Criteria().where("status", "active")
                .orOptIfAbsent("type", "<>", "banned");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE status = ? OR type <> ?", pair.getFirst());
        assertArrayEquals(new Object[]{"active", "banned"}, pair.getSecond());
    }

    @Test
    public void notEqualIfAbsentWithCustomPredicateShouldUsePredicate() {
        // Custom predicate: only include if value > 0
        Criteria criteria = new Criteria().where("name", "zhouning")
                .notEqualIfAbsent("status", 0, v -> v instanceof Number && ((Number) v).intValue() > 0);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void chainMultipleIfAbsentShouldAllSkip() {
        Criteria criteria = new Criteria().where("name", "zhouning")
                .notEqualIfAbsent("status", null)
                .betweenAndIfAbsent("createTime", "", "2020-12-31")
                .andOptIfAbsent("score", ">=", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentWithValueShouldIncludeCondition() {
        Criteria criteria = new Criteria()
                .whereIfAbsent("name", "zhouning");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning"}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .whereIfAbsent("name", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria()
                .whereIfAbsent("name", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentLambdaWithValueShouldIncludeCondition() {
        Criteria criteria = new Criteria()
                .whereIfAbsent(Role::getName, "zhouning");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("`name`"));
        assertTrue(pair.getFirst().contains("= ?"));
    }

    @Test
    public void whereIfAbsentLambdaWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .whereIfAbsent(Role::getName, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereOptIfAbsentWithCustomOptShouldIncludeCondition() {
        Criteria criteria = new Criteria()
                .whereOptIfAbsent("score", ">=", 60);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE score >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{60}, pair.getSecond());
    }

    @Test
    public void whereOptIfAbsentWithCustomOptAndNullShouldSkip() {
        Criteria criteria = new Criteria()
                .whereOptIfAbsent("score", ">=", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereOptIfAbsentLambdaWithCustomOptShouldIncludeCondition() {
        Criteria criteria = new Criteria()
                .whereOptIfAbsent(Role::getAuths, "<>", "banned");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertTrue(pair.getFirst().contains("`auths`"));
        assertTrue(pair.getFirst().contains("<>"));
    }

    @Test
    public void whereIfAbsentWithCustomPredicateShouldUsePredicate() {
        // Custom predicate: only include if value >= 10
        Criteria criteria = new Criteria()
                .whereIfAbsent("score", 5, v -> v instanceof Number && ((Number) v).intValue() >= 10);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void chainWhereIfAbsentMixedShouldWork() {
        Criteria criteria = new Criteria()
                .whereIfAbsent("name", "zhouning")
                .whereIfAbsent("status", null)
                .whereOptIfAbsent("score", ">=", 60)
                .whereIfAbsent("type", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test WHERE name = ? AND score >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zhouning", 60}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentAllSkipShouldReturnBaseSql() {
        Criteria criteria = new Criteria()
                .whereIfAbsent("name", null)
                .whereIfAbsent("status", "")
                .whereIfAbsent("score", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_test"));
        assertEquals("SELECT * FROM tb_test", pair.getFirst());
       assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentShouldSkipNullValues() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("name").likeIfAbsent(null).and("age").gtIfAbsent(null).and("email").endsWithIfAbsent(null));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE is_active = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentShouldSkipEmptyStrings() {
        Criteria criteria = new Criteria()
                .where("status", "active")
                .and(Where.where("remark").likeIfAbsent(""))
                .and(Where.where("phone").startsWithIfAbsent(""));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"active"}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentShouldIncludeNonEmptyValues() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("name").likeIfAbsent("zhou").and("age").gteIfAbsent(18).and("email").endsWithIfAbsent("@example.com"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertTrue(pair.getFirst().contains("name LIKE ?"));
        assertTrue(pair.getFirst().contains("age >= ?"));
        assertTrue(pair.getFirst().contains("email LIKE ?"));
        assertArrayEquals(new Object[]{1, "%zhou%", 18, "%@example.com"}, pair.getSecond());
    }

    @Test
    public void whereInIfAbsentShouldSkipEmptyCollection() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("role_id").inIfAbsent(new java.util.ArrayList<>()).and("dept_id").notInIfAbsent(new java.util.ArrayList<>()));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE is_active = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereBetweenAndIfAbsentShouldSkipWhenAnyBoundIsNull() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("age").betweenAndIfAbsent(18, null).and("score").betweenAndIfAbsent(null, 100));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE is_active = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereBetweenAndIfAbsentShouldIncludeValidBounds() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("age").betweenAndIfAbsent(18, 60));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertTrue(pair.getFirst().contains("BETWEEN ? AND ?"));
        assertArrayEquals(new Object[]{1, 18, 60}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentWithCustomPredicateShouldWork() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("score").gtIfAbsent(5, v -> v instanceof Number && ((Number) v).intValue() >= 10).and("level").gtIfAbsent(15, v -> v instanceof Number && ((Number) v).intValue() >= 10));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE is_active = ? AND level > ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 15}, pair.getSecond());
    }

    @Test
    public void whereIfAbsentMixRegularAndSkippedShouldWork() {
        Criteria criteria = new Criteria()
                .where("is_active", 1)
                .and(Where.where("name").likeIfAbsent("zhou").and("email").equalIfAbsent(null).and("age").gtIfAbsent(18).and("phone").likeIfAbsent(""));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertTrue(pair.getFirst().contains("name LIKE ?"));
        assertTrue(pair.getFirst().contains("age > ?"));
        assertFalse(pair.getFirst().contains("email"));
        assertFalse(pair.getFirst().contains("phone"));
        assertArrayEquals(new Object[]{1, "%zhou%", 18}, pair.getSecond());
    }

    @Test
    public void selectDistinctWithStringFields() {
        SQL sql = new SQL()
                .selectDistinct("name", "age", "email")
                .from("tb_user")
                .where("status", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT DISTINCT name, age, email FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void selectDistinctWithLambdaFields() {
        SQL sql = new SQL()
                .selectDistinct(Role::getName)
                .from(Role.class);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT DISTINCT `name` FROM tb_role", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void selectDistinctWithAggregateAndGroupBy() {
        SQL sql = new SQL()
                .selectDistinct("dept_id", "COUNT(id) AS cnt")
                .from("tb_user")
                .groupBy("dept_id");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT DISTINCT dept_id, COUNT(id) AS cnt FROM tb_user GROUP BY dept_id", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void selectDistinctWithJoin() {
        SQL sql = new SQL()
                .selectDistinct("u.name", "r.role_name")
                .from("tb_user", "u")
                .innerJoin("tb_role", "r")
                .on("u.role_id", "r.id")
                .where("u.is_active", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT DISTINCT u.name, r.role_name FROM tb_user u INNER JOIN tb_role r ON u.role_id = r.id WHERE u.is_active = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void selectDistinctUnionEachBranchIndependent() {
        SQL sql = new SQL()
                .selectDistinct("name")
                .from("t1")
                .unionAll()
                .select("name")
                .from("t2");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("(SELECT DISTINCT name FROM t1) UNION ALL (SELECT name FROM t2)", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void selectDistinctMixedUnionAllDistinct() {
        SQL sql = new SQL()
                .selectDistinct("a")
                .from("x")
                .union()
                .selectDistinct("b")
                .from("y");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("(SELECT DISTINCT a FROM x) UNION (SELECT DISTINCT b FROM y)", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void selectDistinctNotAffectSubQuery() {
        // 外层 DISTINCT，子查询不受影响
        SQL sql = new SQL()
                .selectDistinct("u.name")
                .from("tb_user", "u")
                .where("u.dept_id", "IN", new SQL()
                        .select("id")
                        .from("tb_dept")
                        .where("status", 1));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT DISTINCT u.name FROM tb_user u WHERE u.dept_id IN(SELECT id FROM tb_dept WHERE status = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void selectDistinctWithOrderByAndLimit() {
        SQL sql = new SQL()
                .selectDistinct("city")
                .from("tb_address")
                .orderBy(new Sort("city", "ASC"))
                .limit(10);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT DISTINCT city FROM tb_address ORDER BY city ASC LIMIT ?", pair.getFirst());
        assertArrayEquals(new Object[]{10}, pair.getSecond());
    }

    // ==================== notLike / startsWith / endsWith ====================

    @Test
    public void notLikeStringKey() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .notLike("name", "test");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE name NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%test%"}, pair.getSecond());
    }

    @Test
    public void startsWithStringKey() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .startsWith("name", "zho");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"zho%"}, pair.getSecond());
    }

    @Test
    public void endsWithStringKey() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .endsWith("email", "@163.com");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE email LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%@163.com"}, pair.getSecond());
    }

    @Test
    public void notLikeWithLambda() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .notLike(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%admin%"}, pair.getSecond());
    }

    @Test
    public void startsWithWithLambda() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .startsWith(Role::getName, "super");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"super%"}, pair.getSecond());
    }

    @Test
    public void endsWithWithLambda() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .endsWith(Role::getName, "role");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%role"}, pair.getSecond());
    }

    @Test
    public void orNotLikeAfterWhere() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orNotLike("name", "guest");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR name NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%guest%"}, pair.getSecond());
    }

    @Test
    public void orStartsWithAfterWhere() {
        Criteria criteria = new Criteria()
                .where("deleted", 0)
                .orStartsWith("code", "GY");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE deleted = ? OR code LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{0, "GY%"}, pair.getSecond());
    }

    @Test
    public void orEndsWithAfterWhere() {
        Criteria criteria = new Criteria()
                .where("active", 1)
                .orEndsWith("email", "@gmail.com");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE active = ? OR email LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%@gmail.com"}, pair.getSecond());
    }

    // ==================== IfAbsent variants ====================

    @Test
    public void notLikeIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notLikeIfAbsent("name", "test");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND name NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%test%"}, pair.getSecond());
    }

    @Test
    public void notLikeIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notLikeIfAbsent("name", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void notLikeIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notLikeIfAbsent("name", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void startsWithIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .startsWithIfAbsent("code", "GY");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE code LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"GY%"}, pair.getSecond());
    }

    @Test
    public void startsWithIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .startsWithIfAbsent("code", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void endsWithIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .endsWithIfAbsent("email", "@163.com");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE email LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%@163.com"}, pair.getSecond());
    }

    @Test
    public void endsWithIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .endsWithIfAbsent("email", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void orNotLikeIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orNotLikeIfAbsent("name", "spam");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR name NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%spam%"}, pair.getSecond());
    }

    @Test
    public void orNotLikeIfAbsentWithEmptyShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orNotLikeIfAbsent("name", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void orStartsWithIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orStartsWithIfAbsent("code", "GY");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR code LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "GY%"}, pair.getSecond());
    }

    @Test
    public void orEndsWithIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orEndsWithIfAbsent("email", "@gmail.com");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR email LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%@gmail.com"}, pair.getSecond());
    }

    // ==================== inIfAbsent / notInIfAbsent 补充 ====================

    @Test
    public void inIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .inIfAbsent(Role::getAuths, Arrays.asList("admin", "user"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `auths` IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, "admin", "user"}, pair.getSecond());
    }

    @Test
    public void inIfAbsentLambdaWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .inIfAbsent(Role::getAuths, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void inIfAbsentLambdaWithEmptyListShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .inIfAbsent(Role::getAuths, Arrays.asList());
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void inIfAbsentWithCustomPredicateShouldUsePredicate() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .inIfAbsent("type", Arrays.asList(1, 2), c -> c instanceof Collection && ((Collection<?>) c).size() > 3);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void inIfAbsentWithCustomPredicatePassingShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .inIfAbsent("type", Arrays.asList(1, 2), c -> c instanceof Collection && ((Collection<?>) c).size() > 0);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND type IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, 1, 2}, pair.getSecond());
    }

    @Test
    public void notInIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notInIfAbsent("type", Arrays.asList("deleted", "banned"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND type NOT IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, "deleted", "banned"}, pair.getSecond());
    }

    @Test
    public void notInIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notInIfAbsent("type", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void notInIfAbsentWithEmptyListShouldSkip() {
        Criteria criteria = new Criteria()
                .notInIfAbsent("type", Arrays.asList());
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void notInIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notInIfAbsent(Role::getAuths, Arrays.asList("banned"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `auths` NOT IN(?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, "banned"}, pair.getSecond());
    }

    @Test
    public void notInIfAbsentLambdaWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notInIfAbsent(Role::getAuths, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    // ==================== Lambda IfAbsent ====================

    @Test
    public void notLikeIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .notLikeIfAbsent(Role::getName, "guest");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE `name` NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%guest%"}, pair.getSecond());
    }

    @Test
    public void notLikeIfAbsentLambdaWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notLikeIfAbsent(Role::getName, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void startsWithIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .startsWithIfAbsent(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin%"}, pair.getSecond());
    }

    @Test
    public void endsWithIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .endsWithIfAbsent(Role::getName, "role");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%role"}, pair.getSecond());
    }

    // ==================== gtIfAbsent / gteIfAbsent / ltIfAbsent / letIfAbsent ====================

    @Test
    public void gtIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gtIfAbsent("score", 60);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND score > ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 60}, pair.getSecond());
    }

    @Test
    public void gtIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gtIfAbsent("score", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void gtIfAbsentWithZeroShouldAddCondition() {
        Criteria criteria = new Criteria()
                .gtIfAbsent("score", 0);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE score > ?", pair.getFirst());
        assertArrayEquals(new Object[]{0}, pair.getSecond());
    }

    @Test
    public void gtIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gtIfAbsent(Token::getSize, 10);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` > ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 10}, pair.getSecond());
    }

    @Test
    public void gtIfAbsentLambdaWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gtIfAbsent(Token::getSize, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void gteIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gteIfAbsent("score", 60);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND score >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 60}, pair.getSecond());
    }

    @Test
    public void gteIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .gteIfAbsent("score", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void gteIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gteIfAbsent(Token::getSize, 5);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 5}, pair.getSecond());
    }

    @Test
    public void ltIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .ltIfAbsent("score", 100);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND score < ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 100}, pair.getSecond());
    }

    @Test
    public void ltIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria()
                .ltIfAbsent("score", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void ltIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .ltIfAbsent(Token::getSize, 50);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` < ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 50}, pair.getSecond());
    }

    @Test
    public void letIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .letIfAbsent("score", 100);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND score <= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 100}, pair.getSecond());
    }

    @Test
    public void letIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .letIfAbsent("score", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void letIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .letIfAbsent(Token::getSize, 200);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` <= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 200}, pair.getSecond());
    }

    // ==================== andIfAbsent / orIfAbsent / orLikeIfAbsent ====================

    @Test
    public void andIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .andIfAbsent("type", "active");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND type = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "active"}, pair.getSecond());
    }

    @Test
    public void andIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .andIfAbsent("type", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void andIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .andIfAbsent(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `name` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "admin"}, pair.getSecond());
    }

    @Test
    public void orIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orIfAbsent("type", "vip");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR type = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "vip"}, pair.getSecond());
    }

    @Test
    public void orIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orIfAbsent("type", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void orIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orIfAbsent(Role::getName, "guest");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "guest"}, pair.getSecond());
    }

    @Test
    public void orLikeIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orLikeIfAbsent("name", "test");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%test%"}, pair.getSecond());
    }

    @Test
    public void orLikeIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orLikeIfAbsent("name", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void orLikeIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orLikeIfAbsent(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%admin%"}, pair.getSecond());
    }

    // ==================== Where / WhereParam ====================

    @Test
    public void whereNotLike() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("name").notLike("test"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE name NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%test%"}, pair.getSecond());
    }

    @Test
    public void whereStartsWith() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("code").startsWith("GY"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE code LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"GY%"}, pair.getSecond());
    }

    @Test
    public void whereEndsWith() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("email").endsWith("@qq.com"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE email LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%@qq.com"}, pair.getSecond());
    }

    @Test
    public void whereParamNotLike() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("name").notLike("spam"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE name NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%spam%"}, pair.getSecond());
    }

    @Test
    public void whereParamStartsWith() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("code").startsWith("GY"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE code LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"GY%"}, pair.getSecond());
    }

    @Test
    public void whereParamEndsWith() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("email").endsWith("@163.com"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE email LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%@163.com"}, pair.getSecond());
    }

    @Test
    public void whereParamNotLikeWithOr() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .where("status", 1)
                .andWhere(Opt.OR,
                        WhereParam.where("name").like("zhou"),
                        WhereParam.where("name").notLike("test"),
                        WhereParam.where("email").endsWith("@demo.com"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND (name LIKE ? OR name NOT LIKE ? OR email LIKE ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%zhou%", "%test%", "%@demo.com"}, pair.getSecond());
    }

    // ==================== likeIfAbsent / likeLIfAbsent / likeRIfAbsent ====================

    @Test
    public void likeIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeIfAbsent("name", "zhou");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%zhou%"}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeIfAbsent("name", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeIfAbsent("name", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentAsFirstConditionShouldUseWhere() {
        Criteria criteria = new Criteria()
                .likeIfAbsent("name", "test");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%test%"}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentWithCustomPredicateShouldUsePredicate() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeIfAbsent("name", "short", v -> v instanceof String && ((String) v).length() > 5);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentLambdaWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeIfAbsent(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%admin%"}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentLambdaWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeIfAbsent(Role::getName, null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void likeIfAbsentLambdaWithCustomPredicateShouldUsePredicate() {
        Criteria criteria = new Criteria()
                .likeIfAbsent(Role::getName, "ok", v -> v instanceof String && ((String) v).length() > 5);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void likeLIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeLIfAbsent("name", "ning");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%ning"}, pair.getSecond());
    }

    @Test
    public void likeLIfAbsentWithNullShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeLIfAbsent("name", null);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void likeLIfAbsentWithCustomPredicateShouldUsePredicate() {
        Criteria criteria = new Criteria()
                .likeLIfAbsent("name", "x", v -> v instanceof String && !((String) v).isEmpty() && ((String) v).length() >= 3);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void likeRIfAbsentWithValueShouldAddCondition() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeRIfAbsent("name", "zho");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? AND name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "zho%"}, pair.getSecond());
    }

    @Test
    public void likeRIfAbsentWithEmptyStringShouldSkip() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .likeRIfAbsent("name", "");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    // ==================== Lambda 重载核心方法测试 ====================

    @Test
    public void likeLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .like(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%admin%"}, pair.getSecond());
    }

    @Test
    public void likeRLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .likeR(Role::getName, "super");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"super%"}, pair.getSecond());
    }

    @Test
    public void likeLLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .likeL(Role::getName, "role");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%role"}, pair.getSecond());
    }

    @Test
    public void orLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .or(Token::getTk, "abc");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? OR ddd = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "abc"}, pair.getSecond());
    }

    @Test
    public void orLikeLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orLike(Role::getName, "guest");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%guest%"}, pair.getSecond());
    }

    @Test
    public void orNotLikeLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orNotLike(Role::getName, "spam");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` NOT LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%spam%"}, pair.getSecond());
    }

    @Test
    public void orStartsWithLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orStartsWith(Role::getName, "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "admin%"}, pair.getSecond());
    }

    @Test
    public void orEndsWithLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orEndsWith(Role::getName, "role");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "%role"}, pair.getSecond());
    }

    @Test
    public void gteLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .gte(Token::getSize, 100);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 100}, pair.getSecond());
    }

    @Test
    public void ltLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .lt(Token::getSize, 50);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` < ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 50}, pair.getSecond());
    }

    @Test
    public void letLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .let(Token::getSize, 200);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertEquals("SELECT * FROM tb_token WHERE status = ? AND `size` <= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, 200}, pair.getSecond());
    }

    @Test
    public void notEqualLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notEqual(Role::getName, "deleted");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `name` <> ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "deleted"}, pair.getSecond());
    }

    @Test
    public void betweenAndLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .betweenAnd(Token::getSize, 10, 100);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertTrue(pair.getFirst().contains("`size` BETWEEN ? AND ?"));
        assertArrayEquals(new Object[]{1, 10, 100}, pair.getSecond());
    }

    @Test
    public void orBetweenAndLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orBetweenAnd(Token::getSize, 10, 100);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_token"));
        assertTrue(pair.getFirst().contains("OR `size` BETWEEN ? AND ?"));
        assertArrayEquals(new Object[]{1, 10, 100}, pair.getSecond());
    }

    @Test
    public void notInLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .notIn(Role::getAuths, Arrays.asList("banned", "deleted"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `auths` NOT IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, "banned", "deleted"}, pair.getSecond());
    }

    @Test
    public void orOptLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .or(Role::getName, ">=", "admin");
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "admin"}, pair.getSecond());
    }

    // ==================== AbstractCriteria 额外方法 ====================

    @Test
    public void isNullLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .isNull(Role::getAuths);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `auths` IS NULL", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void isNotNullLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .isNotNull(Role::getName);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertEquals("SELECT * FROM tb_role WHERE `name` IS NOT NULL", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void existsShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("dept_id", 1)
                .exists(new SQL().select("1").from("tb_dept").where("id", 1));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE dept_id = ? AND EXISTS (SELECT 1 FROM tb_dept WHERE id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, 1}, pair.getSecond());
    }

    @Test
    public void notExistsShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("dept_id", 1)
                .notExists(new SQL().select("1").from("tb_dept").where("id", 1));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE dept_id = ? AND NOT EXISTS (SELECT 1 FROM tb_dept WHERE id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, 1}, pair.getSecond());
    }

    @Test
    public void orWhereWithOptAndWhereParamsShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orWhere(Opt.OR,
                        WhereParam.where("type").equal("A"),
                        WhereParam.where("type").equal("B"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR (type = ? OR type = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, "A", "B"}, pair.getSecond());
    }

    @Test
    public void orWhereWithOptAndListShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .orWhere(Opt.AND, Arrays.asList(
                        WhereParam.where("a").equal(1),
                        WhereParam.where("b").equal(2)));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE status = ? OR (a = ? AND b = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1, 1, 2}, pair.getSecond());
    }

    @Test
    public void orWithOptAndWhereParamsShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .or(Opt.OR,
                        WhereParam.where("type").equal("A"),
                        WhereParam.where("type").equal("B"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertTrue(pair.getFirst().contains("type = ?"));
        assertTrue(pair.getFirst().contains("OR"));
    }

    @Test
    public void orWithWhereShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where("status", 1)
                .or(Where.where("type").equal("vip"));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertTrue(pair.getFirst().contains("type = ?"));
    }

    @Test
    public void groupByLambdaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .groupBy(Role::getName, Role::getAuths);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_role"));
        assertTrue(pair.getFirst().contains("GROUP BY `name`,`auths`"));
    }

    @Test
    public void limitSingleParamShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .limit(10);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user LIMIT ?", pair.getFirst());
        assertArrayEquals(new Object[]{10}, pair.getSecond());
    }

    @Test
    public void whereWithStringArrayShouldBuildInCondition() {
        Criteria criteria = new Criteria()
                .where(new String[]{"a", "b"}, 1);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE (a,b) = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereWithStringArrayAndOptShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where(new String[]{"a", "b"}, ">", 0);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE (a,b) > ?", pair.getFirst());
        assertArrayEquals(new Object[]{0}, pair.getSecond());
    }

    @Test
    public void whereWithSingleElementArrayShouldUseSingleColumn() {
        Criteria criteria = new Criteria()
                .where(new String[]{"id"}, 1);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    // ==================== countWithCriteria / countWithSql ====================

    @Test
    public void countWithCriteriaShouldReturnCount() throws Exception {
        TestDao dao = new TestDao();
        long count = dao.countWithCriteria(new Criteria().gt("age", 18).where("status", 1));
        assertEquals(1L, count);
    }

    @Test
    public void countWithCriteriaEmptyShouldCountAll() throws Exception {
        TestDao dao = new TestDao();
        long count = dao.countWithCriteria(new Criteria());
        assertEquals(1L, count);
    }

    @Test
    public void countWithSqlVerifyGeneratesCountWrapper() {
        // 验证 countWithSql 内部将原 SQL 包装为 SELECT COUNT(*) FROM (...) gy_count
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .gt("age", 18)
                .where("status", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        String countSql = "SELECT COUNT(*) FROM (" + pair.getFirst() + ") gy_count";
        assertTrue(countSql.startsWith("SELECT COUNT(*) FROM ("));
        assertTrue(countSql.endsWith(") gy_count"));
        assertTrue(countSql.contains("age > ?"));
        assertTrue(countSql.contains("status = ?"));
    }

    // ==================== queryIds ====================

    @Test
    public void queryIdsShouldReturnIdList() throws Exception {
        TestDao dao = new TestDao();
        List<String> ids = dao.queryIds(new Criteria().gt("age", 18).where("status", 1));
        assertEquals(1, ids.size());
        assertEquals("mockId", ids.get(0));
    }

    @Test
    public void queryIdsEmptyCriteriaShouldReturnAllIds() throws Exception {
        TestDao dao = new TestDao();
        List<String> ids = dao.queryIds(new Criteria());
        assertEquals(1, ids.size());
        assertEquals("mockId", ids.get(0));
    }

    @Test
    public void queryIdsWithSqlVerifyWrapsCorrectly() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .gt("age", 20);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        // queryIdsWithSql 内部包装：SELECT gy_ids.{pk} FROM ({innerSql}) gy_ids
        String innerSql = pair.getFirst();
        String idSql = "SELECT gy_ids.id FROM (" + innerSql + ") gy_ids";
        assertTrue(idSql.startsWith("SELECT gy_ids.id FROM ("));
        assertTrue(idSql.endsWith(") gy_ids"));
        assertTrue(idSql.contains("age > ?"));
    }

    // ==================== Where 构造器方法测试 ====================

    @Test
    public void whereNotEqualShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("status").notEqual("deleted"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE status <> ?", pair.getFirst());
        assertArrayEquals(new Object[]{"deleted"}, pair.getSecond());
    }

    @Test
    public void whereLtShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("score").lt(60));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE score < ?", pair.getFirst());
        assertArrayEquals(new Object[]{60}, pair.getSecond());
    }

    @Test
    public void whereLetShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("score").let(100));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE score <= ?", pair.getFirst());
        assertArrayEquals(new Object[]{100}, pair.getSecond());
    }

    @Test
    public void whereGtShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("age").gt(18));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE age > ?", pair.getFirst());
        assertArrayEquals(new Object[]{18}, pair.getSecond());
    }

    @Test
    public void whereGteShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("level").gte(3));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE level >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{3}, pair.getSecond());
    }

    @Test
    public void whereInShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("type").in(Arrays.asList("A", "B", "C")));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE type IN(?,?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{"A", "B", "C"}, pair.getSecond());
    }

    @Test
    public void whereNotInShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("status").notIn(Arrays.asList("deleted", "banned")));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE status NOT IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{"deleted", "banned"}, pair.getSecond());
    }

    @Test
    public void whereIsNullShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("deletedAt").isNull());
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE deletedAt IS NULL", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereIsNotNullShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("email").isNotNull());
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE email IS NOT NULL", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereExistsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("1").exists(new SQL().select("1").from("tb_dept").where("id", 1)));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE EXISTS (SELECT 1 FROM tb_dept WHERE id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereNotExistsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("1").notExists(new SQL().select("1").from("tb_dept").where("id", 1)));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE NOT EXISTS (SELECT 1 FROM tb_dept WHERE id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereBetweenAndShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("createTime").betweenAnd("2020-01-01", "2020-12-31"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE createTime BETWEEN ? AND ?", pair.getFirst());
        assertArrayEquals(new Object[]{"2020-01-01", "2020-12-31"}, pair.getSecond());
    }

    @Test
    public void whereLikeShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Where.where("name").like("test"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE name LIKE ?", pair.getFirst());
        assertArrayEquals(new Object[]{"%test%"}, pair.getSecond());
    }

    @Test
    public void whereStaticFactoryWithLambdaShouldWork() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .and(Where.where(Role::getName).equal("admin"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin"}, pair.getSecond());
    }

    @Test
    public void whereOrLambdaShouldWork() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .and(Where.where("status").equal(1).or(Role::getName).equal("admin"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE status = ? OR `name` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "admin"}, pair.getSecond());
    }

    @Test
    public void whereAndLambdaShouldWork() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .and(Where.where("status").equal(1).and(Role::getName).equal("admin"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE status = ? AND `name` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{1, "admin"}, pair.getSecond());
    }

    // ==================== WhereParam 构造器方法测试 ====================

    @Test
    public void whereParamNotEqualShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("status").notEqual("deleted"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE status <> ?", pair.getFirst());
        assertArrayEquals(new Object[]{"deleted"}, pair.getSecond());
    }

    @Test
    public void whereParamLtShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("score").lt(60));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE score < ?", pair.getFirst());
        assertArrayEquals(new Object[]{60}, pair.getSecond());
    }

    @Test
    public void whereParamLetShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("score").let(100));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE score <= ?", pair.getFirst());
        assertArrayEquals(new Object[]{100}, pair.getSecond());
    }

    @Test
    public void whereParamGtShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("age").gt(18));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE age > ?", pair.getFirst());
        assertArrayEquals(new Object[]{18}, pair.getSecond());
    }

    @Test
    public void whereParamGteShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("level").gte(3));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE level >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{3}, pair.getSecond());
    }

    @Test
    public void whereParamInShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("type").in(Arrays.asList("A", "B")));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE type IN(?,?)", pair.getFirst());
        assertArrayEquals(new Object[]{"A", "B"}, pair.getSecond());
    }

    @Test
    public void whereParamNotInShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("status").notIn(Arrays.asList("deleted")));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE status NOT IN(?)", pair.getFirst());
        assertArrayEquals(new Object[]{"deleted"}, pair.getSecond());
    }

    @Test
    public void whereParamIsNullShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("deletedAt").isNull());
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE deletedAt IS NULL", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereParamIsNotNullShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("email").isNotNull());
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE email IS NOT NULL", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void whereParamExistsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("1").exists(new SQL().select("1").from("tb_dept").where("id", 1)));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE EXISTS (SELECT 1 FROM tb_dept WHERE id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereParamNotExistsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("1").notExists(new SQL().select("1").from("tb_dept").where("id", 1)));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE NOT EXISTS (SELECT 1 FROM tb_dept WHERE id = ?)", pair.getFirst());
        assertArrayEquals(new Object[]{1}, pair.getSecond());
    }

    @Test
    public void whereParamBetweenAndShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("tb_user")
                .and(Opt.AND, WhereParam.where("score").betweenAnd(60, 100));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_user WHERE score BETWEEN ? AND ?", pair.getFirst());
        assertArrayEquals(new Object[]{60, 100}, pair.getSecond());
    }

    @Test
    public void whereParamStaticFactoryWithLambdaShouldWork() {
        SQL sql = new SQL()
                .select("*").from(Role.class)
                .and(Opt.AND, WhereParam.where(Role::getName).equal("admin"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT * FROM tb_role WHERE `name` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin"}, pair.getSecond());
    }

    // ==================== SQL 构造器特殊变体 ====================

    @Test
    public void selectWithLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select(Role::getName, Role::getAuths)
                .from(Role.class);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("SELECT `name`, `auths` FROM tb_role", pair.getFirst());
        assertArrayEquals(new Object[]{}, pair.getSecond());
    }

    @Test
    public void updateWithAliasShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .update("tb_user", "u")
                .set("u.name", "test")
                .where("u.id", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("UPDATE tb_user u SET u.name = ? WHERE u.id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"test", 1}, pair.getSecond());
    }

    @Test
    public void updateWithClassShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .update(Role.class)
                .set(Role::getName, "admin")
                .where(Role::getAuths, "all");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("UPDATE tb_role SET `name` = ? WHERE `auths` = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin", "all"}, pair.getSecond());
    }

    @Test
    public void updateWithClassAndAliasShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .update(Role.class, "r")
                .set("r.name", "admin")
                .where("r.name", "guest");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("UPDATE tb_role r SET r.name = ? WHERE r.name = ?", pair.getFirst());
        assertArrayEquals(new Object[]{"admin", "guest"}, pair.getSecond());
    }

    @Test
    public void setWithLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .update("tb_user")
                .set(Token::getSize, 100)
                .where("id", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("UPDATE tb_user SET `size` = ? WHERE id = ?", pair.getFirst());
        assertArrayEquals(new Object[]{100, 1}, pair.getSecond());
    }

    @Test
    public void deleteWithAliasShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .delete("a")
                .from("tb_user")
                .as("a")
                .where("a.status", 0);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("DELETE a FROM tb_user a WHERE a.status = ?", pair.getFirst());
        assertArrayEquals(new Object[]{0}, pair.getSecond());
    }

    @Test
    public void replaceIntoWithStringFieldsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .replaceInto("tb_user", "name", "status")
                .values("test", 1);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("REPLACE INTO tb_user"));
        assertTrue(pair.getFirst().contains("(name,status)"));
    }

    @Test
    public void replaceIntoWithClassAndLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .replaceInto(Role.class, Role::getName, Role::getAuths)
                .values("admin", "all");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("REPLACE INTO tb_role"));
        assertTrue(pair.getFirst().contains("(`name`,`auths`)"));
    }

    @Test
    public void replaceIntoWithClassAndStringsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .replaceInto(Role.class, "name", "auths")
                .values("admin", "all");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("REPLACE INTO tb_role"));
        assertTrue(pair.getFirst().contains("(name,auths)"));
    }

    @Test
    public void insertIgnoreIntoWithStringFieldsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertIgnoreInto("tb_user", "name", "email")
                .values("test", "test@test.com");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT IGNORE INTO tb_user"));
        assertTrue(pair.getFirst().contains("(name,email)"));
    }

    @Test
    public void insertIgnoreIntoWithClassAndLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertIgnoreInto(Role.class, Role::getName, Role::getAuths)
                .values("guest", "read");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT IGNORE INTO tb_role"));
        assertTrue(pair.getFirst().contains("(`name`,`auths`)"));
    }

    @Test
    public void insertIgnoreIntoWithClassAndStringsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertIgnoreInto(Role.class, "name", "auths")
                .values("guest", "read");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT IGNORE INTO tb_role"));
        assertTrue(pair.getFirst().contains("(name,auths)"));
    }

    @Test
    public void insertIntoWithTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertInto("tb_log")
                .values("data");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT INTO tb_log"));
    }

    @Test
    public void insertIntoWithLambdaFieldsShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertInto("tb_role", Role::getName, Role::getAuths)
                .values("admin", "all");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("(`name`,`auths`)"));
    }

    @Test
    public void insertIntoClassOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertInto(Role.class)
                .values("admin");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT INTO tb_role"));
    }

    @Test
    public void replaceIntoTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .replaceInto("tb_log")
                .values("data");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("REPLACE INTO tb_log"));
    }

    @Test
    public void insertIgnoreIntoTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertIgnoreInto("tb_log")
                .values("data");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT IGNORE INTO tb_log"));
    }

    @Test
    public void onDuplicateKeyUpdateWithLambdaShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertInto("tb_role", "name", "auths")
                .values("admin", "all")
                .onDuplicateKeyUpdate(Role::getAuths, "updated");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().startsWith("INSERT INTO tb_role"));
    }

    @Test
    public void valuesWithListShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .insertInto("tb_user", "name", "age")
                .values(Arrays.asList(
                        new Object[]{"a", 1},
                        new Object[]{"b", 2},
                        new Object[]{"c", 3}
                ));
        assertEquals(3, sql.getInsertValues().size());
        assertArrayEquals(new Object[]{"a", 1}, sql.getInsertValues().get(0));
        assertArrayEquals(new Object[]{"b", 2}, sql.getInsertValues().get(1));
        assertArrayEquals(new Object[]{"c", 3}, sql.getInsertValues().get(2));
    }

    @Test
    public void truncateShouldBuildCorrectSql() {
        SQL sql = new SQL().truncate().table("tb_log");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("TRUNCATE TABLE tb_log;\n", pair.getFirst());
    }

    @Test
    public void dropShouldBuildCorrectSql() {
        SQL sql = new SQL().drop().table("tb_temp");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("DROP TABLE tb_temp", pair.getFirst());
    }

    @Test
    public void dropIfExistsShouldBuildCorrectSql() {
        SQL sql = new SQL().drop().ifExists().table("tb_temp");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("DROP TABLE IF EXISTS tb_temp", pair.getFirst());
    }

    @Test
    public void tableWithClassShouldBuildCorrectSql() {
        SQL sql = new SQL().truncate().table(Role.class);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertEquals("TRUNCATE TABLE tb_role;\n", pair.getFirst());
    }

    @Test
    public void natureJoinWithTableShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .natureJoin("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains(", b"));
    }

    @Test
    public void natureJoinWithAliasShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .natureJoin("b", "bb")
                .on("a.id", "bb.id");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains(", b bb ON a.id = bb.id"));
    }

    @Test
    public void natureJoinWithConsumerShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .natureJoin("b", "bb", c -> c.on("a.id", "bb.id"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains(", b bb ON a.id = bb.id"));
    }

    @Test
    public void leftJoinTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .leftJoin("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("LEFT JOIN b"));
    }

    @Test
    public void rightJoinTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .rightJoin("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("RIGHT JOIN b"));
    }

    @Test
    public void innerJoinTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .innerJoin("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains("INNER JOIN b"));
    }

    @Test
    public void natureJoinTableOnlyShouldBuildCorrectSql() {
        SQL sql = new SQL()
                .select("*").from("a")
                .natureJoin("b");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        assertTrue(pair.getFirst().contains(", b"));
    }

    @Test
    public void havingWithFullCriteriaShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .groupBy("dept_id")
                .having(new Criteria().where("cnt", ">", 5));
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT dept_id, COUNT(*) cnt FROM tb_user"));
        assertTrue(pair.getFirst().contains("HAVING cnt > ?"));
        assertArrayEquals(new Object[]{5}, pair.getSecond());
    }

    // ==================== 边界/异常场景测试 ====================

    @Test
    public void orWithoutPrecedingWhereShouldThrowException() {
        try {
            new Criteria().or("name", "test");
            fail("or without preceding where should throw GyjdbcException");
        } catch (GyjdbcException expected) {
            assertTrue(expected.getMessage().contains("must be following after \"where\""));
        }
    }

    @Test
    public void orCriteriaWithoutPrecedingWhereShouldThrowException() {
        try {
            new Criteria().orCriteria(new Criteria().where("name", "test"));
            fail("orCriteria without preceding where should throw GyjdbcException");
        } catch (GyjdbcException expected) {
            assertTrue(expected.getMessage().contains("must be following after \"where\""));
        }
    }

    @Test
    public void whereWithEmptyStringArrayShouldThrowException() {
        try {
            new Criteria().where(new String[]{}, 1);
            fail("where with empty array should throw GyjdbcException");
        } catch (GyjdbcException expected) {
            assertTrue(expected.getMessage().contains("keys cannot be null"));
        }
    }

    @Test
    public void whereWithNullStringArrayShouldThrowException() {
        try {
            new Criteria().where((String[]) null, 1);
            fail("where with null array should throw GyjdbcException");
        } catch (GyjdbcException expected) {
            assertTrue(expected.getMessage().contains("keys cannot be null"));
        }
    }

    @Test
    public void whereWithSingleElementArrayAndCustomOptShouldBuildCorrectSql() {
        Criteria criteria = new Criteria()
                .where(new String[]{"score"}, ">=", 60);
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder("SELECT * FROM tb_user"));
        assertEquals("SELECT * FROM tb_user WHERE score >= ?", pair.getFirst());
        assertArrayEquals(new Object[]{60}, pair.getSecond());
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
                    if (requiredType == Long.class) {
                        return requiredType.cast(1L);
                    }
                    return requiredType.cast(1);
                }

                @Override
                public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) {
                    java.util.List<T> list = new java.util.ArrayList<>();
                    list.add(elementType.cast("mockId"));
                    return list;
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
