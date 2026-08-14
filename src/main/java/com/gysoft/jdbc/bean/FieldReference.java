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

    /**
     * 以函数表达式作为字段引用（列对列/列对函数比较等场景）
     *
     * @param expr 函数表达式
     * @return FieldReference 字段引用
     */
    public static FieldReference newFieldRef(FuncExpr expr) {
        return new FieldReference(expr);
    }

    /**
     * 以函数表达式作为字段引用（列对列比较等场景）
     *
     * @param expr 函数表达式
     */
    public FieldReference(FuncExpr expr) {
        this(expr.getSql());
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
