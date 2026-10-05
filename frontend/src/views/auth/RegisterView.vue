<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import userService from '@/services/userService'

const router = useRouter()

// ─── State ─────────────────────────────────────────
const form = reactive({
  name: '',
  username: '',
  email: '',
  phoneNumber: '',
  password: '',
  agreeTerms: false,
})

const loading = ref(false)
const googleLoading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)
const touched = reactive({
  name: false,
  username: false,
  email: false,
  phoneNumber: false,
  password: false,
})

// ─── Config ────────────────────────────────────────
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// ─── Validation Rules ──────────────────────────────
const errors = computed(() => ({
  name: form.name.trim().length < 2 ? 'Name must be at least 2 characters' : '',
  username: !/^[a-zA-Z0-9_]{3,20}$/.test(form.username)
    ? 'Username: 3-20 chars, letters/numbers/underscore only'
    : '',
  email: !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
    ? 'Please enter a valid email'
    : '',
  phoneNumber: form.phoneNumber && !/^\+?[\d\s-()]{7,15}$/.test(form.phoneNumber)
    ? 'Please enter a valid phone number'
    : '',
  password: form.password.length < 6
    ? 'Password must be at least 6 characters'
    : '',
}))

const isFormValid = computed(() => {
  return (
    !errors.value.name &&
    !errors.value.username &&
    !errors.value.email &&
    !errors.value.phoneNumber &&
    !errors.value.password &&
    form.agreeTerms
  )
})

// ─── Password Strength ─────────────────────────────
const passwordStrength = computed(() => {
  const pass = form.password
  if (!pass) return { score: 0, text: '', color: '', width: '0%' }

  let score = 0
  if (pass.length >= 6) score++
  if (pass.length >= 10) score++
  if (/[A-Z]/.test(pass) && /[a-z]/.test(pass)) score++
  if (/[0-9]/.test(pass)) score++
  if (/[^A-Za-z0-9]/.test(pass)) score++

  const levels = [
    { text: 'Too weak', color: 'bg-danger', width: '20%' },
    { text: 'Weak', color: 'bg-danger', width: '40%' },
    { text: 'Fair', color: 'bg-warning', width: '60%' },
    { text: 'Good', color: 'bg-info', width: '80%' },
    { text: 'Strong', color: 'bg-success', width: '100%' },
  ]

  return { score, ...(levels[Math.min(score - 1, 4)] || levels[0]) }
})

// ─── Auto-suggest username from email ──────────────
function autoFillUsername() {
  if (form.email && !form.username && !touched.username) {
    const suggested = form.email.split('@')[0].replace(/[^a-zA-Z0-9_]/g, '')
    if (suggested.length >= 3) {
      form.username = suggested
    }
  }
}

// ─── Handlers ──────────────────────────────────────
async function handleSubmit() {
  // Mark all fields as touched to show errors
  Object.keys(touched).forEach(key => (touched[key] = true))
  errorMsg.value = ''

  if (!isFormValid.value) {
    errorMsg.value = 'Please fix the errors above'
    return
  }

  loading.value = true
  try {
    const email = form.email.trim().toLowerCase()

    await userService.register({
      ...form,
      email,
      username: form.username.trim(),
      image: '',
    })

    // ✅ Do NOT call authStore.setUser() — user is not authenticated yet.
    // ✅ Redirect to OTP verification page with email pre-filled.
    router.push({
      path: '/verify-otp',
      query: { email },
    })
  } catch (err) {
    errorMsg.value = err.message || 'Registration failed. Please try again.'
  } finally {
    loading.value = false
  }
}

function handleGoogleSignup() {
  errorMsg.value = ''
  googleLoading.value = true
  window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
}

function togglePassword() {
  showPassword.value = !showPassword.value
}

function clearError() {
  errorMsg.value = ''
}

function markTouched(field) {
  touched[field] = true
}
</script>

