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
                        sql.append(key).append(" ");
                        if ("IN".equals(opt.toUpperCase()) || "NOT IN".equals(opt.toUpperCase())) {
                            sql.append(opt).append('(');
                            if (value instanceof Collection) {
                                if (CollectionUtils.isNotEmpty(((Collection) value))) {
                                    Iterator iterator = ((Collection) value).iterator();
                                    while (iterator.hasNext()) {
                                        params = ArrayUtils.add(params, iterator.next());
                                        sql.append("?,");
                                    }
                                    sql.setLength(sql.length() - 1);
                                }
                            } else if (value instanceof SQL) {
                                SQL inSql = (SQL) value;
                                Pair<String, Object[]> inPair = useSql(inSql);
                                sql.append(inPair.getFirst());
                                params = ArrayUtils.addAll(params, inPair.getSecond());
                            } else {
                                sql.append(" ").append("?");
                                params = ArrayUtils.add(params, value);
                            }
                            sql.append(')');
                        } else if ("IS".equals(opt.toUpperCase())) {
                            sql.append(opt).append(" ").append(value);
                        } else if ("BETWEEN ? AND ?".equals(opt.toUpperCase())) {
                            sql.append(opt).append(" ");
                            Pair<Object, Object> pair = (Pair<Object, Object>) value;
                            params = ArrayUtils.add(params, pair.getFirst());
                            params = ArrayUtils.add(params, pair.getSecond());
                        } else if ("FIND IN SET".equals(opt)) {
                            params = ArrayUtils.add(params, value);
                        } else {
                            if (value instanceof FieldReference) {
                                FieldReference fieldReference = (FieldReference) value;
                                sql.append(opt).append(" ").append(fieldReference.getField());
                            } else if (value instanceof SQL) {
                                SQL whereSql = (SQL) value;
                                Pair<String, Object[]> wherePair = useSql(whereSql);
                                sql.append(opt).append('(').append(wherePair.getFirst()).append(')');
                                params = ArrayUtils.addAll(params, wherePair.getSecond());
                            } else {
                                sql.append(opt).append(" ").append("?");
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
                sql.append(" ").append("GROUP BY").append(" ");
                Set<String> groupByFileds = criteria.getGroupFields();
                for (String groupByFiled : groupByFileds) {
                    sql.append(groupByFiled + ",");
                }
                sql.setLength(sql.length() - 1);
                if (criteria.getHaving() != null) {
                    Pair<String, Object[]> having = criteria.getHaving();
                    sql.append(" ").append("HAVING").append(having.getFirst());
                    params = ArrayUtils.addAll(params, having.getSecond());
                }
            }
            //排序条件拼接
            if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
                sql.append(" ").append("ORDER BY").append(" ");
                Set<Sort> sorts = criteria.getSorts();
                for (Sort sort : sorts) {
                    sql.append(sort.getSortField()).append(" ").append(sort.getSortType()).append(",");
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
                            sql.append('(').append(criteriaProxy.getSql()).append(')').append(" AND ");
                        }
                    } else if (criteriaType.equals("WITH")) {
                    } else if (criteriaType.equals("WHEREAND")) {
                        sql.append(criteriaProxy.getSql()).append(" AND ");
                    } else if (criteriaType.equals("WHEREOR")) {
                        sql.append(" OR ").append(criteriaProxy.getSql()).append(" AND ");
                    } else {
                        sql.append(" ").append(criteriaType).append('(').append(criteriaProxy.getSql()).append(')').append(" AND ");
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
     * @param sqlObj sql对象
     * @return Pair pair.first sql pair.second 参数数组
     * @author 周宁
     * @version 1.0
     */
    public static Pair<String, Object[]> useSql(SQL sqlObj) {
        //虚拟出查询的根节点，兼容处理联合查询
        SQL parentSQL = new SQL().select("*").from(sqlObj);
        SQLTree sqlTree = new SQLTree();
        sqlTree.setId("0");
        sqlTree.setParams(new Object[]{});
        sqlTree.setSql(" FROM ");
        sqlTree.setChilds(new ArrayList<>());
        buildSQLTree(parentSQL, sqlTree);
        Pair<String, Object[]> pair = recurSql(sqlTree, new Pair<>("", new Object[]{}));
        String parentSql = pair.getFirst().trim();
        //单条sql语句不保留左右两侧括号
        if (sqlObj.getSqlPiepline().getSqlNexts().size() <= 1) {
            parentSql = parentSql.substring(1, parentSql.length() - 1).trim();
        }
        pair.setFirst(parentSql);
        return pair;
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
            SQLTree cTree = new SQLTree(
                    pair.getFirst(),
                    pair.getSecond(),
                    new ArrayList<>(),
                    UUID.randomUUID().toString().replace("-", ""),
                    subSqls.get(i).getUnionType(),
                    subSqls.get(i).getAsTable(),
                    subSqls.get(i).getFromAsTable()
            );
            sqlTree.getChilds().add(cTree);
            buildSQLTree(subSqls.get(i), cTree);
        }
    }

    /**
     * 组装sql
     *
     * @param sqlObj sql对象的封装
     * @return Pair 第一个值为Sql,第二个为参数
     * @author 周宁
     * @version 1.0
     */
    private static Pair<String, Object[]> doSql(SQL sqlObj) {
        Pair<String, Object[]> pair;
        Object[] params = {};
        StringBuilder sql = new StringBuilder();
        if (sqlObj.getSqlType().equals(EntityDao.SQL_SELECT)) {
            sql.append("SELECT ");
            if (CollectionUtils.isNotEmpty(sqlObj.getSelectFields())) {
                List<Object> selects = sqlObj.getSelectFields();
                for (Object obj : selects) {
                    if (obj instanceof ValueReference) {
                        sql.append("?, ");
                        params = ArrayUtils.add(params, ((ValueReference) obj).getValue());
                    } else {
                        sql.append(obj.toString() + ", ");
                    }
                }
                sql.setLength(sql.length() - 2);
            }
            sql.append(" FROM ");
            if (StringUtils.isNotEmpty(sqlObj.getTbName())) {
                sql.append(sqlObj.getTbName());
            }
            if (StringUtils.isNotEmpty(sqlObj.getAliasName())) {
                sql.append(" " + sqlObj.getAliasName());
            }
        } else if (sqlObj.getSqlType().equals(EntityDao.SQL_DELETE)) {
            sql.append("DELETE ");
            if (StringUtils.isNotEmpty(sqlObj.getDeleteAliasName())) {
                sql.append(sqlObj.getDeleteAliasName());
                sql.append(" ");
            }
            sql.append("FROM");
            if (StringUtils.isNotEmpty(sqlObj.getTbName())) {
                sql.append(" ");
                sql.append(sqlObj.getTbName());
            }
            if (StringUtils.isNotEmpty(sqlObj.getAliasName())) {
                sql.append(" ");
                sql.append(sqlObj.getAliasName());
            }
        } else if (sqlObj.getSqlType().equals(EntityDao.SQL_UPDATE)) {
            sql.append("UPDATE ");
            if (StringUtils.isNotEmpty(sqlObj.getTbName())) {
                sql.append(sqlObj.getTbName());
            }
            if (StringUtils.isNotEmpty(sqlObj.getAliasName())) {
                sql.append(" ");
                sql.append(sqlObj.getAliasName());
            }
        } else if (sqlObj.getSqlType().equals(EntityDao.SQL_TRUNCATE)) {
            Drunk drunk = sqlObj.getDrunk();
            Set<String> tables = drunk.getTables();
            for (String tb : tables) {
                sql.append("TRUNCATE TABLE " + tb + ";\n");
            }
            return new Pair<>(sql.toString(), null);

        } else if (sqlObj.getSqlType().equals(EntityDao.SQL_DROP)) {
            Drunk drunk = sqlObj.getDrunk();
            Set<String> tables = drunk.getTables();
            sql.append("DROP TABLE ");
            if (drunk.isIfExists()) {
                sql.append("IF EXISTS ");
            }
            if (CollectionUtils.isNotEmpty(tables)) {
                for (String tb : tables) {
                    sql.append(tb + ",");
                }
                sql.setLength(sql.length() - 1);
            }
            return new Pair<>(sql.toString(), null);

        }
        //连接查询sql组装
        if (CollectionUtils.isNotEmpty(sqlObj.getJoins())) {
            List<Joins.BaseJoin> joins = sqlObj.getJoins();
            for (Joins.BaseJoin join : joins) {
                sql.append(join.getJoinSql());
                List<CriteriaProxy> criteriaProxies = join.getCriteriaProxys();
                params = doCriteriaProxy(criteriaProxies, -2, sql, params);
            }
        }
        //update语句的拼接
        if (sqlObj.getSqlType().equals(EntityDao.SQL_UPDATE)) {
            sql.append(" SET ");
            List<Pair> kvs = sqlObj.getKvs();
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
        pair = doCriteria(sqlObj, sql);
        params = ArrayUtils.addAll(params, pair.getSecond());
        return new Pair<>(pair.getFirst(), params);
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
            //是否为虚拟的根节点sql,虚拟根节点sql不需要添加括号
            boolean isRootSql = sqlTree.getId().equals("0");
            String[] arr = sqlTree.getSql().split("FROM");
            if (sqlTree.getChilds().size() > 1 && sqlTree.getFromAsTable()) {
                if (!isRootSql) {
                    pair.setFirst(pair.getFirst().concat(arr[0] + "FROM(("));
                }
            } else {
                if (!isRootSql) {
                    pair.setFirst(pair.getFirst().concat(arr[0] + "FROM("));
                }
            }
            //子查询递归拼接格式为：(sql) union|,|union all (sql)
            for (int i = 0; i < childs.size(); i++) {
                SQLTree cnode = childs.get(i);
                if (i == 0) {
                    pair.setFirst(pair.getFirst().concat(" ("));
                } else {
                    pair.setFirst(pair.getFirst().concat(" " + cnode.getUnionType() + " ("));
                }
                if (CollectionUtils.isNotEmpty(cnode.getChilds())) {
                    pair = recurSql(cnode, pair);
                } else {
                    pair.setFirst(pair.getFirst().concat(cnode.getId()));
                    pair.setFirst(pair.getFirst().replace(cnode.getId(), cnode.getSql()));
                    pair.setSecond(ArrayUtils.addAll(pair.getSecond(), cnode.getParams()));
                }
                if (StringUtils.isNotEmpty(cnode.getAsTable()) && !cnode.getFromAsTable()) {
                    pair.setFirst(pair.getFirst().concat(") " + cnode.getAsTable() + ""));
                } else {
                    pair.setFirst(pair.getFirst().concat(")"));
                }
            }
            if (sqlTree.getChilds().size() > 1 && sqlTree.getFromAsTable()) {
                //rootSql不存在from(String asTable,SQL c)这种查询方式
                if (!isRootSql) {
                    if (sqlTree.getFromAsTable()) {
                        pair.setFirst(pair.getFirst().concat(") " + sqlTree.getAsTable() + " )" + arr[1]));
                    } else {
                        pair.setFirst(pair.getFirst().concat("))" + arr[1]));
                    }
                }
            } else {
                if (!isRootSql) {
                    if (sqlTree.getFromAsTable()) {
                        pair.setFirst(pair.getFirst().concat(" " + sqlTree.getAsTable() + " )" + arr[1]));
                    } else {
                        pair.setFirst(pair.getFirst().concat(")" + arr[1]));
                    }
                }
            }
            pair.setSecond(ArrayUtils.addAll(pair.getSecond(), sqlTree.getParams()));
        } else {
            pair.setFirst(pair.getFirst().concat(sqlTree.getSql()));
            pair.setSecond(ArrayUtils.addAll(pair.getSecond(), sqlTree.getParams()));
        }
        return pair;
    }

}
