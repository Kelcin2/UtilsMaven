package com.github.flyinghe.exception;

/**
 * Created by FlyingHe on 2017/9/26.
 */
public class WriteExcelException extends Exception {
    public WriteExcelException() {
    }

    public WriteExcelException(String message) {
        super(message);
    }

    public WriteExcelException(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteExcelException(Throwable cause) {
        super(cause);
    }

    public WriteExcelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
