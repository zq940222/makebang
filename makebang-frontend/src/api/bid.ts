import { http } from '@/utils/request'
import type { User, PageResult, PageParams } from '@/types'

// 投标VO
export interface BidVO {
  id: number
  projectId: number
  projectTitle?: string
  developerId: number
  proposedPrice: number
  proposedDays: number
  proposal: string
  status: number
  statusDesc?: string
  createdAt: string
  updatedAt: string
  developer?: User
}

// 创建投标请求
export interface CreateBidRequest {
  projectId: number
  proposedPrice: number
  proposedDays: number
  proposal: string
}

// 更新投标请求
export interface UpdateBidRequest {
  proposedPrice?: number
  proposedDays?: number
  proposal?: string
}

/**
 * 创建投标
 */
export const createBid = (params: CreateBidRequest) => {
  return http.post<BidVO>('/v1/bids', params)
}

/**
 * 更新投标
 */
export const updateBid = (id: number, params: UpdateBidRequest) => {
  return http.put<BidVO>(`/v1/bids/${id}`, params)
}

/**
 * 撤回投标
 */
export const withdrawBid = (id: number) => {
  return http.post<void>(`/v1/bids/${id}/withdraw`)
}

/**
 * 接受投标（雇主操作）
 */
export const acceptBid = (id: number) => {
  return http.post<BidVO>(`/v1/bids/${id}/accept`)
}

/**
 * 拒绝投标（雇主操作）
 */
export const rejectBid = (id: number) => {
  return http.post<void>(`/v1/bids/${id}/reject`)
}

/**
 * 获取投标详情
 */
export const getBid = (id: number) => {
  return http.get<BidVO>(`/v1/bids/${id}`)
}

/**
 * 获取项目的投标列表
 */
export const getProjectBids = (projectId: number) => {
  return http.get<BidVO[]>(`/v1/bids/project/${projectId}`)
}

/**
 * 获取我的投标列表
 */
export const getMyBids = (params: { status?: number; current?: number; size?: number }) => {
  return http.get<PageResult<BidVO>>('/v1/bids/my', { params })
}

/**
 * 检查是否已投标
 */
export const hasBid = (projectId: number) => {
  return http.get<boolean>(`/v1/bids/check/${projectId}`)
}
