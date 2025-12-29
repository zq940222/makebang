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
  DRAFT = 0, // 草稿
  OPEN = 1, // 开放中
  IN_PROGRESS = 2, // 进行中
  COMPLETED = 3, // 已完成
  CANCELLED = 4, // 已取消
  CLOSED = 5, // 已关闭
}

// 分类
export interface Category {
  id: number
  name: string
  parentId: number
  icon?: string
  sort: number
}

// 投标
export interface Bid {
  id: number
  projectId: number
  developerId: number
  proposedPrice: number
  proposedDays: number
  proposal: string
  status: BidStatus
  createdAt: string
  // 关联信息
  developer?: User
  developerProfile?: DeveloperProfile
}

export enum BidStatus {
  PENDING = 0, // 待处理
  ACCEPTED = 1, // 已接受
  REJECTED = 2, // 已拒绝
  WITHDRAWN = 3, // 已撤回
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
  milestones?: Milestone[]
}

export enum OrderStatus {
  PENDING_PAYMENT = 0, // 待付款
  IN_PROGRESS = 1, // 进行中
  DELIVERED = 2, // 已交付
  COMPLETED = 3, // 已完成
  CANCELLED = 4, // 已取消
  DISPUTED = 5, // 争议中
}

// 里程碑
export interface Milestone {
  id: number
  orderId: number
  title: string
  description: string
  amount: number
  sequence: number
  status: MilestoneStatus
  dueDate: string
  completedAt?: string
}

export enum MilestoneStatus {
  PENDING = 0, // 待开始
  IN_PROGRESS = 1, // 进行中
  SUBMITTED = 2, // 已提交
  APPROVED = 3, // 已验收
  REJECTED = 4, // 已驳回
}

// 消息
export interface Message {
  id: number
  senderId: number
  receiverId: number
  content: string
  msgType: MessageType
  isRead: boolean
  createdAt: string
  sender?: User
}

export enum MessageType {
  SYSTEM = 'SYSTEM', // 系统消息
  CHAT = 'CHAT', // 聊天消息
  NOTIFICATION = 'NOTIFICATION', // 通知
}

// 评价
export interface Review {
  id: number
  orderId: number
  reviewerId: number
  revieweeId: number
  rating: number
  content: string
  createdAt: string
  reviewer?: User
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
