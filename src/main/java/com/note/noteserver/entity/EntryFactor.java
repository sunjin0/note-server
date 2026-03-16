package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记条目与影响因素关联实体类
 */
@Data
@TableName("entry_factors")
public class EntryFactor {

    /**
     * 关联ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 日记条目ID
     */
    @TableField("entry_id")
    private String entryId;

    /**
     * 影响因素ID
     */
    @TableField("factor_id")
    private String factorId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
