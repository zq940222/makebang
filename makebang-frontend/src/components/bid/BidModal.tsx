import { useState } from 'react'
import { Modal, Form, InputNumber, Input, message, Alert } from 'antd'
import { createBid, type CreateBidRequest } from '@/api/bid'

const { TextArea } = Input

interface BidModalProps {
  open: boolean
  projectId: number
  projectTitle: string
  budgetMin: number
  budgetMax: number
  onClose: () => void
  onSuccess: () => void
}

const BidModal = ({
  open,
  projectId,
  projectTitle,
  budgetMin,
  budgetMax,
  onClose,
  onSuccess,
}: BidModalProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()

      setLoading(true)
      const request: CreateBidRequest = {
        projectId,
        proposedPrice: values.proposedPrice,
        proposedDays: values.proposedDays,
        proposal: values.proposal,
      }

      await createBid(request)
      message.success('投标成功！')
      form.resetFields()
      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        return
      }
      message.error(error.message || '投标失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = () => {
    form.resetFields()
    onClose()
  }

  return (
    <Modal
      title="投标报价"
      open={open}
      onOk={handleSubmit}
      onCancel={handleCancel}
      confirmLoading={loading}
      okText="提交投标"
      cancelText="取消"
      width={600}
      destroyOnClose
    >
      <Alert
        message={`项目: ${projectTitle}`}
        description={`预算范围: ¥${budgetMin.toLocaleString()} - ¥${budgetMax.toLocaleString()}`}
        type="info"
        showIcon
        className="mb-4"
      />

      <Form
        form={form}
        layout="vertical"
        initialValues={{
          proposedPrice: budgetMin,
          proposedDays: 7,
        }}
      >
        <Form.Item
          name="proposedPrice"
          label="报价金额 (元)"
          rules={[
            { required: true, message: '请输入报价金额' },
          ]}
        >
          <InputNumber
            min={1}
            max={10000000}
            className="w-full"
            formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
            parser={value => value!.replace(/¥\s?|(,*)/g, '')}
            placeholder="请输入您的报价"
          />
        </Form.Item>

        <Form.Item
          name="proposedDays"
          label="预计完成天数"
          rules={[
            { required: true, message: '请输入预计完成天数' },
          ]}
        >
          <InputNumber
            min={1}
            max={365}
            className="w-full"
            placeholder="请输入预计完成天数"
            addonAfter="天"
          />
        </Form.Item>

        <Form.Item
          name="proposal"
          label="投标方案"
          rules={[
            { required: true, message: '请输入投标方案' },
            { min: 50, message: '投标方案至少50个字符' },
          ]}
        >
          <TextArea
            rows={6}
            showCount
            maxLength={5000}
            placeholder={`请详细描述您的方案，包括：

1. 您的技术方案和实现思路
2. 您的相关经验和案例
3. 项目分解和时间安排
4. 交付物和验收标准
5. 其他补充说明`}
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default BidModal
