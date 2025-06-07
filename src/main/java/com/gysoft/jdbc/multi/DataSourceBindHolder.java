package com.gysoft.jdbc.multi;

/**
 * @author 周宁
 */
public abstract class DataSourceBindHolder {
    private static ThreadLocal<DataSourceBind> DataSourceBindHolder = new ThreadLocal<>();

    public static void setDataSource(DataSourceBind dataSourceBind) {
        //上个线程设置的DataSourceBind
        dataSourceBind.setPrev(DataSourceBindHolder.get());
        DataSourceBindHolder.set(dataSourceBind);
    }

    public static void clearDataSource() {
        DataSourceBindHolder.remove();
    }

    public static String getDataSource() {
        DataSourceBind dataSourceBind = DataSourceBindHolder.get();
        if (dataSourceBind == null) {
            return null;
        }
        try {
            return dataSourceBind.select();
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
}
