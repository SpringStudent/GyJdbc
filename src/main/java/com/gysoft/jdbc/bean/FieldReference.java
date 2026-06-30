package com.gysoft.jdbc.bean;

/**
 * 代表字段的引用
 *
 * @author 周宁
 */
public class FieldReference {
    /**
     * 字段
     */
    private String field;

    public static FieldReference newFieldRef(String field) {
        return new FieldReference(field);
    }

    public static <T, R> FieldReference newFieldRef(TypeFunction<T, R> function) {
        return new FieldReference(TypeFunction.getLambdaColumnName(function));
    }

    public FieldReference(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "FieldReference(" + field + ")";
    }
}
