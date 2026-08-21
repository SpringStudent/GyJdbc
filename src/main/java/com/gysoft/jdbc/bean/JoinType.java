package com.gysoft.jdbc.bean;

/**
 * 连接类型。
 *
 * @author 周宁
 */
public enum JoinType {
    LeftJoin("LEFT JOIN"),
    RightJoin("RIGHT JOIN"),
    InnerJoin("INNER JOIN"),
    /**
     * 逗号交叉连接（{@code FROM a, b}），并非 SQL 的 {@code NATURAL JOIN}。
     * <p>MySQL 中 {@code A, B ON ...} 是语法错误，因此该类型不允许携带 ON 条件；
     * 需要连接条件请用 {@link #InnerJoin}，或把条件写进 WHERE。
     * 另外逗号连接的优先级低于显式 JOIN（MySQL 5.0.12 起），与显式 JOIN 混用时注意求值顺序。</p>
     */
    NatureJoin(","),
    /**
     * 标准交叉连接（{@code A CROSS JOIN B}），笛卡尔积。
     * <p>与逗号连接 {@link #NatureJoin} 的区别：{@code CROSS JOIN} 是显式 JOIN，优先级与
     * {@code INNER JOIN} 一致，与其它 JOIN 混用时求值顺序明确。</p>
     */
    CrossJoin("CROSS JOIN");

    private final String type;

    JoinType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
