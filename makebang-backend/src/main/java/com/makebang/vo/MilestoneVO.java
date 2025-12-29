package com.makebang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 里程碑VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneVO {

    private Long id;

    private Long orderId;

    private String title;

    private String description;

    private BigDecimal amount;

    private Integer sequence;

    private Integer status;

    private String statusDesc;

    private LocalDate dueDate;

    private LocalDateTime completedAt;

    private String submitNote;

    private String reviewNote;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
