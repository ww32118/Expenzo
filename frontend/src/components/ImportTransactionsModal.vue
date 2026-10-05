<script setup>
import { ref, computed } from 'vue'
import transactionService from '@/services/transactionService'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'imported'])

const file = ref(null)
const uploading = ref(false)
const result = ref(null)
const errorMsg = ref('')

const accept = computed(() =>
  '.csv,.xlsx,.xls,.pdf,application/pdf,application/vnd.ms-excel,' +
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/csv'
)

function close() {
  if (uploading.value) return
  emit('update:modelValue', false)
  setTimeout(() => {
    file.value = null
    result.value = null
    errorMsg.value = ''
  }, 300)
}

function onFileChange(e) {
  file.value = e.target.files?.[0] || null
  result.value = null
  errorMsg.value = ''
}

async function handleUpload() {
  if (!file.value) return
  uploading.value = true
  errorMsg.value = ''
  result.value = null
  try {
    const res = await transactionService.importFile(file.value)
    result.value = res
    emit('imported', res)
  } catch (err) {
    errorMsg.value =
      err.response?.data?.message || err.message || 'Import failed'
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <transition name="fade">
    <div v-if="modelValue" class="modal-backdrop" @click.self="close">
      <div class="modal-card">
        <h4 class="mb-1">Import Transactions</h4>
        <p class="text-muted-soft small mb-3">
          Upload a <strong>CSV</strong>, <strong>Excel</strong>, or <strong>PDF</strong> file.
        </p>

        <!-- Upload form -->
        <div v-if="!result" class="mb-3">
          <input
            type="file"
            :accept="accept"
            class="form-control"
            :disabled="uploading"
            @change="onFileChange"
          />
          <small class="text-muted-soft d-block mt-2">
            Expected columns: <code>amount, description, date, type, categoryId, bankAccountId</code>
          </small>
        </div>

        <!-- Error alert -->
        <div v-if="errorMsg" class="alert alert-danger py-2 small">
          <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ errorMsg }}
        </div>

        <!-- Result summary -->
        <div v-if="result" class="alert alert-success py-2 small mb-3">
          <strong>{{ result.imported }}</strong> imported,
          <strong>{{ result.failed }}</strong> failed
          (out of {{ result.total }}).
        </div>

        <div v-if="result?.errors?.length" class="mb-3">
          <details>
            <summary class="small text-muted" style="cursor: pointer">
              Show failed rows ({{ result.errors.length }})
            </summary>
            <ul class="small text-danger mt-2" style="max-height: 150px; overflow: auto">
              <li v-for="(e, i) in result.errors" :key="i">{{ e }}</li>
            </ul>
          </details>
        </div>

        <!-- Actions -->
        <div class="d-flex gap-2">
          <button
            v-if="!result"
            type="button"
            class="btn btn-outline-secondary flex-fill"
            :disabled="uploading"
            @click="close"
          >
            Cancel
          </button>
          <button
            v-if="!result"
            type="button"
            class="btn btn-expenzo flex-fill"
            :disabled="!file || uploading"
            @click="handleUpload"
          >
            <span v-if="uploading" class="spinner-border spinner-border-sm me-2"></span>
            {{ uploading ? 'Uploading...' : 'Import' }}
          </button>
          <button
            v-if="result"
            type="button"
            class="btn btn-expenzo flex-fill"
            @click="close"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 1rem;
}

.modal-card {
  background: white;
  padding: 1.75rem;
  border-radius: 12px;
  max-width: 480px;
  width: 100%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
