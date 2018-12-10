package com.github.flyinghe.exception;

/**
 * Created by FlyingHe on 2017/9/26.
 */
public class ReadExcelRuntimeException extends RuntimeException {
    public ReadExcelRuntimeException() {
    }

    public ReadExcelRuntimeException(String message) {
        super(message);
    }

    public ReadExcelRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadExcelRuntimeException(Throwable cause) {
        super(cause);
    }

    public ReadExcelRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
