package com.note.noteserver.controller;

import com.note.noteserver.dto.*;
import com.note.noteserver.exception.UnauthorizedException;
import com.note.noteserver.service.SyncService;
import com.note.noteserver.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据同步控制器
 */
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final JwtUtil jwtUtil;

    /**
     * 执行数据同步
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SyncResponse>> sync(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SyncRequest request) {
        String userId = extractUserId(authHeader);
        SyncResponse response = syncService.sync(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取同步状态
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SyncStatusResponse>> getSyncStatus(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserId(authHeader);
        SyncStatusResponse response = syncService.getSyncStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 解决同步冲突
     */
    @PostMapping("/resolve-conflict")
    public ResponseEntity<ApiResponse<Map<String, String>>> resolveConflict(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ResolveConflictRequest request) {
        String userId = extractUserId(authHeader);
        syncService.resolveConflict(userId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "冲突已解决")));
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
