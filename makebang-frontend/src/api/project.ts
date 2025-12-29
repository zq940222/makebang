import { http } from '@/utils/request'
import type { Project, Category, PageResult, PageParams } from '@/types'

// 项目VO（包含额外信息）
export interface ProjectVO extends Project {
  categoryName?: string
  user?: {
    id: number
    username: string
    avatar?: string
  }
}

// 分类VO（树形结构）
export interface CategoryVO extends Category {
  children?: CategoryVO[]
}

// 创建项目请求
export interface CreateProjectRequest {
  title: string
  description: string
  categoryId: number
  budgetMin: number
  budgetMax: number
  deadline: string
  skillRequirements: string[]
  attachmentUrls?: string[]
  draft?: boolean
}

// 更新项目请求
export interface UpdateProjectRequest {
  title?: string
  description?: string
  categoryId?: number
  budgetMin?: number
  budgetMax?: number
  deadline?: string
  skillRequirements?: string[]
  attachmentUrls?: string[]
}

// 项目查询请求
export interface ProjectQueryRequest extends PageParams {
  keyword?: string
  categoryId?: number
  status?: number
  userId?: number
  budgetMinFrom?: number
  budgetMinTo?: number
  sortBy?: 'created_at' | 'budget_max' | 'view_count' | 'bid_count'
  sortOrder?: 'asc' | 'desc'
}

/**
 * 创建项目
 */
export const createProject = (params: CreateProjectRequest) => {
  return http.post<ProjectVO>('/v1/projects', params)
}

/**
 * 更新项目
 */
export const updateProject = (id: number, params: UpdateProjectRequest) => {
  return http.put<ProjectVO>(`/v1/projects/${id}`, params)
}

/**
 * 删除项目
 */
export const deleteProject = (id: number) => {
  return http.delete<void>(`/v1/projects/${id}`)
}

/**
 * 发布项目（草稿->开放）
 */
export const publishProject = (id: number) => {
  return http.post<ProjectVO>(`/v1/projects/${id}/publish`)
}

/**
 * 关闭项目
 */
export const closeProject = (id: number) => {
  return http.post<void>(`/v1/projects/${id}/close`)
}

/**
 * 获取项目详情（需登录）
 */
export const getProject = (id: number) => {
  return http.get<ProjectVO>(`/v1/projects/${id}`)
}

/**
 * 分页查询项目（需登录）
 */
export const queryProjects = (params: ProjectQueryRequest) => {
  return http.get<PageResult<ProjectVO>>('/v1/projects', { params })
}

/**
 * 获取我发布的项目
 */
export const getMyProjects = (params: { status?: number; current?: number; size?: number }) => {
  return http.get<PageResult<ProjectVO>>('/v1/projects/my', { params })
}

// ========== 公开接口（无需登录） ==========

/**
 * 获取项目分类
 */
export const getCategories = () => {
  return http.get<CategoryVO[]>('/v1/projects/public/categories')
}

/**
 * 获取热门技能标签
 */
export const getHotSkills = () => {
  return http.get<string[]>('/v1/projects/public/skills')
}

/**
 * 公开查询项目列表
 */
export const publicQueryProjects = (params: ProjectQueryRequest) => {
  return http.get<PageResult<ProjectVO>>('/v1/projects/public/list', { params })
}

/**
 * 公开获取项目详情
 */
export const publicGetProject = (id: number) => {
  return http.get<ProjectVO>(`/v1/projects/public/${id}`)
}
