import { Card, Tabs, Avatar, Button, Descriptions, Tag, Row, Col, Statistic } from 'antd'
import { UserOutlined, EditOutlined, SafetyCertificateOutlined } from '@ant-design/icons'

const UserProfile = () => {
  const user = {
    id: 1,
    username: '张三',
    avatar: null,
    phone: '138****8888',
    email: 'zhangsan@example.com',
    userType: 2,
    realName: '张三',
    verified: true,
    creditScore: 98.5,
    completedOrders: 15,
    totalEarnings: 125000,
    skills: ['React', 'TypeScript', 'Node.js', 'PostgreSQL'],
    bio: '5年全栈开发经验,擅长React和Node.js技术栈',
  }

  const tabItems = [
    {
      key: 'info',
      label: '基本信息',
      children: (
        <div className="space-y-6">
          <Descriptions bordered column={2}>
            <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
            <Descriptions.Item label="手机号">{user.phone}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{user.email}</Descriptions.Item>
            <Descriptions.Item label="实名认证">
              {user.verified ? (
                <Tag color="success" icon={<SafetyCertificateOutlined />}>已认证</Tag>
              ) : (
                <Tag color="warning">未认证</Tag>
              )}
            </Descriptions.Item>
            <Descriptions.Item label="用户类型" span={2}>
              <Tag color="blue">需求方</Tag>
              <Tag color="green">程序员</Tag>
            </Descriptions.Item>
          </Descriptions>

          <Card title="技能标签" size="small">
            {user.skills.map(skill => (
              <Tag key={skill} color="blue" className="mb-2">{skill}</Tag>
            ))}
          </Card>

          <Card title="个人简介" size="small">
            <p className="text-gray-600">{user.bio}</p>
          </Card>
        </div>
      ),
    },
    {
      key: 'projects',
      label: '我的项目',
      children: (
        <div className="text-center py-8 text-gray-400">
          暂无项目数据
        </div>
      ),
    },
    {
      key: 'orders',
      label: '我的订单',
      children: (
        <div className="text-center py-8 text-gray-400">
          暂无订单数据
        </div>
      ),
    },
    {
      key: 'reviews',
      label: '收到的评价',
      children: (
        <div className="text-center py-8 text-gray-400">
          暂无评价数据
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      {/* 头部信息 */}
      <Card>
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-6">
            <Avatar size={80} icon={<UserOutlined />} src={user.avatar} />
            <div>
              <h2 className="text-xl font-bold">
                {user.username}
                {user.verified && (
                  <SafetyCertificateOutlined className="text-green-500 ml-2" />
                )}
              </h2>
              <p className="text-gray-500 mt-1">{user.bio}</p>
            </div>
          </div>
          <Button icon={<EditOutlined />}>编辑资料</Button>
        </div>

        <Row gutter={24} className="mt-6">
          <Col span={6}>
            <Statistic title="信用评分" value={user.creditScore} suffix="分" />
          </Col>
          <Col span={6}>
            <Statistic title="完成订单" value={user.completedOrders} suffix="单" />
          </Col>
          <Col span={6}>
            <Statistic title="累计收入" value={user.totalEarnings} prefix="¥" />
          </Col>
          <Col span={6}>
            <Statistic title="好评率" value={98} suffix="%" />
          </Col>
        </Row>
      </Card>

      {/* 详细信息 */}
      <Card>
        <Tabs items={tabItems} />
      </Card>
    </div>
  )
}

export default UserProfile
