import { useState, useEffect, useCallback, useRef } from 'react'
import { Card, Tabs, List, Avatar, Badge, Input, Button, Empty, Spin, message } from 'antd'
import { UserOutlined, BellOutlined, MessageOutlined, SendOutlined, CheckOutlined } from '@ant-design/icons'
import {
  getConversations, getConversationMessages, sendMessage, markConversationAsRead,
  getNotifications, markNotificationAsRead, markAllNotificationsAsRead, getUnreadCount,
  type ConversationVO, type MessageVO, type NotificationVO
} from '@/api/message'

const { TextArea } = Input

const MessageCenter = () => {
  const [activeTab, setActiveTab] = useState('chat')
  const [loading, setLoading] = useState(false)

  // 会话相关
  const [conversations, setConversations] = useState<ConversationVO[]>([])
  const [selectedConversation, setSelectedConversation] = useState<ConversationVO | null>(null)
  const [messages, setMessages] = useState<MessageVO[]>([])
  const [messagesLoading, setMessagesLoading] = useState(false)
  const [inputValue, setInputValue] = useState('')
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  // 通知相关
  const [notifications, setNotifications] = useState<NotificationVO[]>([])

  // 未读数
  const [unreadCounts, setUnreadCounts] = useState({ messageCount: 0, notificationCount: 0 })

  // 加载会话列表
  const loadConversations = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getConversations({ current: 1, size: 50 })
      setConversations(res.data?.records || [])
    } catch (error) {
      console.error('加载会话失败:', error)
    } finally {
      setLoading(false)
    }
  }, [])

  // 加载消息列表
  const loadMessages = useCallback(async (conversationId: number) => {
    setMessagesLoading(true)
    try {
      const res = await getConversationMessages(conversationId, { current: 1, size: 100 })
      // 消息按时间倒序返回，需要反转
      setMessages((res.data?.records || []).reverse())

      // 标记已读
      await markConversationAsRead(conversationId)

      // 更新会话列表中的未读数
      setConversations(prev => prev.map(c =>
        c.id === conversationId ? { ...c, unreadCount: 0 } : c
      ))
    } catch (error) {
      console.error('加载消息失败:', error)
    } finally {
      setMessagesLoading(false)
    }
  }, [])

  // 加载通知列表
  const loadNotifications = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getNotifications({ current: 1, size: 50 })
      setNotifications(res.data?.records || [])
    } catch (error) {
      console.error('加载通知失败:', error)
    } finally {
      setLoading(false)
    }
  }, [])

  // 加载未读数
  const loadUnreadCount = useCallback(async () => {
    try {
      const res = await getUnreadCount()
      setUnreadCounts(res.data || { messageCount: 0, notificationCount: 0 })
    } catch (error) {
      console.error('加载未读数失败:', error)
    }
  }, [])

  useEffect(() => {
    loadUnreadCount()
    if (activeTab === 'chat') {
      loadConversations()
    } else {
      loadNotifications()
    }
  }, [activeTab, loadConversations, loadNotifications, loadUnreadCount])

  useEffect(() => {
    if (selectedConversation) {
      loadMessages(selectedConversation.id)
    }
  }, [selectedConversation, loadMessages])

  // 滚动到底部
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // 发送消息
  const handleSend = async () => {
    if (!inputValue.trim() || !selectedConversation) return

    setSending(true)
    try {
      const newMessage = await sendMessage({
        receiverId: selectedConversation.otherUserId,
        content: inputValue.trim(),
        orderId: selectedConversation.orderId,
      })
      setMessages(prev => [...prev, newMessage.data])
      setInputValue('')

      // 更新会话列表
      setConversations(prev => prev.map(c =>
        c.id === selectedConversation.id
          ? { ...c, lastMessageContent: inputValue.trim(), lastMessageAt: new Date().toISOString() }
          : c
      ))
    } catch (error: any) {
      message.error(error.message || '发送失败')
    } finally {
      setSending(false)
    }
  }

  // 标记通知已读
  const handleMarkNotificationRead = async (id: number) => {
    try {
      await markNotificationAsRead(id)
      setNotifications(prev => prev.map(n =>
        n.id === id ? { ...n, isRead: true } : n
      ))
      loadUnreadCount()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 全部已读
  const handleMarkAllRead = async () => {
    try {
      await markAllNotificationsAsRead()
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
      loadUnreadCount()
      message.success('已全部标记为已读')
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 格式化时间
  const formatTime = (time?: string) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
    return time.split('T')[0]
  }

  const tabItems = [
    {
      key: 'chat',
      label: (
        <span>
          <MessageOutlined /> 私信
          {unreadCounts.messageCount > 0 && (
            <Badge count={unreadCounts.messageCount} offset={[5, -5]} size="small" />
          )}
        </span>
      ),
      children: (
        <div className="flex" style={{ height: 500 }}>
          {/* 会话列表 */}
          <div className="w-1/3 border-r overflow-auto">
            {loading ? (
              <div className="flex justify-center py-10"><Spin /></div>
            ) : conversations.length === 0 ? (
              <Empty description="暂无会话" className="py-10" />
            ) : (
              <List
                itemLayout="horizontal"
                dataSource={conversations}
                renderItem={(item) => (
                  <List.Item
                    className={`cursor-pointer hover:bg-gray-50 px-4 ${selectedConversation?.id === item.id ? 'bg-blue-50' : ''}`}
                    onClick={() => setSelectedConversation(item)}
                  >
                    <List.Item.Meta
                      avatar={
                        <Badge count={item.unreadCount} size="small">
                          <Avatar icon={<UserOutlined />} src={item.otherUser?.avatar} />
                        </Badge>
                      }
                      title={
                        <div className="flex items-center">
                          <span>{item.otherUser?.username || '未知用户'}</span>
                          {item.type === 2 && (
                            <span className="ml-2 text-xs text-blue-500">[订单]</span>
                          )}
                        </div>
                      }
                      description={
                        <div className="text-gray-400 text-xs truncate">
                          {item.lastMessageContent || '暂无消息'}
                        </div>
                      }
                    />
                    <div className="text-gray-400 text-xs">{formatTime(item.lastMessageAt)}</div>
                  </List.Item>
                )}
              />
            )}
          </div>

          {/* 聊天内容 */}
          <div className="flex-1 flex flex-col">
            {selectedConversation ? (
              <>
                {/* 聊天头部 */}
                <div className="border-b px-4 py-3 bg-gray-50">
                  <div className="font-medium">{selectedConversation.otherUser?.username}</div>
                  {selectedConversation.projectTitle && (
                    <div className="text-xs text-gray-500">项目：{selectedConversation.projectTitle}</div>
                  )}
                </div>

                {/* 消息列表 */}
                <div className="flex-1 p-4 overflow-auto">
                  {messagesLoading ? (
                    <div className="flex justify-center py-10"><Spin /></div>
                  ) : messages.length === 0 ? (
                    <div className="text-center text-gray-400 py-10">暂无消息，发送第一条消息吧</div>
                  ) : (
                    <div className="space-y-4">
                      {messages.map((msg) => (
                        <div key={msg.id} className={`flex ${msg.isSelf ? 'justify-end' : 'justify-start'}`}>
                          <div className={`flex ${msg.isSelf ? 'flex-row-reverse' : 'flex-row'} items-start max-w-[70%]`}>
                            <Avatar
                              icon={<UserOutlined />}
                              src={msg.isSelf ? msg.sender?.avatar : msg.sender?.avatar}
                              size="small"
                              className={msg.isSelf ? 'ml-2' : 'mr-2'}
                            />
                            <div>
                              <div className={`px-3 py-2 rounded-lg ${msg.isSelf ? 'bg-blue-500 text-white' : 'bg-gray-100'}`}>
                                {msg.content}
                              </div>
                              <div className={`text-xs text-gray-400 mt-1 ${msg.isSelf ? 'text-right' : ''}`}>
                                {formatTime(msg.createdAt)}
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                      <div ref={messagesEndRef} />
                    </div>
                  )}
                </div>

                {/* 输入框 */}
                <div className="border-t p-4">
                  <div className="flex space-x-2">
                    <TextArea
                      value={inputValue}
                      onChange={(e) => setInputValue(e.target.value)}
                      onPressEnter={(e) => {
                        if (!e.shiftKey) {
                          e.preventDefault()
                          handleSend()
                        }
                      }}
                      placeholder="输入消息... (Enter发送，Shift+Enter换行)"
                      autoSize={{ minRows: 1, maxRows: 4 }}
                      className="flex-1"
                    />
                    <Button type="primary" icon={<SendOutlined />} loading={sending} onClick={handleSend}>
                      发送
                    </Button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-gray-400">
                选择一个会话开始聊天
              </div>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'notification',
      label: (
        <span>
          <BellOutlined /> 通知
          {unreadCounts.notificationCount > 0 && (
            <Badge count={unreadCounts.notificationCount} offset={[5, -5]} size="small" />
          )}
        </span>
      ),
      children: (
        <div>
          {unreadCounts.notificationCount > 0 && (
            <div className="mb-4 text-right">
              <Button size="small" icon={<CheckOutlined />} onClick={handleMarkAllRead}>
                全部已读
              </Button>
            </div>
          )}
          {loading ? (
            <div className="flex justify-center py-10"><Spin /></div>
          ) : (
            <List
              itemLayout="horizontal"
              dataSource={notifications}
              renderItem={(item) => (
                <List.Item
                  className={`cursor-pointer hover:bg-gray-50 ${!item.isRead ? 'bg-blue-50' : ''}`}
                  onClick={() => !item.isRead && handleMarkNotificationRead(item.id)}
                >
                  <List.Item.Meta
                    avatar={
                      <Avatar
                        icon={<BellOutlined />}
                        className={item.isRead ? 'bg-gray-300' : 'bg-blue-500'}
                      />
                    }
                    title={
                      <span className={!item.isRead ? 'font-bold' : ''}>
                        {item.title}
                      </span>
                    }
                    description={
                      <div>
                        <div className="text-gray-500">{item.content}</div>
                        <div className="text-gray-400 text-xs mt-1">{formatTime(item.createdAt)}</div>
                      </div>
                    }
                  />
                </List.Item>
              )}
              locale={{
                emptyText: <Empty description="暂无通知" />,
              }}
            />
          )}
        </div>
      ),
    },
  ]

  return (
    <Card title="消息中心">
      <Tabs
        items={tabItems}
        activeKey={activeTab}
        onChange={setActiveTab}
      />
    </Card>
  )
}

export default MessageCenter
