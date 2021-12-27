package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;
import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import java.sql.Array;
import java.sql.JDBCType;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.gysoft.jdbc.bean.FuncBuilder.*;

/**
 * Unit test for simple App.
 */
public class CriteriaTest {
    private String baseSql = "SELECT * FROM tb_test";

    @Test
    public void testCriteria() {
        Criteria criteria = new Criteria();
        criteria.isNull("filedss");
        criteria.orBetweenAnd("btke", 1, 2);
        criteria.in("sets", new HashSet(Arrays.asList("1234567890", "111111")));
        criteria.orLike("likeKey", "thisi s p lsa");
        criteria.findInSet("niuniuairen","ying");
        criteria.orBetweenAnd("btad", 19920928, 20190321);
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
        criteria = new Criteria().and(Where.where("f1").equal(2).or("fisd").findInSet("findinset").and("f2").in(Arrays.asList(4, 5, 67)).or("f11").betweenAnd("dd", 33)).and("key", 23)
                .orWhere(Opt.OR,WhereParam.where("k1").in(Arrays.asList(1,3,4)),WhereParam.where("k2").equal("k2v"),WhereParam.where("k3").isNotNull())
                .orWhere(Opt.OR,WhereParam.where("l1").findInSet(1),WhereParam.where("l2").findInSet(2))
                .or(Opt.AND,WhereParam.where("lx").findInSet(213),WhereParam.where("kx").findInSet("213"));
        pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testJoinSql() {
        SQL criteria = new SQL().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .natureJoin(new Joins().with(Book.class).as("t3"))
                .leftJoin(new Joins().with(Book.class).as("t4").on("t4.id", "t2.id").on("t4.name", "t2.name").andIfAbsent("t4.andIfAbsent", "=", "123"))
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .limit(12222, 100)
                .union().select("un.ke", "un.ke2").from(Book.class).where("un.ke", 1);
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
                .union().select("un.ke", "un.ke2").from(Book.class).where("un.ke", 1);
        SQL criteria = new SQL().select("*").from(criteria4,
                new SQL().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name")),
                new SQL().select("t2.*").from(Book.class).as("t2").where("t2.kd", "ssd").groupBy("t2.uploader").having("t2.count1", ">", 123),
                new SQL().select("t3.*").from(new SQL().select("t33.*").from(Book.class).as("t33").andIfAbsent("t33.name", "name33").orderBy(new Sort("t33.id")).union().select("t33U.*").from(Book.class).where("t33U", 111111))
        ).as("res").rightJoin(new Joins().with(Book.class).as("t4").on("res.name", "t4.name")).where("res.name", "book1").orderBy(new Sort("res.name"));
        Pair<String, Object[]> p = SqlMakeTools.useSql(criteria);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));
    }

    @Test
    public void testUnionSql() {
        SQL s1 = new SQL().select("u1.*").from(Test.class).where("u1.id", 123).union().select("u2.*").from(Test.class)
                .unionAll().select("u3.*").from(Book.class).where("u3", 123).leftJoin(new Joins().with(Test.class)
                        .as("u31").on("u31.id", "u3.id").and("u31.nmm", "=", "nmmm")).limit(10000);
        SQL s2 = new SQL().select("t1.*").from(Book.class).as("t1").andCriteria(new Criteria().in("t1.id", Arrays.asList(1, 2, 3)).like("t1.name", "name1")).leftJoin(new Joins().with(Book.class).as("j1").on("j1.id", "t1.id").and("j1.name", "=", "j1name"));
        SQL s = new SQL().select("res.*").from(s1, s2).where("res.name", "book1").orderBy(new Sort("res.name")).limit(100);
        Pair<String, Object[]> sqlParamPair = SqlMakeTools.useSql(s);
        System.out.println(sqlParamPair.getFirst());
        System.out.println(ArrayUtils.toString(sqlParamPair.getSecond()));
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

    private interface BookDao extends EntityDao<Book, String> {

    }

    private class BookDaoImpl extends EntityDaoImpl<Book, String> implements BookDao {

    }

    @Test
    public void testFunc() {
        //支持mysql函数拼接
        //聚集函数
        SQL s = new SQL().select(count("*"), avg(Token::getSize), max(Token::getSize), min(Token::getSize), sum(Token::getSize)).from(Token.class);
        //字符串处理函数
        SQL s2 = new SQL().select(concat(Token::getTk, Token::getSize), length(Token::getTk), charLength(Token::getTk), upper(Token::getTk), lower(Token::getTk)).from(Token.class);
        //数值处理函数
        SQL s3 = new SQL().select(abs(Token::getSize), ceil(Token::getSize), floor(Token::getSize)).from(Token.class);
        //时间处理函数
        SQL s4 = new SQL().select(curdate(), curtime(), now(), month(curdate()), week(curdate()), minute(curtime()));

        SQL s5 = new SQL().select(formatAs("10000", "2").as("a")).from(Book.class);

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
        SQL sql = new SQL().create().table("halou").temporary()
                .column().name("id").integer().notNull().primary().autoIncrement().comment("主键").commit()
                .column().name("name").varchar(5).notNull().comment("名称").defaultVal("").commit()
                .column().name("age").tinyint().notNull().commit()
                .column().name("email").jdbcType(JDBCType.LONGVARCHAR).defaultNull().commit()
                .column().name("birthday").datetime().notNull().defaultCurrentTimestamp().commit()
                .index().unique().column("name", "age").name("ix_name_age").commit()
                .index().name("ix_name").column("name").commit()
                .engine(TableEngine.InnoDB).comment("用户").commit()
                .values(1, "zhou", 23)
                .values(2, "peng", 24)
                .values(3, "wei", 25);
//        String tbName = new BookDaoImpl().createWithSql(sql);
//        System.out.println(tbName);
    }

    @Test
    public void testOtherSql() {
        //UPDATE test SET id = ?, name = ? WHERE pid = ?
        SQL sql = new SQL().update("test").set("id", 1).set("name", "asd").where("pid", 15);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //UPDATE test t1
        // INNER JOIN
        // tb_test t2  ON t1.id = t2.id  AND t1.id = ? SET t1.id = ?, t1.id = ? WHERE t1.id IN(?,?)
        sql = new SQL().update("test").as("t1").innerJoin(new Joins().with("tb_test").as("t2")
                .on("t1.id", "t2.id").and("t1.id", "=", 123)).set("t1.id", "t2.pid")
                .set("t1.id", 123).in("t1.id", Arrays.asList("id1", "id2"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE FROM test WHERE id = ?
        sql = new SQL().delete().from("test").where("id", 1);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE t1 FROM tb_table t1
        // INNER JOIN
        //tmp_table t2  ON t2.moduletype  = t1.moduletype  AND t2.unitqdkey = t1.unitqdkey  WHERE t1.unid = ? AND t1.epid = ?
        sql = new SQL().delete("t1").from("tb_table")
                .innerJoin(new Joins().with("tmp_table").as("t2").on("t2.moduletype ", "t1.moduletype")
                        .on("t2.unitqdkey", "t1.unitqdkey")).where("t1.unid", "iiods").and("t1.epid", 9192);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE FROM test WHERE id = ?
        sql = new SQL().delete().from("test").where("id", 1);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //UPDATE test t1 INNER JOIN tb_test t2  ON t1.id = t2.id  AND t1.id = ?
        //INNER JOIN
        //test_tb t3  ON t1.id = t3.id  AND t1.id = ? SET t1.id = t2.pid, t1.id = t3.cid, t1.id = ? WHERE t1.id IN(?,?)
        sql = new SQL().update("test").as("t1")
                .innerJoin(new Joins().with("tb_test").as("t2").on("t1.id", "t2.id").and("t1.id", "=", "id1"))
                .innerJoin(new Joins().with("test_tb").as("t3").on("t1.id", "t3.id").and("t1.id", "=", "id1"))
                .set("t1.id", new FieldReference("t2.pid")).set("t1.id", new FieldReference("t3.cid")).set("t1.id", "id2").in("t1.id", Arrays.asList("id3", "id4"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE orders,items FROM orders,items
        //WHERE orders.userid = items.userid  AND orders.orderid = items.orderid AND orders.date <= ?
        sql = new SQL().delete("orders,items").from("orders,items")
                .where("orders.userid", new FieldReference("items.userid "))
                .and("orders.orderid", new FieldReference("items.orderid"))
                .let("orders.date", "2000/03/01");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        //DELETE FROM orders,items
        // WHERE orders.userid = items.userid  AND orders.orderid = items.orderid AND orders.date <= ?
        sql = new SQL().delete().from("orders,items")
                .where("orders.userid", new FieldReference("items.userid "))
                .and("orders.orderid", new FieldReference("items.orderid"))
                .let("orders.date", "2000/03/01");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testSqlWith() {
        //SELECT m.status mkStatus FROM inspection m LEFT JOIN
        // (SELECT m.inspId,max(m.modelObjId)modelObjId FROM inspection_model m WHERE m.projectId = ? GROUP BY m.projectId)
        // g1  ON g1.inspId = m.id  WHERE m.status IN(?,?,?)
        SQL sql = new SQL().select("m.status mkStatus").from("inspection").as("m")
                .leftJoin(new Joins().with(new SQL().select("m.inspId,max(m.modelObjId)modelObjId").from("inspection_model").as("m")
                        .and("m.projectId", "pid").groupBy("m.projectId")).as("g1").on("g1.inspId", "m.id"))
                .inIfAbsent("m.status", Arrays.asList(1, 2, 3));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testUnionBug() {
        SQL sql = new SQL().select("*").from(new SQL().select("*").from("test").as("t1")
                .unionAll().select("*").from(new SQL().select("*").from("test2")
                        .unionAll().select("*").from("test3")).as("t2")).as("t3")
                .union().select("*").from(new SQL().select("*").from("test3")
                        .unionAll().select("*").from(new SQL().select("t5.*").from("test").as("t5")
                                .leftJoin(new Joins().with("test").as("t6").on("t5.id", "t6.id")
                                        .andIfAbsent("t5.id", ">", 1))).as("t7")).as("t4");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("test").as("t5")
                .leftJoin(new Joins().with("test").as("t6").on("t5.id", "t6.id")
                        .andIfAbsent("t5.id", ">", 1).and("t5.pid", "=", new FieldReference("field")));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("test").as("t")
                .leftJoin(new Joins().with("test").as("t1").on("t1.kid", "t.sid").and("t1.mmm", "=", "asd"))
                .unionAll()
                .select("*").from("t3").where("t3.mid", new SQL().select("mid").from("t4").where("t4.cco", "aaa"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testInSql() {
        SQL sql = new SQL().select("*").from(Book.class).notIn(Book::getId, new SQL().select("id").from("author").where("f1", 123).in("f2", Arrays.asList("g", "l")))
                .andCriteria(new Criteria().and(Book::getName, "name1").or(Book::getNum, "asdsd"));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from(Book.class).where(Book::getId, "this is a id").groupBy(Book::getNum).having(new Criteria().gt(count("name"), 1).or("fix", "heihei"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));

        sql = new SQL().select("*").from("test").as("t").natureJoin(new Joins().with("test1").as("t1"))
                .where("t1.id", new FieldReference("t.id"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));

        sql = new SQL().select("id,name,value", new ValueReference(1), new ValueReference(new Date())).from("test").where(Book::getId, "this is a id");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testWhereSql() {
        SQL sql = new SQL().select("*").from("test").where("id", new SQL().select("pid").from("tb_test").gt("type", 2));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("number, name, id_number, major").from("student_info").exists(new SQL().select("*")
                .from("student_score").where("student_score.number", new FieldReference("student_info.number")));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("student_score").where("(number, subject)", new SQL()
                .select("number", new ValueReference("母猪的产后护理")).from("student_info").limit(1));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("student_score").where("subject", "母猪的产后护理").gt("score",
                new SQL().select(avg("score")).from("student_score").where("subject", "母猪的产后护理"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("student_score").where(new String[]{"number"}, new SQL()
                .select("number", new ValueReference("母猪的产后护理")).from("student_info").limit(1));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("student_score").where(new String[]{"number", "subject"}, new SQL()
                .select("number", new ValueReference("母猪的产后护理")).from("student_info").limit(1));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().update("hehei").as("a").innerJoin(new Joins().with("hahei").as("b")
                .on("a.epid", "b.epid")).set("a.id", new FieldReference("b.id"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("a.*").from("table_a").as("a")
                .leftJoin(new Joins().with("table_b").as("b").on("a.id","b.pid")
                .and("a.key",new FieldReference("b.pos")).on("a.nn","b.nnns").andIfAbsent("a.null",null))
                .in("a.ksd", Arrays.asList(1,4,65,3));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testAndSql() {
        List<WhereParam> whereParams = new ArrayList<>();
        whereParams.add(WhereParam.where("f11").in(Arrays.asList(9,10)));
        whereParams.add(WhereParam.where("f12").notEqual("d"));
        SQL sql = new SQL().select("*").from("table1")
                .where("f1", 1).and(Where.where("f2").like("a").or("f3").gte(1).and("f4").in(Arrays.asList(2, 3, 4)))
                .and(Opt.AND, WhereParam.where("f9").like("c"), WhereParam.where("f10").lt(8))
                .andWhere(Opt.AND, WhereParam.where("f5").betweenAnd(5, 6), WhereParam.where("f6").findInSet("b"))
                .or(Opt.OR, WhereParam.where("f7").notEqual(7), WhereParam.where("f8").isNull())
                .orWhere(Opt.OR,whereParams);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));

        List<WhereParam> params = new ArrayList<>();
        params.add(WhereParam.where("f1").like("v1"));
        params.add(WhereParam.where("f2").in(Arrays.asList(1,2,3)));
        sql = new SQL().select("*").from("table2").and(Opt.AND,params);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }


}