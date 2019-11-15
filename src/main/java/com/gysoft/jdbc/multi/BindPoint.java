package com.gysoft.jdbc.multi;

import java.lang.annotation.*;

/**
 * 数据源绑定注解
 * @author 周宁
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BindPoint {
    /**
     *
     * @return String 数据源key
     */
    String key() default "";

    /**
     * @return String 数据源组
     */
    String group() default "";

    /**
     * @return Class 负载均衡策略class
     */
    Class<? extends LoadBalance> loadBalance() default RoundbinLoadBalance.class;
}
