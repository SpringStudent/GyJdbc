package com.gysoft.jdbc.entity;

/**
 * 自定义 RowMapper 测试用的投影实体，映射 tb_member 的 name / age 两列。
 *
 * @author 周宁
 */
public class MemberNameAgeDto {

    private String name;

    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
