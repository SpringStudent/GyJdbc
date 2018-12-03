package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public interface ISqlParamMapProvider {

    /**
     * 获取sql和paramMap对象
     * @return SqlParamMap sql与其参数map包装对象
     */
    SqlParamMap getSqlParamMap();
}
