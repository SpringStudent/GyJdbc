package com.gysoft.jdbc.bean;

import java.util.function.Consumer;

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

    public Table engine(TableEnum.Engine tableEngine) {
        tableMeta.setEngine(tableEngine);
        return this;
    }

    public Table charset(String charset) {
        tableMeta.setCharacterSet(charset);
        return this;
    }

    public Table collation(String collation) {
        tableMeta.setCollation(collation);
        return this;
    }

    public Table utf8mb4() {
        return this.charset("utf8mb4").collation("utf8mb4_general_ci");
    }

    public Table latin1() {
        return this.charset("latin1").collation("latin1_swedish_ci");
    }

    public Table utf8() {
        return this.charset("utf8").collation("utf8_general_ci");
    }

    public Table autoIncrement(long startVal) {
        tableMeta.setAutoIncrement(startVal);
        return this;
    }

    public Table rowFormat(TableEnum.RowFormat rowFormat) {
        tableMeta.setRowFormat(rowFormat);
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

    public Table column(Consumer<Column> consumer) {
        Column column = new Column(this);
        consumer.accept(column);
        return column.commit();
    }

    public Table index(Consumer<Index> consumer){
        Index index = new Index(this);
        consumer.accept(index);
        return index.commit();
    }

    TableMeta getTableMeta() {
        return tableMeta;
    }

}
