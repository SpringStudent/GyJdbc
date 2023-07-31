package com.gysoft.jdbc.dao;

import com.gysoft.jdbc.bean.*;
import com.gysoft.jdbc.multi.balance.LoadBalance;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;

/**
 *@author 周宁
 */
public interface EntityDao<T,Id extends Serializable>{

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

	/**
	 * 插入指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param t 实体对象
	 */
	int save(T t) throws Exception ;

	/**
	 * 修改指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param t 实体对象
	 */
	int update(T t) throws Exception ;

	/**
	 * 批量保存指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param list 实体对象集合
	 */
	void batchSave(List<T> list) throws Exception ;

	/**
	 * 批量保存指定的持久化对象
	 * @param list 实体对象集合
	 * @return int插入记录的条数
	 * @throws Exception
	 */
	int saveAll(List<T> list)throws Exception;

	/**
	 * 批量更新指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param list 实体对象集合
	 */
	void batchUpdate(List<T> list) throws Exception ;

	/**
	 * 根据主键删除
	 * @throws Exception sql错误抛出异常
	 * @param id 实体主键
	 */
	int delete(Id id) throws Exception ;

	/**
	 * 根据where条件删除
	 * @param criteria 条件参数
	 * @throws Exception sql错误抛出异常
	 */
	int deleteWithCriteria(Criteria criteria) throws Exception;

	/**
	 * 根据主键批量删除
	 * @throws Exception sql错误抛出异常
	 * @param ids 主键集合
	 */
	int batchDelete(List<Id> ids) throws Exception ;

	/**
	 * 根据ID检索持久化对象
	 * @param id 主键
	 * @return T 实体对象
	 * @throws Exception sql错误抛出异常
	 */
	T queryOne(Id id) throws Exception ;

	/**
	 * 根据ID检索持久化对象
	 * @param id 主键
	 * @param tRowMapper 自定义实体映射mapper
	 * @return T 实体对象
	 * @throws Exception sql错误抛出异常
	 */
	T queryOne(Id id, RowMapper<T> tRowMapper) throws Exception ;

	/**
	 * 检索所有持久化对象
	 * @return List 实体对象列表
	 * @throws Exception sql错误抛出异常
	 */
	List<T> queryAll() throws Exception ;

	/**
	 * 检索所有持久化对象
	 * @param tRowMapper 自定义实体映射mapper
	 * @return List 实体对象列表
	 * @throws Exception sql错误抛出异常
	 */
	List<T> queryAll(RowMapper<T> tRowMapper) throws Exception ;

	/**
	 * 分页查询
	 * @param page 分页条件
	 * @return PageResult 分页查询结果
	 * @throws Exception sql错误抛出异常
	 */
	PageResult<T> pageQuery(Page page) throws Exception;

	/**
	 * 分页查询
	 * @param page 分页条件
	 * @param tRowMapper 自定义实体映射mapper
	 * @return PageResult 分页查询结果
	 * @throws Exception sql错误抛出异常
	 */
	PageResult<T> pageQuery(Page page,RowMapper<T> tRowMapper) throws Exception;

	/**
	 * 分页条件查询
	 * @param page 分页条件
	 * @param criteria 查询条件
	 * @return PageResult 分页查询结果
	 * @throws Exception sql错误抛出异常
	 */
	PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception;

