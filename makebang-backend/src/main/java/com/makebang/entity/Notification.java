package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID
     */
    private Long userId;

    /**
     * 通知类型：1-系统通知 2-订单通知 3-投标通知 4-支付通知
     */
    private Integer type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 跳转链接
     */
    private String link;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 通知类型枚举
     */
    public enum Type {
        SYSTEM(1, "系统通知"),
        ORDER(2, "订单通知"),
        BID(3, "投标通知"),
        PAYMENT(4, "支付通知");

        public final int code;
        public final String desc;

        Type(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(int code) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type.desc;
                }
            }
            return "未知";
        }
    }
}
