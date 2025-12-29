package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 投标实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid")
public class Bid extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 投标者ID（程序员）
     */
    private Long developerId;

    /**
     * 报价金额
     */
    private BigDecimal proposedPrice;

    /**
     * 预计完成天数
     */
    private Integer proposedDays;

    /**
     * 投标方案描述
     */
    private String proposal;

    /**
     * 状态: 0-待处理 1-已接受 2-已拒绝 3-已撤回
     */
    private Integer status;

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING(0, "待处理"),
        ACCEPTED(1, "已接受"),
        REJECTED(2, "已拒绝"),
        WITHDRAWN(3, "已撤回");

        public final int code;
        public final String desc;

        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(int code) {
            for (Status s : values()) {
                if (s.code == code) return s.desc;
            }
            return "未知";
        }
    }
}
