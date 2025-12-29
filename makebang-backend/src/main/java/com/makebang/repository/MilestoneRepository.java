package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.Milestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 里程碑数据访问接口
 */
@Mapper
public interface MilestoneRepository extends BaseMapper<Milestone> {

    /**
     * 查询订单的里程碑列表
     */
    @Select("""
        SELECT * FROM milestone
        WHERE order_id = #{orderId}
        AND deleted_at IS NULL
        ORDER BY sequence ASC
        """)
    List<Milestone> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询订单的已完成里程碑数量
     */
    @Select("""
        SELECT COUNT(*) FROM milestone
        WHERE order_id = #{orderId}
        AND status = 3
        AND deleted_at IS NULL
        """)
    int countCompletedByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询订单的当前进行中里程碑
     */
    @Select("""
        SELECT * FROM milestone
        WHERE order_id = #{orderId}
        AND status IN (1, 2)
        AND deleted_at IS NULL
        ORDER BY sequence ASC
        LIMIT 1
        """)
    Milestone findCurrentByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询订单的下一个待开始里程碑
     */
    @Select("""
        SELECT * FROM milestone
        WHERE order_id = #{orderId}
        AND status = 0
        AND deleted_at IS NULL
        ORDER BY sequence ASC
        LIMIT 1
        """)
    Milestone findNextPendingByOrderId(@Param("orderId") Long orderId);
}
