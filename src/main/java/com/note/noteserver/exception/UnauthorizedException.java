package com.note.noteserver.exception;

/**
 * 认证异常
 * 用于表示用户未认证或认证失败的情况
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
