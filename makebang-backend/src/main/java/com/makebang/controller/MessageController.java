package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.common.result.Result;
import com.makebang.dto.message.SendMessageRequest;
import com.makebang.service.MessageService;
import com.makebang.vo.ConversationVO;
import com.makebang.vo.MessageVO;
import com.makebang.vo.NotificationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ========== 消息相关 ==========

    /**
     * 发送消息
     */
    @PostMapping
    public Result<MessageVO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return Result.success(messageService.sendMessage(request));
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/conversation/{conversationId}")
    public Result<IPage<MessageVO>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "50") int size) {
        return Result.success(messageService.getConversationMessages(conversationId, current, size));
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("messageCount", messageService.getUnreadCount());
        result.put("notificationCount", messageService.getUnreadNotificationCount());
        result.put("total", result.get("messageCount") + result.get("notificationCount"));
        return Result.success(result);
    }

    // ========== 会话相关 ==========

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<IPage<ConversationVO>> getConversations(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.getConversations(current, size));
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/conversations/{id}")
    public Result<ConversationVO> getConversation(@PathVariable Long id) {
        return Result.success(messageService.getConversation(id));
    }

    /**
     * 获取或创建与某用户的私聊会话
     */
    @PostMapping("/conversations/private/{userId}")
    public Result<ConversationVO> getOrCreatePrivateConversation(@PathVariable Long userId) {
        return Result.success(messageService.getOrCreatePrivateConversation(userId));
    }

    /**
     * 获取订单会话
     */
    @GetMapping("/conversations/order/{orderId}")
    public Result<ConversationVO> getOrderConversation(@PathVariable Long orderId) {
        return Result.success(messageService.getOrderConversation(orderId));
    }

    /**
     * 标记会话消息为已读
     */
    @PostMapping("/conversations/{conversationId}/read")
    public Result<Void> markConversationAsRead(@PathVariable Long conversationId) {
        messageService.markConversationAsRead(conversationId);
        return Result.success(null);
    }

    // ========== 通知相关 ==========

    /**
     * 获取通知列表
     */
    @GetMapping("/notifications")
    public Result<IPage<NotificationVO>> getNotifications(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.getNotifications(type, current, size));
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/notifications/{id}/read")
    public Result<Void> markNotificationAsRead(@PathVariable Long id) {
        messageService.markNotificationAsRead(id);
        return Result.success(null);
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/notifications/read-all")
    public Result<Void> markAllNotificationsAsRead() {
        messageService.markAllNotificationsAsRead();
        return Result.success(null);
    }
}
