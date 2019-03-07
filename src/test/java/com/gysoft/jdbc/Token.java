package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;
import lombok.Data;

/**
 * @author 周宁
 * @Date 2019-01-04 15:51
 */
@Table(name = "tb_token")
@Data
public class Token {

    private Integer id;

    private Integer size;

    private String tk;
}
