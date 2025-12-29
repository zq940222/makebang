package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评价Repository
 */
@Mapper
public interface ReviewRepository extends BaseMapper<Review> {

    /**
     * 根据订单ID查询评价
     */
    @Select("SELECT * FROM review WHERE order_id = #{orderId} AND deleted_at IS NULL")
    List<Review> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询用户是否已对订单评价
     */
    @Select("SELECT * FROM review WHERE order_id = #{orderId} AND reviewer_id = #{reviewerId} AND deleted_at IS NULL LIMIT 1")
    Review findByOrderAndReviewer(@Param("orderId") Long orderId, @Param("reviewerId") Long reviewerId);

    /**
     * 分页查询用户收到的评价
     */
    @Select("SELECT * FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    IPage<Review> findReceivedReviews(Page<Review> page, @Param("userId") Long userId);

    /**
     * 分页查询用户发出的评价
     */
    @Select("SELECT * FROM review WHERE reviewer_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    IPage<Review> findGivenReviews(Page<Review> page, @Param("userId") Long userId);

    /**
     * 计算用户平均评分
     */
    @Select("SELECT COALESCE(AVG(rating), 0) FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL")
    BigDecimal getAvgRating(@Param("userId") Long userId);

    /**
     * 计算用户各维度平均评分
     */
    @Select("SELECT COALESCE(AVG(skill_rating), 0) FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL")
    BigDecimal getAvgSkillRating(@Param("userId") Long userId);

    @Select("SELECT COALESCE(AVG(communication_rating), 0) FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL")
    BigDecimal getAvgCommunicationRating(@Param("userId") Long userId);

    @Select("SELECT COALESCE(AVG(attitude_rating), 0) FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL")
    BigDecimal getAvgAttitudeRating(@Param("userId") Long userId);

    @Select("SELECT COALESCE(AVG(timeliness_rating), 0) FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL")
    BigDecimal getAvgTimelinessRating(@Param("userId") Long userId);

    /**
     * 统计用户评价数
     */
    @Select("SELECT COUNT(*) FROM review WHERE reviewee_id = #{userId} AND deleted_at IS NULL")
    int countByReviewee(@Param("userId") Long userId);

    /**
     * 统计用户好评数（4-5星）
     */
    @Select("SELECT COUNT(*) FROM review WHERE reviewee_id = #{userId} AND rating >= 4 AND deleted_at IS NULL")
    int countPositiveByReviewee(@Param("userId") Long userId);

    /**
     * 根据项目ID查询评价
     */
    @Select("SELECT * FROM review WHERE project_id = #{projectId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<Review> findByProjectId(@Param("projectId") Long projectId);
}
