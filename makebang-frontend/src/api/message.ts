import { http } from '@/utils/request'
import type { User, PageResult } from '@/types'

// 消息VO
export interface MessageVO {
  id: number
  conversationId: number
  senderId: number
  sender?: User
  receiverId: number
  receiver?: User
  type: number
  typeDesc?: string
  content: string
  attachmentUrl?: string
  attachmentName?: string
  isRead: boolean
  readAt?: string
  isSelf: boolean
  createdAt: string
}

// 会话VO
export interface ConversationVO {
  id: number
  type: number
  typeDesc?: string
  otherUserId: number
  otherUser?: User
  orderId?: number
  orderNo?: string
  projectId?: number
  projectTitle?: string
  lastMessageContent?: string
  lastMessageAt?: string
  unreadCount: number
  createdAt: string
  updatedAt: string
}

// 通知VO
export interface NotificationVO {
  id: number
  type: number
  typeDesc?: string
  title: string
  content: string
  bizType?: string
  bizId?: number
  link?: string
  isRead: boolean
  readAt?: string
  createdAt: string
}

// 发送消息请求
export interface SendMessageRequest {
  receiverId: number
  type?: number
  content: string
  attachmentUrl?: string
  attachmentName?: string
  orderId?: number
}

// 未读数统计
export interface UnreadCount {
  messageCount: number
  notificationCount: number
  total: number
}

// ========== 消息相关 ==========

/**
 * 发送消息
 */
export const sendMessage = (params: SendMessageRequest) => {
  return http.post<MessageVO>('/v1/messages', params)
}

/**
 * 获取会话消息列表
 */
export const getConversationMessages = (conversationId: number, params: {
  current?: number
  size?: number
}) => {
  return http.get<PageResult<MessageVO>>(`/v1/messages/conversation/${conversationId}`, { params })
}

/**
 * 获取未读数统计
 */
export const getUnreadCount = () => {
  return http.get<UnreadCount>('/v1/messages/unread-count')
}

// ========== 会话相关 ==========

/**
 * 获取会话列表
 */
export const getConversations = (params: {
  current?: number
  size?: number
}) => {
  return http.get<PageResult<ConversationVO>>('/v1/messages/conversations', { params })
}

/**
 * 获取会话详情
 */
export const getConversation = (id: number) => {
  return http.get<ConversationVO>(`/v1/messages/conversations/${id}`)
}

/**
 * 获取或创建与某用户的私聊会话
 */
export const getOrCreatePrivateConversation = (userId: number) => {
  return http.post<ConversationVO>(`/v1/messages/conversations/private/${userId}`)
}

/**
 * 获取订单会话
 */
export const getOrderConversation = (orderId: number) => {
  return http.get<ConversationVO>(`/v1/messages/conversations/order/${orderId}`)
}

/**
 * 标记会话消息为已读
 */
export const markConversationAsRead = (conversationId: number) => {
  return http.post<void>(`/v1/messages/conversations/${conversationId}/read`)
}

// ========== 通知相关 ==========

/**
 * 获取通知列表
 */
export const getNotifications = (params: {
  type?: number
  current?: number
  size?: number
}) => {
  return http.get<PageResult<NotificationVO>>('/v1/messages/notifications', { params })
}

/**
 * 标记通知为已读
 */
export const markNotificationAsRead = (id: number) => {
  return http.post<void>(`/v1/messages/notifications/${id}/read`)
}

/**
 * 标记所有通知为已读
 */
export const markAllNotificationsAsRead = () => {
  return http.post<void>('/v1/messages/notifications/read-all')
}
