package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author 周宁
 */
public class Joins {

    private StringBuilder joinSql;

    private List<CriteriaProxy> criteriaProxys;

    public abstract class BaseJoin {

        public void setJoinType(JoinType joinType) {
            joinSql = new StringBuilder(String.format(joinSql.toString(), joinType.getType()));
        }

        public StringBuilder getJoinSql() {
            return new StringBuilder(joinSql.toString().replace("ON  AND", "ON"));
        }

        public List<CriteriaProxy> getCriteriaProxys() {
            return criteriaProxys;
        }
    }

    public class With extends BaseJoin {
        public As as(String aliasName) {
            if(!StringUtils.isEmpty(aliasName)){
                joinSql.append(" " + aliasName + " ");
            }
            return getAs();
        }

        public On on(String field, String field2) {
            joinSql.append(" ON " + field + " = " + field2 + " ");
            return getOn();
        }
    }

    public class As extends BaseJoin {
        public On on(String field, String field2) {
            joinSql.append(" ON " + field + " = " + field2 + " ");
            return getOn();
        }

        public <T, R, M, P> On on(TypeFunction<T, R> function, TypeFunction<M, P> function2) {
            return this.on(TypeFunction.getLambdaColumnName(function), TypeFunction.getLambdaColumnName(function2));
        }

        On on() {
            joinSql.append(" ON ");
            return getOn();
        }
    }

    public class On extends BaseJoin {
        public On on(String field, String field2) {
            joinSql.append(" AND " + field + " = " + field2 + " ");
            return this;
        }

        public <T, R, M, P> On on(TypeFunction<T, R> function, TypeFunction<M, P> function2) {
            return this.on(TypeFunction.getLambdaColumnName(function), TypeFunction.getLambdaColumnName(function2));
        }

        public <T, R> On and(TypeFunction<T, R> function, Object value) {
            return this.and(TypeFunction.getLambdaColumnName(function), "=", value);
        }

        public On and(String key, Object value) {
            return this.and(key, "=", value);
        }

        public <T, R> On and(TypeFunction<T, R> function, String opt, Object value) {
            return this.and(TypeFunction.getLambdaColumnName(function), opt, value);
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

        public On andIfAbsent(String key, Object value) {
            return this.andIfAbsent(key, "=", value);
        }

        public On andIfAbsent(String key, String opt, Object value) {
            if (AuxiliaryOperation.getDefaultPredicate().test(value)) {
                return and(key, opt, value);
            }
            return this;
        }

        public On andIfAbsent(String key, Object value, Predicate<Object> predicate) {
            return this.andIfAbsent(key, "=", value, predicate);
        }

        public On andIfAbsent(String key, String opt, Object value, Predicate<Object> predicate) {
            if (predicate.test(value)) {
                return and(key, opt, value);
            }
            return this;
        }
    }

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

    public With with(SQL sql) {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        joinSql.append(" %s " + "(" + pair.getFirst() + ")");
        CriteriaProxy criteriaProxy = new CriteriaProxy();
        criteriaProxy.setWhereParamsIndex(-1);
        criteriaProxy.setParams(pair.getSecond());
        criteriaProxy.setCriteriaType("WITH");
        criteriaProxys.add(criteriaProxy);
        return getWith();
    }

    public StringBuilder getJoinSql() {
        return joinSql;
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
