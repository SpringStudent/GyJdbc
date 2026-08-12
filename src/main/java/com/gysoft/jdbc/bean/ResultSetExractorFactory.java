package com.gysoft.jdbc.bean;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 周宁
 */
public abstract class ResultSetExractorFactory {

    /**
     * 两列结果值得映射Mapper类
     */
    public static <A, B> ResultSetExtractor createDoubleColumnValueResultSetExtractor(Class<A> kCls, Class<B> vCls) {
        return new ResultSetExtractor() {
            Map result = new LinkedHashMap<>(1);

            @Override
            public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    result.put(getColumVal(kCls, rs, 1), getColumVal(vCls, rs, 2));
                }
                return result;
            }
        };
    }

    public static <T> Object getColumVal(Class<T> cls, ResultSet rs, int i) throws SQLException {
        if (String.class.equals(cls)) {
            return rs.getString(i);
        } else if (int.class.equals(cls) || Integer.class.equals(cls)) {
            return rs.getInt(i);
        } else if (double.class.equals(cls) || Double.class.equals(cls)) {
            return rs.getDouble(i);
        } else if (LocalDateTime.class.equals(cls)) {
            Timestamp timestamp = rs.getTimestamp(i);
            return timestamp == null ? null : timestamp.toLocalDateTime();
        } else if (LocalDate.class.equals(cls)) {
            java.sql.Date date = rs.getDate(i);
            return date == null ? null : date.toLocalDate();
        } else if (LocalTime.class.equals(cls)) {
            Time time = rs.getTime(i);
            return time == null ? null : time.toLocalTime();
        } else if (Instant.class.equals(cls)) {
            Timestamp timestamp = rs.getTimestamp(i);
            return timestamp == null ? null : timestamp.toInstant();
        } else if (Date.class.isAssignableFrom(cls)) {
            return rs.getDate(i);
        } else if (long.class.equals(cls) || Long.class.equals(cls)) {
            return rs.getLong(i);
        } else if (float.class.equals(cls) || Float.class.equals(cls)) {
            return rs.getFloat(i);
        } else if (boolean.class.equals(cls) || Boolean.class.equals(cls)) {
            return rs.getBoolean(i);
        } else if (short.class.equals(cls) || Short.class.equals(cls)) {
            return rs.getShort(i);
        } else if (byte.class.equals(cls) || Byte.class.equals(cls)) {
            return rs.getByte(i);
        } else if (BigDecimal.class.equals(cls)) {
            return rs.getBigDecimal(i);
        } else {
            return rs.getObject(i);
        }
    }

}
