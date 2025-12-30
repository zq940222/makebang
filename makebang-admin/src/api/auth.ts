import request from '@/utils/request'
import type { User } from '@/types'

export interface LoginRequest {
  account: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

/**
 * 管理员登录
 */
export function login(data: LoginRequest) {
  return request.post<LoginResponse>('/api/v1/auth/login', data)
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get<User>('/api/v1/users/me')
}

/**
 * 刷新令牌
 */
export function refreshToken(refreshToken: string) {
  return request.post<LoginResponse>('/api/v1/auth/refresh', { refreshToken })
}
