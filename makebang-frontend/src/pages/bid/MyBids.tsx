import { useState, useEffect, useCallback } from 'react'
import { Card, Table, Tag, Button, Space, message, Popconfirm, Select } from 'antd'
import { EyeOutlined, UndoOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBids, withdrawBid, type BidVO } from '@/api/bid'
import type { ColumnsType } from 'antd/es/table'

const MyBids = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [bids, setBids] = useState<BidVO[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [status, setStatus] = useState<number | undefined>(undefined)

  // 加载投标列表
  const loadBids = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getMyBids({ status, current, size: pageSize })
      setBids(res.data?.records || [])
      setTotal(res.data?.total || 0)
    } catch (error) {
      console.error('加载投标列表失败:', error)
    } finally {
      setLoading(false)
    }
  }, [status, current, pageSize])

  useEffect(() => {
    loadBids()
  }, [loadBids])

  // 撤回投标
  const handleWithdraw = async (id: number) => {
    try {
      await withdrawBid(id)
      message.success('投标已撤回')
      loadBids()
    } catch (error: any) {
      message.error(error.message || '撤回失败')
    }
  }

  // 状态标签
  const getStatusTag = (status: number) => {
    const statusMap: Record<number, { color: string; text: string }> = {
      0: { color: 'processing', text: '待处理' },
      1: { color: 'success', text: '已接受' },
      2: { color: 'error', text: '已拒绝' },
      3: { color: 'default', text: '已撤回' },
    }
    const item = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={item.color}>{item.text}</Tag>
  }

  const columns: ColumnsType<BidVO> = [
    {
      title: '项目',
      dataIndex: 'projectTitle',
      key: 'projectTitle',
      render: (title, record) => (
        <a onClick={() => navigate(`/projects/${record.projectId}`)} className="text-blue-500 hover:underline">
          {title || '未知项目'}
        </a>
      ),
    },
    {
      title: '报价',
      dataIndex: 'proposedPrice',
      key: 'proposedPrice',
      render: (price) => (
        <span className="text-red-500 font-medium">¥{price?.toLocaleString()}</span>
      ),
    },
    {
      title: '预计天数',
      dataIndex: 'proposedDays',
      key: 'proposedDays',
      render: (days) => `${days}天`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: '投标时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (time) => time?.split('T')[0],
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/projects/${record.projectId}`)}
          >
            查看项目
          </Button>
          {record.status === 0 && (
            <Popconfirm
              title="确定撤回此投标？"
              onConfirm={() => handleWithdraw(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link" size="small" danger icon={<UndoOutlined />}>
                撤回
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  // 状态筛选选项
  const statusOptions = [
    { value: undefined, label: '全部状态' },
    { value: 0, label: '待处理' },
    { value: 1, label: '已接受' },
    { value: 2, label: '已拒绝' },
    { value: 3, label: '已撤回' },
  ]

  return (
    <Card
      title="我的投标"
      extra={
        <Select
          value={status}
          onChange={(val) => {
            setStatus(val)
            setCurrent(1)
          }}
          options={statusOptions}
          style={{ width: 120 }}
          placeholder="状态筛选"
          allowClear
        />
      }
    >
      <Table
        columns={columns}
        dataSource={bids}
        rowKey="id"
        loading={loading}
        pagination={{
          current,
          pageSize,
          total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (t) => `共 ${t} 条记录`,
          onChange: (page, size) => {
            setCurrent(page)
            setPageSize(size)
          },
        }}
      />
    </Card>
  )
}

export default MyBids
