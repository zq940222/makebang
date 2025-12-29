import { useState, useEffect, useCallback } from 'react'
import {
  Card, Row, Col, Statistic, Table, Tag, Button, Space, Modal, Form,
  Input, InputNumber, Select, message, Tabs, Empty, Spin
} from 'antd'
import {
  WalletOutlined, ArrowUpOutlined, ArrowDownOutlined,
  BankOutlined, AlipayOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import {
  getMyWallet, getTransactions, recharge, withdraw,
  type WalletVO, type TransactionVO, type RechargeRequest, type WithdrawRequest
} from '@/api/wallet'

const Wallet = () => {
  const [loading, setLoading] = useState(true)
  const [wallet, setWallet] = useState<WalletVO | null>(null)
  const [transactions, setTransactions] = useState<TransactionVO[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [activeTab, setActiveTab] = useState('all')

  // 充值弹窗
  const [rechargeModalOpen, setRechargeModalOpen] = useState(false)
  const [rechargeForm] = Form.useForm()
  const [rechargeLoading, setRechargeLoading] = useState(false)

  // 提现弹窗
  const [withdrawModalOpen, setWithdrawModalOpen] = useState(false)
  const [withdrawForm] = Form.useForm()
  const [withdrawLoading, setWithdrawLoading] = useState(false)

  // 加载钱包信息
  const loadWallet = useCallback(async () => {
    try {
      const res = await getMyWallet()
      setWallet(res.data)
    } catch (error) {
      console.error('加载钱包失败:', error)
    }
  }, [])

  // 加载交易记录
  const loadTransactions = useCallback(async () => {
    setLoading(true)
    try {
      const typeFilter = activeTab === 'all' ? undefined :
                        activeTab === 'income' ? undefined :
                        activeTab === 'expense' ? undefined : undefined

      const res = await getTransactions({
        type: typeFilter,
        current,
        size: pageSize,
      })

      let records = res.data?.records || []

      // 前端过滤收支
      if (activeTab === 'income') {
        records = records.filter(t => t.direction === 1)
      } else if (activeTab === 'expense') {
        records = records.filter(t => t.direction === 2)
      }

      setTransactions(records)
      setTotal(res.data?.total || 0)
    } catch (error) {
      console.error('加载交易记录失败:', error)
    } finally {
      setLoading(false)
    }
  }, [activeTab, current, pageSize])

  useEffect(() => {
    loadWallet()
  }, [loadWallet])

  useEffect(() => {
    loadTransactions()
  }, [loadTransactions])

  // 充值
  const handleRecharge = async (values: RechargeRequest) => {
    setRechargeLoading(true)
    try {
      await recharge(values)
      message.success('充值成功')
      setRechargeModalOpen(false)
      rechargeForm.resetFields()
      loadWallet()
      loadTransactions()
    } catch (error: any) {
      message.error(error.message || '充值失败')
    } finally {
      setRechargeLoading(false)
    }
  }

  // 提现
  const handleWithdraw = async (values: WithdrawRequest) => {
    setWithdrawLoading(true)
    try {
      await withdraw(values)
      message.success('提现申请已提交')
      setWithdrawModalOpen(false)
      withdrawForm.resetFields()
      loadWallet()
      loadTransactions()
    } catch (error: any) {
      message.error(error.message || '提现失败')
    } finally {
      setWithdrawLoading(false)
    }
  }

  // 交易类型颜色映射
  const typeColorMap: Record<number, string> = {
    1: 'green',   // 充值
    2: 'orange',  // 提现
    3: 'blue',    // 支付
    4: 'cyan',    // 收入
    5: 'purple',  // 退款
    6: 'default', // 服务费
  }

  const columns: ColumnsType<TransactionVO> = [
    {
      title: '交易编号',
      dataIndex: 'transactionNo',
      width: 200,
      render: (no) => <span className="font-mono text-xs">{no}</span>,
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
      render: (type, record) => (
        <Tag color={typeColorMap[type]}>{record.typeDesc || '未知'}</Tag>
      ),
    },
    {
      title: '金额',
      dataIndex: 'amount',
      width: 120,
      render: (amount, record) => (
        <span className={record.direction === 1 ? 'text-green-500' : 'text-red-500'}>
          {record.direction === 1 ? '+' : '-'}¥{amount?.toLocaleString()}
        </span>
      ),
    },
    {
      title: '余额变动',
      width: 180,
      render: (_, record) => (
        <span className="text-gray-500">
          ¥{record.balanceBefore?.toLocaleString()} → ¥{record.balanceAfter?.toLocaleString()}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (status, record) => {
        const colorMap: Record<number, string> = {
          0: 'processing',
          1: 'success',
          2: 'error',
        }
        return <Tag color={colorMap[status]}>{record.statusDesc || '未知'}</Tag>
      },
    },
    {
      title: '备注',
      dataIndex: 'remark',
      ellipsis: true,
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      width: 180,
      render: (time) => time?.replace('T', ' ').substring(0, 19),
    },
  ]

  const tabItems = [
    { key: 'all', label: '全部记录' },
    { key: 'income', label: '收入' },
    { key: 'expense', label: '支出' },
  ]

  if (loading && !wallet) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 钱包概览 */}
      <Row gutter={16}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="可用余额"
              value={wallet?.balance || 0}
              precision={2}
              prefix={<WalletOutlined />}
              suffix="元"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="冻结金额"
              value={wallet?.frozenAmount || 0}
              precision={2}
              prefix={<WalletOutlined />}
              suffix="元"
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="累计收入"
              value={wallet?.totalIncome || 0}
              precision={2}
              prefix={<ArrowUpOutlined />}
              suffix="元"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="累计支出"
              value={wallet?.totalExpense || 0}
              precision={2}
              prefix={<ArrowDownOutlined />}
              suffix="元"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 操作按钮 */}
      <Card>
        <Space>
          <Button type="primary" icon={<ArrowUpOutlined />} onClick={() => setRechargeModalOpen(true)}>
            充值
          </Button>
          <Button icon={<ArrowDownOutlined />} onClick={() => setWithdrawModalOpen(true)}>
            提现
          </Button>
        </Space>
      </Card>

      {/* 交易记录 */}
      <Card title="交易记录">
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
          dataSource={transactions}
          rowKey="id"
          loading={loading}
          pagination={{
            current,
            pageSize,
            total,
            showTotal: (t) => `共 ${t} 条记录`,
            showSizeChanger: true,
            onChange: (page, size) => {
              setCurrent(page)
              setPageSize(size)
            },
          }}
          locale={{
            emptyText: <Empty description="暂无交易记录" />,
          }}
        />
      </Card>

      {/* 充值弹窗 */}
      <Modal
        title="账户充值"
        open={rechargeModalOpen}
        onCancel={() => setRechargeModalOpen(false)}
        footer={null}
      >
        <Form
          form={rechargeForm}
          layout="vertical"
          onFinish={handleRecharge}
        >
          <Form.Item
            name="amount"
            label="充值金额"
            rules={[
              { required: true, message: '请输入充值金额' },
              { type: 'number', min: 0.01, message: '充值金额至少0.01元' },
            ]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入充值金额"
              prefix="¥"
              precision={2}
              min={0.01}
              max={100000}
            />
          </Form.Item>
          <Form.Item
            name="paymentMethod"
            label="支付方式"
            initialValue={1}
          >
            <Select>
              <Select.Option value={1}>
                <Space><AlipayOutlined />支付宝</Space>
              </Select.Option>
              <Select.Option value={2}>
                <Space><WalletOutlined />微信支付</Space>
              </Select.Option>
            </Select>
          </Form.Item>
          <div className="text-gray-500 text-sm mb-4">
            注：这是模拟充值，实际项目需要接入支付网关
          </div>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={rechargeLoading} block>
              确认充值
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* 提现弹窗 */}
      <Modal
        title="申请提现"
        open={withdrawModalOpen}
        onCancel={() => setWithdrawModalOpen(false)}
        footer={null}
        width={500}
      >
        <Form
          form={withdrawForm}
          layout="vertical"
          onFinish={handleWithdraw}
        >
          <Form.Item
            name="amount"
            label="提现金额"
            rules={[
              { required: true, message: '请输入提现金额' },
              { type: 'number', min: 1, message: '提现金额至少1元' },
            ]}
            extra={`可用余额：¥${wallet?.balance?.toLocaleString() || 0}`}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入提现金额"
              prefix="¥"
              precision={2}
              min={1}
              max={wallet?.balance || 0}
            />
          </Form.Item>
          <Form.Item
            name="withdrawMethod"
            label="提现方式"
            rules={[{ required: true, message: '请选择提现方式' }]}
            initialValue={1}
          >
            <Select>
              <Select.Option value={1}>
                <Space><AlipayOutlined />支付宝</Space>
              </Select.Option>
              <Select.Option value={2}>
                <Space><BankOutlined />银行卡</Space>
              </Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="account"
            label="收款账号"
            rules={[{ required: true, message: '请输入收款账号' }]}
          >
            <Input placeholder="请输入支付宝账号或银行卡号" />
          </Form.Item>
          <Form.Item
            name="accountName"
            label="收款人姓名"
            rules={[{ required: true, message: '请输入收款人姓名' }]}
          >
            <Input placeholder="请输入收款人真实姓名" />
          </Form.Item>
          <div className="text-gray-500 text-sm mb-4">
            提现申请提交后，将在1-3个工作日内处理
          </div>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={withdrawLoading} block>
              提交申请
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Wallet
