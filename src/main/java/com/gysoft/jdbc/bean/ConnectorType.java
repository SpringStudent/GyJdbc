package com.gysoft.jdbc.bean;

/**
 * SQL条件片段的连接符类型，替代 doCriteriaProxy 中的魔数字符串。
 *
 * @author 周宁
 */
public enum ConnectorType {

    /** andCriteria() 产生的条件组，需要括号包裹 */
    AND_GROUP("AND", true),

    /** orCriteria() 产生的条件组，需要括号包裹 */
    OR_GROUP("OR", true),

    /** and(Where) 产生的条件，不需要括号 */
    WHERE_AND("AND", false),

    /** or(Where) 产生的条件，不需要括号 */
    WHERE_OR("OR", false),

    /** JOIN ON 的第一个条件 */
    JOIN_ON("ON", false),

    /** JOIN ON 中的后续 AND 条件 */
    JOIN_AND("AND", false);

    private final String keyword;
    private final boolean wrapInParens;

    ConnectorType(String keyword, boolean wrapInParens) {
        this.keyword = keyword;
        this.wrapInParens = wrapInParens;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isWrapInParens() {
        return wrapInParens;
    }
}
