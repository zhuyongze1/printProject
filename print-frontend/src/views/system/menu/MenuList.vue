<template>
  <div class="page-container">
    <!-- Action -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增菜单
      </el-button>
    </div>

    <!-- Table (tree) -->
    <el-card shadow="never" class="apple-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        style="width: 100%"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180">
          <template #default="{ row }">
            <span v-if="row.icon" style="margin-right: 6px">
              <el-icon :size="14"><component :is="row.icon" /></el-icon>
            </span>
            {{ row.menuName }}
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :size="18"><component :is="row.icon" /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" align="center" />
        <el-table-column prop="permission" label="权限标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="menuType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTag(row.menuType)" size="small">
              {{ menuTypeLabel(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        label-position="left"
      >
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuOptions"
            :props="{ label: 'menuName', children: 'children', value: 'id' }"
            placeholder="请选择上级菜单（不选为顶级菜单）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径，如 /system/user" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="请输入权限标识，如 sys:user:list" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="menuType">
              <el-select v-model="form.menuType" style="width: 100%">
                <el-option label="目录" value="M" />
                <el-option label="菜单" value="C" />
                <el-option label="按钮" value="F" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-select v-model="form.icon" filterable allow-create clearable style="width: 100%">
                <el-option
                  v-for="icon in iconOptions"
                  :key="icon"
                  :label="icon"
                  :value="icon"
                >
                  <span style="display: flex; align-items: center; gap: 8px;">
                    <el-icon :size="16"><component :is="icon" /></el-icon>
                    <span>{{ icon }}</span>
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序号" prop="sort">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, createMenu, updateMenu, deleteMenu } from '@/api/menu'
import type { FormInstance, FormRules } from 'element-plus'

interface MenuForm {
  parentId: number | null
  menuName: string
  path: string
  component: string
  permission: string
  menuType: string
  icon: string
  sort: number
  status: number
}

const defaultForm: MenuForm = {
  parentId: null,
  menuName: '',
  path: '',
  component: '',
  permission: '',
  menuType: 'M',
  icon: '',
  sort: 0,
  status: 1,
}

const iconOptions = [
  'Odometer', 'List', 'User', 'Tools', 'Setting', 'DataAnalysis',
  'Fold', 'Expand', 'Plus', 'Edit', 'Delete', 'Search',
  'Upload', 'Download', 'Printer', 'Money', 'Link', 'Document',
  'Files', 'FolderOpened', 'FolderDelete', 'Menu', 'Grid',
]

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const menuOptions = ref<any[]>([])
const formRef = ref<FormInstance>()

const form = reactive<MenuForm>({ ...defaultForm })

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
}

function menuTypeLabel(type: string): string {
  const map: Record<string, string> = { M: '目录', C: '菜单', F: '按钮' }
  return map[type] || type
}

function menuTypeTag(type: string): string {
  const map: Record<string, string> = { M: '', C: 'success', F: 'warning' }
  return map[type] || ''
}

function buildTree(menus: any[]): any[] {
  const map = new Map<number, any>()
  const roots: any[] = []
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
    const res = await getMenuList()
    const data = res.data || res
    const menus = Array.isArray(data) ? data : []
    tableData.value = buildTree(menus)
    menuOptions.value = buildTree(menus)
  } catch {
    tableData.value = []
    menuOptions.value = []
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
  form.parentId = row.parentId ?? null
  form.menuName = row.menuName || ''
  form.path = row.path || ''
  form.component = row.component || ''
  form.permission = row.permission || ''
  form.menuType = row.menuType || 'M'
  form.icon = row.icon || ''
  form.sort = row.sort ?? 0
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除菜单 "${row.menuName}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = { ...form }
    if (!submitData.parentId) submitData.parentId = null as any
    if (!submitData.icon) submitData.icon = ''
    if (!submitData.permission) submitData.permission = ''

    if (isEdit.value && editId.value) {
      await updateMenu(editId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createMenu(submitData)
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

.text-muted {
  color: var(--app-text-secondary);
}
</style>
