package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.UserSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户设置 Mapper 接口
 */
@Mapper
public interface UserSettingMapper extends BaseMapper<UserSetting> {

    /**
     * 根据用户ID查询设置
     */
    @Select("SELECT * FROM user_settings WHERE user_id = #{userId}")
    UserSetting findByUserId(@Param("userId") String userId);
}
