import request from '@/utils/request'
import type { User, Order, Transaction, PageResult } from '@/types'

// ========== 类型定义 ==========

// 用户VO（管理后台专用）
export interface UserVO extends User {
  role: number
}

// 项目VO（管理后台专用，包含发布者信息）
export interface AdminProjectVO {
  id: number
  title: string
  description: string
  categoryId?: number
  budgetMin?: number
  budgetMax?: number
  status: number
  createdAt?: string
  employer?: User
}

// 交易记录VO（管理后台专用，包含用户信息）
export interface TransactionVO extends Transaction {
  user?: User
}

export interface DashboardStatsVO {
  totalUsers: number
  todayNewUsers: number
  totalProjects: number
  activeProjects: number
  totalOrders: number
  activeOrders: number
  totalAmount: number
  todayAmount: number
  platformIncome: number
  pendingProjects: number
  pendingWithdrawals: number
  orderTrend: Array<{ date: string; count: number }>
  userTrend: Array<{ date: string; count: number }>
}

// ========== 统计相关 ==========

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats() {
  return request.get<DashboardStatsVO>('/api/v1/admin/dashboard/stats')
}

// ========== 用户管理 ==========

/**
 * 分页查询用户列表
 */
export function getUserList(params: {
  keyword?: string
  userType?: number
  status?: number
  current?: number
  size?: number
}) {
  return request.get<PageResult<UserVO>>('/api/v1/admin/users', { params })
}

/**
 * 禁用用户
 */
export function disableUser(userId: number) {
  return request.post(`/api/v1/admin/users/${userId}/disable`)
}

/**
 * 启用用户
 */
export function enableUser(userId: number) {
  return request.post(`/api/v1/admin/users/${userId}/enable`)
}

/**
 * 设置用户角色
 */
export function setUserRole(userId: number, role: number) {
  return request.post(`/api/v1/admin/users/${userId}/role`, { role })
}

// ========== 项目管理 ==========

/**
 * 分页查询项目列表
 */
export function getProjectList(params: {
  keyword?: string
  status?: number
  current?: number
  size?: number
}) {
  return request.get<PageResult<AdminProjectVO>>('/api/v1/admin/projects', { params })
}

/**
 * 审核项目通过
 */
export function approveProject(projectId: number) {
  return request.post(`/api/v1/admin/projects/${projectId}/approve`)
}

/**
 * 审核项目拒绝
 */
export function rejectProject(projectId: number, reason: string) {
  return request.post(`/api/v1/admin/projects/${projectId}/reject`, { reason })
}

/**
 * 下架项目
 */
export function takedownProject(projectId: number, reason: string) {
  return request.post(`/api/v1/admin/projects/${projectId}/takedown`, { reason })
}

// ========== 订单管理 ==========

/**
 * 分页查询订单列表
 */
export function getOrderList(params: {
  status?: number
  current?: number
  size?: number
}) {
  return request.get<PageResult<Order>>('/api/v1/admin/orders', { params })
}

// ========== 提现管理 ==========

/**
 * 分页查询提现申请列表
 */
export function getWithdrawalList(params: {
  status?: number
  current?: number
  size?: number
}) {
  return request.get<PageResult<TransactionVO>>('/api/v1/admin/withdrawals', { params })
}

/**
 * 审核提现通过
 */
export function approveWithdrawal(transactionId: number) {
  return request.post(`/api/v1/admin/withdrawals/${transactionId}/approve`)
}

/**
 * 审核提现拒绝
 */
export function rejectWithdrawal(transactionId: number, reason: string) {
  return request.post(`/api/v1/admin/withdrawals/${transactionId}/reject`, { reason })
}
