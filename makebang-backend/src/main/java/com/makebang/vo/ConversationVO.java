package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话VO
 */
@Data
@Builder
public class ConversationVO {

    private Long id;

    /**
     * 会话类型
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 对方用户信息
     */
    private UserVO otherUser;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 关联项目标题
     */
    private String projectTitle;

    /**
     * 最后消息内容
     */
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageAt;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
