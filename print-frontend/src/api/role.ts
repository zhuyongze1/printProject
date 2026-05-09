import request from './index'

export function getRoleList(params?: any) {
  return request.get('/roles', { params })
}

export function createRole(data: any) {
  return request.post('/roles', data)
}

export function updateRole(id: number, data: any) {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete(`/roles/${id}`)
}

export function getRoleMenus(id: number) {
  return request.get(`/roles/${id}/menus`)
}

export function assignRoleMenus(id: number, menuIds: number[]) {
  return request.put(`/roles/${id}/menus`, { menuIds })
}
