package com.gysoft.jdbc.entity;

import java.util.Date;

/**
 * 测试用父类实体，验证 EntityTools 对继承字段的收集。
 *
 * @author 周宁
 */
public class BaseEntity {

    private Integer id;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
