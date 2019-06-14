package com.gysoft.jdbc.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周宁
 */
public class TableMeta {
    /**
     * 表名称
     */
    private String name;
    /**
     * 备注
     */
    private String comment;
    /**
     * 引擎类型
     */
    private TableEngine engine;
    /**
     * 是否为临时表
     */
    private boolean temporary;

    private List<ColumnMeta> columns = new ArrayList<>();

    private List<IndexMeta> indexs = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TableEngine getEngine() {
        return engine;
    }

    public void setEngine(TableEngine engine) {
        this.engine = engine;
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }

    public List<IndexMeta> getIndexs() {
        return indexs;
    }

    public void setIndexs(List<IndexMeta> indexs) {
        this.indexs = indexs;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }
}
