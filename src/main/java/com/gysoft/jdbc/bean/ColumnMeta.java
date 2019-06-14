package com.gysoft.jdbc.bean;

import java.sql.JDBCType;

/**
 * @author 周宁
 */
public class ColumnMeta {
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段长度
     */
    private int length;
    /**
     * 字段精度
     */
    private int precision;
    /**
     * 字段范围
     */
    private int scale;
    /**
     * 是否notnull
     */
    private boolean notNull;
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    /**
     * 类型
     */
    private JDBCType jdbcType;
    /**
     * 默认值
     */
    private Object val;
    /**
     * 类型字符串
     */
    private String dataType;
    /**
     * 备注
     */
    private String comment;
    /**
     * 自增长
     */
    private boolean autoIncr;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(JDBCType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isAutoIncr() {
        return autoIncr;
    }

    public void setAutoIncr(boolean autoIncr) {
        this.autoIncr = autoIncr;
    }
}
