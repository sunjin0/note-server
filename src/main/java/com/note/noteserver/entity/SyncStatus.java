package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 同步状态实体类
 */
@Data
@TableName("sync_status")
public class SyncStatus {

    /**
     * 状态ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 最后同步时间
     */
    @TableField("last_sync_at")
    private LocalDateTime lastSyncAt;

    /**
     * 最后同步结果 (JSON)
     */
    @TableField("last_sync_result")
    private String lastSyncResult;

    /**
     * 待处理变更数
     */
    @TableField("pending_changes")
    private Integer pendingChanges;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

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
