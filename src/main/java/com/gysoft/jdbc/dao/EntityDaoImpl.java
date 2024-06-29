package com.gysoft.jdbc.dao;


import com.gysoft.jdbc.bean.ColumnMeta;
import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.FieldReference;
import com.gysoft.jdbc.bean.IndexMeta;
import com.gysoft.jdbc.bean.Page;
import com.gysoft.jdbc.bean.PageResult;
import com.gysoft.jdbc.bean.Pair;
import com.gysoft.jdbc.bean.Result;
import com.gysoft.jdbc.bean.SQL;
import com.gysoft.jdbc.bean.SQLInterceptor;
import com.gysoft.jdbc.bean.SQLType;
import com.gysoft.jdbc.bean.TableMeta;
import com.gysoft.jdbc.multi.DataSourceBind;
import com.gysoft.jdbc.multi.DataSourceBindHolder;
import com.gysoft.jdbc.multi.balance.LoadBalance;
import com.gysoft.jdbc.multi.balance.RoundRobinLoadBalance;
import com.gysoft.jdbc.tools.CollectionUtil;
import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 支持注解，若实体没有注解，实体类名需要按照驼峰命名，属性与数据库字段一致不区分大小写
 *
 * @author 周宁
 */
public class EntityDaoImpl<T, Id extends Serializable> implements EntityDao<T, Id> {

    private static final int BATCH_PAGE_SIZE = 2000;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private List<SQLInterceptor> sqlInterceptors;

    /**
     * 泛型
     */
    private Class<T> entityClass;

    /**
     * 表名
     */
    private String tableName;
    /**
     * 主键
     */
    private String primaryKey;

    @SuppressWarnings("rawtypes")
    private RowMapper<T> rowMapper;

