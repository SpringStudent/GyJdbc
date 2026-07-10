package com.gysoft.jdbc.dao;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.multi.balance.LoadBalance;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author 周宁
 */
public interface EntityDao<T, Id extends Serializable> {

    /**
     * 设置一些操作的常量
     */
    String SQL_SELECT = "select";
    String SQL_INSERT = "insert";
    String SQL_INSERTIGNORE = "insert ignore";
    String SQL_REPLACE = "replace";
    String SQL_UPDATE = "update";
    String SQL_DELETE = "delete";
    String SQL_TRUNCATE = "truncate";
    String SQL_DROP = "drop";
    String SQL_CREATE = "create";

    /**
     * 插入指定的持久化对象
     *
     * @param t 实体对象
     */
    int save(T t);

    /**
     * 修改指定的持久化对象
     *
     * @param t 实体对象
     */
    int update(T t);

    /**
     * 批量保存指定的持久化对象
     *
     * @param list 实体对象集合
     */
    void batchSave(List<T> list);

    /**
     * 保存或更新持久化对象
     *
     * @param t 实体对象
     */
    void saveOrUpdate(T t);

    /**
     * 批量保存指定的持久化对象
     *
     * @param list 实体对象集合
     * @return int插入记录的条数
     */
    int saveAll(List<T> list);

    /**
     * 批量更新指定的持久化对象
     *
     * @param list 实体对象集合
     */
    void batchUpdate(List<T> list);

    /**
     * 根据主键删除
     *
     * @param id 实体主键
     */
    int delete(Id id);

    /**
     * 根据where条件删除
     *
     * @param criteria 条件参数
     */
    int deleteWithCriteria(Criteria criteria);

    /**
     * 根据主键批量删除
     *
     * @param ids 主键集合
     */
    int batchDelete(List<Id> ids);

    /**
     * 根据ID检索持久化对象
     *
     * @param id 主键
     * @return T 实体对象
     */
    T queryOne(Id id);

    /**
     * 根据ID检索持久化对象
     *
     * @param id         主键
     * @param tRowMapper 自定义实体映射mapper
     * @return T 实体对象
     */
    <E> E queryOne(Id id, RowMapper<E> tRowMapper);

    /**
     * 检索所有持久化对象
     *
     * @return List 实体对象列表
     */
    List<T> queryAll();

    /**
     * 检索所有持久化对象
     *
     * @param tRowMapper 自定义实体映射mapper
     * @return List 实体对象列表
     */
    <E> List<E> queryAll(RowMapper<E> tRowMapper);

    /**
     * 分页查询
     *
     * @param page 分页条件
     * @return PageResult 分页查询结果
     */
    PageResult<T> pageQuery(Page page);

    /**
     * 分页查询
     *
     * @param page       分页条件
     * @param tRowMapper 自定义实体映射mapper
     * @return PageResult 分页查询结果
     */
    <E> PageResult<E> pageQuery(Page page, RowMapper<E> tRowMapper);

