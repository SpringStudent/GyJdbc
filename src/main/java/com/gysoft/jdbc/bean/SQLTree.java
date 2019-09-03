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

    public SQLTree(String sql, Object[] params, List<SQLTree> childs, String id) {
        this.sql = sql;
        this.params = params;
        this.childs = childs;
        this.id = id;
    }

    public SQLTree() {
    }
}
