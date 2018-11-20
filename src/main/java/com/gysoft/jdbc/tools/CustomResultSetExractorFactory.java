package com.gysoft.jdbc.tools;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 周宁
 */
public class CustomResultSetExractorFactory {
    /**
     * 两列结果值得映射Mapper类
     */
    private static final ResultSetExtractor DOUBLE_COLUMN_VALUE_RESULTSETEXRACTOR =(rs) -> {
        Map<String,Object> result = new HashMap<>();
        while(rs.next()){
            result.put(rs.getString(1),rs.getObject(2));
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
