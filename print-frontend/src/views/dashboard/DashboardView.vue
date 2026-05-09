<template>
  <div class="page-container">
    <div class="dashboard-header">
      <h2 class="dashboard-title">仪表盘</h2>
      <p class="dashboard-subtitle">欢迎使用印刷管理系统</p>
    </div>
    <div class="stat-cards">
      <el-card
        v-for="card in statCards"
        :key="card.label"
        shadow="never"
        class="apple-card stat-card"
      >
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-icon" :style="{ background: card.color }">
          <el-icon :size="24"><component :is="card.icon" /></el-icon>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/dashboard'

interface StatCard {
  label: string
  value: string | number
  icon: string
  color: string
  key: string
}

const statCards = ref<StatCard[]>([
  { label: '总订单数', value: 0, icon: 'List', color: '#0071e3', key: 'totalOrders' },
  { label: '总客户数', value: 0, icon: 'User', color: '#34c759', key: 'totalCustomers' },
  { label: '总刀模数', value: 0, icon: 'Tools', color: '#ff9500', key: 'totalMolds' },
  { label: '总金额(已发货)', value: '0.00', icon: 'Money', color: '#ff3b30', key: 'totalAmount' },
])

async function fetchStats() {
  try {
    const res = await getDashboardStats()
    const data = res.data || res
    statCards.value = statCards.value.map((card) => {
      const val = data[card.key]
      return {
        ...card,
        value: card.key === 'totalAmount' ? (val ?? 0).toFixed(2) : (val ?? 0),
      }
    })
  } catch {
    // silently ignore
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--app-text-primary);
  margin: 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: var(--app-text-secondary);
  margin: 4px 0 0 0;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
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

.stat-icon {
  position: absolute;
  right: 16px;
  top: 16px;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0.85;
}
</style>
