package com.gysoft.jdbc.tools;


import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

import static com.gysoft.jdbc.dao.EntityDao.*;

/**
 * @author 周宁
 */
public class SqlMakeTools {

    /**
     * 组装SQL
     *
     * @param clazz   类型
     * @param tbName  表名
     * @param sqlFlag sql标识
     * @param <E>     泛型
     * @return String 创建的sql
     */
    public static <E> String makeSql(Class clazz, String tbName, String sqlFlag) {
        StringBuffer sql = new StringBuffer();
        Field[] fields = clazz.getDeclaredFields();
        if (sqlFlag.equals(SQL_INSERT)) {
            sql.append(" INSERT INTO " + tbName);
            sql.append("(");
            for (int i = 0; fields != null && i < fields.length; i++) {
                fields[i].setAccessible(true); // 暴力反射
                String column = EntityTools.getColumnName(fields[i]);//获取属性对应字段名，没有注解默认按照属性名。有Column注解，获取Column的name作为字段名
                sql.append(column).append(",");
            }
            sql = sql.deleteCharAt(sql.length() - 1);
            sql.append(") VALUES (");
            for (int i = 0; fields != null && i < fields.length; i++) {
                sql.append("?,");
            }
            sql = sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
        } else if (sqlFlag.equals(SQL_UPDATE)) {
            String primaryKey = "id";
            sql.append(" UPDATE " + tbName + " SET ");
            for (int i = 0; fields != null && i < fields.length; i++) {
                fields[i].setAccessible(true); // 暴力反射
                String column = EntityTools.getColumnName(fields[i]);//获取属性对应字段名，没有注解默认按照属性名。有Column注解，获取Column的name作为字段名
                if (EntityTools.isPk(clazz, fields[i])) { // id 代表主键
                    primaryKey = column;
                    continue;
                }
                sql.append(column).append("=").append("?,");
            }
            sql = sql.deleteCharAt(sql.length() - 1);
            sql.append(" WHERE " + primaryKey + " = ?");
        } else if (sqlFlag.equals(SQL_DELETE)) {
            String primaryKey = "id";
            for (int i = 0; fields != null && i < fields.length; i++) {
                fields[i].setAccessible(true); // 暴力反射
                String column = EntityTools.getColumnName(fields[i]);//获取属性对应字段名，没有注解默认按照属性名。有Column注解，获取Column的name作为字段名
                if (EntityTools.isPk(clazz, fields[i])) { // id 代表主键
                    primaryKey = column;
                    break;
                }
            }
            sql.append(" DELETE FROM " + tbName + " WHERE " + primaryKey + " = ?");
        }
        return sql.toString();
    }

