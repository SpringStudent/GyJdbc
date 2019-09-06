package com.gysoft.jdbc.dao;

import com.gysoft.jdbc.bean.*;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 *@author 周宁
 */
public interface EntityDao<T,Id extends Serializable>{

	/**
	 * 设置一些操作的常量
	 */
	String SQL_SELECT = "select";
	String SQL_INSERT = "insert";
	String SQL_UPDATE = "update";
	String SQL_DELETE = "delete";
	String SPACE = " ";
	String SQL_IN = "IN";
	String SQL_NOT_IN = "NOT IN";
	char IN_START = '(';
	char IN_END = ')';
	String SQL_IS = "IS";
	String SQL_ORDER_BY = "ORDER BY";
	String SQL_GROUP_BY = "GROUP BY";
	String SQL_BETWEEN_AND = "BETWEEN ? AND ?";

	/**
	 * 插入指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param t 实体对象
	 */
	void save(T t) throws Exception ;

	/**
	 * 修改指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param t 实体对象
	 */
	void update(T t) throws Exception ;

	/**
	 * 批量保存指定的持久化对象
	 * @throws Exception sql错误抛出异常
	 * @param list 实体对象集合
	 */
	void batchSave(List<T> list) throws Exception ;

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
	void delete(Id id) throws Exception ;

	/**
	 * 根据where条件删除
	 * @param criteria 条件参数
	 * @throws Exception sql错误抛出异常
	 */
	void deleteWithCriteria(Criteria criteria) throws Exception;

	/**
	 * 根据主键批量删除
	 * @throws Exception sql错误抛出异常
	 * @param ids 主键集合
	 */
	void batchDelete(List<Id> ids) throws Exception ;

	/**
	 * 根据ID检索持久化对象
	 * @param id 主键
	 * @return T 实体对象
	 * @throws Exception sql错误抛出异常
	 */
	T queryOne(Id id) throws Exception ;

	/**
	 * 检索所有持久化对象
	 * @return List 实体对象列表
	 * @throws Exception sql错误抛出异常
	 */
	List<T> queryAll() throws Exception ;

	/**
	 * 分页查询
	 * @param page 分页条件
	 * @return PageResult 分页查询结果
	 * @throws Exception sql错误抛出异常
	 */
	PageResult<T> pageQuery(Page page) throws Exception;

	/**
	 * 分页条件查询
	 * @param page 分页条件
	 * @param criteria 查询条件
	 * @return PageResult 分页查询结果
	 * @throws Exception sql错误抛出异常
	 */
	PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception;

	/**
	 * 条件查询
	 * @param criteria 查询条件
	 * @return List 结果集
	 * @throws Exception sql错误抛出异常
	 */
	List<T> queryWithCriteria(Criteria criteria) throws Exception;

	/**
	 * 根据条件查询
	 * @param criteria 查询条件
	 * @return T 实体对象
	 * @throws Exception sql错误抛出异常
	 */
	T queryOne(Criteria criteria)throws Exception;

	/**
	 * 根据sql查询
	 * @param sql sql拼接器
	 * @param <E> 查询结果类型
	 * @throws Exception
	 */
	<E> Result<E> queryWithSql(Class<E> clss,SQL sql)throws Exception;

	/**
	 * 根据sql更新
	 * @param sql sql拼接器
	 * @return int 更新条目数量
	 * @throws Exception
	 */
	int updateWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql删除
	 * @param sql sql拼接器
	 * @return int 删除条目数量
	 * @throws Exception
	 */
	int deleteWithSql(SQL sql)throws Exception;

	/**
	 * 键值对查询
	 * @param sql sql拼接器
	 * @param resultSetExtractor 结果抽取器
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return Map 返回类型Map
	 * @throws Exception
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
	 * @throws Exception
	 */
	Integer queryIntegerWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql插入数据
	 * @param sql sql拼接器
	 * @return int 更新条目数量
	 * @throws Exception
	 */
	int insertWithSql(SQL sql)throws Exception;

	/**
	 * 根据sql创建表;如果有指定数据将数据插入
	 * @param sql sql拼接器
	 * @return String 表名称
	 * @throws Exception
	 */
	String createWithSql(SQL sql)throws Exception;

	/**
	 * 绑定指定的dataSource
	 * @param bindKey 数据源dataSource
	 * @return EntityDao 当前的dao对象
	 * @throws Exception
	 */
	EntityDao<T,Id> bindPoint(String bindKey)throws Exception;

	/**
	 * 绑定master的dataSource
	 * @return EntityDao 当前的dao对象
	 * @throws Exception
	 */
	EntityDao<T,Id> bindMaster()throws Exception;

	/**
	 * 绑定slave的dataSource
	 * @return EntityDao 当前的dao对象
	 * @throws Exception
	 */
	EntityDao<T,Id> bindSlave()throws Exception;
}
