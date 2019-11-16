package com.gysoft.jdbc.multi;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 周宁
 */
public class DataSourceBind {

    private static final Map<Class, LoadBalance> LoadBalanceMap = new HashMap<>();

    static {
        LoadBalanceMap.put(RoundbinLoadBalance.class, RoundbinLoadBalance.getInstance());
        LoadBalanceMap.put(RandomLoadBalance.class, RandomLoadBalance.getInstance());
    }

    enum BindType {
        byMethod, byAnno;
    }

    private BindType bindType;

    private String key;

    private String group;

    private Class<? extends LoadBalance> loadBalance;

    private String select;

    private DataSourceBind prev;

    public DataSourceBind(String key, BindType bindType, String group, Class<? extends LoadBalance> loadBalance) {
        this.key = key;
        this.bindType = bindType;
        this.group = group;
        this.loadBalance = loadBalance;

    }


    public static DataSourceBind bindKey(String key) {
        return new DataSourceBind(key, BindType.byMethod, null, null);
    }

    public static DataSourceBind bindGroup(String group, Class<? extends LoadBalance> loadBalance) {
        return new DataSourceBind(null, BindType.byMethod, group, loadBalance);
    }

    public static DataSourceBind bindPoint(BindPoint bindPoint) {
        String key = bindPoint.key();
        String group = bindPoint.group();
        if (StringUtils.isNotEmpty(key)) {
            return new DataSourceBind(key, BindType.byAnno, null, null);
        } else if (StringUtils.isNotEmpty(group)) {
            return new DataSourceBind(null, BindType.byAnno, group, bindPoint.loadBalance());
        }
        return null;
    }

    public String select() {
        if(StringUtils.isNotEmpty(this.select)){
            return select;
        }
        if (StringUtils.isNotEmpty(key)) {
            this.select = key;
        } else if(StringUtils.isNotEmpty(group)){
            this.select = LoadBalanceMap.get(loadBalance).select(this);
        }
        return select;
    }

    public void setPrev(DataSourceBind prev) {
        if(this.getBindType().equals(BindType.byMethod)&&prev!=null&&prev.getBindType().equals(BindType.byAnno)){
            this.prev = prev;
        }
    }

    public DataSourceBind getPrev() {
        return prev;
    }

    public String getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }

    public Class<? extends LoadBalance> getLoadBalance() {
        return loadBalance;
    }

    public BindType getBindType() {
        return bindType;
    }

    @Override
    public String toString() {
        return "DataSourceBind{" +
                "bindType=" + bindType +
                ", key='" + key + '\'' +
                ", group='" + group + '\'' +
                ", loadBalance=" + loadBalance +
                '}';
    }
}
