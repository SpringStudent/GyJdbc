package com.gysoft.jdbc.jdbctest;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.entity.MemberEntity;
import com.gysoft.jdbc.entity.MemberNameAgeDto;
import com.gysoft.jdbc.entity.OrderEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * EntityDaoImpl 与 H2（MODE=MySQL）的真实集成测试。
 * <p>覆盖：实体映射（继承/static-transient/@Column/java.time）、
 * save/update/delete/saveAll/batchSave/batchUpdate/saveOrUpdate、
 * 分页（LIMIT ?,? 参数化 + COUNT 子查询）、条件查询/统计/存在性/主键列表、
 * SQL 入参方法（查询/聚合/更新/删除/Insert/Create/drunk）、union/子查询、
 * inner/left/right join 连接查询、andCriteria/orCriteria 嵌套条件、
 * xxxIfAbsent 可选条件（null/空白/空集合跳过、自定义断言）等与真实数据库强相关的行为。</p>
 *
 * @author 周宁
 */
public class EntityDaoImplIT extends AbstractJdbcIT {

    @Test
    public void saveThenQueryOneMapsAllColumns() {
        LocalDateTime createTime = LocalDateTime.of(2026, 8, 12, 10, 30, 15);
        MemberEntity member = newMember(1, "zhangsan", 18);
        member.setCreateTime(createTime);

        assertEquals(1, memberDao.save(member));

        MemberEntity found = memberDao.queryOne(1);
        assertNotNull(found);
        assertEquals("zhangsan", found.getName());
        assertEquals(Integer.valueOf(18), found.getAge());
        assertEquals(createTime, found.getCreateTime());      // java.time 往返
        assertEquals("zhangsan@example.com", found.getEmailAddr()); // @Column(name="email_addr") 映射
        assertEquals(new BigDecimal("99.50"), found.getScore());
        assertEquals(Boolean.TRUE, found.getActive());
        assertNotNull(found.getUpdateTime());                 // 父类继承字段
        assertNotNull(found.getId());                         // 父类继承主键
    }

    @Test
    public void autoIncrementPkWhenIdNull() {
        MemberEntity member = newMember(0, "lisi", 20);
        member.setId(null);

        memberDao.save(member);

        List<MemberEntity> all = memberDao.queryAll();
        assertEquals(1, all.size());
        assertNotNull("自增主键应被数据库生成", all.get(0).getId());
        assertEquals("lisi", all.get(0).getName());
    }

    @Test
    public void updateOnlyChangesSetFields() {
        memberDao.save(newMember(1, "zhangsan", 18));
        MemberEntity member = newMember(1, "zhangsan-updated", 28);
        member.setCreateTime(LocalDateTime.of(2026, 8, 12, 10, 30));

        assertEquals(1, memberDao.update(member));

        MemberEntity found = memberDao.queryOne(1);
        assertEquals("zhangsan-updated", found.getName());
        assertEquals(Integer.valueOf(28), found.getAge());
        assertEquals(new BigDecimal("99.50"), found.getScore());
        assertEquals("zhangsan-updated@example.com", found.getEmailAddr());
        assertNotNull(found.getUpdateTime());
    }

