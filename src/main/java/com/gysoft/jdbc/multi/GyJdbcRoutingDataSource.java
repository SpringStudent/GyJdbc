package com.gysoft.jdbc.multi;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author 周宁
 */
public class GyJdbcRoutingDataSource extends AbstractRoutingDataSource {
    /**
     * 默认的数据源key;如果您的dao没有调用任何bind***()方法
     * 或者在对应的方法上使用了@BindPoint("slave")注解
     * 则该值会很有用,他会帮我们应用一个全局的数据源key
     */
    private String defaultLookUpKey;

    public void setDefaultLookUpKey(String defaultLookUpKey) {
        this.defaultLookUpKey = defaultLookUpKey;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String lookupkey = DataSourceIdHolder.getDataSource();
        if (lookupkey == null) {
            lookupkey = defaultLookUpKey;
        }
        return lookupkey;
    }


}
