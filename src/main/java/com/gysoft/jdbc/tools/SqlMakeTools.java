package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        StringBuilder sql = new StringBuilder();
        Field[] fields = EntityTools.getDeclaredFields(clazz);
        if (sqlFlag.equals(SQL_INSERT)) {
            sql.append(" INSERT INTO " + tbName);
            sql.append("(");
            for (int i = 0; fields != null && i < fields.length; i++) {
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
        Field[] fields = EntityTools.getDeclaredFields(clzz);
        if (sqlFlag.equals(SQL_INSERT)) {
            Object[] args = new Object[fields.length];
            for (int i = 0; args != null && i < args.length; i++) {
                try {
                    args[i] = fields[i].get(entity);
                } catch (Exception e) {
                    throw new GyjdbcException(e);
                }
            }
            return args;
        } else if (sqlFlag.equals(SQL_UPDATE)) {
            Object[] args = new Object[fields.length];
            Object primaryValue = new Object();
            int j = 0;
            for (int i = 0; fields != null && i < fields.length; i++) {
                try {
                    if (EntityTools.isPk(clzz, fields[i])) { // id 代表主键
                        primaryValue = fields[i].get(entity);
                        continue;
                    }
                    args[j] = fields[i].get(entity);
                    j++;
                } catch (Exception e) {
                    throw new GyjdbcException(e);
                }
            }
            args[args.length - 1] = primaryValue;
            return args;
        } else if (sqlFlag.equals(SQL_DELETE)) {
            Object primaryValue = new Object();
            for (int i = 0; fields != null && i < fields.length; i++) {
                try {
                    if (EntityTools.isPk(clzz, fields[i])) { // id 代表主键
                        primaryValue = fields[i].get(entity);
                        break;
                    }
                } catch (Exception e) {
                    throw new GyjdbcException(e);
                }
            }
            Object[] args = new Object[1]; // 长度是1
            try {
                args[0] = primaryValue;
            } catch (Exception e) {
                throw new GyjdbcException(e);
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
        Field[] fields = EntityTools.getDeclaredFields(entity.getClass());
        if (sqlFlag.equals(SQL_INSERT)) {
            int[] argTypes = new int[fields.length];
            try {
                for (int i = 0; argTypes != null && i < argTypes.length; i++) {
                    argTypes[i] = getTypes(fields[i]);
                }
            } catch (Exception e) {
                throw new GyjdbcException(e);
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
                throw new GyjdbcException(e);
            }
            return argTypes;

        } else if (sqlFlag.equals(SQL_DELETE)) {
            int[] argTypes = new int[1]; // 长度是1
            try {
                argTypes[0] = getTypes(fields[0]);
            } catch (Exception e) {
                throw new GyjdbcException(e);
            }
            return argTypes;
        }
        return null;
    }

    private static int getTypes(Field arg) {
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
     * @author 周宁
     */
    public static Pair<String, Object[]> doCriteria(AbstractCriteria criteria, StringBuilder sql) {
        List<Object> params = new ArrayList<>();
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
                        String rawKey = whereParam.getKey();
                        String key = rawKey;
                        // 处理 OR 前缀：回退前一个条件的 " AND "，替换为 " OR "
                        if (rawKey.startsWith(" OR ")) {
                            key = rawKey.substring(4);
                            int len = sql.length();
                            if (len >= 5 && " AND ".equals(sql.substring(len - 5))) {
                                sql.setLength(len - 5);
                                sql.append(" OR ");
                            }
                        }
                        String opt = whereParam.getOpt();
                        Object value = whereParam.getValue();
                        sql.append(key).append(" ");
                        if ("IN".equals(opt.toUpperCase()) || "NOT IN".equals(opt.toUpperCase())) {
                            sql.append(opt).append('(');
                            if (value instanceof Collection) {
                                if (CollectionUtils.isNotEmpty(((Collection) value))) {
                                    Iterator iterator = ((Collection) value).iterator();
                                    while (iterator.hasNext()) {
                                        params.add(iterator.next());
                                        sql.append("?,");
                                    }
                                    sql.setLength(sql.length() - 1);
                                }
                            } else if (value instanceof SQL) {
                                SQL inSql = (SQL) value;
                                Pair<String, Object[]> inPair = useSql(inSql);
                                sql.append(inPair.getFirst());
                                addAll(params, inPair.getSecond());
                            } else {
                                sql.append(" ").append("?");
                                params.add(value);
                            }
                            sql.append(')');
                        } else if ("IS".equals(opt.toUpperCase())) {
                            sql.append(opt).append(" ").append(value);
                        } else if ("BETWEEN ? AND ?".equals(opt.toUpperCase())) {
                            sql.append(opt).append(" ");
                            Pair<Object, Object> pair = (Pair<Object, Object>) value;
                            params.add(pair.getFirst());
                            params.add(pair.getSecond());
                        } else {
                            if (value instanceof FieldReference) {
                                FieldReference fieldReference = (FieldReference) value;
                                sql.append(opt).append(" ").append(fieldReference.getField());
                            } else if (value instanceof SQL) {
                                SQL whereSql = (SQL) value;
                                Pair<String, Object[]> wherePair = useSql(whereSql);
                                sql.append(opt).append('(').append(wherePair.getFirst()).append(')');
                                addAll(params, wherePair.getSecond());
                            } else {
                                sql.append(opt).append(" ").append("?");
                                params.add(value);
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
            }
            //having拼接
            if (criteria.getHaving() != null) {
                Pair<String, Object[]> having = criteria.getHaving();
                sql.append(" ").append("HAVING").append(having.getFirst());
                addAll(params, having.getSecond());
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
                params.add(criteria.getOffset());
                if (criteria.getSize() > 0) {
                    sql.append(", ?");
                    params.add(criteria.getSize());
                }
            }
        }
        return new Pair<>(sql.toString(), params.toArray());
    }

    /**
     * 更复杂的条件组装
     *
     * @author 周宁
     * @version 1.0
     */
    private static List<Object> doCriteriaProxy(List<CriteriaProxy> criteriaProxys, int whereParamIndex, StringBuilder sql, List<Object> params) {
        if (CollectionUtils.isNotEmpty(criteriaProxys)) {
            for (CriteriaProxy criteriaProxy : criteriaProxys) {
                if (criteriaProxy.getWhereParamsIndex() - 1 == whereParamIndex) {
                    String criteriaType = criteriaProxy.getCriteriaType();
                    // 去掉前一个条件追加的 " AND " 分隔符，避免 "AND  OR" 双分隔符
                    if (!"AND".equals(criteriaType) && !"WHEREAND".equals(criteriaType)) {
                        int len = sql.length();
                        if (len >= 5 && " AND ".equals(sql.substring(len - 5))) {
                            sql.setLength(len - 5);
                        }
                    }
                    if (criteriaType.equals("AND")) {
                        if (criteriaProxy.getWhereParamsIndex() == -1) {
                            sql.append(" AND ").append(criteriaProxy.getSql());
                        } else {
                            sql.append('(').append(criteriaProxy.getSql()).append(')').append(" AND ");
                        }
                    } else if (criteriaType.equals("JOINS")) {
                        sql.append(" ON ").append(criteriaProxy.getSql());
                    } else if (criteriaType.equals("WITH")) {
                    } else if (criteriaType.equals("WHEREAND")) {
                        sql.append(criteriaProxy.getSql()).append(" AND ");
                    } else if (criteriaType.equals("WHEREOR")) {
                        sql.append(" OR ").append(criteriaProxy.getSql()).append(" AND ");
                    } else {
                        sql.append(" ").append(criteriaType).append(" (").append(criteriaProxy.getSql()).append(')').append(" AND ");
                    }
                    addAll(params, criteriaProxy.getParams());
                }
            }
        }
        return params;
    }

    private static void addAll(List<Object> params, Object[] values) {
        if (values != null) {
            Collections.addAll(params, values);
        }
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
        //只有查询语句才需要虚拟查询树，其他场景不需要
        if (!SQL_SELECT.equals(sqlObj.getSqlType())) {
            return doSql(sqlObj);
        } else {
            //虚拟出查询的根节点，兼容处理联合查询和子查询
            SQL parentSQL = new SQL().select("*").from(sqlObj);
            SQLTree sqlTree = new SQLTree();
            sqlTree.setId("0");
            sqlTree.setParams(new Object[]{});
            sqlTree.setSql(" FROM ");
            sqlTree.setChilds(new ArrayList<>());
            //递归组装SQL树
            buildSQLTree(parentSQL, sqlTree);
            //递归SQL树获取真正的sql和参数
            Pair<String, Object[]> pair = recurSql(sqlTree, new Pair<>("", new Object[]{}));
            String parentSql = pair.getFirst().trim();
            //非联合查询sql语句不必保留左右两侧括号
            if (sqlObj.getSqlPiepline().getSqlNexts().size() <= 1 && parentSql.startsWith("(") && parentSql.endsWith(")")) {
                parentSql = parentSql.substring(1, parentSql.length() - 1).trim();
            }
            pair.setFirst(parentSql);
            return pair;
        }
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
            //单个sql对象对应的sql和参数组装
            Pair<String, Object[]> pair = doSql(subSqls.get(i));
            SQLTree cTree = new SQLTree(pair.getFirst(), pair.getSecond(), new ArrayList<>(), UUID.randomUUID().toString().replace("-", ""), subSqls.get(i).getUnionType(), subSqls.get(i).getAsTable(), subSqls.get(i).getFromAsTable());
            //加入子查询列表
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
        //先拼接基础查询
        Pair<String, Object[]> pair;
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        if (sqlObj.getSqlType().equals(EntityDao.SQL_SELECT)) {
            sql.append("SELECT ");
            if (CollectionUtils.isNotEmpty(sqlObj.getSelectFields())) {
                List<Object> selects = sqlObj.getSelectFields();
                for (Object obj : selects) {
                    if (obj instanceof ValueReference) {
                        sql.append("?, ");
                        params.add(((ValueReference) obj).getValue());
                    } else if (obj instanceof SQL) {
                        //兼容select字段为一条sql
                        Pair<String, Object[]> temp = SqlMakeTools.useSql((SQL) obj);
                        if (temp.getFirst().startsWith("(")) {
                            sql.append(temp.getFirst()).append(", ");
                        } else {
                            sql.append("(").append(temp.getFirst()).append("), ");
                        }
                        addAll(params, temp.getSecond());
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
            return new Pair<>(sql.toString(), new Object[]{});

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
            return new Pair<>(sql.toString(), new Object[]{});

        } else if (sqlObj.getSqlType().equals(SQL_INSERT) || sqlObj.getSqlType().equals(SQL_INSERTIGNORE) || sqlObj.getSqlType().equals(SQL_REPLACE)) {
            sql.append(sqlObj.getSqlType().toUpperCase()).append(" INTO ");

            if (StringUtils.isNotEmpty(sqlObj.getInsert().getFirst())) {
                sql.append(sqlObj.getInsert().getFirst()).append(" ");
            }
            List<String> insertFields = sqlObj.getInsert().getSecond();
            if (CollectionUtils.isNotEmpty(insertFields)) {
                sql.append("(").append(insertFields.stream().collect(Collectors.joining(","))).append(")");
            }
            return new Pair<>(sql.toString(), new Object[]{});
        } else if (sqlObj.getSqlType().equals(SQL_CREATE)) {
            TableMeta tableMeta = sqlObj.getTableMeta();
            List<ColumnMeta> columns = tableMeta.getColumns();
            if (columns.isEmpty()) {
                throw new GyjdbcException("未指定任何字段");
            }
            String tbName = EntityTools.transferColumnName(StringUtils.isEmpty(tableMeta.getName()) ? "tmp_" + UUID.randomUUID().toString().toLowerCase().replace("-", "") : tableMeta.getName());
            StringBuilder createSql = new StringBuilder();
            createSql.append("CREATE ");
            if (tableMeta.isTemporary()) {
                createSql.append("TEMPORARY ");
            }
            createSql.append("TABLE ");
            if (tableMeta.isIfNotExists()) {
                createSql.append("IF NOT EXISTS ");
            }
            createSql.append(tbName);
            createSql.append(" (");
            AtomicBoolean hasAutoIncrField = new AtomicBoolean(false);
            columns.forEach(columnMeta -> {
                createSql.append(EntityTools.transferColumnName(columnMeta.getName()));
                createSql.append(" ").append(columnMeta.getDataType());
                if (columnMeta.isNotNull()) {
                    createSql.append(" NOT NULL");
                }
                if (columnMeta.isPrimaryKey()) {
                    createSql.append(" PRIMARY KEY");
                    if (columnMeta.isAutoIncr()) {
                        createSql.append(" AUTO_INCREMENT");
                        hasAutoIncrField.set(true);
                    }
                }
                if (columnMeta.getVal() != null) {
                    String upperVal = columnMeta.getVal().toUpperCase();
                    if (columnMeta.getJdbcType().equals(JDBCType.TIMESTAMP) || "NULL".equals(upperVal) || "CURRENT_TIMESTAMP".equals(upperVal) || upperVal.contains("()")) {
                        createSql.append(String.format(" DEFAULT %s", (columnMeta.getVal())));
                    } else {
                        createSql.append(String.format(" DEFAULT '%s'", (columnMeta.getVal())));
                    }
                }
                if (StringUtils.isNotEmpty(columnMeta.getComment())) {
                    createSql.append(String.format(" COMMENT '%s'", columnMeta.getComment()));
                }
                createSql.append(",");
            });
            List<IndexMeta> indexMetas = tableMeta.getIndexs();
            indexMetas.forEach(indexMeta -> {
                createSql.append((indexMeta.isUnique() ? "UNIQUE" : "") + " KEY " + (indexMeta.getIndexName() == null ? EntityTools.transferColumnName("ix_" + indexMeta.getColumnNames().stream().map(cName -> EntityTools.transferFieldName(cName)).collect(Collectors.joining("_"))) : EntityTools.transferColumnName(indexMeta.getIndexName())) + " (");
                indexMeta.getColumnNames().forEach(cc -> {
                    createSql.append(EntityTools.transferColumnName(cc));
                    createSql.append(",");
                });
                createSql.setLength(createSql.length() - 1);
                createSql.append(")");
                if (StringUtils.isNotEmpty(indexMeta.getIndexType())) {
                    createSql.append(" ").append(indexMeta.getIndexType());
                }
                if (StringUtils.isNotEmpty(indexMeta.getComment())) {
                    createSql.append(" COMMENT '").append(indexMeta.getComment()).append("'");
                }
                createSql.append(",");
            });
            createSql.setLength(createSql.length() - 1);
            createSql.append(")");
            if (tableMeta.getEngine() != null) {
                createSql.append(" ENGINE=").append(tableMeta.getEngine());
            }
            if (StringUtils.isNotEmpty(tableMeta.getCharacterSet())) {
                createSql.append(" DEFAULT CHARSET=").append(tableMeta.getCharacterSet());
            } else {
                createSql.append(" DEFAULT CHARSET=utf8mb4");
            }
            if (StringUtils.isNotEmpty(tableMeta.getCollation())) {
                createSql.append(" COLLATE=").append(tableMeta.getCollation());
            }
            if (tableMeta.getAutoIncrement() != null && hasAutoIncrField.get()) {
                createSql.append(" AUTO_INCREMENT=").append(tableMeta.getAutoIncrement());
            }
            if (tableMeta.getRowFormat() != null) {
                createSql.append(" ROW_FORMAT=").append(tableMeta.getRowFormat());
            }
            if (StringUtils.isNotEmpty(tableMeta.getComment())) {
                createSql.append(" COMMENT=" + "'" + tableMeta.getComment() + "'");
            }
            return new Pair<>(createSql.toString(), new Object[]{});
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
        //update字段拼接
        if (sqlObj.getSqlType().equals(EntityDao.SQL_UPDATE)) {
            sql.append(" SET ");
            List<Pair> kvs = sqlObj.getKvs();
            if (CollectionUtils.isNotEmpty(kvs)) {
                for (int i = 0; i < kvs.size(); i++) {
                    Pair p = kvs.get(i);
                    if (p.getSecond() instanceof FieldReference) {
                        FieldReference fieldReference = (FieldReference) p.getSecond();
                        sql.append(p.getFirst() + " = " + fieldReference.getField() + ", ");
                    } else if (p.getSecond() instanceof SQL) {
                        Pair<String, Object[]> updatePair = useSql((SQL) p.getSecond());
                        sql.append(p.getFirst() + " = (" + updatePair.getFirst() + "), ");
                        addAll(params, updatePair.getSecond());
                    } else {
                        sql.append(p.getFirst() + " = ?, ");
                        params.add(p.getSecond());
                    }
                }
                sql.setLength(sql.length() - 2);
            }
        }
        //组装条件
        pair = doCriteria(sqlObj, sql);
        addAll(params, pair.getSecond());
        if (EntityDao.SQL_SELECT.equals(sqlObj.getSqlType()) && StringUtils.isNotEmpty(sqlObj.getLockClause())) {
            pair.setFirst(pair.getFirst() + " " + sqlObj.getLockClause());
        }
        return new Pair<>(pair.getFirst(), params.toArray());
    }

    /**
     * 递归组装子查询参数和sql
     *
     * @param sqlTree 待构造的查询树
     * @return Pair 第一个值为Sql,第二个为参数数组
     * @author 周宁
     * @version 2.0 优化版本：使用StringBuilder提升性能，修复replace占位符bug，简化逻辑
     */
    private static Pair<String, Object[]> recurSql(SQLTree sqlTree, Pair<String, Object[]> pair) {
        List<SQLTree> childs = sqlTree.getChilds();
        StringBuilder sqlBuilder = new StringBuilder(pair.getFirst());
        List<Object> paramsList = new ArrayList<>();
        // 将现有参数添加到列表中
        if (pair.getSecond() != null) {
            Collections.addAll(paramsList, pair.getSecond());
        }
        if (CollectionUtils.isNotEmpty(childs)) {
            //是否为虚拟的根节点sql,虚拟根节点sql不需要添加括号
            boolean isRootSql = "0".equals(sqlTree.getId());
            // 安全地分割FROM子句
            String parentSql = sqlTree.getSql();
            int fromIndex = parentSql.indexOf("FROM");
            String beforeFrom = fromIndex >= 0 ? parentSql.substring(0, fromIndex) : parentSql;
            String afterFrom = fromIndex >= 0 && fromIndex + 4 < parentSql.length() ? parentSql.substring(fromIndex + 4) : "";
            boolean hasMultipleChildren = childs.size() > 1;
            boolean hasFromAsTable = StringUtils.isNotEmpty(sqlTree.getFromAsTable());
            // 处理FROM之前的SQL部分
            if (!isRootSql) {
                sqlBuilder.append(beforeFrom);
                if (hasMultipleChildren && hasFromAsTable) {
                    sqlBuilder.append("FROM( (");
                } else {
                    sqlBuilder.append("FROM(");
                }
            }
            // 递归处理子查询
            for (int i = 0; i < childs.size(); i++) {
                SQLTree childNode = childs.get(i);
                // 添加UNION类型（第一个子查询不需要）
                if (i > 0 && StringUtils.isNotEmpty(childNode.getUnionType())) {
                    sqlBuilder.append(" ").append(childNode.getUnionType());
                }
                if (hasMultipleChildren || StringUtils.isNotEmpty(childNode.getAsTable()) || hasFromAsTable) {
                    sqlBuilder.append(" (");
                }
                // 递归处理子节点
                if (CollectionUtils.isNotEmpty(childNode.getChilds())) {
                    Pair<String, Object[]> childPair = recurSql(childNode, new Pair<>("", new Object[]{}));
                    sqlBuilder.append(childPair.getFirst());
                    if (childPair.getSecond() != null) {
                        Collections.addAll(paramsList, childPair.getSecond());
                    }
                } else {
                    sqlBuilder.append(childNode.getSql());
                    if (childNode.getParams() != null) {
                        Collections.addAll(paramsList, childNode.getParams());
                    }
                }
                if (hasMultipleChildren || StringUtils.isNotEmpty(childNode.getAsTable()) || hasFromAsTable) {
                    sqlBuilder.append(")");
                }
                if (StringUtils.isNotEmpty(childNode.getAsTable())) {
                    sqlBuilder.append(" ").append(childNode.getAsTable());
                }
            }
            // 处理FROM之后的SQL部分
            if (!isRootSql) {
                if (hasMultipleChildren && hasFromAsTable) {
                    sqlBuilder.append(") ").append(sqlTree.getFromAsTable()).append(" )");
                } else if (hasFromAsTable) {
                    sqlBuilder.append(" ").append(sqlTree.getFromAsTable()).append(" )");
                } else {
                    sqlBuilder.append(")");
                }
                sqlBuilder.append(afterFrom);
            }
            // 添加当前节点的参数
            if (sqlTree.getParams() != null) {
                Collections.addAll(paramsList, sqlTree.getParams());
            }
        } else {
            // 叶子节点：直接添加SQL和参数
            sqlBuilder.append(sqlTree.getSql());
            if (sqlTree.getParams() != null) {
                Collections.addAll(paramsList, sqlTree.getParams());
            }
        }
        // 转换为数组返回
        return new Pair<>(sqlBuilder.toString(), paramsList.toArray());
    }
}
