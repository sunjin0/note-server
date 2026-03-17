package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 解决同步冲突请求
 */
@Data
public class ResolveConflictRequest {

    @NotBlank(message = "{validation.entry.id.required}")
    private String entryId;

    @NotBlank(message = "{validation.resolution.required}")
    private String resolution;

    private SyncRequest.MoodEntryDto mergedEntry;
}
