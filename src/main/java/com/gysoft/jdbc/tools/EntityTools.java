package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库实体映射工具类
 *
 * @author 彭佳佳
 */
public class EntityTools {

    private static final Map<Class<?>, String> TABLE_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> PK_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field[]> FIELDS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Field, String> COLUMN_NAME_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据实体类名，获取表名称
     *
     * @param entity 实体类型
     * @return String 表名称
     */
    public static String getTableName(Class<?> entity) {
        return TABLE_NAME_CACHE.computeIfAbsent(entity, EntityTools::doGetTableName);
    }

    private static String doGetTableName(Class<?> entity) {
        Table table = entity.getAnnotation(Table.class);
        if (null != table && !StringUtils.isEmpty(table.name())) {
            return table.name();
        } else {
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
     * @param entity 实体类型
     * @return String 表主键
     */
    public static String getPk(Class<?> entity) {
        return PK_CACHE.computeIfAbsent(entity, EntityTools::doGetPk);
    }

    private static String doGetPk(Class<?> entity) {
        Table table = entity.getAnnotation(Table.class);
        if (null != table) {
            return table.pk();
        }
        return "id";
    }

    /**
     * 获取实体所有需要持久化的字段：
     * 1. 递归收集父类字段，支持实体继承；
     * 2. 排除 static / transient 字段，避免 serialVersionUID 等被误当作数据库列；
     * 3. 子类字段优先，父类同名字段视为被隐藏。
     */
    public static Field[] getDeclaredFields(Class<?> entity) {
        return FIELDS_CACHE.computeIfAbsent(entity, clss -> {
            LinkedHashMap<String, Field> fieldMap = new LinkedHashMap<>();
            Class<?> current = clss;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())
                            || Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }
                    // 子类字段优先，父类同名字段被隐藏
                    if (!fieldMap.containsKey(field.getName())) {
                        field.setAccessible(true);
                        fieldMap.put(field.getName(), field);
                    }
                }
                current = current.getSuperclass();
            }
            return fieldMap.values().toArray(new Field[0]);
        });
    }

    /**
     * 判断是否为主键
     *
     * @param entity 实体类型
     * @param field  属性
     * @return boolean true代表主键 false反之
     */
    public static boolean isPk(Class<?> entity, Field field) {
        String pk = getPk(entity);
        // 兼容 @Table.pk 声明为字段名或列名两种写法：字段原名与 @Column 列名任一匹配即为主键
        String fieldName = field.getName();
        String columnName = getColumnName(field);
        return pk.equals(fieldName) || pk.equals(columnName);
    }

    /**
     * 定位主键字段（通过 isPk 识别，兼容主键字段带 @Column 改名的场景）
     *
     * @param entity 实体类型
     * @return 主键字段；未识别到主键时返回 null
     */
    public static Field getPkField(Class<?> entity) {
        for (Field field : getDeclaredFields(entity)) {
            if (isPk(entity, field)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 获取主键列名：优先取主键字段的 @Column 列名（无注解则用字段名），
     * 避免直接用 @Table.pk 字符串拼 SQL 时主键列被 @Column 改名导致列名错误
     *
     * @param entity 实体类型
     * @return 主键列名；未识别到主键时回退为 @Table.pk（或默认 "id"）
     */
    public static String getPkColumnName(Class<?> entity) {
        Field pkField = getPkField(entity);
        return pkField == null ? getPk(entity) : getColumnName(pkField);
    }

    public static String getColumnName(Field field) {
        return COLUMN_NAME_CACHE.computeIfAbsent(field, EntityTools::doGetColumnName);
    }

    private static String doGetColumnName(Field field) {
        String columnName = field.getName();
        Column anno = field.getAnnotation(Column.class);
        // @Column 存在但未指定 name 时，回退使用字段名，避免生成空列名
        if (anno != null && StringUtils.isNotBlank(anno.name())) {
            columnName = anno.name();
        }
        return columnName;
    }

    public static String transferColumnName(String columnName) {
        if (!columnName.startsWith("`") && !columnName.endsWith("`")) {
            return "`" + columnName + "`";
        }
        return columnName;
    }

    public static String transferFieldName(String fieldName) {
        if (fieldName.startsWith("`") && fieldName.endsWith("`")) {
            return fieldName.substring(1, fieldName.length() - 1);
        }
        return fieldName;
    }
}