	/**
	 * 分页条件查询
	 * @param page 分页条件
	 * @param criteria 查询条件
	 * @param tRowMapper 自定义实体映射mapper
	 * @return PageResult 分页查询结果
	 * @throws Exception sql错误抛出异常
	 */
	PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria,RowMapper<T> tRowMapper) throws Exception;

	/**
	 * 条件查询
	 * @param criteria 查询条件
	 * @return List 结果集
	 * @throws Exception sql错误抛出异常
	 */
	List<T> queryWithCriteria(Criteria criteria) throws Exception;

	/**
	 * 条件查询
	 * @param criteria 查询条件
	 * @param tRowMapper 自定义实体映射mapper
	 * @return List 结果集
	 * @throws Exception sql错误抛出异常
	 */
	List<T> queryWithCriteria(Criteria criteria,RowMapper<T> tRowMapper) throws Exception;

	/**
	 * 根据条件查询
	 * @param criteria 查询条件
	 * @return T 实体对象
	 * @throws Exception sql错误抛出异常
	 */
	T queryOne(Criteria criteria)throws Exception;

	/**
	 * 根据条件查询
	 * @param criteria 查询条件
	 * @param tRowMapper 自定义实体映射mapper
	 * @return T 实体对象
	 * @throws Exception sql错误抛出异常
	 */
	T queryOne(Criteria criteria,RowMapper<T> tRowMapper)throws Exception;

	/**
	 * 根据sql查询
	 * @param sql sql拼接器
	 * @param <E> 查询结果类型
	 * @throws Exception sql错误抛出异常
	 */
	<E> Result<E> queryWithSql(Class<E> clss,SQL sql)throws Exception;

	/**
	 * 根据sql更新
	 * @param sql sql拼接器
	 * @return int 更新条目数量
	 * @throws Exception sql错误抛出异常
	 */
	int updateWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql删除
	 * @param sql sql拼接器
	 * @return int 删除条目数量
	 * @throws Exception sql错误抛出异常
	 */
	int deleteWithSql(SQL sql)throws Exception;

	/**
	 * 键值对查询
	 * @param sql sql拼接器
	 * @param resultSetExtractor 结果抽取器
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return Map 返回类型Map
	 * @throws Exception sql错误抛出异常
	 */
	<K,V> Map<K,V> queryMapWithSql(SQL sql,ResultSetExtractor<Map<K,V>> resultSetExtractor)throws Exception;

	/**
	 * 根据条件查询Map集合
	 * @param sql sql拼接器
	 * @return List 结果集
	 * @throws Exception sql错误抛出异常
	 */
	List<Map<String,Object>> queryMapsWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql查询一个int值
	 * @param sql sql拼接器
	 * @return Integer 结果类型，一般为查询数量
	 * @throws Exception sql错误抛出异常
	 */
	Integer queryIntegerWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql插入数据
	 * @param sql sql拼接器
	 * @return int 更新条目数量
	 * @throws Exception sql错误抛出异常
	 */
	int insertWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql创建表;如果有指定数据将数据插入
	 * @param sql sql拼接器
	 * @return String 表名称
	 * @throws Exception sql错误抛出异常
	 */
	String createWithSql(SQL sql)throws Exception;

	/**
	 * 删除表
	 * @throws Exception sql错误抛出异常
	 */
	void drop()throws Exception;

	/**
	 * 清除表数据和delete不同的是，该方法不需要where
	 * 条件并且数据一旦清除不可恢复
	 * @throws Exception sql错误抛出异常
	 */
	void truncate()throws Exception;

	/**
	 * 喝醉了干一些犯浑的事情，比如删除表，清楚数据
	 * @param sql sql拼接器
	 * @throws Exception sql错误抛出异常
	 */
	void drunk(SQL sql)throws Exception;

	/**
	 * 绑定指定key的数据源
	 * @param bindKey 数据源dataSource
	 * @return EntityDao 当前的dao对象
	 * @throws Exception sql错误抛出异常
	 */
	EntityDao<T,Id> bindKey(String bindKey)throws Exception;
	/**
	 * 绑定指定组的数据源
	 * @param group 指定的数据源组
	 * @param loadBalance 负载均衡策略
	 * @return EntityDao 当前的dao对象
	 * @throws Exception sql错误抛出异常
	 */
	EntityDao<T,Id> bindGroup(String group, Class<? extends LoadBalance> loadBalance)throws Exception;

	/**
	 * 绑定指定组的数据源
	 * @param group 指定的数据源组
	 * @return EntityDao 当前的dao对象
	 * @throws Exception sql错误抛出异常
	 */
	EntityDao<T,Id> bindGroup(String group)throws Exception;

}
