package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.SyncLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 同步日志 Mapper 接口
 */
@Mapper
public interface SyncLogMapper extends BaseMapper<SyncLog> {

    /**
     * 根据用户ID查询同步日志
     */
    @Select("SELECT * FROM sync_logs WHERE user_id = #{userId} ORDER BY started_at DESC LIMIT #{limit}")
    List<SyncLog> findByUserId(@Param("userId") String userId, @Param("limit") Integer limit);

    /**
     * 根据设备ID查询同步日志
     */
    @Select("SELECT * FROM sync_logs WHERE device_id = #{deviceId} ORDER BY started_at DESC")
    List<SyncLog> findByDeviceId(@Param("deviceId") String deviceId);
}
