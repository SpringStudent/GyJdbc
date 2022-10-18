package com.gysoft.jdbc.bean;

/**
 * @author zhouning
 */
public interface SQLInterceptor {
    /**
     * 在真正构建sql和参数之前执行
     * @param criteria 组装撑的sql对象
     * @throws Exception sql错误抛出异常
     * @author ZhouNing
     * @date 2022/10/17 16:24
     **/
    void beforeBuild(SQLType sqlType, AbstractCriteria criteria)throws Exception;
    /**
     * 成功构建sql和参数后执行
     * @param sql 真正的sql字符串
     * @param args sql入参数组
     * @throws Exception sql错误抛出异常
     * @author ZhouNing
     * @date 2022/10/17 16:28
     **/
    void afterBuild(String sql,Object[] args)throws Exception;


}
