package com.gysoft.jdbc.dao;

import com.gysoft.jdbc.bean.Page;
import com.gysoft.jdbc.bean.PageResult;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author 周宁
 */
public interface ISpringJdbc {
    JdbcTemplate getJdbcTemplate();

    NamedParameterJdbcTemplate getNamedParameterJdbcTemplate();

    String insertForId(String sql);

    String insertForId(String sql, Object[] args);

    int batchInsert(String sql, List<Object[]> batchArgs);

    int batchInsert(String sql, List<Object[]> batchArgs, int[] types);

    int batchInsert(String sql, List<Object[]> batchArgs, int batchPageSize);

    int batchInsert(String sql, List<Object[]> batchArgs, int[] types, int batchPageSize);

    void batchUpdate(String sql, List<Object[]> batchArgs);

    void batchUpdate(String sql, List<Object[]> batchArgs, int batchPageSize);

    void batchUpdate(String sql, List<Object[]> batchArgs, int[] types);

    void batchUpdate(String sql, List<Object[]> batchArgs, int[] types, int batchPageSize);

    int update(String sql, Object[] args);

    <T> T query(String sql, ResultSetExtractor<T> rse);

    <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse);

    <T> List<T> query(String sql, RowMapper<T> rowMapper);

    <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper);

    <T> List<T> query(String sql, Class<T> elementType);

    <T> List<T> query(String sql, Object[] args, Class<T> elementType);

    <T> List<T> query(String sql, Map<String, Object> paramMap, Class<T> elementType);

    <T> T queryForObject(String sql, Class<T> requiredType);

    <T> T queryForObject(String sql, Object[] args, Class<T> requiredType);

    List<Map<String, Object>> queryForList(String sql, Object[] args);

    <T> PageResult<T> queryForPageResult(Page page, String sql, Object[] args, Class<T> requiredType);

    <T> PageResult<T> queryForPageResult(Page page, String sql, Map<String, Object> paramMap, Class<T> requiredType);
}
