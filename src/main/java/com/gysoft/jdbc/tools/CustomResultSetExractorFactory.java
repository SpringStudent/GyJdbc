package com.gysoft.jdbc.tools;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 周宁
 */
public class CustomResultSetExractorFactory {
    /**
     * 两列结果值得映射Mapper类
     */
    private static final ResultSetExtractor DOUBLE_COLUMN_VALUE_RESULTSETEXRACTOR =(rs) -> {
        Map<Object,Object> result = new LinkedHashMap<>();
        while(rs.next()){
            result.put(rs.getObject(1),rs.getObject(2));
        }
        return result;
    };


    /**
     * 创建两列结果值得Map集合的RowMapper
     * @return ResultSetExtractor 两列结果值得映射抽取器
     */
    public static ResultSetExtractor createDoubleColumnValueResultSetExractor(){
        return DOUBLE_COLUMN_VALUE_RESULTSETEXRACTOR;
    }

}
