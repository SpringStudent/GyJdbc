package com.gysoft.jdbc.multi;

import com.gysoft.jdbc.bean.GyjdbcException;
import com.gysoft.jdbc.multi.balance.LoadBalance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 周宁
 */
public class JdbcRoutingDataSource extends AbstractRoutingDataSource {

    private final ConcurrentMap<Class<? extends LoadBalance>, LoadBalance> loadBalances =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> activeScopes = new ConcurrentHashMap<>();
    private volatile Map<String, List<String>> dataSourceKeysGroup = Collections.emptyMap();
    private volatile Set<String> targetDataSourceKeys = Collections.emptySet();
    private String defaultLookUpKey;

    public void setDataSourceKeysGroup(Map<String, String> groups) {
        if (groups == null || groups.isEmpty()) {
            dataSourceKeysGroup = Collections.emptyMap();
            return;
        }
        Map<String, List<String>> normalized = new HashMap<>();
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            String group = StringUtils.trimToNull(entry.getKey());
            if (group == null) {
                throw new GyjdbcException("Data source group name cannot be blank");
            }
            String configuredKeys = entry.getValue();
            if (StringUtils.isBlank(configuredKeys)) {
                throw new GyjdbcException("No data source configured for group " + group);
            }
            List<String> keys = new ArrayList<>();
            for (String configuredKey : configuredKeys.split(",", -1)) {
                String key = StringUtils.trimToNull(configuredKey);
                if (key == null) {
                    throw new GyjdbcException("Data source key in group " + group + " cannot be blank");
                }
                keys.add(key);
            }
            normalized.put(group, Collections.unmodifiableList(keys));
        }
        dataSourceKeysGroup = Collections.unmodifiableMap(normalized);
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        if (targetDataSources == null || targetDataSources.isEmpty()) {
            targetDataSourceKeys = Collections.emptySet();
            return;
        }
        Set<String> keys = new HashSet<>();
        for (Object key : targetDataSources.keySet()) {
            keys.add(String.valueOf(key));
        }
        targetDataSourceKeys = Collections.unmodifiableSet(keys);
    }

    public void setDefaultLookUpKey(String defaultLookUpKey) {
        this.defaultLookUpKey = defaultLookUpKey;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        validateConfiguredGroups();
        if (StringUtils.isNotBlank(defaultLookUpKey)) {
            validateTargetKey(defaultLookUpKey);
        }
    }

    @Override
    public Object determineCurrentLookupKey() {
        DataSourceBind dataSourceBind = DataSourceBindHolder.currentDataSource();
        if (dataSourceBind == null) {
            return defaultLookUpKey;
        }
        List<String> candidates = null;
        LoadBalance loadBalance = null;
        if (StringUtils.isNotEmpty(dataSourceBind.getGroup())) {
            candidates = dataSourceKeysGroup.get(dataSourceBind.getGroup());
            if (candidates == null || candidates.isEmpty()) {
                throw new GyjdbcException("Unknown data source group " + dataSourceBind.getGroup());
            }
            loadBalance = loadBalance(dataSourceBind.getLoadBalance());
        }
        String selectedKey = dataSourceBind.resolve(loadBalance, candidates, this::activeCount);
        validateTargetKey(selectedKey);
        if (dataSourceBind.registerActive(() -> decreaseActiveScope(selectedKey))) {
            activeScopes.compute(selectedKey, (key, count) -> count == null ? 1 : count + 1);
        }
        return selectedKey;
    }

    private LoadBalance loadBalance(Class<? extends LoadBalance> loadBalanceClass) {
        LoadBalance existing = loadBalances.get(loadBalanceClass);
        if (existing != null) {
            return existing;
        }
        LoadBalance created;
        try {
            Constructor<? extends LoadBalance> constructor = loadBalanceClass.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            created = constructor.newInstance();
        } catch (ReflectiveOperationException | SecurityException exception) {
            throw new GyjdbcException("Cannot create load balance strategy " + loadBalanceClass.getName(), exception);
        }
        LoadBalance previous = loadBalances.putIfAbsent(loadBalanceClass, created);
        return previous == null ? created : previous;
    }

    private int activeCount(String dataSourceKey) {
        Integer count = activeScopes.get(dataSourceKey);
        return count == null ? 0 : count;
    }

    private void decreaseActiveScope(String dataSourceKey) {
        activeScopes.computeIfPresent(dataSourceKey,
                (key, count) -> count <= 1 ? null : count - 1);
    }

    private void validateConfiguredGroups() {
        if (targetDataSourceKeys.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : dataSourceKeysGroup.entrySet()) {
            for (String key : entry.getValue()) {
                if (!targetDataSourceKeys.contains(key)) {
                    throw new GyjdbcException("Unknown data source key " + key + " in group " + entry.getKey());
                }
            }
        }
    }

    private void validateTargetKey(String dataSourceKey) {
        if (StringUtils.isBlank(dataSourceKey)) {
            throw new GyjdbcException("Resolved data source key cannot be blank");
        }
        if (!targetDataSourceKeys.isEmpty() && !targetDataSourceKeys.contains(dataSourceKey)) {
            throw new GyjdbcException("Unknown data source key " + dataSourceKey);
        }
    }
}
