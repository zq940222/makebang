import { Rate, Progress, Statistic, Row, Col } from 'antd'
import { StarOutlined, CheckCircleOutlined, LikeOutlined } from '@ant-design/icons'
import type { UserStatsVO } from '@/api/review'

interface UserStatsProps {
  stats: UserStatsVO | null
  loading?: boolean
}

const UserStats: React.FC<UserStatsProps> = ({ stats, loading }) => {
  if (!stats) {
    return null
  }

  return (
    <div className="bg-gray-50 rounded-lg p-4">
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={6}>
          <Statistic
            title="综合评分"
            value={stats.avgRating}
            precision={1}
            prefix={<StarOutlined className="text-yellow-500" />}
            suffix="/ 5"
            loading={loading}
          />
        </Col>
        <Col xs={12} sm={6}>
          <Statistic
            title="完成订单"
            value={stats.completedOrderCount}
            prefix={<CheckCircleOutlined className="text-green-500" />}
            suffix="单"
            loading={loading}
          />
        </Col>
        <Col xs={12} sm={6}>
          <Statistic
            title="收到评价"
            value={stats.reviewCount}
            prefix={<LikeOutlined className="text-blue-500" />}
            suffix="条"
            loading={loading}
          />
        </Col>
        <Col xs={12} sm={6}>
          <Statistic
            title="好评率"
            value={stats.positiveRate}
            precision={0}
            suffix="%"
            valueStyle={{ color: stats.positiveRate >= 80 ? '#3f8600' : '#faad14' }}
            loading={loading}
          />
        </Col>
      </Row>

      {stats.reviewCount > 0 && (
        <div className="mt-4 pt-4 border-t">
          <div className="text-sm text-gray-500 mb-3">详细评分</div>
          <Row gutter={[16, 8]}>
            <Col xs={12} sm={6}>
              <div className="flex items-center">
                <span className="text-gray-500 text-sm w-16">专业技能</span>
                <Rate disabled value={stats.avgSkillRating} className="text-xs" />
                <span className="ml-2 text-sm">{stats.avgSkillRating}</span>
              </div>
            </Col>
            <Col xs={12} sm={6}>
              <div className="flex items-center">
                <span className="text-gray-500 text-sm w-16">沟通能力</span>
                <Rate disabled value={stats.avgCommunicationRating} className="text-xs" />
                <span className="ml-2 text-sm">{stats.avgCommunicationRating}</span>
              </div>
            </Col>
            <Col xs={12} sm={6}>
              <div className="flex items-center">
                <span className="text-gray-500 text-sm w-16">工作态度</span>
                <Rate disabled value={stats.avgAttitudeRating} className="text-xs" />
                <span className="ml-2 text-sm">{stats.avgAttitudeRating}</span>
              </div>
            </Col>
            <Col xs={12} sm={6}>
              <div className="flex items-center">
                <span className="text-gray-500 text-sm w-16">交付及时</span>
                <Rate disabled value={stats.avgTimelinessRating} className="text-xs" />
                <span className="ml-2 text-sm">{stats.avgTimelinessRating}</span>
              </div>
            </Col>
          </Row>
        </div>
      )}
    </div>
  )
}

export default UserStats
