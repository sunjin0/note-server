package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记条目实体类
 */
@Data
@TableName("mood_entries")
public class MoodEntry {

    /**
     * 条目ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 日记日期
     */
    @TableField("entry_date")
    private LocalDate entryDate;

    /**
     * 心情类型 (great/good/okay/sad/angry)
     */
    @TableField("mood_type")
    private String moodType;

    /**
     * 日记内容
     */
    @TableField("journal_content")
    private String journalContent;

    /**
     * 是否加密
     */
    @TableField("journal_encrypted")
    private Boolean journalEncrypted;

    /**
     * 照片列表 (JSON)
     */
    private String photos;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 同步版本
     */
    @TableField("sync_version")
    private Integer syncVersion;
}
