package com.gysoft.jdbc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@author 周宁
 *@date 2018/8/28 13:45
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhereParam {

    private String key;
    private String opt;
    private Object value;

}