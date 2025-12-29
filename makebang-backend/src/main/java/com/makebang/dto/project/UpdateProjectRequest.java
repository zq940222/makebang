package com.makebang.dto.project;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 更新项目请求DTO
 */
@Data
public class UpdateProjectRequest {

    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @Size(min = 50, message = "描述至少50个字符")
    private String description;

    private Integer categoryId;

    @DecimalMin(value = "100", message = "最低预算不能低于100元")
    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    @Future(message = "截止日期必须是将来的日期")
    private LocalDate deadline;

    private List<String> skillRequirements;

    private List<String> attachmentUrls;
}
