package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public class Table {
    /**
     * 持有的sql对象
     */
    private final SQL sql;
    /**
     * 表的元数据
     */
    private TableMeta tableMeta;

    public Table(SQL sql) {
        this.sql = sql;
        tableMeta = new TableMeta();
    }

    public Table temporary(){
        tableMeta.setTemporary(true);
        return this;
    }

    public Table table(String name) {
        tableMeta.setName(name);
        return this;
    }

    public Table table(){
        return this;
    }

    public Table ifNotExists(){
        tableMeta.setIfNotExists(true);
        return this;
    }

    public Table comment(String comment) {
        tableMeta.setComment(comment);
        return this;
    }

    public Table engine(TableEngine tableEngine) {
        tableMeta.setEngine(tableEngine);
        return this;
    }

    public SQL commit() {
        sql.setTableMeta(tableMeta);
        return sql;
    }

    public Column column() {
        return new Column(this);
    }

    public Index index(){
        return new Index(this);
    }

    TableMeta getTableMeta() {
        return tableMeta;
    }

}
