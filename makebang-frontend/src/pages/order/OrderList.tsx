import { useState, useEffect, useCallback } from 'react'
import { Card, Tabs, Table, Tag, Button, Space, Empty, message, Popconfirm, Progress } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useNavigate } from 'react-router-dom'
import { getMyOrders, cancelOrder, confirmPayment, type OrderVO } from '@/api/order'
import { useSelector } from 'react-redux'
import type { RootState } from '@/store'

const OrderList = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [orders, setOrders] = useState<OrderVO[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [activeTab, setActiveTab] = useState('all')
  const [role, setRole] = useState<'employer' | 'developer' | undefined>(undefined)

  const { user } = useSelector((state: RootState) => state.user)

  // 状态映射
  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待付款', color: 'orange' },
    1: { text: '进行中', color: 'blue' },
    2: { text: '已交付', color: 'cyan' },
    3: { text: '已完成', color: 'green' },
    4: { text: '已取消', color: 'default' },
    5: { text: '争议中', color: 'red' },
  }

  // 加载订单列表
  const loadOrders = useCallback(async () => {
    setLoading(true)
    try {
      const statusFilter = activeTab === 'all' ? undefined :
                          activeTab === 'pending' ? 0 :
                          activeTab === 'progress' ? 1 :
                          activeTab === 'delivered' ? 2 :
                          activeTab === 'completed' ? 3 : undefined

      const res = await getMyOrders({
        role,
        status: statusFilter,
        current,
        size: pageSize,
      })
      setOrders(res.data?.records || [])
      setTotal(res.data?.total || 0)
    } catch (error) {
      console.error('加载订单失败:', error)
    } finally {
      setLoading(false)
    }
  }, [activeTab, role, current, pageSize])

  useEffect(() => {
    loadOrders()
  }, [loadOrders])

  // 取消订单
  const handleCancel = async (id: number) => {
    try {
      await cancelOrder(id)
      message.success('订单已取消')
      loadOrders()
    } catch (error: any) {
      message.error(error.message || '取消失败')
    }
  }

  // 确认付款
  const handlePay = async (id: number) => {
    try {
      await confirmPayment(id)
      message.success('付款成功，订单已开始')
      loadOrders()
    } catch (error: any) {
      message.error(error.message || '付款失败')
    }
  }

  // 判断当前用户在订单中的角色
  const getUserRole = (order: OrderVO): 'employer' | 'developer' => {
    return order.employerId === user?.id ? 'employer' : 'developer'
  }

  const columns: ColumnsType<OrderVO> = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      width: 180,
      render: (orderNo) => <span className="font-mono">{orderNo}</span>,
    },
    {
      title: '项目名称',
      dataIndex: 'projectTitle',
      ellipsis: true,
      render: (title, record) => (
        <a onClick={() => navigate(`/projects/${record.projectId}`)} className="text-blue-500 hover:underline">
          {title || '未知项目'}
        </a>
      ),
    },
    {
      title: '金额',
      dataIndex: 'amount',
      width: 120,
      render: (amount) => <span className="text-red-500 font-medium">¥{amount?.toLocaleString()}</span>,
    },
    {
      title: '对方',
      width: 120,
      render: (_, record) => {
        const isEmployer = getUserRole(record) === 'employer'
        const otherParty = isEmployer ? record.developer : record.employer
        return (
          <span>
            {otherParty?.username || '未知'}
            <Tag className="ml-1" color={isEmployer ? 'green' : 'blue'}>
              {isEmployer ? '开发者' : '雇主'}
            </Tag>
          </span>
        )
      },
    },
    {
      title: '进度',
      width: 100,
      render: (_, record) => (
        <Progress
          percent={record.progress || 0}
          size="small"
          status={record.status === 3 ? 'success' : record.status === 4 ? 'exception' : 'active'}
        />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: number) => (
        <Tag color={statusMap[status]?.color}>{statusMap[status]?.text || '未知'}</Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      width: 120,
      render: (time) => time?.split('T')[0],
    },
    {
      title: '操作',
      width: 180,
      render: (_, record) => {
        const isEmployer = getUserRole(record) === 'employer'

        return (
          <Space>
            <Button type="link" size="small" onClick={() => navigate(`/orders/${record.id}`)}>
              查看详情
            </Button>

            {/* 雇主操作 */}
            {isEmployer && record.status === 0 && (
              <>
                <Popconfirm
                  title="确认付款？"
                  description="付款后订单将开始执行"
                  onConfirm={() => handlePay(record.id)}
                >
                  <Button type="link" size="small">付款</Button>
                </Popconfirm>
                <Popconfirm
                  title="确定取消订单？"
                  onConfirm={() => handleCancel(record.id)}
                >
                  <Button type="link" size="small" danger>取消</Button>
                </Popconfirm>
              </>
            )}
          </Space>
        )
      },
    },
  ]

  const tabItems = [
    { key: 'all', label: '全部订单' },
    { key: 'pending', label: '待付款' },
    { key: 'progress', label: '进行中' },
    { key: 'delivered', label: '待验收' },
    { key: 'completed', label: '已完成' },
  ]

  const roleOptions = [
    { key: undefined, label: '全部' },
    { key: 'employer', label: '作为雇主' },
    { key: 'developer', label: '作为开发者' },
  ]

  return (
    <Card
      title="我的订单"
      extra={
        <Space>
          {roleOptions.map(opt => (
            <Button
              key={opt.key || 'all'}
              type={role === opt.key ? 'primary' : 'default'}
              size="small"
              onClick={() => {
                setRole(opt.key as any)
                setCurrent(1)
              }}
            >
              {opt.label}
            </Button>
          ))}
        </Space>
      }
    >
      <Tabs
        items={tabItems}
        activeKey={activeTab}
        onChange={(key) => {
          setActiveTab(key)
          setCurrent(1)
        }}
      />
      <Table
        columns={columns}
        dataSource={orders}
        rowKey="id"
        loading={loading}
        pagination={{
          current,
          pageSize,
          total,
          showTotal: (t) => `共 ${t} 条记录`,
          showSizeChanger: true,
          showQuickJumper: true,
          onChange: (page, size) => {
            setCurrent(page)
            setPageSize(size)
          },
        }}
        locale={{
          emptyText: <Empty description="暂无订单" />,
        }}
      />
    </Card>
  )
}

export default OrderList
