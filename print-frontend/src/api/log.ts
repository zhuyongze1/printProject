import request from './index'

export function getLogList(params?: any) {
  return request.get('/logs', { params })
}
