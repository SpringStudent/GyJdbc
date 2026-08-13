package com.gysoft.jdbc.entity;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;

/**
 * 集成测试专用实体：验证带 @Column 的实体中 primitive 字段（int/boolean）
 * 遇到 NULL 列时，查询侧不抛异常、按默认值回读（0/false），与 BeanPropertyRowMapper 行为一致。
 *
 * @author 周宁
 */
@Table(name = "tb_primitive")
public class PrimitiveFieldEntity {

    private Integer id;

    @Column(name = "real_name")
    private String nickname;

    private int age;

    private boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
