package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.tools.EntityTools;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author 周宁
 */
@FunctionalInterface
public interface TypeFunction<T, R> extends Serializable, Function<T, R> {

    /**
     * 获取列名称
     *
     * @param lambda lamda表达式
     * @return String 列名称
     */
    /**
     * Lambda 列名解析结果缓存：key 为「实现类全限定名#字段名」，避免同一 Lambda 引用重复反射解析
     */
    Map<String, String> LAMBDA_COLUMN_CACHE = new ConcurrentHashMap<>();

    static String getLambdaColumnName(Serializable lambda) {
        try {
            Method method = lambda.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(lambda);
            String implMethodName = serializedLambda.getImplMethodName();
            // 按前缀截取属性名,兼容getXxx/isXxx;
            // 不能用replace("get","")——那会误删属性名中间的"get"(如getTarget -> tar)
            String propertyName;
            if (implMethodName.startsWith("get") && implMethodName.length() > 3) {
                propertyName = implMethodName.substring(3);
            } else if (implMethodName.startsWith("is") && implMethodName.length() > 2) {
                propertyName = implMethodName.substring(2);
            } else {
                propertyName = implMethodName;
            }
            String fieldName = Introspector.decapitalize(propertyName);
            String implClass = serializedLambda.getImplClass().replace("/", ".");
            // writeReplace 反射每次都要执行（否则拿不到 SerializedLambda），只缓存字段名解析结果
            return LAMBDA_COLUMN_CACHE.computeIfAbsent(implClass + "#" + fieldName,
                    key -> resolveColumnName(implClass, fieldName));
        } catch (ReflectiveOperationException e) {
            throw new GyjdbcException(e);
        }
    }

    /**
     * 解析列名：优先取字段上的 @Column 注解列名，否则按驼峰转下划线
     *
     * @param implClass 实现类全限定名
     * @param fieldName 字段名
     * @return String 列名
     */
    static String resolveColumnName(String implClass, String fieldName) {
        try {
            Class<?> clazz = Class.forName(implClass);
            Field field = null;
            for (Field f : EntityTools.getDeclaredFields(clazz)) {
                if (f.getName().equals(fieldName)) {
                    field = f;
                    break;
                }
            }
            if (field == null) {
                throw new GyjdbcException(
                        "cannot resolve field '" + fieldName + "' in " + clazz.getName() + " or its superclasses");
            }
            Column anno = field.getAnnotation(Column.class);
            if (anno != null) {
                return EntityTools.getColumnName(field);
            } else {
                return EntityTools.transferColumnName(fieldName);
            }
        } catch (ClassNotFoundException e) {
            throw new GyjdbcException(e);
        }
    }
}
