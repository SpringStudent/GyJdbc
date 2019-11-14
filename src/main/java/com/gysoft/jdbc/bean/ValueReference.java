package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public class ValueReference {

    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ValueReference(Object value) {
        this.value = value;
    }

}
