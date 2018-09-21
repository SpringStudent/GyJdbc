package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 数据库实体映射工具类
 *
 * @author 彭佳佳
 * @data 2018年3月6日
 */
public class EntityTools {
	
    /**
     * 根据实体类名，获取表名称
     *
     * @param entity
     * @return
     */
    public static String getTableName(Class<?> entity) {
        Table table = entity.getAnnotation(Table.class);
        if (null != table&&!StringUtils.isEmpty(table.name())) {
    		 return table.name();
        }else {
        	StringBuffer tableName = new StringBuffer();
        	String entityName = entity.getSimpleName();
            StringBuilder str = new StringBuilder();
            char[] subStr = entityName.toCharArray();
            int i = 0;
            char z = 'Z';
            while (i < entityName.length()) {
                while (i < entityName.length() && subStr[i] > z) {
                    if (Character.isLowerCase(subStr[i])) {
                        str.append(String.valueOf(subStr[i]).toUpperCase());
                    } else {
                        str.append(subStr[i]);
                    }
                    i++;
                }
                if (str.toString().length() > 0) {
                    if ((i - entityName.length() == 0)) {
                        tableName.append(str.toString());
                    } else {
                        tableName.append(str.toString() + "_");
                    }
                }
                if (i < entityName.length()) {
                    str = new StringBuilder();
                    str.append(subStr[i++]);
                }
            }
            return tableName.toString();
        }
    }

    /**
     * 获取主键名称
     *
     * @param entity
     * @return
     */
    public static String getPk(Class<?> entity) {
        Table table = entity.getAnnotation(Table.class);
        if (null != table) {
            return table.pk();
        }
        return "id";
    }

    /**
     * 判断是否为主键
     *
     * @return
     */
    public static boolean isPk(Class<?> entity, Field field) {
        String pk = getPk(entity);
        String columnName = field.getName();
        //该字段是主键
        if (pk.equals(columnName)) {
            return true;
        }
        return false;
    }

    public static String getColumnName(Field field) {
        String columnName = field.getName();
        Column anno = field.getAnnotation(Column.class);
        if (anno != null) {
            Method[] meth = anno.annotationType().getDeclaredMethods();
            if (meth != null) {
                for (Method me : meth) {
                    if (!me.isAccessible()) {
                        me.setAccessible(true);
                    }
                    try {
                        if (me.getName().equals("name")) {
                            columnName = me.invoke(anno, null).toString();
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return columnName;
    }
}
