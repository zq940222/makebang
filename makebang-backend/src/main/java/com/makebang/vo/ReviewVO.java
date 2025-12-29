package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价VO
 */
@Data
@Builder
public class ReviewVO {

    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 评价者ID
     */
    private Long reviewerId;

    /**
     * 评价者信息
     */
    private UserVO reviewer;

    /**
     * 被评价者ID
     */
    private Long revieweeId;

    /**
     * 被评价者信息
     */
    private UserVO reviewee;

    /**
     * 评价类型
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 综合评分
     */
    private Integer rating;

    /**
     * 技能评分
     */
    private Integer skillRating;

    /**
     * 沟通评分
     */
    private Integer communicationRating;

    /**
     * 态度评分
     */
    private Integer attitudeRating;

    /**
     * 及时性评分
     */
    private Integer timelinessRating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签
     */
    private List<String> tags;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous;

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
