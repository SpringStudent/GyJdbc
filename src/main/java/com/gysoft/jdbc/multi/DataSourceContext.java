package com.gysoft.jdbc.multi;

import com.gysoft.jdbc.bean.GyjdbcException;
import com.gysoft.jdbc.multi.balance.LoadBalance;

import java.util.function.Supplier;

/**
 * @author 周宁
 */
public final class DataSourceContext {

    private DataSourceContext() {
    }

    public static <T> T withDataSource(String key, Supplier<T> callback) {
        return execute(DataSourceBind.bindScopedKey(key), callback);
    }

    public static void withDataSource(String key, Runnable callback) {
        execute(DataSourceBind.bindScopedKey(key), runnable(callback));
    }

    public static <T> T withDataSourceGroup(String group, Class<? extends LoadBalance> loadBalance,
                                            Supplier<T> callback) {
        return execute(DataSourceBind.bindScopedGroup(group, loadBalance), callback);
    }

    public static void withDataSourceGroup(String group, Class<? extends LoadBalance> loadBalance,
                                           Runnable callback) {
        execute(DataSourceBind.bindScopedGroup(group, loadBalance), runnable(callback));
    }

    private static <T> T execute(DataSourceBind dataSourceBind, Supplier<T> callback) {
        if (callback == null) {
            throw new GyjdbcException("Data source callback cannot be null");
        }
        DataSourceBindHolder.pushDataSource(dataSourceBind);
        try {
            return callback.get();
        } finally {
            DataSourceBindHolder.popDataSource();
        }
    }

    private static Supplier<Void> runnable(Runnable callback) {
        if (callback == null) {
            throw new GyjdbcException("Data source callback cannot be null");
        }
        return () -> {
            callback.run();
            return null;
        };
    }
}
