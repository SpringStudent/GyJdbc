package com.gysoft.jdbc.dao;


import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.multi.DataSourceBind;
import com.gysoft.jdbc.multi.DataSourceBindHolder;
import com.gysoft.jdbc.multi.balance.LoadBalance;
import com.gysoft.jdbc.multi.balance.RoundRobinLoadBalance;
import com.gysoft.jdbc.tools.CollectionUtil;
import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
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
    public void saveOrUpdate(T t) throws Exception {
        Field field = ReflectionUtils.findField(entityClass, primaryKey);
        field.setAccessible(true);
        Id id = (Id) ReflectionUtils.getField(field, t);
        if (id == null) {
            throw new GyjdbcException("entity primary key must be not null");
        }
        if (this.queryOne(id) == null) {
            this.save(t);
        } else {
            this.update(t);
        }
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
        return this.pageQueryWithCriteria(page, null, tRowMapper);
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
            params = appendParams(params, page.getOffset(), page.getPageSize());
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
    public boolean existsWithCriteria(Criteria criteria) throws Exception {
        String sql = "SELECT 1 FROM " + tableName;
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
        String existsSql = pair.getFirst();
        Object[] params = pair.getSecond();
        if (!existsSql.toUpperCase().contains(" LIMIT ")) {
            existsSql = existsSql + " LIMIT ?";
            params = appendParams(params, 1);
        }
        List<Object> results = jdbcTemplate.query(existsSql, params, (rs, rowNum) -> rs.getObject(1));
        return !results.isEmpty();
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
            throw new GyjdbcException("不支持的操作!");
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
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return new Result<>(clss, pair.getFirst(), pair.getSecond(), jdbcTemplate);
    }

    @Override
    public int updateWithSql(SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return jdbcTemplate.update(pair.getFirst(), pair.getSecond());
    }

    @Override
    public int deleteWithSql(SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return jdbcTemplate.update(pair.getFirst(), pair.getSecond());
    }

    @Override
    public <K, V> Map<K, V> queryMapWithSql(SQL sql,
                                            ResultSetExtractor<Map<K, V>> resultSetExtractor)
            throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), resultSetExtractor);
    }

    @Override
    public List<Map<String, Object>> queryMapsWithSql(SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), new ColumnMapRowMapper());
    }

    @Override
    public Integer queryIntegerWithSql(SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return jdbcTemplate.queryForObject(pair.getFirst(), pair.getSecond(), Integer.class);
    }

    @Override
    public boolean existsWithSql(SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        String existsSql = "SELECT 1 FROM (" + pair.getFirst() + ") gy_exists LIMIT ?";
        Object[] params = appendParams(pair.getSecond(), 1);
        List<Object> results = jdbcTemplate.query(existsSql, params, (rs, rowNum) -> rs.getObject(1));
        return !results.isEmpty();
    }

    private Object[] appendParams(Object[] params, Object... extraParams) {
        List<Object> result = new ArrayList<>();
        if (params != null) {
            Collections.addAll(result, params);
        }
        Collections.addAll(result, extraParams);
        return result.toArray();
    }

    @Override
    public int insertWithSql(SQL sql) throws Exception {
        String originTbName = sql.tableName();
        String originSqlType = sql.getSqlType();
        try {
            sql.changeTableName(sql.getInsert().getFirst());
            Pair<String, Object[]> baseInsertPair = SqlMakeTools.useSql(sql);
            String baseInsertSql = baseInsertPair.getFirst();
            List<Object[]> params = sql.getInsertValues();
            List<Pair> kvs = sql.getKvs();
            int res = 0;
            if (CollectionUtils.isNotEmpty(params)) {
                int colCount = params.get(0).length;
                String rowPlaceholder = "(" + String.join(",", Collections.nCopies(colCount, "?")) + ")";
                List<Object[]>[] batchs = CollectionUtil.slice(params, BATCH_PAGE_SIZE);
                for (List<Object[]> batch : batchs) {
                    List<Object> paramList = new ArrayList<>();
                    StringBuilder tempInsertSql = new StringBuilder(baseInsertSql);
                    tempInsertSql.append(" VALUES ");
                    // APPEND (?,?)
                    for (int i = 0; i < batch.size(); i++) {
                        Object[] param = batch.get(i);
                        if (param.length != colCount) {
                            throw new GyjdbcException("Param length not match");
                        }
                        tempInsertSql.append(rowPlaceholder);
                        if (i < batch.size() - 1) {
                            tempInsertSql.append(",");
                        }
                        Collections.addAll(paramList, param);
                    }
                    // ON DUPLICATE KEY UPDATE
                    if (CollectionUtils.isNotEmpty(kvs)) {
                        tempInsertSql.append(" ON DUPLICATE KEY UPDATE ");
                        for (int i = 0; i < kvs.size(); i++) {
                            Pair p = kvs.get(i);
                            if (p.getSecond() instanceof FieldReference) {
                                FieldReference fieldRef = (FieldReference) p.getSecond();
                                tempInsertSql.append(p.getFirst()).append(" = ").append(fieldRef.getField());
                            } else {
                                tempInsertSql.append(p.getFirst()).append(" = ?");
                                paramList.add(p.getSecond());
                            }
                            if (i < kvs.size() - 1) {
                                tempInsertSql.append(", ");
                            }
                        }
                    }
                    Object[] finalParams = paramList.toArray();
                    res += jdbcTemplate.update(tempInsertSql.toString(), finalParams);
                }
            } else if (CollectionUtils.isNotEmpty(sql.getSelectFields())) {
                //insert...select...
                sql.changeSqlType(EntityDao.SQL_SELECT);
                sql.changeTableName(originTbName);
                Pair<String, Object[]> selectPair = SqlMakeTools.useSql(sql);
                String finalSql = baseInsertSql + " " + selectPair.getFirst();
                Object[] finalParams = selectPair.getSecond();
                res = jdbcTemplate.update(finalSql, finalParams);
            }
            return res;
        } finally {
            sql.changeTableName(originTbName);
            sql.changeSqlType(originSqlType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createWithSql(SQL sql) throws Exception {
        TableMeta tableMeta = sql.getTableMeta();
        String originTbName = EntityTools.transferColumnName(StringUtils.isEmpty(tableMeta.getName()) ? "tmp_" + UUID.randomUUID().toString().toLowerCase().replace("-", "") : tableMeta.getName());
        String originSqlType = sql.getSqlType();
        String originInsertTbName = sql.getInsert().getFirst();
        List<String> originInsertFields = sql.getInsert().getSecond();
        try {
            sql.changeTableName(originTbName);
            Pair<String, Object[]> createSqlPair = SqlMakeTools.useSql(sql);
            jdbcTemplate.execute(createSqlPair.getFirst());
            //判断是否有数据需要插入,有则插入
            if (CollectionUtils.isNotEmpty(sql.getInsertValues()) || CollectionUtils.isNotEmpty(sql.getSelectFields())) {
                sql.changeSqlType(EntityDao.SQL_INSERT);
                sql.getInsert().setFirst(originTbName);
                sql.getInsert().setSecond(tableMeta.getColumns().stream().map(columnMeta -> EntityTools.transferColumnName(columnMeta.getName())).collect(Collectors.toList()));
                insertWithSql(sql);
            }
            return originTbName;
        } finally {
            tableMeta.setName(originTbName);
            sql.changeSqlType(originSqlType);
            sql.getInsert().setFirst(originInsertTbName);
            sql.getInsert().setSecond(originInsertFields);
        }
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
            String truncateSql = pair.getFirst();
            jdbcTemplate.batchUpdate(truncateSql.split("\n"));
        } else if (sql.getSqlType().equals(EntityDao.SQL_DROP)) {
            String dropSql = pair.getFirst();
            jdbcTemplate.execute(dropSql);
        } else {
            throw new GyjdbcException("method drunk only support `DROP` AND `TRUNCATE`");
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

}
