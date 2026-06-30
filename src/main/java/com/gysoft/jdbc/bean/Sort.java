package com.gysoft.jdbc.bean;

import java.io.Serializable;

/**
 * @author 周宁
 */
public class Sort implements Serializable {

    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序类型(DESC降序，ASC升序)
     */
    private String sortType = "DESC";

    public static Sort by(String sortField) {
        return new Sort(sortField);
    }

    public static <T, R> Sort by(TypeFunction<T, R> function) {
        return new Sort(function);
    }

    public static Sort asc(String sortField) {
        return new Sort(sortField, "ASC");
    }

    public static <T, R> Sort asc(TypeFunction<T, R> function) {
        return new Sort(function, "ASC");
    }

    public static Sort desc(String sortField) {
        return new Sort(sortField, "DESC");
    }

    public static <T, R> Sort desc(TypeFunction<T, R> function) {
        return new Sort(function, "DESC");
    }

    public Sort(String sortField) {
        this.sortField = sortField;
    }

    public <T, R> Sort(TypeFunction<T, R> function) {
        this.sortField = TypeFunction.getLambdaColumnName(function);
    }

    public Sort(String sortField, String sortType) {
        this.sortField = sortField;
        this.sortType = sortType;
    }

    public <T, R> Sort(TypeFunction<T, R> function, String sortType) {
        this.sortField = TypeFunction.getLambdaColumnName(function);
        this.sortType = sortType;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }
}
