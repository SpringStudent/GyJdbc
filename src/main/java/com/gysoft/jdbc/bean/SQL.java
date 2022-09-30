package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.tools.EntityTools;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 周宁
 */
public class SQL extends AbstractCriteria<SQL> {
    /**
     * sql类型
     */
    private String sqlType;
    /**
     * 表名称
     */
    private String tbName;
    /**
     * 表的别名
     */
    private String aliasName;
    /**
     * 将sql作为表的别名
     */
    private String asTable;

    /**
     * 标识从from(String asTable,SQL c)
     * 方法传递asTable，此方法用于给子查询起别名
     */
    private boolean fromAsTable = false;

    /**
     * 删除语句中表的别名
     */
    private String deleteAliasName;
    /**
     * 连接
     */
    private List<Joins.BaseJoin> joins;
    /**
     * 子查询条件
     */
    private List<SQL> subSqls;
    /**
     * sql管道拼接
     */
    private SQLPiepline sqlPiepline = new SQLPiepline(this);
    /**
     * 表元数据
     */
    private TableMeta tableMeta;
    /**
     * 被查询的字段
     */
    private List<Object> selectFields;
    /**
     * sql插入
     */
    private Pair<String, List<Object[]>> pair;
    /**
     * 待更新的字段和相应的值
     */
    private List<Pair> kvs;
    /**
     * 连接类型
     */
    private String unionType;

    /**
     * 喝醉了的，代表人很糊涂删除表或者清楚数据
     */
    private Drunk drunk;

    public SQL() {
        selectFields = new ArrayList<>();
        kvs = new ArrayList<>();
        joins = new ArrayList<>();
        subSqls = new ArrayList<>();
        pair = new Pair<>();
        pair.setSecond(new ArrayList<>());
    }

    public SQL from(SQL... cc) {
        for (SQL c : cc) {
            c.getSqlPiepline().getSqlNexts().forEach(sqlNext -> {
                SQL s = sqlNext.getSql();
                if (sqlNext.getUnionType() != null) {
                    s.setUnionType(sqlNext.getUnionType());
                } else {
                    s.setUnionType(",");
                }
                subSqls.add(s);
            });
        }
        return this;
    }

    public SQL from(SQL c, String asTable) {
        this.asTable = asTable;
        this.fromAsTable = true;
        c.getSqlPiepline().getSqlNexts().forEach(sqlNext -> {
            SQL s = sqlNext.getSql();
            if (sqlNext.getUnionType() != null) {
                s.setUnionType(sqlNext.getUnionType());
            } else {
                s.setUnionType(",");
            }
            subSqls.add(s);
        });
        return this;
    }

    public SQL union() {
        SQL next = new SQL();
        sqlPiepline.add(next, "UNION");
        next.setSqlPiepline(sqlPiepline);
        return next;
    }

    public SQL unionAll() {
        SQL next = new SQL();
        sqlPiepline.add(next, "UNION ALL");
        next.setSqlPiepline(sqlPiepline);
        return next;
    }

    public List<Object> getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(List<Object> selectFields) {
        this.selectFields = selectFields;
    }

    public List<Pair> getKvs() {
        return kvs;
    }

