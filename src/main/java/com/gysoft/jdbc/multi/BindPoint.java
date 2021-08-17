package com.gysoft.jdbc.multi;

import com.gysoft.jdbc.multi.balance.LoadBalance;
import com.gysoft.jdbc.multi.balance.RoundRobinLoadBalance;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 数据源绑定注解
 *
 * @author 周宁
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BindPointAspectRegistar.class)
public @interface BindPoint {
    /**
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
    Class<? extends LoadBalance> loadBalance() default RoundRobinLoadBalance.class;
}
