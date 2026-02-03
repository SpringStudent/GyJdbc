package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public enum JoinType {
    LeftJoin("LEFT JOIN"),
    RightJoin("RIGHT JOIN"),
    InnerJoin("INNER JOIN"),
    @Deprecated
    NatureJoin(",");

    private final String type;

    JoinType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
