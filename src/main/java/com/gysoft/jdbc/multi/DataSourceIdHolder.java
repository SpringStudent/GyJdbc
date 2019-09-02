package com.gysoft.jdbc.multi;

import org.springframework.core.NamedThreadLocal;

/**
 * @author 周宁
 */
public abstract class DataSourceIdHolder {
    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

    /**
     * 保存当前线程绑定的dataSourceId
     */
    private static ThreadLocal<String> dataSourceKeyHolderByMethod = new NamedThreadLocal<>("dataSourceKeyHolderByMethod");
    private static ThreadLocal<String> dataSourceKeyHolderByAnno = new NamedThreadLocal<>("dataSourceKeyHolderByAnno");

    public static void setDataSource(String bindKey, BindPointType bindPointType) {
        if (bindPointType.equals(BindPointType.ByMethod)) {
            dataSourceKeyHolderByMethod.set(bindKey);
        } else if (bindPointType.equals(BindPointType.ByAnno)) {
            dataSourceKeyHolderByAnno.set(bindKey);
        }
    }

    public static String getDataSource() {
        if (dataSourceKeyHolderByMethod.get() != null) {
            String bindKey = dataSourceKeyHolderByMethod.get();
            dataSourceKeyHolderByMethod.remove();
            return bindKey;
        } else if (dataSourceKeyHolderByAnno.get() != null) {
            String bindKey = dataSourceKeyHolderByAnno.get();
            return bindKey;
        } else {
            return null;
        }
    }

    public static void clearDataSource() {
        dataSourceKeyHolderByMethod.remove();
        dataSourceKeyHolderByAnno.remove();
    }
}
