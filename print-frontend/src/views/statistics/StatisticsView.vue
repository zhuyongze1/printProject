<template>
  <div class="page-container">
    <!-- Stat cards -->
    <div class="stat-cards">
      <el-card
        v-for="card in statCards"
        :key="card.label"
        shadow="never"
        class="apple-card stat-card"
      >
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </el-card>
    </div>

    <!-- Charts -->
    <div class="charts-row">
      <el-card shadow="never" class="apple-card chart-card">
        <template #header>
          <span class="chart-title">近30天订单趋势</span>
        </template>
        <VChart :option="trendOption" autoresize style="height: 350px" />
      </el-card>
      <el-card shadow="never" class="apple-card chart-card">
        <template #header>
          <span class="chart-title">客户下单排名 (Top 10)</span>
        </template>
        <VChart :option="rankingOption" autoresize style="height: 350px" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import VChart from 'vue-echarts'
import 'echarts'
import type { EChartsOption } from 'echarts'
import { getDashboardStats, getOrderTrend, getCustomerRanking } from '@/api/dashboard'

const statCards = ref([
  { label: '总订单数', value: 0, key: 'totalOrders' },
  { label: '总客户数', value: 0, key: 'totalCustomers' },
  { label: '总刀模数', value: 0, key: 'totalMolds' },
  { label: '总金额(已发货)', value: '0.00', key: 'totalAmount' },
])

const trendOption = ref<EChartsOption>({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [], axisLabel: { rotate: 45, fontSize: 11 } },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      data: [],
      itemStyle: { color: '#0071e3' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(0,113,227,0.3)' },
            { offset: 1, color: 'rgba(0,113,227,0.02)' },
          ],
        },
      },
    },
  ],
})

const rankingOption = ref<EChartsOption>({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [], axisLabel: { rotate: 30, fontSize: 11 } },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: '订单数',
      type: 'bar',
      data: [],
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#0071e3' },
            { offset: 1, color: 'rgba(0,113,227,0.4)' },
          ],
        },
        borderRadius: [4, 4, 0, 0],
      },
    },
  ],
})

async function fetchStats() {
  try {
    const res = await getDashboardStats()
    const data = res.data || res
    statCards.value = statCards.value.map((card) => ({
      ...card,
      value: card.key === 'totalAmount' ? (data[card.key] ?? 0).toFixed(2) : (data[card.key] ?? 0),
    }))
  } catch {
    // silently ignore
  }
}

async function fetchTrend() {
  try {
    const res = await getOrderTrend({ days: 30 })
    const data = res.data || res
    const list = Array.isArray(data) ? data : data.records || data.list || []
    trendOption.value = {
      ...trendOption.value,
      xAxis: { ...trendOption.value.xAxis, data: list.map((i: any) => i.date) },
      series: [
        { ...(trendOption.value as any).series[0], data: list.map((i: any) => i.count ?? 0) },
      ],
    }
  } catch {
    // silently ignore
  }
}

async function fetchRanking() {
  try {
    const res = await getCustomerRanking({ top: 10 })
    const data = res.data || res
    const list = Array.isArray(data) ? data : data.records || data.list || []
    rankingOption.value = {
      ...rankingOption.value,
      xAxis: { ...rankingOption.value.xAxis, data: list.map((i: any) => i.customerName) },
      series: [
        { ...(rankingOption.value as any).series[0], data: list.map((i: any) => i.orderCount ?? 0) },
      ],
    }
  } catch {
    // silently ignore
  }
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  fetchRanking()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--app-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--app-text-secondary);
  margin-top: 4px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
}
</style>
