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
    NatureJoin(","),
    CrossJoin("CROSS JOIN");

    private final String type;

    JoinType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
