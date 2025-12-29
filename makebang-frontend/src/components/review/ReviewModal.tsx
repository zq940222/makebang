import { useState } from 'react'
import { Modal, Form, Rate, Input, Checkbox, Tag, Space, message } from 'antd'
import { createReview, type CreateReviewRequest } from '@/api/review'

const { TextArea } = Input

interface ReviewModalProps {
  open: boolean
  orderId: number
  orderNo?: string
  onClose: () => void
  onSuccess: () => void
}

// 评价标签选项
const tagOptions = {
  positive: ['专业能力强', '沟通顺畅', '响应及时', '态度好', '超出预期', '值得推荐'],
  negative: ['沟通困难', '响应慢', '质量一般', '不够专业'],
}

const ReviewModal: React.FC<ReviewModalProps> = ({
  open,
  orderId,
  orderNo,
  onClose,
  onSuccess,
}) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [selectedTags, setSelectedTags] = useState<string[]>([])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()

      setLoading(true)

      const params: CreateReviewRequest = {
        orderId,
        rating: values.rating,
        skillRating: values.skillRating,
        communicationRating: values.communicationRating,
        attitudeRating: values.attitudeRating,
        timelinessRating: values.timelinessRating,
        content: values.content,
        tags: selectedTags,
        isAnonymous: values.isAnonymous,
      }

      await createReview(params)
      message.success('评价提交成功')
      form.resetFields()
      setSelectedTags([])
      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        return // 表单验证错误
      }
      message.error(error.message || '评价提交失败')
    } finally {
      setLoading(false)
    }
  }

  const handleTagClick = (tag: string) => {
    if (selectedTags.includes(tag)) {
      setSelectedTags(selectedTags.filter(t => t !== tag))
    } else if (selectedTags.length < 5) {
      setSelectedTags([...selectedTags, tag])
    } else {
      message.warning('最多选择5个标签')
    }
  }

  return (
    <Modal
      title={`评价订单 ${orderNo || ''}`}
      open={open}
      onOk={handleSubmit}
      onCancel={onClose}
      confirmLoading={loading}
      okText="提交评价"
      width={600}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          rating: 5,
          skillRating: 5,
          communicationRating: 5,
          attitudeRating: 5,
          timelinessRating: 5,
          isAnonymous: false,
        }}
      >
        <Form.Item
          name="rating"
          label="综合评分"
          rules={[{ required: true, message: '请选择综合评分' }]}
        >
          <Rate allowHalf={false} />
        </Form.Item>

        <div className="grid grid-cols-2 gap-4">
          <Form.Item name="skillRating" label="专业技能">
            <Rate allowHalf={false} />
          </Form.Item>
          <Form.Item name="communicationRating" label="沟通能力">
            <Rate allowHalf={false} />
          </Form.Item>
          <Form.Item name="attitudeRating" label="工作态度">
            <Rate allowHalf={false} />
          </Form.Item>
          <Form.Item name="timelinessRating" label="交付及时">
            <Rate allowHalf={false} />
          </Form.Item>
        </div>

        <Form.Item label="评价标签（可选，最多5个）">
          <div className="space-y-2">
            <div>
              <span className="text-gray-500 text-sm mr-2">好评：</span>
              <Space wrap>
                {tagOptions.positive.map(tag => (
                  <Tag
                    key={tag}
                    color={selectedTags.includes(tag) ? 'blue' : undefined}
                    className="cursor-pointer"
                    onClick={() => handleTagClick(tag)}
                  >
                    {tag}
                  </Tag>
                ))}
              </Space>
            </div>
            <div>
              <span className="text-gray-500 text-sm mr-2">待改进：</span>
              <Space wrap>
                {tagOptions.negative.map(tag => (
                  <Tag
                    key={tag}
                    color={selectedTags.includes(tag) ? 'orange' : undefined}
                    className="cursor-pointer"
                    onClick={() => handleTagClick(tag)}
                  >
                    {tag}
                  </Tag>
                ))}
              </Space>
            </div>
          </div>
        </Form.Item>

        <Form.Item
          name="content"
          label="评价内容"
          rules={[
            { required: true, message: '请输入评价内容' },
            { min: 10, message: '评价内容至少10个字符' },
          ]}
        >
          <TextArea
            rows={4}
            placeholder="请详细描述您的合作体验，至少10个字符"
            showCount
            maxLength={1000}
          />
        </Form.Item>

        <Form.Item name="isAnonymous" valuePropName="checked">
          <Checkbox>匿名评价（对方将看不到您的真实信息）</Checkbox>
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default ReviewModal
