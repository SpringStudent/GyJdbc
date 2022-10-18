package com.gysoft.jdbc.bean;

/**
 * 用于在构建sql、参数数组钱执行相应逻辑，
 * 可用于添加统一的条件、强制更新时间戳、记录sql等,
 *
 * @author zhouning
 */
public interface SQLInterceptor {
    /**
     * 在真正构建sql和参数之前执行，该方法会影响最终的sql和参数
     * 对于EntityDao无Criteria、Sql参数的方法第二个参数为null，需要自行非空判断
     *
     * @param criteria 组装撑的sql对象
     * @throws Exception sql错误抛出异常
     * @author ZhouNing
     **/
    void beforeBuild(SQLType sqlType, AbstractCriteria criteria) throws Exception;

    /**
     * 成功构建sql和参数后执行，该方法执行不能够影响sql
     *
     * @param sql  真正的sql字符串
     * @param args sql入参数组
     * @throws Exception sql错误抛出异常
     * @author ZhouNing
     **/
    void afterBuild(String sql, Object[] args) throws Exception;


}
