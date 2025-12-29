package com.makebang.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值请求
 */
@Data
public class RechargeRequest {

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额至少0.01元")
    private BigDecimal amount;

    /**
     * 支付方式：1-支付宝 2-微信
     */
    private Integer paymentMethod;
}
