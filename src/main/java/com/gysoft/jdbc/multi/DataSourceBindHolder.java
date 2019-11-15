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
        if (dataSourceBind.getBindType().equals(DataSourceBind.BindType.byMethod)) {
            clearDataSource();
            DataSourceBindHolder.set(dataSourceBind.getPrev());
        }
        return dataSourceBind.select();
    }
}
