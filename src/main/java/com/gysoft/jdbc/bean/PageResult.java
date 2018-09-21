package com.gysoft.jdbc.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回通用类
 * 
 * @author 彭佳佳
 * @data 2018年3月6日
 * @param <T>
 */
@Data
public class PageResult<T> implements Serializable {
	/**
	 * 总数量 
	 */
	private Integer total;
	/**
	 * 返回对象类型列表
	 */
	private List<T> list;

	public PageResult() {
		
	}

	public PageResult(List<T> list, Integer total) {
		this.total = total;
		this.list = list;
	}

	/**
	 * 创建一个空的分页结果集
     * @author 周宁
	 * @param
	 * @return PageResult
	 * @throws
	 * @version 1.0
	 */
	public static PageResult emptyPageResult(){
	    return new PageResult(Collections.EMPTY_LIST,0);
    }

}
