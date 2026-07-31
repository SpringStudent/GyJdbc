package com.gysoft.jdbc.bean;

/**
 * 代理拼接处更复杂的sql
 *
 * @author 周宁
 */
public class CriteriaProxy {
    /**
     * 插入到Set<WhereParam>中的位置
     */
    private int whereParamsIndex = 0;
    /**
     * andCriteria或者orCriteria的sql拼接
     */
    private StringBuilder sql;
    /**
     * 保存andCriteria或者orCriteria条件的入参
     */
    private Object[] params;
    /**
     * 条件片段连接类型
     */
    private ConnectorType connectorType;

    public StringBuilder getSql() {
        return sql;
    }

    public void setSql(StringBuilder sql) {
        this.sql = sql;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public int getWhereParamsIndex() {
        return whereParamsIndex;
    }

    public void setWhereParamsIndex(int whereParamsIndex) {
        this.whereParamsIndex = whereParamsIndex;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }
}
