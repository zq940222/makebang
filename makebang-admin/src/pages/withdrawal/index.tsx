import { useState, useEffect, useCallback } from 'react'
import { Card, Table, Select, Tag, Button, Space, message, Modal, Avatar, Descriptions, Input } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { getWithdrawalList, approveWithdrawal, rejectWithdrawal, type TransactionVO } from '@/api/admin'
import type { ColumnsType } from 'antd/es/table'

const { Option } = Select
const { TextArea } = Input

const WithdrawalReview = () => {
  const [loading, setLoading] = useState(false)
  const [withdrawals, setWithdrawals] = useState<TransactionVO[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [status, setStatus] = useState<number | undefined>()

  // 详情弹窗
  const [detailModalOpen, setDetailModalOpen] = useState(false)
  const [selectedWithdrawal, setSelectedWithdrawal] = useState<TransactionVO | null>(null)

  // 拒绝弹窗
  const [rejectModalOpen, setRejectModalOpen] = useState(false)
  const [reason, setReason] = useState('')

  const loadWithdrawals = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getWithdrawalList({
        status,
        current,
        size: pageSize,
      })
      setWithdrawals(res.data.records)
      setTotal(res.data.total)
    } catch (error) {
      console.error('加载提现列表失败:', error)
      message.error('加载提现列表失败')
    } finally {
      setLoading(false)
    }
  }, [status, current, pageSize])

  useEffect(() => {
    loadWithdrawals()
  }, [loadWithdrawals])

  const handleApprove = async (transactionId: number) => {
    try {
      await approveWithdrawal(transactionId)
      message.success('提现已通过')
      loadWithdrawals()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const openRejectModal = (withdrawal: TransactionVO) => {
    setSelectedWithdrawal(withdrawal)
    setReason('')
    setRejectModalOpen(true)
  }

  const handleReject = async () => {
    if (!selectedWithdrawal || !reason.trim()) {
      message.warning('请输入拒绝原因')
      return
    }
    try {
      await rejectWithdrawal(selectedWithdrawal.id, reason)
      message.success('提现已拒绝')
      setRejectModalOpen(false)
      loadWithdrawals()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const openDetailModal = (withdrawal: TransactionVO) => {
    setSelectedWithdrawal(withdrawal)
    setDetailModalOpen(true)
  }

  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待处理', color: 'orange' },
    1: { text: '已完成', color: 'green' },
    2: { text: '已拒绝', color: 'red' },
  }

  const columns: ColumnsType<TransactionVO> = [
    {
      title: '申请人',
      key: 'user',
      render: (_, record) => (
        <div className="flex items-center">
          <Avatar icon={<UserOutlined />} src={record.user?.avatar} size="small" />
          <span className="ml-2">{record.user?.username}</span>
        </div>
      ),
    },
    {
      title: '提现金额',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number) => (
        <span className="text-red-500 font-bold">¥{amount?.toLocaleString()}</span>
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
      title: '申请时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => date?.split('T')[0],
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => openDetailModal(record)}>
            详情
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
        </Space>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold mb-4">提现审核</h2>

      <Card>
        <div className="flex flex-wrap gap-4 mb-4">
          <Select
            placeholder="状态"
            value={status}
            onChange={setStatus}
            style={{ width: 120 }}
            allowClear
          >
            <Option value={0}>待处理</Option>
            <Option value={1}>已完成</Option>
            <Option value={2}>已拒绝</Option>
          </Select>
          <Button type="primary" onClick={loadWithdrawals}>
            刷新
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={withdrawals}
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

      {/* 详情弹窗 */}
      <Modal
        title="提现详情"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={600}
      >
        {selectedWithdrawal && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="申请人">
              {selectedWithdrawal.user?.username}
            </Descriptions.Item>
            <Descriptions.Item label="提现金额">
              <span className="text-red-500 font-bold">
                ¥{selectedWithdrawal.amount?.toLocaleString()}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusMap[selectedWithdrawal.status]?.color}>
                {statusMap[selectedWithdrawal.status]?.text}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="申请时间">
              {selectedWithdrawal.createdAt?.replace('T', ' ')}
            </Descriptions.Item>
            <Descriptions.Item label="备注">
              {selectedWithdrawal.remark || '-'}
            </Descriptions.Item>
            {selectedWithdrawal.status === 2 && (
              <Descriptions.Item label="拒绝原因">
                {selectedWithdrawal.remark || '-'}
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>

      {/* 拒绝弹窗 */}
      <Modal
        title="拒绝提现"
        open={rejectModalOpen}
        onOk={handleReject}
        onCancel={() => setRejectModalOpen(false)}
        okText="确定拒绝"
        okButtonProps={{ danger: true }}
      >
        <div className="py-4">
          <div className="mb-4">
            提现金额:{' '}
            <span className="text-red-500 font-bold">
              ¥{selectedWithdrawal?.amount?.toLocaleString()}
            </span>
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
    </div>
  )
}

export default WithdrawalReview
