package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 周宁
 */
public abstract class AbstractCriteria<S extends AbstractCriteria<S>> implements AuxiliaryOperation<S> {
    /**
     * 条件入参
     */
    private List<WhereParam> whereParams;
    /**
     * 排序入参
     */
    private Set<Sort> sorts;
    /**
     * 代理更复杂的sql拼接
     */
    private List<CriteriaProxy> criteriaProxys;
    /**
     * 分组字段
     */
    private Set<String> groupFields;
    /**
     * having子句
     */
    private Pair<String, Object[]> having;

    /**
     * 偏移量
     */
    private int offset = -1;
    /**
     * 大小
     */
    private int size;

    private Where where;

    public Set<String> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(Set<String> groupFields) {
        this.groupFields = groupFields;
    }

    public List<WhereParam> getWhereParams() {
        return whereParams;
    }

    public void setWhereParams(List<WhereParam> whereParams) {
        this.whereParams = whereParams;
    }

    public Set<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(Set<Sort> sorts) {
        this.sorts = sorts;
    }

    public List<CriteriaProxy> getCriteriaProxys() {
        return criteriaProxys;
    }

    public void setCriteriaProxys(List<CriteriaProxy> criteriaProxys) {
        this.criteriaProxys = criteriaProxys;
    }

    public Pair<String, Object[]> getHaving() {
        return having;
    }

