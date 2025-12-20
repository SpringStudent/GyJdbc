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
    private TableEnum.Engine engine;
    /**
     * 是否为临时表
     */
    private boolean temporary;
    /**
     * 是否判断表的存在与否
     */
    private boolean ifNotExists;

    /**
     * 字符集 (例如: utf8mb4)
     * 对应 SQL: DEFAULT CHARSET=utf8mb4
     */
    private String characterSet;

    /**
     * 排序规则 (例如: utf8mb4_general_ci)
     * 对应 SQL: COLLATE=utf8mb4_general_ci
     */
    private String collation;

    /**
     * 自增主键起始值
     * 对应 SQL: AUTO_INCREMENT=1000
     */
    private Long autoIncrement;

    /**
     * 行格式 (例如: DYNAMIC, COMPRESSED, FIXED)
     * 对应 SQL: ROW_FORMAT=DYNAMIC
     */
    private TableEnum.RowFormat rowFormat;

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

    public TableEnum.Engine getEngine() {
        return engine;
    }

    public void setEngine(TableEnum.Engine engine) {
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

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public Long getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Long autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public TableEnum.RowFormat getRowFormat() {
        return rowFormat;
    }

    public void setRowFormat(TableEnum.RowFormat rowFormat) {
        this.rowFormat = rowFormat;
    }
}
