import { http } from '@/utils/request'
import type { User, ApiResponse } from '@/types'

// 登录请求参数
export interface LoginParams {
  account: string
  password: string
  rememberMe?: boolean
}

// 注册请求参数
export interface RegisterParams {
  username: string
  phone: string
  email?: string
  password: string
  verifyCode: string
  userType: number
}

// 登录响应
export interface LoginResult {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

/**
 * 用户登录
 */
export const login = (params: LoginParams) => {
  return http.post<LoginResult>('/v1/auth/login', params)
}

/**
 * 用户注册
 */
export const register = (params: RegisterParams) => {
  return http.post<User>('/v1/auth/register', params)
}

/**
 * 刷新令牌
 */
export const refreshToken = (refreshToken: string) => {
  return http.post<LoginResult>('/v1/auth/refresh', null, {
    params: { refreshToken }
  })
}

/**
 * 发送验证码
 */
export const sendVerifyCode = (phone: string) => {
  return http.post<void>('/v1/auth/verify-code', null, {
    params: { phone }
  })
}

/**
 * 检查用户名是否可用
 */
export const checkUsername = (username: string) => {
  return http.get<boolean>('/v1/auth/check-username', {
    params: { username }
  })
}

/**
 * 检查手机号是否可用
 */
export const checkPhone = (phone: string) => {
  return http.get<boolean>('/v1/auth/check-phone', {
    params: { phone }
  })
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => {
  return http.get<User>('/v1/users/me')
}

/**
 * 更新用户信息
 */
export const updateUser = (params: { avatar?: string; email?: string; realName?: string }) => {
  return http.put<User>('/v1/users/me', params)
}

/**
 * 修改密码
 */
export const changePassword = (params: { oldPassword: string; newPassword: string; confirmPassword: string }) => {
  return http.put<void>('/v1/users/me/password', params)
}
