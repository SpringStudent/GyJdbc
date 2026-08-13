package com.gysoft.jdbc.tools;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.dao.EntityDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.*;
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
            String primaryKey = null;
            String setPrefix = " UPDATE " + tbName + " SET ";
            sql.append(setPrefix);
            for (int i = 0; fields != null && i < fields.length; i++) {
                String column = EntityTools.getColumnName(fields[i]);//获取属性对应字段名，没有注解默认按照属性名。有Column注解，获取Column的name作为字段名
                if (EntityTools.isPk(clazz, fields[i])) { // id 代表主键
                    primaryKey = column;
                    continue;
                }
                sql.append(column).append("=").append("?,");
            }
            if (primaryKey == null) {
                throw new GyjdbcException("Primary key field not found in entity " + clazz.getName());
            }
            if (sql.length() == setPrefix.length()) {
                throw new GyjdbcException("No non-primary-key fields to update in entity " + clazz.getName());
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
     * 将 INSERT 单行模板 SQL 拆分为「insert 前缀」与「列清单/values 后缀」两部分，
     * 供 {@link com.gysoft.jdbc.dao.EntityDaoImpl#saveAll} 拼接多行 VALUES 使用。
     * <p>以 " VALUES "（两侧带空格）定位关键字，避免表名/列名含 "VALUES" 子串时
     * indexOf 提前命中而切错位置。</p>
     *
     * @param insertSql makeSql 生成的 INSERT 模板，形如 " INSERT INTO tb (a,b) VALUES (?,?)"
     * @return first=insert 前缀（含 VALUES 关键字），second=以 "(" 开始的列清单部分
     */
    public static Pair<String, String> splitInsertSql(String insertSql) {
        int valuesIndex = insertSql.indexOf(" VALUES ");
        if (valuesIndex < 0) {
            throw new GyjdbcException("insert sql does not contain a VALUES clause: " + insertSql);
        }
        int columnParenIndex = insertSql.indexOf("(", valuesIndex);
        if (columnParenIndex < 0) {
            throw new GyjdbcException("insert sql does not contain a column list: " + insertSql);
        }
        return new Pair<>(insertSql.substring(0, columnParenIndex), insertSql.substring(columnParenIndex));
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
            Object primaryValue = null;
            boolean pkFound = false;
            int j = 0;
            for (int i = 0; fields != null && i < fields.length; i++) {
                try {
                    if (EntityTools.isPk(clzz, fields[i])) { // id 代表主键
                        primaryValue = fields[i].get(entity);
                        pkFound = true;
                        continue;
                    }
                    args[j] = fields[i].get(entity);
                    j++;
                } catch (Exception e) {
                    throw new GyjdbcException(e);
                }
            }
            if (!pkFound) {
                throw new GyjdbcException("Primary key field not found in entity " + clzz.getName());
            }
            args[args.length - 1] = primaryValue;
            return args;
        } else if (sqlFlag.equals(SQL_DELETE)) {
            Object primaryValue = null;
            boolean pkFound = false;
            for (int i = 0; fields != null && i < fields.length; i++) {
                try {
                    if (EntityTools.isPk(clzz, fields[i])) { // id 代表主键
                        primaryValue = fields[i].get(entity);
                        pkFound = true;
                        break;
                    }
                } catch (Exception e) {
                    throw new GyjdbcException(e);
                }
            }
            if (!pkFound) {
                throw new GyjdbcException("Primary key field not found in entity " + clzz.getName());
            }
            Object[] args = new Object[1]; // 长度是1
            args[0] = primaryValue;
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
                    argTypes[i] = MixUtils.getSqlType(fields[i].getType());
                }
            } catch (Exception e) {
                throw new GyjdbcException(e);
            }
            return argTypes;
        } else if (sqlFlag.equals(SQL_UPDATE)) {
            int[] argTypes = new int[fields.length];
            int primaryType = 0;
            boolean pkFound = false;
            try {
                int j = 0;
                for (int i = 0; i < fields.length; i++) {
                    if (EntityTools.isPk(entity.getClass(), fields[i])) { // id 代表主键
                        primaryType = MixUtils.getSqlType(fields[i].getType());
                        pkFound = true;
                        continue;
                    }
                    argTypes[j] = MixUtils.getSqlType(fields[i].getType());
                    j++;
                }
                if (!pkFound) {
                    throw new GyjdbcException("Primary key field not found in entity " + entity.getClass().getName());
                }
                argTypes[argTypes.length - 1] = primaryType;
            } catch (GyjdbcException e) {
                throw e;
            } catch (Exception e) {
                throw new GyjdbcException(e);
            }
            return argTypes;

        } else if (sqlFlag.equals(SQL_DELETE)) {
            int[] argTypes = new int[1]; // 长度是1
            boolean pkFound = false;
            try {
                for (Field field : fields) {
                    if (EntityTools.isPk(entity.getClass(), field)) { // id 代表主键
                        argTypes[0] = MixUtils.getSqlType(field.getType());
                        pkFound = true;
                        break;
                    }
                }
                if (!pkFound) {
                    throw new GyjdbcException("Primary key field not found in entity " + entity.getClass().getName());
                }
            } catch (GyjdbcException e) {
                throw e;
            } catch (Exception e) {
                throw new GyjdbcException(e);
            }
            return argTypes;
        }
        return null;
    }

    /**
     * 创建条件查询sql和入参（v3: 片段收集 + 统一拼接，消除 setLength 回退）
     */
    public static Pair<String, Object[]> doCriteria(AbstractCriteria criteria, StringBuilder sql) {
        List<Object> params = new ArrayList<>();
        if (null != criteria) {
            if (CollectionUtils.isNotEmpty(criteria.getWhereParams())) {
                List<ConditionSegment> segments = new ArrayList<>();
                List<CriteriaProxy> criteriaProxys = criteria.getCriteriaProxys();
                List<WhereParam> whereParams = criteria.getWhereParams();
                boolean hasProxies = CollectionUtils.isNotEmpty(criteriaProxys);
                for (int whereIdx = 0; whereIdx <= whereParams.size(); whereIdx++) {
                    // 在当前位置插入匹配的代理片段（仅一段逻辑覆盖循环内和循环后）
                    if (hasProxies) {
                        for (CriteriaProxy proxy : criteriaProxys) {
                            if (proxy.getWhereParamsIndex() - 1 == whereIdx && proxy.getConnectorType() != null) {
                                String s = proxy.getSql().toString();
                                if (proxy.getConnectorType().isWrapInParens()) {
                                    s = "(" + s + ")";
                                }
                                segments.add(new ConditionSegment(s, proxy.getParams(), proxy.getConnectorType()));
                            }
                        }
                    }
                    if (whereIdx == whereParams.size()) {
                        break;
                    }
                    WhereParam wp = whereParams.get(whereIdx);
                    if (StringUtils.isEmpty(wp.getKey())) {
                        continue;
                    }
                    // 将 WhereParam 转为 ConditionSegment
                    String rawKey = wp.getKey();
                    ConnectorType connector;
                    String key;
                    if (rawKey.startsWith(" OR ")) {
                        connector = ConnectorType.WHERE_OR;
                        key = rawKey.substring(4);
                    } else {
                        connector = null;
                        key = rawKey;
                    }
                    List<Object> wpParams = new ArrayList<>();
                    StringBuilder wpSql = new StringBuilder();
                    wpSql.append(key).append(" ");
                    String opt = wp.getOpt();
                    Object value = wp.getValue();
                    if ("IN".equals(opt.toUpperCase()) || "NOT IN".equals(opt.toUpperCase())) {
                        wpSql.append(opt).append("(");
                        if (value instanceof Collection) {
                            if (CollectionUtils.isNotEmpty(((Collection) value))) {
                                Iterator iterator = ((Collection) value).iterator();
                                while (iterator.hasNext()) {
                                    wpParams.add(iterator.next());
                                    wpSql.append("?,");
                                }
                                wpSql.setLength(wpSql.length() - 1);
                            } else {
                                throw new GyjdbcException("in condition collection cannot be empty");
                            }
                        } else if (value instanceof SQL) {
                            SQL inSql = (SQL) value;
                            Pair<String, Object[]> inPair = useSql(inSql);
                            wpSql.append(inPair.getFirst());
                            MixUtils.addAll(wpParams, inPair.getSecond());
                        } else {
                            wpSql.append("?");
                            wpParams.add(value);
                        }
                        wpSql.append(')');
                    } else if ("IS".equals(opt.toUpperCase())) {
                        wpSql.append(opt).append(" ").append(value);
                    } else if ("BETWEEN ? AND ?".equals(opt.toUpperCase())) {
                        wpSql.append(opt).append(" ");
                        Pair<Object, Object> pair = (Pair<Object, Object>) value;
                        wpParams.add(pair.getFirst());
                        wpParams.add(pair.getSecond());
                    } else {
                        if (value instanceof FieldReference) {
                            wpSql.append(opt).append(" ").append(((FieldReference) value).getField());
                        } else if (value instanceof SQL) {
                            SQL whereSql = (SQL) value;
                            Pair<String, Object[]> wherePair = useSql(whereSql);
                            wpSql.append(opt).append('(').append(wherePair.getFirst()).append(')');
                            MixUtils.addAll(wpParams, wherePair.getSecond());
                        } else if (key.startsWith("(") && value != null && (value instanceof Collection || value.getClass().isArray())) {
                            // 元组比较值列表：(a,b) = (?,?)
                            Object[] vals = value instanceof Collection
                                    ? ((Collection<?>) value).toArray()
                                    : MixUtils.toObjectArray(value);
                            if (vals.length == 0) {
                                throw new GyjdbcException("tuple comparison value cannot be empty");
                            }
                            wpSql.append(opt).append(" (");
                            for (Object v : vals) {
                                wpParams.add(v);
                                wpSql.append("?,");
                            }
                            wpSql.setLength(wpSql.length() - 1);
                            wpSql.append(")");
                        } else {
                            wpSql.append(opt).append(" ?");
                            wpParams.add(value);
                        }
                    }
                    segments.add(new ConditionSegment(wpSql.toString(), wpParams.toArray(), connector));
                }
                //组装where条件
                if (!segments.isEmpty()) {
                    sql.append(" WHERE ");
                    for (int i = 0; i < segments.size(); i++) {
                        ConditionSegment seg = segments.get(i);
                        if (i > 0) {
                            ConnectorType ct = seg.getConnector();
                            sql.append(" ").append(ct != null ? ct.getKeyword() : "AND").append(" ");
                        }
                        sql.append(seg.getSql());
                        if (seg.getParams() != null) {
                            Collections.addAll(params, seg.getParams());
                        }
                    }
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
                sql.append(" ").append("HAVING").append(" ").append(having.getFirst());
                MixUtils.addAll(params, having.getSecond());
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
            //组装limit条件
            if (criteria.getOffset() >= 0) {
                sql.append(" LIMIT ?");
                params.add(criteria.getOffset());
                if (criteria.getSize() > 0) {
                    sql.append(", ?");
                    params.add(criteria.getSize());
                }
            } else if (criteria.getSize() > 0) {
                // 仅指定行数（单参 limit(n)）：LIMIT n
                sql.append(" LIMIT ?");
                params.add(criteria.getSize());
            }
        }
        return new Pair<>(sql.toString(), params.toArray());
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
            SQL subSql = subSqls.get(i);
            Pair<String, Object[]> pair = doSql(subSql);
            SQLTree cTree = new SQLTree(pair.getFirst(), pair.getSecond(), new ArrayList<>(), UUID.randomUUID().toString().replace("-", ""), subSql.getUnionType(), subSql.getAsTable(), subSql.getFromAsTable());
            if (SQL_SELECT.equals(subSql.getSqlType())) {
                Pair<Integer, Integer> metadata = getFromMetadata(subSql);
                cTree.setFromIndex(metadata.getFirst());
                cTree.setParamsBeforeFrom(metadata.getSecond());
            }
            //加入子查询列表
            sqlTree.getChilds().add(cTree);
            buildSQLTree(subSql, cTree);
        }
    }

    /**
     * 获取sql的from下表位置和from之前的参数下标
     *
     * @param sqlObj sql对象的封装
     * @author 周宁
     * @version 1.0
     */
    private static Pair<Integer, Integer> getFromMetadata(SQL sqlObj) {
        int selectLength = sqlObj.isDistinctSelect() ? "SELECT DISTINCT ".length() : "SELECT ".length();
        int paramsBeforeFrom = 0;
        if (CollectionUtils.isNotEmpty(sqlObj.getSelectFields())) {
            for (Object field : sqlObj.getSelectFields()) {
                if (field instanceof ValueReference) {
                    selectLength += "?, ".length();
                    paramsBeforeFrom++;
                } else if (field instanceof SQL) {
                    Pair<String, Object[]> pair = useSql((SQL) field);
                    selectLength += pair.getFirst().length() + (pair.getFirst().startsWith("(") ? 0 : 2) + 2;
                    paramsBeforeFrom += pair.getSecond().length;
                } else {
                    selectLength += field.toString().length() + 2;
                }
            }
            selectLength -= 2;
        }
        return new Pair<>(selectLength + 1, paramsBeforeFrom);
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
            if (CollectionUtils.isEmpty(sqlObj.getSelectFields())) {
                throw new GyjdbcException("select fields cannot be empty");
            }
            if (sqlObj.getSelectFields().contains(null)) {
                throw new GyjdbcException("select fields cannot contain null");
            }
            sql.append(sqlObj.isDistinctSelect() ? "SELECT DISTINCT " : "SELECT ");
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
                    MixUtils.addAll(params, temp.getSecond());
                } else {
                    sql.append(obj.toString() + ", ");
                }
            }
            sql.setLength(sql.length() - 2);
            // 存在 FROM 来源(表名或 from 子查询)才拼接 FROM,支持 SELECT 1 这类无表查询(MySQL 合法语法)
            if (StringUtils.isNotEmpty(sqlObj.getTbName()) || CollectionUtils.isNotEmpty(sqlObj.getSubSqls())) {
                sql.append(" FROM ");
                if (StringUtils.isNotEmpty(sqlObj.getTbName())) {
                    sql.append(sqlObj.getTbName());
                }
                if (StringUtils.isNotEmpty(sqlObj.getAliasName())) {
                    sql.append(" " + sqlObj.getAliasName());
                }
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
            boolean hasAutoIncrField = false;
            for (ColumnMeta columnMeta : columns) {
                createSql.append(EntityTools.transferColumnName(columnMeta.getName()));
                createSql.append(" ").append(columnMeta.getDataType());
                if (columnMeta.isNotNull()) {
                    createSql.append(" NOT NULL");
                }
                if (columnMeta.isPrimaryKey()) {
                    createSql.append(" PRIMARY KEY");
                    if (columnMeta.isAutoIncr()) {
                        createSql.append(" AUTO_INCREMENT");
                        hasAutoIncrField = true;
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
            }
            List<IndexMeta> indexMetas = tableMeta.getIndexs();
            for (IndexMeta indexMeta : indexMetas) {
                createSql.append((indexMeta.isUnique() ? "UNIQUE" : "") + " KEY " + (indexMeta.getIndexName() == null ? EntityTools.transferColumnName("ix_" + indexMeta.getColumnNames().stream().map(cName -> EntityTools.transferFieldName(cName)).collect(Collectors.joining("_"))) : EntityTools.transferColumnName(indexMeta.getIndexName())) + " (");
                for (String cc : indexMeta.getColumnNames()) {
                    createSql.append(EntityTools.transferColumnName(cc));
                    createSql.append(",");
                }
                createSql.setLength(createSql.length() - 1);
                createSql.append(")");
                if (StringUtils.isNotEmpty(indexMeta.getIndexType())) {
                    createSql.append(" ").append(indexMeta.getIndexType());
                }
                if (StringUtils.isNotEmpty(indexMeta.getComment())) {
                    createSql.append(" COMMENT '").append(indexMeta.getComment()).append("'");
                }
                createSql.append(",");
            }
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
            if (tableMeta.getAutoIncrement() != null && hasAutoIncrField) {
                createSql.append(" AUTO_INCREMENT=").append(tableMeta.getAutoIncrement());
            }
            if (tableMeta.getRowFormat() != null) {
                createSql.append(" ROW_FORMAT=").append(tableMeta.getRowFormat());
            }
            if (StringUtils.isNotEmpty(tableMeta.getComment())) {
                createSql.append(" COMMENT=" + "'" + tableMeta.getComment() + "'");
            }
            return new Pair<>(createSql.toString(), new Object[]{tbName});
        }
        //连接查询sql组装
        if (CollectionUtils.isNotEmpty(sqlObj.getJoins())) {
            List<Joins.BaseJoin> joins = sqlObj.getJoins();
            for (Joins.BaseJoin join : joins) {
                sql.append(join.getJoinSql());
                List<CriteriaProxy> criteriaProxies = join.getCriteriaProxys();
                if (CollectionUtils.isNotEmpty(criteriaProxies)) {
                    for (CriteriaProxy proxy : criteriaProxies) {
                        if (proxy.getWhereParamsIndex() != -1) {
                            continue;
                        }
                        ConnectorType type = proxy.getConnectorType();
                        if (type == null) {
                            MixUtils.addAll(params, proxy.getParams());
                            continue;
                        }
                        sql.append(" ").append(type.getKeyword()).append(" ");
                        sql.append(proxy.getSql());
                        MixUtils.addAll(params, proxy.getParams());
                    }
                }
            }
        }
        //update字段拼接
        if (sqlObj.getSqlType().equals(EntityDao.SQL_UPDATE)) {
            List<Pair> kvs = sqlObj.getKvs();
            if (CollectionUtils.isEmpty(kvs)) {
                throw new GyjdbcException("update fields cannot be empty");
            }
            sql.append(" SET ");
            for (int i = 0; i < kvs.size(); i++) {
                Pair p = kvs.get(i);
                if (p.getSecond() instanceof FieldReference) {
                    FieldReference fieldReference = (FieldReference) p.getSecond();
                    sql.append(p.getFirst() + " = " + fieldReference.getField() + ", ");
                } else if (p.getSecond() instanceof SQL) {
                    Pair<String, Object[]> updatePair = useSql((SQL) p.getSecond());
                    sql.append(p.getFirst() + " = (" + updatePair.getFirst() + "), ");
                    MixUtils.addAll(params, updatePair.getSecond());
                } else {
                    sql.append(p.getFirst() + " = ?, ");
                    params.add(p.getSecond());
                }
            }
            sql.setLength(sql.length() - 2);
        }
        //组装条件
        pair = doCriteria(sqlObj, sql);
        MixUtils.addAll(params, pair.getSecond());
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
            int fromIndex = sqlTree.getFromIndex();
            String beforeFrom = fromIndex >= 0 ? parentSql.substring(0, fromIndex) : parentSql;
            String afterFrom = fromIndex >= 0 && fromIndex + 4 < parentSql.length() ? parentSql.substring(fromIndex + 4) : "";
            Object[] parentParams = sqlTree.getParams();
            int paramsBeforeFrom = Math.min(sqlTree.getParamsBeforeFrom(), parentParams == null ? 0 : parentParams.length);
            if (parentParams != null) {
                for (int i = 0; i < paramsBeforeFrom; i++) {
                    paramsList.add(parentParams[i]);
                }
            }
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
            if (parentParams != null) {
                for (int i = paramsBeforeFrom; i < parentParams.length; i++) {
                    paramsList.add(parentParams[i]);
                }
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
