package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * mysql查询条件封装
 *
 * @author 周宁
 * @date 2018/4/13 16:08
 */
public class Criteria {
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

    public Criteria() {
        selectFields = new LinkedHashSet<>();
        whereParams = new LinkedHashSet<>();
        sorts = new LinkedHashSet<>();
        criteriaProxys = new ArrayList<>();
        groupFields = new LinkedHashSet<>();
    }

    public Criteria select(String... fields){
        selectFields.addAll(Arrays.asList(fields));
        return this;
    }

    public Criteria where(String key, Object value) {
        return this.where(key, "=", value);
    }

    public Criteria where(String key, String opt, Object value) {
        this.whereParams.add(WhereParam.builder().key(key).opt(opt).value(value).build());
        return this;
    }

    public Criteria like(String key, Object value) {
        return this.where(key, "LIKE", "%" + value + "%");
    }

    public Criteria gt(String key, Object value) {
        return this.where(key, ">", value);
    }

    public Criteria gte(String key, Object value) {
        return this.where(key, ">=", value);
    }

    public Criteria lt(String key, Object value) {
        return this.where(key, "<", value);
    }

    public Criteria let(String key, Object value) {
        return this.where(key, "<=", value);
    }

    public Criteria notEqual(String key, Object value) {
        return this.where(key, "<>", value);
    }

    public Criteria isNull(String key) {
        return this.where(key, "IS", "NULL");
    }

    public Criteria isNotNull(String key) {
        return this.where(key, "IS", "NOT NULL");
    }

    public Criteria and(String key, Object value) {
        return this.where(key, value);
    }

    public Criteria and(String key, String opt, Object value) {
        return this.where(key, opt, value);
    }

    public Criteria or(String key, Object value) {
        return this.or(key, "=", value);
    }

    public Criteria or(String key, String opt, Object value) {
        if (CollectionUtils.isEmpty(this.whereParams)) {
            throw new RuntimeException("sql error,condition \"or\" must be following after \"where\"!");
        }
        return this.where(" OR " + key, opt, value);
    }

    public Criteria in(String key, List<?> args) {
        return this.where(key, "IN", args);
    }

    public Criteria notIn(String key, List<?> args){
        return this.where(key, "NOT IN", args);
    }

    public Criteria andCriteria(Criteria criteria) {
        return criteria(criteria,"AND");
    }

    public Criteria orCriteria(Criteria criteria) {
        if(CollectionUtils.isEmpty(whereParams)){
            throw new RuntimeException("sql error,condition \"orCriteria\" must be following after \"where\"!");
        }
        return criteria(criteria,"OR");
    }

    private Criteria criteria(Criteria criteria,String criteriaType){
        if(CollectionUtils.isNotEmpty(criteria.getSorts())){
            throw new RuntimeException("unsupport doCriteria operate");
        }
       /* if (EmptyUtils.isNotEmpty(criteria.getCriteriaProxys())) {
            throw new RuntimeException("criteria nesting query is not support");
        }*/
        if(CollectionUtils.isEmpty(whereParams)){
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

    public Criteria orderBy(Sort sort) {
        sorts.add(sort);
        return this;
    }

}