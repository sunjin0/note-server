package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 解决同步冲突请求 - 以 SYNC_DESIGN.md 为准
 */
@Data
public class ResolveConflictRequest {
    @NotBlank(message = "{validation.resolution.required}")
    private String resolution;

    /**
     * 当 resolution = client 时，提供客户端版本用于覆盖服务端
     */
    private SyncRequest.MoodEntryDto entry;
}
