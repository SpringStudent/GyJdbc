package com.gysoft.jdbc.bean;

/**
 * SQL 条件片段：一个不可再分的条件单元，包含 SQL 文本、参数和连接符。
 * 用于替代 doCriteria/doCriteriaProxy 中"先 append 再回退"的字符串拼接模式。
 *
 * @author 周宁
 */
public class ConditionSegment {

    /** 条件 SQL 片段，如 "name = ?" 或 "(type = ? OR status = ?)" */
    private final String sql;

    /** 该片段对应的参数 */
    private final Object[] params;

    /** 该片段与前一个片段之间的连接符（第一个片段可为 null，表示无需连接符） */
    private final ConnectorType connector;

    public ConditionSegment(String sql, Object[] params, ConnectorType connector) {
        this.sql = sql;
        this.params = params;
        this.connector = connector;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParams() {
        return params;
    }

    public ConnectorType getConnector() {
        return connector;
    }
}
