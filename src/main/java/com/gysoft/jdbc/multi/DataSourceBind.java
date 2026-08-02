package com.gysoft.jdbc.multi;

import com.gysoft.jdbc.bean.GyjdbcException;
import com.gysoft.jdbc.multi.balance.LoadBalance;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author 周宁
 */
public class DataSourceBind {

    enum BindType {
        byMethod, byAnno, byScope
    }

    private final BindType bindType;
    private final String key;
    private final String group;
    private final Class<? extends LoadBalance> loadBalance;
    private String selectedKey;
    private List<String> candidateKeys = Collections.emptyList();
    private ToIntFunction<String> activeCountProvider = ignored -> 0;
    private Runnable releaseAction;
    private boolean activeRegistered;
    private DataSourceBind prev;

    public DataSourceBind(String key, BindType bindType, String group,
                          Class<? extends LoadBalance> loadBalance) {
        this.key = key;
        this.bindType = bindType;
        this.group = group;
        this.loadBalance = loadBalance;
    }

    public static DataSourceBind bindKey(String key) {
        validateKey(key);
        return new DataSourceBind(key, BindType.byMethod, null, null);
    }

    public static DataSourceBind bindGroup(String group, Class<? extends LoadBalance> loadBalance) {
        validateGroup(group, loadBalance);
        return new DataSourceBind(null, BindType.byMethod, group, loadBalance);
    }

    static DataSourceBind bindScopedKey(String key) {
        validateKey(key);
        return new DataSourceBind(key, BindType.byScope, null, null);
    }

    static DataSourceBind bindScopedGroup(String group, Class<? extends LoadBalance> loadBalance) {
        validateGroup(group, loadBalance);
        return new DataSourceBind(null, BindType.byScope, group, loadBalance);
    }

    public static DataSourceBind bindPoint(BindPoint bindPoint) {
        if (bindPoint == null) {
            throw new GyjdbcException("BindPoint cannot be null");
        }
        String key = StringUtils.trimToNull(bindPoint.key());
        String group = StringUtils.trimToNull(bindPoint.group());
        if (key != null && group != null) {
            throw new GyjdbcException("BindPoint key and group cannot both be configured");
        }
        if (key != null) {
            return new DataSourceBind(key, BindType.byAnno, null, null);
        }
        if (group != null) {
            validateGroup(group, bindPoint.loadBalance());
            return new DataSourceBind(null, BindType.byAnno, group, bindPoint.loadBalance());
        }
        throw new GyjdbcException("BindPoint must configure either key or group");
    }

    public String resolve(LoadBalance strategy, List<String> candidates,
                   ToIntFunction<String> activeCountProvider) {
        if (StringUtils.isNotEmpty(selectedKey)) {
            return selectedKey;
        }
        if (StringUtils.isNotEmpty(key)) {
            selectedKey = key;
            return selectedKey;
        }
        if (strategy == null) {
            throw new GyjdbcException("No load balance strategy configured for group " + group);
        }
        if (candidates == null || candidates.isEmpty()) {
            throw new GyjdbcException("No data source configured for group " + group);
        }
        this.candidateKeys = Collections.unmodifiableList(new ArrayList<>(candidates));
        this.activeCountProvider = activeCountProvider;
        selectedKey = strategy.select(this);
        if (StringUtils.isBlank(selectedKey) || !candidateKeys.contains(selectedKey)) {
            throw new GyjdbcException("Load balance strategy selected an invalid data source for group " + group);
        }
        return selectedKey;
    }

    synchronized boolean registerActive(Runnable releaseAction) {
        if (bindType != BindType.byMethod && !activeRegistered) {
            this.releaseAction = releaseAction;
            this.activeRegistered = true;
            return true;
        }
        return false;
    }

    synchronized void release() {
        if (activeRegistered) {
            activeRegistered = false;
            Runnable action = releaseAction;
            releaseAction = null;
            action.run();
        }
    }

    public String select(boolean updateActive) {
        return StringUtils.isNotEmpty(selectedKey) ? selectedKey : key;
    }

    public void setPrev(DataSourceBind prev) {
        this.prev = prev;
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

    BindType getBindType() {
        return bindType;
    }

    public int getActive() {
        return activeRegistered ? 1 : 0;
    }

    public List<String> getCandidateKeys() {
        return candidateKeys;
    }

    public int getActiveCount(String dataSourceKey) {
        return activeCountProvider.applyAsInt(dataSourceKey);
    }

    private static void validateKey(String key) {
        if (StringUtils.isBlank(key)) {
            throw new GyjdbcException("Data source key cannot be blank");
        }
    }

    private static void validateGroup(String group, Class<? extends LoadBalance> loadBalance) {
        if (StringUtils.isBlank(group)) {
            throw new GyjdbcException("Data source group cannot be blank");
        }
        if (loadBalance == null) {
            throw new GyjdbcException("Load balance strategy cannot be null");
        }
    }

    @Override
    public String toString() {
        return "DataSourceBind{" +
                "bindType=" + bindType +
                ", key='" + key + '\'' +
                ", group='" + group + '\'' +
                ", loadBalance=" + loadBalance +
                ", selectedKey='" + selectedKey + '\'' +
                '}';
    }
}
