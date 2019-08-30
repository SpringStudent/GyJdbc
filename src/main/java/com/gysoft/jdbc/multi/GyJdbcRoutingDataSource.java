package com.gysoft.jdbc.multi;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author 周宁
 */
public class GyJdbcRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceIdHolder.getDataSource();
    }
}
