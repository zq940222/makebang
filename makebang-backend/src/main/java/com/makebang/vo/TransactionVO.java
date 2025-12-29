package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录VO
 */
@Data
@Builder
public class TransactionVO {

    /**
     * 交易ID
     */
    private Long id;

    /**
     * 交易编号
     */
    private String transactionNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易类型
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 关联里程碑ID
     */
    private Long milestoneId;

    /**
     * 关联里程碑标题
     */
    private String milestoneTitle;

    /**
     * 交易状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 收支方向：1-收入 2-支出
     */
    private Integer direction;

    /**
     * 方向描述
     */
    private String directionDesc;
}
