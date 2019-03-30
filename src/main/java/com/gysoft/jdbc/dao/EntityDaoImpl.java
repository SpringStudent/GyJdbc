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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支持注解，若实体没有注解，实体类名需要按照驼峰命名，属性与数据库字段一致不区分大小写
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
    public List<Map<String, Object>> queryMapsWithCriteria(Criteria criteria) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, doCriteriaSelect(criteria));
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), new ColumnMapRowMapper());
    }

    @Override
    public Map<String, Object> queryMapWithCriteria(Criteria criteria, ResultSetExtractor<Map<String, Object>> resultSetExtractor) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, doCriteriaSelect(criteria));
        return jdbcTemplate.query(pair.getFirst(), pair.getSecond(), resultSetExtractor);
    }

    private StringBuilder doCriteriaSelect(Criteria criteria) {
        StringBuilder sql = new StringBuilder();
        Set<String> selectFields = criteria.getSelectFields();
        if (CollectionUtils.isEmpty(selectFields)) {
            sql.append("SELECT * FROM " + tableName);
        } else {
            sql.append("SELECT ");
            selectFields.forEach(selectField -> sql.append(selectField + ", "));
            sql.setLength(sql.length() - 2);
            sql.append(" FROM " + tableName);
        }
        return sql;
    }

    @Override
    public Integer queryIntegerWithCriteria(Criteria criteria) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, doCriteriaSelect(criteria));
        return jdbcTemplate.queryForObject(pair.getFirst(), pair.getSecond(), Integer.class);
    }

    @Override
    public String queryStringWithCriteria(Criteria criteria) throws Exception {
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, doCriteriaSelect(criteria));
        return jdbcTemplate.queryForObject(pair.getFirst(), pair.getSecond(), String.class);
    }

    @Override
    public int updateWithCriteria(Criteria criteria) throws Exception {
        List<Pair> kvs = criteria.getKvs();
        if (!CollectionUtils.isEmpty(kvs)) {
            Object[] params = {};
            StringBuilder sql = new StringBuilder();
            sql.append(SQL_UPDATE + SPACE + tableName + SPACE + "SET" + SPACE);
            for (int i = 0; i < kvs.size(); i++) {
                Pair pair = kvs.get(i);
                sql.append(pair.getFirst() + " = ?, ");
                params = ArrayUtils.add(params, pair.getSecond());
            }
            sql.setLength(sql.length() - 2);
            Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, new StringBuilder(sql));
            return jdbcTemplate.update(pair.getFirst(), ArrayUtils.addAll(params, pair.getSecond()));
        }
        return 0;
    }

    @Override
    public <E> Result<E> useCriteria(Class<E> clss, Criteria criteria) throws Exception {
        Pair<String,Object[]> pair = SqlMakeTools.doCriteria(criteria,null);
        return new Result<>(clss, pair.getFirst(), pair.getSecond(),jdbcTemplate);
    }

    @Override
    public <E> Result<E> useSql(Class<E> clss, String sql, Object... params)throws Exception{
        return new Result<>(clss, sql, params,jdbcTemplate);
    }

    @Override
    public <E extends Map<String, Object>> Result<E> useSql(String sql, Object... params)throws Exception {
        return new Result<>(null, sql, params, jdbcTemplate);
    }

    @Override
    public <E> Result<E> joinQuery(Class<E> clss, Criteria criteria) throws Exception {
        //表名称为空，帮忙设置下主表
        if(StringUtils.isEmpty(criteria.getpTable())){
            criteria.setpTable(tableName);
        }
        Pair<String, Object[]> pair = SqlMakeTools.doCriteria(criteria, null);
        return new Result<>(clss, pair.getFirst(), pair.getSecond(), jdbcTemplate);
    }

    @Override
    public <E> Result<E> subQuery(Class<E> clss, Criteria criteria) throws Exception {
        CriteriaTree criteriaTree = new CriteriaTree();
        Pair<String,Object[]> pair = SqlMakeTools.doCriteria(criteria,null);
        criteriaTree.setId("0");
        criteriaTree.setParams(pair.getSecond());
        criteriaTree.setSql(pair.getFirst());
        criteriaTree.setChildCriteriaTree(new ArrayList<>());
        SqlMakeTools.buildCriteriaTree(criteria,criteriaTree);
        Pair<String, Object[]> sqlParamPair = SqlMakeTools.doSubCriteria(criteriaTree, new Pair<>("", new Object[]{}));
        return new Result<>(clss, sqlParamPair.getFirst(), sqlParamPair.getSecond(), jdbcTemplate);
    }

}
