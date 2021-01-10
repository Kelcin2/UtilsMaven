package com.github.flyinghe.exception;

/**
 * Created by FlyingHe on 2021/1/10.
 */
public class GitUtilsException extends Exception {
    public GitUtilsException() {
    }

    public GitUtilsException(String message) {
        super(message);
    }

    public GitUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitUtilsException(Throwable cause) {
        super(cause);
    }

    public GitUtilsException(String message, Throwable cause, boolean enableSuppression,
                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
