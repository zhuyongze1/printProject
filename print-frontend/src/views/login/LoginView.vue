<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">印刷管理系统</h1>
      <p class="login-subtitle">Print Industry Management System</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="register-link">
        还没有账号？<el-link type="primary" @click="showRegister = true">立即注册</el-link>
      </p>
    </div>

    <el-dialog v-model="showRegister" title="注册" width="400px" :close-on-click-modal="false">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registerForm.realName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register as registerApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const showRegister = ref(false)
const registerFormRef = ref<FormInstance>()
const registerLoading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
})

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await registerApi(registerForm)
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f5f7 0%, #e8e8ed 100%);
}

.login-card {
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.06);
  padding: 48px 40px;
  width: 400px;
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 4px;
}

.login-subtitle {
  text-align: center;
  color: #86868b;
  font-size: 13px;
  margin: 0 0 32px;
}

.register-link {
  text-align: center;
  color: #86868b;
  font-size: 13px;
  margin: 16px 0 0;
}
</style>
