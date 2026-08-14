package com.gysoft.jdbc.bean;

/**
 * SQL 函数表达式对象。
 *
 * @author 周宁
 */
public class FuncExpr {

    /**
     * SQL 表达式文本（如 LENGTH(name) 或 'Tom'）
     */
    private final String sql;

    public FuncExpr(String sql) {
        if (sql == null) {
            throw new GyjdbcException("func expr sql cannot be null");
        }
        this.sql = sql;
    }

    /**
     * 追加别名，返回新表达式，可继续链式组合
     *
     * @param alias 别名
     * @return FuncExpr 带别名的表达式
     */
    public FuncExpr as(String alias) {
        return new FuncExpr(sql + " AS " + alias);
    }

    /**
     * 获取表达式 SQL 文本
     *
     * @return String 表达式文本
     */
    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
