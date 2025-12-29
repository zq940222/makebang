import { http } from '@/utils/request'
import type { PageResult } from '@/types'

// 钱包VO
export interface WalletVO {
  id: number
  userId: number
  balance: number
  frozenAmount: number
  totalIncome: number
  totalExpense: number
  status: number
  statusDesc?: string
  createdAt: string
  updatedAt: string
}

// 交易记录VO
export interface TransactionVO {
  id: number
  transactionNo: string
  userId: number
  type: number
  typeDesc?: string
  amount: number
  balanceBefore: number
  balanceAfter: number
  orderId?: number
  orderNo?: string
  milestoneId?: number
  milestoneTitle?: string
  status: number
  statusDesc?: string
  remark?: string
  createdAt: string
  direction: number
  directionDesc?: string
}

// 充值请求
export interface RechargeRequest {
  amount: number
  paymentMethod?: number
}

// 提现请求
export interface WithdrawRequest {
  amount: number
  withdrawMethod: number
  account: string
  accountName: string
  payPassword?: string
}

/**
 * 获取我的钱包
 */
export const getMyWallet = () => {
  return http.get<WalletVO>('/v1/wallet')
}

/**
 * 充值
 */
export const recharge = (params: RechargeRequest) => {
  return http.post<TransactionVO>('/v1/wallet/recharge', params)
}

/**
 * 提现
 */
export const withdraw = (params: WithdrawRequest) => {
  return http.post<TransactionVO>('/v1/wallet/withdraw', params)
}

/**
 * 获取交易记录
 */
export const getTransactions = (params: {
  type?: number
  current?: number
  size?: number
}) => {
  return http.get<PageResult<TransactionVO>>('/v1/wallet/transactions', { params })
}

/**
 * 获取订单相关交易记录
 */
export const getOrderTransactions = (orderId: number) => {
  return http.get<TransactionVO[]>(`/v1/wallet/transactions/order/${orderId}`)
}
