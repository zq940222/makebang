import { useState, useEffect, useCallback } from 'react'
import { Card, Row, Col, Tag, Button, Avatar, Descriptions, Divider, List, message, Spin, Empty, Popconfirm } from 'antd'
import { UserOutlined, CalendarOutlined, EyeOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { publicGetProject, type ProjectVO } from '@/api/project'
import { getProjectBids, acceptBid, rejectBid, type BidVO } from '@/api/bid'
import { useSelector } from 'react-redux'
import type { RootState } from '@/store'
import BidModal from '@/components/bid/BidModal'

const ProjectDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [project, setProject] = useState<ProjectVO | null>(null)
  const [bids, setBids] = useState<BidVO[]>([])
  const [bidsLoading, setBidsLoading] = useState(false)
  const [bidModalOpen, setBidModalOpen] = useState(false)

  const { isAuthenticated, user } = useSelector((state: RootState) => state.user)

  // 是否是项目发布者
  const isOwner = user && project && user.id === project.userId

  // 加载项目详情
  useEffect(() => {
    const loadProject = async () => {
      if (!id) return

      setLoading(true)
      try {
        const res = await publicGetProject(Number(id))
        setProject(res.data)
      } catch (error) {
        console.error('加载项目详情失败:', error)
        message.error('加载项目详情失败')
      } finally {
        setLoading(false)
      }
    }
    loadProject()
  }, [id])

  // 加载投标列表
  const loadBids = useCallback(async () => {
    if (!id) return

    setBidsLoading(true)
    try {
      const res = await getProjectBids(Number(id))
      setBids(res.data || [])
    } catch (error) {
      console.error('加载投标列表失败:', error)
    } finally {
      setBidsLoading(false)
    }
  }, [id])

  useEffect(() => {
    loadBids()
  }, [loadBids])

  // 投标处理
  const handleBid = () => {
    if (!isAuthenticated) {
      message.info('请先登录后再投标')
      navigate('/login', { state: { from: `/projects/${id}` } })
      return
    }

    if (user?.userType === 0) {
      message.warning('需求方账号不能投标，请使用程序员账号')
      return
    }

    setBidModalOpen(true)
  }

  // 投标成功回调
  const handleBidSuccess = () => {
    setBidModalOpen(false)
    loadBids()
    // 刷新项目信息
    if (project) {
      setProject({ ...project, bidCount: (project.bidCount || 0) + 1 })
    }
  }

  // 接受投标
  const handleAcceptBid = async (bidId: number) => {
    try {
      await acceptBid(bidId)
      message.success('已接受投标，项目进入进行中状态')
      loadBids()
      // 刷新项目状态
      if (project) {
        setProject({ ...project, status: 2 })
      }
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  // 拒绝投标
  const handleRejectBid = async (bidId: number) => {
    try {
      await rejectBid(bidId)
      message.success('已拒绝该投标')
      loadBids()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  // 状态标签
  const getStatusTag = (status: number) => {
    const statusMap: Record<number, { color: string; text: string }> = {
      0: { color: 'default', text: '草稿' },
      1: { color: 'green', text: '开放中' },
      2: { color: 'blue', text: '进行中' },
      3: { color: 'gold', text: '已完成' },
      4: { color: 'red', text: '已取消' },
      5: { color: 'default', text: '已关闭' },
    }
    const item = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={item.color}>{item.text}</Tag>
  }

  // 投标状态标签
  const getBidStatusTag = (status: number) => {
    const statusMap: Record<number, { color: string; text: string }> = {
      0: { color: 'processing', text: '待处理' },
      1: { color: 'success', text: '已接受' },
      2: { color: 'error', text: '已拒绝' },
      3: { color: 'default', text: '已撤回' },
    }
    const item = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={item.color}>{item.text}</Tag>
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!project) {
    return (
      <Card>
        <Empty description="项目不存在或已被删除" />
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      <Row gutter={24}>
        {/* 左侧详情 */}
        <Col xs={24} lg={16}>
          <Card>
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-2xl font-bold">{project.title}</h1>
              {getStatusTag(project.status)}
            </div>

            <div className="flex items-center space-x-6 text-gray-500 mb-4">
              <span><CalendarOutlined /> 发布于 {project.createdAt?.split('T')[0]}</span>
              <span><EyeOutlined /> {project.viewCount || 0} 浏览</span>
              <span>{project.bidCount || 0} 人投标</span>
              {project.categoryName && <Tag color="cyan">{project.categoryName}</Tag>}
            </div>

            <div className="mb-4">
              {project.skillRequirements?.map((skill) => (
                <Tag key={skill} color="blue" className="mr-2 mb-2">{skill}</Tag>
              ))}
            </div>

            <Divider />

            <div className="mb-6">
              <h3 className="text-lg font-medium mb-3">项目描述</h3>
              <div className="whitespace-pre-wrap text-gray-600">
                {project.description}
              </div>
            </div>

            <Descriptions bordered column={2}>
              <Descriptions.Item label="预算范围">
                <span className="text-red-500 font-bold">
                  ¥{(project.budgetMin || 0).toLocaleString()} - {(project.budgetMax || 0).toLocaleString()}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="期望完成日期">
                {project.deadline}
              </Descriptions.Item>
            </Descriptions>

            {/* 附件 */}
            {project.attachmentUrls && project.attachmentUrls.length > 0 && (
              <div className="mt-6">
                <h3 className="text-lg font-medium mb-3">附件</h3>
                <div className="space-y-2">
                  {project.attachmentUrls.map((url, index) => (
                    <a key={index} href={url} target="_blank" rel="noopener noreferrer" className="block text-blue-500 hover:underline">
                      附件 {index + 1}
                    </a>
                  ))}
                </div>
              </div>
            )}
          </Card>

          {/* 投标列表 */}
          <Card
            title={`投标列表 (${bids.length})`}
            className="mt-6"
            loading={bidsLoading}
          >
            <List
              itemLayout="horizontal"
              dataSource={bids}
              locale={{ emptyText: '暂无投标' }}
              renderItem={(bid) => (
                <List.Item
                  actions={
                    isOwner && project.status === 1 && bid.status === 0
                      ? [
                          <Popconfirm
                            key="accept"
                            title="确定接受此投标？"
                            description="接受后项目将进入进行中状态，其他投标将被自动拒绝"
                            onConfirm={() => handleAcceptBid(bid.id)}
                            okText="确定"
                            cancelText="取消"
                          >
                            <Button type="primary" size="small" icon={<CheckOutlined />}>
                              接受
                            </Button>
                          </Popconfirm>,
                          <Popconfirm
                            key="reject"
                            title="确定拒绝此投标？"
                            onConfirm={() => handleRejectBid(bid.id)}
                            okText="确定"
                            cancelText="取消"
                          >
                            <Button size="small" danger icon={<CloseOutlined />}>
                              拒绝
                            </Button>
                          </Popconfirm>,
                        ]
                      : undefined
                  }
                >
                  <List.Item.Meta
                    avatar={<Avatar icon={<UserOutlined />} src={bid.developer?.avatar} />}
                    title={
                      <div className="flex items-center justify-between">
                        <span>
                          {bid.developer?.username || '未知用户'}
                          <span className="ml-2">{getBidStatusTag(bid.status)}</span>
                        </span>
                        <span className="text-red-500 font-bold">¥{bid.proposedPrice.toLocaleString()}</span>
                      </div>
                    }
                    description={
                      <div>
                        <div className="text-gray-500 mb-2 line-clamp-2">{bid.proposal}</div>
                        <div className="text-gray-400 text-sm">
                          预计: {bid.proposedDays}天 |
                          投标时间: {bid.createdAt?.split('T')[0]}
                        </div>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 右侧信息 */}
        <Col xs={24} lg={8}>
          <Card className="sticky top-20">
            <div className="text-center mb-6">
              <div className="text-3xl font-bold text-red-500 mb-2">
                ¥{(project.budgetMin || 0).toLocaleString()} - {(project.budgetMax || 0).toLocaleString()}
              </div>
              <div className="text-gray-500">项目预算</div>
            </div>

            {project.status === 1 && !isOwner && (
              <Button type="primary" size="large" block onClick={handleBid}>
                立即投标
              </Button>
            )}

            {project.status === 1 && isOwner && (
              <Button type="default" size="large" block disabled>
                这是您发布的项目
              </Button>
            )}

            {project.status !== 1 && (
              <Button type="default" size="large" block disabled>
                {project.status === 0 ? '项目未发布' :
                 project.status === 2 ? '项目进行中' :
                 project.status === 3 ? '项目已完成' : '项目已关闭'}
              </Button>
            )}

            <Divider />

            {/* 发布者信息 */}
            <div className="text-center">
              <Avatar size={64} icon={<UserOutlined />} src={project.user?.avatar} />
              <div className="mt-2 font-medium">{project.user?.username || '未知用户'}</div>
              <div className="text-gray-400 text-sm mt-1">
                项目发布者
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 投标弹窗 */}
      <BidModal
        open={bidModalOpen}
        projectId={Number(id)}
        projectTitle={project.title}
        budgetMin={project.budgetMin || 0}
        budgetMax={project.budgetMax || 0}
        onClose={() => setBidModalOpen(false)}
        onSuccess={handleBidSuccess}
      />
    </div>
  )
}

export default ProjectDetail
