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
