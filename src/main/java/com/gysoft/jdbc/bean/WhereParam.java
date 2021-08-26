package com.gysoft.jdbc.bean;

import java.util.Collection;

/**
 *@author 周宁
 */
public class WhereParam {

    public enum OptEnum {
        Like, Gt, Gte, Lt, Let, NotEqual, IsNull, IsNotNull,
        Equal, Exists, NotExists, BetweenAnd, In, NotIn, FindInSet;
    }

    private String key;
    private String opt;
    private Object value;

    private OptEnum optEnum;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public OptEnum getOptEnum() {
        return optEnum;
    }

    public void setOptEnum(OptEnum optEnum) {
        this.optEnum = optEnum;
    }

    WhereParam(String key, String opt, Object value) {
        this.key = key;
        this.opt = opt;
        this.value = value;
    }

    WhereParam(String key, OptEnum optEnum, Object value) {
        this.key = key;
        this.optEnum = optEnum;
        this.value = value;
    }

    WhereParam(String key) {
        this.key = key;
    }

    WhereParam() {
    }


    public static WhereParam where(String key){
        return new WhereParam(key);
    }

    public static <T, R> WhereParam where(TypeFunction<T, R> function) {
        return new WhereParam(TypeFunction.getLambdaColumnName(function));
    }


    public WhereParam equal(Object val) {
        this.optEnum = OptEnum.Equal;
        this.value =val;
        return this;
    }

    public WhereParam notEqual(Object val) {
        this.optEnum = OptEnum.NotEqual;
        this.value =val;
        return this;
    }

    public WhereParam lt(Object val) {
        this.optEnum = OptEnum.Lt;
        this.value =val;
        return this;
    }

    public WhereParam let(Object val) {
        this.optEnum = OptEnum.Let;
        this.value =val;
        return this;
    }

    public WhereParam gt(Object val) {
        this.optEnum = OptEnum.Gt;
        this.value =val;
        return this;
    }

    public WhereParam gte(Object val) {
        this.optEnum = OptEnum.Gte;
        this.value =val;
        return this;
    }

    public WhereParam in(Collection<?> val) {
        this.optEnum = OptEnum.In;
        this.value =val;
        return this;
    }

    public WhereParam notIn(Collection<?> val) {
        this.optEnum = OptEnum.NotIn;
        this.value =val;
        return this;
    }

    public WhereParam like(Object val) {
        this.optEnum = OptEnum.Like;
        this.value =val;
        return this;
    }

    public WhereParam isNull() {
        this.optEnum = OptEnum.IsNull;
        this.value =null;
        return this;
    }

    public WhereParam isNotNull() {
        this.optEnum = OptEnum.IsNotNull;
        this.value =null;
        return this;
    }

    public WhereParam exists(SQL sql) {
        this.optEnum = OptEnum.Exists;
        this.value =sql;
        return this;
    }

    public WhereParam notExists(SQL sql) {
        this.optEnum = OptEnum.NotExists;
        this.value =sql;
        return this;
    }

    public WhereParam findInSet(Object val) {
        this.optEnum = OptEnum.FindInSet;
        this.value =val;
        return this;
    }

    public WhereParam betweenAnd(Object v1, Object v2) {
        this.optEnum = OptEnum.BetweenAnd;
        this.value =new Pair(v1,v2);
        return this;
    }
}