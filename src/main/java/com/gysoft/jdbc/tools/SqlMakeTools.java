package com.gysoft.jdbc.tools;


import com.gysoft.jdbc.bean.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;
import java.util.function.Consumer;

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
    public static Pair<String, Object[]> doCriteria(Criteria criteria, StringBuilder sql) {
        boolean joinFlag = criteria.isJoinFlag();
        Pair<String, Object[]> result = new Pair<>();
        Object[] params = {};
        if (sql == null) {
            sql = doCriteriaSelect(criteria);
        }
        if (joinFlag) {
            StringBuilder overrideSql = doCriteriaSelect(criteria);
            //重新生成sql
            List<Joins.BaseJoin> joins = criteria.getJoins();
            for (Joins.BaseJoin join : joins) {
                overrideSql.append(join.getJoinSql());
                List<CriteriaProxy> criteriaProxies = join.getCriteriaProxys();
                params = doCriteriaProxy(criteriaProxies, -2, overrideSql, params);
            }
            sql = overrideSql;
        }
        if (null != criteria) {
            if (CollectionUtils.isNotEmpty(criteria.getWhereParams())) {
                //where 条件参数拼接
                Set<WhereParam> whereParams = criteria.getWhereParams();
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
                        } else if(SQL_BETWEEN_AND.equals(opt.toUpperCase())){
                            sql.append(opt).append(SPACE);
                            Pair<Object,Object> pair = (Pair<Object, Object>) value;
                            params = ArrayUtils.add(params, pair.getFirst());
                            params = ArrayUtils.add(params,pair.getSecond());
                        }else {
                            sql.append(opt).append(SPACE).append("?");
                            params = ArrayUtils.add(params, value);
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
                if(criteria.getHaving()!=null){
                    Pair<String,Object[]> having = criteria.getHaving();
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
        }
        String realSql = sql.toString().replace(", WHERE", " WHERE").replace("AND  OR", "OR");
        result.setFirst(realSql);
        result.setSecond(params);
        return result;
    }

    /**
     * 组装更复杂的sql
     *
     * @param criteriaProxys
     * @param whereParamIndex
     * @param params
     * @param sql
     * @return Object[]
     * @throws
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
                    } else {
                        sql.append(SPACE).append(criteriaType).append(IN_START).append(criteriaProxy.getSql()).append(IN_END).append(" AND ");
                    }
                    params = ArrayUtils.addAll(params, criteriaProxy.getParams());
                }
            }
        }
        return params;
    }

    private static StringBuilder doCriteriaSelect(Criteria criteria) {
        //重新生成sql
        StringBuilder overrideSql = new StringBuilder();
        Set<String> selectFields = criteria.getSelectFields();
        if (!CollectionUtils.isEmpty(selectFields)) {
            overrideSql.append("SELECT ");
            criteria.getSelectFields().forEach(selectField -> overrideSql.append(selectField + ", "));
            overrideSql.setLength(overrideSql.length() - 2);
            overrideSql.append(" FROM ");
        } else {
            overrideSql.append("SELECT * FROM ");
        }
        if (StringUtils.isNotEmpty(criteria.getpTable())) {
            overrideSql.append(criteria.getpTable());
        }
        if (StringUtils.isNotEmpty(criteria.getAliasName())) {
            overrideSql.append(" AS " + criteria.getAliasName());
        }
        return overrideSql;
    }

    /**
     * 递归构造查询树
     *
     * @param criteria 查询条件
     * @param criteriaTree 待构造的查询树
     * @author 周宁
     * @version 1.0
     */
    public static void buildCriteriaTree(Criteria criteria, CriteriaTree criteriaTree) {
        List<Criteria> criterias = criteria.getCriterias();
        for (int i = 0; i < criterias.size(); i++) {
            Pair<String, Object[]> pair = doCriteria(criterias.get(i), doCriteriaSelect(criterias.get(i)));
            CriteriaTree cTree = CriteriaTree.builder().id((UUID.randomUUID().toString())).sql(pair.getFirst()).params(pair.getSecond()).childCriteriaTree(new ArrayList<>()).build();
            criteriaTree.getChildCriteriaTree().add(cTree);
            buildCriteriaTree(criterias.get(i), cTree);
        }
    }

    /**
     * 递归组装子查询参数和sql
     * @author 周宁
     * @param criteriaTree 查询条件树
     * @param pair sql和查询参数包装
     * @version 1.0
     * @return Pair sql和查询参数包装
     */

    public static Pair<String,Object[]> doSubCriteria(CriteriaTree criteriaTree, Pair<String,Object[]> pair) {
        List<CriteriaTree> childCriteriaNodes = criteriaTree.getChildCriteriaTree();
        if (CollectionUtils.isNotEmpty(childCriteriaNodes)) {
            String[] arr = criteriaTree.getSql().split("FROM");
            pair.setFirst(pair.getFirst().concat(arr[0] + "FROM("));
            for (CriteriaTree cnode : childCriteriaNodes) {
                pair.setFirst(pair.getFirst().concat(" UNION ALL "));
                if (CollectionUtils.isNotEmpty(cnode.getChildCriteriaTree())) {
                    pair = doSubCriteria(cnode, pair);
                } else {
                    pair.setFirst(pair.getFirst().concat(cnode.getId()));
                    pair.setFirst(pair.getFirst().replace(cnode.getId(), cnode.getSql()));
                    pair.setSecond(ArrayUtils.addAll(pair.getSecond(), cnode.getParams()));
                }
            }
            pair.setFirst(pair.getFirst().concat(")" + arr[1]));
            pair.setSecond(ArrayUtils.addAll(pair.getSecond(), criteriaTree.getParams()));
        } else {
            pair.setFirst(pair.getFirst().concat(criteriaTree.getSql()));
            pair.setSecond(ArrayUtils.addAll(pair.getSecond(), criteriaTree.getParams()));
        }
        pair.setFirst(pair.getFirst().replace("( UNION ALL", "("));
        return pair;
    }
}
