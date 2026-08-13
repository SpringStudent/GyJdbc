package com.gysoft.jdbc.entity;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;

/**
 * 集成测试专用实体：验证 @Column(name) 与属性名完全不一致时，查询侧仍能正确回读。
 * <p>属性 nickname 映射列 real_name，二者无法经驼峰/下划线互转对上，
 * 用于实证「@Column 仅在写侧生效、查询侧读回 null」缺陷的修复。</p>
 *
 * @author 周宁
 */
@Table(name = "tb_column_mapped")
public class ColumnMappedEntity {

    private Integer id;

    @Column(name = "real_name")
    private String nickname;

    private Integer age;

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
