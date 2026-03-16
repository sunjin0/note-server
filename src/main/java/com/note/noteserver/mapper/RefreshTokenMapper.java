package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 刷新令牌 Mapper 接口
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    /**
     * 根据用户ID查询令牌列表
     */
    @Select("SELECT * FROM refresh_tokens WHERE user_id = #{userId} AND is_revoked = FALSE")
    List<RefreshToken> findByUserId(@Param("userId") String userId);

    /**
     * 根据令牌哈希查询
     */
    @Select("SELECT * FROM refresh_tokens WHERE token_hash = #{tokenHash}")
    RefreshToken findByTokenHash(@Param("tokenHash") String tokenHash);
}
