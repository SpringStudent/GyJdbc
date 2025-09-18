package com.gysoft.jdbc.multi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 周宁
 */
public abstract class DataSourceBindHolder {
    private static ThreadLocal<DataSourceBind> DataSourceBindHolder = new ThreadLocal<>();
    private static Map<String, Integer> activeCountMap = new ConcurrentHashMap<>();

    public static void setDataSource(DataSourceBind dataSourceBind) {
        //上个线程设置的DataSourceBind
        dataSourceBind.setPrev(DataSourceBindHolder.get());
        DataSourceBindHolder.set(dataSourceBind);
    }

    public static void clearDataSource() {
        DataSourceBind dataSourceBind = DataSourceBindHolder.get();
        if (dataSourceBind != null) {
            decreaseActiveCount(dataSourceBind);
        }
        DataSourceBindHolder.remove();
    }

    public static String getDataSource() {
        DataSourceBind dataSourceBind = DataSourceBindHolder.get();
        if (dataSourceBind == null) {
            return null;
        }
        try {
            String result = dataSourceBind.select(true);
            increaseActiveCount(result);
            return result;
        } finally {
            //清理或者恢复上次的DataSourceBind
            //这么写通过entityDao.bindXxx()方法调用时就不会存在内存泄漏问题了
            if (dataSourceBind.getBindType().equals(DataSourceBind.BindType.byMethod)) {
                clearDataSource();
                if (dataSourceBind.getPrev() != null) {
                    DataSourceBindHolder.set(dataSourceBind.getPrev());
                }
            }
        }
    }

    private static void increaseActiveCount(String key) {
        if (key != null) {
            activeCountMap.compute(key, (k, v) -> v == null ? 1 : v + 1);
        }
    }

    private static void decreaseActiveCount(DataSourceBind dataSourceBind) {
        String key = dataSourceBind.select(false);
        if (key != null) {
            activeCountMap.computeIfPresent(key, (k, v) -> v - dataSourceBind.getActive());
        }
    }

    public static Integer getActiveCount(String dataSourceKey) {
        return activeCountMap.get(dataSourceKey);
    }
}
