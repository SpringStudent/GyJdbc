package com.gysoft.jdbc.bean;

import java.io.Serializable;

/**
 * 包装两个对象关系
 *
 * @author 周宁
 * @date 2017/11/13 11:22
 */
public class Pair<F, S> implements Serializable {
    /**
     * 第一个属性(一般为key)
     */
    private F first;
    /**
     * 第二个属性(一般为value)
     */
    private S second;

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public Pair() {
    }

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}
