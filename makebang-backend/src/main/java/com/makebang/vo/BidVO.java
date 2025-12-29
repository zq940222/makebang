package com.makebang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidVO {

    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 投标者ID
     */
    private Long developerId;

    /**
     * 报价金额
     */
    private BigDecimal proposedPrice;

    /**
     * 预计完成天数
     */
    private Integer proposedDays;

    /**
     * 投标方案描述
     */
    private String proposal;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 投标者信息
     */
    private UserVO developer;

    /**
     * 投标者详细资料（如果有）
     */
    private DeveloperProfileVO developerProfile;
}
