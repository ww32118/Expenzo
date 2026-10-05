<script setup>
import { ref, onErrorCaptured } from 'vue'
import { logger } from '@/utils/logger'

const hasError = ref(false)
const error = ref(null)
const showDetails = ref(false)

onErrorCaptured((err, instance, info) => {
  logger.error('🔴 ErrorBoundary caught:', err)
  logger.error('   Component:', instance)
  logger.error('   Info:', info)

  hasError.value = true
  error.value = err

  // Return false to stop the error from propagating further up
  return false
})

function reload() {
  window.location.reload()
}

function goHome() {
  window.location.href = '/'
}
</script>

<template>
  <!-- Happy path: render children normally -->
  <slot v-if="!hasError" />

  <!-- Error fallback -->
  <div v-else class="error-boundary d-flex align-items-center justify-content-center min-vh-100">
    <div class="text-center p-4" style="max-width: 500px">

      <div class="mb-4">
        <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 3.5rem"></i>
      </div>

      <h3 class="font-brand fw-bold mb-2">Oops — something broke</h3>
      <p class="text-muted-soft mb-4">
        An unexpected error occurred. Try one of the options below, or reload
        the page. If this keeps happening, please log out and log back in.
      </p>

      <div class="d-flex flex-column flex-sm-row gap-2 justify-content-center mb-4">
        <button class="btn btn-expenzo" @click="reload">
          <i class="bi bi-arrow-clockwise me-2"></i>
          Reload Page
        </button>
        <button class="btn btn-outline-secondary" @click="goHome">
          <i class="bi bi-house me-2"></i>
          Go Home
        </button>
      </div>

      <!-- Details toggle (only in dev) -->
      <template v-if="isDev && error">
        <button
          class="btn btn-link btn-sm text-muted p-0 text-decoration-none"
          @click="showDetails = !showDetails"
        >
          {{ showDetails ? '▾ Hide' : '▸ Show' }} error details
        </button>

        <pre
          v-if="showDetails"
          class="text-start small mt-3 p-3"
          style="background: #fef2f2; border: 1px solid #fca5a5; border-radius: 8px; color: #991b1b; white-space: pre-wrap; max-height: 300px; overflow: auto;"
        >{{ error.message }}

{{ error.stack }}</pre>
      </template>

    </div>
  </div>
</template>

<script>
// Compute isDev in a separate plain script block to keep it reactive-safe
export default {
  computed: {
    isDev() {
      return import.meta.env.DEV
    },
  },
}
</script>

<style scoped>
.error-boundary {
  background: var(--color-bg-base, #f5f5f5);
  padding: 1rem;
}
</style>