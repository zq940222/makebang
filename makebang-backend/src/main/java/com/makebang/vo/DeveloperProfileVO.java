package com.makebang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 开发者资料VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperProfileVO {

    private Long id;

    private Long userId;

    /**
     * 技能列表
     */
    private List<String> skills;

    /**
     * 工作经验年限
     */
    private Integer experienceYears;

    /**
     * 时薪
     */
    private BigDecimal hourlyRate;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * GitHub地址
     */
    private String githubUrl;

    /**
     * 认证状态
     */
    private Integer certificationStatus;

    /**
     * 信用分
     */
    private Integer creditScore;

    /**
     * 完成订单数
     */
    private Integer completedOrderCount;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;
}
