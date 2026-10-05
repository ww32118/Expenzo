// src/stores/auth.js
import { defineStore } from 'pinia'
import api from '@/services/api'

function safeParse(value) {
  try {
    return value ? JSON.parse(value) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    user: null,
    loading: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isLoggedIn: (state) => !!state.token, // alias

    displayName: (state) => {
      const u = state.user
      if (!u) return 'Guest'
      return (
        u.name ||
        u.username ||
        u?.responseDTO?.name ||
        u?.responseDTO?.username ||
        (u.email ? u.email.split('@')[0] : 'Guest')
      )
    },

    userId: (state) => {
      const u = state.user
      return u?.id || u?.responseDTO?.id || null
    },
  },

  actions: {
    // ✅ Restores auth from localStorage on app boot
    restoreSession() {
      const token = localStorage.getItem('token')
      const user = safeParse(localStorage.getItem('user'))
      if (token) this.token = token
      if (user) this.user = user
    },

    // ✅ Unified setter (recommended)
    setAuth(token, user) {
      this.token = token
      this.user = user?.responseDTO ? user.responseDTO : user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    // ✅ Individual setters (used by LoginView)
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUser(user) {
      this.user = user?.responseDTO ? user.responseDTO : user
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    // ✅ NEW: Fetch the current user from the backend using the stored JWT.
    // Called by OAuthSuccess.vue and on app startup if user isn't in state.
    async fetchCurrentUser() {
      if (!this.token) {
        throw new Error('No token available')
      }
      this.loading = true
      try {
        const res = await api.get('/users/me')
        this.setUser(res.data)
        return res.data
      } finally {
        this.loading = false
      }
    },

    // ✅ Alias, in case other files call loadUser()
    async loadUser() {
      return this.fetchCurrentUser()
    },

    logout() {
      this.token = null
      this.user = null
      this.loading = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
  },
})