<script setup>
import { ref } from 'vue'
import api from '@/services/api'

const email = ref('')
const loading = ref(false)
const sent = ref(false)
const errorMsg = ref('')

async function handleSubmit() {
  if (!email.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    await api.post('/auth/forgot-password', { email: email.value })
    sent.value = true
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Something went wrong. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center" style="background:var(--color-bg-base)">
    <div class="surface-card p-4 p-md-5" style="max-width:440px;width:100%;border-radius:var(--radius-lg)">

      <!-- Before sending -->
      <template v-if="!sent">
        <div class="text-center mb-4">
          <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
               style="width:56px;height:56px;background:linear-gradient(135deg,#6366f1,#8b5cf6)">
            <i class="bi bi-key" style="color:#fff;font-size:1.5rem"></i>
          </div>
          <h3 class="font-brand fw-bold mb-1">Forgot Password?</h3>
          <p class="text-muted-soft small mb-0">
            Enter your email and we'll send you a reset link.
          </p>
        </div>

        <div v-if="errorMsg"
             class="alert mb-3 py-2 px-3"
             style="background:#fef2f2;border:1px solid #fca5a5;border-radius:var(--radius-md);color:#dc2626;font-size:0.875rem">
          <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label class="form-label small text-muted-soft">Email Address</label>
            <input
              v-model="email"
              type="email"
              class="form-control"
              placeholder="you@example.com"
              required
              autofocus
            />
          </div>
          <button class="btn btn-expenzo w-100" :disabled="loading || !email">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Sending...' : 'Send Reset Link' }}
          </button>
        </form>

        <div class="text-center mt-3">
          <router-link to="/login" class="small" style="color:var(--color-primary);text-decoration:none">
            <i class="bi bi-arrow-left me-1"></i>Back to Login
          </router-link>
        </div>
      </template>

      <!-- After sending -->
      <template v-else>
        <div class="text-center">
          <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
               style="width:56px;height:56px;background:#f0fdf4">
            <i class="bi bi-check-circle-fill" style="color:#16a34a;font-size:1.75rem"></i>
          </div>
          <h3 class="font-brand fw-bold mb-2">Check Your Email</h3>
          <p class="text-muted-soft small mb-3">
            If an account exists for <strong>{{ email }}</strong>, we've sent a password reset link. Check your inbox (and spam folder).
          </p>
          <p class="text-muted-soft small mb-4">
            The link expires in <strong>30 minutes</strong>.
          </p>
          <router-link to="/login" class="btn btn-expenzo w-100">
            Back to Login
          </router-link>
        </div>
      </template>
    </div>
  </div>
</template>
