import { useState, useEffect, useCallback } from 'react'
import { Card, Row, Col, Descriptions, Tag, Button, Steps, Avatar, message, Spin, Empty, Modal, Input, Popconfirm, Progress, Space } from 'antd'
import { UserOutlined, CheckCircleOutlined, ClockCircleOutlined, ExclamationCircleOutlined, StarOutlined, MessageOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { getOrder, confirmPayment, cancelOrder, submitMilestone, reviewMilestone, type OrderVO, type MilestoneVO } from '@/api/order'
import { getOrderReviews, canReviewOrder, type ReviewVO } from '@/api/review'
import { useSelector } from 'react-redux'
import type { RootState } from '@/store'
import ReviewModal from '@/components/review/ReviewModal'
import ReviewList from '@/components/review/ReviewList'

const { TextArea } = Input

const OrderDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [order, setOrder] = useState<OrderVO | null>(null)

  // 提交里程碑弹窗
  const [submitModalOpen, setSubmitModalOpen] = useState(false)
  const [submitNote, setSubmitNote] = useState('')
  const [currentMilestoneId, setCurrentMilestoneId] = useState<number | null>(null)

  // 验收里程碑弹窗
  const [reviewModalOpen, setReviewModalOpen] = useState(false)
  const [reviewNote, setReviewNote] = useState('')

  // 评价相关
  const [orderReviewModalOpen, setOrderReviewModalOpen] = useState(false)
  const [reviews, setReviews] = useState<ReviewVO[]>([])
  const [canReview, setCanReview] = useState(false)

  const { user } = useSelector((state: RootState) => state.user)

  // 判断当前用户角色
  const isEmployer = order?.employerId === user?.id
  const isDeveloper = order?.developerId === user?.id

  // 加载订单详情
  const loadOrder = useCallback(async () => {
    if (!id) return

    setLoading(true)
    try {
      const res = await getOrder(Number(id))
      setOrder(res.data)
    } catch (error) {
      console.error('加载订单失败:', error)
      message.error('加载订单失败')
    } finally {
      setLoading(false)
    }
  }, [id])

  // 加载评价
  const loadReviews = useCallback(async () => {
    if (!id) return
    try {
      const res = await getOrderReviews(Number(id))
      setReviews(res.data || [])
    } catch (error) {
      console.error('加载评价失败:', error)
    }
  }, [id])

  // 检查是否可以评价
  const checkCanReview = useCallback(async () => {
    if (!id) return
    try {
      const res = await canReviewOrder(Number(id))
      setCanReview(res.data)
    } catch (error) {
      console.error('检查评价状态失败:', error)
    }
  }, [id])

  useEffect(() => {
    loadOrder()
    loadReviews()
    checkCanReview()
  }, [loadOrder, loadReviews, checkCanReview])

  // 确认付款
  const handlePay = async () => {
    if (!order) return
    try {
      await confirmPayment(order.id)
      message.success('付款成功')
      loadOrder()
    } catch (error: any) {
      message.error(error.message || '付款失败')
    }
  }

  // 取消订单
  const handleCancel = async () => {
    if (!order) return
    try {
      await cancelOrder(order.id)
      message.success('订单已取消')
      loadOrder()
    } catch (error: any) {
      message.error(error.message || '取消失败')
    }
  }

  // 打开提交弹窗
  const openSubmitModal = (milestoneId: number) => {
    setCurrentMilestoneId(milestoneId)
    setSubmitNote('')
    setSubmitModalOpen(true)
  }

  // 提交里程碑
  const handleSubmit = async () => {
    if (!currentMilestoneId || submitNote.length < 10) {
      message.warning('请输入至少10个字符的提交说明')
      return
    }

    try {
      await submitMilestone(currentMilestoneId, { submitNote })
      message.success('已提交，等待雇主验收')
      setSubmitModalOpen(false)
      loadOrder()
    } catch (error: any) {
      message.error(error.message || '提交失败')
    }
  }

  // 打开验收弹窗
  const openReviewModal = (milestoneId: number) => {
    setCurrentMilestoneId(milestoneId)
    setReviewNote('')
    setReviewModalOpen(true)
  }

  // 验收里程碑
  const handleReview = async (approved: boolean) => {
    if (!currentMilestoneId) return

    try {
      await reviewMilestone(currentMilestoneId, { approved, reviewNote })
      message.success(approved ? '验收通过' : '已驳回')
      setReviewModalOpen(false)
      loadOrder()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  // 状态映射
  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待付款', color: 'orange' },
    1: { text: '进行中', color: 'blue' },
    2: { text: '已交付', color: 'cyan' },
    3: { text: '已完成', color: 'green' },
    4: { text: '已取消', color: 'default' },
    5: { text: '争议中', color: 'red' },
  }

  // 里程碑状态映射
  const milestoneStatusMap: Record<number, { text: string; color: string; icon: React.ReactNode }> = {
    0: { text: '待开始', color: 'default', icon: <ClockCircleOutlined /> },
    1: { text: '进行中', color: 'processing', icon: <ClockCircleOutlined /> },
    2: { text: '已提交', color: 'warning', icon: <ExclamationCircleOutlined /> },
    3: { text: '已验收', color: 'success', icon: <CheckCircleOutlined /> },
    4: { text: '已驳回', color: 'error', icon: <ExclamationCircleOutlined /> },
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!order) {
    return (
      <Card>
        <Empty description="订单不存在" />
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      {/* 订单基本信息 */}
      <Card
        title={
          <div className="flex items-center justify-between">
            <span>订单详情</span>
            <Tag color={statusMap[order.status]?.color} className="text-base">
              {statusMap[order.status]?.text}
            </Tag>
          </div>
        }
        extra={
          <Space>
            {isEmployer && order.status === 0 && (
              <>
                <Button type="primary" onClick={handlePay}>确认付款</Button>
                <Popconfirm title="确定取消订单？" onConfirm={handleCancel}>
                  <Button danger>取消订单</Button>
                </Popconfirm>
              </>
            )}
          </Space>
        }
      >
        <Row gutter={24}>
          <Col xs={24} md={16}>
            <Descriptions column={2} bordered>
              <Descriptions.Item label="订单编号" span={2}>
                <span className="font-mono">{order.orderNo}</span>
              </Descriptions.Item>
              <Descriptions.Item label="项目名称" span={2}>
                <a onClick={() => navigate(`/projects/${order.projectId}`)} className="text-blue-500 hover:underline">
                  {order.projectTitle}
                </a>
              </Descriptions.Item>
              <Descriptions.Item label="订单金额">
                <span className="text-red-500 font-bold text-lg">¥{order.amount?.toLocaleString()}</span>
              </Descriptions.Item>
              <Descriptions.Item label="截止日期">{order.deadline}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{order.createdAt?.split('T')[0]}</Descriptions.Item>
              <Descriptions.Item label="开始时间">{order.startedAt?.split('T')[0] || '-'}</Descriptions.Item>
              {order.completedAt && (
                <Descriptions.Item label="完成时间" span={2}>{order.completedAt?.split('T')[0]}</Descriptions.Item>
              )}
            </Descriptions>
          </Col>
          <Col xs={24} md={8}>
            <Card size="small" title="参与方">
              <div className="space-y-4">
                <div className="flex items-center">
                  <Avatar icon={<UserOutlined />} src={order.employer?.avatar} />
                  <div className="ml-3">
                    <div className="font-medium">{order.employer?.username}</div>
                    <Tag color="gold">雇主</Tag>
                  </div>
                </div>
                <div className="flex items-center">
                  <Avatar icon={<UserOutlined />} src={order.developer?.avatar} />
                  <div className="ml-3">
                    <div className="font-medium">{order.developer?.username}</div>
                    <Tag color="blue">开发者</Tag>
                  </div>
                </div>
              </div>
            </Card>

            <Card size="small" title="项目进度" className="mt-4">
              <Progress
                percent={order.progress || 0}
                status={order.status === 3 ? 'success' : order.status === 4 ? 'exception' : 'active'}
              />
              <div className="text-gray-500 text-sm mt-2">
                已完成 {order.milestones?.filter(m => m.status === 3).length || 0} / {order.milestoneCount} 个里程碑
              </div>
            </Card>
          </Col>
        </Row>
      </Card>

      {/* 里程碑列表 */}
      <Card title="里程碑">
        <Steps
          direction="vertical"
          current={order.milestones?.findIndex(m => m.status === 1 || m.status === 2) ?? -1}
          items={order.milestones?.map((milestone) => ({
            title: (
              <div className="flex items-center justify-between">
                <span>{milestone.title}</span>
                <Tag color={milestoneStatusMap[milestone.status]?.color}>
                  {milestoneStatusMap[milestone.status]?.text}
                </Tag>
              </div>
            ),
            description: (
              <div className="mt-2">
                <div className="text-gray-500">{milestone.description || '无描述'}</div>
                <div className="mt-2 text-sm text-gray-400">
                  金额: ¥{milestone.amount?.toLocaleString()} | 截止: {milestone.dueDate}
                </div>

                {/* 提交说明 */}
                {milestone.submitNote && (
                  <div className="mt-2 p-2 bg-blue-50 rounded">
                    <div className="text-xs text-gray-500">开发者提交说明:</div>
                    <div className="text-sm">{milestone.submitNote}</div>
                  </div>
                )}

                {/* 验收意见 */}
                {milestone.reviewNote && (
                  <div className="mt-2 p-2 bg-green-50 rounded">
                    <div className="text-xs text-gray-500">雇主验收意见:</div>
                    <div className="text-sm">{milestone.reviewNote}</div>
                  </div>
                )}

                {/* 操作按钮 */}
                <div className="mt-3">
                  {/* 开发者操作 */}
                  {isDeveloper && (milestone.status === 1 || milestone.status === 4) && (
                    <Button type="primary" size="small" onClick={() => openSubmitModal(milestone.id)}>
                      提交成果
                    </Button>
                  )}

                  {/* 雇主操作 */}
                  {isEmployer && milestone.status === 2 && (
                    <Button type="primary" size="small" onClick={() => openReviewModal(milestone.id)}>
                      验收
                    </Button>
                  )}
                </div>
              </div>
            ),
            status: milestone.status === 3 ? 'finish' :
                    milestone.status === 4 ? 'error' :
                    (milestone.status === 1 || milestone.status === 2) ? 'process' : 'wait',
            icon: milestoneStatusMap[milestone.status]?.icon,
          })) || []}
        />
      </Card>

      {/* 提交里程碑弹窗 */}
      <Modal
        title="提交里程碑成果"
        open={submitModalOpen}
        onOk={handleSubmit}
        onCancel={() => setSubmitModalOpen(false)}
        okText="提交"
      >
        <div className="mb-2">请描述您完成的工作内容:</div>
        <TextArea
          rows={4}
          value={submitNote}
          onChange={(e) => setSubmitNote(e.target.value)}
          placeholder="请详细描述您完成的工作，至少10个字符"
          showCount
          maxLength={2000}
        />
      </Modal>

      {/* 验收里程碑弹窗 */}
      <Modal
        title="验收里程碑"
        open={reviewModalOpen}
        onCancel={() => setReviewModalOpen(false)}
        footer={[
          <Button key="reject" danger onClick={() => handleReview(false)}>
            驳回
          </Button>,
          <Button key="approve" type="primary" onClick={() => handleReview(true)}>
            通过
          </Button>,
        ]}
      >
        <div className="mb-2">请输入验收意见 (可选):</div>
        <TextArea
          rows={4}
          value={reviewNote}
          onChange={(e) => setReviewNote(e.target.value)}
          placeholder="请输入验收意见或反馈"
          showCount
          maxLength={2000}
        />
      </Modal>

      {/* 评价区域 */}
      {order.status === 3 && (
        <Card
          title={
            <div className="flex items-center">
              <StarOutlined className="mr-2" />
              订单评价
            </div>
          }
          extra={
            canReview && (
              <Button type="primary" icon={<StarOutlined />} onClick={() => setOrderReviewModalOpen(true)}>
                评价订单
              </Button>
            )
          }
        >
          {reviews.length > 0 ? (
            <ReviewList
              reviews={reviews}
              currentUserId={user?.id}
              onReplySuccess={loadReviews}
            />
          ) : (
            <Empty description="暂无评价" />
          )}
        </Card>
      )}

      {/* 评价弹窗 */}
      <ReviewModal
        open={orderReviewModalOpen}
        orderId={order.id}
        orderNo={order.orderNo}
        onClose={() => setOrderReviewModalOpen(false)}
        onSuccess={() => {
          setOrderReviewModalOpen(false)
          loadReviews()
          checkCanReview()
        }}
      />
    </div>
  )
}

export default OrderDetail
