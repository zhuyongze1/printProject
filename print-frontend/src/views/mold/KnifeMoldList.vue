<template>
  <div class="page-container">
    <!-- Search -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索刀模名称/编号"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchForm.shapeType" placeholder="形状类型" clearable>
        <el-option label="矩形" value="RECTANGLE" />
        <el-option label="圆形" value="CIRCLE" />
        <el-option label="椭圆形" value="OVAL" />
        <el-option label="自定义" value="CUSTOM" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable>
        <el-option label="在库" value="IN_STOCK" />
        <el-option label="出库" value="OUT_STOCK" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- Action -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增刀模
      </el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="apple-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="moldNo" label="刀模编号" min-width="130" />
        <el-table-column prop="moldName" label="刀模名称" min-width="140" />
        <el-table-column prop="shapeType" label="形状类型" min-width="100">
          <template #default="{ row }">
            <el-tag :type="shapeTypeTag(row.shapeType)">
              {{ shapeTypeLabel(row.shapeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="型号" min-width="100" />
        <el-table-column prop="locationCode" label="位置编码" min-width="110" />
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'IN_STOCK' ? 'success' : 'warning'">
              {{ row.status === 'IN_STOCK' ? '在库' : '出库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button type="success" link @click="handlePrintLabel(row)">打印标签</el-button>
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

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑刀模' : '新增刀模'"
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
        <el-form-item label="刀模名称" prop="moldName">
          <el-input v-model="form.moldName" placeholder="请输入刀模名称" />
        </el-form-item>
        <el-form-item label="形状类型" prop="shapeType">
          <el-select v-model="form.shapeType" placeholder="请选择形状类型" style="width: 100%">
            <el-option label="矩形" value="RECTANGLE" />
            <el-option label="圆形" value="CIRCLE" />
            <el-option label="椭圆形" value="OVAL" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>

        <!-- Rectangle shape fields -->
        <template v-if="form.shapeType === 'RECTANGLE'">
          <el-form-item label="长度(mm)" prop="length">
            <el-input-number v-model="form.length" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="宽度(mm)" prop="width">
            <el-input-number v-model="form.width" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </template>

        <!-- Circle shape fields -->
        <template v-if="form.shapeType === 'CIRCLE'">
          <el-form-item label="直径(mm)" prop="diameter">
            <el-input-number v-model="form.diameter" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </template>

        <!-- Oval shape fields -->
        <template v-if="form.shapeType === 'OVAL'">
          <el-form-item label="长度(mm)" prop="length">
            <el-input-number v-model="form.length" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="宽度(mm)" prop="width">
            <el-input-number v-model="form.width" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </template>

        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="位置编码" prop="locationCode">
          <el-input v-model="form.locationCode" placeholder="请输入位置编码" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="在库" value="IN_STOCK" />
            <el-option label="出库" value="OUT_STOCK" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" type="textarea" :rows="2" />
        </el-form-item>
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
import { getMoldList, createMold, updateMold, deleteMold, printMoldLabel } from '@/api/mold'
import type { FormInstance, FormRules } from 'element-plus'

interface MoldForm {
  moldName: string
  shapeType: string
  model: string
  locationCode: string
  status: string
  length: number | null
  width: number | null
  diameter: number | null
  remark: string
}

const defaultForm: MoldForm = {
  moldName: '',
  shapeType: 'RECTANGLE',
  model: '',
  locationCode: '',
  status: 'IN_STOCK',
  length: null,
  width: null,
  diameter: null,
  remark: '',
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const formRef = ref<FormInstance>()

const searchForm = reactive({
  keyword: '',
  shapeType: '',
  status: '',
})

const form = reactive<MoldForm>({ ...defaultForm })

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const rules: FormRules = {
  moldName: [{ required: true, message: '请输入刀模名称', trigger: 'blur' }],
  shapeType: [{ required: true, message: '请选择形状类型', trigger: 'change' }],
}

function shapeTypeLabel(type: string): string {
  const map: Record<string, string> = {
    RECTANGLE: '矩形',
    CIRCLE: '圆形',
    OVAL: '椭圆形',
    CUSTOM: '自定义',
  }
  return map[type] || type
}

function shapeTypeTag(type: string): string {
  const map: Record<string, string> = {
    RECTANGLE: '',
    CIRCLE: 'success',
    OVAL: 'warning',
    CUSTOM: 'info',
  }
  return map[type] || ''
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.shapeType) params.shapeType = searchForm.shapeType
    if (searchForm.status) params.status = searchForm.status
    const res = await getMoldList(params)
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
  searchForm.shapeType = ''
  searchForm.status = ''
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
  form.moldName = row.moldName || ''
  form.shapeType = row.shapeType || 'RECTANGLE'
  form.model = row.model || ''
  form.locationCode = row.locationCode || ''
  form.status = row.status || 'IN_STOCK'
  form.length = row.length ?? null
  form.width = row.width ?? null
  form.diameter = row.diameter ?? null
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除刀模 "${row.moldName}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteMold(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

async function handlePrintLabel(row: any) {
  try {
    await printMoldLabel({ id: row.id })
    ElMessage.success('打印标签已提交')
  } catch {
    // error handled by interceptor
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = { ...form }
    // Clean shape-specific fields
    if (form.shapeType === 'CIRCLE') {
      submitData.length = null
      submitData.width = null
    } else if (form.shapeType === 'RECTANGLE' || form.shapeType === 'OVAL') {
      submitData.diameter = null
    } else if (form.shapeType === 'CUSTOM') {
      submitData.length = null
      submitData.width = null
      submitData.diameter = null
    }

    if (isEdit.value && editId.value) {
      await updateMold(editId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createMold(submitData)
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