    /**
     * 分页条件查询
     *
     * @param page     分页条件
     * @param criteria 查询条件
     * @return PageResult 分页查询结果
     */
    PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria);

    /**
     * 分页条件查询
     *
     * @param page       分页条件
     * @param criteria   查询条件
     * @param tRowMapper 自定义实体映射mapper
     * @return PageResult 分页查询结果
     */
    <E> PageResult<E> pageQueryWithCriteria(Page page, Criteria criteria, RowMapper<E> tRowMapper);

    /**
     * 条件查询
     *
     * @param criteria 查询条件
     * @return List 结果集
     */
    List<T> queryWithCriteria(Criteria criteria);

    /**
     * 根据criteria判断是否有满足条件的数据
     *
     * @param criteria 查询条件
     * @return boolean 结果类型，数据是否存在
     */
    boolean existsWithCriteria(Criteria criteria);

    /**
     * 根据criteria统计数量
     *
     * @param criteria 查询条件
     * @return long 满足条件的记录数
     */
    long countWithCriteria(Criteria criteria);

    /**
     * 根据criteria查询主键列表，只查主键字段，适合批量操作前获取id集合
     *
     * @param criteria 查询条件
     * @return List 主键列表
     */
    List<Id> queryIds(Criteria criteria);

    /**
     * 条件查询
     *
     * @param criteria   查询条件
     * @param tRowMapper 自定义实体映射mapper
     * @return List 结果集
     */
    <E> List<E> queryWithCriteria(Criteria criteria, RowMapper<E> tRowMapper);

    /**
     * 根据条件查询
     *
     * @param criteria 查询条件
     * @return T 实体对象
     */
    T queryOne(Criteria criteria);

    /**
     * 根据条件查询
     *
     * @param criteria   查询条件
     * @param tRowMapper 自定义实体映射mapper
     * @return T 实体对象
     */
   <E> E queryOne(Criteria criteria, RowMapper<E> tRowMapper);

   /**
    * 根据sql查询
    *
    * @param sql sql拼接器
    * @param <E> 查询结果类型
    */
   <E> Result<E> queryWithSql(Class<E> clss, SQL sql);

    /**
     * 根据sql查询，直接返回对象列表
     *
     * @param clss 结果类型
     * @param sql  sql拼接器
     * @param <E>  查询结果类型
     * @return List 结果集
     */
    <E> List<E> queryListWithSql(Class<E> clss, SQL sql);

    /**
     * 根据sql查询，直接返回单个对象
     *
     * @param clss 结果类型
     * @param sql  sql拼接器
     * @param <E>  查询结果类型
     * @return E 单个实体对象
     */
    <E> E queryOneWithSql(Class<E> clss, SQL sql);

    /**
     * 根据sql分页查询
     *
     * @param page 分页条件
     * @param clss 结果类型
     * @param sql  sql拼接器
     * @param <E>  查询结果类型
     * @return PageResult 分页查询结果
     */
    <E> PageResult<E> pageQueryWithSql(Page page, Class<E> clss, SQL sql);

    /**
     * 根据sql更新
     *
     * @param sql sql拼接器
     * @return int 更新条目数量
     */
    int updateWithSql(SQL sql);

    /**
     * 根据sql删除
     *
     * @param sql sql拼接器
     * @return int 删除条目数量
     */
    int deleteWithSql(SQL sql);

    /**
     * 键值对查询
     *
     * @param sql                sql拼接器
     * @param resultSetExtractor 结果抽取器
     * @param <K>                键类型
     * @param <V>                值类型
     * @return Map 返回类型Map
     */
    <K, V> Map<K, V> queryMapWithSql(SQL sql, ResultSetExtractor<Map<K, V>> resultSetExtractor);

    /**
     * 根据条件查询Map集合
     *
     * @param sql sql拼接器
     * @return List 结果集
     */
    List<Map<String, Object>> queryMapsWithSql(SQL sql);

    /**
     * 根据sql查询一个int值
     *
     * @param sql sql拼接器
     * @return Integer 结果类型，一般为查询数量
     */
    Integer queryIntegerWithSql(SQL sql);

    /**
     * 根据sql统计数量
     *
     * @param sql sql拼接器
     * @return Integer 结果类型，一般为查询数量
     */
    long countWithSql(SQL sql);

    /**
     * 根据sql判断是否有满足条件的数据
     *
     * @param sql sql拼接器
     * @return boolean 结果类型，数据是否存在
     */
    boolean existsWithSql(SQL sql);

    /**
     * 根据sql插入数据
     *
     * @param sql sql拼接器
     * @return int 更新条目数量
     */
    int insertWithSql(SQL sql);

    /**
     * 根据sql创建表;如果有指定数据将数据插入
     *
     * @param sql sql拼接器
     * @return String 表名称
     */
    String createWithSql(SQL sql);

    /**
     * 删除表
     *
     */
    void drop();

    /**
     * 清除表数据和delete不同的是，该方法不需要where
     * 条件并且数据一旦清除不可恢复
     *
     */
    void truncate();

    /**
     * 喝醉了干一些犯浑的事情，比如删除表，清楚数据
     *
     * @param sql sql拼接器
     */
    void drunk(SQL sql);

    /**
     * 绑定指定key的数据源
     *
     * @param bindKey 数据源dataSource
     * @return EntityDao 当前的dao对象
     */
    EntityDao<T, Id> bindKey(String bindKey);

    /**
     * 绑定指定组的数据源
     *
     * @param group       指定的数据源组
     * @param loadBalance 负载均衡策略
     * @return EntityDao 当前的dao对象
     */
    EntityDao<T, Id> bindGroup(String group, Class<? extends LoadBalance> loadBalance);

    /**
     * 绑定指定组的数据源
     *
     * @param group 指定的数据源组
     * @return EntityDao 当前的dao对象
     */
    EntityDao<T, Id> bindGroup(String group);

    default Optional<T> queryOneOpt(Id id) {
        try {
            return Optional.ofNullable(queryOne(id));
        } catch (Exception e) {
            throw new GyjdbcException(e);
        }
    }

    default <E> Optional<E> queryOneOpt(Id id, RowMapper<E> tRowMapper) {
        try {
            return Optional.ofNullable(queryOne(id, tRowMapper));
        } catch (Exception e) {
            throw new GyjdbcException(e);
        }
    }

    default Optional<T> queryOneOpt(Criteria criteria) {
        try {
            return Optional.ofNullable(queryOne(criteria));
        } catch (Exception e) {
            throw new GyjdbcException(e);
        }
    }

    default <E> Optional<E> queryOneOpt(Criteria criteria, RowMapper<E> tRowMapper) {
        try {
            return Optional.ofNullable(queryOne(criteria, tRowMapper));
        } catch (Exception e) {
            throw new GyjdbcException(e);
        }
    }

    default <E> Optional<E> queryOneWithSqlOpt(Class<E> clss, SQL sql) {
        try {
            return Optional.ofNullable(queryOneWithSql(clss, sql));
        } catch (Exception e) {
            throw new GyjdbcException(e);
        }
    }
}
