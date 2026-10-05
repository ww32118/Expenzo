<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleSubmit() {
  errorMsg.value = ''

  if (newPassword.value.length < 6) {
    errorMsg.value = 'New password must be at least 6 characters.'
    return
  }
  if (newPassword.value === currentPassword.value) {
    errorMsg.value = 'New password must be different from your current password.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMsg.value = 'Passwords do not match.'
    return
  }

  loading.value = true
  try {
    await api.put('/users/change-password', {
      id: authStore.user?.id,
      oldPassword: currentPassword.value,
      newPassword: newPassword.value
    })
    // Clear the mustChangePassword flag in local state
    if (authStore.user) {
      authStore.user.mustChangePassword = false
    }
    router.push('/dashboard')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.response?.data || 'Failed to change password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center" style="background:var(--color-bg-base)">
    <div class="surface-card p-4 p-md-5" style="max-width:440px;width:100%;border-radius:var(--radius-lg)">
      <div class="text-center mb-4">
        <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
             style="width:56px;height:56px;background:linear-gradient(135deg,#f59e0b,#f97316)">
          <i class="bi bi-shield-exclamation" style="color:#fff;font-size:1.5rem"></i>
        </div>
        <h3 class="font-brand fw-bold mb-1">Change Your Password</h3>
        <p class="text-muted-soft small mb-0">
          You're using a temporary password. Please set a new one to continue.
        </p>
      </div>

      <div v-if="errorMsg"
           class="alert mb-3 py-2 px-3"
           style="background:#fef2f2;border:1px solid #fca5a5;border-radius:var(--radius-md);color:#dc2626;font-size:0.875rem">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label small text-muted-soft">Current (Temporary) Password</label>
          <input
            v-model="currentPassword"
            type="password"
            class="form-control"
            placeholder="From your welcome email"
            required
            autofocus
          />
        </div>

        <div class="mb-3">
          <label class="form-label small text-muted-soft">New Password</label>
          <input
            v-model="newPassword"
            type="password"
            class="form-control"
            placeholder="Min 6 characters"
            required
          />
        </div>

        <div class="mb-3">
          <label class="form-label small text-muted-soft">Confirm New Password</label>
          <input
            v-model="confirmPassword"
            type="password"
            class="form-control"
            placeholder="Re-enter new password"
            required
          />
        </div>

        <button class="btn btn-expenzo w-100" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          {{ loading ? 'Saving...' : 'Set New Password & Continue' }}
        </button>
      </form>
    </div>
  </div>
</template>
