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

    public With with(Class clss) {
        joinSql.append(" %s " + EntityTools.getTableName(clss));
        return getWith();
    }

    public With with(String tb) {
        joinSql.append(" %s " + tb);
        return getWith();
    }

    public StringBuilder getJoinSql() {
        return joinSql;
    }

    public class With extends BaseJoin {
        public As as(String aliasName) {
            joinSql.append(" AS " + aliasName + " ");
            return getAs();
        }
    }

    public class As extends BaseJoin {
        public On on(String field, String field2) {
            joinSql.append(" ON " + field + " = " + field2 + " ");
            return getOn();
        }
    }

    public class On extends BaseJoin {
        public On on(String field, String field2) {
            joinSql.append(" AND " + field + " = " + field2 + " ");
            return this;
        }

        public On and(String key, String opt, Object value) {
            CriteriaProxy criteriaProxy = new CriteriaProxy();
            Pair<String, Object[]> pair = SqlMakeTools.doCriteria(new Criteria().where(key, opt, value), new StringBuilder());
            criteriaProxy.setSql(new StringBuilder(pair.getFirst().replace("WHERE", "").trim()));
            criteriaProxy.setParams(pair.getSecond());
            criteriaProxy.setCriteriaType("AND");
            criteriaProxy.setWhereParamsIndex(-1);
            criteriaProxys.add(criteriaProxy);
            return this;
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

    public abstract class BaseJoin {

        public void setJoinType(JoinType joinType) {
            joinSql = new StringBuilder(String.format(joinSql.toString(), joinType.getType()));
        }

        public StringBuilder getJoinSql() {
            return joinSql;
        }

        public List<CriteriaProxy> getCriteriaProxys() {
            return criteriaProxys;
        }
    }
}
