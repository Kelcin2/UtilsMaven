package com.github.flyinghe.exception;

/**
 * Created by FlyingHe on 2017/9/26.
 */
public class WriteExcelRuntimeException extends RuntimeException {
    public WriteExcelRuntimeException() {
    }

    public WriteExcelRuntimeException(String message) {
        super(message);
    }

    public WriteExcelRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteExcelRuntimeException(Throwable cause) {
        super(cause);
    }

    public WriteExcelRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
