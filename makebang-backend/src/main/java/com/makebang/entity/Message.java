package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型：1-文本 2-图片 3-文件 4-系统消息
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附件URL（图片/文件）
     */
    private String attachmentUrl;

    /**
     * 附件名称
     */
    private String attachmentName;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 关联业务类型：order-订单 project-项目
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 消息类型枚举
     */
    public enum Type {
        TEXT(1, "文本"),
        IMAGE(2, "图片"),
        FILE(3, "文件"),
        SYSTEM(4, "系统消息");

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
