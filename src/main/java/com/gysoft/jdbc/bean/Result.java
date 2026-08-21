package com.gysoft.jdbc.bean;

import org.springframework.dao.support.DataAccessUtils;
import com.gysoft.jdbc.tools.MixUtils;
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
    private final boolean hasLimit;

    public Result(Class<E> type, String sql, Object[] params, JdbcTemplate jdbcTemplate) {
        this(type, sql, params, jdbcTemplate, false);
    }

    public Result(Class<E> type, String sql, Object[] params, JdbcTemplate jdbcTemplate, boolean hasLimit) {
        this.type = type;
        this.sql = sql;
        this.params = params;
        this.jdbcTemplate = jdbcTemplate;
        this.hasLimit = hasLimit;
    }

    public E queryOne() {
        return DataAccessUtils.singleResult(queryList());
    }

    public E queryObject() {
        return jdbcTemplate.queryForObject(sql, params, type);
    }

    public List<E> queryList() {
        return jdbcTemplate.query(sql, EntityRowMapper.newRowMapper(type), params);
    }

    public List<E> queryForList() {
        return jdbcTemplate.queryForList(sql, type, params);
    }

    public List<Map<String, Object>> queryMaps() {
        return jdbcTemplate.query(sql, params, new ColumnMapRowMapper());
    }

    public PageResult<E> pageQuery(Page page) {
        if (page == null) {
            throw new GyjdbcException("page param cannot be null");
        }
        if (hasLimit) {
            throw new GyjdbcException("pageQuery does not support limit in SQL, use Page to control paging");
        }
        //数据查询不包派生表:MySQL 合并派生表时会丢弃其中无 LIMIT 的 ORDER BY,导致翻页顺序不稳定
        String pageSql = sql + " LIMIT ?,?";
        Object[] pageParams = MixUtils.appendParams(params, page.getOffset(), page.getPageSize());
        List<E> paged = jdbcTemplate.query(pageSql, pageParams, EntityRowMapper.newRowMapper(type));
        //独立统计总数,避免FOUND_ROWS()依赖同一连接在连接池/并发下取到错误计数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") temp";
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        return new PageResult(paged, count == null ? 0 : count);
    }
}
