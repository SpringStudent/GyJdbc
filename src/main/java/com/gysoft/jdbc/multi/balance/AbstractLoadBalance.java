package com.gysoft.jdbc.multi.balance;

import com.gysoft.jdbc.multi.DataSourceBind;

import java.util.List;

/**
 * 负载均衡抽象策略实现类
 *
 * @author 周宁
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String select(DataSourceBind dataSourceBind) {
        List<String> keys = dataSourceBind.getCandidateKeys();
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        if (keys.size() == 1) {
            return keys.get(0);
        }
        return doSelect(keys, dataSourceBind.getGroup(), dataSourceBind);
    }

    protected String doSelect(List<String> keys, String group, DataSourceBind dataSourceBind) {
        return doSelect(keys, group);
    }

    protected abstract String doSelect(List<String> keys, String group);
}
