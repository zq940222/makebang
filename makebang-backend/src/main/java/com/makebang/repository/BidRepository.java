package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Bid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 投标数据访问接口
 */
@Mapper
public interface BidRepository extends BaseMapper<Bid> {

    /**
     * 查询项目的投标列表
     */
    @Select("""
        SELECT * FROM bid
        WHERE project_id = #{projectId}
        AND deleted_at IS NULL
        ORDER BY created_at DESC
        """)
    List<Bid> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户的投标列表
     */
    @Select("""
        SELECT * FROM bid
        WHERE developer_id = #{developerId}
        AND deleted_at IS NULL
        ORDER BY created_at DESC
        """)
    List<Bid> findByDeveloperId(@Param("developerId") Long developerId);

    /**
     * 检查用户是否已对项目投标
     */
    @Select("""
        SELECT COUNT(*) FROM bid
        WHERE project_id = #{projectId}
        AND developer_id = #{developerId}
        AND deleted_at IS NULL
        """)
    int countByProjectAndDeveloper(@Param("projectId") Long projectId, @Param("developerId") Long developerId);

    /**
     * 查询项目的投标数量
     */
    @Select("""
        SELECT COUNT(*) FROM bid
        WHERE project_id = #{projectId}
        AND deleted_at IS NULL
        """)
    int countByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户的投标数量
     */
    @Select("""
        SELECT COUNT(*) FROM bid
        WHERE developer_id = #{developerId}
        AND deleted_at IS NULL
        """)
    int countByDeveloperId(@Param("developerId") Long developerId);

    /**
     * 查询用户被接受的投标数量
     */
    @Select("""
        SELECT COUNT(*) FROM bid
        WHERE developer_id = #{developerId}
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countAcceptedByDeveloperId(@Param("developerId") Long developerId);
}
