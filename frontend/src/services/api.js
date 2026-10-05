import axios from 'axios'
import { ref } from 'vue'
import { logger } from '@/utils/logger'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// ═══════════════════════════════════════════════════════════════
// Axios instance
// ═══════════════════════════════════════════════════════════════
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// ═══════════════════════════════════════════════════════════════
// Global loading state — used by App.vue to render a top progress bar
// ═══════════════════════════════════════════════════════════════
export const isApiLoading = ref(false)
let activeRequests = 0

function incrementLoading() {
  activeRequests++
  if (activeRequests === 1) isApiLoading.value = true
}

function decrementLoading() {
  activeRequests = Math.max(0, activeRequests - 1)
  if (activeRequests === 0) isApiLoading.value = false
}

// ═══════════════════════════════════════════════════════════════
// Request interceptor — attach JWT
// ═══════════════════════════════════════════════════════════════
api.interceptors.request.use(
  (config) => {
    incrementLoading()

    const token = localStorage.getItem('token')

    // Only attach if it looks like a real JWT (not "null"/"undefined" and long enough)
    if (token && token !== 'null' && token !== 'undefined' && token.length > 20) {
      config.headers.Authorization = `Bearer ${token}`
      logger.log('🔑 Attached token to', config.method?.toUpperCase(), config.url)
    } else {
      logger.log('ℹ️ No token for', config.method?.toUpperCase(), config.url)
    }

    return config
  },
  (error) => {
    decrementLoading()
    return Promise.reject(error)
  }
)

// ═══════════════════════════════════════════════════════════════
// Response interceptor — handle 401 / session expiry
// ═══════════════════════════════════════════════════════════════
api.interceptors.response.use(
  (res) => {
    decrementLoading()
    return res
  },
  (err) => {
    decrementLoading()

    const url = err.config?.url || ''
    const status = err.response?.status

    // Requests that should NEVER trigger a logout/redirect on 401
    const isAuthFlow =
      url.includes('/users/login') ||
      url.includes('/users/add-user') ||
      url.includes('/users/register') ||
      url.includes('/users/verify-otp') ||
      url.includes('/users/resend-otp') ||
      url.includes('/auth/')

    if (status === 401 && !isAuthFlow) {
      logger.warn('🔒 401 on', url, '- clearing session')

      const authStore = useAuthStore()
      if (authStore.token) {
        authStore.logout()

        // Use the router for SPA-friendly navigation (no full reload)
        router.replace({
          name: 'login',
          query: { error: 'session_expired' },
        })
      }
    }

    // Normalize the error message so components can just read err.message
    if (!err.message && err.response?.data?.message) {
      err.message = err.response.data.message
    }

    return Promise.reject(err)
  }
)

// ═══════════════════════════════════════════════════════════════
// Optional — cancel all in-flight requests (called on logout)
// ═══════════════════════════════════════════════════════════════
const pendingControllers = new Set()

export function cancelAllRequests() {
  logger.log('🛑 Cancelling', pendingControllers.size, 'in-flight requests')
  pendingControllers.forEach((controller) => {
    try {
      controller.abort()
    } catch {
      /* ignore */
    }
  })
  pendingControllers.clear()
}

// Attach an AbortController to every request so it can be cancelled
api.interceptors.request.use((config) => {
  // Skip if the caller already provided their own signal
  if (!config.signal && !config.__controllerAttached) {
    const controller = new AbortController()
    config.signal = controller.signal
    config.__controllerAttached = true
    pendingControllers.add(controller)
  }
  return config
})

// Clean up the controller when the response comes back
api.interceptors.response.use(
  (res) => {
    if (res.config?.__controllerAttached) {
      pendingControllers.delete(res.config.signal)
    }
    return res
  },
  (err) => {
    if (err.config?.__controllerAttached) {
      pendingControllers.delete(err.config.signal)
    }
    return Promise.reject(err)
  }
)

export default api