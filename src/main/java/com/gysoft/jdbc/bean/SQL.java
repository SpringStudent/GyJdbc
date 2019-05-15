package com.gysoft.jdbc.bean;


import com.gysoft.jdbc.tools.EntityTools;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 周宁
 */
public class SQL extends AbstractCriteria<SQL> {

    /**
     * 被查询的字段
     */
    private Set<String> selectFields;

    /**
     * 更新
     */
    private List<Pair> kvs;

    /**
     * 表名称
     */
    private String tbName;
    /**
     * 别名
     */
    private String aliasName;
    /**
     * 连接
     */
    private List<Joins.BaseJoin> joins;
    /**
     * 子查询条件
     */
    private List<SQL> subSqls;

    private SQLPiepline sqlPiepline = new SQLPiepline(this);

    /**
     * sql插入
     */
    private Pair<StringBuilder, List<Object[]>> pair;

    public SQL() {
        selectFields = new LinkedHashSet<>();
        kvs = new ArrayList<>();
        joins = new ArrayList<>();
        subSqls = new ArrayList<>();
        pair = new Pair<>();
    }

    public SQL from(SQL... cc) {
        subSqls.addAll(Arrays.asList(cc));
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

    public Set<String> getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(Set<String> selectFields) {
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

    public SQL select(String... fields) {
        selectFields.addAll(Arrays.asList(fields));
        return this;
    }

    public <T, R> SQL select(TypeFunction<T, R>... functions) {
        selectFields.addAll(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()));
        return this;
    }

    public SQL update(String key, Object value) {
        kvs.add(new Pair(key, value));
        return this;
    }

    public <T, R> SQL update(TypeFunction<T, R> function, Object value) {
        kvs.add(new Pair(TypeFunction.getLambdaColumnName(function), value));
        return this;
    }

    public SQL as(String aliasName) {
        this.aliasName = aliasName;
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

    public SQL insertInto(String tbName, String... fields) {
        pair.setFirst(new StringBuilder().append("INSERT INTO " + tbName + "("
                + Arrays.stream(fields).collect(Collectors.joining(","))
                + ") "));
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public <T, R> SQL insertInto(String tbName, TypeFunction<T, R>... functions) {
        pair.setFirst(new StringBuilder().append("INSERT INTO " + tbName + "("
                + Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.joining(","))
                + ") "));
        pair.setSecond(new ArrayList<>());
        return this;
    }

    public SQL insertInto(Class clss, String... fields) {
        return insertInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL insertInto(Class clss, TypeFunction<T, R>... functions) {
        return insertInto(EntityTools.getTableName(clss), functions);
    }

    public SQL values(Object... values) {
        pair.getSecond().add(values);
        return this;
    }

    public Pair<StringBuilder, List<Object[]>> getPair() {
        return pair;
    }

}
