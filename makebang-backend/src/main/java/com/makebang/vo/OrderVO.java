package com.makebang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long projectId;

    private String projectTitle;

    private Long bidId;

    private Long employerId;

    private Long developerId;

    private BigDecimal amount;

    private Integer status;

    private String statusDesc;

    private Integer milestoneCount;

    private LocalDateTime startedAt;

    private LocalDate deadline;

    private LocalDateTime completedAt;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 雇主信息
     */
    private UserVO employer;

    /**
     * 开发者信息
     */
    private UserVO developer;

    /**
     * 项目信息
     */
    private ProjectVO project;

    /**
     * 里程碑列表
     */
    private List<MilestoneVO> milestones;

    /**
     * 当前进度百分比
     */
    private Integer progress;
}
