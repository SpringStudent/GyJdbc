package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public class Index {
    /**
     * 表
     */
    private final Table table;
    /**
     * 索引元数据
     */
    private IndexMeta indexMeta;

    public Index(Table table) {
        this.table = table;
        indexMeta = new IndexMeta();
    }

    public Index name(String indexName) {
        this.indexMeta.setIndexName(indexName);
        return this;
    }

    public Index unique() {
        this.indexMeta.setUnique(true);
        return this;
    }

    public Index column(String column) {
        this.indexMeta.getColumnNames().add(column);
        return this;
    }

    public Table commit() {
        table.getTableMeta().getIndexs().add(this.indexMeta);
        return table;
    }
}
