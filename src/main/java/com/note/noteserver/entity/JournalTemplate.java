package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记模板实体类
 */
@Data
@TableName("journal_templates")
public class JournalTemplate {

    /**
     * 模板ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID (NULL表示系统预设)
     */
    @TableField("user_id")
    private String userId;

    /**
     * 分类 (work/study/travel/health/life/custom)
     */
    private String category;

    /**
     * 标题键
     */
    @TableField("title_key")
    private String titleKey;

    /**
     * 内容键
     */
    @TableField("content_key")
    private String contentKey;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 是否自定义
     */
    @TableField("is_custom")
    private Boolean isCustom;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 使用次数
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 最后使用时间
     */
    @TableField("last_used_at")
    private LocalDateTime lastUsedAt;

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
}
