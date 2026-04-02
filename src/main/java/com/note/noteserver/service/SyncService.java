package com.note.noteserver.service;

import com.note.noteserver.dto.*;

/**
 * 数据同步服务接口 - 以 SYNC_DESIGN.md 为准
 */
public interface SyncService {

    /**
     * 执行双向同步 (apiSync)
     * 上传本地数据并下载云端数据
     */
    SyncResponse sync(String userId, SyncRequest request);
    
    /**
     * 获取同步状态 (apiGetSyncStatus)
     */
    SyncStatusResponse getSyncStatus(String userId);

    /**
     * 解决同步冲突 (apiResolveConflict)
     */
    void resolveConflict(String userId, String entryId, ResolveConflictRequest request);
}
