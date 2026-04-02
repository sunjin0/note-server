package com.note.noteserver.dto;

import java.time.LocalDateTime;

/**
 * 同步状态响应 - 以 SYNC_DESIGN.md 为准
 */
public class SyncStatusResponse {

    private LocalDateTime lastSyncAt;

    private Integer pendingChanges;

    public SyncStatusResponse(LocalDateTime lastSyncAt, Integer pendingChanges) {
        this.lastSyncAt = lastSyncAt;
        this.pendingChanges = pendingChanges;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public Integer getPendingChanges() {
        return pendingChanges;
    }
}
