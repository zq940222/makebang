package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索历史数据访问层
 */
@Mapper
public interface SearchHistoryRepository extends BaseMapper<SearchHistory> {

    /**
     * 获取用户最近的搜索历史
     */
    @Select("""
            SELECT DISTINCT ON (keyword) *
            FROM search_history
            WHERE user_id = #{userId}
            ORDER BY keyword, created_at DESC
            LIMIT #{limit}
            """)
    List<SearchHistory> getUserRecentSearches(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 获取用户搜索建议（基于历史）
     */
    @Select("""
            SELECT DISTINCT keyword
            FROM search_history
            WHERE user_id = #{userId} AND keyword LIKE #{prefix} || '%'
            ORDER BY keyword
            LIMIT #{limit}
            """)
    List<String> getSuggestions(@Param("userId") Long userId,
                                @Param("prefix") String prefix,
                                @Param("limit") int limit);

    /**
     * 清除用户搜索历史
     */
    @Select("DELETE FROM search_history WHERE user_id = #{userId}")
    int clearUserHistory(@Param("userId") Long userId);
}
