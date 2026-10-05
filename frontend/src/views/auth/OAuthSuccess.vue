<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  const token = route.query.token

  if (!token) {
    router.push({ name: 'login', query: { error: 'no_token' } })
    return
  }

  try {
    authStore.setToken(token)
    await authStore.fetchCurrentUser()
    router.push({ name: 'dashboard' })
  } catch (err) {
    console.error('❌ OAuth login failed:', err)
    console.error('Status:', err.response?.status)
    console.error('Data:', err.response?.data)
    authStore.logout()
    router.push({ name: 'login', query: { error: 'authentication_failed' } })
  }
})
</script>

<template>
  <div class="d-flex flex-column align-items-center justify-content-center min-vh-100">
    <div class="text-center p-5">
      <div class="spinner-border text-warning" role="status"></div>
      <p class="mt-3 text-muted">Signing you in...</p>
    </div>
  </div>
</template>