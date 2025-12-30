// 用户类型
export interface User {
  id: number
  username: string
  phone?: string
  email?: string
  avatar?: string
  userType: UserType
  realName?: string
  status: number
  role?: number // 0-普通用户 1-管理员 2-超级管理员
  createdAt: string
}

// 用户角色
export enum UserRole {
  USER = 0,
  ADMIN = 1,
  SUPER_ADMIN = 2,
}

export enum UserType {
  EMPLOYER = 0, // 需求方
  DEVELOPER = 1, // 程序员
  BOTH = 2, // 两者
}

// 程序员资料
export interface DeveloperProfile {
  id: number
  userId: number
  skills: string[]
  experienceYears: number
  hourlyRate: number
  portfolio: PortfolioItem[]
  bio: string
  githubUrl?: string
  certificationStatus: number
  creditScore: number
}

export interface PortfolioItem {
  title: string
  description: string
  imageUrl: string
  link?: string
}

// 项目/需求
export interface Project {
  id: number
  userId: number
  title: string
  description: string
  categoryId: number
  budgetMin: number
  budgetMax: number
  deadline: string
  skillRequirements: string[]
  attachmentUrls: string[]
  status: ProjectStatus
  viewCount: number
  bidCount: number
  createdAt: string
  updatedAt: string
  // 关联信息
  user?: User
  category?: Category
}

export enum ProjectStatus {
  PENDING = 0, // 待审核
  OPEN = 1, // 招标中
  IN_PROGRESS = 2, // 进行中
  COMPLETED = 3, // 已完成
  CANCELLED = 4, // 已取消
  REJECTED = 5, // 已拒绝
  TAKEDOWN = 6, // 已下架
}

// 分类
export interface Category {
  id: number
  name: string
  parentId: number
  icon?: string
  sort: number
}

// 订单
export interface Order {
  id: number
  projectId: number
  employerId: number
  developerId: number
  amount: number
  status: OrderStatus
  milestoneCount: number
  startedAt?: string
  deadline: string
  completedAt?: string
  // 关联信息
  project?: Project
  employer?: User
  developer?: User
}

export enum OrderStatus {
  PENDING_PAYMENT = 0, // 待付款
  IN_PROGRESS = 1, // 进行中
  DELIVERED = 2, // 已交付
  COMPLETED = 3, // 已完成
  CANCELLED = 4, // 已取消
  DISPUTED = 5, // 争议中
}

// 交易记录
export interface Transaction {
  id: number
  userId: number
  type: number
  amount: number
  status: number
  remark?: string
  createdAt: string
}

// API响应类型
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  current: number
  size: number
  total: number
  pages: number
  records: T[]
}

// 分页请求参数
export interface PageParams {
  current?: number
  size?: number
}
