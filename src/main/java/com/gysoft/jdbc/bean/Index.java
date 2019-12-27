package com.gysoft.jdbc.bean;

import java.util.Arrays;

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

    public Index column(String... columns) {
        this.indexMeta.getColumnNames().addAll(Arrays.asList(columns));
        return this;
    }

    public Index usingBtree(){
        this.indexMeta.setIndexType("USING BTREE");
        return this;
    }

    public Index usingHash(){
        this.indexMeta.setIndexType("USING HASH");
        return this;
    }

    public Index comment(String comment){
        this.indexMeta.setComment(comment);
        return this;
    }

    public Table commit() {
        table.getTableMeta().getIndexs().add(this.indexMeta);
        return table;
    }
}
