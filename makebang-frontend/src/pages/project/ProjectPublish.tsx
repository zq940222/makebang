import { useState, useEffect } from 'react'
import { Form, Input, InputNumber, Select, DatePicker, Button, Card, message, Upload, Checkbox, Spin } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { createProject, getCategories, getHotSkills, type CreateProjectRequest, type CategoryVO } from '@/api/project'
import dayjs from 'dayjs'

const { TextArea } = Input

const ProjectPublish = () => {
  const [loading, setLoading] = useState(false)
  const [categoriesLoading, setCategoriesLoading] = useState(true)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const [categories, setCategories] = useState<CategoryVO[]>([])
  const [hotSkills, setHotSkills] = useState<string[]>([])

  // 加载分类和热门技能
  useEffect(() => {
    const loadData = async () => {
      setCategoriesLoading(true)
      try {
        const [categoriesRes, skillsRes] = await Promise.all([
          getCategories(),
          getHotSkills(),
        ])
        setCategories(categoriesRes.data || [])
        setHotSkills(skillsRes.data || [])
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        setCategoriesLoading(false)
      }
    }
    loadData()
  }, [])

  // 构建分类选项（扁平化树形结构）
  const categoryOptions = categories.flatMap(cat => [
    { value: cat.id, label: cat.name, disabled: (cat.children?.length || 0) > 0 },
    ...(cat.children || []).map(child => ({
      value: child.id,
      label: `  ${child.name}`,
    })),
  ])

  // 技能选项
  const skillOptions = hotSkills.map(skill => ({ value: skill, label: skill }))

  const onFinish = async (values: any) => {
    // 验证预算
    if (values.budgetMax < values.budgetMin) {
      message.error('最高预算不能低于最低预算')
      return
    }

    setLoading(true)
    try {
      const request: CreateProjectRequest = {
        title: values.title,
        description: values.description,
        categoryId: values.categoryId,
        budgetMin: values.budgetMin,
        budgetMax: values.budgetMax,
        deadline: values.deadline.format('YYYY-MM-DD'),
        skillRequirements: values.skillRequirements || [],
        attachmentUrls: values.attachmentUrls || [],
        draft: values.draft || false,
      }

      const res = await createProject(request)

      if (values.draft) {
        message.success('项目已保存为草稿')
      } else {
        message.success('项目发布成功')
      }

      navigate(`/projects/${res.data.id}`)
    } catch (error: any) {
      message.error(error.message || '发布失败,请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  // 保存为草稿
  const handleSaveDraft = () => {
    form.setFieldValue('draft', true)
    form.submit()
  }

  // 直接发布
  const handlePublish = () => {
    form.setFieldValue('draft', false)
    form.submit()
  }

  if (categoriesLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <Card title="发布需求" className="max-w-3xl mx-auto">
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        initialValues={{
          budgetMin: 1000,
          budgetMax: 5000,
          draft: false,
        }}
      >
        <Form.Item name="draft" hidden>
          <Checkbox />
        </Form.Item>

        <Form.Item
          name="title"
          label="项目标题"
          rules={[
            { required: true, message: '请输入项目标题' },
            { max: 100, message: '标题最多100个字符' },
          ]}
        >
          <Input placeholder="请输入项目标题,简洁描述您的需求" maxLength={100} showCount />
        </Form.Item>

        <Form.Item
          name="categoryId"
          label="项目分类"
          rules={[{ required: true, message: '请选择项目分类' }]}
        >
          <Select
            placeholder="请选择项目分类"
            options={categoryOptions}
            showSearch
            optionFilterProp="label"
          />
        </Form.Item>

        <Form.Item
          name="description"
          label="项目描述"
          rules={[
            { required: true, message: '请输入项目描述' },
            { min: 50, message: '描述至少50个字符' },
          ]}
        >
          <TextArea
            rows={8}
            showCount
            maxLength={5000}
            placeholder={`请详细描述您的项目需求,包括:

1. 项目背景和目的
2. 功能需求列表
3. 技术要求
4. 交付物要求
5. 其他补充说明`}
          />
        </Form.Item>

        <Form.Item
          name="skillRequirements"
          label="技能要求"
          rules={[{ required: true, message: '请选择技能要求' }]}
        >
          <Select
            mode="multiple"
            placeholder="请选择所需技能（可多选）"
            options={skillOptions}
            showSearch
            optionFilterProp="label"
            maxTagCount={10}
          />
        </Form.Item>

        <Form.Item label="预算范围 (元)" required>
          <Input.Group compact>
            <Form.Item
              name="budgetMin"
              noStyle
              rules={[{ required: true, message: '请输入最低预算' }]}
            >
              <InputNumber
                min={100}
                max={10000000}
                placeholder="最低预算"
                style={{ width: '45%' }}
                formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={value => value!.replace(/¥\s?|(,*)/g, '')}
              />
            </Form.Item>
            <span className="px-2 leading-8">-</span>
            <Form.Item
              name="budgetMax"
              noStyle
              rules={[{ required: true, message: '请输入最高预算' }]}
            >
              <InputNumber
                min={100}
                max={10000000}
                placeholder="最高预算"
                style={{ width: '45%' }}
                formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={value => value!.replace(/¥\s?|(,*)/g, '')}
              />
            </Form.Item>
          </Input.Group>
        </Form.Item>

        <Form.Item
          name="deadline"
          label="期望完成日期"
          rules={[{ required: true, message: '请选择期望完成日期' }]}
        >
          <DatePicker
            className="w-full"
            placeholder="请选择日期"
            disabledDate={(current) => current && current < dayjs().startOf('day')}
          />
        </Form.Item>

        <Form.Item
          name="attachments"
          label="附件 (可选)"
        >
          <Upload
            action="/api/v1/upload"
            listType="text"
            maxCount={5}
            beforeUpload={(file) => {
              const isLt50M = file.size / 1024 / 1024 < 50
              if (!isLt50M) {
                message.error('文件大小不能超过50MB')
              }
              return isLt50M
            }}
          >
            <Button icon={<UploadOutlined />}>上传附件</Button>
          </Upload>
          <div className="text-gray-400 text-sm mt-1">
            支持 jpg、png、pdf、doc、zip 格式,单个文件不超过50MB,最多5个文件
          </div>
        </Form.Item>

        <Form.Item className="mt-8">
          <div className="flex space-x-4">
            <Button
              size="large"
              onClick={handleSaveDraft}
              loading={loading}
              className="flex-1"
            >
              保存为草稿
            </Button>
            <Button
              type="primary"
              size="large"
              onClick={handlePublish}
              loading={loading}
              className="flex-1"
            >
              立即发布
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Card>
  )
}

export default ProjectPublish
