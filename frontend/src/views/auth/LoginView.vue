<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import userService from '@/services/userService'
import { useAuthStore } from '@/stores/auth'

// ─── Composables ───────────────────────────────────
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ─── State ─────────────────────────────────────────
const form = reactive({
  email: '',
  password: '',
  rememberMe: false,
})
const loading = ref(false)
const googleLoading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const showPassword = ref(false)
const isGoogleAccountError = ref(false)   // ✅ NEW

// ─── Config ────────────────────────────────────────
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// ─── OAuth Error Messages ──────────────────────────
const oauthErrorMessages = {
  email_not_found: 'Google did not provide an email. Please try again.',
  authentication_failed: 'Google login failed. Please try again.',
  email_exists_local: 'This email is registered with a password. Please log in manually.',
  no_token: 'Session expired. Please log in again.',
  access_denied: 'You denied access to your Google account.',
  session_expired: 'Your session has expired. Please log in again.',
}

// ─── Computed ──────────────────────────────────────
const isFormValid = computed(() => {
  return form.email.trim() !== '' && form.password.length >= 6
})

const emailError = computed(() => {
  if (!form.email) return ''
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(form.email) ? '' : 'Please enter a valid email'
})

// ─── Lifecycle ─────────────────────────────────────
onMounted(() => {
  // Handle OAuth error from URL
  const errorCode = route.query.error
  if (errorCode && oauthErrorMessages[errorCode]) {
    errorMsg.value = oauthErrorMessages[errorCode]
    router.replace({ query: {} })
  }

  // Handle success message (after registration — legacy flow)
  if (route.query.registered === 'true') {
    successMsg.value = 'Account created successfully! Please log in.'
  }

  // Handle "verified" message (after OTP verification)
  if (route.query.verified === 'true') {
    successMsg.value = 'Email verified successfully! Please log in.'
    router.replace({ query: {} })
  }

  // Auto-fill remembered email
  const rememberedEmail = localStorage.getItem('rememberedEmail')
  if (rememberedEmail) {
    form.email = rememberedEmail
    form.rememberMe = true
  }
})

// ─── Handlers ──────────────────────────────────────
async function handleSubmit() {
  errorMsg.value = ''
  successMsg.value = ''
  isGoogleAccountError.value = false   // ✅ Reset
  loading.value = true

  try {
    const response = await userService.login({
      email: form.email.trim().toLowerCase(),
      password: form.password,
    })

    if (!response?.token) {
      throw new Error('No token received from server')
    }

    // Save auth (flat response: { token, id, name, username, email, ... })
    authStore.setAuth(response.token, response)

    // Remember email preference
    if (form.rememberMe) {
      localStorage.setItem('rememberedEmail', form.email)
    } else {
      localStorage.removeItem('rememberedEmail')
    }

    // Redirect to intended page or dashboard
    const redirect = route.query.redirect || '/'
    await router.push(redirect)
  } catch (err) {
    console.error('Login error:', err)

    // Extract backend message (could be string or object)
    const rawMessage =
      err.response?.data?.message ||
      err.response?.data ||
      err.message ||
      ''

    const messageStr = typeof rawMessage === 'string' ? rawMessage : ''

    // ✅ If email is not verified, redirect to OTP verification page
    if (messageStr.includes('EMAIL_NOT_VERIFIED')) {
      router.push({
        path: '/verify-otp',
        query: { email: form.email.trim().toLowerCase() },
      })
      return
    }

    // ✅ If the account uses Google sign-in, show a helpful message
    if (messageStr.toLowerCase().includes('google sign-in') ||
        messageStr.toLowerCase().includes('google login') ||
        messageStr.toLowerCase().includes('uses google')) {
      isGoogleAccountError.value = true
      errorMsg.value = 'This account was created with Google. Please use "Continue with Google" below.'
      return
    }

    // Otherwise, show the error normally
    errorMsg.value = messageStr || 'Login failed. Please try again.'
  } finally {
    loading.value = false
  }
}

