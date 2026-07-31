package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.bean.GyjdbcException;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        } else if (BigDecimal.class.equals(clazz)) {
            return Types.DECIMAL;
        } else {
            return Types.OTHER;
        }
    }
}
