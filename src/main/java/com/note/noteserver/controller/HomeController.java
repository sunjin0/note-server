package com.note.noteserver.controller;

import com.note.noteserver.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 首页控制器
 */
@RestController
public class HomeController {

    /**
     * 首页 - API 信息
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, String>>> home() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "name", "Mood Journal API",
                "version", "1.0.0",
                "status", "running"
        )));
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "UP",
                "timestamp", java.time.LocalDateTime.now().toString()
        )));
    }
}
