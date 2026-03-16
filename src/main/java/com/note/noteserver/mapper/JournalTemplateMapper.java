package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.JournalTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日记模板 Mapper 接口
 */
@Mapper
public interface JournalTemplateMapper extends BaseMapper<JournalTemplate> {

    /**
     * 查询系统预设模板
     */
    @Select("SELECT * FROM journal_templates WHERE user_id IS NULL AND is_active = TRUE ORDER BY category")
    List<JournalTemplate> findSystemTemplates();

    /**
     * 根据用户ID查询自定义模板
     */
    @Select("SELECT * FROM journal_templates WHERE user_id = #{userId} AND is_active = TRUE ORDER BY category")
    List<JournalTemplate> findByUserId(@Param("userId") String userId);

    /**
     * 根据分类查询模板
     */
    @Select("SELECT * FROM journal_templates WHERE category = #{category} AND is_active = TRUE ORDER BY usage_count DESC")
    List<JournalTemplate> findByCategory(@Param("category") String category);
}
