package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户钱包
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet")
public class Wallet extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
     * 冻结金额（托管中）
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
     * 钱包状态：0-正常 1-冻结
     */
    private Integer status;

    /**
     * 钱包状态枚举
     */
    public enum Status {
        NORMAL(0, "正常"),
        FROZEN(1, "冻结");

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
