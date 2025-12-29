package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户统计VO
 */
@Data
@Builder
public class UserStatsVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;

    /**
     * 技能平均分
     */
    private BigDecimal avgSkillRating;

    /**
     * 沟通平均分
     */
    private BigDecimal avgCommunicationRating;

    /**
     * 态度平均分
     */
    private BigDecimal avgAttitudeRating;

    /**
     * 及时性平均分
     */
    private BigDecimal avgTimelinessRating;

    /**
     * 评价总数
     */
    private Integer reviewCount;

    /**
     * 完成订单数
     */
    private Integer completedOrderCount;

    /**
     * 好评数（4-5星）
     */
    private Integer positiveCount;

    /**
     * 好评率
     */
    private BigDecimal positiveRate;
}
