package com.makebang.dto.order;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建里程碑请求
 */
@Data
public class CreateMilestoneRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100个字符")
    private String title;

    @Size(max = 2000, message = "描述最多2000个字符")
    private String description;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "1", message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "截止日期不能为空")
    @Future(message = "截止日期必须是将来的日期")
    private LocalDate dueDate;
}
