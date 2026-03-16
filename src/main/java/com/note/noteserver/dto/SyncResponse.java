package com.note.noteserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据同步响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {

    private LocalDateTime serverTimestamp;
    private List<SyncRequest.MoodEntryDto> entries;
    private SyncRequest.UserSettingsDto settings;
    private List<SyncRequest.FactorOptionDto> customFactors;
    private List<SyncRequest.JournalTemplateDto> customTemplates;
    private SyncRequest.SecuritySettingsDto securitySettings;
    private List<String> deletedIds;
    private List<ConflictInfo> conflicts;

    /**
     * 冲突信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private String entryId;
        private String resolution;
        private SyncRequest.MoodEntryDto mergedEntry;
    }
}
