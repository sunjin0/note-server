package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.MoodEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记条目 Mapper 接口
 */
@Mapper
public interface MoodEntryMapper extends BaseMapper<MoodEntry> {

    /**
     * 根据用户ID查询日记列表
     */
    @Select("SELECT * FROM mood_entries WHERE user_id = #{userId} AND is_deleted = FALSE ORDER BY entry_date DESC")
    List<MoodEntry> findByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和日期查询
     */
    @Select("SELECT * FROM mood_entries WHERE user_id = #{userId} AND entry_date = #{entryDate} AND is_deleted = FALSE")
    MoodEntry findByUserIdAndDate(@Param("userId") String userId, @Param("entryDate") LocalDate entryDate);

    /**
     * 根据心情类型查询
     */
    @Select("SELECT * FROM mood_entries WHERE user_id = #{userId} AND mood_type = #{moodType} AND is_deleted = FALSE ORDER BY entry_date DESC")
    List<MoodEntry> findByMoodType(@Param("userId") String userId, @Param("moodType") String moodType);
}
