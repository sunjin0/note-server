package com.note.noteserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 通用 API 响应包装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiError error;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ApiError(code, message, null))
                .build();
    }

    /**
     * 错误响应（带字段错误）
     */
    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ApiError(code, message, fieldErrors))
                .build();
    }

    /**
     * API 错误信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiError {
        private String code;
        private String message;
        private Map<String, String> fieldErrors;
    }
}
