package com.gysoft.jdbc.dao;


import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.tools.EntityTools;
import com.gysoft.jdbc.tools.SqlMakeTools;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.JDBCType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支持注解，若实体没有注解，实体类名需要按照驼峰命名，属性与数据库字段一致不区分大小写
 *
 * @author 彭佳佳
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
    private RowMapper rowMapper;

    @SuppressWarnings("unchecked")
    public EntityDaoImpl() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) type.getActualTypeArguments()[0];
        tableName = EntityTools.getTableName(entityClass);
        primaryKey = EntityTools.getPk(entityClass);
        rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
    }

    @Override
    public void save(T t) throws Exception {
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_INSERT);
        Object[] args = SqlMakeTools.setArgs(t, SQL_INSERT);
        int[] argTypes = SqlMakeTools.setArgTypes(t, SQL_INSERT);
        jdbcTemplate.update(sql, args, argTypes);
    }

    @Override
    public void update(T t) throws Exception {
        String sql = SqlMakeTools.makeSql(entityClass, tableName, SQL_UPDATE);
        Object[] args = SqlMakeTools.setArgs(t, SQL_UPDATE);
        int[] argTypes = SqlMakeTools.setArgTypes(t, SQL_UPDATE);
        jdbcTemplate.update(sql, args, argTypes);
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
        String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?";
        List<T> result = jdbcTemplate.query(sql, rowMapper, id);
        return DataAccessUtils.singleResult(result);
    }

    @Override
    public void delete(Id id) throws Exception {
        this.batchDelete(Collections.singletonList(id));
    }

    @Override
    public void batchDelete(List<Id> ids) throws Exception {
        if (CollectionUtils.isNotEmpty(ids)) {
            StringBuilder sql = new StringBuilder();
            List<String> marks = ids.stream().map(s -> "?").collect(Collectors.toList());
            sql.append(" DELETE FROM " + tableName + " WHERE " + primaryKey + " in (");
            sql.append(String.join(",", marks));
            sql.append(")");
            jdbcTemplate.update(sql.toString(), ids.toArray());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> queryAll() throws Exception {
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.query(sql, rowMapper);
    }


    @Override
    public PageResult<T> pageQuery(Page page) throws Exception {
        return this.pageQueryWithCriteria(page, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception {
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
        List<T> paged = jdbcTemplate.query(pageSql, params, rowMapper);
        String countSql = "SELECT FOUND_ROWS() ";
        int count = jdbcTemplate.queryForObject(countSql, Integer.class);
        return new PageResult(paged, count);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> queryWithCriteria(Criteria criteria) throws Exception {
        String sql = "SELECT * FROM " + tableName;
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), rowMapper);
    }

    @Override
    public void deleteWithCriteria(Criteria criteria) throws Exception {
        if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
            throw new RuntimeException("不支持的操作!");
        }
        String sql = "delete FROM " + tableName;
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
        jdbcTemplate.update(pair.getFirst(), pair.getSecond());
    }

    @Override
    public T queryOne(Criteria criteria) throws Exception {
        List<T> result = this.queryWithCriteria(criteria);
        return DataAccessUtils.singleResult(result);
    }

    @Override
    public <E> Result<E> queryWithSql(Class<E> clss, SQL sql) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.useSql(sql);
        return new Result<>(clss, pair.getFirst(), pair.getSecond(), jdbcTemplate);
    }

    @Override
    public int updateWithSql(SQL sql) throws Exception {
        List<Pair> kvs = sql.getKvs();
        if (!CollectionUtils.isEmpty(kvs)) {
            Object[] params = {};
            StringBuilder updateSql = new StringBuilder();
            updateSql.append(SQL_UPDATE + SPACE + tableName + SPACE + "SET" + SPACE);
            for (int i = 0; i < kvs.size(); i++) {
                Pair pair = kvs.get(i);
                updateSql.append(pair.getFirst() + " = ?, ");
                params = ArrayUtils.add(params, pair.getSecond());
            }
            updateSql.setLength(updateSql.length() - 2);
            Pair<String, Object[]> pair = SqlMakeTools.doCriteria(sql, new StringBuilder(updateSql));
            return jdbcTemplate.update(pair.getFirst(), ArrayUtils.addAll(params, pair.getSecond()));
        }
        return 0;
    }

    @Override
    public <K, V> Map<K, V> queryMapWithSql(SQL sql, ResultSetExtractor<Map<K, V>> resultSetExtractor) throws Exception {
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
    public int insertWithSql(SQL sql) throws Exception {
        //插入sql
        StringBuilder insertSql = new StringBuilder(String.format(sql.getPair().getFirst(), tableName));
        //待插入数据
        List<Object[]> params = sql.getPair().getSecond();
        int res=0;
        if (CollectionUtils.isNotEmpty(params)) {
            List<Object> paramList = new ArrayList<>();
            insertSql.append("VALUES ");
            for (Object[] param : params) {
                insertSql.append("(");
                for (Object obj : param) {
                    insertSql.append("?,");
                    paramList.add(obj);
                }
                insertSql.setLength(insertSql.length() - 1);
                insertSql.append("),");
            }
            insertSql.setLength(insertSql.length() - 1);
            res = jdbcTemplate.update(insertSql.toString(), paramList.toArray());
        } else if(CollectionUtils.isNotEmpty(sql.getSelectFields())){
            Pair<String, Object[]> p = SqlMakeTools.useSql(sql);
            insertSql.append(p.getFirst());
            res = jdbcTemplate.update(insertSql.toString(), p.getSecond());
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createWithSql(SQL sql) throws Exception {
        TableMeta tableMeta = sql.getTableMeta();
        StringBuilder createSql = new StringBuilder();
        StringBuilder insertSql = new StringBuilder();
        //创建表
        String tbName = StringUtils.isEmpty(tableMeta.getName()) ? "tmp_" + UUID.randomUUID().toString().toLowerCase().replace("-", "") : tableMeta.getName();
        List<ColumnMeta> columns = tableMeta.getColumns();

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("未指定任何字段");
        }
        createSql.append("CREATE ");
        insertSql.append("INSERT INTO ");
        if (tableMeta.isTemporary()) {
            createSql.append("TEMPORARY ");
        }
        createSql.append("TABLE ");
        createSql.append(tbName);
        insertSql.append(tbName);
        createSql.append("(");
        insertSql.append("(");
        columns.forEach(columnMeta -> {
            createSql.append("`");
            createSql.append(columnMeta.getName());
            insertSql.append(columnMeta.getName());
            createSql.append("` ");
            createSql.append(columnMeta.getDataType());
            if (columnMeta.isNotNull()) {
                createSql.append(" not null");
            }
            if (columnMeta.isPrimaryKey()) {
                createSql.append(" primary key");
                if(columnMeta.isAutoIncr()){
                    createSql.append(" auto_increment");
                }
            }
            if (columnMeta.getVal()!=null) {
                if(columnMeta.getJdbcType().equals(JDBCType.TIMESTAMP)){
                    createSql.append(String.format(" default %s",(columnMeta.getVal())));
                }else{
                    createSql.append(String.format(" default '%s'",(columnMeta.getVal())));
                }
            }
            if (StringUtils.isNotEmpty(columnMeta.getComment())) {
                createSql.append(String.format(" comment '%s'", columnMeta.getComment()));
            }
            createSql.append(",");
            insertSql.append(",");
        });
        //索引
        List<IndexMeta> indexMetas = tableMeta.getIndexs();
        indexMetas.forEach(indexMeta -> {
            createSql.append((indexMeta.isUnique() ? "unique" : "") + " key`" + indexMeta.getIndexName() + "`(");
            indexMeta.getColumnNames().forEach(cc -> {
                createSql.append("`"+cc+"`");
                createSql.append(",");
            });
            createSql.setLength(createSql.length() - 1);
            createSql.append("),");
        });
        insertSql.setLength(insertSql.length() - 1);
        createSql.setLength(createSql.length()-1);
        createSql.append(")ENGINE = " + tableMeta.getEngine() + " CHARACTER SET utf8 ");
        if (StringUtils.isNotEmpty(tableMeta.getComment())) {
            createSql.append("COMMENT=" + "'" + tableMeta.getComment() + "'");
        }
        insertSql.append(")");
        jdbcTemplate.execute(createSql.toString());
        //保存数据
        sql.getPair().setFirst(insertSql.toString());
        insertWithSql(sql);
        return tbName;
    }
}
