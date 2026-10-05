// main.js
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import '@/assets/theme.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from '@/App.vue'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import { logger } from '@/utils/logger'

const app = createApp(App)
const pinia = createPinia()

// ✅ Register Pinia FIRST
app.use(pinia)

// ✅ Restore session BEFORE router (so guards see valid auth)
const authStore = useAuthStore()
authStore.restoreSession()

// ✅ Then register router
app.use(createPinia())
app.use(router)

// Theme
const savedTheme = localStorage.getItem('expenzo-theme') || 'light'
document.documentElement.setAttribute('data-theme', savedTheme)

app.config.errorHandler = (err, instance, info) => {
  logger.error('🔴 Global Vue error:', err)
  logger.error('   Component:', instance?.$options?.name || instance)
  logger.error('   Info:', info)
}

// ─── Vue warning handler (dev only) ────────────────────────
if (import.meta.env.DEV) {
  app.config.warnHandler = (msg, instance, trace) => {
    console.warn('[Vue warn]:', msg, trace)
  }
}

// ─── Browser-level: unhandled promise rejections ────────────
window.addEventListener('unhandledrejection', (event) => {
  logger.error('🔴 Unhandled promise rejection:', event.reason)
})

// ─── Browser-level: synchronous runtime errors ──────────────
window.addEventListener('error', (event) => {
  logger.error('🔴 Global runtime error:', event.error || event.message)
})

app.mount('#app')