package com.gysoft.jdbc.entity;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;

import java.time.LocalDateTime;

/**
 * 测试用实体：包含 static/transient 字段（应被排除）与父类继承字段（应被收集）。
 *
 * @author 周宁
 */
@Table(name = "tb_user_ext")
public class UserEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private transient String temp;

    private String name;

    private LocalDateTime createTime;

    @Column(name = "email_addr")
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
