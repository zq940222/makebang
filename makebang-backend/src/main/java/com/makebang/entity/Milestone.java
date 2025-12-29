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
 * 里程碑实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("milestone")
public class Milestone extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 顺序
     */
    private Integer sequence;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 截止日期
     */
    private LocalDate dueDate;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 开发者提交说明
     */
    private String submitNote;

    /**
     * 雇主验收意见
     */
    private String reviewNote;

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING(0, "待开始"),
        IN_PROGRESS(1, "进行中"),
        SUBMITTED(2, "已提交"),
        APPROVED(3, "已验收"),
        REJECTED(4, "已驳回");

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
