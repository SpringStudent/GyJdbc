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
            throw new RuntimeException("keys cannot be null or []");
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
            throw new RuntimeException("sql error,condition \"or\" must be following after \"where\"!");
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

    public S andCriteria(Criteria criteria) {
        return criteria(criteria, "AND");
    }

    public S orCriteria(Criteria criteria) {
        if (CollectionUtils.isEmpty(whereParams)) {
            throw new RuntimeException("sql error,condition \"orCriteria\" must be following after \"where\"!");
        }
        return criteria(criteria, "OR");
    }

    private S criteria(Criteria criteria, String criteriaType) {
        if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
            throw new RuntimeException("unsupport doCriteria operate");
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
