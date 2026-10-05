<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()

const email = ref(route.query.email || '')
const digits = ref(['', '', '', '', '', ''])
const loading = ref(false)
const resending = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const cooldown = ref(0)

let cooldownTimer = null

// ─── Auto-focus & input handling ──────────────────────────────
function handleInput(index, event) {
  const val = event.target.value.replace(/\D/g, '')
  digits.value[index] = val.slice(-1)
  if (val && index < 5) {
    const next = document.querySelector(`#otp-${index + 1}`)
    if (next) next.focus()
  }
}

function handleKeydown(index, event) {
  if (event.key === 'Backspace' && !digits.value[index] && index > 0) {
    const prev = document.querySelector(`#otp-${index - 1}`)
    if (prev) prev.focus()
  }
}

function handlePaste(event) {
  const text = event.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
  for (let i = 0; i < 6; i++) {
    digits.value[i] = text[i] || ''
  }
  const focusIdx = Math.min(text.length, 5)
  const el = document.querySelector(`#otp-${focusIdx}`)
  if (el) el.focus()
  event.preventDefault()
}

const otpFull = computed(() => digits.value.join(''))
const isComplete = computed(() => otpFull.value.length === 6)

// ─── Verify OTP ───────────────────────────────────────────────
async function verify() {
  if (!isComplete.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    await api.post('/users/verify-otp', {
      email: email.value,
      otp: otpFull.value
    })
    successMsg.value = 'Email verified! Redirecting to login...'
    setTimeout(() => router.push('/login'), 2000)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.response?.data || 'Invalid OTP. Try again.'
    digits.value = ['', '', '', '', '', '']
    document.querySelector('#otp-0')?.focus()
  } finally {
    loading.value = false
  }
}

// ─── Resend OTP ───────────────────────────────────────────────
async function resend() {
  if (cooldown.value > 0) return
  resending.value = true
  errorMsg.value = ''
  try {
    await api.post('/users/resend-otp', { email: email.value })
    successMsg.value = 'New OTP sent to your email!'
    startCooldown()
    setTimeout(() => { successMsg.value = '' }, 4000)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to resend OTP'
  } finally {
    resending.value = false
  }
}

function startCooldown() {
  cooldown.value = 60
  cooldownTimer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) clearInterval(cooldownTimer)
  }, 1000)
}

onMounted(() => {
  document.querySelector('#otp-0')?.focus()
})
</script>

<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center" style="background:var(--color-bg-base)">
    <div class="surface-card p-4 p-md-5" style="max-width:440px;width:100%;border-radius:var(--radius-lg)">
      <!-- Header -->
      <div class="text-center mb-4">
        <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
             style="width:56px;height:56px;background:linear-gradient(135deg,#6366f1,#8b5cf6)">
          <i class="bi bi-envelope-check" style="color:#fff;font-size:1.5rem"></i>
        </div>
        <h3 class="font-brand fw-bold mb-1">Verify Your Email</h3>
        <p class="text-muted-soft small mb-0">
          We sent a 6-digit code to <strong>{{ email }}</strong>
        </p>
      </div>

      <!-- Error / Success messages -->
      <div v-if="errorMsg"
           class="alert mb-3 py-2 px-3"
           style="background:#fef2f2;border:1px solid #fca5a5;border-radius:var(--radius-md);color:#dc2626;font-size:0.875rem">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <div v-if="successMsg"
           class="alert mb-3 py-2 px-3"
           style="background:#f0fdf4;border:1px solid #86efac;border-radius:var(--radius-md);color:#16a34a;font-size:0.875rem">
        <i class="bi bi-check-circle me-1"></i>{{ successMsg }}
      </div>

      <!-- OTP Boxes -->
      <div class="d-flex justify-content-center gap-2 mb-4" @paste="handlePaste">
        <input
          v-for="(d, i) in digits"
          :key="i"
          :id="`otp-${i}`"
          type="text"
          inputmode="numeric"
          maxlength="1"
          :value="d"
          @input="handleInput(i, $event)"
          @keydown="handleKeydown(i, $event)"
          class="form-control text-center fw-bold"
          style="width:48px;height:56px;font-size:1.35rem;border-radius:var(--radius-md);border:2px solid var(--color-border);transition:border-color 0.2s"
          :style="d ? 'border-color:#6366f1' : ''"
        />
      </div>

      <!-- Verify Button -->
      <button
        class="btn btn-expenzo w-100 mb-3"
        :disabled="!isComplete || loading"
        @click="verify"
      >
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        {{ loading ? 'Verifying...' : 'Verify Email' }}
      </button>

      <!-- Resend -->
      <div class="text-center">
        <p class="text-muted-soft small mb-1">Didn't receive the code?</p>
        <button
          class="btn btn-link p-0 small"
          :disabled="cooldown > 0 || resending"
          @click="resend"
          style="color:var(--color-primary);text-decoration:none"
        >
          <span v-if="resending" class="spinner-border spinner-border-sm me-1"></span>
          {{ cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend OTP' }}
        </button>
      </div>

      <!-- Timer info -->
      <p class="text-center text-muted-soft mt-3 mb-0" style="font-size:0.75rem">
        <i class="bi bi-clock me-1"></i>Code expires in 10 minutes
      </p>
    </div>
  </div>
</template>
