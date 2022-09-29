package com.gysoft.jdbc.bean;



import java.util.List;

/**
 * 查询条件节点抽象
 * @author 周宁
 */
public class SQLTree {
    /**
     * 该criteria节点对应的sql
     */
    private String sql;
    /**
     * 该criteria节点对应的参数
     */
    private Object[] params;
    /**
     * 子sql
     */
    private List<SQLTree> childs;
    /**
     * 树节点的id
     */
    private String id;
    /**
     * 连接类型
     */
    private String unionType;
    /**
     * 将sql作为表的别名
     */
    private String asTable;
    /**
     * 标识从from(String asTable,SQL c)
     * 方法传递asTable，此方法用于给子查询起别名
     */
    private boolean fromAsTable;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public List<SQLTree> getChilds() {
        return childs;
    }

    public void setChilds(List<SQLTree> childs) {
        this.childs = childs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnionType() {
        return unionType;
    }

    public String getAsTable() {
        return asTable;
    }

    public Boolean getFromAsTable() {
        return fromAsTable;
    }

    public void setFromAsTable(Boolean fromAsTable) {
        this.fromAsTable = fromAsTable;
    }

    public void setAsTable(String asTable) {
        this.asTable = asTable;
    }

    public void setUnionType(String unionType) {
        this.unionType = unionType;
    }

    public SQLTree(String sql, Object[] params, List<SQLTree> childs, String id,String unionType,String asTable,boolean fromAsTable) {
        this.sql = sql;
        this.params = params;
        this.childs = childs;
        this.id = id;
        this.unionType = unionType;
        this.asTable = asTable;
        this.fromAsTable = fromAsTable;
    }

    public SQLTree() {
    }
}
