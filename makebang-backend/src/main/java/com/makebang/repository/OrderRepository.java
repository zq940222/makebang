package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单数据访问接口
 */
@Mapper
public interface OrderRepository extends BaseMapper<Order> {

    /**
     * 根据订单编号查询
     */
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo} AND deleted_at IS NULL")
    Order findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询雇主的订单列表
     */
    @Select("""
        SELECT * FROM `order`
        WHERE employer_id = #{employerId}
        AND deleted_at IS NULL
        ORDER BY created_at DESC
        """)
    List<Order> findByEmployerId(@Param("employerId") Long employerId);

    /**
     * 查询开发者的订单列表
     */
    @Select("""
        SELECT * FROM `order`
        WHERE developer_id = #{developerId}
        AND deleted_at IS NULL
        ORDER BY created_at DESC
        """)
    List<Order> findByDeveloperId(@Param("developerId") Long developerId);

    /**
     * 根据项目ID查询订单
     */
    @Select("SELECT * FROM `order` WHERE project_id = #{projectId} AND deleted_at IS NULL")
    Order findByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户（作为雇主或开发者）的订单数量
     */
    @Select("""
        SELECT COUNT(*) FROM `order`
        WHERE (employer_id = #{userId} OR developer_id = #{userId})
        AND deleted_at IS NULL
        """)
    int countByUserId(@Param("userId") Long userId);

    /**
     * 查询用户已完成的订单数量
     */
    @Select("""
        SELECT COUNT(*) FROM `order`
        WHERE developer_id = #{developerId}
        AND status = 3
        AND deleted_at IS NULL
        """)
    int countCompletedByDeveloperId(@Param("developerId") Long developerId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE `order` SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
