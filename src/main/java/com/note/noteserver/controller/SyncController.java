package com.note.noteserver.controller;

import com.note.noteserver.dto.*;
import com.note.noteserver.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 数据同步控制器 - 以 SYNC_DESIGN.md 为准
 */
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * 执行双向同步 (apiSync)
     * 上传本地数据并下载云端数据
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SyncResponse>> sync(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody SyncRequest request) {
        SyncResponse response = syncService.sync(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取同步状态 (apiGetSyncStatus)
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SyncStatusResponse>> getSyncStatus(
            @RequestAttribute("userId") String userId) {
        SyncStatusResponse response = syncService.getSyncStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 解决冲突
     */
    @PostMapping("/conflicts/{entryId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveConflict(
            @RequestAttribute("userId") String userId,
            @PathVariable("entryId") String entryId,
            @Valid @RequestBody ResolveConflictRequest request) {
        syncService.resolveConflict(userId, entryId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
