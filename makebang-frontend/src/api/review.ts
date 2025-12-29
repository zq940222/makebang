import { http } from '@/utils/request'
import type { User, PageResult } from '@/types'

// 评价VO
export interface ReviewVO {
  id: number
  orderId: number
  orderNo?: string
  projectId: number
  projectTitle?: string
  reviewerId: number
  reviewer?: User
  revieweeId: number
  reviewee?: User
  type: number
  typeDesc?: string
  rating: number
  skillRating?: number
  communicationRating?: number
  attitudeRating?: number
  timelinessRating?: number
  content: string
  tags?: string[]
  isAnonymous: boolean
  reply?: string
  createdAt: string
}

// 用户统计VO
export interface UserStatsVO {
  userId: number
  avgRating: number
  avgSkillRating: number
  avgCommunicationRating: number
  avgAttitudeRating: number
  avgTimelinessRating: number
  reviewCount: number
  completedOrderCount: number
  positiveCount: number
  positiveRate: number
}

// 创建评价请求
export interface CreateReviewRequest {
  orderId: number
  rating: number
  skillRating?: number
  communicationRating?: number
  attitudeRating?: number
  timelinessRating?: number
  content: string
  tags?: string[]
  isAnonymous?: boolean
}

// 回复评价请求
export interface ReplyReviewRequest {
  reply: string
}

/**
 * 创建评价
 */
export const createReview = (params: CreateReviewRequest) => {
  return http.post<ReviewVO>('/v1/reviews', params)
}

/**
 * 获取评价详情
 */
export const getReview = (id: number) => {
  return http.get<ReviewVO>(`/v1/reviews/${id}`)
}

/**
 * 获取订单评价
 */
export const getOrderReviews = (orderId: number) => {
  return http.get<ReviewVO[]>(`/v1/reviews/order/${orderId}`)
}

/**
 * 检查是否可以评价订单
 */
export const canReviewOrder = (orderId: number) => {
  return http.get<boolean>(`/v1/reviews/order/${orderId}/can-review`)
}

/**
 * 回复评价
 */
export const replyReview = (id: number, params: ReplyReviewRequest) => {
  return http.post<ReviewVO>(`/v1/reviews/${id}/reply`, params)
}

/**
 * 获取用户收到的评价
 */
export const getReceivedReviews = (userId: number, params: {
  current?: number
  size?: number
}) => {
  return http.get<PageResult<ReviewVO>>(`/v1/reviews/user/${userId}/received`, { params })
}

/**
 * 获取我发出的评价
 */
export const getGivenReviews = (params: {
  current?: number
  size?: number
}) => {
  return http.get<PageResult<ReviewVO>>('/v1/reviews/my/given', { params })
}

/**
 * 获取用户评分统计
 */
export const getUserStats = (userId: number) => {
  return http.get<UserStatsVO>(`/v1/reviews/user/${userId}/stats`)
}

/**
 * 获取项目评价
 */
export const getProjectReviews = (projectId: number) => {
  return http.get<ReviewVO[]>(`/v1/reviews/project/${projectId}`)
}
