package com.makebang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.dto.message.SendMessageRequest;
import com.makebang.vo.ConversationVO;
import com.makebang.vo.MessageVO;
import com.makebang.vo.NotificationVO;

/**
 * 消息服务接口
 */
public interface MessageService {

    // ========== 消息相关 ==========

    /**
     * 发送消息
     */
    MessageVO sendMessage(SendMessageRequest request);

    /**
     * 获取会话消息列表
     */
    IPage<MessageVO> getConversationMessages(Long conversationId, int current, int size);

    /**
     * 获取未读消息数
     */
    int getUnreadCount();

    // ========== 会话相关 ==========

    /**
     * 获取会话列表
     */
    IPage<ConversationVO> getConversations(int current, int size);

    /**
     * 获取会话详情
     */
    ConversationVO getConversation(Long id);

    /**
     * 获取或创建与某用户的私聊会话
     */
    ConversationVO getOrCreatePrivateConversation(Long userId);

    /**
     * 获取订单会话
     */
    ConversationVO getOrderConversation(Long orderId);

    /**
     * 标记会话消息为已读
     */
    void markConversationAsRead(Long conversationId);

    // ========== 通知相关 ==========

    /**
     * 获取通知列表
     */
    IPage<NotificationVO> getNotifications(Integer type, int current, int size);

    /**
     * 获取未读通知数
     */
    int getUnreadNotificationCount();

    /**
     * 标记通知为已读
     */
    void markNotificationAsRead(Long id);

    /**
     * 标记所有通知为已读
     */
    void markAllNotificationsAsRead();

    /**
     * 发送系统通知
     */
    void sendNotification(Long userId, Integer type, String title, String content, String bizType, Long bizId, String link);
}
