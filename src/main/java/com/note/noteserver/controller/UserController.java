package com.note.noteserver.controller;

import com.note.noteserver.dto.ApiResponse;
import com.note.noteserver.dto.UpdateProfileRequest;
import com.note.noteserver.dto.UserDto;
import com.note.noteserver.exception.UnauthorizedException;
import com.note.noteserver.service.UserService;
import com.note.noteserver.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户资料控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserId(authHeader);
        UserDto user = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        String userId = extractUserId(authHeader);
        UserDto user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteAccount(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserId(authHeader);
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "账户已删除")));
    }

    /**
     * 从 Authorization Header 中提取用户ID
     */
    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("无效的授权头");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("令牌无效或已过期");
        }
        return jwtUtil.extractUserId(token);
    }
}
