package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息VO
 */
@Data
@Builder
public class MessageVO {

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
     * 发送者信息
     */
    private UserVO sender;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者信息
     */
    private UserVO receiver;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附件URL
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
     * 是否是自己发送的
     */
    private Boolean isSelf;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
