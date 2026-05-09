<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="app-sidebar">
      <div class="sidebar-header">
        <span v-if="!isCollapsed" class="sidebar-title">印刷管理系统</span>
        <span v-else class="sidebar-title-mini">印</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed"
        :router="true"
        :collapse-transition="false"
        background-color="#1d1d1f"
        text-color="#ffffff"
        active-text-color="#0071e3"
      >
        <template v-for="item in menuList" :key="item.id">
          <el-menu-item v-if="!item.children || item.children.length === 0" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.menuName }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="item.path || item.id.toString()">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.menuName }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.id" :index="child.path">
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-navbar">
        <div class="navbar-left">
          <el-icon style="cursor: pointer; font-size: 20px" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" style="margin-left: 16px">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="navbar-right">
          <span class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-dropdown trigger="click">
            <el-avatar :size="32" style="cursor: pointer; background-color: #0071e3">
              {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'U')[0] }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

// Static menu for now; will be dynamic later
const menuList = computed(() => [
  { id: 1, menuName: '仪表盘', path: '/dashboard', icon: 'Odometer', children: [] },
  { id: 2, menuName: '订单管理', path: '/order', icon: 'List', children: [] },
  { id: 3, menuName: '客户管理', path: '/customer', icon: 'User', children: [] },
  { id: 4, menuName: '刀模管理', path: '/knife-mold', icon: 'Tools', children: [] },
  { id: 5, menuName: '数据统计', path: '/statistics', icon: 'DataAnalysis', children: [] },
  {
    id: 6, menuName: '系统管理', icon: 'Setting', children: [
      { id: 7, menuName: '用户管理', path: '/system/user' },
      { id: 8, menuName: '角色管理', path: '/system/role' },
      { id: 9, menuName: '菜单管理', path: '/system/menu' },
      { id: 10, menuName: '操作日志', path: '/system/log' },
    ],
  },
])

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  if (!userStore.userInfo) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.app-sidebar {
  background-color: #1d1d1f;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-title {
  font-size: 16px;
  letter-spacing: 1px;
}

.sidebar-title-mini {
  font-size: 20px;
}

.el-menu {
  border-right: none;
}

.app-navbar {
  height: var(--app-navbar-height);
  background: var(--app-card-bg);
  border-bottom: 1px solid var(--app-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: var(--app-text-primary);
  font-size: 14px;
}

.app-main {
  background: var(--app-bg-color);
  padding: 0;
  overflow-y: auto;
}
</style>
