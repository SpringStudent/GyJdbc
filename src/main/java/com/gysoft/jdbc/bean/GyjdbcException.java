package com.gysoft.jdbc.bean;

/**
 * 异常类
 * @author 周宁
 */
public class GyjdbcException extends RuntimeException{

    public GyjdbcException() {
        super();
    }

    public GyjdbcException(String message) {
        super(message);
    }

    public GyjdbcException(String message, Throwable cause) {
        super(message, cause);
    }

    public GyjdbcException(Throwable cause) {
        super(cause);
    }

    protected GyjdbcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