    @Test
    public void deleteByPk() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        assertEquals(1, memberDao.delete(1));
        assertNull(memberDao.queryOne(1));
        assertNotNull(memberDao.queryOne(2));
    }

    @Test
    public void batchDeleteByPks() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        assertEquals(2, memberDao.batchDelete(Arrays.asList(1, 3)));

        assertEquals(1, memberDao.queryAll().size());
        assertNull(memberDao.queryOne(1));
        assertNull(memberDao.queryOne(3));
        assertNotNull(memberDao.queryOne(2));
    }

    @Test
    public void queryAllReturnsAllRows() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        List<MemberEntity> all = memberDao.queryAll();
        assertEquals(3, all.size());
    }

    @Test
    public void batchSaveInsertsInBatches() {
        memberDao.batchSave(Arrays.asList(
                newMember(1, "a", 10),
                newMember(2, "b", 20),
                newMember(3, "c", 30),
                newMember(4, "d", 40)));

        assertEquals(4, memberDao.queryAll().size());
        assertNotNull(memberDao.queryOne(4));
    }

    @Test
    public void saveAllMultiRowInsertReturnsRowCount() {
        int rows = memberDao.saveAll(Arrays.asList(
                newMember(1, "a", 10),
                newMember(2, "b", 20),
                newMember(3, "c", 30)));

        assertEquals(3, rows);
        assertEquals(3, memberDao.queryAll().size());
        assertEquals("b", memberDao.queryOne(2).getName());
    }

    @Test
    public void batchUpdateUpdatesEachRow() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        MemberEntity m1 = memberDao.queryOne(1);
        m1.setName("a1");
        MemberEntity m2 = memberDao.queryOne(2);
        m2.setName("b1");
        memberDao.batchUpdate(Arrays.asList(m1, m2));

        assertEquals("a1", memberDao.queryOne(1).getName());
        assertEquals("b1", memberDao.queryOne(2).getName());
    }

    @Test
    public void saveOrUpdateInsertsThenUpdates() {
        // 不存在 -> insert
        memberDao.saveOrUpdate(newMember(1, "first", 18));
        assertNotNull(memberDao.queryOne(1));

        // 已存在 -> update
        MemberEntity member = newMember(1, "second", 28);
        member.setCreateTime(LocalDateTime.of(2026, 8, 12, 10, 30));
        memberDao.saveOrUpdate(member);

        MemberEntity found = memberDao.queryOne(1);
        assertEquals("second", found.getName());
        assertEquals(Integer.valueOf(28), found.getAge());
        // 不产生重复记录
        assertEquals(1, memberDao.queryAll().size());
    }

    @Test
    public void pageQueryReturnsPageAndTotal() {
        for (int i = 1; i <= 5; i++) {
            memberDao.save(newMember(i, "name" + i, 10 + i));
        }

        PageResult<MemberEntity> page1 = memberDao.pageQuery(new Page(1, 2));
        assertEquals(2, page1.getList().size());
        assertEquals(5, page1.getTotal().intValue());

        PageResult<MemberEntity> page2 = memberDao.pageQuery(new Page(2, 2));
        assertEquals(2, page2.getList().size());
        assertEquals(5, page2.getTotal().intValue());

        PageResult<MemberEntity> page3 = memberDao.pageQuery(new Page(3, 2));
        assertEquals(1, page3.getList().size());
    }

    @Test
    public void pageQueryWithCriteriaFiltersAndCounts() {
        // age: 11..15，其中 >12 的为 3 条（13,14,15）
        for (int i = 1; i <= 5; i++) {
            memberDao.save(newMember(i, "name" + i, 10 + i));
        }
        Criteria criteria = Criteria.newCriteria().gt("age", 12).orderBy(Sort.asc("age"));

        PageResult<MemberEntity> page = memberDao.pageQueryWithCriteria(new Page(1, 2), criteria);

        assertEquals(3, page.getTotal().intValue());
        assertEquals(2, page.getList().size());
        // orderBy age asc 过滤后为 13,14,15，第一页取前两条
        assertEquals(Integer.valueOf(13), page.getList().get(0).getAge());
        assertEquals(Integer.valueOf(14), page.getList().get(1).getAge());
    }

    @Test
    public void queryWithCriteriaAndSort() {
        memberDao.save(newMember(1, "a", 30));
        memberDao.save(newMember(2, "b", 10));
        memberDao.save(newMember(3, "c", 20));

        List<MemberEntity> byAge = memberDao.queryWithCriteria(
                Criteria.newCriteria().orderBy(Sort.asc("age")));
        assertEquals(3, byAge.size());
        assertEquals("b", byAge.get(0).getName()); // age 10
        assertEquals("c", byAge.get(1).getName()); // age 20

        List<MemberEntity> filtered = memberDao.queryWithCriteria(
                Criteria.newCriteria().gt("age", 15));
        assertEquals(2, filtered.size());
        assertEquals("a", filtered.get(0).getName());

        List<MemberEntity> like = memberDao.queryWithCriteria(
                Criteria.newCriteria().like("name", "%a%"));
        assertEquals(1, like.size());
        assertEquals("a", like.get(0).getName());
    }

    @Test
    public void countAndExistsWithCriteria() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        assertEquals(2L, memberDao.countWithCriteria(Criteria.newCriteria()));
        assertEquals(1L, memberDao.countWithCriteria(Criteria.newCriteria().gt("age", 15)));
        assertTrue(memberDao.existsWithCriteria(Criteria.newCriteria().where("name", "a")));
        assertFalse(memberDao.existsWithCriteria(Criteria.newCriteria().where("name", "zzz")));
    }

    @Test
    public void queryIdsReturnsPkList() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        List<Integer> ids = memberDao.queryIds(Criteria.newCriteria().gt("age", 15));
        assertEquals(2, ids.size());
        assertTrue(ids.contains(2));
        assertTrue(ids.contains(3));
    }

    @Test
    public void queryOneByCriteriaAndOptional() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        MemberEntity one = memberDao.queryOne(Criteria.newCriteria().where("name", "b"));
        assertNotNull(one);
        Assert.assertEquals(Integer.valueOf(2), one.getId());

        Optional<MemberEntity> present = memberDao.queryOneOpt(Criteria.newCriteria().where("name", "a"));
        assertTrue(present.isPresent());

        Optional<MemberEntity> empty = memberDao.queryOneOpt(Criteria.newCriteria().where("name", "zzz"));
        assertFalse(empty.isPresent());
    }

    @Test
    public void deleteWithCriteria() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        int deleted = memberDao.deleteWithCriteria(Criteria.newCriteria().lt("age", 25));
        assertEquals(2, deleted);
        assertEquals(1, memberDao.queryAll().size());
        assertEquals("c", memberDao.queryAll().get(0).getName());
    }

    @Test
    public void truncateClearsAllData() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        memberDao.truncate();

        assertTrue(memberDao.queryAll().isEmpty());
        assertEquals(0L, memberDao.countWithCriteria(Criteria.newCriteria()));
    }

    @Test
    public void staticAndTransientFieldsNotPersistedAsColumns() {
        // 若 serialVersionUID / temp 被当作列，建表 DDL 会因无对应列而 INSERT 失败；
        // 这里 save 成功即证明字段被正确过滤。
        memberDao.save(newMember(1, "a", 10));
        assertNotNull(memberDao.queryOne(1));

        // 同时校验表元数据中没有 temp / serialVersionUID 列
        List<String> columns = jdbcTemplate.query(
                "SELECT LOWER(COLUMN_NAME) FROM INFORMATION_SCHEMA.COLUMNS WHERE LOWER(TABLE_NAME) = 'tb_member'",
                (rs, i) -> rs.getString(1));
        assertEquals("应恰好 8 个持久化列", 8, columns.size());
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
        assertTrue(columns.contains("createtime"));
        assertTrue(columns.contains("email_addr"));
        assertTrue(columns.contains("age"));
        assertTrue(columns.contains("active"));
        assertTrue(columns.contains("score"));
        assertTrue(columns.contains("updatetime"));
        assertFalse("表不应包含 temp 列", columns.contains("temp"));
        assertFalse("表不应包含 serialVersionUID 列", columns.contains("serialversionuid"));
    }

    @Test
    public void columnAnnotationValueStoredInDb() {
        memberDao.save(newMember(1, "zhangsan", 18));

        // 直接查库，验证 @Column(name="email_addr") 落库列名
        String email = jdbcTemplate.queryForObject(
                "SELECT email_addr FROM tb_member WHERE id = 1", String.class);
        assertEquals("zhangsan@example.com", email);
    }

    // ==================== SQL 入参方法 — 查询类 ====================

    @Test
    public void queryWithSqlResultQueryListOneMaps() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        Result<MemberEntity> result = memberDao.queryWithSql(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class).where("age", ">", 15));

        List<MemberEntity> list = result.queryList();
        assertEquals(1, list.size());
        assertEquals("b", list.get(0).getName());

        MemberEntity one = result.queryOne();
        assertNotNull(one);
        assertEquals("b", one.getName());

        List<Map<String, Object>> maps = result.queryMaps();
        assertEquals(1, maps.size());
        assertEquals("b", maps.get(0).get("name"));
    }

    @Test
    public void queryWithSqlScalarQueryObjectAndForList() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        // queryObject：底层 queryForObject 要求结果恰好一行
        Result<String> single = memberDao.queryWithSql(String.class,
                new SQL().select("name").from(MemberEntity.class).where("id", 1));
        assertEquals("a", single.queryObject());

        // queryForList：多行返回列表
        Result<String> multi = memberDao.queryWithSql(String.class,
                new SQL().select("name").from(MemberEntity.class).orderBy(new Sort("age", "ASC")));
        List<String> names = multi.queryForList();
        assertEquals(Arrays.asList("a", "b"), names);
    }

    @Test
    public void queryListWithSqlReturnsList() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        List<MemberEntity> list = memberDao.queryListWithSql(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class).where("age", ">", 10));

        assertEquals(1, list.size());
        assertEquals("b", list.get(0).getName());
    }

    @Test
    public void queryOneWithSqlReturnsSingle() {
        memberDao.save(newMember(1, "a", 10));

        MemberEntity one = memberDao.queryOneWithSql(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class).where("id", 1));
        assertNotNull(one);
        assertEquals("a", one.getName());

        // default 方法 queryOneWithSqlOpt
        Optional<MemberEntity> present = memberDao.queryOneWithSqlOpt(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class).where("id", 1));
        assertTrue(present.isPresent());
        assertEquals("a", present.get().getName());

        Optional<MemberEntity> empty = memberDao.queryOneWithSqlOpt(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class).where("id", 999));
        assertFalse(empty.isPresent());
    }

    @Test
    public void queryMapsWithSqlReturnsMapList() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        List<Map<String, Object>> maps = memberDao.queryMapsWithSql(
                new SQL().select("name", "age").from(MemberEntity.class).where("age", ">", 10));

        assertEquals(1, maps.size());
        assertEquals("b", maps.get(0).get("name"));
        assertEquals(Integer.valueOf(20), maps.get(0).get("age"));
    }

    @Test
    public void queryMapWithSqlReturnsDoubleColumnMap() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        Map<String, Integer> map = memberDao.queryMapWithSql(
                new SQL().select("name", "age").from(MemberEntity.class).orderBy(new Sort("age", "ASC")),
                ResultSetExractorFactory.createDoubleColumnValueResultSetExtractor(String.class, Integer.class));

        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(10), map.get("a"));
        assertEquals(Integer.valueOf(20), map.get("b"));
    }

    @Test
    public void pageQueryWithSqlReturnsPage() {
        for (int i = 1; i <= 5; i++) {
            memberDao.save(newMember(i, "name" + i, 10 + i));
        }

        PageResult<MemberEntity> page = memberDao.pageQueryWithSql(
                new Page(1, 2), MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class)
                        .where("age", ">", 12).orderBy(new Sort("age", "ASC")));

        assertEquals(3, page.getTotal().intValue());
        assertEquals(2, page.getList().size());
        // orderBy age asc 过滤后为 13,14,15，第一页取前两条
        assertEquals(Integer.valueOf(13), page.getList().get(0).getAge());
        assertEquals(Integer.valueOf(14), page.getList().get(1).getAge());
    }

    // ==================== SQL 入参方法 — 聚合/计数/存在性 ====================

    @Test
    public void queryIntegerWithSqlReturnsCount() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        Integer count = memberDao.queryIntegerWithSql(
                new SQL().select("COUNT(*)").from(MemberEntity.class));
        assertEquals(Integer.valueOf(2), count);
    }

    @Test
    public void countWithSqlReturnsLong() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        long count = memberDao.countWithSql(
                new SQL().select("COUNT(*)").from(MemberEntity.class).where("age", ">", 15));
        assertEquals(1L, count);
    }

    @Test
    public void existsWithSqlReturnsBoolean() {
        memberDao.save(newMember(1, "a", 10));

        assertTrue(memberDao.existsWithSql(
                new SQL().select("*").from(MemberEntity.class).where("name", "a")));
        assertFalse(memberDao.existsWithSql(
                new SQL().select("*").from(MemberEntity.class).where("name", "zzz")));
    }

    // ==================== SQL 入参方法 — 更新/删除 ====================

    @Test
    public void updateWithSqlChangesFields() {
        memberDao.save(newMember(1, "a", 10));

        int updated = memberDao.updateWithSql(
                new SQL().update(MemberEntity.class).set("name", "renamed").where("id", 1));
        assertEquals(1, updated);

        MemberEntity found = memberDao.queryOne(1);
        assertEquals("renamed", found.getName());
        assertEquals(Integer.valueOf(10), found.getAge());
    }

    @Test
    public void deleteWithSqlRemovesRows() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        int deleted = memberDao.deleteWithSql(
                new SQL().delete().from(MemberEntity.class).where("age", ">", 15));
        assertEquals(1, deleted);
        assertNull(memberDao.queryOne(2));
        assertNotNull(memberDao.queryOne(1));
    }

    // ==================== SQL 入参方法 — Insert 类 ====================

    @Test
    public void insertWithSqlValuesInsertsRow() {
        int inserted = memberDao.insertWithSql(
                new SQL().insertInto(MemberEntity.class, "id", "name", "age")
                        .values(1, "sql-insert", 30));
        assertEquals(1, inserted);

        MemberEntity found = memberDao.queryOne(1);
        assertNotNull(found);
        assertEquals("sql-insert", found.getName());
        assertEquals(Integer.valueOf(30), found.getAge());
    }

    @Test
    public void insertWithSqlOnDuplicateKeyUpdate() {
        memberDao.save(newMember(1, "original", 10));

        // 静态值更新：重复键时仅更新 ODKU 指定的列，其余列保持原值
        memberDao.insertWithSql(
                new SQL().insertInto(MemberEntity.class, "id", "name", "age")
                        .values(1, "updated", 20)
                        .onDuplicateKeyUpdate("name", "static-updated"));
        MemberEntity found = memberDao.queryOne(1);
        assertEquals("static-updated", found.getName());
        assertEquals("未在 ODKU 中的列应保持原值", Integer.valueOf(10), found.getAge());

        // VALUES() 引用插入值更新：name/age 均被覆盖
        memberDao.insertWithSql(
                new SQL().insertInto(MemberEntity.class, "id", "name", "age")
                        .values(1, "updated", 20)
                        .onDuplicateKeyUpdateValues("name", "age"));
        found = memberDao.queryOne(1);
        assertEquals("updated", found.getName());
        assertEquals(Integer.valueOf(20), found.getAge());
        // 不产生重复记录
        assertEquals(1, memberDao.queryAll().size());
    }

    @Test
    public void insertWithSqlSelectCopiesRows() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));

        // INSERT INTO tb_member (name,age) SELECT name,age FROM tb_member WHERE age > 10
        int inserted = memberDao.insertWithSql(
                new SQL().insertInto(MemberEntity.class, "name", "age")
                        .select("name", "age").from(MemberEntity.class).where("age", ">", 10));
        assertEquals(1, inserted);
        assertEquals(3, memberDao.queryAll().size());
    }

    // ==================== SQL 入参方法 — Create / Drunk ====================

    @Test
    public void createWithSqlCreatesTable() {
        String tableName = memberDao.createWithSql(
                new SQL().create().table("tb_member_log").ifNotExists()
                        .column(c -> c.name("id").integer().primary())
                        .column(c -> c.name("name").varchar(16))
                        .commit());

        assertEquals("`tb_member_log`", tableName);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_member_log'",
                Integer.class);
        assertEquals(Integer.valueOf(1), count);
    }

    @Test
    public void createWithSqlWithInsertData() {
        String tableName = memberDao.createWithSql(
                new SQL().create().table("tb_member_log_data").ifNotExists()
                        .column(c -> c.name("id").integer().primary().autoIncrement())
                        .column(c -> c.name("name").varchar(16))
                        .commit()
                        .values(null, "first")
                        .values(null, "second"));

        assertEquals("`tb_member_log_data`", tableName);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM tb_member_log_data");
        assertEquals(2, rows.size());
        // 自增主键由数据库生成
        assertTrue(((Number) rows.get(0).get("id")).intValue() > 0);
        assertEquals("first", rows.get(0).get("name"));
        assertEquals("second", rows.get(1).get("name"));
    }

    @Test
    public void drunkTruncatesTable() {
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));
        assertEquals(2, orderDao.queryAll().size());

        memberDao.drunk(new SQL().truncate().table("tb_order"));

        assertEquals(0, orderDao.queryAll().size());
    }

    @Test
    public void drunkDropsTable() {
        // 先建一张临时表再 DROP，避免影响其它用例的表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_drunk_test (id INTEGER PRIMARY KEY, name VARCHAR(16))");
        jdbcTemplate.update("INSERT INTO tb_drunk_test (id,name) VALUES (1,'x')");

        memberDao.drunk(new SQL().drop().ifExists().table("tb_drunk_test"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_drunk_test'",
                Integer.class);
        assertEquals(Integer.valueOf(0), count);
    }

    // ==================== UNION / 子查询（第二张表 tb_order）====================

    @Test
    public void unionTwoTablesDeduplicatesRows() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));

        // SELECT name FROM tb_member WHERE age > 20 UNION SELECT orderNo FROM tb_order WHERE amount > 150
        List<String> rows = memberDao.queryWithSql(String.class,
                        new SQL().select("name").from("tb_member").where("age", ">", 20)
                                .union()
                                .select("orderNo").from("tb_order").where("amount", ">", 150))
                .queryForList();

        assertEquals(2, rows.size());
        assertTrue(rows.contains("lisi"));
        assertTrue(rows.contains("O002"));
    }

    @Test
    public void unionAllKeepsDuplicateRows() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));

        List<String> rows = memberDao.queryWithSql(String.class,
                        new SQL().select("name").from("tb_member")
                                .unionAll()
                                .select("orderNo").from("tb_order"))
                .queryForList();

        // UNION ALL 不去重：4 条数据全部返回
        assertEquals(4, rows.size());
        assertTrue(rows.contains("zhangsan"));
        assertTrue(rows.contains("lisi"));
        assertTrue(rows.contains("O001"));
        assertTrue(rows.contains("O002"));
    }

    @Test
    public void subqueryMembersWithOrdersIn() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        memberDao.save(newMember(3, "wangwu", 40));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));

        // SELECT * FROM tb_member WHERE id IN (SELECT memberId FROM tb_order WHERE amount > 150)
        List<MemberEntity> members = memberDao.queryListWithSql(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class)
                        .in("id", new SQL().select("memberId").from("tb_order").where("amount", ">", 150)));

        assertEquals(1, members.size());
        assertEquals("lisi", members.get(0).getName());
    }

    @Test
    public void subqueryMembersNotInCancelledOrders() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        memberDao.save(newMember(3, "wangwu", 40));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "CANCELLED"));

        // SELECT * FROM tb_member WHERE id NOT IN (SELECT memberId FROM tb_order WHERE status = 'CANCELLED')
        List<MemberEntity> members = memberDao.queryListWithSql(MemberEntity.class,
                new SQL().select("*").from(MemberEntity.class)
                        .notIn("id", new SQL().select("memberId").from("tb_order").where("status", "CANCELLED")));

        assertEquals(2, members.size());
        for (MemberEntity member : members) {
            assertNotEquals("有 CANCELLED 订单的会员不应出现", "lisi", member.getName());
        }
    }

    // ==================== JOIN 连接查询（inner / left / right）====================
    // 数据约定：会员 1(zhangsan) 有 O001、会员 2(lisi) 有 O002、会员 3(wangwu) 无订单、
    // 订单 O999 为孤儿订单（memberId=999 无对应会员）。用 queryMapsWithSql 直接校验连接两侧列。
    // 列别名统一小写（H2 DATABASE_TO_LOWER 下未加引号标识符一律小写），保证 map 键可预测。

    @Test
    public void innerJoinReturnsOnlyMatchedRows() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        memberDao.save(newMember(3, "wangwu", 40));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));
        orderDao.save(newOrder(3, 999, "O999", "300.00", "PAID"));

        List<Map<String, Object>> rows = memberDao.queryMapsWithSql(
                new SQL().select("m.name AS name", "o.orderNo AS orderno")
                        .from(MemberEntity.class, "m")
                        .innerJoin(OrderEntity.class, "o", on -> on.on("m.id", "o.memberId"))
                        .orderBy(new Sort("m.id", "ASC")));

        // 仅双方有匹配的行：zhangsan-O001、lisi-O002；无订单会员与孤儿订单都不出现
        assertEquals(2, rows.size());
        assertEquals("zhangsan", rows.get(0).get("name"));
        assertEquals("O001", rows.get(0).get("orderno"));
        assertEquals("lisi", rows.get(1).get("name"));
        assertEquals("O002", rows.get(1).get("orderno"));
    }

    @Test
    public void innerJoinWithExtraOnConditionAndWhereFilter() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "CANCELLED"));

        // ON 追加 status='PAID' 条件 + WHERE 过滤 m.age>15
        List<Map<String, Object>> rows = memberDao.queryMapsWithSql(
                new SQL().select("m.name AS name", "o.orderNo AS orderno")
                        .from(MemberEntity.class, "m")
                        .innerJoin(OrderEntity.class, "o",
                                on -> on.on("m.id", "o.memberId").and("o.status", "PAID"))
                        .where("m.age", ">", 15)
                        .orderBy(new Sort("m.id", "ASC")));

        // lisi 的订单是 CANCELLED，被 ON 附加条件排除；只剩 zhangsan-O001
        assertEquals(1, rows.size());
        assertEquals("zhangsan", rows.get(0).get("name"));
        assertEquals("O001", rows.get(0).get("orderno"));
    }

    @Test
    public void leftJoinKeepsAllLeftRowsWithNulls() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        memberDao.save(newMember(3, "wangwu", 40));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));

        List<Map<String, Object>> rows = memberDao.queryMapsWithSql(
                new SQL().select("m.name AS name", "o.orderNo AS orderno")
                        .from(MemberEntity.class, "m")
                        .leftJoin(OrderEntity.class, "o", on -> on.on("m.id", "o.memberId"))
                        .orderBy(new Sort("m.id", "ASC")));

        // 左表 tb_member 全部保留，无订单的 wangwu 右侧列为 NULL
        assertEquals(3, rows.size());
        assertEquals("zhangsan", rows.get(0).get("name"));
        assertEquals("O001", rows.get(0).get("orderno"));
        assertEquals("lisi", rows.get(1).get("name"));
        assertEquals("O002", rows.get(1).get("orderno"));
        assertEquals("wangwu", rows.get(2).get("name"));
        assertNull("无订单会员 orderNo 应为 NULL", rows.get(2).get("orderno"));
    }

    @Test
    public void rightJoinKeepsAllRightRowsWithNulls() {
        memberDao.save(newMember(1, "zhangsan", 18));
        memberDao.save(newMember(2, "lisi", 30));
        memberDao.save(newMember(3, "wangwu", 40));
        orderDao.save(newOrder(1, 1, "O001", "100.00", "PAID"));
        orderDao.save(newOrder(2, 2, "O002", "200.00", "PAID"));
        orderDao.save(newOrder(3, 999, "O999", "300.00", "PAID"));

        List<Map<String, Object>> rows = memberDao.queryMapsWithSql(
                new SQL().select("m.name AS name", "o.orderNo AS orderno")
                        .from(MemberEntity.class, "m")
                        .rightJoin(OrderEntity.class, "o", on -> on.on("m.id", "o.memberId"))
                        .orderBy(new Sort("o.id", "ASC")));

        // 右表 tb_order 全部保留，孤儿订单 O999 左侧会员列为 NULL；无订单会员 wangwu 不出现
        assertEquals(3, rows.size());
        assertEquals("zhangsan", rows.get(0).get("name"));
        assertEquals("O001", rows.get(0).get("orderno"));
        assertEquals("lisi", rows.get(1).get("name"));
        assertEquals("O002", rows.get(1).get("orderno"));
        assertNull("孤儿订单无对应会员 name 应为 NULL", rows.get(2).get("name"));
        assertEquals("O999", rows.get(2).get("orderno"));
    }

    // ==================== andCriteria / orCriteria 嵌套条件 ====================

    @Test
    public void andCriteriaGroupsSubConditions() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));
        memberDao.save(newMember(4, "d", 40));

        // age > 15 AND (name = 'b' OR name = 'c')，子条件组带括号
        List<MemberEntity> list = memberDao.queryWithCriteria(
                Criteria.newCriteria()
                        .gt("age", 15)
                        .andCriteria(sub -> sub.where("name", "b").or("name", "c"))
                        .orderBy(Sort.asc("age")));

        assertEquals(2, list.size());
        assertEquals("b", list.get(0).getName());
        assertEquals("c", list.get(1).getName());
    }

    @Test
    public void andCriteriaSkipsWhenBooleanFalse() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));
        memberDao.save(newMember(4, "d", 40));

        // boolean 为 false → 子条件整体跳过 → 等价于 age > 15
        Criteria skipped = Criteria.newCriteria().gt("age", 15)
                .andCriteria(sub -> sub.lt("age", 25), false);
        assertEquals("条件为 false 时子条件被跳过", 3, memberDao.countWithCriteria(skipped));

        // BooleanSupplier 为 true → 子条件生效 → 15 < age < 25
        Criteria applied = Criteria.newCriteria().gt("age", 15)
                .andCriteria(sub -> sub.lt("age", 25), () -> true);
        assertEquals("条件为 true 时子条件生效", 1, memberDao.countWithCriteria(applied));
    }

    @Test
    public void orCriteriaGroupsSubConditions() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));
        memberDao.save(newMember(4, "d", 40));

        // age < 15 OR (name = 'c' OR name = 'd')
        List<MemberEntity> list = memberDao.queryWithCriteria(
                Criteria.newCriteria()
                        .lt("age", 15)
                        .orCriteria(sub -> sub.where("name", "c").or("name", "d"))
                        .orderBy(Sort.asc("age")));

        assertEquals(3, list.size());
        assertEquals("a", list.get(0).getName());
        assertEquals("c", list.get(1).getName());
        assertEquals("d", list.get(2).getName());
    }

    @Test
    public void andCriteriaWithCriteriaObjectAndEmptySkip() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));
        memberDao.save(newMember(4, "d", 40));

        // 直接传 Criteria 对象：age > 15 AND age < 25
        Criteria sub = Criteria.newCriteria().lt("age", 25);
        Criteria criteria = Criteria.newCriteria().gt("age", 15).andCriteria(sub);
        assertEquals(1, memberDao.countWithCriteria(criteria));

        // 空子条件被忽略（不产生条件）→ 等价于 age > 15
        Criteria withEmpty = Criteria.newCriteria().gt("age", 15)
                .andCriteria(Criteria.newCriteria());
        assertEquals("空子条件应被跳过", 3, memberDao.countWithCriteria(withEmpty));
    }

    @Test
    public void andCriteriaOnSqlBuilder() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // SQL 构建器同样支持 andCriteria：age > 10 AND age < 30
        SQL sql = new SQL().select("*").from(MemberEntity.class)
                .where("age", ">", 10)
                .andCriteria(sub -> sub.lt("age", 30));
        List<MemberEntity> list = memberDao.queryListWithSql(MemberEntity.class, sql);

        assertEquals(1, list.size());
        assertEquals("b", list.get(0).getName());
    }

    // ==================== xxxIfAbsent 可选条件 ====================
    // 默认断言：null / 空白字符串 / 空集合 / 空 Map / 空数组 / 空 Optional 都跳过，其余生效。

    @Test
    public void whereIfAbsentSkipsNullAndBlank() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        String nullName = null;
        String blankName = "   ";
        // null / 空白字符串被默认断言跳过 → 无过滤条件 → 查全部
        List<MemberEntity> all = memberDao.queryWithCriteria(
                Criteria.newCriteria()
                        .whereIfAbsent("name", nullName)
                        .whereIfAbsent("name", blankName));
        assertEquals(3, all.size());

        // 非空生效
        List<MemberEntity> filtered = memberDao.queryWithCriteria(
                Criteria.newCriteria().whereIfAbsent("name", "b"));
        assertEquals(1, filtered.size());
        assertEquals("b", filtered.get(0).getName());
    }

    @Test
    public void comparisonIfAbsentSkipsNullAppliesWhenPresent() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // gtIfAbsent(null) 跳过，gtIfAbsent(15) 生效 → age > 15
        Criteria criteria = Criteria.newCriteria()
                .gtIfAbsent("age", null)
                .gtIfAbsent("age", 15);
        assertEquals(2, memberDao.countWithCriteria(criteria)); // 20, 30

        // gteIfAbsent + letIfAbsent 组合 → 10 <= age <= 20
        Criteria range = Criteria.newCriteria()
                .gteIfAbsent("age", 10)
                .letIfAbsent("age", 20);
        assertEquals(2, memberDao.countWithCriteria(range)); // 10, 20
    }

    @Test
    public void andOrIfAbsent() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // andIfAbsent(null) 跳过 → 等价于 name = 'a'
        Criteria andSkip = Criteria.newCriteria().where("name", "a").andIfAbsent("age", null);
        assertEquals(1, memberDao.countWithCriteria(andSkip));

        // andIfAbsent 生效 → name = 'a' AND age = 10
        Criteria andApplied = Criteria.newCriteria().where("name", "a").andIfAbsent("age", 10);
        assertEquals(1, memberDao.countWithCriteria(andApplied));

        // orIfAbsent 生效 → name = 'a' OR age = 30
        Criteria orApplied = Criteria.newCriteria().where("name", "a").orIfAbsent("age", 30);
        assertEquals(2, memberDao.countWithCriteria(orApplied)); // a, c
    }

    @Test
    public void inLikeIfAbsent() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // 空集合被跳过
        Criteria inEmpty = Criteria.newCriteria().inIfAbsent("age", Collections.emptyList());
        assertEquals(3, memberDao.countWithCriteria(inEmpty));

        // 非空集合生效
        Criteria inApplied = Criteria.newCriteria().inIfAbsent("age", Arrays.asList(10, 30));
        assertEquals(2, memberDao.countWithCriteria(inApplied));

        // likeIfAbsent(null) 跳过
        Criteria likeSkip = Criteria.newCriteria().likeIfAbsent("name", null);
        assertEquals(3, memberDao.countWithCriteria(likeSkip));

        // likeIfAbsent 生效（%b%）
        Criteria likeApplied = Criteria.newCriteria().likeIfAbsent("name", "b");
        assertEquals(1, memberDao.countWithCriteria(likeApplied));
    }

    @Test
    public void betweenAndIfAbsentAndCustomPredicate() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // 边界值有 null → 跳过
        Criteria betweenSkip = Criteria.newCriteria().betweenAndIfAbsent("age", null, 30);
        assertEquals(3, memberDao.countWithCriteria(betweenSkip));

        // 都非空生效 → 15 <= age <= 25
        Criteria betweenApplied = Criteria.newCriteria().betweenAndIfAbsent("age", 15, 25);
        assertEquals(1, memberDao.countWithCriteria(betweenApplied)); // 20

        // 自定义断言：仅当 age >= 18 时应用
        Criteria customSkip = Criteria.newCriteria()
                .gtIfAbsent("age", 15, v -> v != null && (Integer) v >= 18);
        assertEquals("15 < 18 断言不通过 → 跳过", 3, memberDao.countWithCriteria(customSkip));

        Criteria customApplied = Criteria.newCriteria()
                .gtIfAbsent("age", 18, v -> v != null && (Integer) v >= 18);
        assertEquals("18 >= 18 断言通过 → 生效", 2, memberDao.countWithCriteria(customApplied)); // 20, 30
    }

    @Test
    public void whereOptIfAbsent() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // opt + 非空值生效 → age > 15
        Criteria optApplied = Criteria.newCriteria().whereOptIfAbsent("age", ">", 15);
        assertEquals(2, memberDao.countWithCriteria(optApplied)); // 20, 30

        // opt + null 跳过
        Criteria optSkip = Criteria.newCriteria().whereOptIfAbsent("age", ">", null);
        assertEquals(3, memberDao.countWithCriteria(optSkip));
    }

    // ==================== 自定义 RowMapper ====================
    // 三个带 RowMapper 参数的重载方法：queryOne(id, mapper) / queryWithCriteria(criteria, mapper)
    // / pageQueryWithCriteria(page, criteria, mapper)，允许将行映射为任意类型（而非默认实体）。
    // 这里用自定义 RowMapper 把行映射为投影实体 MemberNameAgeDto，验证映射结果与泛型返回类型。

    @Test
    public void queryOneWithCustomRowMapperMapsColumns() {
        memberDao.save(newMember(1, "zhangsan", 18));

        MemberNameAgeDto mapped = memberDao.queryOne(1, memberNameAgeMapper());

        assertEquals("zhangsan", mapped.getName());
        assertEquals(Integer.valueOf(18), mapped.getAge());
    }

    @Test
    public void queryWithCriteriaWithCustomRowMapperMapsAllRows() {
        memberDao.save(newMember(1, "a", 10));
        memberDao.save(newMember(2, "b", 20));
        memberDao.save(newMember(3, "c", 30));

        // 过滤 + 排序后映射为投影实体列表，验证行级映射与 orderBy 同时生效
        List<MemberNameAgeDto> mapped = memberDao.queryWithCriteria(
                Criteria.newCriteria().gt("age", 15).orderBy(Sort.asc("age")),
                memberNameAgeMapper());

        assertEquals(2, mapped.size());
        assertEquals("b", mapped.get(0).getName());
        assertEquals(Integer.valueOf(20), mapped.get(0).getAge());
        assertEquals("c", mapped.get(1).getName());
        assertEquals(Integer.valueOf(30), mapped.get(1).getAge());
    }

    @Test
    public void pageQueryWithCriteriaWithCustomRowMapperReturnsPage() {
        // age: 11..15，其中 >12 的为 3 条（13,14,15）
        for (int i = 1; i <= 5; i++) {
            memberDao.save(newMember(i, "name" + i, 10 + i));
        }

        // 映射为投影实体分页结果，验证 LIMIT 分页与独立 COUNT 总数统计仍正确
        PageResult<MemberNameAgeDto> page = memberDao.pageQueryWithCriteria(
                new Page(1, 2),
                Criteria.newCriteria().gt("age", 12).orderBy(Sort.asc("age")),
                memberNameAgeMapper());

        assertEquals(3, page.getTotal().intValue());
        assertEquals(2, page.getList().size());
        assertEquals("name3", page.getList().get(0).getName());
        assertEquals(Integer.valueOf(13), page.getList().get(0).getAge());
        assertEquals("name4", page.getList().get(1).getName());
        assertEquals(Integer.valueOf(14), page.getList().get(1).getAge());
    }

    /** 把 tb_member 行映射为 name/age 投影实体 */
    private static RowMapper<MemberNameAgeDto> memberNameAgeMapper() {
        return (rs, rowNum) -> {
            MemberNameAgeDto dto = new MemberNameAgeDto();
            dto.setName(rs.getString("name"));
            dto.setAge(rs.getInt("age"));
            return dto;
        };
    }
}
