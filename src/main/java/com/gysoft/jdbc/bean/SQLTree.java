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
public class SQLTree {
    /**
     * 该criteria节点对应的sql
     */
    private String sql;
    /**
     * 该criteria节点对应的参数
     */
    private Object[] params;
    /**
     * 子sql
     */
    private List<SQLTree> childs;
    /**
     * 树节点的id
     */
    private String id;

}
