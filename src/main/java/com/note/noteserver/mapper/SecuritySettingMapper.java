package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.SecuritySetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 安全设置 Mapper 接口
 */
@Mapper
public interface SecuritySettingMapper extends BaseMapper<SecuritySetting> {

    /**
     * 根据用户ID查询安全设置
     */
    @Select("SELECT * FROM security_settings WHERE user_id = #{userId}")
    SecuritySetting findByUserId(@Param("userId") String userId);
}
