package com.makebang.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现请求
 */
@Data
public class WithdrawRequest {

    /**
     * 提现金额
     */
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "1.00", message = "提现金额至少1.00元")
    private BigDecimal amount;

    /**
     * 提现方式：1-支付宝 2-银行卡
     */
    @NotNull(message = "提现方式不能为空")
    private Integer withdrawMethod;

    /**
     * 收款账号
     */
    @NotBlank(message = "收款账号不能为空")
    private String account;

    /**
     * 收款人姓名
     */
    @NotBlank(message = "收款人姓名不能为空")
    private String accountName;

    /**
     * 支付密码（预留）
     */
    private String payPassword;
}
