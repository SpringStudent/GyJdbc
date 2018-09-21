package com.gysoft.jdbc.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 周宁
 * @date 2018/4/10 15:32
 */
@Data
public class Sort implements Serializable {

    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序类型(DESC降序，ASC升序)
     */
    private String sortType = "DESC";

    public Sort() {

    }

    public Sort(String sortField) {
        this.sortField = sortField;
    }

    public Sort(String sortField, String sortType) {
        this.sortField = sortField;
        this.sortType = sortType;
    }
}
