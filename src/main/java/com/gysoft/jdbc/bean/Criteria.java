package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * mysql查询条件封装
 *
 * @author 周宁
 */
public class Criteria implements AuxiliaryOperation{
    /**
     * 被查询的字段
     */
    private Set<String> selectFields;
    /**
     * 条件入参
     */
    private Set<WhereParam> whereParams;
    /**
     * 排序入参
     */
    private Set<Sort> sorts;

    /**
     * 代理更复杂的sql拼接
     */
    private List<CriteriaProxy> criteriaProxys;
    /**
     * 分组字段
     */
    private Set<String> groupFields;
    /**
     * 更新
     */
    private List<Pair> kvs;

    /**
     * having子句
     */
    private Pair<String,Object[]> having;

    public Set<String> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(Set<String> groupFields) {
        this.groupFields = groupFields;
    }

    public Set<WhereParam> getWhereParams() {
        return whereParams;
    }

    public void setWhereParams(Set<WhereParam> whereParams) {
        this.whereParams = whereParams;
    }

    public Set<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(Set<Sort> sorts) {
        this.sorts = sorts;
    }

    public List<CriteriaProxy> getCriteriaProxys() {
        return criteriaProxys;
    }

    public void setCriteriaProxys(List<CriteriaProxy> criteriaProxys) {
        this.criteriaProxys = criteriaProxys;
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

    public Pair<String, Object[]> getHaving() {
        return having;
    }

    public void setHaving(Pair<String, Object[]> having) {
        this.having = having;
    }

    public Criteria() {
        selectFields = new LinkedHashSet<>();
        whereParams = new LinkedHashSet<>();
        sorts = new LinkedHashSet<>();
        criteriaProxys = new ArrayList<>();
        groupFields = new LinkedHashSet<>();
        kvs = new ArrayList<>();
        joins = new ArrayList<>();
        criterias = new ArrayList<>();
    }

    public Criteria select(String... fields) {
        selectFields.addAll(Arrays.asList(fields));
        return this;
    }

    public <T, R> Criteria select(TypeFunction<T, R>... functions) {
        selectFields.addAll(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()));
        return this;
    }

    public Criteria where(String key, Object value) {
        return this.where(key, "=", value);
    }

