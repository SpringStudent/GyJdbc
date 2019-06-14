package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.bean.ColumnMeta;

import java.sql.JDBCType;

/**
 * @author 周宁
 */
public class JdbcDataType {

    /**
     * 数据类型转黄
     * @param meta 一行对应的元数据
     * @return String 将java的行类型转换为mysql认识的类型
     */
    public static String dataType(ColumnMeta meta) {
        if (meta.getJdbcType().equals(JDBCType.CHAR)) {
            return "char(" + meta.getLength() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.VARCHAR)) {
            return "varchar(" + meta.getLength() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.TIMESTAMP)) {
            return "datetime("+meta.getLength()+")";
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
            return "bigint";
        }
        if (meta.getJdbcType().equals(JDBCType.DOUBLE)) {
            return "double";
        }
        if (meta.getJdbcType().equals(JDBCType.INTEGER)) {
            return "int";
        }
        if (meta.getJdbcType().equals(JDBCType.NUMERIC)) {
            return "decimal(" + meta.getPrecision() + "," + meta.getScale() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.DECIMAL)) {
            return "decimal(" + meta.getPrecision() + "," + meta.getScale() + ")";
        }
        if (meta.getJdbcType().equals(JDBCType.TINYINT)) {
            return "tinyint";
        }
        if (meta.getJdbcType().equals(JDBCType.OTHER)) {
            return "other";
        }
        throw new IllegalArgumentException("不合法的jdbcType");
    }
}