    @SuppressWarnings("unchecked")
    public EntityDaoImpl() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) type.getActualTypeArguments()[0];
        tableName = EntityTools.getTableName(entityClass);
        primaryKey = EntityTools.getPk(entityClass);
        rowMapper = BeanPropertyRowMapper.newInstance(entityClass);

    }

    @Override
    public int save(T t) throws Exception {
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_INSERT);
        Object[] args = SqlMakeTools.setArgs(t, SQL_INSERT);
        int[] argTypes = SqlMakeTools.setArgTypes(t, SQL_INSERT);
        return jdbcTemplate.update(sql, args, argTypes);
    }

    @Override
    public int update(T t) throws Exception {
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_UPDATE);
        Object[] args = SqlMakeTools.setArgs(t, SQL_UPDATE);
        int[] argTypes = SqlMakeTools.setArgTypes(t, SQL_UPDATE);
        return jdbcTemplate.update(sql, args, argTypes);
    }

    @Override
    public void batchSave(List<T> list) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //分页操作
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_INSERT);
        int[] argTypes = SqlMakeTools.setArgTypes(list.get(0), SQL_INSERT);
        Integer j = 0;
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        for (int i = 0; i < list.size(); i++) {
            batchArgs.add(SqlMakeTools.setArgs(list.get(i), SQL_INSERT));
            j++;
            if (j.intValue() == BATCH_PAGE_SIZE) {
                jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);
                batchArgs = new ArrayList<>();
                j = 0;
            }
        }
        jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);
    }

    @Override
    public int saveAll(List<T> list) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        //分页操作
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_INSERT);
        int[] argTypes = SqlMakeTools.setArgTypes(list.get(0), SQL_INSERT);
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        for (int i = 0; i < list.size(); i++) {
            batchArgs.add(SqlMakeTools.setArgs(list.get(i), SQL_INSERT));
        }
        //将sql分为左右两部分
        int index = sql.indexOf("VALUES");
        index = sql.indexOf("(", index);
        //sql的左侧insert into
        String sqlLeft = sql.substring(0, index);
        //sql的右侧values
        String sqlRight = sql.substring(index);
        //分批次插入
        List<Object[]>[] batchArgsArr = CollectionUtil.slice(batchArgs, BATCH_PAGE_SIZE);
        //影响记录数量
        int resultSize = 0;
        for (List<Object[]> args : batchArgsArr) {
            //本批次的大小
            int batchSize = args.size();
            //插入语句
            StringBuilder insSql = new StringBuilder(sqlLeft);
            //参数
            List<Object> params = new ArrayList<>();
            //字段类型数组
            int[] types = new int[batchSize * argTypes.length];
            for (int i = 0; i < batchSize; i++) {
                for (int j = 0; j < argTypes.length; j++) {
                    types[i * argTypes.length + j] = argTypes[j];
                }
                insSql.append(sqlRight).append(",");
            }
            insSql.setLength(insSql.length() - 1);
            for (Object[] objs : args) {
                for (Object arg : objs) {
                    params.add(arg);
                }
            }
            resultSize = resultSize + jdbcTemplate.update(insSql.toString(), params.toArray(), types);
        }
        return resultSize;
    }

    @Override
    public void batchUpdate(List<T> list) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //分页操作
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_UPDATE);
        int[] argTypes = SqlMakeTools.setArgTypes(list.get(0), SQL_UPDATE);
        Integer j = 0;
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            batchArgs.add(SqlMakeTools.setArgs(list.get(i), SQL_UPDATE));
            j++;
            if (j.intValue() == BATCH_PAGE_SIZE) {
                jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);
                batchArgs = new ArrayList<>();
                j = 0;
            }
        }
        jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);
    }


    @SuppressWarnings("unchecked")
    @Override
    public T queryOne(Id id) throws Exception {
        return this.queryOne(id, rowMapper);
    }

    @Override
    public T queryOne(Id id, RowMapper<T> tRowMapper) throws Exception {
        String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?";
        List<T> result = jdbcTemplate.query(sql, tRowMapper, id);
        return DataAccessUtils.singleResult(result);
    }

    @Override
    public int delete(Id id) throws Exception {
        return this.batchDelete(Collections.singletonList(id));
    }

    @Override
    public int batchDelete(List<Id> ids) throws Exception {
        if (CollectionUtils.isNotEmpty(ids)) {
            StringBuilder sql = new StringBuilder();
            List<String> marks = ids.stream().map(s -> "?").collect(Collectors.toList());
            sql.append(" DELETE FROM " + tableName + " WHERE " + primaryKey + " in (");
            sql.append(String.join(",", marks));
            sql.append(")");
            return jdbcTemplate.update(sql.toString(), ids.toArray());
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> queryAll() throws Exception {
        return this.queryAll(rowMapper);
    }

    @Override
    public List<T> queryAll(RowMapper<T> tRowMapper) throws Exception {
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.query(sql, tRowMapper);
    }

    @Override
    public PageResult<T> pageQuery(Page page) throws Exception {
        return this.pageQueryWithCriteria(page, null);
    }

    @Override
    public PageResult<T> pageQuery(Page page, RowMapper<T> tRowMapper) throws Exception {
        return this.pageQueryWithCriteria(page, null, rowMapper);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception {
        return this.pageQueryWithCriteria(page, criteria, rowMapper);
    }

    @Override
    public PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria, RowMapper<T> tRowMapper) throws Exception {
        String sql = "SELECT * FROM " + tableName;
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
        sql = pair.getFirst();
        Object[] params = pair.getSecond();
        String pageSql = "SELECT SQL_CALC_FOUND_ROWS * FROM (" + sql + ") temp ";
        if (page != null) {
            pageSql = pageSql + " LIMIT ?,?";
            params = ArrayUtils.add(params, page.getOffset());
            params = ArrayUtils.add(params, page.getPageSize());
        }
        List<T> paged = jdbcTemplate.query(pageSql, params, tRowMapper);
        String countSql = "SELECT FOUND_ROWS() ";
        int count = jdbcTemplate.queryForObject(countSql, Integer.class);
        return new PageResult(paged, count);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> queryWithCriteria(Criteria criteria) throws Exception {
        return this.queryWithCriteria(criteria, rowMapper);
    }

    @Override
    public List<T> queryWithCriteria(Criteria criteria, RowMapper<T> tRowMapper) throws Exception {
        String sql = "SELECT * FROM " + tableName;
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), tRowMapper);
    }

    @Override
    public int deleteWithCriteria(Criteria criteria) throws Exception {
        if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
            throw new RuntimeException("不支持的操作!");
        }
        String sql = "delete FROM " + tableName;
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
        return jdbcTemplate.update(pair.getFirst(), pair.getSecond());
    }

    @Override
    public T queryOne(Criteria criteria) throws Exception {
        List<T> result = this.queryWithCriteria(criteria);
        return DataAccessUtils.singleResult(result);
    }

    @Override
    public T queryOne(Criteria criteria, RowMapper<T> tRowMapper) throws Exception {
        List<T> result = this.queryWithCriteria(criteria, tRowMapper);
        return DataAccessUtils.singleResult(result);
    }

    @Override
    public <E> Result<E> queryWithSql(Class<E> clss, SQL sql) throws Exception {
        doBeforeBuild(SQLType.Select, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return new Result<>(clss, pair.getFirst(), pair.getSecond(), jdbcTemplate, (s, objects) -> {
            try {
                EntityDaoImpl.this.doAfterBuild(s, objects);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public int updateWithSql(SQL sql) throws Exception {
        doBeforeBuild(SQLType.Update, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        doAfterBuild(pair.getFirst(), pair.getSecond());
        return jdbcTemplate.update(pair.getFirst(), pair.getSecond());
    }

    @Override
    public int deleteWithSql(SQL sql) throws Exception {
        doBeforeBuild(SQLType.Delete, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        doAfterBuild(pair.getFirst(), pair.getSecond());
        return jdbcTemplate.update(pair.getFirst(), pair.getSecond());
    }

    @Override
    public <K, V> Map<K, V> queryMapWithSql(SQL sql,
                                            ResultSetExtractor<Map<K, V>> resultSetExtractor)
            throws Exception {
        doBeforeBuild(SQLType.Select, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        doAfterBuild(pair.getFirst(), pair.getSecond());
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), resultSetExtractor);
    }

    @Override
    public List<Map<String, Object>> queryMapsWithSql(SQL sql) throws Exception {
        doBeforeBuild(SQLType.Select, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        doAfterBuild(pair.getFirst(), pair.getSecond());
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), new ColumnMapRowMapper());
    }

    @Override
    public Integer queryIntegerWithSql(SQL sql) throws Exception {
        doBeforeBuild(SQLType.Select, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        doAfterBuild(pair.getFirst(), pair.getSecond());
        return jdbcTemplate.queryForObject(pair.getFirst(), pair.getSecond(), Integer.class);
    }

    @Override
    public boolean existsWithSql(SQL sql) throws Exception {
        doBeforeBuild(SQLType.Select, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        doAfterBuild(pair.getFirst(), pair.getSecond());
        return jdbcTemplate.queryForObject(pair.getFirst(), pair.getSecond(), (rs, rowNum) -> rs.next());
    }

    @Override
    public int insertWithSql(SQL sql) throws Exception {
        //插入sql
        String selectTbName = sql.getTbName();
        sql.getModifier().changeTableName(sql.getInsert().getFirst());
        doBeforeBuild(SQLType.Insert, sql);
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        String insertSql = pair.getFirst();
        //待插入数据
        List<Object[]> params = sql.getInsertValues();
        List<Pair> kvs = sql.getKvs();
        int res = 0;
        if (CollectionUtils.isNotEmpty(params)) {
            List<Object[]>[] batchs = CollectionUtil.slice(params, BATCH_PAGE_SIZE);
            for (List<Object[]> batch : batchs) {
                List<Object> paramList = new ArrayList<>();
                StringBuilder tempInsertSql = new StringBuilder(insertSql);
                tempInsertSql.append(" VALUES ");
                for (Object[] param : batch) {
                    tempInsertSql.append("(");
                    for (Object obj : param) {
                        tempInsertSql.append("?,");
                        paramList.add(obj);
                    }
                    tempInsertSql.setLength(tempInsertSql.length() - 1);
                    tempInsertSql.append("),");
                }
                tempInsertSql.setLength(tempInsertSql.length() - 1);
                if (CollectionUtils.isNotEmpty(kvs)) {
                    tempInsertSql.append(" ON DUPLICATE KEY UPDATE ");
                    for (Pair p : kvs) {
                        if (p.getSecond() instanceof FieldReference) {
                            FieldReference fieldReference = (FieldReference) p.getSecond();
                            tempInsertSql.append(p.getFirst() + " = " + fieldReference.getField()
                                    + ", ");
                        } else {
                            tempInsertSql.append(p.getFirst() + " = ?, ");
                            paramList.add(p.getSecond());
                        }
                    }
                    tempInsertSql.setLength(tempInsertSql.length() - 2);
                }
                doAfterBuild(tempInsertSql.toString(), paramList.toArray());
                res += jdbcTemplate.update(tempInsertSql.toString(), paramList.toArray());
            }
        } else if (CollectionUtils.isNotEmpty(sql.getSelectFields())) {
            sql.getModifier().changeSqlType(EntityDao.SQL_SELECT);
            sql.getModifier().changeTableName(selectTbName);
            Pair<String, Object[]> p = SqlMakeTools.useSql(sql);
            insertSql += " " + p.getFirst();
            doAfterBuild(insertSql, p.getSecond());
            res = jdbcTemplate.update(insertSql, p.getSecond());
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createWithSql(SQL sql) throws Exception {
        TableMeta tableMeta = sql.getTableMeta();
        List<ColumnMeta> columns = tableMeta.getColumns();
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("未指定任何字段");
        }
        StringBuilder createSql = new StringBuilder();
        List<String> fileds = new ArrayList<>();
        //创建表
        String tbName = EntityTools.transferColumnName(StringUtils.isEmpty(tableMeta.getName()) ? "tmp_" + UUID.randomUUID().toString().toLowerCase().replace("-", "") : tableMeta.getName());
        sql.getModifier().changeTableName(tbName);
        doBeforeBuild(SQLType.Create, sql);
        createSql.append("CREATE ");
        if (tableMeta.isTemporary()) {
            createSql.append("TEMPORARY ");
        }
        createSql.append("TABLE ");
        if (tableMeta.isIfNotExists()) {
            createSql.append("IF NOT EXISTS ");
        }
        createSql.append(tbName);
        createSql.append("(");
        columns.forEach(columnMeta -> {
            createSql.append(EntityTools.transferColumnName(columnMeta.getName()));
            fileds.add(EntityTools.transferColumnName(columnMeta.getName()));
            createSql.append(columnMeta.getDataType());
            if (columnMeta.isNotNull()) {
                createSql.append(" not null");
            }
            if (columnMeta.isPrimaryKey()) {
                createSql.append(" primary key");
                if (columnMeta.isAutoIncr()) {
                    createSql.append(" auto_increment");
                }
            }
            if (columnMeta.getVal() != null) {
                if (columnMeta.getJdbcType().equals(JDBCType.TIMESTAMP)
                        || columnMeta.getVal().toLowerCase().equals("null")) {
                    createSql.append(String.format(" default %s", (columnMeta.getVal())));
                } else {
                    createSql.append(String.format(" default '%s'", (columnMeta.getVal())));
                }
            }
            if (StringUtils.isNotEmpty(columnMeta.getComment())) {
                createSql.append(String.format(" comment '%s'", columnMeta.getComment()));
            }
            createSql.append(",");
        });
        //索引
        List<IndexMeta> indexMetas = tableMeta.getIndexs();
        indexMetas.forEach(indexMeta -> {
            createSql.append((indexMeta.isUnique() ? "unique" : "") + " key" + (
                    indexMeta.getIndexName() == null ? EntityTools
                            .transferColumnName(indexMeta.getColumnNames().iterator().next())
                            : EntityTools.transferColumnName(indexMeta.getIndexName())) + "(");
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
        createSql.append(")ENGINE = " + tableMeta.getEngine() + " CHARSET=utf8 ");
        if (StringUtils.isNotEmpty(tableMeta.getComment())) {
            createSql.append("COMMENT=" + "'" + tableMeta.getComment() + "'");
        }
        doAfterBuild(createSql.toString(), new Object[]{});
        jdbcTemplate.execute(createSql.toString());
        //判断是否有数据需要插入
        if (CollectionUtils.isNotEmpty(sql.getInsertValues()) || CollectionUtils.isNotEmpty(sql.getSelectFields())) {
            sql.getModifier().changeSqlType(EntityDao.SQL_INSERT);
            sql.getInsert().setFirst(tbName);
            sql.getInsert().setSecond(fileds);
            insertWithSql(sql);
        }
        return tbName;
    }

    @Override
    public void drop() throws Exception {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        jdbcTemplate.execute(sql);
    }

    @Override
    public void truncate() throws Exception {
        String sql = "TRUNCATE TABLE " + tableName;
        jdbcTemplate.execute(sql);
    }

    @Override
    public void drunk(SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        if (sql.getSqlType().equals(EntityDao.SQL_TRUNCATE)) {
            doBeforeBuild(SQLType.Truncate, sql);
            String truncateSql = pair.getFirst();
            doAfterBuild(truncateSql, new Object[]{});
            jdbcTemplate.batchUpdate(truncateSql.split("\n"));
        } else if (sql.getSqlType().equals(EntityDao.SQL_DROP)) {
            doBeforeBuild(SQLType.Drop, sql);
            String dropSql = pair.getFirst();
            doAfterBuild(dropSql, new Object[]{});
            jdbcTemplate.execute(dropSql);
        } else {
            throw new RuntimeException("method drunk only support `DROP` AND `TRUNCATE`");
        }
    }

    @Override
    public EntityDaoImpl<T, Id> bindKey(String bindKey) throws Exception {
        DataSourceBindHolder.setDataSource(DataSourceBind.bindKey(bindKey));
        return this;
    }

    @Override
    public EntityDao<T, Id> bindGroup(String group, Class<? extends LoadBalance> loadBalance)
            throws Exception {
        DataSourceBindHolder.setDataSource(DataSourceBind.bindGroup(group, loadBalance));
        return this;
    }

    @Override
    public EntityDao<T, Id> bindGroup(String group) throws Exception {
        return bindGroup(group, RoundRobinLoadBalance.class);
    }

    public void doBeforeBuild(SQLType sqlType, SQL sql) throws Exception {
        if (CollectionUtils.isNotEmpty(sqlInterceptors)) {
            for (SQLInterceptor sqlInterceptor : sqlInterceptors) {
                sqlInterceptor.beforeBuild(sqlType, sql.getModifier());
            }
        }
    }

    public void doAfterBuild(String sql, Object[] args) throws Exception {
        if (CollectionUtils.isNotEmpty(sqlInterceptors)) {
            for (SQLInterceptor sqlInterceptor : sqlInterceptors) {
                sqlInterceptor.afterBuild(sql, args);
            }
        }
    }

}
