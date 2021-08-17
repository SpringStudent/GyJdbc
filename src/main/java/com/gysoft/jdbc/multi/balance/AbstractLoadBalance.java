package com.gysoft.jdbc.multi.balance;

import com.gysoft.jdbc.multi.DataSourceBind;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 负载均衡抽象策略实现类
 * @author 周宁
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    private static Map<String, String> dataSourceKeysGroup = new HashMap<>();

    @Override
    public String select(DataSourceBind dataSourceBind) {
        String group = dataSourceBind.getGroup();
        if (StringUtils.isEmpty(group)) {
            return null;
        }
        String keys = dataSourceKeysGroup.get(group);
        if (StringUtils.isNotEmpty(keys)) {
            List<String> list = Arrays.asList(keys.split(","));
            if (list.size() == 1) {
                return list.get(0);
            }
            return doSelect(list,group);
        }
        return null;
    }

    protected abstract String doSelect(List<String> keys,String group);

    public static void initDataSourceKeysGroup(Map<String, String> group) {
        dataSourceKeysGroup.putAll(group);
    }

}
