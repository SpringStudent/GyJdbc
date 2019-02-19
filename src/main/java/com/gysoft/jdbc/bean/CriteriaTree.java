package com.gysoft.jdbc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询条件节点抽象
 * @author 周宁
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriteriaTree {
    /**
     * 该criteria节点对应的sql
     */
    private String sql;
    /**
     * 该criteria节点对应的参数
     */
    private Object[] params;
    /**
     * 孩子查询条件节点
     */
    private List<CriteriaTree> childCriteriaTree;
    /**
     * 树节点的id，树的深度*树在当前节点编号
     */
    private String id;

}
