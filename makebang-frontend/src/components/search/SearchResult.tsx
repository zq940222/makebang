import { useState, useEffect, useCallback } from 'react'
import { Card, List, Tag, Empty, Spin, Pagination, Space, Select, Tooltip } from 'antd'
import { ClockCircleOutlined, EyeOutlined, TeamOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { search, type SearchResultVO, type SearchParams } from '@/api/search'
import type { ProjectVO } from '@/api/project'

const { Option } = Select

interface SearchResultProps {
  keyword?: string
  categoryId?: number
}

const SearchResult = ({ keyword, categoryId }: SearchResultProps) => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<SearchResultVO | null>(null)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  // 从URL获取搜索参数
  const urlKeyword = searchParams.get('keyword') || keyword
  const urlCategoryId = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : categoryId

  const loadResults = useCallback(async () => {
    setLoading(true)
    try {
      const params: SearchParams = {
        keyword: urlKeyword,
        categoryId: urlCategoryId,
        current,
        size: pageSize,
      }
      const res = await search(params)
      setResult(res.data)
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      setLoading(false)
    }
  }, [urlKeyword, urlCategoryId, current, pageSize])

  useEffect(() => {
    if (urlKeyword || urlCategoryId) {
      loadResults()
    }
  }, [loadResults, urlKeyword, urlCategoryId])

  const handlePageChange = (page: number, size: number) => {
    setCurrent(page)
    setPageSize(size)
  }

  const statusMap: Record<number, { text: string; color: string }> = {
    1: { text: '招标中', color: 'blue' },
    2: { text: '进行中', color: 'processing' },
    3: { text: '已完成', color: 'default' },
  }

  const renderItem = (item: ProjectVO) => (
    <List.Item
      key={item.id}
      className="cursor-pointer hover:bg-gray-50 transition-colors"
      onClick={() => navigate(`/projects/${item.id}`)}
    >
      <div className="w-full">
        <div className="flex items-start justify-between mb-2">
          <div className="flex-1">
            <h3 className="text-lg font-medium text-gray-900 hover:text-blue-500 line-clamp-1">
              {item.title}
            </h3>
            <p className="text-gray-500 text-sm mt-1 line-clamp-2">
              {item.description}
            </p>
          </div>
          <div className="ml-4 text-right">
            <div className="text-red-500 font-bold text-lg">
              ¥{item.budgetMin?.toLocaleString()} - ¥{item.budgetMax?.toLocaleString()}
            </div>
            <Tag color={statusMap[item.status]?.color} className="mt-1">
              {statusMap[item.status]?.text}
            </Tag>
          </div>
        </div>

        <div className="flex items-center justify-between text-sm text-gray-400">
          <Space size="middle">
            {item.skillRequirements?.slice(0, 5).map((skill) => (
              <Tag key={skill} className="mr-0">{skill}</Tag>
            ))}
            {item.skillRequirements && item.skillRequirements.length > 5 && (
              <span>+{item.skillRequirements.length - 5}</span>
            )}
          </Space>
          <Space size="middle">
            <span>
              <EyeOutlined className="mr-1" />
              {item.viewCount}
            </span>
            <span>
              <TeamOutlined className="mr-1" />
              {item.bidCount} 投标
            </span>
            <span>
              <ClockCircleOutlined className="mr-1" />
              {item.createdAt?.split('T')[0]}
            </span>
          </Space>
        </div>
      </div>
    </List.Item>
  )

  if (!urlKeyword && !urlCategoryId) {
    return null
  }

  return (
    <Card>
      {/* 搜索信息 */}
      <div className="flex items-center justify-between mb-4">
        <div className="text-gray-500">
          {urlKeyword && (
            <>
              搜索 "<span className="text-blue-500 font-medium">{urlKeyword}</span>"
            </>
          )}
          {result && (
            <span className="ml-2">
              共找到 <span className="text-blue-500 font-medium">{result.total}</span> 个项目
            </span>
          )}
          {result?.searchType === 'hybrid' && (
            <Tooltip title="使用了AI语义搜索，结果更精准">
              <Tag icon={<ThunderboltOutlined />} color="purple" className="ml-2">
                智能搜索
              </Tag>
            </Tooltip>
          )}
          {result?.costTime && (
            <span className="text-xs text-gray-400 ml-2">
              ({result.costTime}ms)
            </span>
          )}
        </div>
      </div>

      {/* 搜索结果列表 */}
      <Spin spinning={loading}>
        {result?.projects?.length ? (
          <>
            <List
              itemLayout="vertical"
              dataSource={result.projects}
              renderItem={renderItem}
            />
            <div className="flex justify-center mt-6">
              <Pagination
                current={current}
                pageSize={pageSize}
                total={result.total}
                showSizeChanger
                showQuickJumper
                showTotal={(total) => `共 ${total} 条`}
                onChange={handlePageChange}
              />
            </div>
          </>
        ) : (
          <Empty
            description={
              <div>
                <p>没有找到相关项目</p>
                {result?.suggestions && result.suggestions.length > 0 && (
                  <div className="mt-4">
                    <p className="text-gray-400">您可以试试：</p>
                    <Space className="mt-2">
                      {result.suggestions.map((s) => (
                        <Tag
                          key={s}
                          className="cursor-pointer"
                          onClick={() => navigate(`/projects?keyword=${encodeURIComponent(s)}`)}
                        >
                          {s}
                        </Tag>
                      ))}
                    </Space>
                  </div>
                )}
              </div>
            }
          />
        )}
      </Spin>
    </Card>
  )
}

export default SearchResult
