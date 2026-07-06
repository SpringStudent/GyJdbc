package com.gysoft.jdbc;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

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
