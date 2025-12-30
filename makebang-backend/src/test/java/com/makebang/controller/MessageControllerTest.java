package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.dto.message.SendMessageRequest;
import com.makebang.service.MessageService;
import com.makebang.vo.ConversationVO;
import com.makebang.vo.MessageVO;
import com.makebang.vo.NotificationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MessageController 单元测试
 */
@WebMvcTest(MessageController.class)
@DisplayName("消息控制器测试")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private MessageVO testMessageVO;
    private ConversationVO testConversationVO;
    private NotificationVO testNotificationVO;

    @BeforeEach
    void setUp() {
        testMessageVO = MessageVO.builder()
                .id(1L)
                .conversationId(1L)
                .senderId(1L)
                .content("你好，请问这个项目什么时候可以完成？")
                .type(0)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        testConversationVO = ConversationVO.builder()
                .id(1L)
                .type(0)
                .lastMessage("你好，请问这个项目什么时候可以完成？")
                .lastMessageTime(LocalDateTime.now())
                .unreadCount(1)
                .build();

        testNotificationVO = NotificationVO.builder()
                .id(1L)
                .userId(1L)
                .type(1)
                .title("项目审核通过")
                .content("您的项目「测试项目」已通过审核")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== 消息相关 ==========

    @Test
    @DisplayName("发送消息 - 成功")
    @WithMockUser(username = "testuser")
    void sendMessage_Success() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("测试消息内容");

        when(messageService.sendMessage(any(SendMessageRequest.class))).thenReturn(testMessageVO);

        mockMvc.perform(post("/v1/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.conversationId").value(1));

        verify(messageService).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("发送消息 - 未登录")
    void sendMessage_Unauthorized() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("测试消息");

        mockMvc.perform(post("/v1/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取会话消息列表 - 成功")
    @WithMockUser(username = "testuser")
    void getConversationMessages_Success() throws Exception {
        Page<MessageVO> page = new Page<>(1, 50);
        page.setRecords(Arrays.asList(testMessageVO));
        page.setTotal(1);

        when(messageService.getConversationMessages(eq(1L), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/messages/conversation/1")
                        .param("current", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(messageService).getConversationMessages(eq(1L), anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取未读消息数 - 成功")
    @WithMockUser(username = "testuser")
    void getUnreadCount_Success() throws Exception {
        when(messageService.getUnreadCount()).thenReturn(5);
        when(messageService.getUnreadNotificationCount()).thenReturn(3);

        mockMvc.perform(get("/v1/messages/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.messageCount").value(5))
                .andExpect(jsonPath("$.data.notificationCount").value(3))
                .andExpect(jsonPath("$.data.total").value(8));

        verify(messageService).getUnreadCount();
        verify(messageService).getUnreadNotificationCount();
    }

    // ========== 会话相关 ==========

    @Test
    @DisplayName("获取会话列表 - 成功")
    @WithMockUser(username = "testuser")
    void getConversations_Success() throws Exception {
        Page<ConversationVO> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(testConversationVO));
        page.setTotal(1);

        when(messageService.getConversations(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/messages/conversations")
                        .param("current", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(messageService).getConversations(anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取会话详情 - 成功")
    @WithMockUser(username = "testuser")
    void getConversation_Success() throws Exception {
        when(messageService.getConversation(1L)).thenReturn(testConversationVO);

        mockMvc.perform(get("/v1/messages/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(messageService).getConversation(1L);
    }

    @Test
    @DisplayName("获取或创建与某用户的私聊会话 - 成功")
    @WithMockUser(username = "testuser")
    void getOrCreatePrivateConversation_Success() throws Exception {
        when(messageService.getOrCreatePrivateConversation(2L)).thenReturn(testConversationVO);

        mockMvc.perform(post("/v1/messages/conversations/private/2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(messageService).getOrCreatePrivateConversation(2L);
    }

    @Test
    @DisplayName("获取订单会话 - 成功")
    @WithMockUser(username = "testuser")
    void getOrderConversation_Success() throws Exception {
        testConversationVO.setType(1);
        when(messageService.getOrderConversation(1L)).thenReturn(testConversationVO);

        mockMvc.perform(get("/v1/messages/conversations/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value(1));

        verify(messageService).getOrderConversation(1L);
    }

    @Test
    @DisplayName("标记会话消息为已读 - 成功")
    @WithMockUser(username = "testuser")
    void markConversationAsRead_Success() throws Exception {
        doNothing().when(messageService).markConversationAsRead(1L);

        mockMvc.perform(post("/v1/messages/conversations/1/read")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService).markConversationAsRead(1L);
    }

    // ========== 通知相关 ==========

    @Test
    @DisplayName("获取通知列表 - 成功")
    @WithMockUser(username = "testuser")
    void getNotifications_Success() throws Exception {
        Page<NotificationVO> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(testNotificationVO));
        page.setTotal(1);

        when(messageService.getNotifications(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/messages/notifications")
                        .param("current", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(messageService).getNotifications(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取通知列表 - 按类型筛选")
    @WithMockUser(username = "testuser")
    void getNotifications_ByType() throws Exception {
        Page<NotificationVO> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(testNotificationVO));
        page.setTotal(1);

        when(messageService.getNotifications(eq(1), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/messages/notifications")
                        .param("type", "1")
                        .param("current", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService).getNotifications(eq(1), anyInt(), anyInt());
    }

    @Test
    @DisplayName("标记通知为已读 - 成功")
    @WithMockUser(username = "testuser")
    void markNotificationAsRead_Success() throws Exception {
        doNothing().when(messageService).markNotificationAsRead(1L);

        mockMvc.perform(post("/v1/messages/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService).markNotificationAsRead(1L);
    }

    @Test
    @DisplayName("标记所有通知为已读 - 成功")
    @WithMockUser(username = "testuser")
    void markAllNotificationsAsRead_Success() throws Exception {
        doNothing().when(messageService).markAllNotificationsAsRead();

        mockMvc.perform(post("/v1/messages/notifications/read-all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService).markAllNotificationsAsRead();
    }
}
