import { http } from '@/utils/request'
import type { ProjectVO } from './project'

// 搜索结果VO
export interface SearchResultVO {
  projects: ProjectVO[]
  total: number
  current: number
  size: number
  pages: number
  searchType: 'keyword' | 'semantic' | 'hybrid'
  costTime: number
  suggestions?: string[]
}

// 搜索建议VO
export interface SearchSuggestionVO {
  hotKeywords: string[]
  historyKeywords: string[]
  completions: string[]
  relatedSkills: string[]
}

// 搜索参数
export interface SearchParams {
  keyword?: string
  categoryId?: number
  minBudget?: number
  maxBudget?: number
  current?: number
  size?: number
}

/**
 * 综合搜索
 */
export const search = (params: SearchParams) => {
  return http.get<SearchResultVO>('/v1/search', { params })
}

/**
 * 语义搜索
 */
export const semanticSearch = (query: string, limit: number = 10) => {
  return http.get<ProjectVO[]>('/v1/search/semantic', {
    params: { query, limit }
  })
}

/**
 * 获取搜索建议
 */
export const getSuggestions = (prefix?: string, limit: number = 10) => {
  return http.get<SearchSuggestionVO>('/v1/search/suggestions', {
    params: { prefix, limit }
  })
}

/**
 * 获取热门搜索关键词
 */
export const getHotKeywords = (limit: number = 10) => {
  return http.get<string[]>('/v1/search/hot-keywords', {
    params: { limit }
  })
}

/**
 * 获取用户搜索历史
 */
export const getUserSearchHistory = (limit: number = 10) => {
  return http.get<string[]>('/v1/search/history', {
    params: { limit }
  })
}

/**
 * 清除用户搜索历史
 */
export const clearUserSearchHistory = () => {
  return http.delete<void>('/v1/search/history')
}
