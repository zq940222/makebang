package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.HotKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 热门关键词数据访问层
 */
@Mapper
public interface HotKeywordRepository extends BaseMapper<HotKeyword> {

    /**
     * 获取热门搜索关键词
     */
    @Select("""
            SELECT * FROM hot_keyword
            ORDER BY search_count DESC
            LIMIT #{limit}
            """)
    List<HotKeyword> getHotKeywords(@Param("limit") int limit);

    /**
     * 更新热门关键词（使用数据库函数）
     */
    @Select("SELECT update_hot_keyword(#{keyword})")
    void updateHotKeyword(@Param("keyword") String keyword);

    /**
     * 根据前缀获取热门关键词（用于搜索建议）
     */
    @Select("""
            SELECT keyword FROM hot_keyword
            WHERE keyword LIKE #{prefix} || '%'
            ORDER BY search_count DESC
            LIMIT #{limit}
            """)
    List<String> getHotSuggestions(@Param("prefix") String prefix, @Param("limit") int limit);

    /**
     * 清理过期的热门关键词（30天未搜索的）
     */
    @Update("""
            DELETE FROM hot_keyword
            WHERE last_searched_at < CURRENT_TIMESTAMP - INTERVAL '30 days'
              AND search_count < 10
            """)
    int cleanupOldKeywords();
}
