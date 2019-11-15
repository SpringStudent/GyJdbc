package com.gysoft.jdbc.multi;

/**
 * 负载均衡策略接口
 *
 * @author 周宁
 */
public interface LoadBalance {
    /**
     * 选择一个数据源
     * @param dataSourceBind 数据源绑定参数
     * @return String 数据源的key
     */
    String select(DataSourceBind dataSourceBind);
}
