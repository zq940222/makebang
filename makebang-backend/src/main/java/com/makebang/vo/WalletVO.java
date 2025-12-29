package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包VO
 */
@Data
@Builder
public class WalletVO {

    /**
     * 钱包ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 累计收入
     */
    private BigDecimal totalIncome;

    /**
     * 累计支出
     */
    private BigDecimal totalExpense;

    /**
     * 钱包状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
