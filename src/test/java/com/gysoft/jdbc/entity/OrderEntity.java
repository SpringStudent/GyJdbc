package com.gysoft.jdbc.entity;

import com.gysoft.jdbc.annotation.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 集成测试第二张表实体（tb_order）。
 * <p>用于验证 EntityDao 中 SQL 入参方法（union、子查询、insertWithSql、createWithSql、drunk 等）。
 * 通过 memberId 关联 tb_member.id，orderNo 为 varchar，可与 tb_member.name 做 UNION。
 * 字段名全部为 camelCase 且未显式使用 @Column，与 tb_member 惯例一致：
 * 列名与属性名大小写不敏感匹配，BeanPropertyRowMapper 可双向读写。</p>
 *
 * @author 周宁
 */
@Table(name = "tb_order")
public class OrderEntity extends BaseEntity {

    /** 关联的会员主键（tb_member.id） */
    private Integer memberId;

    /** 订单号（varchar，用于 UNION 测试） */
    private String orderNo;

    /** 订单金额 */
    private BigDecimal amount;

    /** 订单状态：PAID / CANCELLED 等 */
    private String status;

    private LocalDateTime createTime;

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
