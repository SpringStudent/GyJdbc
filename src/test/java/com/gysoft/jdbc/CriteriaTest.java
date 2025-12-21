package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;
import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import java.sql.JDBCType;
import java.util.*;

import static com.gysoft.jdbc.bean.FuncBuilder.*;
import static com.gysoft.jdbc.dao.EntityDao.SQL_INSERT;

/**
 * Unit test for simple App.
 */
public class CriteriaTest {
    private String baseSql = "SELECT * FROM tb_test";

    @Test
    public void testCriteria() {
        Criteria criteria = new Criteria();
        Pair<String, Object[]> pair = new Pair<>();
        criteria.isNull("filedss");
        criteria.orBetweenAnd("btke", 1, 2);
        criteria.in("sets", new HashSet(Arrays.asList("1234567890", "111111")));
        criteria.orLike("likeKey", "thisi s p lsa");
        criteria.orBetweenAnd("btad", 19920928, 20190321);
        criteria.orLike("okd", "s123").orLike(Token::getTk, "sd");
        criteria.orLikeIfAbsent("dsa", "11").orLikeIfAbsent(Token::getTk, "111");
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
        criteria.likeL("likeL","毛弹头");
        criteria.likeLIfAbsent("likeL","毛弹头");
        criteria.likeLIfAbsent(Token::getTk,"毛弹头");
        criteria.likeLIfAbsent("likeLL","xxx");
        criteria.likeR("likeR","毛弹头");
        criteria.likeRIfAbsent("likeR","毛弹头");
        criteria.likeRIfAbsent(Token::getTk,"毛弹头");
        criteria.likeRIfAbsent("likeR","");

        criteria.having(count("asd"), "in", Arrays.asList(1, 2, 3)).limit(1);
        pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        criteria = new Criteria().and(Where.where("f2").in(Arrays.asList(4, 5, 67)).or("f11").betweenAnd("dd", 33)).and("key", 23)
                .or(Where.where("xmld").equal("eqeual").and("andd").gt(1230).or("xsdads").like("mmmdsa"))
                .andWhere(Opt.OR, WhereParam.where("k1").in(Arrays.asList(1, 3, 4)), WhereParam.where("k2").equal("k2v"), WhereParam.where("k3").isNotNull())
                .and(Opt.AND, WhereParam.where("isnol").isNotNull(), WhereParam.where("xds").exists(new SQL().select("*").from("haobads").where("dddx", 12)));
        pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(baseSql));
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testJoinSql() {
        SQL criteria = new SQL().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .andWhere(Opt.OR, WhereParam.where("k1").in(Arrays.asList(1, 3, 4)), WhereParam.where("k2").equal("k2v"), WhereParam.where("k3").isNotNull())
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
        //real sql
        SQL sql = new SQL().select("au.user_id", "au.pname").from(new SQL().select("*").from("sys_user").isNotNull("parent").and("del_flag", 0).and("status", 1), new SQL().select("@parent := '1545572506026774529'").from("dual")).gt("FIND_IN_SET(parent,@parent)", 0).and("@parent", ":=", new FieldReference(concat("@parent", "','", "user_id")))
                .union().select("au.user_id", "au.pname").from(new SQL().select("*").from("sys_user").isNotNull("parent").and("del_flag", 0).and("status", 1).notEqual("'level'", 1))
                .gt("FIND_IN_SET(parent,@parent)", 0).and("@parent", ":=", new FieldReference(concat("@parent", "','", "user_id"))).union().select("user_id", "pname").from("sys_user")
                .where("user_id", "1545572506026774529").and("status", 1).notEqual("'level'", 1).and("del_flag", 0);
        p = SqlMakeTools.useSql(sql);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("a").from("a_tb").union().select("b").from("b_tb"));
        p = SqlMakeTools.useSql(sql);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));
        sql = new SQL().select("t1.name", "t2.username").from(Book.class).as("t1")
                .natureJoin(new Joins().with(Book.class).as("t2"))
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .natureJoin(new Joins().with(Book.class).as("t3"))
                .leftJoin(new Joins().with(Book.class).as("t4").on("t4.id", "t2.id"))
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .union().select("un.ke", "un.ke2").from(Book.class).where("un.ke", 1);
        p = SqlMakeTools.useSql(sql);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("a").from("a_tb").asTable("aquery"), new SQL().select("b").from("b_tb").asTable("bquery"));
        p = SqlMakeTools.useSql(sql);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));
        sql = new SQL().select("a.*").from("a_tb").asTable("a").where("1", 1).unionAll().select("b.*").from("b_tb").asTable("b").and("2", 2);
        p = SqlMakeTools.useSql(sql);
        System.out.println(p.getFirst());
        System.out.println(Arrays.toString(p.getSecond()));

    }

    @Test
    public void testUnionSql() {
        SQL s1 = new SQL().select("u1.*").from(Test.class).where("u1.id", 123).union().select("u2.*").from(Test.class).and("xd22", 1).or(Opt.OR, WhereParam.where("zx").isNull(), WhereParam.where("had").equal(2335))
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
                .engine(TableEnum.Engine.InnoDB).utf8mb4().comment("用户").commit()
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
        sql = new SQL().delete("t1").from("tb_table").as("t1")
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
                .and("orders.orderid", new FieldReference("items.orderid+" + 2))
                .let("orders.date", "2000/03/01");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().delete("a,b").from("flow_instance").as("a").innerJoin(new Joins().with("flow_action").as("b").on("a.id", "b.flowInstanceId")).where("b.bizId", "dddddd");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));

    }

    @Test
    public void testSqlWith() {
        //SELECT m.status mkStatus FROM inspection m LEFT JOIN
        // (SELECT m.inspId,max(m.modelObjId)modelObjId FROM inspection_model m WHERE m.projectId = ? GROUP BY m.projectId)
        // g1  ON g1.inspId = m.id  WHERE m.status IN(?,?,?)
        SQL sql = new SQL().select("m.status mkStatus").from("inspection", "m")
                .leftJoin(new SQL().select("m.inspId,max(m.modelObjId)modelObjId").from("inspection_model").as("m")
                        .and("m.projectId", "pid").groupBy("m.projectId"), "g1").on("g1.inspId", "m.id")
                .inIfAbsent("m.status", Arrays.asList(1, 2, 3));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testUnionBug() {
        SQL sql = new SQL().select("*").from(new SQL().select("*").from("test").as("t1")
                .unionAll().select("*").from(new SQL().select("*").from("test2")
                        .unionAll().select("*").from("test3").where("god", "pls")).as("t2")).as("t3")
                .union().select("*").from(new SQL().select("*").from("test3")
                        .unionAll().select("*").from(new SQL().select("t5.*").from("test").as("t5")
                                .leftJoin(new Joins().with("test").as("t6").on("t5.id", "t6.id")
                                        .andIfAbsent("t5.id", ">", 1))).as("t7")).as("t4");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("test").as("t5")
                .leftJoin("test", "t6").on("t5.id", "t6.id")
                .on("t5.id", ">", 1).on("t5.pid", "=", new FieldReference("field"));
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
                .leftJoin(new Joins().with("table_b").as("b").on("a.id", "b.pid")
                        .and("a.key", new FieldReference("b.pos")).on("a.nn", "b.nnns").andIfAbsent("a.null", null))
                .in("a.ksd", Arrays.asList(1, 4, 65, 3));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from(
                new SQL().select("a.field1").from(
                        new SQL().select("a.*").from(
                                new SQL().select("*").from(
                                        new SQL().select("a").from(
                                                new SQL().select("*").from("tablea")
                                        ).where("key", "k").asTable("aquery"), new SQL().select("b").from("b_tb").asTable("bquery")
                                )).where("1", 1).asTable("ddd").unionAll().select("b.*").from("b_tb").asTable("b").and("2", 2)
                ).where("a.id", "1"), "astb");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("a").from("a_tb").asTable("aquery"), new SQL().select("b").from("b_tb").asTable("bquery"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("a.*").from("a_tb").asTable("a").where("1", 1).unionAll().select("b.*").from("b_tb").asTable("b").and("2", 2);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("a.*").from("a_tb").as("a").where("1", 1).unionAll().select("b.*").from("b_tb").as("b").and("2", 2), "mmm").where("id", "id1");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("tablea").asTable("bb");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("ab.*").from(new SQL().select("a.*").from("a_tb").as("a").where("id", 1).unionAll().select("b.*").from("b_tb").as("b").where("id", 2), "ab");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("e.name", new SQL().select("d.name").from("dept").as("d").where("e.deptno", new FieldReference("d.deptno")).as("dname").asTable("dname"), "e.testname").from("emp").as("e");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("employee_number", "name").from("employees").as("emp").gt("salary", new SQL().select(avg("salary")).from("employees").where("department", new FieldReference("emp.department")).groupBy("department").having("sum(gdp)", ">", 10000));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select(count("average")).from(new SQL().select("sid", avgAs("score").as("average")).from("sc").groupBy("sid")).gt("average", "t2.average").asTable("rank");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("t2.sid", "sutdent.sname", "t2.average", new SQL().select(count("average")).from(new SQL().select("sid", avgAs("score").as("average")).from("sc").groupBy("sid").asTable("t1")).gt("average", "t2.average").asTable("rank"))
                .from("student").where("t2.sid", "student.sid").orderBy(new Sort("average", "desc"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void testAndSql() {
        List<WhereParam> whereParams = new ArrayList<>();
        whereParams.add(WhereParam.where("f11").in(Arrays.asList(9, 10)));
        whereParams.add(WhereParam.where("f12").notEqual("d"));
        SQL sql = new SQL().select("*").from("table1")
                .where("f1", 1).and(Where.where("f2").like("a").or("f3").gte(1).and("f4").in(Arrays.asList(2, 3, 4)))
                .and(Opt.AND, WhereParam.where("f9").like("c"), WhereParam.where("f10").lt(8))
                .andWhere(Opt.AND, WhereParam.where("f5").betweenAnd(5, 6))
                .or(Opt.OR, WhereParam.where("f7").notEqual(7), WhereParam.where("f8").isNull())
                .orWhere(Opt.OR, whereParams)
                .andWhere(Where.where("m").equal("22").or("x").like("xx"))
                .orWhere(Where.where("c").equal("111").and("key").isNotNull());
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        List<WhereParam> params = new ArrayList<>();
        params.add(WhereParam.where("f1").like("v1"));
        params.add(WhereParam.where("f2").in(Arrays.asList(1, 2, 3)));
        sql = new SQL().select("*").from("table2").and(Opt.AND, params);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        Long start = null;
        Long end = 1646461369000L;
        sql = new SQL().select("id", "type", "name", unixTimeStamp("comTime") + "* 1000").from("member")
                .gteIfAbsent("createTime", Optional.ofNullable(start).map(Date::new).orElse(null))
                .letIfAbsent("createTime", Optional.ofNullable(end).map(Date::new).orElse(null));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
    }

    @Test
    public void finalTest() {
        //delete a,b from flow_instance as a inner join flow_action b on a.id=b.flowInstanceId where b.bizId = ?
        SQL sql = new SQL().delete("a", "b").from("flow_instance").as("a").innerJoin("flow_action","b").on("a.id", "b.flowInstanceId").where("b.bizId", "id123456");
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().delete().from("t_order").where(new String[]{"user_id", "product_id"}, "in", new SQL().select("t.user_id,t.product_id").from(new SQL().select("user_id,product_id").from("t_order").groupBy("user_id", "product_id").having("count(1)", ">", 1).asTable("t")))
                .notIn("id", new SQL().select("t.id").from(new SQL().select(minAs("id").as("id")).from("t_order")).groupBy("user_id", "product_id").having("count(1)", ">", 1).asTable("t"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select(count("average")).from(
                new SQL().select("sid", avgAs("score").as("average")
                ).from("sc").groupBy("sid")).gt("average", "t2.average").asTable("rank");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("a", "b", new SQL().select("c").from("c1").where("c1.id", 1).asTable("c2")).from("table");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from("tablea", "a").leftJoin("tableb", "b").on("a.id", "b.id").on("a.tb", "=", "1");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("m.status mkStatus").from("inspection", "m")
                .rightJoin(new SQL().select("m.inspId,max(m.modelObjId)modelObjId").from("inspection_model").as("m").and("m.projectId", "pid").groupBy("m.projectId"), "g1").on("g1.inspId", "m.id")
                .leftJoin("b", "b").on("a.id", "b.id").on("a.tb", "=", "dddd")
                .inIfAbsent("m.status", Arrays.asList(1, 2, 3));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("*").from("test").as("t1")
                .unionAll().select("*").from(new SQL().select("*").from("test2")
                        .unionAll().select("*").from("test3").where("god", "pls")).as("t2")).as("t3")
                .union().select("*").from(new SQL().select("*").from("test3")
                        .unionAll().select("*").from(new SQL().select("t5.*").from("test").as("t5")
                                .leftJoin("test", "t6").on("t5.id", "t6.id").on("t5.id", ">", 1)).as("t7")).as("t4");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(Arrays.toString(pair.getSecond()));
        sql = new SQL().select("t1.name", "t2.username").from(Book.class, "t1")
                .natureJoin(Book.class, "t2")
                .and("sd", "in", Arrays.asList("sd1", "xg1")).gt("sdf", 12)
                .andWhere(Opt.OR, WhereParam.where("k1").in(Arrays.asList(1, 3, 4)), WhereParam.where("k2").equal("k2v"), WhereParam.where("k3").isNotNull())
                .natureJoin(Book.class, "t3")
                .leftJoin(Book.class, "t4").on("t4.id", "t2.id").on("t4.name", "t2.name").on("t4.andIfAbsent", "=", "123")
                .andCriteria(new Criteria().where("k1", "v1").or("k2", "v2")).or("k3", "k5")
                .limit(12222, 100)
                .union().select("un.ke", "un.ke2").from(Book.class).where("un.ke", 1);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));

        sql = new SQL().select("t0.id,t0.lon,t0.lat,t3.investmentStatus").from("project", "t0")
                .leftJoin(
                        new SQL().select("t1.projectId,t1.investmentStatus").from("project_progress", "t1").innerJoin(
                                new SQL().select("projectId,max(createTime) createTime").from("project_progress").groupBy("projectId")
                                , "t2").on("t1.createTime", "t2.createTime").and("t1.projectId", "t2.projectId")
                        , "t3").on("t0.id", "t3.projectId");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("t0.id,t0.lon,t0.lat,t3.investmentStatus").from("project").as("t0")
                .leftJoin(new Joins().with(
                        new SQL().select("t1.projectId,t1.investmentStatus").from("project_progress").as("t1")
                                .innerJoin(new Joins().with(new SQL().select("projectId,max(createTime) createTime").from("project_progress").groupBy("projectId"))
                                        .as("t2").on("t1.createTime", "t2.createTime").and("t1.projectId", new FieldReference("t2.projectId")).and("tt",111))
                )
                        .as("t3").on("t0.id", "t3.projectId"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));

        sql = new SQL().select("t0.investmentStatus status,count( t0.projectId ) projectCount").from(
                new SQL().select("t1.projectId,t1.investmentStatus").from("project_progress").as("t1")
                        .innerJoin(new Joins().with(new SQL().select("projectId,max(createTime) createTime").from("project_progress").groupBy("projectId"))
                                .as("t2").on("t1.createTime", "t2.createTime").on("t1.projectId", "t2.projectId"))
                        .innerJoin(new Joins().with("project").as("t3").on("t1.projectId", "t3.id").and("t3.deleteFlag", 0))
        ).as("t0").groupBy("t0.investmentStatus");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }

    @Test
    public void testMakeTools() {
        System.out.println(SqlMakeTools.makeSql(Role.class, "role", SQL_INSERT));
        SQL sql = new SQL().update("a").set("(a1,a2,a3)", new SQL().select("B1,B2,B3").from("B").where("B1", 2));
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().update("test001", "a")
                .innerJoin("test002", "b").on("a.id", "b.id")
                .set("a.name", new FieldReference("b.name")).set("a.age", new FieldReference("b.age"))
                .gt("b.age", 30);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("t1.progressPhoto").from("project_progress").as("t1")
                .innerJoin(new Joins().with(new SQL().select("projectId,max(createTime) createTime").from("project_progress").groupBy("projectId"))
                        .as("t2").on("t1.createTime", "t2.createTime").on("t1.projectId", "t2.projectId"))
                .where("t1.projectId", "1");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from("a").union().select("*").from("b");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from("b").asTable("t");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select(Role::getName).from(Role.class);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select(Role::getName).select(Token::getId).from(Role.class).innerJoin(Token.class).on("id", "b").on("a.ud", "=", "dd")
                .leftJoin("Tobsd").on("dd", "dxx").on("dds", "=", new FieldReference("xxs"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        String pid = "7";
        sql = new SQL().select("t1.id,t1.name").from(new SQL().select("*").from("office_folder").asTable("t1"), new SQL().select("@parent:=" + pid).from("dual").asTable("t2"))
                .gt("FIND_IN_SET(parentId, @parent)", new FieldReference("0")).and("@parent:=", "", new FieldReference("concat(@parent, ',', id )"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().delete().from("a,b");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().delete("t1,t2").from("t1").natureJoin(new Joins().with("t2")).natureJoin(new Joins().with("t3"))
                .where("t1.id", new FieldReference("t2.id")).and("t2.id", new FieldReference("t3.id"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("project.id", "project.projectName", "t2.id", "t2.actualTime")
                .from("project")
                .natureJoin(new Joins().with(new SQL().select("t1.id", "max(t2.actualTime) actualTime").from("project").as("t1").leftJoin("inspect", "t2").on("t1.id", "t2.projectId").groupBy("t1.id").asTable("t2")))
                .where("project.id", new FieldReference("t2.id")).and("project.deleteFlag", 0);
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("d").from("t_d").union().select("c").from("t_c"), "cd");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from(new SQL().select("1").from("a").union().select("2").from(new SQL().select("3").from("b").leftJoin(new SQL().select("4").from("c"), "c").on("b.id", "c.id"), "t1").asTable("mock"), "t2").where("a.id", "1000");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from("a").leftJoin(new Joins().with("b").on("a.id", "b.id").and("a.name", "123"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from("a").rightJoin("b").on("a.id", "b.id").on("a.name", "=", "123").asTable("xxxxc");
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));

        sql = new SQL().select("a.dangerStatus dangerStatus,a.level level,a.dangerDesc dangerDesc,a.dangerLocation dangerLocation,a.engineering engineering,a.parts parts,a.risk risk,1000*unix_timestamp(a.deadline) deadline")
                .from("inspect_danger").as("a")
                .innerJoin(new Joins().with("inspect").as("b").on("a.inspectId", "b.id").and("b.inspectStatus", 1))
                .innerJoin(new Joins().with(
                        new SQL().select("c.id id,max(d.actionTime) actionTime").from("inspect_danger").as("c").leftJoin(new Joins().with("inspect_danger_action").as("d").on("c.id", "d.dangerId").and("d.actionComment", "SUBMIT_RECT")).where("c.dangerStatus", 1).groupBy("c.id")).as("e").on("e.id", "a.id")
                )
                .where("a.projectId", 1);
        pair = SqlMakeTools.useSql(sql);

        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("t1.id AS id,t1.projectName as projectName,t1.lon as lon,t1.lat as lat,t2.`name` AS departmentName,MAX(t3.actualTime) AS lastInspectTime,100 - SUM(t4.score) AS score,count(t4.id) AS dangerCount,t5.investmentStatus AS investmentStatus")
                .from("project").as("t1")
                .innerJoin(new Joins().with("department").as("t2").on("t1.departmentId", "t2.id").andIfAbsent("t2.id", "deptId"))
                .leftJoin(new Joins().with("inspect").as("t3").on("t1.id", "t3.projectId"))
                .leftJoin(new Joins().with("inspect_danger").as("t4").on("t3.id", "t4.inspectId").and("t4.dangerStatus", "<>", 2).and("t3.inspectStatus", 1))
                .leftJoin(new Joins().with(
                        new SQL().select("t6.projectId,t6.investmentStatus").from("project_progress").as("t6")
                                .innerJoin(new Joins().with(
                                        new SQL().select("projectId,MAX(createTime) AS lastProgressTime").from("project_progress").groupBy("projectId")
                                ).as("t7").on("t6.createTime", "t7.lastProgressTime"))
                ).as("t5").on("t1.id", "t5.projectId").andIfAbsent("t5.investmentStatus", 1))
                .where("t1.deleteFlag", 0).likeIfAbsent("t1.projectName", "projName")
                .groupBy("t1.id", "t1.projectName", "t1.lon", "t1.lat", "t2.`name`", "t5.investmentStatus")
                .orderBy(new Sort("score", "asc"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("t1.id AS id,t1.projectName as projectName,t1.lon as lon,t1.lat as lat,t2.`name` AS departmentName,MAX(t3.actualTime) AS lastInspectTime,100 - SUM(t4.score) AS score,count(t4.id) AS dangerCount,t5.investmentStatus AS investmentStatus")
                .from("project").as("t1")
                .innerJoin("department", "t2").on("t1.departmentId", "t2.id").on("t2.id", "=", "deptId")
                .leftJoin("inspect", "t3").on("t1.id", "t3.projectId")
                .leftJoin("inspect_danger", "t4").on("t3.id", "t4.inspectId").on("t4.dangerStatus", "<>", 2).on("t3.inspectStatus","=", 1)
                .leftJoin(new SQL().select("t6.projectId,t6.investmentStatus").from("project_progress").as("t6")
                                .innerJoin(new SQL().select("projectId,MAX(createTime) AS lastProgressTime").from("project_progress").groupBy("projectId")
                                        , "t7").on("t6.createTime", "t7.lastProgressTime")
                        , "t5").on("t1.id", "t5.projectId").on("t5.investmentStatus","=", 1)
                .where("t1.deleteFlag", 0).likeIfAbsent("t1.projectName", "projName")
                .groupBy("t1.id", "t1.projectName", "t1.lon", "t1.lat", "t2.`name`", "t5.investmentStatus")
                .orderBy(new Sort("score", "asc"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));

        sql = new SQL().select("*").from("table_1123").and(Where.where("a").equal(1).and("b").equal(2).or("c").equal(3).and("d").equal(4));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
        sql = new SQL().select("*").from("table111").innerJoin("table222","b",c->c.on("table111.id","b.id").and("b.name","=","testname"))
                .leftJoin("table333","c",c->c.on(Role::getName,Token::getTk).on("table111.id","c.id").and("c.type",">",2))
                .rightJoin("table444","d",c->c.on("table111.id","d.id").and("d.status","=","active"))
                .where("table111.flag",1).andCriteria(c->c.where("table111.type","A").or("table111.type","B"));
        pair = SqlMakeTools.useSql(sql);
        System.out.println(pair.getFirst());
        System.out.println(ArrayUtils.toString(pair.getSecond()));
    }

}