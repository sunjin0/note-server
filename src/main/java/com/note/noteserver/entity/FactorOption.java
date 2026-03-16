package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 影响因素选项实体类
 */
@Data
@TableName("factor_options")
public class FactorOption {

    /**
     * 选项ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID (NULL表示系统预设)
     */
    @TableField("user_id")
    private String userId;

    /**
     * 标签
     */
    private String label;

    /**
     * 表情符号
     */
    private String emoji;

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
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
