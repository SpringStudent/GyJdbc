package com.gysoft.jdbc.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 周宁
 * @date 2018/4/10 15:29
 */
@Data
public class Page implements Serializable {
    /**
     * 当前页
     */
    private int currentPage;
    /**
     * 每页记录数
     */
    private int pageSize;

    public int getOffset() {
        //当前页数小于0
        if (currentPage < 1) {
            return 0;
        }
        return (this.currentPage - 1) * this.pageSize;
    }

    public Page() {

    }

    public Page(int currentPage, int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
}