function handleGoogleLogin() {
  errorMsg.value = ''
  isGoogleAccountError.value = false
  googleLoading.value = true
  window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
}

function togglePassword() {
  showPassword.value = !showPassword.value
}

function clearError() {
  errorMsg.value = ''
  isGoogleAccountError.value = false
}
</script>

<template>
  <div class="auth-card fade-in-up">
    <!-- ─── Header ───────────────────────────────── -->
    <div class="text-center mb-4">
      <div class="brand-icon-wrapper">
        <i class="bi bi-wallet2" style="color: var(--color-accent-orange)"></i>
      </div>
      <h2 class="font-brand mt-2 mb-0" style="font-size: clamp(1.6rem, 1.3rem + 1vw, 2.1rem)">
        Welcome back
      </h2>
      <p class="text-muted-soft mb-0">Log in to keep tracking your expenses.</p>
    </div>

    <!-- ─── Success Alert ──────────────────────────── -->
    <transition name="fade">
      <div
        v-if="successMsg"
        class="alert alert-success py-2 small d-flex align-items-center"
      >
        <i class="bi bi-check-circle-fill me-2"></i>
        {{ successMsg }}
      </div>
    </transition>

    <!-- ─── Google-Account Error (special style) ───── -->
    <transition name="fade">
      <div
        v-if="isGoogleAccountError"
        class="alert alert-warning py-2 small"
        style="border-left: 4px solid #f59e0b"
      >
        <div class="d-flex align-items-start">
          <i class="bi bi-google me-2 mt-1" style="color:#4285F4"></i>
          <div>
            <strong>This account uses Google sign-in.</strong>
            <p class="mb-0 mt-1 small">
              Click <strong>"Continue with Google"</strong> below to sign in.
            </p>
          </div>
        </div>
      </div>
    </transition>

    <!-- ─── Generic Error ──────────────────────────── -->
    <transition name="fade">
      <div
        v-if="errorMsg && !isGoogleAccountError"
        class="alert alert-danger py-2 small d-flex align-items-center justify-content-between"
      >
        <span>
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          {{ errorMsg }}
        </span>
        <button
          type="button"
          class="btn-close btn-close-sm"
          aria-label="Close"
          @click="clearError"
        ></button>
      </div>
    </transition>

    <!-- ─── Login Form ───────────────────────────── -->
    <form @submit.prevent="handleSubmit" novalidate>
      <!-- Email -->
      <div class="mb-3">
        <label for="email" class="form-label fw-semibold">Email</label>
        <div class="input-group">
          <span class="input-group-text bg-white">
            <i class="bi bi-envelope text-muted"></i>
          </span>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="form-control"
            :class="{ 'is-invalid': emailError && form.email }"
            required
            placeholder="you@example.com"
            autocomplete="email"
            :disabled="loading"
          />
        </div>
        <small v-if="emailError && form.email" class="text-danger">
          {{ emailError }}
        </small>
      </div>

      <!-- Password -->
      <div class="mb-3">
        <label for="password" class="form-label fw-semibold">Password</label>
        <div class="input-group">
          <span class="input-group-text bg-white">
            <i class="bi bi-lock text-muted"></i>
          </span>
          <input
            id="password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            class="form-control"
            required
            placeholder="••••••••"
            autocomplete="current-password"
            :disabled="loading"
          />
          <button
            type="button"
            class="btn btn-outline-secondary"
            :disabled="loading"
            @click="togglePassword"
          >
            <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>
      </div>

      <!-- Remember + Forgot -->
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div class="form-check">
          <input
            id="rememberMe"
            v-model="form.rememberMe"
            type="checkbox"
            class="form-check-input"
            :disabled="loading"
          />
          <label for="rememberMe" class="form-check-label small">
            Remember me
          </label>
        </div>
        <router-link
          to="/forgot-password"
          class="small text-decoration-none fw-semibold"
          style="color: var(--color-accent-orange)"
        >
          Forgot password?
        </router-link>
      </div>

      <!-- Submit -->
      <button
        type="submit"
        class="btn btn-expenzo w-100"
        :disabled="loading || !isFormValid"
      >
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        {{ loading ? 'Logging in...' : 'Log In' }}
      </button>
    </form>

    <!-- ─── OR Divider ───────────────────────────── -->
    <div class="divider-or my-4">
      <span>OR</span>
    </div>

    <!-- ─── Google Login ─────────────────────────── -->
    <button
      type="button"
      class="btn btn-google w-100"
      :class="{ 'pulse-highlight': isGoogleAccountError }"
      :disabled="googleLoading || loading"
      @click="handleGoogleLogin"
    >
      <span v-if="googleLoading" class="spinner-border spinner-border-sm me-2"></span>
      <svg v-else class="google-icon me-2" viewBox="0 0 24 24" width="18" height="18">
        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
      </svg>
      {{ googleLoading ? 'Redirecting to Google...' : 'Continue with Google' }}
    </button>

    <!-- ─── Footer ───────────────────────────────── -->
    <p class="text-center text-muted-soft mt-4 mb-0">
      New to Expenzo?
      <router-link
        to="/register"
        class="fw-semibold text-decoration-none"
        style="color: var(--color-accent-orange)"
      >
        Create an account
      </router-link>
    </p>
  </div>
