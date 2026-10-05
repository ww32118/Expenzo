<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()

const token = ref(route.query.token || '')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const verifying = ref(true)
const tokenValid = ref(false)
const errorMsg = ref('')
const success = ref(false)

// ─── Verify token on mount ────────────────────────────────────
onMounted(async () => {
  if (!token.value) {
    errorMsg.value = 'No reset token found. Please request a new reset link.'
    verifying.value = false
    return
  }
  try {
    await api.get('/auth/verify-reset-token', { params: { token: token.value } })
    tokenValid.value = true
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'This reset link is invalid or expired.'
  } finally {
    verifying.value = false
  }
})

// ─── Submit new password ──────────────────────────────────────
async function handleSubmit() {
  errorMsg.value = ''
  if (newPassword.value.length < 6) {
    errorMsg.value = 'Password must be at least 6 characters.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMsg.value = 'Passwords do not match.'
    return
  }
  loading.value = true
  try {
    await api.post('/auth/reset-password', {
      token: token.value,
      newPassword: newPassword.value
    })
    success.value = true
    setTimeout(() => router.push('/login'), 3000)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to reset password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center" style="background:var(--color-bg-base)">
    <div class="surface-card p-4 p-md-5" style="max-width:440px;width:100%;border-radius:var(--radius-lg)">

      <!-- Loading state -->
      <div v-if="verifying" class="text-center py-5">
        <div class="spinner-border mb-3" style="color:var(--color-primary)"></div>
        <p class="text-muted-soft small">Verifying reset link...</p>
      </div>

      <!-- Token invalid -->
      <div v-else-if="!tokenValid && !success" class="text-center">
        <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
             style="width:56px;height:56px;background:#fef2f2">
          <i class="bi bi-x-circle-fill" style="color:#dc2626;font-size:1.75rem"></i>
        </div>
        <h3 class="font-brand fw-bold mb-2">Invalid Link</h3>
        <p class="text-muted-soft small mb-4">{{ errorMsg }}</p>
        <router-link to="/forgot-password" class="btn btn-expenzo w-100">
          Request New Reset Link
        </router-link>
      </div>

      <!-- Success -->
      <div v-else-if="success" class="text-center">
        <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
             style="width:56px;height:56px;background:#f0fdf4">
          <i class="bi bi-check-circle-fill" style="color:#16a34a;font-size:1.75rem"></i>
        </div>
        <h3 class="font-brand fw-bold mb-2">Password Reset!</h3>
        <p class="text-muted-soft small mb-4">
          Your password has been changed. Redirecting to login...
        </p>
        <router-link to="/login" class="btn btn-expenzo w-100">
          Go to Login
        </router-link>
      </div>

      <!-- Reset form -->
      <template v-else>
        <div class="text-center mb-4">
          <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
               style="width:56px;height:56px;background:linear-gradient(135deg,#6366f1,#8b5cf6)">
            <i class="bi bi-shield-lock" style="color:#fff;font-size:1.5rem"></i>
          </div>
          <h3 class="font-brand fw-bold mb-1">Set New Password</h3>
          <p class="text-muted-soft small mb-0">Choose a strong password for your account.</p>
        </div>

        <div v-if="errorMsg"
             class="alert mb-3 py-2 px-3"
             style="background:#fef2f2;border:1px solid #fca5a5;border-radius:var(--radius-md);color:#dc2626;font-size:0.875rem">
          <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label class="form-label small text-muted-soft">New Password</label>
            <input
              v-model="newPassword"
              type="password"
              class="form-control"
              placeholder="Min 6 characters"
              required
              autofocus
            />
          </div>
          <div class="mb-3">
            <label class="form-label small text-muted-soft">Confirm Password</label>
            <input
              v-model="confirmPassword"
              type="password"
              class="form-control"
              placeholder="Re-enter password"
              required
            />
          </div>
          <button class="btn btn-expenzo w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Resetting...' : 'Reset Password' }}
          </button>
        </form>
      </template>
    </div>
  </div>
</template>
