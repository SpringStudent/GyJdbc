package com.gysoft.jdbc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author 周宁
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SqlParamMap {
    /**
     * sql语句
     */
    private String sql;
    /**
     * 参数map
     */
    private Map<String,Object> paramMap;
}
