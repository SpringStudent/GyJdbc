package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public class ValueReference {

    private Object value;

    public static ValueReference newValueRef(Object value) {
        return new ValueReference(value);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ValueReference(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ValueReference(" + value + ")";
    }
}
