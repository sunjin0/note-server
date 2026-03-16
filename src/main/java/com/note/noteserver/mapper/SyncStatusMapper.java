package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.SyncStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 同步状态 Mapper 接口
 */
@Mapper
public interface SyncStatusMapper extends BaseMapper<SyncStatus> {

    /**
     * 根据用户ID查询同步状态列表
     */
    @Select("SELECT * FROM sync_status WHERE user_id = #{userId} AND is_active = TRUE")
    List<SyncStatus> findByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和设备ID查询
     */
    @Select("SELECT * FROM sync_status WHERE user_id = #{userId} AND device_id = #{deviceId}")
    SyncStatus findByUserIdAndDeviceId(@Param("userId") String userId, @Param("deviceId") String deviceId);
}
