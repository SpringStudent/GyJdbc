package com.gysoft.jdbc.entity;

import com.gysoft.jdbc.annotation.Column;
import com.gysoft.jdbc.annotation.Table;

/**
 * 集成测试专用实体：主键字段被 @Column 改名。
 * <p>@Table(pk="id") 声明主键为字段名 id，但 id 字段经 @Column 映射到列 user_id。
 * 用于验证 isPk 双重比对 + 主键列名推导（EntityTools.getPkColumnName）
 * 在 save/update/delete/queryOne/queryIds/saveOrUpdate 全链路生效：
 * 拼 SQL 一律使用列名 user_id，而非 @Table.pk 的字段名 id。</p>
 *
 * @author 周宁
 */
@Table(name = "tb_pk_mapped", pk = "id")
public class PkMappedEntity {

    @Column(name = "user_id")
    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
