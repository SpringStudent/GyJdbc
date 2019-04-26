package com.gysoft.jdbc.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周宁
 */
public class SQLPiepline {

    private List<SQLNext> sqlNexts = new ArrayList<>();

    public SQLPiepline(SQL head) {
        sqlNexts.add(new SQLNext(head,null));
    }

    public boolean pielineSql(){
        return sqlNexts.size()>1;
    }

    public void add(SQL sql, String type){
        sqlNexts.add(new SQLNext(sql,type));
    }

    public List<SQLNext> getSqlNexts() {
        return sqlNexts;
    }

    public static class SQLNext{

        private SQL sql;

        private String unionType;

        public SQL getSql() {
            return sql;
        }

        public SQLNext(SQL sql, String unionType) {
            this.sql = sql;
            this.unionType = unionType;
        }

        public void setSql(SQL sql) {
            this.sql = sql;
        }

        public String getUnionType() {
            return unionType;
        }

        public void setUnionType(String unionType) {
            this.unionType = unionType;
        }
    }
}