</template>

<style scoped>
/* ─── Brand Icon ─────────────────────────────────── */
.brand-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: rgba(255, 122, 0, 0.1);
  border-radius: 50%;
  font-size: 1.8rem;
}

/* ─── OR Divider ─────────────────────────────────── */
.divider-or {
  display: flex;
  align-items: center;
  text-align: center;
  color: #999;
  font-size: 0.85rem;
  font-weight: 600;
  letter-spacing: 1px;
}

.divider-or::before,
.divider-or::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e0e0e0;
}

.divider-or span {
  padding: 0 12px;
}

/* ─── Google Button ──────────────────────────────── */
.btn-google {
  background-color: #ffffff;
  border: 1px solid #dadce0;
  color: #3c4043;
  font-weight: 500;
  padding: 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.btn-google:hover:not(:disabled) {
  background-color: #f8f9fa;
  border-color: #c6c9cd;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.btn-google:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.btn-google:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.google-icon {
  flex-shrink: 0;
}

/* ✅ New: pulse animation when Google is the correct option */
@keyframes pulseHighlight {
  0%   { box-shadow: 0 0 0 0 rgba(66, 133, 244, 0.6); }
  70%  { box-shadow: 0 0 0 12px rgba(66, 133, 244, 0); }
  100% { box-shadow: 0 0 0 0 rgba(66, 133, 244, 0); }
}

.pulse-highlight {
  animation: pulseHighlight 1.5s ease-in-out 2;
  border-color: #4285F4;
  border-width: 2px;
}

/* ─── Input Group Enhancements ───────────────────── */
.input-group-text {
  border-right: none;
}

.input-group .form-control {
  border-left: none;
}

.input-group .form-control:focus {
  box-shadow: none;
  border-color: #dee2e6;
}

.input-group:focus-within {
  box-shadow: 0 0 0 0.25rem rgba(255, 122, 0, 0.15);
  border-radius: 6px;
}

/* ─── Fade Transitions ───────────────────────────── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ─── Button Close ───────────────────────────────── */
.btn-close-sm {
  font-size: 0.7rem;
  padding: 0.25rem;
}

/* ─── Form Check Enhancement ─────────────────────── */
.form-check-input:checked {
  background-color: var(--color-accent-orange);
  border-color: var(--color-accent-orange);
}

.form-check-input:focus {
  box-shadow: 0 0 0 0.25rem rgba(255, 122, 0, 0.25);
  border-color: var(--color-accent-orange);
}
</style>
