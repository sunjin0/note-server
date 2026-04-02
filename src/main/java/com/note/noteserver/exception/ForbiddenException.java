package com.note.noteserver.exception;

/**
 * 授权异常
 * 用于表示用户已认证但无权限访问的情况（403）
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
