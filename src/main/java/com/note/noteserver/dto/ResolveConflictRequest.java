package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 解决同步冲突请求
 */
@Data
public class ResolveConflictRequest {

    @NotBlank(message = "条目ID不能为空")
    private String entryId;

    @NotBlank(message = "解决方式不能为空")
    private String resolution;

    private SyncRequest.MoodEntryDto mergedEntry;
}
