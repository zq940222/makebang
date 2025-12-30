import { useState, useEffect, useCallback } from 'react'
import { Card, Table, Input, Select, Tag, Button, Space, message, Modal, Avatar } from 'antd'
import { SearchOutlined, UserOutlined, EyeOutlined } from '@ant-design/icons'
import { getProjectList, approveProject, rejectProject, takedownProject, type AdminProjectVO } from '@/api/admin'
import type { ColumnsType } from 'antd/es/table'

const { Option } = Select
const { TextArea } = Input

const ProjectReview = () => {
  const [loading, setLoading] = useState(false)
  const [projects, setProjects] = useState<AdminProjectVO[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<number | undefined>()

  // 拒绝/下架弹窗
  const [rejectModalOpen, setRejectModalOpen] = useState(false)
  const [takedownModalOpen, setTakedownModalOpen] = useState(false)
  const [detailModalOpen, setDetailModalOpen] = useState(false)
  const [selectedProject, setSelectedProject] = useState<AdminProjectVO | null>(null)
  const [reason, setReason] = useState('')

  const loadProjects = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getProjectList({
        keyword: keyword || undefined,
        status,
        current,
        size: pageSize,
      })
      setProjects(res.data.records)
      setTotal(res.data.total)
    } catch (error) {
      console.error('加载项目列表失败:', error)
      message.error('加载项目列表失败')
    } finally {
      setLoading(false)
    }
  }, [keyword, status, current, pageSize])

  useEffect(() => {
    loadProjects()
  }, [loadProjects])

  const handleApprove = async (projectId: number) => {
    try {
      await approveProject(projectId)
      message.success('项目已通过审核')
      loadProjects()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const openRejectModal = (project: AdminProjectVO) => {
    setSelectedProject(project)
    setReason('')
    setRejectModalOpen(true)
  }

  const handleReject = async () => {
    if (!selectedProject || !reason.trim()) {
      message.warning('请输入拒绝原因')
      return
    }
    try {
      await rejectProject(selectedProject.id, reason)
      message.success('项目已拒绝')
      setRejectModalOpen(false)
      loadProjects()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const openTakedownModal = (project: AdminProjectVO) => {
    setSelectedProject(project)
    setReason('')
    setTakedownModalOpen(true)
  }

  const handleTakedown = async () => {
    if (!selectedProject || !reason.trim()) {
      message.warning('请输入下架原因')
      return
    }
    try {
      await takedownProject(selectedProject.id, reason)
      message.success('项目已下架')
      setTakedownModalOpen(false)
      loadProjects()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const openDetailModal = (project: AdminProjectVO) => {
    setSelectedProject(project)
    setDetailModalOpen(true)
  }

  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待审核', color: 'orange' },
    1: { text: '招标中', color: 'blue' },
    2: { text: '进行中', color: 'processing' },
    3: { text: '已完成', color: 'success' },
    4: { text: '已取消', color: 'default' },
    5: { text: '已拒绝', color: 'error' },
    6: { text: '已下架', color: 'default' },
  }

  const columns: ColumnsType<AdminProjectVO> = [
    {
      title: '项目',
      key: 'project',
      width: 300,
      render: (_, record) => (
        <div>
          <div className="font-medium line-clamp-1">{record.title}</div>
          <div className="text-xs text-gray-400 line-clamp-1">{record.description}</div>
        </div>
      ),
    },
    {
      title: '发布者',
      key: 'employer',
      render: (_, record) => (
        <div className="flex items-center">
          <Avatar icon={<UserOutlined />} src={record.employer?.avatar} size="small" />
          <span className="ml-2">{record.employer?.username}</span>
        </div>
      ),
    },
    {
      title: '预算',
      key: 'budget',
      render: (_, record) => (
        <span className="text-red-500">
          ¥{record.budgetMin?.toLocaleString()} - ¥{record.budgetMax?.toLocaleString()}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={statusMap[status]?.color}>{statusMap[status]?.text}</Tag>
      ),
    },
    {
      title: '发布时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => date?.split('T')[0],
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => openDetailModal(record)}
          >
            查看
          </Button>
          {record.status === 0 && (
            <>
              <Button
                type="link"
                size="small"
                onClick={() => handleApprove(record.id)}
              >
                通过
              </Button>
              <Button
                type="link"
                danger
                size="small"
                onClick={() => openRejectModal(record)}
              >
                拒绝
              </Button>
            </>
          )}
          {(record.status === 1 || record.status === 2) && (
            <Button
              type="link"
              danger
              size="small"
              onClick={() => openTakedownModal(record)}
            >
              下架
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold mb-4">项目审核</h2>

      <Card>
        <div className="flex flex-wrap gap-4 mb-4">
          <Input
            placeholder="搜索项目标题"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
            allowClear
          />
          <Select
            placeholder="状态"
            value={status}
            onChange={setStatus}
            style={{ width: 120 }}
            allowClear
          >
            <Option value={0}>待审核</Option>
            <Option value={1}>招标中</Option>
            <Option value={2}>进行中</Option>
            <Option value={3}>已完成</Option>
            <Option value={4}>已取消</Option>
            <Option value={5}>已拒绝</Option>
            <Option value={6}>已下架</Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={loadProjects}>
            搜索
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={projects}
          rowKey="id"
          loading={loading}
          pagination={{
            current,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, size) => {
              setCurrent(page)
              setPageSize(size)
            },
          }}
        />
      </Card>

      {/* 项目详情弹窗 */}
      <Modal
        title="项目详情"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={700}
      >
        {selectedProject && (
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-bold">{selectedProject.title}</h3>
              <Tag color={statusMap[selectedProject.status]?.color}>
                {statusMap[selectedProject.status]?.text}
              </Tag>
            </div>
            <div>
              <p className="text-gray-500 mb-1">项目描述:</p>
              <p className="whitespace-pre-wrap">{selectedProject.description}</p>
            </div>
            <div className="flex gap-8">
              <div>
                <p className="text-gray-500 mb-1">预算范围:</p>
                <p className="text-red-500 font-bold">
                  ¥{selectedProject.budgetMin?.toLocaleString()} - ¥{selectedProject.budgetMax?.toLocaleString()}
                </p>
              </div>
              <div>
                <p className="text-gray-500 mb-1">发布时间:</p>
                <p>{selectedProject.createdAt?.replace('T', ' ')}</p>
              </div>
            </div>
            <div>
              <p className="text-gray-500 mb-1">发布者:</p>
              <div className="flex items-center">
                <Avatar icon={<UserOutlined />} src={selectedProject.employer?.avatar} />
                <span className="ml-2">{selectedProject.employer?.username}</span>
              </div>
            </div>
          </div>
        )}
      </Modal>

      {/* 拒绝弹窗 */}
      <Modal
        title="拒绝项目"
        open={rejectModalOpen}
        onOk={handleReject}
        onCancel={() => setRejectModalOpen(false)}
        okText="确定拒绝"
        okButtonProps={{ danger: true }}
      >
        <div className="py-4">
          <div className="mb-4">
            项目: <span className="font-medium">{selectedProject?.title}</span>
          </div>
          <div className="mb-2">拒绝原因:</div>
          <TextArea
            rows={4}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="请输入拒绝原因"
            maxLength={500}
            showCount
          />
        </div>
      </Modal>

      {/* 下架弹窗 */}
      <Modal
        title="下架项目"
        open={takedownModalOpen}
        onOk={handleTakedown}
        onCancel={() => setTakedownModalOpen(false)}
        okText="确定下架"
        okButtonProps={{ danger: true }}
      >
        <div className="py-4">
          <div className="mb-4">
            项目: <span className="font-medium">{selectedProject?.title}</span>
          </div>
          <div className="mb-2">下架原因:</div>
          <TextArea
            rows={4}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="请输入下架原因"
            maxLength={500}
            showCount
          />
        </div>
      </Modal>
    </div>
  )
}

export default ProjectReview
