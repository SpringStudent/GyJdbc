package com.gysoft.jdbc.bean;

import java.io.Serializable;
import java.util.Objects;

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
        this.sortType = normalizeSortType(sortType);
    }

    public <T, R> Sort(TypeFunction<T, R> function, String sortType) {
        this.sortField = TypeFunction.getLambdaColumnName(function);
        this.sortType = normalizeSortType(sortType);
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
        this.sortType = normalizeSortType(sortType);
    }

    private static String normalizeSortType(String sortType) {
        if (sortType == null || sortType.trim().isEmpty()) {
            throw new GyjdbcException("sort type cannot be null or empty");
        }
        String upper = sortType.trim().toUpperCase();
        if (!"ASC".equals(upper) && !"DESC".equals(upper)) {
            throw new GyjdbcException("invalid sort type: '" + sortType + "', only ASC or DESC is allowed");
        }
        return upper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sort sort = (Sort) o;
        return Objects.equals(sortField, sort.sortField)
                && Objects.equals(sortType, sort.sortType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortField, sortType);
    }
}
