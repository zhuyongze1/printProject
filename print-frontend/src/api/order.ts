import request from './index'

export function getOrderList(params?: any) {
  return request.get('/orders', { params })
}

export function getOrder(id: number) {
  return request.get('/orders/' + id)
}

export function createOrder(data: any) {
  return request.post('/orders', data)
}

export function updateOrder(id: number, data: any) {
  return request.put('/orders/' + id, data)
}

export function deleteOrder(id: number) {
  return request.delete('/orders/' + id)
}

export function importOrders(data: FormData) {
  return request.post('/orders/import', data)
}

export function exportOrders(params?: any) {
  return request.post('/orders/export', params, { responseType: 'blob' })
}

export function printDeliveryNote(data: any) {
  return request.post('/orders/print', data)
}
