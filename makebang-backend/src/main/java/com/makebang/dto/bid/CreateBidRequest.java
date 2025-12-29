package com.makebang.dto.bid;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建投标请求
 */
@Data
public class CreateBidRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "报价金额不能为空")
    @DecimalMin(value = "1", message = "报价金额必须大于0")
    private BigDecimal proposedPrice;

    @NotNull(message = "预计天数不能为空")
    @Min(value = 1, message = "预计天数必须大于0")
    @Max(value = 365, message = "预计天数不能超过365天")
    private Integer proposedDays;

    @NotBlank(message = "投标方案不能为空")
    @Size(min = 50, max = 5000, message = "投标方案长度需在50-5000字符之间")
    private String proposal;
}
