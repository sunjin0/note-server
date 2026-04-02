package com.note.noteserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据同步响应 - 以 SYNC_DESIGN.md 为准
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {

    /** 服务器时间 */
    private LocalDateTime serverTime;

    /** 服务端下发的 entries（增量） */
    private List<SyncRequest.MoodEntryDto> entries;

    /** 服务端下发的 deletes（增量） */
    private List<SyncRequest.DeleteDto> deletes;

    /** 冲突列表 */
    private List<ConflictInfo> conflicts;

    /** 统计信息 */
    private Stats stats;

    /**
     * 冲突信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private String id;

        private LocalDateTime clientUpdatedAt;
        private LocalDateTime serverUpdatedAt;

        private SyncRequest.MoodEntryDto clientEntry;
        private SyncRequest.MoodEntryDto serverEntry;

        /** server | client | manual */
        private String autoResolution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private Integer uploaded;
        private Integer downloaded;
        private Integer deleted;
        private Integer conflicts;
    }
}
