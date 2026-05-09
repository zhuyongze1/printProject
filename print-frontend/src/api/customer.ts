import request from './index'

export function getCustomerList(params?: any) {
  return request.get('/customers', { params })
}

export function getAllCustomers() {
  return request.get('/customers/all')
}

export function createCustomer(data: any) {
  return request.post('/customers', data)
}

export function updateCustomer(id: number, data: any) {
  return request.put('/customers/' + id, data)
}

export function deleteCustomer(id: number) {
  return request.delete('/customers/' + id)
}
