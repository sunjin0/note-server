package com.note.noteserver.controller;

import com.note.noteserver.dto.*;
import com.note.noteserver.service.AuthService;
import com.note.noteserver.util.I18nMessageUtil;
import com.note.noteserver.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserId(authHeader);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", I18nMessageUtil.getMessage("success.logout"))));
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserId(authHeader);
        UserDto user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        String userId = extractUserId(authHeader);
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", I18nMessageUtil.getMessage("success.password.changed"))));
    }

    /**
     * 从 Authorization Header 中提取用户ID
     */
    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException(I18nMessageUtil.getMessage("error.auth.invalid.auth.header"));
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException(I18nMessageUtil.getMessage("error.auth.invalid.token"));
        }
        return jwtUtil.extractUserId(token);
    }
}
