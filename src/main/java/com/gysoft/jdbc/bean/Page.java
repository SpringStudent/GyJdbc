package com.gysoft.jdbc.bean;


import java.io.Serializable;

/**
 * @author 周宁
 */
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
        // 当前页数小于 1 时按第一页处理
        if (currentPage < 1) {
            return 0;
        }
        // 每页记录数必须为正，否则分页无意义且会向下游 LIMIT 传入非法值
        if (pageSize < 1) {
            throw new GyjdbcException("pageSize must be positive, but got: " + pageSize);
        }
        // 用 long 计算偏移量，避免 int 乘法溢出为负数
        long offset = (long) (currentPage - 1) * pageSize;
        if (offset > Integer.MAX_VALUE) {
            throw new GyjdbcException("分页偏移量超出 int 范围: currentPage=" + currentPage + ", pageSize=" + pageSize);
        }
        return (int) offset;
    }

    public Page() {

    }

    public static Page newPage() {
        return new Page();
    }

    public static Page newPage(int currentPage, int pageSize) {
        return new Page(currentPage, pageSize);
    }

    public Page(int currentPage, int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
