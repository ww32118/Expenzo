import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ═══════════════════════════════════════════════════
  // Public auth routes — wrapped in AuthLayout (no sidebar)
  // ═══════════════════════════════════════════════════
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, layout: 'auth', title: 'Log In' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, layout: 'auth', title: 'Create Account' },
  },
  {
    path: '/oauth-success',
    name: 'oauth-success',
    component: () => import('@/views/auth/OAuthSuccess.vue'),
    meta: { public: true, layout: 'auth', title: 'Signing In…' },
  },
  {
    path: '/verify-otp',
    name: 'verify-otp',
    component: () => import('@/views/auth/VerifyOtpView.vue'),
    meta: { public: true, layout: 'auth', title: 'Verify Email' },
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    meta: { public: true, layout: 'auth', title: 'Forgot Password' },
  },
  {
    path: '/reset-password',
    name: 'reset-password',
    component: () => import('@/views/auth/ResetPasswordView.vue'),
    meta: { public: true, layout: 'auth', title: 'Reset Password' },
  },

  // ═══════════════════════════════════════════════════
  // Protected app routes — wrapped in AppShell (sidebar + topbar)
  // ═══════════════════════════════════════════════════
  {
    path: '/',
    name: 'dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { layout: 'app', title: 'Dashboard' },
  },
  {
    path: '/transactions',
    name: 'transactions',
    component: () => import('@/views/TransactionsView.vue'),
    meta: { layout: 'app', title: 'Transactions' },
  },
  {
    path: '/categories',
    name: 'categories',
    component: () => import('@/views/CategoriesView.vue'),
    meta: { layout: 'app', title: 'Categories' },
  },
  {
    path: '/budgets',
    name: 'budgets',
    component: () => import('@/views/BudgetsView.vue'),
    meta: { layout: 'app', title: 'Budgets' },
  },
  {
    path: '/bank-accounts',
    name: 'bank-accounts',
    component: () => import('@/views/BankAccountsView.vue'),
    meta: { layout: 'app', title: 'Bank Accounts' },
  },
  {
    path: '/export',
    name: 'export',
    component: () => import('@/views/ExportView.vue'),
    meta: { layout: 'app', title: 'Export' },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { layout: 'app', title: 'Profile' },
  },

  // ═══════════════════════════════════════════════════
  // Catch-all
  // ═══════════════════════════════════════════════════
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

// ─── Route classification ────────────────────────────────────
const GUEST_ONLY_ROUTES = ['login', 'register']
const ALLOW_WHEN_AUTHENTICATED = [
  'oauth-success',
  'verify-otp',
  'reset-password',
  'forgot-password',
]

// ─── Router instance ─────────────────────────────────────────
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  },
})

// ─── Navigation guard ────────────────────────────────────────
router.beforeEach((to, from) => {
  const authStore = useAuthStore()

  // 1. Protected route + not logged in → redirect to login
  if (!to.meta.public && !authStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 2. Guest-only route + already logged in → redirect to dashboard
  if (
    to.meta.public &&
    authStore.isLoggedIn &&
    GUEST_ONLY_ROUTES.includes(to.name) &&
    !ALLOW_WHEN_AUTHENTICATED.includes(to.name)
  ) {
    return { name: 'dashboard' }
  }

  return true
})

// ─── Document title ──────────────────────────────────────────
router.afterEach((to) => {
  const base = 'Expenzo'
  document.title = to.meta.title ? `${to.meta.title} · ${base}` : base
})

export default router