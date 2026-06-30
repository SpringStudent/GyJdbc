package com.gysoft.jdbc.bean;

/**
 * mysql查询条件封装
 * @author 周宁
 */
public class Criteria extends AbstractCriteria<Criteria> {

    public static Criteria newCriteria() {
        return new Criteria();
    }

    public static Criteria newCriteria(String key, Object value) {
        return newCriteria().where(key, value);
    }

    public static <T, R> Criteria newCriteria(TypeFunction<T, R> function, Object value) {
        return newCriteria().where(function, value);
    }

    public static Criteria newCriteria(String key, String opt, Object value) {
        return newCriteria().where(key, opt, value);
    }

    public static <T, R> Criteria newCriteria(TypeFunction<T, R> function, String opt, Object value) {
        return newCriteria().where(function, opt, value);
    }
}
