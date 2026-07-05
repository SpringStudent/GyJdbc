package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.tools.EntityTools;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
            // 通过字段名获取字段
            Field field =
                    Class.forName(serializedLambda.getImplClass().replace("/", "."))
                            .getDeclaredField(fieldName);

            Column anno = field.getAnnotation(Column.class);
            if (anno != null) {
                return EntityTools.getColumnName(field);
            } else {
                return EntityTools.transferColumnName(fieldName);
            }
        } catch (ReflectiveOperationException e) {
            throw new GyjdbcException(e);
        }
    }
}
