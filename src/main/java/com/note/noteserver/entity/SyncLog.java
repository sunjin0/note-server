package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 同步日志实体类
 */
@Data
@TableName("sync_logs")
public class SyncLog {

    /**
     * 日志ID (UUID)
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
     * 同步类型 (push/pull/full)
     */
    @TableField("sync_type")
    private String syncType;

    /**
     * 状态 (success/error/conflict)
     */
    private String status;

    /**
     * 上传条目数
     */
    @TableField("entries_uploaded")
    private Integer entriesUploaded;

    /**
     * 下载条目数
     */
    @TableField("entries_downloaded")
    private Integer entriesDownloaded;

    /**
     * 冲突数
     */
    @TableField("conflicts_count")
    private Integer conflictsCount;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 开始时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;
}
