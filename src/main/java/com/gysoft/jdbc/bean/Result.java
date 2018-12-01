package com.gysoft.jdbc.bean;

import com.gysoft.jdbc.dao.EntityDao;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

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

    public Result(Class<E> type, String sql, Object[] params, EntityDao entityDao) {
        this.type = type;
        this.sql = sql;
        this.params = params;
        this.jdbcTemplate = entityDao.getJdbcTemplate();
    }

    public E queryOne() {
        return DataAccessUtils.singleResult(queryAll());
    }

    public E queryForObject() {
        return jdbcTemplate.queryForObject(sql, params, type);
    }

    public List<E> queryAll() {
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(type), params);
    }

    public List<Map<String, Object>> queryForMaps() {
        return jdbcTemplate.query(sql, params, new ColumnMapRowMapper());
    }

    public PageResult<E> pageQuery(Page page) {
        Object pageParams[] = {};
        String pageSql = "SELECT SQL_CALC_FOUND_ROWS * FROM (" + sql + ") temp ";
        pageSql = pageSql + " LIMIT ?,?";
        pageParams = ArrayUtils.addAll(params, new Object[]{page.getOffset(), page.getPageSize()});
        List<E> paged = jdbcTemplate.query(pageSql, pageParams, BeanPropertyRowMapper.newInstance(type));
        String countSql = "SELECT FOUND_ROWS() ";
        int count = jdbcTemplate.queryForObject(countSql, Integer.class);
        return new PageResult(paged, count);
    }
}
