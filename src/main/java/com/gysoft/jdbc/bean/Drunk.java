package com.gysoft.jdbc.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * Drunk形容一个人喝醉了，犯糊涂了然后清楚表数据或者删除表
 * @author 周宁
 */
public class Drunk {
    /**
     * 表名称集合
     */
    private Set<String> tables;
    /**
     * 是否管exists
     */
    private boolean ifExists;

    public Drunk(){
        tables = new HashSet<>();
    }

    public Set<String> getTables() {
        return tables;
    }

    public void setTables(Set<String> tables) {
        this.tables = tables;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }
}
