<template>
  <div class="page-container">
    <!-- Search -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索操作/方法/IP"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-date-picker
        v-model="searchForm.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        clearable
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="apple-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作" min-width="140" show-overflow-tooltip />
        <el-table-column prop="method" label="方法" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <code style="font-size: 12px; background: #f5f5f7; padding: 2px 6px; border-radius: 4px;">
              {{ row.method }}
            </code>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" align="right">
          <template #default="{ row }">
            <span>{{ row.costTime ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" min-width="160" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLogList } from '@/api/log'

const loading = ref(false)
const tableData = ref<any[]>([])

const searchForm = reactive({
  keyword: '',
  dateRange: null as string[] | null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
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

    const res = await getLogList(params)
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
  pagination.page = 1
  fetchData()
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
