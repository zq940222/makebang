package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会话实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation")
public class Conversation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话类型：1-私聊 2-订单会话
     */
    private Integer type;

    /**
     * 参与者1的用户ID
     */
    private Long participant1Id;

    /**
     * 参与者2的用户ID
     */
    private Long participant2Id;

    /**
     * 关联订单ID（订单会话时使用）
     */
    private Long orderId;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息内容（冗余，用于列表显示）
     */
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageAt;

    /**
     * 参与者1未读消息数
     */
    private Integer participant1Unread;

    /**
     * 参与者2未读消息数
     */
    private Integer participant2Unread;

    /**
     * 会话类型枚举
     */
    public enum Type {
        PRIVATE(1, "私聊"),
        ORDER(2, "订单会话");

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