    public void setKvs(List<Pair> kvs) {
        this.kvs = kvs;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setJoins(List<Joins.BaseJoin> joins) {
        this.joins = joins;
    }

    public SQLPiepline getSqlPiepline() {
        return sqlPiepline;
    }

    public void setSqlPiepline(SQLPiepline sqlPiepline) {
        this.sqlPiepline = sqlPiepline;
    }

    public List<SQL> getSubSqls() {
        return subSqls;
    }

    public void setSubSqls(List<SQL> subSqls) {
        this.subSqls = subSqls;
    }

    public String getDeleteAliasName() {
        return deleteAliasName;
    }

    public void setDeleteAliasName(String deleteAliasName) {
        this.deleteAliasName = deleteAliasName;
    }

    public String getAsTable() {
        return asTable;
    }

    public void setAsTable(String asTable) {
        this.asTable = asTable;
    }

    public boolean getFromAsTable() {
        return fromAsTable;
    }

    public void setFromAsTable(boolean fromAsTable) {
        this.fromAsTable = fromAsTable;
    }

    public SQL select(Object... fields) {
        selectFields.addAll(Arrays.asList(fields));
        this.sqlType = EntityDao.SQL_SELECT;
        return this;
    }

    public <T, R> SQL select(TypeFunction<T, R>... functions) {
        selectFields.addAll(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_SELECT;
        return this;
    }

    public SQL update(String table) {
        this.tbName = table;
        this.sqlType = EntityDao.SQL_UPDATE;
        return this;
    }

    public SQL update(Class clss) {
        this.tbName = EntityTools.getTableName(clss);
        this.sqlType = EntityDao.SQL_UPDATE;
        return this;
    }

    public SQL delete(String deleteAliasName) {
        this.deleteAliasName = deleteAliasName;
        this.sqlType = EntityDao.SQL_DELETE;
        return this;
    }

    public SQL delete(String... deleteAliasNames) {
        this.deleteAliasName = Arrays.stream(deleteAliasNames).collect(Collectors.joining(","));
        this.sqlType = EntityDao.SQL_DELETE;
        return this;
    }

    public SQL delete() {
        this.sqlType = EntityDao.SQL_DELETE;
        return this;
    }

    public SQL insertInto(String table, String... fields) {
        pair.setFirst(new String("INSERT INTO " + table + " ("
                + Arrays.stream(fields).collect(Collectors.joining(","))
                + ") "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public SQL replaceInto(String table, String... fields) {
        pair.setFirst(new String("REPLACE INTO " + table + " ("
                + Arrays.stream(fields).collect(Collectors.joining(","))
                + ") "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public <T, R> SQL insertInto(String table, TypeFunction<T, R>... functions) {
        pair.setFirst(new String("INSERT INTO " + table + " ("
                + Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.joining(","))
                + ") "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public <T, R> SQL replaceInto(String table, TypeFunction<T, R>... functions) {
        pair.setFirst(new String("REPLACE INTO " + table + " ("
                + Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.joining(","))
                + ") "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public SQL insertInto(String table) {
        pair.setFirst(new String("INSERT INTO " + table + " "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public SQL replaceInto(String table) {
        pair.setFirst(new String("REPLACE INTO " + table + " "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public <T, R> SQL insertInto(Class clss, String... fields) {
        return insertInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL replaceInto(Class clss, String... fields) {
        return replaceInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL insertInto(Class clss, TypeFunction<T, R>... functions) {
        return insertInto(EntityTools.getTableName(clss), functions);
    }

    public <T, R> SQL replaceInto(Class clss, TypeFunction<T, R>... functions) {
        return replaceInto(EntityTools.getTableName(clss), functions);
    }

    public <T, R> SQL insertInto(Class clss) {
        return insertInto(EntityTools.getTableName(clss));
    }

    public <T, R> SQL replaceInto(Class clss) {
        return replaceInto(EntityTools.getTableName(clss));
    }

    public SQL insertIgnoreInto(String table, String... fields) {
        pair.setFirst(new String("INSERT IGNORE INTO " + table + " ("
                + Arrays.stream(fields).collect(Collectors.joining(","))
                + ") "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public <T, R> SQL insertIgnoreInto(String table, TypeFunction<T, R>... functions) {
        pair.setFirst(new String("INSERT IGNORE INTO " + table + " ("
                + Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.joining(","))
                + ") "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public SQL insertIgnoreInto(String table) {
        pair.setFirst(new String("INSERT IGNORE INTO " + table + " "));
        this.sqlType = EntityDao.SQL_INSERT;
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public <T, R> SQL insertIgnoreInto(Class clss, String... fields) {
        return insertIgnoreInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL insertIgnoreInto(Class clss, TypeFunction<T, R>... functions) {
        return insertIgnoreInto(EntityTools.getTableName(clss), functions);
    }

    public <T, R> SQL insertIgnoreInto(Class clss) {
        return insertIgnoreInto(EntityTools.getTableName(clss));
    }

    public SQL truncate() {
        sqlType = EntityDao.SQL_TRUNCATE;
        drunk = new Drunk();
        return this;
    }

    public SQL table(String... tables) {
        drunk.setTables(Arrays.stream(tables).collect(Collectors.toSet()));
        return this;
    }

    public SQL table(Class... clss) {
        drunk.setTables(Arrays.stream(clss).map(EntityTools::getTableName).collect(Collectors.toSet()));
        return this;
    }

    public SQL drop() {
        sqlType = EntityDao.SQL_DROP;
        drunk = new Drunk();
        return this;
    }

    public SQL ifExists() {
        drunk.setIfExists(true);
        return this;
    }

    public SQL set(String key, Object value) {
        kvs.add(new Pair(key, value));
        return this;
    }

    public <T, R> SQL set(TypeFunction<T, R> function, Object value) {
        kvs.add(new Pair(TypeFunction.getLambdaColumnName(function), value));
        return this;
    }

    public SQL as(String aliasName) {
        this.aliasName = aliasName;
        return this;
    }

    public SQL asTable(String aliasName) {
        this.asTable = aliasName;
        return this;
    }

    public SQL from(Class clss) {
        tbName = EntityTools.getTableName(clss);
        return this;
    }

    public SQL from(String tbName) {
        this.tbName = tbName;
        return this;
    }

    public SQL leftJoin(Joins.On on) {
        on.setJoinType(JoinType.LeftJoin);
        return join(on);
    }

    public SQL rightJoin(Joins.On on) {
        on.setJoinType(JoinType.RightJoin);
        return join(on);
    }

    public SQL innerJoin(Joins.On on) {
        on.setJoinType(JoinType.InnerJoin);
        return join(on);
    }


    public SQL natureJoin(Joins.BaseJoin as) {
        as.setJoinType(JoinType.NatureJoin);
        joins.add(as);
        return this;
    }

    private SQL join(Joins.On join) {
        joins.add(join);
        return this;
    }

    public String getTbName() {
        return tbName;
    }

    public void setTbName(String tbName) {
        this.tbName = tbName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public List<Joins.BaseJoin> getJoins() {
        return joins;
    }

    public SQL values(Object... values) {
        pair.getSecond().add(values);
        return this;
    }

    public SQL values(List<Object[]> values) {
        pair.getSecond().addAll(values);
        return this;
    }

    public Pair<String, List<Object[]>> getPair() {
        return pair;
    }

    public Table create() {
        return new Table(this);
    }

    public void setTableMeta(TableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }

    public TableMeta getTableMeta() {
        return tableMeta;
    }


    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getUnionType() {
        return unionType;
    }

    public void setUnionType(String unionType) {
        this.unionType = unionType;
    }

    public Drunk getDrunk() {
        return drunk;
    }

    public void setDrunk(Drunk drunk) {
        this.drunk = drunk;
    }

    public SQL onDuplicateKeyUpdate(String key, Object value) {
        kvs.add(new Pair(key, value));
        return this;
    }

    public <T, R> SQL onDuplicateKeyUpdate(TypeFunction<T, R> function, Object value) {
        kvs.add(new Pair(TypeFunction.getLambdaColumnName(function), value));
        return this;
    }

}
