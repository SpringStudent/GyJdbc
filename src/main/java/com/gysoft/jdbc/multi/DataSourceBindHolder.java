package com.gysoft.jdbc.multi;

import com.gysoft.jdbc.bean.GyjdbcException;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author 周宁
 */
public abstract class DataSourceBindHolder {

    private static final ThreadLocal<DataSourceBind> nextDataSource = new ThreadLocal<>();
    private static final ThreadLocal<Deque<DataSourceBind>> scopedDataSources = new ThreadLocal<>();

    public static void pushDataSource(DataSourceBind dataSourceBind) {
        if (dataSourceBind == null) {
            throw new GyjdbcException("Data source binding cannot be null");
        }
        if (DataSourceBind.BindType.byMethod.equals(dataSourceBind.getBindType())) {
            nextDataSource.set(dataSourceBind);
        } else {
            Deque<DataSourceBind> stack = scopedDataSources.get();
            if (stack == null) {
                stack = new ArrayDeque<>();
                scopedDataSources.set(stack);
            }
            stack.push(dataSourceBind);
        }
    }

    public static void popDataSource() {
        Deque<DataSourceBind> stack = scopedDataSources.get();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.pop().release();
        nextDataSource.remove();
        if (stack.isEmpty()) {
            scopedDataSources.remove();
        }
    }

    public static DataSourceBind currentDataSource() {
        DataSourceBind oneShot = nextDataSource.get();
        if (oneShot != null) {
            nextDataSource.remove();
            return oneShot;
        }
        Deque<DataSourceBind> stack = scopedDataSources.get();
        return stack == null || stack.isEmpty() ? null : stack.peek();
    }

    public static void clearDataSource() {
        nextDataSource.remove();
        Deque<DataSourceBind> stack = scopedDataSources.get();
        if (stack != null) {
            while (!stack.isEmpty()) {
                stack.pop().release();
            }
            scopedDataSources.remove();
        }
    }

}
