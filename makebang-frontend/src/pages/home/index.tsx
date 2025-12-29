import { Card, Row, Col, Statistic, Typography, Button, Space, Tag, Input, Carousel } from 'antd'
import {
  SearchOutlined,
  ProjectOutlined,
  TeamOutlined,
  TransactionOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  CustomerServiceOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

const { Title, Paragraph, Text } = Typography
const { Search } = Input

const Home = () => {
  const navigate = useNavigate()

  const features = [
    {
      icon: <SafetyCertificateOutlined className="text-4xl text-blue-500" />,
      title: '安全担保',
      description: '资金托管,交易有保障',
    },
    {
      icon: <ThunderboltOutlined className="text-4xl text-green-500" />,
      title: '快速匹配',
      description: 'AI智能推荐,精准对接',
    },
    {
      icon: <CustomerServiceOutlined className="text-4xl text-orange-500" />,
      title: '专业服务',
      description: '全程跟进,售后无忧',
    },
  ]

  const categories = [
    { name: 'Web开发', count: 1234 },
    { name: '移动应用', count: 856 },
    { name: '小程序开发', count: 678 },
    { name: '数据分析', count: 432 },
    { name: 'UI设计', count: 567 },
    { name: 'AI/机器学习', count: 234 },
  ]

  return (
    <div className="space-y-8">
      {/* Hero区域 */}
      <Card className="bg-gradient-to-r from-blue-500 to-blue-600 text-white border-none">
        <div className="text-center py-12">
          <Title level={1} className="!text-white !mb-4">
            码客邦 - 连接需求与技术的桥梁
          </Title>
          <Paragraph className="!text-white/90 text-lg mb-8">
            专业程序员接单平台,让每一个想法都能实现
          </Paragraph>
          <Space direction="vertical" size="large" className="w-full max-w-xl">
            <Search
              placeholder="搜索项目需求..."
              enterButton={<><SearchOutlined /> 搜索</>}
              size="large"
              onSearch={(value) => navigate(`/projects?keyword=${value}`)}
            />
            <Space>
              <Button
                size="large"
                type="primary"
                ghost
                onClick={() => navigate('/projects/publish')}
              >
                我要发布需求
              </Button>
              <Button
                size="large"
                onClick={() => navigate('/projects')}
                className="bg-white text-blue-500"
              >
                我要接单赚钱
              </Button>
            </Space>
          </Space>
        </div>
      </Card>

      {/* 数据统计 */}
      <Row gutter={16}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="累计项目"
              value={15678}
              prefix={<ProjectOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="注册程序员"
              value={8234}
              prefix={<TeamOutlined />}
              suffix="人"
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="交易金额"
              value={12560000}
              prefix={<TransactionOutlined />}
              precision={0}
              suffix="元"
            />
          </Card>
        </Col>
      </Row>

      {/* 平台优势 */}
      <Card title="为什么选择码客邦">
        <Row gutter={24}>
          {features.map((feature, index) => (
            <Col xs={24} sm={8} key={index}>
              <div className="text-center p-6">
                {feature.icon}
                <Title level={4} className="mt-4">
                  {feature.title}
                </Title>
                <Text type="secondary">{feature.description}</Text>
              </div>
            </Col>
          ))}
        </Row>
      </Card>

      {/* 热门分类 */}
      <Card
        title="热门分类"
        extra={<Button type="link" onClick={() => navigate('/projects')}>查看全部</Button>}
      >
        <Row gutter={[16, 16]}>
          {categories.map((category, index) => (
            <Col xs={12} sm={8} md={4} key={index}>
              <Card
                hoverable
                className="text-center"
                onClick={() => navigate(`/projects?category=${category.name}`)}
              >
                <div className="font-medium">{category.name}</div>
                <div className="text-gray-400 text-sm mt-1">{category.count}个需求</div>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>

      {/* 最新需求 */}
      <Card
        title="最新需求"
        extra={<Button type="link" onClick={() => navigate('/projects')}>更多需求</Button>}
      >
        <div className="space-y-4">
          {[1, 2, 3].map((item) => (
            <Card
              key={item}
              hoverable
              size="small"
              onClick={() => navigate(`/projects/${item}`)}
            >
              <div className="flex justify-between items-start">
                <div>
                  <div className="font-medium text-lg">
                    企业官网开发 - 响应式设计
                  </div>
                  <div className="text-gray-500 mt-2 line-clamp-2">
                    需要开发一个企业官网,要求响应式设计,支持PC和移动端访问...
                  </div>
                  <Space className="mt-3">
                    <Tag color="blue">React</Tag>
                    <Tag color="green">TypeScript</Tag>
                    <Tag color="orange">响应式</Tag>
                  </Space>
                </div>
                <div className="text-right">
                  <div className="text-xl font-bold text-red-500">
                    ¥5,000 - 10,000
                  </div>
                  <div className="text-gray-400 text-sm mt-1">
                    3人投标
                  </div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </Card>
    </div>
  )
}

export default Home
