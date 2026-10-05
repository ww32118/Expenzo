<script setup>
import { ref } from 'vue'
import exportService from '@/services/exportService'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loadingCsv = ref(false)
const loadingPdf = ref(false)
const errorMsg = ref('')

async function handleCsv() {
  loadingCsv.value = true
  errorMsg.value = ''
  try {
    await exportService.downloadCsv(authStore.userId)
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    loadingCsv.value = false
  }
}

async function handlePdf() {
  loadingPdf.value = true
  errorMsg.value = ''
  try {
    await exportService.downloadPdf(authStore.userId)
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    loadingPdf.value = false
  }
}
</script>

<template>
  <div>
    <div class="mb-4 fade-in-up">
      <h1 class="font-brand page-title mb-1">Export</h1>
      <p class="text-muted-soft mb-0">Download your transaction history.</p>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">{{ errorMsg }}</div>

    <div class="row g-3 fade-in-up stagger-2">
      <div class="col-md-6">
        <div class="surface-card surface-card--interactive p-4 text-center">
          <i class="bi bi-filetype-csv fs-1" style="color: var(--color-primary)"></i>
          <h5 class="font-display fw-semibold mt-3">CSV Export</h5>
          <p class="text-muted-soft small">Spreadsheet-friendly format for all your transactions.</p>
          <button class="btn btn-expenzo-outline" :disabled="loadingCsv" @click="handleCsv">
            <span v-if="loadingCsv" class="spinner-border spinner-border-sm me-2"></span>
            Download CSV
          </button>
        </div>
      </div>
      <div class="col-md-6">
        <div class="surface-card surface-card--interactive p-4 text-center">
          <i class="bi bi-filetype-pdf fs-1" style="color: var(--color-accent-flare)"></i>
          <h5 class="font-display fw-semibold mt-3">PDF Export</h5>
          <p class="text-muted-soft small">A shareable, printable statement of your transactions.</p>
          <button class="btn btn-expenzo" :disabled="loadingPdf" @click="handlePdf">
            <span v-if="loadingPdf" class="spinner-border spinner-border-sm me-2"></span>
            Download PDF
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
