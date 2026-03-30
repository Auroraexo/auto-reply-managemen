import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/rules',
    children: [
      {
        path: '/rules',
        name: 'Rules',
        component: () => import('@/views/Rules.vue'),
        meta: { title: '回复规则' }
      },
      {
        path: '/messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue'),
        meta: { title: '消息记录' }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: '/backup',
        name: 'Backup',
        component: () => import('@/views/Backup.vue'),
        meta: { title: '数据备份' }
      },
      {
        path: '/config',
        name: 'Config',
        component: () => import('@/views/Config.vue'),
        meta: { title: '公众号配置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 微信公众号自动回复管理系统` : '微信公众号自动回复管理系统'
  
  // 检查是否需要登录
  const token = localStorage.getItem('token')
  
  if (to.path === '/login' || to.path === '/register') {
    // 如果已登录，跳转到首页
    if (token) {
      next('/rules')
    } else {
      next()
    }
  } else {
    // 需要登录的页面
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
