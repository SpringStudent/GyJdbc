package com.gysoft.jdbc.entity;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 集成测试专用实体。
 * <p>属性名与列名满足 BeanPropertyRowMapper 的驼峰/下划线互转规则（无需 @Column 也能双向匹配），
 * 其中 emailAddr 通过 @Column 显式映射列名 email_addr，用于验证 save 侧注解生效、
 * 查询侧按属性名规则同样能匹配回 emailAddr 属性。</p>
 * <p>覆盖场景：父类继承字段（id/updateTime）、static/transient 字段过滤、
 * java.time 类型、数值/布尔/高精度小数类型。</p>
 *
 * @author 周宁
 */
@Table(name = "tb_member")
public class MemberEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private transient String temp;

    private String name;

    private LocalDateTime createTime;

    @Column(name = "email_addr")
    private String emailAddr;

    private Integer age;

    private Boolean active;

    private BigDecimal score;

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

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
