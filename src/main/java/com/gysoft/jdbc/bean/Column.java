package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.JdbcDataType;

import java.sql.JDBCType;

/**
 * @author 周宁
 */
public class Column {
    /**
     * 表
     */
    private Table table;
    /**
     * 行元数据
     */
    private ColumnMeta columnMeta;

    public Column(Table table) {
        this.table = table;
        columnMeta = new ColumnMeta();
    }

    public Column jdbcType(JDBCType jdbcType) {
        columnMeta.setJdbcType(jdbcType);
        return this;
    }

    public Column notNull() {
        columnMeta.setNotNull(true);
        return this;
    }

    public Column defaults(String val) {
        columnMeta.setVal(val);
        return this;
    }

    public Column defaultCurrentTimestamp() {
        return defaults("CURRENT_TIMESTAMP");
    }

    public Column defaultNull() {
        return defaults("NULL");
    }

    public Column length(int length) {
        columnMeta.setLength(length);
        return this;
    }

    public Column length(int precision, int scale) {
        columnMeta.setLength(precision);
        columnMeta.setScale(scale);
        columnMeta.setPrecision(precision);
        return this;
    }

    public Column varchar(int length) {
        return this.jdbcType(JDBCType.VARCHAR).length(length);
    }

    public Column number(int precision, int scale) {
        return this.jdbcType(JDBCType.NUMERIC).length(precision, scale);
    }

    public Column number(int len) {
        return this.jdbcType(JDBCType.NUMERIC).length(len, 0);
    }

    public Column clob() {
        return this.jdbcType(JDBCType.CLOB);
    }

    public Column integer() {
        return this.jdbcType(JDBCType.INTEGER);
    }

    public Column datetime() {
        return this.jdbcType(JDBCType.TIMESTAMP);
    }

    public Column datetime(int length) {
        return this.jdbcType(JDBCType.TIMESTAMP).length(length);
    }

    public Column tinyint() {
        return this.jdbcType(JDBCType.TINYINT);
    }

    public Column primary() {
        columnMeta.setPrimaryKey(true);
        return this;
    }

    public Column name(String name) {
        columnMeta.setName(name);
        return this;
    }

    public Column comment(String comment){
        columnMeta.setComment(comment);
        return this;
    }

    public Column autoIncrement(){
        columnMeta.setAutoIncr(true);
        return this;
    }

    public Table commit() {
        columnMeta.setDataType(JdbcDataType.dataType(this.columnMeta));
        table.getTableMeta().getColumns().add(this.columnMeta);
        return table;
    }


}
