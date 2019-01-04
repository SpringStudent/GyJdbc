package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周宁
 */
public class Joins {

    private StringBuilder joinSql;

    private List<CriteriaProxy> criteriaProxys;

    public Joins() {
        this.joinSql = new StringBuilder();
        this.criteriaProxys = new ArrayList<>();
    }

    public With with(JoinType joinType, Class clss) {
        joinSql.append(" " + joinType.getType() + " " + EntityTools.getTableName(clss));
        return getWith();
    }

    public StringBuilder getJoinSql() {
        return joinSql;
    }

    public class With {
        public As as(String aliasName) {
            joinSql.append(" AS " + aliasName + " ");
            return getAs();
        }

    }

    public class As {
        public On on(String field, String field2) {
            joinSql.append(" ON " + field + " = " + field2 + " ");
            return getOn();
        }
    }

    public class On {
        public On on(String field, String field2) {
            joinSql.append(" AND " + field + " = " + field2 + " ");
            return this;
        }

        public On and(String key,String opt,Object value){
            CriteriaProxy criteriaProxy = new CriteriaProxy();
            Pair<String, Object[]> pair = SqlMakeTools.doCriteria(new Criteria().where(key, opt, value), new StringBuilder());
            criteriaProxy.setSql(new StringBuilder(pair.getFirst().replace("WHERE", "").trim()));
            criteriaProxy.setParams(pair.getSecond());
            criteriaProxy.setCriteriaType("AND");
            criteriaProxy.setWhereParamsIndex(-1);
            criteriaProxys.add(criteriaProxy);
            return this;
        }

        public StringBuilder getJoinSql() {
            return joinSql;
        }

        public List<CriteriaProxy> getCriteriaProxys(){
            return criteriaProxys;
        }

    }

    private With getWith() {
        return new With();
    }

    private As getAs() {
        return new As();
    }

    private On getOn() {
        return new On();
    }

}
