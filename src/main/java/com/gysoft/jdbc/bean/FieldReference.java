package com.gysoft.jdbc.bean;

/**
 * 代表字段的引用
 * @author 周宁
 */
public class FieldReference {
    /**
     * 字段
     */
    private String field;

    public FieldReference(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

}
