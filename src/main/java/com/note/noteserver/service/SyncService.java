package com.note.noteserver.service;

import com.note.noteserver.dto.*;

/**
 * 数据同步服务接口
 */
public interface SyncService {

    /**
     * 执行数据同步
     */
    SyncResponse sync(String userId, SyncRequest request);

    /**
     * 获取同步状态
     */
    SyncStatusResponse getSyncStatus(String userId);

    /**
     * 解决同步冲突
     */
    void resolveConflict(String userId, ResolveConflictRequest request);
}
