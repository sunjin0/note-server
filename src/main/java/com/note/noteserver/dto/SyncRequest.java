package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据同步请求
 */
@Data
public class SyncRequest {

    @NotBlank(message = "{validation.device.id.required}")
    private String deviceId;

    private LocalDateTime lastSyncAt;

    @NotNull(message = "{validation.sync.data.required}")
    private SyncData data;

    private List<String> deletedIds;

    /**
     * 同步数据
     */
    @Data
    public static class SyncData {
        private List<MoodEntryDto> entries;
        private UserSettingsDto settings;
        private List<FactorOptionDto> customFactors;
        private List<JournalTemplateDto> customTemplates;
        private SecuritySettingsDto securitySettings;
    }

    /**
     * 日记条目 DTO
     */
    @Data
    public static class MoodEntryDto {
        private String id;
        private String date;
        private String mood;
        private String journal;
        private List<String> factors;
        private List<String> photos;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 用户设置 DTO
     */
    @Data
    public static class UserSettingsDto {
        private Boolean encrypted;
        private LocalDateTime createdAt;
    }

    /**
     * 影响因素 DTO
     */
    @Data
    public static class FactorOptionDto {
        private String id;
        private String label;
        private String emoji;
        private Boolean isCustom;
    }

    /**
     * 日记模板 DTO
     */
    @Data
    public static class JournalTemplateDto {
        private String id;
        private String category;
        private String titleKey;
        private String contentKey;
        private Boolean isCustom;
        private LocalDateTime createdAt;
    }
    /**
     * 安全设置 DTO
     */
    @Data
    public static class SecuritySettingsDto {
        @NotBlank(message = "密码是否启用不能为空")
        private Boolean passwordEnabled;
        @NotBlank(message = "密码哈希不能为空")
        private String passwordHash;
        @NotNull(message = "安全问题列表不能为空")
        private List<SecurityQuestionDto> securityQuestions;
        @NotNull(message = "锁定尝试次数不能为空")
        private Integer lockoutAttempts;
        @NotNull(message = "锁定截止时间不能为空")
        private LocalDateTime lockoutUntil;
    }
    /**
     * 安全问题 DTO
     */
    @Data
    public static class SecurityQuestionDto {
        private String id;
        private String question;
        private String answerHash;
    }
}
