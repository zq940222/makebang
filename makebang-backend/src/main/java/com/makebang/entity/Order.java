package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
public class Order extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 投标ID
     */
    private Long bidId;

    /**
     * 雇主ID
     */
    private Long employerId;

    /**
     * 开发者ID
     */
    private Long developerId;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 里程碑数量
     */
    private Integer milestoneCount;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING_PAYMENT(0, "待付款"),
        IN_PROGRESS(1, "进行中"),
        DELIVERED(2, "已交付"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消"),
        DISPUTED(5, "争议中");

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
