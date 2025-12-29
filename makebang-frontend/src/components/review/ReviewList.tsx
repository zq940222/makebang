import { useState } from 'react'
import { List, Avatar, Rate, Tag, Space, Button, Input, Empty, message } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { replyReview, type ReviewVO } from '@/api/review'

const { TextArea } = Input

interface ReviewListProps {
  reviews: ReviewVO[]
  currentUserId?: number
  onReplySuccess?: () => void
}

const ReviewList: React.FC<ReviewListProps> = ({ reviews, currentUserId, onReplySuccess }) => {
  const [replyingId, setReplyingId] = useState<number | null>(null)
  const [replyContent, setReplyContent] = useState('')
  const [loading, setLoading] = useState(false)

  const handleReply = async (reviewId: number) => {
    if (!replyContent.trim()) {
      message.warning('请输入回复内容')
      return
    }

    setLoading(true)
    try {
      await replyReview(reviewId, { reply: replyContent })
      message.success('回复成功')
      setReplyingId(null)
      setReplyContent('')
      onReplySuccess?.()
    } catch (error: any) {
      message.error(error.message || '回复失败')
    } finally {
      setLoading(false)
    }
  }

  if (reviews.length === 0) {
    return <Empty description="暂无评价" />
  }

  return (
    <List
      itemLayout="vertical"
      dataSource={reviews}
      renderItem={(review) => (
        <List.Item className="border-b last:border-b-0">
          <div className="flex">
            <Avatar
              icon={<UserOutlined />}
              src={review.reviewer?.avatar}
              size={48}
              className="mr-4"
            />
            <div className="flex-1">
              {/* 评价者信息和评分 */}
              <div className="flex items-center justify-between mb-2">
                <div>
                  <span className="font-medium">{review.reviewer?.username || '匿名用户'}</span>
                  <Tag className="ml-2" color={review.type === 1 ? 'gold' : 'blue'}>
                    {review.typeDesc}
                  </Tag>
                </div>
                <span className="text-gray-400 text-sm">
                  {review.createdAt?.split('T')[0]}
                </span>
              </div>

              {/* 综合评分 */}
              <div className="flex items-center mb-2">
                <Rate disabled value={review.rating} className="text-sm" />
                <span className="ml-2 text-orange-500 font-medium">{review.rating}分</span>
              </div>

              {/* 详细评分 */}
              <div className="flex flex-wrap gap-4 mb-2 text-sm text-gray-500">
                {review.skillRating && (
                  <span>专业技能: <Rate disabled value={review.skillRating} className="text-xs" /></span>
                )}
                {review.communicationRating && (
                  <span>沟通能力: <Rate disabled value={review.communicationRating} className="text-xs" /></span>
                )}
                {review.attitudeRating && (
                  <span>工作态度: <Rate disabled value={review.attitudeRating} className="text-xs" /></span>
                )}
                {review.timelinessRating && (
                  <span>交付及时: <Rate disabled value={review.timelinessRating} className="text-xs" /></span>
                )}
              </div>

              {/* 评价标签 */}
              {review.tags && review.tags.length > 0 && (
                <div className="mb-2">
                  <Space wrap>
                    {review.tags.map((tag, index) => (
                      <Tag key={index} color="blue">{tag}</Tag>
                    ))}
                  </Space>
                </div>
              )}

              {/* 评价内容 */}
              <div className="text-gray-700 mb-2">{review.content}</div>

              {/* 项目信息 */}
              {review.projectTitle && (
                <div className="text-sm text-gray-400 mb-2">
                  项目：{review.projectTitle}
                </div>
              )}

              {/* 回复 */}
              {review.reply && (
                <div className="bg-gray-50 p-3 rounded mt-2">
                  <div className="text-xs text-gray-500 mb-1">
                    {review.reviewee?.username} 的回复：
                  </div>
                  <div className="text-gray-700">{review.reply}</div>
                </div>
              )}

              {/* 回复按钮（被评价者可以回复） */}
              {currentUserId === review.revieweeId && !review.reply && (
                <div className="mt-2">
                  {replyingId === review.id ? (
                    <div className="space-y-2">
                      <TextArea
                        value={replyContent}
                        onChange={(e) => setReplyContent(e.target.value)}
                        placeholder="输入回复内容"
                        rows={2}
                        maxLength={500}
                        showCount
                      />
                      <Space>
                        <Button
                          type="primary"
                          size="small"
                          loading={loading}
                          onClick={() => handleReply(review.id)}
                        >
                          提交回复
                        </Button>
                        <Button size="small" onClick={() => setReplyingId(null)}>
                          取消
                        </Button>
                      </Space>
                    </div>
                  ) : (
                    <Button type="link" size="small" onClick={() => setReplyingId(review.id)}>
                      回复
                    </Button>
                  )}
                </div>
              )}
            </div>
          </div>
        </List.Item>
      )}
    />
  )
}

export default ReviewList
