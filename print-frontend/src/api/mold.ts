import request from './index'

export function getMoldList(params?: any) {
  return request.get('/molds', { params })
}

export function getAllMolds() {
  return request.get('/molds/all')
}

export function createMold(data: any) {
  return request.post('/molds', data)
}

export function updateMold(id: number, data: any) {
  return request.put('/molds/' + id, data)
}

export function deleteMold(id: number) {
  return request.delete('/molds/' + id)
}

export function printMoldLabel(data: any) {
  return request.post('/molds/print', data)
}
