package com.gysoft.jdbc.multi;

import org.springframework.core.NamedThreadLocal;

/**
 * @author 周宁
 */
public abstract class DataSourceIdHolder{

    public static final String MASTER = "master";

    public static final String SLAVE = "slave";
    /**
     * 保存当前线程绑定的dataSourceId
     */
    private static ThreadLocal<String> dataSourceIdHolder = new NamedThreadLocal<>("DataSource-Id");

    public static void setDataSource(String ds){
        dataSourceIdHolder.set(ds);
    }

    public static String getDataSource(){
        return dataSourceIdHolder.get();
    }
}
