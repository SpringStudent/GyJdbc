package com.gysoft.jdbc.bean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author 周宁
 */
public class IndexMeta {
    /**
     * 索引名称
     */
    private String indexName;
    /**
     * 行
     */
    private Set<String> columnNames = new LinkedHashSet();
    /**
     * 是否为唯一索引
     */
    private boolean unique;
    /**
     * 索引类型
     */
    private String indexType;
    /**
     * 备注
     */
    private String comment;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Set<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(Set<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
