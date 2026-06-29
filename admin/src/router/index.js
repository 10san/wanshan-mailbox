import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
  {
    path: '/',
    component: () => import('@/views/LayoutView.vue'),
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/DashboardView.vue') },
      { path: 'posts', name: 'Posts', component: () => import('@/views/PostManageView.vue') },
      { path: 'reports', name: 'Reports', component: () => import('@/views/ReportManageView.vue') },
      { path: 'sensitive-words', name: 'SensitiveWords', component: () => import('@/views/SensitiveWordView.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
