<script setup>
import { ref } from 'vue'

defineProps({
  label: {
    type: String,
    default: 'Continue with Google'
  }
})

const loading = ref(false)

function handleClick() {
  loading.value = true
  // ✅ MUST use window.location - NEVER axios/fetch
  window.location.href = 'http://localhost:8080/oauth2/authorization/google'
}
</script>

<template>
  <button
    type="button"
    class="btn btn-google w-100"
    :disabled="loading"
    @click="handleClick"
  >
    <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
    <i v-else class="bi bi-google me-2" style="color: #ea4335"></i>
    {{ loading ? 'Redirecting to Google...' : label }}
  </button>
</template>

<style scoped>
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
</style>