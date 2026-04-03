package com.note.noteserver.exception;

import com.note.noteserver.dto.ApiResponse;
import com.note.noteserver.util.I18nMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 支持国际化消息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("参数校验失败: {}", fieldErrors);
        String message = I18nMessageUtil.getMessage("error.validation.failed");
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", message, fieldErrors));
    }

    /**
     * 处理业务异常（400）
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BUSINESS_ERROR", ex.getMessage()));
    }

    /**
     * 处理认证异常（401）
     */
    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(RuntimeException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = I18nMessageUtil.getMessage("error.unauthorized");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", message));
    }

    /**
     * 处理无权限异常（403）
     */
    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(RuntimeException ex) {
        log.warn("访问被拒绝: {}", ex.getMessage());
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = I18nMessageUtil.getMessage("error.forbidden");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", message));
    }

    /**
     * 处理邮件发送异常（500）
     */
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponse<Void>> handleMailException(MailException ex) {
        log.error("邮件发送失败: {}", ex.getMessage());
        String message = I18nMessageUtil.getMessage("error.mail.send.failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("MAIL_SEND_FAILED", message));
    }

    /**
     * 处理业务异常（400）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BUSINESS_ERROR", ex.getMessage()));
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        String message = I18nMessageUtil.getMessage("error.server.internal");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", message));
    }
}