<template>
  <div class="auth-card fade-in-up">
    <!-- ─── Header ───────────────────────────────── -->
    <div class="text-center mb-4">
      <div class="brand-icon-wrapper">
        <i class="bi bi-person-plus-fill" style="color: var(--color-accent-orange)"></i>
      </div>
      <h2 class="font-brand mt-2 mb-0" style="font-size: clamp(1.5rem, 1.2rem + 1vw, 2rem)">
        Create your account
      </h2>
      <p class="text-muted-soft mb-0">Start budgeting smarter today.</p>
    </div>

    <!-- ─── Error Alert ──────────────────────────── -->
    <transition name="fade">
      <div
        v-if="errorMsg"
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

    <!-- ─── Registration Form ────────────────────── -->
    <form @submit.prevent="handleSubmit" novalidate>
      <!-- Full Name -->
      <div class="mb-3">
        <label for="name" class="form-label fw-semibold">Full name</label>
        <div class="input-group">
          <span class="input-group-text bg-white">
            <i class="bi bi-person text-muted"></i>
          </span>
          <input
            id="name"
            v-model="form.name"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': touched.name && errors.name }"
            required
            placeholder="John Doe"
            autocomplete="name"
            :disabled="loading"
            @blur="markTouched('name')"
          />
        </div>
        <small v-if="touched.name && errors.name" class="text-danger">
          {{ errors.name }}
        </small>
      </div>

      <!-- Username -->
      <div class="mb-3">
        <label for="username" class="form-label fw-semibold">Username</label>
        <div class="input-group">
          <span class="input-group-text bg-white">
            <i class="bi bi-at text-muted"></i>
          </span>
          <input
            id="username"
            v-model="form.username"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': touched.username && errors.username }"
            required
            placeholder="johndoe"
            autocomplete="username"
            :disabled="loading"
            @blur="markTouched('username')"
          />
        </div>
        <small v-if="touched.username && errors.username" class="text-danger">
          {{ errors.username }}
        </small>
      </div>

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
            :class="{ 'is-invalid': touched.email && errors.email }"
            required
            placeholder="you@example.com"
            autocomplete="email"
            :disabled="loading"
            @blur="markTouched('email'); autoFillUsername()"
          />
        </div>
        <small v-if="touched.email && errors.email" class="text-danger">
          {{ errors.email }}
        </small>
      </div>

      <!-- Phone Number -->
      <div class="mb-3">
        <label for="phone" class="form-label fw-semibold">
          Phone number <span class="text-muted-soft small">(optional)</span>
        </label>
        <div class="input-group">
          <span class="input-group-text bg-white">
            <i class="bi bi-telephone text-muted"></i>
          </span>
          <input
            id="phone"
            v-model="form.phoneNumber"
            type="tel"
            class="form-control"
            :class="{ 'is-invalid': touched.phoneNumber && errors.phoneNumber }"
            placeholder="+91 98765 43210"
            autocomplete="tel"
            :disabled="loading"
            @blur="markTouched('phoneNumber')"
          />
        </div>
        <small v-if="touched.phoneNumber && errors.phoneNumber" class="text-danger">
          {{ errors.phoneNumber }}
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
            :class="{ 'is-invalid': touched.password && errors.password }"
            required
            placeholder="••••••••"
            autocomplete="new-password"
            :disabled="loading"
            @blur="markTouched('password')"
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

        <!-- Password Strength Meter -->
        <div v-if="form.password" class="password-strength mt-2">
          <div class="progress" style="height: 4px">
            <div
              class="progress-bar"
              :class="passwordStrength.color"
              :style="{ width: passwordStrength.width, transition: 'all 0.3s ease' }"
            ></div>
          </div>
          <small class="d-flex justify-content-between mt-1">
            <span class="text-muted-soft">Password strength:</span>
            <span
              :class="{
                'text-danger': passwordStrength.score <= 2,
                'text-warning': passwordStrength.score === 3,
                'text-info': passwordStrength.score === 4,
                'text-success': passwordStrength.score === 5,
              }"
              class="fw-semibold"
            >
              {{ passwordStrength.text }}
            </span>
          </small>
        </div>

        <small v-if="touched.password && errors.password" class="text-danger">
          {{ errors.password }}
        </small>
      </div>

      <!-- Terms & Conditions -->
      <div class="mb-4">
        <div class="form-check">
          <input
            id="agreeTerms"
            v-model="form.agreeTerms"
            type="checkbox"
            class="form-check-input"
            :disabled="loading"
          />
          <label for="agreeTerms" class="form-check-label small">
            I agree to the
            <a href="#" class="fw-semibold" style="color: var(--color-accent-orange)">
              Terms of Service
            </a>
            &
            <a href="#" class="fw-semibold" style="color: var(--color-accent-orange)">
              Privacy Policy
            </a>
          </label>
        </div>
      </div>

      <!-- Submit Button -->
      <button
        type="submit"
        class="btn btn-expenzo w-100"
        :disabled="loading || !isFormValid"
      >
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        {{ loading ? 'Creating account...' : 'Create Account' }}
      </button>
    </form>

    <!-- ─── OR Divider ───────────────────────────── -->
    <div class="divider-or my-4">
      <span>OR</span>
    </div>

    <!-- ─── Google Signup Button ─────────────────── -->
    <button
      type="button"
      class="btn btn-google w-100"
      :disabled="googleLoading || loading"
      @click="handleGoogleSignup"
    >
      <span v-if="googleLoading" class="spinner-border spinner-border-sm me-2"></span>
      <svg v-else class="google-icon me-2" viewBox="0 0 24 24" width="18" height="18">
        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
      </svg>
      {{ googleLoading ? 'Redirecting to Google...' : 'Sign up with Google' }}
    </button>

    <!-- ─── Footer ───────────────────────────────── -->
    <p class="text-center text-muted-soft mt-4 mb-0">
      Already have an account?
      <router-link
        to="/login"
        class="fw-semibold text-decoration-none"
        style="color: var(--color-accent-orange)"
      >
        Log in
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
}

.btn-google:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.google-icon {
  flex-shrink: 0;
}

/* ─── Input Groups ───────────────────────────────── */
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

.form-control.is-invalid {
  border-color: #dc3545;
}

/* ─── Password Strength ──────────────────────────── */
.password-strength .progress {
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.password-strength .progress-bar {
  transition: width 0.3s ease, background-color 0.3s ease;
}

/* ─── Form Check ─────────────────────────────────── */
.form-check-input:checked {
  background-color: var(--color-accent-orange);
  border-color: var(--color-accent-orange);
}

.form-check-input:focus {
  box-shadow: 0 0 0 0.25rem rgba(255, 122, 0, 0.25);
  border-color: var(--color-accent-orange);
}

/* ─── Transitions ────────────────────────────────── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.btn-close-sm {
  font-size: 0.7rem;
  padding: 0.25rem;
}
</style>