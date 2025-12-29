package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.ResultCode;
import com.makebang.dto.message.SendMessageRequest;
import com.makebang.entity.*;
import com.makebang.repository.*;
import com.makebang.service.MessageService;
import com.makebang.service.UserService;
import com.makebang.vo.ConversationVO;
import com.makebang.vo.MessageVO;
import com.makebang.vo.NotificationVO;
import com.makebang.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    // ========== 消息相关 ==========

    @Override
    @Transactional
    public MessageVO sendMessage(SendMessageRequest request) {
        UserVO currentUser = userService.getCurrentUser();
        Long senderId = currentUser.getId();
        Long receiverId = request.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能给自己发送消息");
        }

        // 获取或创建会话
        Conversation conversation;
        if (request.getOrderId() != null) {
            conversation = getOrCreateOrderConversation(request.getOrderId(), senderId, receiverId);
        } else {
            conversation = getOrCreatePrivateConversationEntity(senderId, receiverId);
        }

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(request.getType() != null ? request.getType() : Message.Type.TEXT.code);
        message.setContent(request.getContent());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setAttachmentName(request.getAttachmentName());
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        messageRepository.insert(message);

        // 更新会话最后消息
        String lastContent = request.getContent();
        if (lastContent != null && lastContent.length() > 100) {
            lastContent = lastContent.substring(0, 100) + "...";
        }
        conversationRepository.updateLastMessage(conversation.getId(), message.getId(), lastContent);

        // 更新接收者未读数
        if (conversation.getParticipant1Id().equals(receiverId)) {
            conversationRepository.incrementParticipant1Unread(conversation.getId());
        } else {
            conversationRepository.incrementParticipant2Unread(conversation.getId());
        }

        log.info("消息发送成功: {} -> {}", senderId, receiverId);

        return toMessageVO(message, senderId);
    }

    @Override
    public IPage<MessageVO> getConversationMessages(Long conversationId, int current, int size) {
        UserVO currentUser = userService.getCurrentUser();

        // 验证用户是会话参与者
        Conversation conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }

        if (!conversation.getParticipant1Id().equals(currentUser.getId()) &&
            !conversation.getParticipant2Id().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此会话");
        }

        Page<Message> page = new Page<>(current, size);
        IPage<Message> result = messageRepository.findByConversationId(page, conversationId);

        return result.convert(m -> toMessageVO(m, currentUser.getId()));
    }

    @Override
    public int getUnreadCount() {
        UserVO currentUser = userService.getCurrentUser();
        return messageRepository.countUnreadByUserId(currentUser.getId());
    }

    // ========== 会话相关 ==========

    @Override
    public IPage<ConversationVO> getConversations(int current, int size) {
        UserVO currentUser = userService.getCurrentUser();

        Page<Conversation> page = new Page<>(current, size);
        IPage<Conversation> result = conversationRepository.findByUserId(page, currentUser.getId());

        return result.convert(c -> toConversationVO(c, currentUser.getId()));
    }

    @Override
    public ConversationVO getConversation(Long id) {
        UserVO currentUser = userService.getCurrentUser();

        Conversation conversation = conversationRepository.selectById(id);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }

        if (!conversation.getParticipant1Id().equals(currentUser.getId()) &&
            !conversation.getParticipant2Id().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此会话");
        }

        return toConversationVO(conversation, currentUser.getId());
    }

    @Override
    @Transactional
    public ConversationVO getOrCreatePrivateConversation(Long userId) {
        UserVO currentUser = userService.getCurrentUser();

        if (currentUser.getId().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能与自己创建会话");
        }

        Conversation conversation = getOrCreatePrivateConversationEntity(currentUser.getId(), userId);
        return toConversationVO(conversation, currentUser.getId());
    }

    @Override
    public ConversationVO getOrderConversation(Long orderId) {
        UserVO currentUser = userService.getCurrentUser();

        Conversation conversation = conversationRepository.findByOrderId(orderId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单会话不存在");
        }

        if (!conversation.getParticipant1Id().equals(currentUser.getId()) &&
            !conversation.getParticipant2Id().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此会话");
        }

        return toConversationVO(conversation, currentUser.getId());
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId) {
        UserVO currentUser = userService.getCurrentUser();

        Conversation conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            return;
        }

        // 标记消息为已读
        messageRepository.markAsReadByConversation(conversationId, currentUser.getId());

        // 清空未读数
        if (conversation.getParticipant1Id().equals(currentUser.getId())) {
            conversationRepository.clearParticipant1Unread(conversationId);
        } else if (conversation.getParticipant2Id().equals(currentUser.getId())) {
            conversationRepository.clearParticipant2Unread(conversationId);
        }
    }

    // ========== 通知相关 ==========

    @Override
    public IPage<NotificationVO> getNotifications(Integer type, int current, int size) {
        UserVO currentUser = userService.getCurrentUser();

        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> result = notificationRepository.findByUserId(page, currentUser.getId(), type);

        return result.convert(this::toNotificationVO);
    }

    @Override
    public int getUnreadNotificationCount() {
        UserVO currentUser = userService.getCurrentUser();
        return notificationRepository.countUnreadByUserId(currentUser.getId());
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long id) {
        notificationRepository.markAsRead(id);
    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead() {
        UserVO currentUser = userService.getCurrentUser();
        notificationRepository.markAllAsRead(currentUser.getId());
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, Integer type, String title, String content,
                                  String bizType, Long bizId, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        notification.setLink(link);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationRepository.insert(notification);

        log.info("发送通知给用户 {}: {}", userId, title);
    }

    // ========== 私有方法 ==========

    private Conversation getOrCreatePrivateConversationEntity(Long userId1, Long userId2) {
        Conversation conversation = conversationRepository.findPrivateConversation(userId1, userId2);

        if (conversation == null) {
            conversation = new Conversation();
            conversation.setType(Conversation.Type.PRIVATE.code);
            conversation.setParticipant1Id(userId1);
            conversation.setParticipant2Id(userId2);
            conversation.setParticipant1Unread(0);
            conversation.setParticipant2Unread(0);
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());

            conversationRepository.insert(conversation);
        }

        return conversation;
    }

    private Conversation getOrCreateOrderConversation(Long orderId, Long senderId, Long receiverId) {
        Conversation conversation = conversationRepository.findByOrderId(orderId);

        if (conversation == null) {
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            }

            conversation = new Conversation();
            conversation.setType(Conversation.Type.ORDER.code);
            conversation.setParticipant1Id(order.getEmployerId());
            conversation.setParticipant2Id(order.getDeveloperId());
            conversation.setOrderId(orderId);
            conversation.setProjectId(order.getProjectId());
            conversation.setParticipant1Unread(0);
            conversation.setParticipant2Unread(0);
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());

            conversationRepository.insert(conversation);
        }

        return conversation;
    }

    private MessageVO toMessageVO(Message message, Long currentUserId) {
        User sender = userRepository.selectById(message.getSenderId());
        User receiver = userRepository.selectById(message.getReceiverId());

        return MessageVO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .sender(sender != null ? userService.toVO(sender) : null)
                .receiverId(message.getReceiverId())
                .receiver(receiver != null ? userService.toVO(receiver) : null)
                .type(message.getType())
                .typeDesc(Message.Type.getDesc(message.getType()))
                .content(message.getContent())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentName(message.getAttachmentName())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .isSelf(message.getSenderId().equals(currentUserId))
                .createdAt(message.getCreatedAt())
                .build();
    }

    private ConversationVO toConversationVO(Conversation conversation, Long currentUserId) {
        // 确定对方用户
        Long otherUserId = conversation.getParticipant1Id().equals(currentUserId)
                ? conversation.getParticipant2Id()
                : conversation.getParticipant1Id();

        User otherUser = userRepository.selectById(otherUserId);

        // 获取未读数
        int unreadCount = conversation.getParticipant1Id().equals(currentUserId)
                ? (conversation.getParticipant1Unread() != null ? conversation.getParticipant1Unread() : 0)
                : (conversation.getParticipant2Unread() != null ? conversation.getParticipant2Unread() : 0);

        ConversationVO vo = ConversationVO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .typeDesc(Conversation.Type.getDesc(conversation.getType()))
                .otherUserId(otherUserId)
                .otherUser(otherUser != null ? userService.toVO(otherUser) : null)
                .orderId(conversation.getOrderId())
                .projectId(conversation.getProjectId())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();

        // 加载关联信息
        if (conversation.getOrderId() != null) {
            Order order = orderRepository.selectById(conversation.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
        }

        if (conversation.getProjectId() != null) {
            Project project = projectRepository.selectById(conversation.getProjectId());
            if (project != null) {
                vo.setProjectTitle(project.getTitle());
            }
        }

        return vo;
    }

    private NotificationVO toNotificationVO(Notification notification) {
        return NotificationVO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .typeDesc(Notification.Type.getDesc(notification.getType()))
                .title(notification.getTitle())
                .content(notification.getContent())
                .bizType(notification.getBizType())
                .bizId(notification.getBizId())
                .link(notification.getLink())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
