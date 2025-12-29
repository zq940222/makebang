import { http } from '@/utils/request'
import type { User, PageResult } from '@/types'

// 里程碑VO
export interface MilestoneVO {
  id: number
  orderId: number
  title: string
  description?: string
  amount: number
  sequence: number
  status: number
  statusDesc?: string
  dueDate: string
  completedAt?: string
  submitNote?: string
  reviewNote?: string
  createdAt: string
  updatedAt: string
}

// 订单VO
export interface OrderVO {
  id: number
  orderNo: string
  projectId: number
  projectTitle?: string
  bidId: number
  employerId: number
  developerId: number
  amount: number
  status: number
  statusDesc?: string
  milestoneCount: number
  startedAt?: string
  deadline: string
  completedAt?: string
  remark?: string
  createdAt: string
  updatedAt: string
  employer?: User
  developer?: User
  milestones?: MilestoneVO[]
  progress?: number
}

// 创建里程碑请求
export interface CreateMilestoneRequest {
  title: string
  description?: string
  amount: number
  dueDate: string
}

// 提交里程碑请求
export interface SubmitMilestoneRequest {
  submitNote: string
}

// 验收里程碑请求
export interface ReviewMilestoneRequest {
  approved: boolean
  reviewNote?: string
}

/**
 * 获取订单详情
 */
export const getOrder = (id: number) => {
  return http.get<OrderVO>(`/v1/orders/${id}`)
}

/**
 * 根据订单编号获取订单
 */
export const getOrderByNo = (orderNo: string) => {
  return http.get<OrderVO>(`/v1/orders/no/${orderNo}`)
}

/**
 * 获取我的订单列表
 */
export const getMyOrders = (params: {
  role?: 'employer' | 'developer'
  status?: number
  current?: number
  size?: number
}) => {
  return http.get<PageResult<OrderVO>>('/v1/orders/my', { params })
}

/**
 * 取消订单
 */
export const cancelOrder = (id: number) => {
  return http.post<void>(`/v1/orders/${id}/cancel`)
}

/**
 * 确认付款
 */
export const confirmPayment = (id: number) => {
  return http.post<OrderVO>(`/v1/orders/${id}/pay`)
}

/**
 * 完成订单
 */
export const completeOrder = (id: number) => {
  return http.post<OrderVO>(`/v1/orders/${id}/complete`)
}

// ========== 里程碑相关 ==========

/**
 * 获取订单的里程碑列表
 */
export const getOrderMilestones = (orderId: number) => {
  return http.get<MilestoneVO[]>(`/v1/orders/${orderId}/milestones`)
}

/**
 * 添加里程碑
 */
export const addMilestone = (orderId: number, params: CreateMilestoneRequest) => {
  return http.post<MilestoneVO>(`/v1/orders/${orderId}/milestones`, params)
}

/**
 * 开始里程碑
 */
export const startMilestone = (milestoneId: number) => {
  return http.post<MilestoneVO>(`/v1/orders/milestones/${milestoneId}/start`)
}

/**
 * 提交里程碑
 */
export const submitMilestone = (milestoneId: number, params: SubmitMilestoneRequest) => {
  return http.post<MilestoneVO>(`/v1/orders/milestones/${milestoneId}/submit`, params)
}

/**
 * 验收里程碑
 */
export const reviewMilestone = (milestoneId: number, params: ReviewMilestoneRequest) => {
  return http.post<MilestoneVO>(`/v1/orders/milestones/${milestoneId}/review`, params)
}
