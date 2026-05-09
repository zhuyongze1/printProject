import request from './index'

export function getDashboardStats() {
  return request.get('/dashboard/statistics')
}

export function getOrderTrend(params?: any) {
  return request.get('/dashboard/order-trend', { params })
}

export function getCustomerRanking(params?: any) {
  return request.get('/dashboard/customer-ranking', { params })
}
