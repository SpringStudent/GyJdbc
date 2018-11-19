package com.gysoft.jdbc.dao;

import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.Page;
import com.gysoft.jdbc.bean.PageResult;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 *@author 周宁
 *@date 2018/3/7
 */
public interface EntityDao<T,Id extends Serializable>{

	/**
	 * 设置一些操作的常量
	 */
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

	/**
	 * 插入指定的持久化对象
	 * 
	 * @param t
	 * @return
	 */
	void save(T t) throws Exception ;

	/**
	 * 修改指定的持久化对象 
	 * 
	 * @param t
	 */
	void update(T t) throws Exception ;

	/**
	 * 批量保存指定的持久化对象
	 * 
	 * @param list
	 */
	void batchSave(List<T> list) throws Exception ;

	/**
	 * 批量更新指定的持久化对象
	 * 
	 * @param list
	 */
	void batchUpdate(List<T> list) throws Exception ;

	/**
	 * 根据主键删除
	 * 
	 * @param id
	 */
	void delete(Id id) throws Exception ;
	
	/**
	 * 
	 *根据where条件删除
	 * @author DJZ-PJJ
	 * @date 2018年5月30日 下午7:30:04
	 * @param criteria
	 * @throws Exception
	 */
	void deleteWithCriteria(Criteria criteria) throws Exception;

	/**
	 * 根据主键批量删除
	 * 
	 * @param ids
	 */
	void batchDelete(List<Id> ids) throws Exception ;

	/**
	 * 根据ID检索持久化对象
	 */
	T queryOne(Id id) throws Exception ;

	/**
	 * 检索所有持久化对象
	 */
	List<T> queryAll() throws Exception ;

	/**
	 * 分页查询
	 * @param page
	 * @return
	 * @throws Exception
	 */
	PageResult<T> pageQuery(Page page) throws Exception;

	/**
	 * 分页条件查询
	 * @param page
	 * @param criteria
	 * @return
	 * @throws Exception
	 */
	PageResult<T> pageQueryWithCriteria(Page page, Criteria criteria) throws Exception;

	/**
	 * 条件查询
	 * @param criteria
	 * @return
	 * @throws Exception
	 */
	List<T> queryWithCriteria(Criteria criteria) throws Exception;

	/**
	 * 根据条件查询
	 * @param criteria
	 * @return
	 * @throws Exception
	 */
	T queryOne(Criteria criteria)throws Exception;

	/**
	 * 根据条件查询Map集合
	 * @param criteria
	 * @return List<Map<String,Object>>
	 * @throws Exception
	 */
	List<Map<String,Object>> queryMapsWithCriteria(Criteria criteria)throws Exception;

	/**
	 * 根据条件查询Map
	 * @param criteria
	 * @param resultSetExtractor
	 * @return Map<String,Object>
	 * @throws Exception
	 */
	Map<String,Object> queryMapWithCriteria(Criteria criteria, ResultSetExtractor<Map<String, Object>> resultSetExtractor)throws Exception;

	/**
	 * 根据条件查询某个整数列值
	 * @param criteria
	 * @return Integer
	 * @throws Exception
	 */
	Integer queryIntegerWithCriteria(Criteria criteria)throws Exception;

	/**
	 * 根据条件查询某个字符列值
	 * @return
	 * @throws Exception
	 */
	String queryStringWithCriteria(Criteria criteria)throws Exception;

	/**
	 * 根据条件更新
	 * @param criteria
	 * @return int
	 * @throws Exception
	 */
	int updateWithCriteria(Criteria criteria)throws Exception;
}
