package com.gysoft.jdbc.bean;

/**
 * @author 周宁
 */
public class TableEnum {

    public enum Engine {
        InnoDB, MyISAM, MEMORY;
    }

    public enum RowFormat {
        DYNAMIC, COMPRESSED, FIXED;
    }
}
