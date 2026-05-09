import request from './index'

export function getUserList(params?: any) {
  return request.get('/users', { params })
}

export function getUser(id: number) {
  return request.get(`/users/${id}`)
}

export function createUser(data: any) {
  return request.post('/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/users/${id}`)
}

export function assignRoles(id: number, roleIds: number[]) {
  return request.put(`/users/${id}/roles`, { roleIds })
}
