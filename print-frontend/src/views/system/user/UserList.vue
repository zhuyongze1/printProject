<template>
  <div class="page-container">
    <!-- Search -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索用户名/姓名/电话"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- Action -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="apple-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button type="warning" link @click="handleAssignRole(row)">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="left"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- Assign Role Dialog -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="当前用户">
          <span>{{ currentUserName }}</span>
        </el-form-item>
        <el-form-item label="选择角色">
          <el-select
            v-model="selectedRoleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleLoading" @click="handleAssignRoleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, assignRoles } from '@/api/user'
import { getRoleList } from '@/api/role'
import type { FormInstance, FormRules } from 'element-plus'

interface UserForm {
  username: string
  realName: string
  password: string
  phone: string
  email: string
  status: number
}

const defaultForm: UserForm = {
  username: '',
  realName: '',
  password: '',
  phone: '',
  email: '',
  status: 1,
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const roleLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const formRef = ref<FormInstance>()
const roleOptions = ref<any[]>([])
const selectedRoleIds = ref<number[]>([])
const currentUserId = ref<number | null>(null)
const currentUserName = ref('')

const searchForm = reactive({
  keyword: '',
})

const form = reactive<UserForm>({ ...defaultForm })

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchForm.keyword || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    const data = res.data || res
    tableData.value = data.records || data.list || []
    pagination.total = data.total || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, defaultForm)
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.username = row.username || ''
  form.realName = row.realName || ''
  form.password = ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

async function handleAssignRole(row: any) {
  currentUserId.value = row.id
  currentUserName.value = row.realName || row.username
  selectedRoleIds.value = []
  // Load role options
  try {
    const roleRes = await getRoleList({ page: 1, pageSize: 999 })
    const roleData = roleRes.data || roleRes
    roleOptions.value = roleData.records || roleData.list || []
  } catch {
    roleOptions.value = []
  }
  roleDialogVisible.value = true
}

async function handleAssignRoleSubmit() {
  if (!currentUserId.value) return
  roleLoading.value = true
  try {
    await assignRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch {
    // error handled by interceptor
  } finally {
    roleLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = { ...form }
    if (isEdit.value && !submitData.password) {
      delete (submitData as any).password
    }

    if (isEdit.value && editId.value) {
      await updateUser(editId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createUser(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
