package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.tools.CollectionUtil;
import com.gysoft.jdbc.tools.EntityTools;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author 周宁
 */
public class SQL extends AbstractCriteria<SQL> {
    /**
     * sql类型
     */
    private String sqlType;
    /**
     * 表名称
     */
    private String tbName;
    /**
     * 表的别名
     */
    private String aliasName;
    /**
     * 将sql作为表的别名
     */
    private String asTable;
    /**
     * 标识从from(SQL c,String fromAsTable)
     */
    private String fromAsTable;
    /**
     * 删除语句中表的别名
     */
    private String deleteAliasName;
    /**
     * 连接
     */
    private List<Joins.BaseJoin> joins;
    /**
     * 子查询条件
     */
    private List<SQL> subSqls;
    /**
     * sql管道拼接
     */
    private SQLPiepline sqlPiepline;
    /**
     * 表元数据
     */
    private TableMeta tableMeta;
    /**
     * 被查询的字段
     */
    private List<Object> selectFields;
    /**
     * 插入数据
     */
    private List<Object[]> insertValues;

    /**
     * 插入sql
     */
    private Pair<String, List<String>> insert;

    /**
     * 待更新的字段和相应的值
     */
    private List<Pair> kvs;
    /**
     * 连接类型
     */
    private String unionType;

    /**
     * 喝醉了的，代表人很糊涂删除表或者清楚数据
     */
    private Drunk drunk;

    private SqlModifier sqlModifier;

    private String id;

    public SQL() {
        this(null);
    }

    public SQL(String id) {
        sqlModifier = new SqlModifier(this);
        sqlPiepline = new SQLPiepline(this);
        selectFields = new ArrayList<>();
        kvs = new ArrayList<>();
        joins = new ArrayList<>();
        subSqls = new ArrayList<>();
        insertValues = new ArrayList<>();
        insert = new Pair<>();
        this.id = id;
    }

    public SQL from(SQL... cc) {
        for (SQL c : cc) {
            c.getSqlPiepline().getSqlNexts().forEach(sqlNext -> {
                SQL s = sqlNext.getSql();
                if (sqlNext.getUnionType() != null) {
                    s.setUnionType(sqlNext.getUnionType());
                } else {
                    s.setUnionType(",");
                }
                subSqls.add(s);
            });
        }
        return this;
    }

    public SQL from(SQL c, String fromAsTable) {
        this.fromAsTable = fromAsTable;
        c.getSqlPiepline().getSqlNexts().forEach(sqlNext -> {
            SQL s = sqlNext.getSql();
            if (sqlNext.getUnionType() != null) {
                s.setUnionType(sqlNext.getUnionType());
            } else {
                s.setUnionType(",");
            }
            subSqls.add(s);
        });
        return this;
    }

    public SQL union() {
        SQL next = new SQL();
        sqlPiepline.add(next, "UNION");
        next.setSqlPiepline(sqlPiepline);
        return next;
    }

    public SQL unionAll() {
        SQL next = new SQL();
        sqlPiepline.add(next, "UNION ALL");
        next.setSqlPiepline(sqlPiepline);
        return next;
    }

    public SQL select(Object... fields) {
        selectFields.addAll(Arrays.stream(fields).collect(Collectors.toList()));
        //复合查询insert select语法BUG修复
        if (this.sqlType == null) {
            this.sqlType = EntityDao.SQL_SELECT;
        }
        return this;
    }

