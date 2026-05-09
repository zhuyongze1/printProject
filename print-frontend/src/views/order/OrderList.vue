<template>
  <div class="page-container">
    <!-- Search -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索订单号/印刷名称/送货单号"
        clearable
        @keyup.enter="handleSearch"
        style="width: 240px"
      />
      <el-date-picker
        v-model="searchForm.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="下单开始日期"
        end-placeholder="下单结束日期"
        value-format="YYYY-MM-DD"
        clearable
      />
      <el-select v-model="searchForm.shipped" placeholder="发货状态" clearable style="width: 130px">
        <el-option label="已发货" :value="true" />
        <el-option label="未发货" :value="false" />
      </el-select>
      <el-select
        v-model="searchForm.customerId"
        placeholder="选择客户"
        clearable
        filterable
        style="width: 160px"
      >
        <el-option
          v-for="c in customerOptions"
          :key="c.id"
          :label="c.customerName"
          :value="c.id"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- Action -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增订单
      </el-button>
      <el-button @click="handleImport">
        <el-icon><Upload /></el-icon>导入Excel
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>导出Excel
      </el-button>
      <el-button type="success" @click="handlePrintDelivery">
        <el-icon><Printer /></el-icon>打印送货单
      </el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="apple-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="orderNo" label="订单编号" min-width="130" />
        <el-table-column prop="orderDate" label="下单日期" min-width="100" />
        <el-table-column prop="deliveryNo" label="送货单号" min-width="120" />
        <el-table-column prop="printName" label="印刷名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="unitPrice" label="单价" width="80" align="right">
          <template #default="{ row }">
            {{ row.unitPrice != null ? Number(row.unitPrice).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="right">
          <template #default="{ row }">
            {{ row.amount != null ? Number(row.amount).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="shipped" label="发货状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.shipped ? 'success' : 'info'" size="small">
              {{ row.shipped ? '已发货' : '未发货' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
      :title="isEdit ? '编辑订单' : '新增订单'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="客户名称" prop="customerId">
          <el-autocomplete
            v-model="form.customerName"
            :fetch-suggestions="queryCustomerSuggestions"
            placeholder="请输入客户名称搜索"
            value-key="customerName"
            style="width: 100%"
            @select="handleCustomerSelect"
          />
        </el-form-item>
        <el-form-item label="刀模名称" prop="moldId">
          <el-autocomplete
            v-model="form.moldName"
            :fetch-suggestions="queryMoldSuggestions"
            placeholder="请输入刀模名称搜索"
            value-key="moldName"
            style="width: 100%"
            @select="handleMoldSelect"
          />
        </el-form-item>
        <el-form-item label="下单日期" prop="orderDate">
          <el-date-picker
            v-model="form.orderDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="送货单号" prop="deliveryNo">
          <el-input v-model="form.deliveryNo" placeholder="请输入送货单号" />
        </el-form-item>
        <el-form-item label="印刷名称" prop="printName">
          <el-input v-model="form.printName" placeholder="请输入印刷名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="数量" prop="quantity">
              <el-input-number
                v-model="form.quantity"
                :min="0"
                :precision="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价" prop="unitPrice">
              <el-input-number
                v-model="form.unitPrice"
                :min="0"
                :precision="2"
                :step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="金额">
              <span class="amount-display">￥{{ computedAmount }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发货状态">
          <el-switch v-model="form.shipped" active-text="已发货" inactive-text="未发货" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="订单详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ viewData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单日期">{{ viewData.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ viewData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="刀模名称">{{ viewData.moldName }}</el-descriptions-item>
        <el-descriptions-item label="送货单号">{{ viewData.deliveryNo }}</el-descriptions-item>
        <el-descriptions-item label="印刷名称">{{ viewData.printName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ viewData.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单价">￥{{ viewData.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="金额">￥{{ viewData.amount }}</el-descriptions-item>
        <el-descriptions-item label="发货状态">
          <el-tag :type="viewData.shipped ? 'success' : 'info'" size="small">
            {{ viewData.shipped ? '已发货' : '未发货' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- File input for import (hidden) -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".xlsx,.xls"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderList,
  createOrder,
  updateOrder,
  deleteOrder,
  importOrders,
  exportOrders,
  printDeliveryNote,
} from '@/api/order'
import { getAllCustomers } from '@/api/customer'
import { getAllMolds } from '@/api/mold'
import type { FormInstance, FormRules } from 'element-plus'

interface OrderForm {
  customerId: number | null
  customerName: string
  moldId: number | null
  moldName: string
  orderDate: string
  deliveryNo: string
  printName: string
  quantity: number
  unitPrice: number
  shipped: boolean
  remark: string
}

const defaultForm: OrderForm = {
  customerId: null,
  customerName: '',
  moldId: null,
  moldName: '',
  orderDate: '',
  deliveryNo: '',
  printName: '',
  quantity: 0,
  unitPrice: 0,
  shipped: false,
  remark: '',
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const selectedOrders = ref<any[]>([])
const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement>()

const customerOptions = ref<any[]>([])
const customerCache = ref<any[]>([])
const moldCache = ref<any[]>([])

const searchForm = reactive({
  keyword: '',
  dateRange: null as string[] | null,
  shipped: null as boolean | null,
  customerId: null as number | null,
})

const form = reactive<OrderForm>({ ...defaultForm })

const viewData = reactive<any>({})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const rules: FormRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  moldId: [{ required: true, message: '请选择刀模', trigger: 'change' }],
  orderDate: [{ required: true, message: '请选择下单日期', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
}

const computedAmount = computed(() => {
  const qty = form.quantity || 0
  const price = form.unitPrice || 0
  return (qty * price).toFixed(2)
})

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.shipped !== null) params.shipped = searchForm.shipped
    if (searchForm.customerId) params.customerId = searchForm.customerId

    const res = await getOrderList(params)
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
  searchForm.dateRange = null
  searchForm.shipped = null
  searchForm.customerId = null
  pagination.page = 1
  fetchData()
}

function handleSelectionChange(rows: any[]) {
  selectedOrders.value = rows
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, { ...defaultForm, orderDate: new Date().toISOString().slice(0, 10) })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.customerId = row.customerId
  form.customerName = row.customerName || ''
  form.moldId = row.moldId
  form.moldName = row.moldName || ''
  form.orderDate = row.orderDate || ''
  form.deliveryNo = row.deliveryNo || ''
  form.printName = row.printName || ''
  form.quantity = row.quantity ?? 0
  form.unitPrice = row.unitPrice ?? 0
  form.shipped = !!row.shipped
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleView(row: any) {
  Object.assign(viewData, row)
  viewDialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除订单 "${row.orderNo}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

function handleImport() {
  fileInputRef.value?.click()
}

async function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    await importOrders(formData)
    ElMessage.success('导入成功')
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    target.value = ''
  }
}

async function handleExport() {
  try {
    const params: any = {}
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.shipped !== null) params.shipped = searchForm.shipped
    if (searchForm.customerId) params.customerId = searchForm.customerId

    const res = await exportOrders(params)
    const blob = new Blob([res as any], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `订单导出_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    // error handled by interceptor
  }
}

async function handlePrintDelivery() {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请至少选择一个订单')
    return
  }
  try {
    const ids = selectedOrders.value.map((o) => o.id)
    await printDeliveryNote({ orderIds: ids })
    ElMessage.success('打印任务已提交')
  } catch {
    // error handled by interceptor
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      ...form,
      amount: Number(computedAmount.value),
    }

    if (isEdit.value && editId.value) {
      await updateOrder(editId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createOrder(submitData)
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

// Autocomplete: customers
async function queryCustomerSuggestions(query: string, cb: (results: any[]) => void) {
  if (!query) {
    cb(customerCache.value.slice(0, 10))
    return
  }
  const filtered = customerCache.value.filter(
    (c) => c.customerName && c.customerName.includes(query),
  )
  cb(filtered.slice(0, 10))
}

function handleCustomerSelect(item: any) {
  form.customerId = item.id
  form.customerName = item.customerName
}

// Autocomplete: molds
async function queryMoldSuggestions(query: string, cb: (results: any[]) => void) {
  if (!query) {
    cb(moldCache.value.slice(0, 10))
    return
  }
  const filtered = moldCache.value.filter((m) => m.moldName && m.moldName.includes(query))
  cb(filtered.slice(0, 10))
}

function handleMoldSelect(item: any) {
  form.moldId = item.id
  form.moldName = item.moldName
}

// Load caches and initial data
async function loadCaches() {
  try {
    const custRes = await getAllCustomers()
    const custData = custRes.data || custRes
    customerCache.value = Array.isArray(custData) ? custData : []
    customerOptions.value = customerCache.value

    const moldRes = await getAllMolds()
    const moldData = moldRes.data || moldRes
    moldCache.value = Array.isArray(moldData) ? moldData : []
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadCaches()
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.amount-display {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-primary);
  line-height: 32px;
}
</style>
