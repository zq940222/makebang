package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 交易记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transaction")
public class Transaction extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 交易编号
     */
    private String transactionNo;

    /**
     * 钱包ID
     */
    private Long walletId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易类型：1-充值 2-提现 3-支付（托管） 4-收入（里程碑验收） 5-退款 6-平台服务费
     */
    private Integer type;

    /**
     * 交易金额（正数）
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
     * 关联里程碑ID
     */
    private Long milestoneId;

    /**
     * 交易状态：0-处理中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 第三方交易号
     */
    private String outTradeNo;

    /**
     * 交易类型枚举
     */
    public enum Type {
        RECHARGE(1, "充值"),
        WITHDRAW(2, "提现"),
        PAYMENT(3, "支付"),
        INCOME(4, "收入"),
        REFUND(5, "退款"),
        SERVICE_FEE(6, "平台服务费");

        private final int code;
        private final String desc;

        Type(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Type fromCode(int code) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 交易状态枚举
     */
    public enum Status {
        PROCESSING(0, "处理中"),
        SUCCESS(1, "成功"),
        FAILED(2, "失败");

        private final int code;
        private final String desc;

        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
