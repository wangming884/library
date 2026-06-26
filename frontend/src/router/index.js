import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/reader-login',
    name: 'ReaderLogin',
    component: () => import('../views/login/ReaderLogin.vue'),
    meta: { title: '读者登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/login/Register.vue'),
    meta: { title: '读者注册' }
  },
  // ====== 管理员后台 ======
  {
    path: '/admin',
    component: () => import('../components/AdminLayout.vue'),
    meta: { requireAuth: true, role: 'admin' },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/admin/dashboard/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'readers', name: 'Readers', component: () => import('../views/admin/reader/ReaderList.vue'), meta: { title: '读者管理' } },
      { path: 'books', name: 'Books', component: () => import('../views/admin/book/BookList.vue'), meta: { title: '图书管理' } },
      { path: 'copies', name: 'Copies', component: () => import('../views/admin/book/CopyList.vue'), meta: { title: '副本管理' } },
      { path: 'categories', name: 'Categories', component: () => import('../views/admin/book/CategoryList.vue'), meta: { title: '分类管理' } },
      { path: 'borrow', name: 'Borrow', component: () => import('../views/admin/borrow/BorrowList.vue'), meta: { title: '借阅管理' } },
      { path: 'borrow-records', name: 'BorrowRecords', component: () => import('../views/admin/borrow/BorrowRecords.vue'), meta: { title: '借阅记录' } },
      { path: 'reservations', name: 'Reservations', component: () => import('../views/admin/borrow/ReservationList.vue'), meta: { title: '预约管理' } },
      { path: 'fines', name: 'Fines', component: () => import('../views/admin/fine/FineList.vue'), meta: { title: '罚款管理' } },
      { path: 'reports', name: 'Reports', component: () => import('../views/admin/report/ReportView.vue'), meta: { title: '统计报表' } },
      { path: 'config', name: 'Config', component: () => import('../views/admin/system/ConfigView.vue'), meta: { title: '系统设置' } },
      { path: 'logs', name: 'Logs', component: () => import('../views/admin/system/LogView.vue'), meta: { title: '操作日志' } },
      { path: 'announcements', name: 'Announcements', component: () => import('../views/admin/system/AnnouncementList.vue'), meta: { title: '公告管理' } },
      { path: 'feedback', name: 'Feedback', component: () => import('../views/admin/system/FeedbackList.vue'), meta: { title: '留言反馈' } },
    ]
  },
  // ====== 读者前台 ======
  {
    path: '/portal',
    component: () => import('../components/PortalLayout.vue'),
    meta: { requireAuth: true, role: 'reader' },
    redirect: '/portal/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/reader-portal/Home.vue'), meta: { title: '首页' } },
      { path: 'search', name: 'Search', component: () => import('../views/reader-portal/Search.vue'), meta: { title: '图书检索' } },
      { path: 'book/:id', name: 'BookDetail', component: () => import('../views/reader-portal/BookDetail.vue'), meta: { title: '图书详情' } },
      { path: 'center', name: 'Center', component: () => import('../views/reader-portal/Center.vue'), meta: { title: '个人中心' } },
      { path: 'feedback', name: 'FeedbackPortal', component: () => import('../views/reader-portal/Feedback.vue'), meta: { title: '留言反馈' } },
    ]
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory('/library/'),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = (to.meta.title || '图书馆管理系统') + ' - 图书馆管理系统'

  if (to.meta.requireAuth) {
    const userStore = useUserStore()
    if (!userStore.token) {
      next('/login')
      return
    }
    // 角色检查
    if (to.meta.role === 'admin' && userStore.roleKey === 'reader') {
      next('/portal/home')
      return
    }
    if (to.meta.role === 'reader' && userStore.roleKey !== 'reader') {
      next('/admin/dashboard')
      return
    }
  }
  next()
})

export default router
