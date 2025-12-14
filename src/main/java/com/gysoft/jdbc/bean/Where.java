package com.gysoft.jdbc.bean;

import java.util.Collection;

/**
 * @author zhouning
 */
public class Where {

    private String key;
    private Criteria criteria;

    public Where() {
        this.criteria = new Criteria();
    }

    public Where(String key) {
        this.criteria = new Criteria();
        this.key = key;
    }

    protected Where(Criteria criteria, String key) {
        this.criteria = criteria;
        this.key = key;
    }

    public static Where where(String key) {
        return new Where(key);
    }

    public static <T, R> Where where(TypeFunction<T, R> function) {
        return new Where(TypeFunction.getLambdaColumnName(function));
    }

    public Where or(String key) {
        return new Where(criteria, " OR " + key);
    }

    public <T, R> Where or(TypeFunction<T, R> function) {
        return new Where(criteria, " OR " + TypeFunction.getLambdaColumnName(function));
    }

    public Where and(String key) {
        return new Where(criteria, key);
    }

    public <T, R> Where and(TypeFunction<T, R> function) {
        return and(TypeFunction.getLambdaColumnName(function));
    }

    public Where equal(Object val) {
        criteria.and(key, val);
        return this;
    }

    public Where notEqual(Object val) {
        criteria.notEqual(key, val);
        return this;
    }

    public Where lt(Object val) {
        criteria.lt(key, val);
        return this;
    }

    public Where let(Object val) {
        criteria.let(key, val);
        return this;
    }

    public Where gt(Object val) {
        criteria.gt(key, val);
        return this;
    }

    public Where gte(Object val) {
        criteria.gte(key, val);
        return this;
    }

    public Where in(Collection<?> val) {
        criteria.in(key, val);
        return this;
    }

    public Where notIn(Collection<?> val) {
        criteria.notIn(key, val);
        return this;
    }

    public Where like(Object val) {
        criteria.like(key, val);
        return this;
    }

    public Where isNull() {
        criteria.isNull(key);
        return this;
    }

    public Where isNotNull() {
        criteria.isNotNull(key);
        return this;
    }

    public Where exists(SQL sql) {
        criteria.exists(sql);
        return this;
    }

    public Where notExists(SQL sql) {
        criteria.notExists(sql);
        return this;
    }

    public Where betweenAnd(Object v1, Object v2) {
        criteria.betweenAnd(key, v1, v2);
        return this;
    }

    Criteria getCriteria() {
        return criteria;
    }

}
