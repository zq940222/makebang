package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 项目数据访问接口
 */
@Mapper
public interface ProjectRepository extends BaseMapper<Project> {

    /**
     * 增加浏览量
     */
    @Update("UPDATE project SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加投标数
     */
    @Update("UPDATE project SET bid_count = bid_count + 1 WHERE id = #{id}")
    void incrementBidCount(@Param("id") Long id);

    /**
     * 减少投标数
     */
    @Update("UPDATE project SET bid_count = GREATEST(bid_count - 1, 0) WHERE id = #{id}")
    void decrementBidCount(@Param("id") Long id);

    /**
     * 查询用户发布的项目数量
     */
    @Select("SELECT COUNT(*) FROM project WHERE user_id = #{userId} AND deleted_at IS NULL")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 查询用户已完成的项目数量
     */
    @Select("SELECT COUNT(*) FROM project WHERE user_id = #{userId} AND status = 3 AND deleted_at IS NULL")
    int countCompletedByUserId(@Param("userId") Long userId);

    /**
     * 根据技能搜索项目(使用JSONB包含查询)
     */
    @Select("""
        SELECT * FROM project
        WHERE deleted_at IS NULL
        AND status = 1
        AND skill_requirements @> #{skills}::jsonb
        ORDER BY created_at DESC
        """)
    List<Project> findBySkills(@Param("skills") String skills);

    /**
     * 获取热门技能标签
     */
    @Select("""
        SELECT skill, COUNT(*) as cnt
        FROM project, jsonb_array_elements_text(skill_requirements) as skill
        WHERE deleted_at IS NULL AND status IN (1, 2)
        GROUP BY skill
        ORDER BY cnt DESC
        LIMIT #{limit}
        """)
    List<String> getHotSkills(@Param("limit") int limit);
}
