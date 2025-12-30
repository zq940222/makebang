import { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Spin, message } from 'antd'
import {
  UserOutlined,
  ProjectOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  ClockCircleOutlined,
  RiseOutlined,
} from '@ant-design/icons'
import { getDashboardStats, type DashboardStatsVO } from '@/api/admin'
import { Line } from '@ant-design/charts'

const Dashboard = () => {
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<DashboardStatsVO | null>(null)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    setLoading(true)
    try {
      const res = await getDashboardStats()
      setStats(res.data)
    } catch (error) {
      console.error('加载统计数据失败:', error)
      message.error('加载统计数据失败')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!stats) {
    return null
  }

  // 订单趋势图配置
  const orderTrendConfig = {
    data: stats.orderTrend || [],
    xField: 'date',
    yField: 'count',
    smooth: true,
    color: '#1890ff',
    point: {
      size: 4,
      shape: 'circle',
    },
    label: {
      style: {
        fill: '#aaa',
      },
    },
  }

  // 用户趋势图配置
  const userTrendConfig = {
    data: stats.userTrend || [],
    xField: 'date',
    yField: 'count',
    smooth: true,
    color: '#52c41a',
    point: {
      size: 4,
      shape: 'circle',
    },
    label: {
      style: {
        fill: '#aaa',
      },
    },
  }

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold mb-4">数据概览</h2>

      {/* 核心指标 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={stats.totalUsers}
              prefix={<UserOutlined />}
              suffix={
                <span className="text-sm text-green-500">
                  +{stats.todayNewUsers} 今日
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总项目数"
              value={stats.totalProjects}
              prefix={<ProjectOutlined />}
              suffix={
                <span className="text-sm text-blue-500">
                  {stats.activeProjects} 进行中
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总订单数"
              value={stats.totalOrders}
              prefix={<ShoppingCartOutlined />}
              suffix={
                <span className="text-sm text-blue-500">
                  {stats.activeOrders} 进行中
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总交易额"
              value={stats.totalAmount}
              precision={2}
              prefix={<DollarOutlined />}
              suffix="元"
            />
          </Card>
        </Col>
      </Row>

      {/* 待处理事项 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-orange-50">
            <Statistic
              title="待审核项目"
              value={stats.pendingProjects}
              prefix={<ClockCircleOutlined className="text-orange-500" />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-orange-50">
            <Statistic
              title="待处理提现"
              value={stats.pendingWithdrawals}
              prefix={<ClockCircleOutlined className="text-orange-500" />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-green-50">
            <Statistic
              title="平台收入"
              value={stats.platformIncome}
              precision={2}
              prefix={<RiseOutlined className="text-green-500" />}
              suffix="元"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-blue-50">
            <Statistic
              title="今日交易额"
              value={stats.todayAmount}
              precision={2}
              prefix={<DollarOutlined className="text-blue-500" />}
              suffix="元"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 趋势图表 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="最近7天订单趋势">
            <Line {...orderTrendConfig} height={300} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="最近7天用户增长趋势">
            <Line {...userTrendConfig} height={300} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard
