import request from './index'

export interface LoginRequest {
  username: string
  password: string
}

export function login(data: LoginRequest) {
  return request.post('/auth/login', data)
}

export function register(data: LoginRequest & { realName?: string }) {
  return request.post('/auth/register', data)
}

export function getUserInfo() {
  return request.get('/auth/user-info')
}
