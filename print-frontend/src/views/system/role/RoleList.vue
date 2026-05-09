<template>
  <div class="page-container">
    <!-- Action -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增角色
      </el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="apple-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button type="warning" link @click="handleAssignPermission(row)">分配权限</el-button>
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
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="left"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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

    <!-- Permission Tree Dialog -->
    <el-dialog
      v-model="permDialogVisible"
      title="分配权限"
      width="450px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 12px; color: var(--app-text-secondary); font-size: 13px;">
        当前角色：{{ currentRoleName }}
      </div>
      <el-tree
        ref="treeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
        highlight-current
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="handleAssignPermissionSubmit">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/role'
import { getMenuList } from '@/api/menu'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'

interface RoleForm {
  roleName: string
  roleCode: string
  description: string
  status: number
}

const defaultForm: RoleForm = {
  roleName: '',
  roleCode: '',
  description: '',
  status: 1,
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const permLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const formRef = ref<FormInstance>()
const treeRef = ref<InstanceType<typeof ElTree>>()

const menuTree = ref<any[]>([])
const currentRoleId = ref<number | null>(null)
const currentRoleName = ref('')

const form = reactive<RoleForm>({ ...defaultForm })

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

function buildMenuTree(menus: any[]): any[] {
  const map = new Map<number, any>()
  const roots: any[] = []

  // Sort by sort order
  const sorted = [...menus].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))

  for (const m of sorted) {
    map.set(m.id, { ...m, children: [] })
  }

  for (const m of sorted) {
    const node = map.get(m.id)
    if (m.parentId && map.has(m.parentId)) {
      map.get(m.parentId).children.push(node)
    } else if (!m.parentId) {
      roots.push(node)
    }
  }

  return roots
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleList({
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

function handleAdd() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, defaultForm)
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.roleName = row.roleName || ''
  form.roleCode = row.roleCode || ''
  form.description = row.description || ''
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除角色 "${row.roleName}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

async function handleAssignPermission(row: any) {
  currentRoleId.value = row.id
  currentRoleName.value = row.roleName

  // Load menu tree
  try {
    const menuRes = await getMenuList()
    const menuData = menuRes.data || menuRes
    const menus = Array.isArray(menuData) ? menuData : []
    menuTree.value = buildMenuTree(menus)
  } catch {
    menuTree.value = []
  }

  // Load current role's menu IDs
  try {
    const permRes = await getRoleMenus(row.id)
    const permData = permRes.data || permRes
    const menuIds = Array.isArray(permData) ? permData : permData.menuIds || []
    nextTick(() => {
      treeRef.value?.setCheckedKeys(menuIds)
    })
  } catch {
    // ignore
  }

  permDialogVisible.value = true
}


async function handleAssignPermissionSubmit() {
  if (!currentRoleId.value) return
  permLoading.value = true
  try {
    const checkedIds = treeRef.value?.getCheckedKeys(false) || []
    await assignRoleMenus(currentRoleId.value, checkedIds as number[])
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch {
    // error handled by interceptor
  } finally {
    permLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateRole(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createRole({ ...form })
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
