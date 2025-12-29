package com.makebang.dto.bid;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新投标请求
 */
@Data
public class UpdateBidRequest {

    @DecimalMin(value = "1", message = "报价金额必须大于0")
    private BigDecimal proposedPrice;

    @Min(value = 1, message = "预计天数必须大于0")
    @Max(value = 365, message = "预计天数不能超过365天")
    private Integer proposedDays;

    @Size(min = 50, max = 5000, message = "投标方案长度需在50-5000字符之间")
    private String proposal;
}
