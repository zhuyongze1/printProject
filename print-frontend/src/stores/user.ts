import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'
import type { LoginRequest } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)
  const permissions = ref<string[]>([])

  async function login(loginReq: LoginRequest) {
    const res = await loginApi(loginReq)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    return res
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data.user
    permissions.value = res.data.permissions || []
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
  }

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  return { token, userInfo, permissions, login, fetchUserInfo, logout, hasPermission }
})
