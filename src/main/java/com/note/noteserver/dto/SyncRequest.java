package com.note.noteserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据同步请求 - 以 SYNC_DESIGN.md 为准
 */
@Data
public class SyncRequest {

    @NotBlank(message = "{validation.device.id.required}")
    private String deviceId;

    /**
     * ISO8601。客户端上次完成同步的时间戳
     */
    private LocalDateTime lastSyncAt;

    /**
     * 待上传的 entries（增量）
     */
    @Valid
    private List<MoodEntryDto> entries;

    /**
     * 待上传的 deletes（增量）
     */
    @Valid
    private List<DeleteDto> deletes;

    /**
     * 日记条目 DTO
     */
    @Data
    public static class MoodEntryDto {
        private String id;
        private String date;
        /**
         * 心情类型 (great/good/okay/sad/angry)
         */
        private String mood;
        private List<String> factors;
        private String journal;
        private List<String> photos;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    /**
     * 删除操作 DTO
     */
    @Data
    public static class DeleteDto {
        private String id;
        private LocalDateTime deletedAt;
    }
}
