package com.gysoft.jdbc.bean;

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
     * @param lambda lamda表达式
     * @return String 列名称
     */
    static String getLambdaColumnName(Serializable lambda) {
        try {
            Method method = lambda.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(lambda);
            String getter = serializedLambda.getImplMethodName();
            String fieldName = Introspector.decapitalize(getter.replace("get", ""));

            // 通过字段名获取字段
            Field field =
                    Class.forName(serializedLambda.getImplClass().replace("/", "."))
                            .getDeclaredField(fieldName);

            // 获取字段上的Column注解
            return EntityTools.getColumnName(field);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
