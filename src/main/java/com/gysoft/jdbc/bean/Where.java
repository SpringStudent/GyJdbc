package com.gysoft.jdbc.bean;

import java.util.Collection;
import java.util.function.Predicate;

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

    public Where notLike(Object val) {
        criteria.notLike(key, val);
        return this;
    }

    public Where startsWith(Object val) {
        criteria.startsWith(key, val);
        return this;
    }

   public Where endsWith(Object val) {
       criteria.endsWith(key, val);
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

    public Where likeIfAbsent(Object val) {
        return likeIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where likeIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.like(key, val);
        return this;
    }

    public Where likeRIfAbsent(Object val) {
        return likeRIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where likeRIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.likeR(key, val);
        return this;
    }

    public Where likeLIfAbsent(Object val) {
        return likeLIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where likeLIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.likeL(key, val);
        return this;
    }

    public Where equalIfAbsent(Object val) {
        return equalIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where equalIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.and(key, val);
        return this;
    }

    public Where notEqualIfAbsent(Object val) {
        return notEqualIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where notEqualIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.notEqual(key, val);
        return this;
    }

    public Where gtIfAbsent(Object val) {
        return gtIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where gtIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.gt(key, val);
        return this;
    }

    public Where gteIfAbsent(Object val) {
        return gteIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where gteIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.gte(key, val);
        return this;
    }

    public Where ltIfAbsent(Object val) {
        return ltIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where ltIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.lt(key, val);
        return this;
    }

    public Where letIfAbsent(Object val) {
        return letIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where letIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.let(key, val);
        return this;
    }

    public Where inIfAbsent(Collection<?> val) {
        return inIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where inIfAbsent(Collection<?> val, Predicate<Collection> predicate) {
        if (predicate.test(val)) criteria.in(key, val);
        return this;
    }

    public Where notInIfAbsent(Collection<?> val) {
        return notInIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where notInIfAbsent(Collection<?> val, Predicate<Collection> predicate) {
        if (predicate.test(val)) criteria.notIn(key, val);
        return this;
    }

    public Where notLikeIfAbsent(Object val) {
        return notLikeIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where notLikeIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.notLike(key, val);
        return this;
    }

    public Where startsWithIfAbsent(Object val) {
        return startsWithIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where startsWithIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.startsWith(key, val);
        return this;
    }

    public Where endsWithIfAbsent(Object val) {
        return endsWithIfAbsent(val, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where endsWithIfAbsent(Object val, Predicate<Object> predicate) {
        if (predicate.test(val)) criteria.endsWith(key, val);
        return this;
    }

    public Where betweenAndIfAbsent(Object v1, Object v2) {
        return betweenAndIfAbsent(v1, v2, AuxiliaryOperation.getDefaultPredicate());
    }

    public Where betweenAndIfAbsent(Object v1, Object v2, Predicate<Object> predicate) {
        if (predicate.test(v1) && predicate.test(v2)) criteria.betweenAnd(key, v1, v2);
        return this;
    }

    Criteria getCriteria() {
        return criteria;
    }

}
