package com.gysoft.jdbc.multi;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

/**
 * @author 周宁
 */
public class JdbcRoutingDataSource extends AbstractRoutingDataSource {
    /**
     * 默认的数据源key;如果你的dao没有调用任何bind***()方法
     * 或者在对应的方法上没有使用类似@BindPoint("slave")注解
     * 则该值会很有用,他会帮我们应用一个全局的数据源key
     */
    private String defaultLookUpKey;

    public void setDataSourceKeysGroup(Map<String, String> dataSourceKeysGroup) {
        AbstractLoadBalance.initDataSourceKeysGroup(dataSourceKeysGroup);
    }

    public void setDefaultLookUpKey(String defaultLookUpKey) {
        this.defaultLookUpKey = defaultLookUpKey;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String lookUpKey = DataSourceBindHolder.getDataSource();
        if(StringUtils.isEmpty(lookUpKey)){
            lookUpKey = defaultLookUpKey;
        }
        return lookUpKey;
    }

}
