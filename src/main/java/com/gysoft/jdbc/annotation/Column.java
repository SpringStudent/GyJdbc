package com.gysoft.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 字段名及相关属性信息
 * @author 彭佳佳
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Column {

    /**
     * 字段名称
     * @return String
     */
    String name() default "";

    /**
     * 是否唯一
     * @return boolean
     */
    boolean unique() default false;

    /**
     * 是否允许为空
     * @return boolean
     */
    boolean nullable() default true;

    /**
     * 长度
     * @return int
     */
    int length() default 255;

}