    public void setHaving(Pair<String, Object[]> having) {
        this.having = having;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public AbstractCriteria() {
        whereParams = new ArrayList<>();
        sorts = new LinkedHashSet<>();
        criteriaProxys = new ArrayList<>();
        groupFields = new LinkedHashSet<>();
    }

    public S where(String key, Object value) {
        return this.where(key, "=", value);
    }

    public S where(String[] keys, Object value) {
        return this.where(keys, "=", value);
    }

    public S where(String[] keys, String opt, Object value) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("keys cannot be null or []");
        }
        if (keys.length == 1) {
            return this.where(keys[0], opt, value);
        } else {
            StringBuilder columnsAppender = new StringBuilder();
            columnsAppender.append("(");
            columnsAppender.append(StringUtils.join(keys, ","));
            columnsAppender.append(")");
            return this.where(columnsAppender.toString(), opt, value);
        }
    }

    public <T, R> S where(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "=", value);
    }

    public S where(String key, String opt, Object value) {
        this.whereParams.add(new WhereParam(key, opt, value));
        return self();
    }

    private S self() {
        return (S) this;
    }

    public <T, R> S where(TypeFunction<T, R> function, String opt, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), opt, value);
    }

    @Override
    public S like(String key, Object value) {
        return this.where(key, "LIKE", "%" + value + "%");
    }

    @Override
    public <T, R> S like(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "LIKE", "%" + value + "%");
    }

    @Override
    public S gt(String key, Object value) {
        return this.where(key, ">", value);
    }

    @Override
    public <T, R> S gt(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), ">", value);
    }

    @Override
    public S gte(String key, Object value) {
        return this.where(key, ">=", value);
    }

    @Override
    public <T, R> S gte(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), ">=", value);
    }

    @Override
    public S lt(String key, Object value) {
        return this.where(key, "<", value);
    }

    @Override
    public <T, R> S lt(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "<", value);
    }

    @Override
    public S let(String key, Object value) {
        return this.where(key, "<=", value);
    }

    @Override
    public <T, R> S let(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "<=", value);
    }

    @Override
    public S doNothing() {
        return self();
    }

    public S notEqual(String key, Object value) {
        return this.where(key, "<>", value);
    }

    public <T, R> S notEqual(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), "<>", value);
    }

    public S isNull(String key) {
        return this.where(key, "IS", "NULL");
    }

    public <T, R> S isNull(TypeFunction<T, R> function) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IS", "NULL");
    }

    public S isNotNull(String key) {
        return this.where(key, "IS", "NOT NULL");
    }

    public <T, R> S isNotNull(TypeFunction<T, R> function) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IS", "NOT NULL");
    }

    @Override
    public S and(String key, Object value) {
        return this.where(key, value);
    }

    @Override
    public <T, R> S and(TypeFunction<T, R> function, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), value);
    }

    public S and(String key, String opt, Object value) {
        return this.where(key, opt, value);
    }

    public <T, R> S and(TypeFunction<T, R> function, String opt, Object value) {
        return this.where(TypeFunction.getLambdaColumnName(function), opt, value);
    }

    @Override
    public S or(String key, Object value) {
        return this.or(key, "=", value);
    }

    @Override
    public <T, R> S or(TypeFunction<T, R> function, Object value) {
        return this.or(TypeFunction.getLambdaColumnName(function), "=", value);
    }

    @Override
    public S orLike(String key, Object value) {
        return this.or(key, "like", "%" + value + "%");
    }

    @Override
    public <T, R> S orLike(TypeFunction<T, R> function, Object value) {
        return this.or(TypeFunction.getLambdaColumnName(function), "like", "%" + value + "%");
    }

    public S or(String key, String opt, Object value) {
        if (CollectionUtils.isEmpty(this.whereParams)) {
            throw new IllegalArgumentException("sql error,condition \"or\" must be following after \"where\"!");
        }
        return this.where(" OR " + key, opt, value);
    }

    public S exists(SQL sql) {
        return this.where("EXISTS", "", sql);
    }

    public S notExists(SQL sql) {
        return this.where("NOT EXISTS", "", sql);
    }

    public <T, R> S or(TypeFunction<T, R> function, String opt, Object value) {
        return this.or(TypeFunction.getLambdaColumnName(function), opt, value);
    }

    public S orBetweenAnd(String key, Object v1, Object v2) {
        return this.or(key, "BETWEEN ? AND ?", new Pair<>(v1, v2));
    }

    public <T, R> S orBetweenAnd(TypeFunction<T, R> function, Object v1, Object v2) {
        return this.or(TypeFunction.getLambdaColumnName(function), "BETWEEN ? AND ?", new Pair<>(v1, v2));
    }

    public S in(String key, SQL sql) {
        return this.where(key, "IN", sql);
    }

    public <T, R> S in(TypeFunction<T, R> function, SQL sql) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IN", sql);
    }

    @Override
    public S in(String key, Collection<?> args) {
        return this.where(key, "IN", args);
    }

    @Override
    public <T, R> S in(TypeFunction<T, R> function, Collection<?> args) {
        return this.where(TypeFunction.getLambdaColumnName(function), "IN", args);
    }

    @Override
    public S notIn(String key, Collection<?> args) {
        return this.where(key, "NOT IN", args);
    }

    @Override
    public <T, R> S notIn(TypeFunction<T, R> function, Collection<?> args) {
        return this.where(TypeFunction.getLambdaColumnName(function), "NOT IN", args);
    }

    public S notIn(String key, SQL sql) {
        return this.where(key, "NOT IN", sql);
    }

    public <T, R> S notIn(TypeFunction<T, R> function, SQL sql) {
        return this.where(TypeFunction.getLambdaColumnName(function), "NOT IN", sql);
    }

    public S betweenAnd(String key, Object v1, Object v2) {
        return this.where(key, "BETWEEN ? AND ?", new Pair<>(v1, v2));
    }

    public <T, R> S betweenAnd(TypeFunction<T, R> function, Object v1, Object v2) {
        return this.where(TypeFunction.getLambdaColumnName(function), "BETWEEN ? AND ?", new Pair<>(v1, v2));
    }

    public S findInSet(String key, Object value) {
        return this.where("FIND_IN_SET(?," + key + ")", "FIND IN SET", value);
    }

    public <T, R> S findInSet(TypeFunction<T, R> function, Object value) {
        return this.findInSet(TypeFunction.getLambdaColumnName(function), value);
    }

    public S orFindInSet(String key, Object value) {
        return this.or("FIND_IN_SET(?," + key + ")", "FIND IN SET", value);
    }

    public <T, R> S orFindInSet(TypeFunction<T, R> function, Object value) {
        return this.orFindInSet(TypeFunction.getLambdaColumnName(function), value);
    }

    public S andCriteria(Criteria criteria) {
        return criteria(criteria, "AND");
    }

    public S orCriteria(Criteria criteria) {
        if (CollectionUtils.isEmpty(whereParams)) {
            throw new IllegalArgumentException("sql error,condition \"orCriteria\" must be following after \"where\"!");
        }
        return criteria(criteria, "OR");
    }

    public S and(Where where) {
        return criteria(where.getCriteria(), "WHEREAND");
    }

    public S or(Where where) {
        return criteria(where.getCriteria(), "WHEREOR");
    }

    public S and(Opt opt, List<WhereParam> whereParams) {
        return and(buildWhere(opt, whereParams.toArray(new WhereParam[whereParams.size()])));
    }

    public S and(Opt opt, WhereParam... whereParams) {
        return and(buildWhere(opt, whereParams));
    }

    public S or(Opt opt, List<WhereParam> whereParams) {
        return or(buildWhere(opt, whereParams.toArray(new WhereParam[whereParams.size()])));
    }

    public S or(Opt opt, WhereParam... whereParams) {
        return or(buildWhere(opt, whereParams));
    }

    public S andWhere(Opt opt, List<WhereParam> whereParams) {
        return andCriteria(buildWhere(opt, whereParams.toArray(new WhereParam[whereParams.size()])).getCriteria());
    }

    public S andWhere(Opt opt, WhereParam... whereParams) {
        return andCriteria(buildWhere(opt, whereParams).getCriteria());
    }

    public S orWhere(Opt opt, List<WhereParam> whereParams) {
        return orCriteria(buildWhere(opt, whereParams.toArray(new WhereParam[whereParams.size()])).getCriteria());
    }

    public S orWhere(Opt opt, WhereParam... whereParams) {
        return orCriteria(buildWhere(opt, whereParams).getCriteria());
    }

    public Where buildWhere(Opt opt, WhereParam... whereParams) {
        WhereParam first = whereParams[0];
        Where where = new Where(first.getKey());
        where = whereParam(first, where);
        for (int i = 1; i < whereParams.length; i++) {
            WhereParam wp = whereParams[i];
            if (opt.equals(Opt.AND)) {
                where = where.and(wp.getKey());
            } else {
                where = where.or(wp.getKey());
            }
            where = whereParam(wp, where);
        }
        return where;
    }

    private Where whereParam(WhereParam wp, Where where) {
        if (wp.getOptEnum().equals(WhereParam.OptEnum.Equal)) {
            where = where.equal(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.BetweenAnd)) {
            Pair pair = (Pair) wp.getValue();
            where = where.betweenAnd(pair.getFirst(), pair.getSecond());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.NotEqual)) {
            where = where.notEqual(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.Gt)) {
            where = where.gt(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.Gte)) {
            where = where.gte(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.Lt)) {
            where = where.lt(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.Let)) {
            where = where.let(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.IsNull)) {
            where = where.isNull();
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.IsNotNull)) {
            where = where.isNotNull();
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.Exists)) {
            where = where.exists((SQL) wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.NotExists)) {
            where = where.notExists((SQL) wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.Like)) {
            where = where.like(wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.In)) {
            where = where.in((Collection<?>) wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.NotIn)) {
            where = where.notIn((Collection<?>) wp.getValue());
        } else if (wp.getOptEnum().equals(WhereParam.OptEnum.FindInSet)) {
            where = where.findInSet(wp.getValue());
        }
        return where;
    }

    private S criteria(Criteria criteria, String criteriaType) {
        if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
            throw new IllegalArgumentException("unsupport doCriteria operate");
        }
        //如果子查询中的子查询条件为空直接返回
        if (CollectionUtils.isEmpty(criteria.getWhereParams())) {
            return self();
        }
        if (CollectionUtils.isEmpty(whereParams)) {
            whereParams.add(new WhereParam());
        }
        CriteriaProxy criteriaProxy = new CriteriaProxy();
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder());
        criteriaProxy.setWhereParamsIndex(whereParams.size() + 1);
        criteriaProxy.setSql(new StringBuilder(pair.getFirst().replace("WHERE", "").trim()));
        criteriaProxy.setParams(pair.getSecond());
        criteriaProxy.setCriteriaType(criteriaType);
        criteriaProxys.add(criteriaProxy);
        return self();
    }

    public S groupBy(String... fields) {
        groupFields.addAll(Arrays.asList(fields));
        return self();
    }

    public <T, R> S groupBy(TypeFunction<T, R>... functions) {
        groupFields.addAll(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()));
        return self();
    }

    public S having(String funcField, String opt, Object value) {
        return having(new Criteria().where(funcField, opt, value));
    }

    public S having(Criteria criteria) {
        having = SqlMakeTools.doCriteria(criteria, new StringBuilder());
        having.setFirst(having.getFirst().replace("WHERE ", ""));
        return self();
    }

    public S orderBy(Sort... sort) {
        sorts.addAll(Arrays.asList(sort));
        return self();
    }

    public S limit(int offset, int size) {
        this.offset = offset;
        this.size = size;
        return self();
    }

    public S limit(int offset) {
        this.offset = offset;
        return self();
    }

}
