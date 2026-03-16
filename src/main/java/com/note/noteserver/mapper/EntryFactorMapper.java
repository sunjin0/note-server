package com.note.noteserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.note.noteserver.entity.EntryFactor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日记条目与影响因素关联 Mapper 接口
 */
@Mapper
public interface EntryFactorMapper extends BaseMapper<EntryFactor> {

    /**
     * 根据日记条目ID查询关联
     */
    @Select("SELECT * FROM entry_factors WHERE entry_id = #{entryId}")
    List<EntryFactor> findByEntryId(@Param("entryId") String entryId);

    /**
     * 根据影响因素ID查询关联
     */
    @Select("SELECT * FROM entry_factors WHERE factor_id = #{factorId}")
    List<EntryFactor> findByFactorId(@Param("factorId") String factorId);

    /**
     * 删除日记条目的所有关联
     */
    @Select("DELETE FROM entry_factors WHERE entry_id = #{entryId}")
    void deleteByEntryId(@Param("entryId") String entryId);
}
