package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;

/**
 * @author 周宁
 * @Date 2019-01-04 11:18
 */
@Table(name = "tb_role")
public class Role {

    private String name;

    private String auths;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuths() {
        return auths;
    }

    public void setAuths(String auths) {
        this.auths = auths;
    }
}
