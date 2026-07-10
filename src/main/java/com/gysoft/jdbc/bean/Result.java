package com.gysoft.jdbc.bean;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @param <E> 结果类型
 * @author 周宁
 */
public class Result<E> {

    private final Class<E> type;
    private final String sql;
    private final Object[] params;
    private final JdbcTemplate jdbcTemplate;

    public Result(Class<E> type, String sql, Object[] params, JdbcTemplate jdbcTemplate) {
        this.type = type;
        this.sql = sql;
        this.params = params;
        this.jdbcTemplate = jdbcTemplate;
    }

    public E queryOne() {
        return DataAccessUtils.singleResult(queryList());
    }

    public E queryObject() {
        return jdbcTemplate.queryForObject(sql, params, type);
    }

    public List<E> queryList() {
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(type), params);
    }

    public List<E> queryForList() {
        return jdbcTemplate.queryForList(sql, type, params);
    }

    public List<Map<String, Object>> queryMaps() {
        return jdbcTemplate.query(sql, params, new ColumnMapRowMapper());
    }

    public PageResult<E> pageQuery(Page page) {
        String pageSql = "SELECT * FROM (" + sql + ") temp LIMIT ?,?";
        Object[] pageParams = appendParams(params, page.getOffset(), page.getPageSize());
        List<E> paged = jdbcTemplate.query(pageSql, pageParams, BeanPropertyRowMapper.newInstance(type));
        //独立统计总数,避免FOUND_ROWS()依赖同一连接在连接池/并发下取到错误计数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") temp";
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        return new PageResult(paged, count == null ? 0 : count);
    }

    private Object[] appendParams(Object[] params, Object... extraParams) {
        List<Object> result = new ArrayList<>();
        if (params != null) {
            Collections.addAll(result, params);
        }
        Collections.addAll(result, extraParams);
        return result.toArray();
    }
}