    public <T, R> SQL select(TypeFunction<T, R>... functions) {
        selectFields.addAll(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()));
        //复合查询insert select语法BUG修复
        if (this.sqlType == null) {
            this.sqlType = EntityDao.SQL_SELECT;
        }
        return this;
    }

    public SQL update(String table) {
        this.tbName = table;
        this.sqlType = EntityDao.SQL_UPDATE;
        return this;
    }

    public SQL update(String table, String aliasName) {
        this.aliasName = aliasName;
        return update(table);
    }

    public SQL update(Class clss) {
        this.tbName = EntityTools.getTableName(clss);
        this.sqlType = EntityDao.SQL_UPDATE;
        return this;
    }

    public SQL update(Class clss, String aliasName) {
        this.aliasName = aliasName;
        return update(EntityTools.getTableName(clss));
    }

    public SQL delete(String deleteAliasName) {
        this.deleteAliasName = deleteAliasName;
        this.sqlType = EntityDao.SQL_DELETE;
        return this;
    }

    public SQL delete(String... deleteAliasNames) {
        this.deleteAliasName = Arrays.stream(deleteAliasNames).collect(Collectors.joining(","));
        this.sqlType = EntityDao.SQL_DELETE;
        return this;
    }

    public SQL delete() {
        this.sqlType = EntityDao.SQL_DELETE;
        return this;
    }

    public SQL insertInto(String table, String... fields) {
        insert.setFirst(table);
        insert.setSecond(Arrays.stream(fields).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_INSERT;
        return this;
    }

    public SQL replaceInto(String table, String... fields) {
        insert.setFirst(table);
        insert.setSecond(Arrays.stream(fields).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_REPLACE;
        return this;
    }

    public SQL insertIgnoreInto(String table, String... fields) {
        insert.setFirst(table);
        insert.setSecond(Arrays.stream(fields).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_INSERTIGNORE;
        return this;
    }


    public <T, R> SQL insertInto(String table, TypeFunction<T, R>... functions) {
        insert.setFirst(table);
        insert.setSecond(Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_INSERT;

        return this;
    }

    public <T, R> SQL replaceInto(String table, TypeFunction<T, R>... functions) {
        insert.setFirst(table);
        insert.setSecond(Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_REPLACE;
        return this;
    }


    public <T, R> SQL insertIgnoreInto(String table, TypeFunction<T, R>... functions) {
        insert.setFirst(table);
        insert.setSecond(Arrays.stream(functions).map(f -> TypeFunction.getLambdaColumnName(f)).collect(Collectors.toList()));
        this.sqlType = EntityDao.SQL_INSERTIGNORE;
        return this;
    }


    public SQL insertInto(String table) {
        insert.setFirst(table);
        this.sqlType = EntityDao.SQL_INSERT;
        return this;
    }

    public SQL replaceInto(String table) {
        insert.setFirst(table);
        this.sqlType = EntityDao.SQL_REPLACE;
        return this;
    }


    public SQL insertIgnoreInto(String table) {
        insert.setFirst(table);
        this.sqlType = EntityDao.SQL_INSERTIGNORE;
        return this;
    }

    public <T, R> SQL insertInto(Class clss, String... fields) {
        return insertInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL replaceInto(Class clss, String... fields) {
        return replaceInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL insertInto(Class clss, TypeFunction<T, R>... functions) {
        return insertInto(EntityTools.getTableName(clss), functions);
    }

    public <T, R> SQL replaceInto(Class clss, TypeFunction<T, R>... functions) {
        return replaceInto(EntityTools.getTableName(clss), functions);
    }

    public <T, R> SQL insertInto(Class clss) {
        return insertInto(EntityTools.getTableName(clss));
    }

    public <T, R> SQL replaceInto(Class clss) {
        return replaceInto(EntityTools.getTableName(clss));
    }

    public <T, R> SQL insertIgnoreInto(Class clss, String... fields) {
        return insertIgnoreInto(EntityTools.getTableName(clss), fields);
    }

    public <T, R> SQL insertIgnoreInto(Class clss, TypeFunction<T, R>... functions) {
        return insertIgnoreInto(EntityTools.getTableName(clss), functions);
    }

    public <T, R> SQL insertIgnoreInto(Class clss) {
        return insertIgnoreInto(EntityTools.getTableName(clss));
    }

    public SQL truncate() {
        sqlType = EntityDao.SQL_TRUNCATE;
        drunk = new Drunk();
        return this;
    }

    public SQL table(String... tables) {
        drunk.setTables(Arrays.stream(tables).collect(Collectors.toSet()));
        return this;
    }

    public SQL table(Class... clss) {
        drunk.setTables(Arrays.stream(clss).map(EntityTools::getTableName).collect(Collectors.toSet()));
        return this;
    }

    public SQL drop() {
        sqlType = EntityDao.SQL_DROP;
        drunk = new Drunk();
        return this;
    }

    public SQL ifExists() {
        drunk.setIfExists(true);
        return this;
    }

    public SQL set(String key, Object value) {
        kvs.add(new Pair(key, value));
        return this;
    }

    public <T, R> SQL set(TypeFunction<T, R> function, Object value) {
        kvs.add(new Pair(TypeFunction.getLambdaColumnName(function), value));
        return this;
    }

    public SQL as(String aliasName) {
        this.aliasName = aliasName;
        return this;
    }

    public SQL asTable(String aliasName) {
        this.asTable = aliasName;
        return this;
    }

    public SQL from(Class clss) {
        tbName = EntityTools.getTableName(clss);
        return this;
    }

    public SQL from(String tbName) {
        this.tbName = tbName;
        return this;
    }

    public SQL from(String tbName, String aliasName) {
        this.tbName = tbName;
        return as(aliasName);
    }

    public SQL from(Class clss, String aliasName) {
        tbName = EntityTools.getTableName(clss);
        return as(aliasName);
    }

    public SQL leftJoin(Joins.On on) {
        on.setJoinType(JoinType.LeftJoin);
        return join(on);
    }

    public SQL rightJoin(Joins.On on) {
        on.setJoinType(JoinType.RightJoin);
        return join(on);
    }

    public SQL innerJoin(Joins.On on) {
        on.setJoinType(JoinType.InnerJoin);
        return join(on);
    }


    public SQL natureJoin(Joins.BaseJoin as) {
        as.setJoinType(JoinType.NatureJoin);
        joins.add(as);
        return this;
    }

    private SQL join(Joins.On join) {
        joins.add(join);
        return this;
    }

    public SQL join(JoinType joinType, Object table, String aliasName) {
        Joins.As as = null;
        if (table instanceof Class) {
            as = new Joins().with((Class) table).as(aliasName);
        } else if (table instanceof String) {
            as = new Joins().with((String) table).as(aliasName);
        } else if (table instanceof SQL) {
            as = new Joins().with((SQL) table).as(aliasName);
        }
        as.setJoinType(joinType);
        joins.add(as);
        return this;
    }

    public SQL join(JoinType joinType, Object table) {
        Joins.With with = null;
        if (table instanceof Class) {
            with = new Joins().with((Class) table);
        } else if (table instanceof String) {
            with = new Joins().with((String) table);
        } else if (table instanceof SQL) {
            with = new Joins().with((SQL) table);
        }
        with.setJoinType(joinType);
        joins.add(with);
        return this;
    }

    public SQL leftJoin(Object table, String aliasName) {
        return join(JoinType.LeftJoin, table, aliasName);
    }

    public SQL leftJoin(Object table) {
        return join(JoinType.LeftJoin, table);
    }

    public SQL rightJoin(Object table, String aliasName) {
        return join(JoinType.RightJoin, table, aliasName);
    }

    public SQL rightJoin(Object table) {
        return join(JoinType.RightJoin, table);
    }

    public SQL innerJoin(Object table, String aliasName) {
        return join(JoinType.InnerJoin, table, aliasName);
    }

    public SQL innerJoin(Object table) {
        return join(JoinType.InnerJoin, table);
    }

    public SQL natureJoin(Object table, String aliasName) {
        return join(JoinType.NatureJoin, table, aliasName);
    }

    public SQL natureJoin(Object table) {
        return join(JoinType.NatureJoin, table);
    }

    public SQL on(String field, String field2) {
        Object obj = joins.get(joins.size() - 1);
        if (obj instanceof Joins.With) {
            joins.remove(obj);
            Joins.On on = ((Joins.With) obj).on(field, field2);
            joins.add(on);
        } else if (obj instanceof Joins.As) {
            joins.remove(obj);
            Joins.On on = ((Joins.As) obj).on(field, field2);
            joins.add(on);
        } else {
            ((Joins.On) obj).on(field, field2);
        }
        return this;
    }

    public SQL on(String field, String opt, Object field2) {
        Object as = joins.get(joins.size() - 1);
        if (as instanceof Joins.On) {
            ((Joins.On) as).and(field, opt, field2);
        } else {
            throw new RuntimeException("the sql has no join condition");
        }
        return this;
    }

    public SQL values(Object... values) {
        insertValues.add(values);
        return this;
    }

    public SQL values(List<Object[]> values) {
        insertValues.addAll(values);
        return this;
    }

    public Table create() {
        this.sqlType = EntityDao.SQL_CREATE;
        return new Table(this);
    }

    public SQL onDuplicateKeyUpdate(String key, Object value) {
        kvs.add(new Pair(key, value));
        return this;
    }

    public <T, R> SQL onDuplicateKeyUpdate(TypeFunction<T, R> function, Object value) {
        kvs.add(new Pair(TypeFunction.getLambdaColumnName(function), value));
        return this;
    }

    void setTableMeta(TableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }

    void setInsertValues(List<Object[]> insertValues) {
        this.insertValues = insertValues;
    }

    void setTbName(String tbName) {
        this.tbName = tbName;
    }

    void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    void setSqlPiepline(SQLPiepline sqlPiepline) {
        this.sqlPiepline = sqlPiepline;
    }

    void setUnionType(String unionType) {
        this.unionType = unionType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public String getTbName() {
        return tbName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public String getAsTable() {
        return asTable;
    }

    public String getFromAsTable() {
        return fromAsTable;
    }

    public String getDeleteAliasName() {
        return deleteAliasName;
    }

    public List<Joins.BaseJoin> getJoins() {
        return joins;
    }

    public List<SQL> getSubSqls() {
        return subSqls;
    }

    public SQLPiepline getSqlPiepline() {
        return sqlPiepline;
    }

    public TableMeta getTableMeta() {
        return tableMeta;
    }

    public List<Object> getSelectFields() {
        return selectFields;
    }

    public List<Object[]> getInsertValues() {
        return insertValues;
    }

    public Pair<String, List<String>> getInsert() {
        return insert;
    }

    public List<Pair> getKvs() {
        return kvs;
    }

    public String getUnionType() {
        return unionType;
    }

    public Drunk getDrunk() {
        return drunk;
    }

    public SqlModifier getModifier() {
        return sqlModifier;
    }

    public String getId() {
        if (StringUtils.isNotEmpty(id)) {
            return id;
        } else {
            return System.currentTimeMillis() + ":" + sqlModifier.sqlType() + ":" + sqlModifier.tableName();
        }
    }
}