    public <T, R> Criteria where(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "=", value);
    }

    public Criteria where(String key, String opt, Object value) {
        this.whereParams.add(WhereParam.builder().key(key).opt(opt).value(value).build());
        return this;
    }

    public <T, R> Criteria where(TypeFunction<T, R> function, String opt, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), opt, value);
    }
    @Override
    public Criteria like(String key, Object value) {
        return this.where(key, "LIKE", "%" + value + "%");
    }

    @Override
    public <T, R> Criteria like(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "LIKE", "%" + value + "%");
    }
    @Override
    public Criteria gt(String key, Object value) {
        return this.where(key, ">", value);
    }

    @Override
    public <T, R> Criteria gt(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), ">", value);
    }
    @Override
    public Criteria gte(String key, Object value) {
        return this.where(key, ">=", value);
    }

    @Override
    public <T, R> Criteria gte(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), ">=", value);
    }
    @Override
    public Criteria lt(String key, Object value) {
        return this.where(key, "<", value);
    }

    @Override
    public <T, R> Criteria lt(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "<", value);
    }
    @Override
    public Criteria let(String key, Object value) {
        return this.where(key, "<=", value);
    }

    @Override
    public <T, R> Criteria let(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "<=", value);
    }

    @Override
    public Criteria doNothing() {
        return this;
    }

    public Criteria notEqual(String key, Object value) {
        return this.where(key, "<>", value);
    }

    public <T, R> Criteria notEqual(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "<>", value);
    }

    public Criteria isNull(String key) {
        return this.where(key, "IS", "NULL");
    }

    public <T, R> Criteria isNull(TypeFunction<T, R> function) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IS", "NULL");
    }

    public Criteria isNotNull(String key) {
        return this.where(key, "IS", "NOT NULL");
    }

    public <T, R> Criteria isNotNull(TypeFunction<T, R> function) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IS", "NOT NULL");
    }
    @Override
    public Criteria and(String key, Object value) {
        return this.where(key, value);
    }

    @Override
    public <T, R> Criteria and(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), value);
    }

    public Criteria and(String key, String opt, Object value) {
        return this.where(key, opt, value);
    }

    public <T, R> Criteria and(TypeFunction<T, R> function, String opt, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), opt, value);
    }
    @Override
    public Criteria or(String key, Object value) {
        return this.or(key, "=", value);
    }

    @Override
    public <T, R> Criteria or(TypeFunction<T, R> function, Object value) {
        return this.or(TypeFunction.getLambdaColumnName(function), "=", value);
    }

    public Criteria or(String key, String opt, Object value) {
        if (CollectionUtils.isEmpty(this.whereParams)) {
            throw new RuntimeException("sql error,condition \"or\" must be following after \"where\"!");
        }
        return this.where(" OR " + key, opt, value);
    }

    public <T, R> Criteria or(TypeFunction<T, R> function, String opt, Object value) {
        return this.or(TypeFunction.getLambdaColumnName(function), opt, value);
    }

    public Criteria orBetweenAnd(String key, Object v1, Object v2){
        return this.or(key,"BETWEEN ? AND ?",new Pair<>(v1,v2));
    }

    public <T,R> Criteria orBetweenAnd(TypeFunction<T, R> function, Object v1, Object v2){
        return this.or(TypeFunction.getLambdaColumnName(function),"BETWEEN ? AND ?",new Pair<>(v1,v2));
    }

    @Override
    public Criteria in(String key, Collection<?> args) {
        return this.where(key, "IN", args);
    }

    @Override
    public <T, R> Criteria in(TypeFunction<T, R> function, Collection<?> args) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IN", args);
    }
    @Override
    public Criteria notIn(String key, Collection<?> args) {
        return this.where(key, "NOT IN", args);
    }

    @Override
    public <T, R> Criteria notIn(TypeFunction<T, R> function, Collection<?> args) {
        return this.where(TypeFunction.getLambdaColumnName(function), "NOT IN", args);
    }

    public Criteria betweenAnd(String key, Object v1, Object v2) {
        return this.where(key, "BETWEEN ? AND ?", new Pair<>(v1, v2));
    }

    public <T, R> Criteria betweenAnd(TypeFunction<T, R> function, Object v1, Object v2) {
        return this.where(TypeFunction.getLambdaColumnName(function), "BETWEEN ? AND ?", new Pair<>(v1, v2));
    }

    public Criteria andCriteria(Criteria criteria) {
        return criteria(criteria, "AND");
    }

    public Criteria orCriteria(Criteria criteria) {
        if (CollectionUtils.isEmpty(whereParams)) {
            throw new RuntimeException("sql error,condition \"orCriteria\" must be following after \"where\"!");
        }
        return criteria(criteria, "OR");
    }

    private Criteria criteria(Criteria criteria, String criteriaType) {
        if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
            throw new RuntimeException("unsupport doCriteria operate");
        }
        if (CollectionUtils.isEmpty(whereParams)) {
            whereParams.add(new WhereParam());
        }
        CriteriaProxy criteriaProxy = new CriteriaProxy();
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder());
        criteriaProxy.setWhereParamsIndex(whereParams.size() + 1);
        criteriaProxy.setSql(new StringBuilder(pair.getFirst().replace("WHERE", "").trim()));
        criteriaProxy.setParams(pair.getSecond());
        criteriaProxy.setCriteriaType(criteriaType);
        criteriaProxys.add(criteriaProxy);
        return this;
    }

    public Criteria groupBy(String... fields) {
        groupFields.addAll(Arrays.asList(fields));
        return this;
    }

    public <T, R> Criteria groupBy(TypeFunction<T, R>... functions) {
        groupFields.addAll(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()));
        return this;
    }

    public Criteria having(String funcField,String opt,Object value){
        having = SqlMakeTools.doCriteria(new Criteria().where(funcField, opt, value), new StringBuilder());
        having.setFirst(having.getFirst().replace("WHERE ", ""));
        return this;
    }

    public Criteria orderBy(Sort... sort) {
        sorts.addAll(Arrays.asList(sort));
        return this;
    }

    public Criteria update(String key, Object value) {
        kvs.add(new Pair(key, value));
        return this;
    }

    public <T, R> Criteria update(TypeFunction<T, R> function, Object value) {
        kvs.add(new Pair(TypeFunction.getLambdaColumnName(function), value));
        return this;
    }

    /**
     * 连接主表
     */
    private String pTable;
    /**
     * 别名
     */
    private String aliasName;

    /**
     * 开启连接查询的标识位
     */
    private boolean joinFlag;

    private List<Joins.BaseJoin> joins;

    public boolean isJoinFlag() {
        return joinFlag;
    }

    public Criteria as(String aliasName){
        this.aliasName = aliasName;
        return this;
    }

    public Criteria from(Class clss){
        pTable =EntityTools.getTableName(clss);
        return this;
    }

    public Criteria leftJoin(Joins.On on){
        on.setJoinType(JoinType.LeftJoin);
        return join(on);
    }

    public Criteria rightJoin(Joins.On on){
        on.setJoinType(JoinType.RightJoin);
        return join(on);
    }

    public Criteria innerJoin(Joins.On on){
        on.setJoinType(JoinType.InnerJoin);
        return join(on);
    }

    public Criteria natureJoin(Joins.BaseJoin as){
        joinFlag = true;
        as.setJoinType(JoinType.NatureJoin);
        joins.add(as);
        return this;
    }

    private Criteria join(Joins.On join){
        joinFlag = true;
        joins.add(join);
        return this;
    }

    public String getpTable() {
        return pTable;
    }

    public String getAliasName() {
        return aliasName;
    }

    public List<Joins.BaseJoin> getJoins() {
        return joins;
    }

    public void setpTable(String pTable) {
        this.pTable = pTable;
    }

    /**
     * 子查询条件
     */
    private List<Criteria> criterias;

    public Criteria from(Criteria... cc){
        criterias.addAll(Arrays.asList(cc));
        return this;
    }
    public List<Criteria> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<Criteria> criterias) {
        this.criterias = criterias;
    }

}