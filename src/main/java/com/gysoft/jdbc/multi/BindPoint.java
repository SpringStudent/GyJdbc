package com.gysoft.jdbc.multi;

import java.lang.annotation.*;

/**
 * 方法级别的数据源绑定注解
 *
 * @author 周宁
 * @Date 2019-09-02 8:31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BindPoint {
    /**
     * dataSource的key
     * @return String
     */
    String value();
}
