package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.EntityTools;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 按 {@link com.gysoft.jdbc.annotation.Column} 列名映射的实体 RowMapper。
 * <p>写侧使用 @Column(name) 落库，读侧也应使用同一列名回读，否则当属性名与列名
 * 无法经驼峰/下划线互转对上时（如属性 nickname 映射列 real_name），
 * save/query 会出现数据不一致。本类建立「列名（统一小写）→ 字段」映射，
 * 同时保留字段名的驼峰转下划线形式，兼容 @Column 改名与默认匹配两种场景；
 * 列值按字段类型用 JdbcUtils.getResultSetValue 读取，primitive 字段遇 NULL 跳过设置保留默认值，
 * 与 BeanPropertyRowMapper 行为一致。</p>
 *
 * @param <T> 实体类型
 * @author 周宁
 */
public class EntityRowMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;

    /** 列名（统一小写）→ 字段 */
    private final Map<String, Field> columnToField = new HashMap<>();

    /**
     * 创建实体查询 RowMapper。
     * <p>字段带 @Column(name) 时使用按注解列名映射的 {@link EntityRowMapper}，
     * 否则回退为 Spring 的 BeanPropertyRowMapper。修复 @Column(name) 仅在写侧生效、
     * 查询侧读回 null 导致 save/query 数据不一致的缺陷。</p>
     */
    public static <T> RowMapper<T> newRowMapper(Class<T> clss) {
        if (EntityTools.hasColumnAnnotation(clss)) {
            return new EntityRowMapper<>(clss);
        }
        return BeanPropertyRowMapper.newInstance(clss);
    }

    public EntityRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        for (Field field : EntityTools.getDeclaredFields(mappedClass)) {
            String column = EntityTools.getColumnName(field);
            columnToField.put(column.toLowerCase(), field);
            String underscored = underscoreName(field.getName());
            if (!underscored.equalsIgnoreCase(column)) {
                columnToField.put(underscored.toLowerCase(), field);
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T bean = BeanUtils.instantiateClass(mappedClass);
        BeanWrapper bw = new BeanWrapperImpl(bean);
        bw.setConversionService(DefaultConversionService.getSharedInstance());
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            Field field = columnToField.get(meta.getColumnLabel(i).toLowerCase());
            if (field != null) {
                Object value = JdbcUtils.getResultSetValue(rs, i, field.getType());
                // primitive 字段遇 NULL 时 getResultSetValue 返回 null，跳过设置以保留默认值（0/false），
                // 与 BeanPropertyRowMapper 的 primitivesDefaultedForNullValue=true 行为一致
                if (value == null && field.getType().isPrimitive()) {
                    continue;
                }
                bw.setPropertyValue(field.getName(), value);
            }
        }
        return bean;
    }

    /**
     * 驼峰转下划线（小写），与 BeanPropertyRowMapper 的默认列名匹配规则一致。
     */
    private static String underscoreName(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }
}
