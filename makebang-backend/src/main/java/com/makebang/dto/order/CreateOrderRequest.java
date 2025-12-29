package com.makebang.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建订单请求（通常由接受投标自动创建，此DTO用于手动创建场景）
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "投标ID不能为空")
    private Long bidId;

    @NotNull(message = "截止日期不能为空")
    private LocalDate deadline;

    private String remark;

    /**
     * 里程碑列表（可选，如果不传则默认单个里程碑）
     */
    @Valid
    private List<CreateMilestoneRequest> milestones;
}
