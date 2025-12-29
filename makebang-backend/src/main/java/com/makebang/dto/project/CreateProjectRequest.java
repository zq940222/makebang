package com.makebang.dto.project;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建项目请求DTO
 */
@Data
public class CreateProjectRequest {

    @NotBlank(message = "项目标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @NotBlank(message = "项目描述不能为空")
    @Size(min = 50, message = "描述至少50个字符")
    private String description;

    @NotNull(message = "请选择项目分类")
    private Integer categoryId;

    @NotNull(message = "请输入最低预算")
    @DecimalMin(value = "100", message = "最低预算不能低于100元")
    private BigDecimal budgetMin;

    @NotNull(message = "请输入最高预算")
    private BigDecimal budgetMax;

    @NotNull(message = "请选择截止日期")
    @Future(message = "截止日期必须是将来的日期")
    private LocalDate deadline;

    @NotEmpty(message = "请选择技能要求")
    private List<String> skillRequirements;

    private List<String> attachmentUrls;

    /**
     * 是否保存为草稿
     */
    private Boolean draft = false;
}
