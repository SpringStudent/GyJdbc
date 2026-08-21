package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.bean.GyjdbcException;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 通用工具方法，收敛项目中散落的重复代码。
 *
 * @author 周宁
 */
public class MixUtils {

    /**
     * 将多个参数数组合并为一个。
     */
    public static Object[] appendParams(Object[] params, Object... extraParams) {
        List<Object> result = new ArrayList<>();
        if (params != null) {
            Collections.addAll(result, params);
        }
        Collections.addAll(result, extraParams);
        return result.toArray();
    }

    /**
     * 将数组元素追加到列表中（副作用）。
     */
    public static void addAll(List<Object> params, Object[] values) {
        if (values != null) {
            Collections.addAll(params, values);
        }
    }

    /**
     * 将集合切片，每片大小为 size。
     */
    public static <T> List<T>[] slice(List<T> c, int size) {
        if (size < 1) {
            throw new GyjdbcException("无效的size:" + size);
        }
        if (CollectionUtils.isEmpty(c)) {
            return new List[0];
        }
        int cSize = c.size();
        int sliceNum = cSize % size == 0 ? cSize / size : cSize / size + 1;
        List<T>[] result = new ArrayList[sliceNum];
        for (int i = 0; i < sliceNum; i++) {
            int startIdx = size * i;
            int endIdx = size * (i + 1) > cSize ? cSize : size * (i + 1);
            List<T> l = new ArrayList<>();
            for (int j = startIdx; j < endIdx; j++) {
                l.add(c.get(j));
            }
            result[i] = l;
        }
        return result;
    }

    /**
     * 将 Java 类型映射为 JDBC Types 常量。
     */
    public static int getSqlType(Class<?> clazz) {
        if (String.class.equals(clazz)) {
            return Types.VARCHAR;
        } else if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            return Types.INTEGER;
        } else if (double.class.equals(clazz) || Double.class.equals(clazz)) {
            return Types.DOUBLE;
        } else if (LocalDateTime.class.equals(clazz) || Instant.class.equals(clazz)) {
            return Types.TIMESTAMP;
        } else if (LocalDate.class.equals(clazz) || java.sql.Date.class.equals(clazz)) {
            return Types.DATE;
        } else if (LocalTime.class.equals(clazz) || Time.class.equals(clazz)) {
            return Types.TIME;
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return Types.TIMESTAMP;
        } else if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return Types.BIGINT;
        } else if (float.class.equals(clazz) || Float.class.equals(clazz)) {
            return Types.FLOAT;
        } else if (boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return Types.BOOLEAN;
        } else if (short.class.equals(clazz) || Short.class.equals(clazz)) {
            return Types.INTEGER;
        } else if (byte.class.equals(clazz) || Byte.class.equals(clazz)) {
            return Types.INTEGER;
        } else if (char.class.equals(clazz) || Character.class.equals(clazz)) {
            return Types.CHAR;
        } else if (BigDecimal.class.equals(clazz)) {
            return Types.DECIMAL;
        } else if (BigInteger.class.equals(clazz)) {
            // BigInteger 可能超出 BIGINT 范围，按 DECIMAL 传更安全
            return Types.DECIMAL;
        } else if (clazz != null && clazz.isEnum()) {
            // 走 setString(value.toString())，默认即枚举 name()
            return Types.VARCHAR;
        } else if (UUID.class.equals(clazz)) {
            return Types.VARCHAR;
        } else if (byte[].class.equals(clazz) || Byte[].class.equals(clazz)) {
            return Types.VARBINARY;
        } else if (OffsetDateTime.class.equals(clazz) || ZonedDateTime.class.equals(clazz)) {
            return Types.TIMESTAMP;
        } else if (OffsetTime.class.equals(clazz)) {
            return Types.TIME;
        } else {
            return Types.OTHER;
        }
    }

    /**
     * SQL 字符串字面量转义，用于必须把常量内联进 SQL 文本的场景（函数表达式、DDL 注释/默认值）。
     * <p>按 MySQL 默认 sql_mode（反斜杠是转义符）处理：反斜杠加倍，单引号双写，
     * NUL/换行/回车/Ctrl-Z 转为对应转义序列。单引号采用双写而非 {@code \'}，
     * 因为双写在 {@code NO_BACKSLASH_ESCAPES} 模式下同样合法。</p>
     * <p>必须单趟扫描完成：先 {@code ' -> ''} 再 {@code \ -> \\} 会把已生成的转义再转一次。</p>
     *
     * @param s 原始字符串
     * @return String 转义后可安全内联单引号字面量的字符串，入参为 null 时返回 null
     */
    public static String escapeSqlLiteral(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\'':
                    sb.append("''");
                    break;
                case '\0':
                    sb.append("\\0");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\032':
                    sb.append("\\Z");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将任意数组（含原始类型数组，如 int[]、long[]）转换为 Object[]，用于展开元组比较值
     *
     * @param value 数组对象
     * @return Object[] 数组
     */
    public static Object[] toObjectArray(Object value) {
        int length = Array.getLength(value);
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = Array.get(value, i);
        }
        return result;
    }

}
