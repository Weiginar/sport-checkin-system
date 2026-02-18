import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'user', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'sport', name: 'SportManage', component: () => import('../views/SportManage.vue'), meta: { title: '运动项目管理' } },
      { path: 'checkin', name: 'CheckinManage', component: () => import('../views/CheckinManage.vue'), meta: { title: '打卡记录管理' } },
      { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { title: '数据统计' } },
      { path: 'points', name: 'PointsManage', component: () => import('../views/PointsManage.vue'), meta: { title: '积分记录管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path !== '/login' && role !== 'ADMIN') {
    localStorage.clear()
    next('/login')
  } else {
    next()
  }
})

export default router
