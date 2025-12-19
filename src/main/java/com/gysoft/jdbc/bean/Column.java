package com.gysoft.jdbc.bean;

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

    public Column defaultVal(String val) {
        columnMeta.setVal(val);
        return this;
    }

    public Column defaultCurrentTimestamp() {
        return defaultVal("CURRENT_TIMESTAMP");
    }

    public Column defaultNull() {
        return defaultVal("NULL");
    }

    public Column length(int length) {
        columnMeta.setLength(length);
        columnMeta.setPrecision(length);
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

    public Column comment(String comment) {
        columnMeta.setComment(comment);
        return this;
    }

    public Column autoIncrement() {
        columnMeta.setAutoIncr(true);
        return this;
    }

    public Table commit() {
        columnMeta.setDataType(dataType(this.columnMeta));
        table.getTableMeta().getColumns().add(this.columnMeta);
        return table;
    }

    private String dataType(ColumnMeta meta) {
        if (meta.getJdbcType().equals(JDBCType.CHAR)) {
            return "char(" + meta.getLength() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.VARCHAR)) {
            return "varchar(" + meta.getLength() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.TIMESTAMP)) {
            return "datetime(" + Math.min(6, meta.getLength()) + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.TIME)) {
            return "time";
        }
        if (meta.getJdbcType().equals(JDBCType.DATE)) {
            return "date";
        }
        if (meta.getJdbcType().equals(JDBCType.CLOB)) {
            return "text";
        }
        if (meta.getJdbcType().equals(JDBCType.LONGVARBINARY)) {
            return "longblob";
        }
        if (meta.getJdbcType().equals(JDBCType.LONGVARCHAR)) {
            return "longtext";
        }
        if (meta.getJdbcType().equals(JDBCType.BLOB)) {
            return "blob";
        }
        if (meta.getJdbcType().equals(JDBCType.BIGINT)) {
            if (meta.getLength() > 0) {
                return "bigint(" + meta.getLength() + ")";
            } else {
                return "bigint";
            }
        }
        if (meta.getJdbcType().equals(JDBCType.INTEGER)) {
            if (meta.getLength() > 0) {
                return "int(" + meta.getLength() + ")";
            } else {
                return "int";
            }
        }
        if (meta.getJdbcType().equals(JDBCType.TINYINT)) {
            if (meta.getLength() > 0) {
                return "tinyint(" + meta.getLength() + ")";
            } else {
                return "tinyint";
            }
        }
        if (meta.getJdbcType().equals(JDBCType.BOOLEAN)) {
            return "tinyint";
        }
        if (meta.getJdbcType().equals(JDBCType.NUMERIC)) {
            return "decimal(" + meta.getPrecision() + "," + meta.getScale() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.DECIMAL)) {
            return "decimal(" + meta.getPrecision() + "," + meta.getScale() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.DOUBLE)) {
            if (meta.getLength() > 0) {
                return "double(" + meta.getPrecision() + "," + meta.getScale() + ")";
            } else {
                return "double";
            }
        }
        if (meta.getJdbcType().equals(JDBCType.OTHER)) {
            return "other";
        }
        throw new GyjdbcException("unknown jdbcType");
    }


}
