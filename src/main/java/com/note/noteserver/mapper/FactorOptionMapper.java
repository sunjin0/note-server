package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.FactorOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 影响因素选项 Mapper 接口
 */
@Mapper
public interface FactorOptionMapper extends BaseMapper<FactorOption> {

    /**
     * 查询系统预设选项
     */
    @Select("SELECT * FROM factor_options WHERE user_id IS NULL AND is_active = TRUE ORDER BY sort_order")
    List<FactorOption> findSystemOptions();

    /**
     * 根据用户ID查询自定义选项
     */
    @Select("SELECT * FROM factor_options WHERE user_id = #{userId} AND is_active = TRUE ORDER BY sort_order")
    List<FactorOption> findByUserId(@Param("userId") String userId);

    /**
     * 查询用户的所有选项（系统+自定义）
     */
    @Select("SELECT * FROM factor_options WHERE (user_id IS NULL OR user_id = #{userId}) AND is_active = TRUE ORDER BY sort_order")
    List<FactorOption> findAllByUserId(@Param("userId") String userId);
}
