import { useState, useEffect, useCallback, useRef } from 'react'
import { Input, AutoComplete, Tag, Space, Divider, message } from 'antd'
import { SearchOutlined, ClockCircleOutlined, FireOutlined, CloseOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getSuggestions, clearUserSearchHistory, type SearchSuggestionVO } from '@/api/search'
import { useSelector } from 'react-redux'
import type { RootState } from '@/store'
import debounce from 'lodash/debounce'

interface SearchBoxProps {
  defaultValue?: string
  placeholder?: string
  style?: React.CSSProperties
  onSearch?: (keyword: string) => void
}

const SearchBox = ({
  defaultValue = '',
  placeholder = '搜索项目、技能、关键词...',
  style,
  onSearch,
}: SearchBoxProps) => {
  const navigate = useNavigate()
  const { isLoggedIn } = useSelector((state: RootState) => state.user)
  const [value, setValue] = useState(defaultValue)
  const [suggestions, setSuggestions] = useState<SearchSuggestionVO | null>(null)
  const [options, setOptions] = useState<any[]>([])
  const [open, setOpen] = useState(false)
  const inputRef = useRef<any>(null)

  // 加载搜索建议
  const loadSuggestions = useCallback(async (prefix?: string) => {
    try {
      const res = await getSuggestions(prefix, 10)
      setSuggestions(res.data)
    } catch (error) {
      console.error('加载搜索建议失败:', error)
    }
  }, [])

  // 防抖加载建议
  const debouncedLoadSuggestions = useCallback(
    debounce((prefix: string) => loadSuggestions(prefix), 300),
    [loadSuggestions]
  )

  // 初始加载
  useEffect(() => {
    loadSuggestions()
  }, [loadSuggestions])

  // 构建选项
  useEffect(() => {
    if (!suggestions) return

    const newOptions: any[] = []

    // 联想词
    if (suggestions.completions?.length > 0) {
      newOptions.push({
        label: (
          <div className="text-xs text-gray-400 px-2 py-1">
            <SearchOutlined className="mr-1" />
            搜索建议
          </div>
        ),
        options: suggestions.completions.map((item) => ({
          value: item,
          label: (
            <div className="flex items-center">
              <SearchOutlined className="mr-2 text-gray-400" />
              <span>{item}</span>
            </div>
          ),
        })),
      })
    }

    // 搜索历史
    if (isLoggedIn && suggestions.historyKeywords?.length > 0) {
      newOptions.push({
        label: (
          <div className="flex items-center justify-between px-2 py-1">
            <span className="text-xs text-gray-400">
              <ClockCircleOutlined className="mr-1" />
              搜索历史
            </span>
            <span
              className="text-xs text-blue-500 cursor-pointer hover:underline"
              onClick={(e) => {
                e.stopPropagation()
                handleClearHistory()
              }}
            >
              清除
            </span>
          </div>
        ),
        options: suggestions.historyKeywords.map((item) => ({
          value: item,
          label: (
            <div className="flex items-center">
              <ClockCircleOutlined className="mr-2 text-gray-400" />
              <span>{item}</span>
            </div>
          ),
        })),
      })
    }

    // 热门搜索
    if (suggestions.hotKeywords?.length > 0) {
      newOptions.push({
        label: (
          <div className="text-xs text-gray-400 px-2 py-1">
            <FireOutlined className="mr-1" />
            热门搜索
          </div>
        ),
        options: suggestions.hotKeywords.map((item) => ({
          value: item,
          label: (
            <div className="flex items-center">
              <FireOutlined className="mr-2 text-orange-400" />
              <span>{item}</span>
            </div>
          ),
        })),
      })
    }

    // 相关技能
    if (suggestions.relatedSkills?.length > 0 && !value) {
      newOptions.push({
        label: (
          <div className="px-2 py-2">
            <div className="text-xs text-gray-400 mb-2">热门技能</div>
            <Space wrap size={[8, 8]}>
              {suggestions.relatedSkills.map((skill) => (
                <Tag
                  key={skill}
                  className="cursor-pointer"
                  onClick={() => handleSelect(skill)}
                >
                  {skill}
                </Tag>
              ))}
            </Space>
          </div>
        ),
        options: [],
      })
    }

    setOptions(newOptions)
  }, [suggestions, isLoggedIn, value])

  // 处理搜索
  const handleSearch = (searchValue: string) => {
    const trimmedValue = searchValue.trim()
    if (!trimmedValue) return

    setOpen(false)
    if (onSearch) {
      onSearch(trimmedValue)
    } else {
      navigate(`/projects?keyword=${encodeURIComponent(trimmedValue)}`)
    }
  }

  // 处理选择
  const handleSelect = (selectedValue: string) => {
    setValue(selectedValue)
    handleSearch(selectedValue)
  }

  // 处理输入变化
  const handleChange = (inputValue: string) => {
    setValue(inputValue)
    if (inputValue) {
      debouncedLoadSuggestions(inputValue)
    } else {
      loadSuggestions()
    }
  }

  // 清除搜索历史
  const handleClearHistory = async () => {
    try {
      await clearUserSearchHistory()
      message.success('搜索历史已清除')
      loadSuggestions()
    } catch (error) {
      message.error('清除失败')
    }
  }

  return (
    <AutoComplete
      ref={inputRef}
      value={value}
      options={options}
      open={open}
      onFocus={() => setOpen(true)}
      onBlur={() => setTimeout(() => setOpen(false), 200)}
      onChange={handleChange}
      onSelect={handleSelect}
      style={{ width: 400, ...style }}
      dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
      popupMatchSelectWidth={true}
    >
      <Input.Search
        placeholder={placeholder}
        allowClear
        enterButton={<SearchOutlined />}
        size="large"
        onSearch={handleSearch}
      />
    </AutoComplete>
  )
}

export default SearchBox