    /**
     * 设置参数
     *
     * @param entity  实体
     * @param sqlFlag sql标识
     * @param <E>     泛型
     * @return Object[] 参数数组
     */
    public static <E> Object[] setArgs(E entity, String sqlFlag) {
        Class<?> clzz = entity.getClass();
        Field[] fields = clzz.getDeclaredFields();
        if (sqlFlag.equals(SQL_INSERT)) {
            Object[] args = new Object[fields.length];
            for (int i = 0; args != null && i < args.length; i++) {
                try {
                    fields[i].setAccessible(true); // 暴力反射
                    args[i] = fields[i].get(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return args;
        } else if (sqlFlag.equals(SQL_UPDATE)) {
            Object[] args = new Object[fields.length];
            Object primaryValue = new Object();
            int j = 0;
            for (int i = 0; fields != null && i < fields.length; i++) {
                try {
                    fields[i].setAccessible(true); // 暴力反射
                    if (EntityTools.isPk(clzz, fields[i])) { // id 代表主键
                        primaryValue = fields[i].get(entity);
                        continue;
                    }
                    args[j] = fields[i].get(entity);
                    j++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            args[args.length - 1] = primaryValue;
            return args;
        } else if (sqlFlag.equals(SQL_DELETE)) {
            Object primaryValue = new Object();
            for (int i = 0; fields != null && i < fields.length; i++) {
                try {
                    fields[i].setAccessible(true); // 暴力反射
                    if (EntityTools.isPk(clzz, fields[i])) { // id 代表主键
                        primaryValue = fields[i].get(entity);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Object[] args = new Object[1]; // 长度是1
            try {
                args[0] = primaryValue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return args;
        }
        return null;

    }

    /**
     * 设置参数类型(缺少的用到了再添加)
     *
     * @param entity  实体
     * @param sqlFlag sql标识
     * @param <E>     泛型
     * @return int[] 参数类型数组
     */
    public static <E> int[] setArgTypes(E entity, String sqlFlag) {
        Field[] fields = entity.getClass().getDeclaredFields();
        if (sqlFlag.equals(SQL_INSERT)) {
            int[] argTypes = new int[fields.length];
            try {
                for (int i = 0; argTypes != null && i < argTypes.length; i++) {
                    argTypes[i] = getTypes(fields[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return argTypes;
        } else if (sqlFlag.equals(SQL_UPDATE)) {
            int[] tempArgTypes = new int[fields.length];
            int[] argTypes = new int[fields.length];
            try {
                for (int i = 0; tempArgTypes != null && i < tempArgTypes.length; i++) {
                    tempArgTypes[i] = getTypes(fields[i]);
                }
                System.arraycopy(tempArgTypes, 1, argTypes, 0, tempArgTypes.length - 1); // 数组拷贝
                argTypes[argTypes.length - 1] = tempArgTypes[0];

            } catch (Exception e) {
                e.printStackTrace();
            }
            return argTypes;

        } else if (sqlFlag.equals(SQL_DELETE)) {
            int[] argTypes = new int[1]; // 长度是1
            try {
                argTypes[0] = getTypes(fields[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return argTypes;
        }
        return null;
    }

    private static int getTypes(Field arg) {
        arg.setAccessible(true); // 暴力反射
        if (String.class.equals(arg.getType())) {
            return Types.VARCHAR;
        } else if (int.class.equals(arg.getType()) || Integer.class.equals(arg.getType())) {
            return Types.INTEGER;
        } else if (double.class.equals(arg.getType()) || Double.class.equals(arg.getType())) {
            return Types.DOUBLE;
        } else if (java.util.Date.class.isAssignableFrom(arg.getType())) {
            return Types.TIMESTAMP;
        } else if (long.class.equals(arg.getType()) || Long.class.equals(arg.getType())) {
            return Types.BIGINT;
        } else if (float.class.equals(arg.getType()) || Float.class.equals(arg.getType())) {
            return Types.FLOAT;
        } else if (boolean.class.equals(arg.getType()) || Boolean.class.equals(arg.getType())) {
            return Types.BOOLEAN;
        } else if (short.class.equals(arg.getType()) || Short.class.equals(arg.getType())) {
            return Types.INTEGER;
        } else if (byte.class.equals(arg.getType()) || Byte.class.equals(arg.getType())) {
            return Types.INTEGER;
        } else if (BigDecimal.class.equals(arg.getType())) {
            return Types.DECIMAL;
        } else {
            return Types.OTHER;
        }
    }

    /**
     * 创建条件查询sql和入参
     *
     * @param criteria 查询条件
     * @param sql      sql语句
     * @return Pair sql与sql入参对
     */
    public static Pair<String, Object[]> doCriteria(AbstractCriteria criteria, StringBuilder sql) {
        Pair<String, Object[]> result = new Pair<>();
        Object[] params = {};
        if (null != criteria) {
            if (CollectionUtils.isNotEmpty(criteria.getWhereParams())) {
                //where 条件参数拼接
                List<WhereParam> whereParams = criteria.getWhereParams();
                List<CriteriaProxy> criteriaProxys = criteria.getCriteriaProxys();
                int whereParamIndex = 0;
                if (null != criteria && CollectionUtils.isNotEmpty(whereParams)) {
                    sql.append(" WHERE ");
                    for (WhereParam whereParam : whereParams) {
                        params = doCriteriaProxy(criteriaProxys, whereParamIndex, sql, params);
                        whereParamIndex += 1;
                        if (StringUtils.isEmpty(whereParam.getKey())) {
                            continue;
                        }
                        String key = whereParam.getKey();
                        String opt = whereParam.getOpt();
                        Object value = whereParam.getValue();
                        sql.append(key).append(SPACE);
                        if (SQL_IN.equals(opt.toUpperCase()) || SQL_NOT_IN.equals(opt.toUpperCase())) {
                            sql.append(opt).append(IN_START);
                            if (value instanceof Collection) {
                                if (CollectionUtils.isNotEmpty(((Collection) value))) {
                                    Iterator iterator = ((Collection) value).iterator();
                                    while (iterator.hasNext()) {
                                        params = ArrayUtils.add(params, iterator.next());
                                        sql.append("?,");
                                    }
                                    sql.setLength(sql.length() - 1);
                                }
                            } else {
                                sql.append(SPACE).append("?");
                                params = ArrayUtils.add(params, value);
                            }
                            sql.append(IN_END);
                        } else if (SQL_IS.equals(opt.toUpperCase())) {
                            sql.append(opt).append(SPACE).append(value);
                        } else if (SQL_BETWEEN_AND.equals(opt.toUpperCase())) {
                            sql.append(opt).append(SPACE);
                            Pair<Object, Object> pair = (Pair<Object, Object>) value;
                            params = ArrayUtils.add(params, pair.getFirst());
                            params = ArrayUtils.add(params, pair.getSecond());
                        } else {
                            if (value instanceof FieldReference) {
                                FieldReference fieldReference = (FieldReference) value;
                                sql.append(opt).append(SPACE).append(fieldReference.getField());
                            } else {
                                sql.append(opt).append(SPACE).append("?");
                                params = ArrayUtils.add(params, value);
                            }
                        }
                        sql.append(" AND ");
                    }
                    params = doCriteriaProxy(criteriaProxys, whereParamIndex, sql, params);
                    sql.setLength(sql.length() - 5);
                }
            }
            //group by条件拼接
            if (CollectionUtils.isNotEmpty(criteria.getGroupFields())) {
                sql.append(SPACE).append(SQL_GROUP_BY).append(SPACE);
                Set<String> groupByFileds = criteria.getGroupFields();
                for (String groupByFiled : groupByFileds) {
                    sql.append(groupByFiled + ",");
                }
                sql.setLength(sql.length() - 1);
                if (criteria.getHaving() != null) {
                    Pair<String, Object[]> having = criteria.getHaving();
                    sql.append(SPACE).append("HAVING").append(having.getFirst());
                    params = ArrayUtils.addAll(params, having.getSecond());
                }
            }
            //排序条件拼接
            if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
                sql.append(SPACE).append(SQL_ORDER_BY).append(SPACE);
                Set<Sort> sorts = criteria.getSorts();
                for (Sort sort : sorts) {
                    sql.append(sort.getSortField()).append(SPACE).append(sort.getSortType()).append(",");
                }
                sql.setLength(sql.length() - 1);
            }
            if (criteria.getOffset() >= 0) {
                sql.append(" LIMIT ?");
                params = ArrayUtils.add(params, criteria.getOffset());
                if (criteria.getSize() > 0) {
                    sql.append(", ?");
                    params = ArrayUtils.add(params, criteria.getSize());
                }
            }
        }
        String realSql = sql.toString().replace(", WHERE", " WHERE").replace("AND  OR", "OR");
        result.setFirst(realSql);
        result.setSecond(params);
        return result;
    }

    /**
     * 更复杂的条件组装
     *
     * @author 周宁
     * @version 1.0
     */
    private static Object[] doCriteriaProxy(List<CriteriaProxy> criteriaProxys, int whereParamIndex, StringBuilder sql, Object[] params) {
        if (CollectionUtils.isNotEmpty(criteriaProxys)) {
            for (CriteriaProxy criteriaProxy : criteriaProxys) {
                if (criteriaProxy.getWhereParamsIndex() - 1 == whereParamIndex) {
                    String criteriaType = criteriaProxy.getCriteriaType();
                    if (criteriaType.equals("AND")) {
                        if (criteriaProxy.getWhereParamsIndex() == -1) {
                            sql.append(" AND ").append(criteriaProxy.getSql());
                        } else {
                            sql.append(IN_START).append(criteriaProxy.getSql()).append(IN_END).append(" AND ");
                        }
                    } else if (criteriaType.equals("WITH")) {
                    } else {
                        sql.append(SPACE).append(criteriaType).append(IN_START).append(criteriaProxy.getSql()).append(IN_END).append(" AND ");
                    }
                    params = ArrayUtils.addAll(params, criteriaProxy.getParams());
                }
            }
        }
        return params;
    }

    /**
     * 使用自定义sql
     *
     * @author 周宁
     * @version 1.0
     */
    public static Pair<String, Object[]> useSql(SQL sql) {
        SQLTree sqlTree = new SQLTree();
        Pair<String, Object[]> pair = doSql(sql);
        sqlTree.setId("0");
        sqlTree.setParams(pair.getSecond());
        sqlTree.setSql(pair.getFirst());
        sqlTree.setChilds(new ArrayList<>());
        buildSQLTree(sql, sqlTree);
        return recurSql(sqlTree, new Pair<>("", new Object[]{}));
    }

    /**
     * 递归构造查询树
     *
     * @param sql     sql拼接器
     * @param sqlTree 待构造的查询树
     * @author 周宁
     * @version 1.0
     */
    private static void buildSQLTree(SQL sql, SQLTree sqlTree) {
        List<SQL> subSqls = sql.getSubSqls();
        for (int i = 0; i < subSqls.size(); i++) {
            Pair<String, Object[]> pair = doSql(subSqls.get(i));
            SQLTree cTree = new SQLTree(pair.getFirst(), pair.getSecond(), new ArrayList<>(), UUID.randomUUID().toString().replace("-", ""));
            sqlTree.getChilds().add(cTree);
            buildSQLTree(subSqls.get(i), cTree);
        }
    }

    /**
     * 组装sql
     *
     * @param ss sql拼接器
     * @return Pair 第一个值为Sql,第二个为参数
     * @throws Pair
     * @author 周宁
     * @version 1.0
     */
    private static Pair<String, Object[]> doSql(SQL ss) {
        SQLPiepline piepline = ss.getSqlPiepline();
        StringBuilder finalSql = new StringBuilder();
        Pair<String, Object[]> pair;
        Object[] params = {};
        List<SQLPiepline.SQLNext> sqlNexts = piepline.getSqlNexts();
        boolean needSurroundBrackets = sqlNexts.size() > 1 ? true : false;
        for (SQLPiepline.SQLNext sqlNext : sqlNexts) {
            SQL nextSql = sqlNext.getSql();
            StringBuilder sql = new StringBuilder();
            if (nextSql.getSqlType().equals(EntityDao.SQL_SELECT)) {
                sql.append("SELECT ");
                if (CollectionUtils.isNotEmpty(nextSql.getSelectFields())) {
                    nextSql.getSelectFields().forEach(selectField -> sql.append(selectField + ", "));
                    sql.setLength(sql.length() - 2);
                }
                sql.append(" FROM ");
                if (StringUtils.isNotEmpty(nextSql.getTbName())) {
                    sql.append(nextSql.getTbName());
                }
                if (StringUtils.isNotEmpty(nextSql.getAliasName())) {
                    sql.append(SPACE + nextSql.getAliasName());
                }
            } else if (nextSql.getSqlType().equals(EntityDao.SQL_DELETE)) {
                sql.append("DELETE ");
                if (StringUtils.isNotEmpty(nextSql.getAliasName())) {
                    sql.append(nextSql.getAliasName());
                    sql.append(SPACE);
                }
                sql.append("FROM");
                if (StringUtils.isNotEmpty(nextSql.getTbName())) {
                    sql.append(SPACE);
                    sql.append(nextSql.getTbName());
                }
                if (StringUtils.isNotEmpty(nextSql.getAliasName())) {
                    sql.append(SPACE);
                    sql.append(nextSql.getAliasName());
                }
            } else if (nextSql.getSqlType().equals(EntityDao.SQL_UPDATE)) {
                sql.append("UPDATE ");
                if (StringUtils.isNotEmpty(nextSql.getTbName())) {
                    sql.append(nextSql.getTbName());
                }
                if (StringUtils.isNotEmpty(nextSql.getAliasName())) {
                    sql.append(SPACE);
                    sql.append(nextSql.getAliasName());
                }
            }
            //连接查询sql组装
            if (CollectionUtils.isNotEmpty(nextSql.getJoins())) {
                List<Joins.BaseJoin> joins = nextSql.getJoins();
                for (Joins.BaseJoin join : joins) {
                    sql.append(join.getJoinSql());
                    List<CriteriaProxy> criteriaProxies = join.getCriteriaProxys();
                    params = doCriteriaProxy(criteriaProxies, -2, sql, params);
                }
            }
            //update语句的拼接
            if (nextSql.getSqlType().equals(EntityDao.SQL_UPDATE)) {
                sql.append(" SET ");
                List<Pair> kvs = nextSql.getKvs();
                for (int i = 0; i < kvs.size(); i++) {
                    Pair p = kvs.get(i);
                    if (p.getSecond() instanceof FieldReference) {
                        FieldReference fieldReference = (FieldReference) p.getSecond();
                        sql.append(p.getFirst() + " = " + fieldReference.getField() + ", ");
                    } else {
                        sql.append(p.getFirst() + " = ?, ");
                        params = ArrayUtils.add(params, p.getSecond());
                    }
                }
                sql.setLength(sql.length() - 2);
            }
            pair = doCriteria(nextSql, sql);
            if (StringUtils.isNotEmpty(sqlNext.getUnionType())) {
                finalSql.append(SPACE).append(sqlNext.getUnionType());
            }
            if (needSurroundBrackets) {
                finalSql.append(SPACE).append(IN_START).append(pair.getFirst()).append(IN_END);
            } else {
                finalSql.append(SPACE).append(pair.getFirst());
            }
            params = ArrayUtils.addAll(params, pair.getSecond());
        }
        return new Pair<>(finalSql.toString().replaceFirst(" ", ""), params);
    }

    /**
     * 递归组装子查询参数和sql
     *
     * @param sqlTree 待构造的查询树
     * @author 周宁
     * @version 1.0
     */
    private static Pair<String, Object[]> recurSql(SQLTree sqlTree, Pair<String, Object[]> pair) {
        List<SQLTree> childs = sqlTree.getChilds();
        if (CollectionUtils.isNotEmpty(childs)) {
            String[] arr = sqlTree.getSql().split("FROM");
            pair.setFirst(pair.getFirst().concat(arr[0] + "FROM("));
            for (SQLTree cnode : childs) {
                pair.setFirst(pair.getFirst().concat(" UNION ALL ("));
                if (CollectionUtils.isNotEmpty(cnode.getChilds())) {
                    pair = recurSql(cnode, pair);
                } else {
                    pair.setFirst(pair.getFirst().concat(cnode.getId()));
                    pair.setFirst(pair.getFirst().replace(cnode.getId(), cnode.getSql()));
                    pair.setSecond(ArrayUtils.addAll(pair.getSecond(), cnode.getParams()));
                }
                pair.setFirst(pair.getFirst().concat(")"));
            }
            pair.setFirst(pair.getFirst().concat(")" + arr[1]));
            pair.setSecond(ArrayUtils.addAll(pair.getSecond(), sqlTree.getParams()));
        } else {
            pair.setFirst(pair.getFirst().concat(sqlTree.getSql()));
            pair.setSecond(ArrayUtils.addAll(pair.getSecond(), sqlTree.getParams()));
        }
        pair.setFirst(pair.getFirst().replace("( UNION ALL", "("));
        return pair;
    }
}
