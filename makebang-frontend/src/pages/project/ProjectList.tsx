import { useState, useEffect, useCallback } from 'react'
import { Card, Select, Row, Col, Tag, Pagination, Empty, Spin, Space, Tooltip } from 'antd'
import { ThunderboltOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getCategories, type ProjectVO, type CategoryVO } from '@/api/project'
import { search, type SearchParams, type SearchResultVO } from '@/api/search'
import SearchBox from '@/components/search/SearchBox'

const ProjectList = () => {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const [loading, setLoading] = useState(false)
  const [projects, setProjects] = useState<ProjectVO[]>([])
  const [categories, setCategories] = useState<CategoryVO[]>([])
  const [total, setTotal] = useState(0)
  const [searchType, setSearchType] = useState<string>('keyword')
  const [costTime, setCostTime] = useState<number>(0)

  // 查询参数
  const [queryParams, setQueryParams] = useState<SearchParams>({
    keyword: searchParams.get('keyword') || '',
    categoryId: searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined,
    minBudget: searchParams.get('minBudget') ? Number(searchParams.get('minBudget')) : undefined,
    maxBudget: searchParams.get('maxBudget') ? Number(searchParams.get('maxBudget')) : undefined,
    current: Number(searchParams.get('page')) || 1,
    size: 10,
  })

  // 预算范围选项
  const budgetRanges = [
    { value: '', label: '不限预算' },
    { value: '0-5000', label: '5000以下' },
    { value: '5000-10000', label: '5000-10000' },
    { value: '10000-30000', label: '10000-30000' },
    { value: '30000+', label: '30000以上' },
  ]

  // 加载分类
  useEffect(() => {
    const loadCategories = async () => {
      try {
        const res = await getCategories()
        setCategories(res.data || [])
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }
    loadCategories()
  }, [])

  // 加载项目列表
  const loadProjects = useCallback(async () => {
    setLoading(true)
    try {
      const res = await search(queryParams)
      setProjects(res.data?.projects || [])
      setTotal(res.data?.total || 0)
      setSearchType(res.data?.searchType || 'keyword')
      setCostTime(res.data?.costTime || 0)
    } catch (error) {
      console.error('加载项目失败:', error)
      setProjects([])
    } finally {
      setLoading(false)
    }
  }, [queryParams])

  useEffect(() => {
    loadProjects()
  }, [loadProjects])

  // 更新URL参数
  useEffect(() => {
    const params = new URLSearchParams()
    if (queryParams.keyword) params.set('keyword', queryParams.keyword)
    if (queryParams.categoryId) params.set('categoryId', String(queryParams.categoryId))
    if (queryParams.current && queryParams.current > 1) params.set('page', String(queryParams.current))
    setSearchParams(params, { replace: true })
  }, [queryParams, setSearchParams])

  // 搜索处理
  const handleSearch = (value: string) => {
    setQueryParams(prev => ({ ...prev, keyword: value, current: 1 }))
  }

  // 分类筛选
  const handleCategoryChange = (value: number | undefined) => {
    setQueryParams(prev => ({ ...prev, categoryId: value || undefined, current: 1 }))
  }

  // 预算筛选
  const handleBudgetChange = (value: string) => {
    let minBudget: number | undefined
    let maxBudget: number | undefined

    if (value === '0-5000') {
      minBudget = 0
      maxBudget = 5000
    } else if (value === '5000-10000') {
      minBudget = 5000
      maxBudget = 10000
    } else if (value === '10000-30000') {
      minBudget = 10000
      maxBudget = 30000
    } else if (value === '30000+') {
      minBudget = 30000
    }

    setQueryParams(prev => ({ ...prev, minBudget, maxBudget, current: 1 }))
  }

  // 分页处理
  const handlePageChange = (page: number, pageSize: number) => {
    setQueryParams(prev => ({ ...prev, current: page, size: pageSize }))
  }

  // 构建分类选项（扁平化树形结构）
  const categoryOptions = [
    { value: undefined, label: '全部分类' },
    ...categories.flatMap(cat => [
      { value: cat.id, label: cat.name },
      ...(cat.children || []).map(child => ({
        value: child.id,
        label: `  ${child.name}`,
      })),
    ]),
  ]

  return (
    <div className="space-y-6">
      {/* 搜索和筛选 */}
      <Card>
        <Row gutter={16} align="middle">
          <Col xs={24} md={12}>
            <SearchBox
              defaultValue={queryParams.keyword}
              onSearch={handleSearch}
              placeholder="搜索项目、技能、关键词..."
              style={{ width: '100%' }}
            />
          </Col>
          <Col xs={12} md={6}>
            <Select
              className="w-full"
              size="large"
              placeholder="选择分类"
              options={categoryOptions}
              value={queryParams.categoryId}
              onChange={handleCategoryChange}
              allowClear
            />
          </Col>
          <Col xs={12} md={6}>
            <Select
              className="w-full"
              size="large"
              placeholder="预算范围"
              options={budgetRanges}
              onChange={handleBudgetChange}
              allowClear
            />
          </Col>
        </Row>
        {/* 搜索信息 */}
        {queryParams.keyword && (
          <div className="mt-4 text-gray-500 text-sm flex items-center">
            搜索 "<span className="text-blue-500">{queryParams.keyword}</span>"
            {total > 0 && <span className="ml-2">共 {total} 个结果</span>}
            {searchType === 'hybrid' && (
              <Tooltip title="使用了AI语义搜索，结果更精准">
                <Tag icon={<ThunderboltOutlined />} color="purple" className="ml-2">
                  智能搜索
                </Tag>
              </Tooltip>
            )}
            {costTime > 0 && (
              <span className="text-gray-400 ml-2">({costTime}ms)</span>
            )}
          </div>
        )}
      </Card>

      {/* 项目列表 */}
      <Spin spinning={loading}>
        {projects.length > 0 ? (
          <div className="space-y-4">
            {projects.map((project) => (
              <Card
                key={project.id}
                hoverable
                onClick={() => navigate(`/projects/${project.id}`)}
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h3 className="text-lg font-medium text-gray-800 mb-2">
                      {project.title}
                    </h3>
                    <p className="text-gray-500 line-clamp-2 mb-3">
                      {project.description}
                    </p>
                    <Space size={[0, 8]} wrap>
                      {project.skillRequirements?.map((skill) => (
                        <Tag key={skill} color="blue">{skill}</Tag>
                      ))}
                    </Space>
                    <div className="text-gray-400 text-sm mt-3">
                      <span className="mr-4">发布于 {project.createdAt?.split('T')[0]}</span>
                      <span className="mr-4">{project.viewCount || 0} 浏览</span>
                      <span>{project.bidCount || 0} 投标</span>
                      {project.categoryName && (
                        <span className="ml-4">
                          <Tag color="green">{project.categoryName}</Tag>
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="text-right ml-6">
                    <div className="text-2xl font-bold text-red-500">
                      ¥{(project.budgetMin || 0).toLocaleString()} - {(project.budgetMax || 0).toLocaleString()}
                    </div>
                    <div className="text-gray-400 text-sm mt-1">预算</div>
                  </div>
                </div>
              </Card>
            ))}

            {/* 分页 */}
            <div className="flex justify-center mt-6">
              <Pagination
                current={queryParams.current}
                total={total}
                pageSize={queryParams.size}
                showSizeChanger
                showQuickJumper
                showTotal={(t) => `共 ${t} 个项目`}
                onChange={handlePageChange}
              />
            </div>
          </div>
        ) : (
          <Card>
            <Empty description={loading ? '加载中...' : '暂无项目'} />
          </Card>
        )}
      </Spin>
    </div>
  )
}

export default ProjectList
