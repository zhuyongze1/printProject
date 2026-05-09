import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { noAuth: true },
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单管理', icon: 'List' },
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/customer/CustomerList.vue'),
        meta: { title: '客户管理', icon: 'User' },
      },
      {
        path: 'knife-mold',
        name: 'KnifeMold',
        component: () => import('@/views/mold/KnifeMoldList.vue'),
        meta: { title: '刀模管理', icon: 'Tools' },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis' },
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'Setting' },
        redirect: '/system/user',
        children: [
          {
            path: 'user',
            name: 'User',
            component: () => import('@/views/system/user/UserList.vue'),
            meta: { title: '用户管理' },
          },
          {
            path: 'role',
            name: 'Role',
            component: () => import('@/views/system/role/RoleList.vue'),
            meta: { title: '角色管理' },
          },
          {
            path: 'menu',
            name: 'Menu',
            component: () => import('@/views/system/menu/MenuList.vue'),
            meta: { title: '菜单管理' },
          },
          {
            path: 'log',
            name: 'Log',
            component: () => import('@/views/system/log/LogList.vue'),
            meta: { title: '操作日志' },
          },
        ],
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.noAuth) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
