package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;
import com.gysoft.jdbc.tools.SqlMakeTools;

/**
 * @author 周宁
 * @Date 2019-01-04 15:51
 */
@Table(name = "tb_token")
public class Token {
    private Integer id;

    private Integer size;
    @Column(name = "ddd")
    private String tk;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getTk() {
        return tk;
    }

    public void setTk(String tk) {
        this.tk = tk;
    }
}
