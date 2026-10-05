<script setup>
import { ref, reactive, onMounted } from 'vue'
import transactionService from '@/services/transactionService'
import categoryService from '@/services/categoryService'
import bankAccountService from '@/services/bankAccountService'
import { useAuthStore } from '@/stores/auth'
import { logger } from '@/utils/logger'
import AppModal from '@/components/AppModal.vue'
import ImportTransactionsModal from '@/components/ImportTransactionsModal.vue'

const authStore = useAuthStore()

// ─── State ──────────────────────────────────────────────
const transactions = ref([])
const categories = ref([])
const bankAccounts = ref([])
const loading = ref(true)
const errorMsg = ref('')
const saving = ref(false)

const showModal = ref(false)
const showImportModal = ref(false)              // ✅ new
const editingId = ref(null)
const deletingId = ref(null)                     // ✅ new

const form = reactive({
  amount: '',
  currency: 'INR',
  description: '',
  date: new Date().toISOString().slice(0, 16),
  categoryId: '',
  bankAccountId: '',
  transactionType: 'DEBIT',
  isRecurring: false,
  recurrenceDay: null,
})

// ─── Helpers ────────────────────────────────────────────
function extractErrorMessage(err, fallback = 'Something went wrong') {
  const raw =
    err?.response?.data?.message ??
    err?.response?.data ??
    err?.message ??
    fallback
  return typeof raw === 'string' ? raw : fallback
}

function resetForm() {
  editingId.value = null
  form.amount = ''
  form.currency = 'INR'
  form.description = ''
  form.date = new Date().toISOString().slice(0, 16)
  form.categoryId = categories.value[0]?.id || ''
  form.bankAccountId = bankAccounts.value[0]?.id || ''
  form.transactionType = 'DEBIT'
  form.isRecurring = false
  form.recurrenceDay = null
}

function openAddModal() {
  resetForm()
  errorMsg.value = ''
  showModal.value = true
}

function openEditModal(t) {
  editingId.value = t.id
  form.amount = t.amount
  form.currency = t.currency
  form.description = t.description
  form.date = t.date ? String(t.date).slice(0, 16) : new Date().toISOString().slice(0, 16)
  form.categoryId = t.categoryId || categories.value.find((c) => c.name === t.categoryName)?.id || ''
  form.bankAccountId = t.bankAccountId || ''
  form.transactionType = t.transactionType || 'DEBIT'
  form.isRecurring = t.recurring || false
  form.recurrenceDay = t.recurrenceDay || null
  errorMsg.value = ''
  showModal.value = true
}

// ─── Load all ───────────────────────────────────────────
async function loadAll() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [tx, cat, banks] = await Promise.all([
      transactionService.getAll(),
      categoryService.getAllCategories(),
      bankAccountService.getAll(),
    ])

    const currentUserId = authStore.userId

    categories.value = (cat || []).filter((c) => c.userId === currentUserId)
    bankAccounts.value = (banks || []).filter((b) => b.userId === currentUserId)
    transactions.value = (tx || []).filter((t) => t.userId === currentUserId)

    logger.log(`📊 Loaded ${transactions.value.length} transactions`)
  } catch (err) {
    if (err.code === 'ERR_CANCELED') return
    logger.error('❌ Load transactions failed:', err)
    errorMsg.value = extractErrorMessage(err, 'Failed to load transactions')
  } finally {
    loading.value = false
  }
}

// ─── Save (add / update) ────────────────────────────────
async function handleSubmit() {
  saving.value = true
  errorMsg.value = ''

  const payload = {
    amount: Number(form.amount),
    currency: form.currency,
    description: form.description,
    date: form.date,
    userId: authStore.userId,
    categoryId: Number(form.categoryId),
    bankAccountId: Number(form.bankAccountId),
    transactionType: form.transactionType,
    recurring: form.isRecurring,
    recurrenceDay: form.isRecurring ? Number(form.recurrenceDay) : null,
  }

  try {
    if (editingId.value) {
      await transactionService.update(editingId.value, payload)
    } else {
      await transactionService.add(payload)
    }
    showModal.value = false
    await loadAll()
  } catch (err) {
    if (err.code === 'ERR_CANCELED') return
    logger.error('❌ Save transaction failed:', err)
    errorMsg.value = extractErrorMessage(err, 'Failed to save transaction')
  } finally {
    saving.value = false
  }
}

// ─── Delete ─────────────────────────────────────────────
async function handleDelete(id) {
  if (deletingId.value) return
  if (!confirm('Delete this transaction?')) return

  deletingId.value = id
  errorMsg.value = ''
  try {
    await transactionService.remove(id)
    transactions.value = transactions.value.filter((t) => t.id !== id)
  } catch (err) {
    if (err.code === 'ERR_CANCELED') return
    logger.error('❌ Delete transaction failed:', err)
    errorMsg.value = extractErrorMessage(err, 'Failed to delete transaction')
  } finally {
    deletingId.value = null
  }
}

// ─── After import ───────────────────────────────────────
async function handleImported(result) {
  logger.log('✅ Import completed:', result)
  await loadAll()
}

// ─── Formatting ─────────────────────────────────────────
function formatCurrency(value, currency = 'INR') {
  try {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency }).format(value || 0)
  } catch {
    return value
  }
}

function formatDate(date) {
  if (!date) return '—'
  try {
    return new Date(date).toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    })
  } catch {
    return date
  }
}

function maskAccount(t) {
  if (!t.bankName) return '—'
  const num = String(t.bankAccountNumber || '')
  const last4 = num.length > 4 ? '•••• ' + num.slice(-4) : num
  return `${t.bankName} (${last4})`
}

function maskAccountOption(b) {
  if (!b) return '—'
  const num = String(b.bankAccountNumber || '')
  const last4 = num.length > 4 ? '•••• ' + num.slice(-4) : num
  return `${b.bankName} — ${last4}`
}

onMounted(loadAll)
</script>

<template>
  <div>
    <!-- ═══ Header ═══ -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2 fade-in-up">
      <div>
        <h1 class="font-brand page-title mb-1">Transactions</h1>
        <p class="text-muted-soft mb-0">Every rupee, tracked.</p>
      </div>
      <div class="d-flex gap-2">
        <!-- ✅ Import button -->
        <button class="btn btn-expenzo-outline" @click="showImportModal = true">
          <i class="bi bi-upload me-1"></i> Import
        </button>
        <button class="btn btn-expenzo" @click="openAddModal">
          <i class="bi bi-plus-lg me-1"></i> Add Transaction
        </button>
      </div>
    </div>

    <!-- ═══ Error ═══ -->
    <div v-if="errorMsg" class="alert alert-danger d-flex justify-content-between align-items-center">
      <span><i class="bi bi-exclamation-triangle-fill me-2"></i>{{ errorMsg }}</span>
      <button type="button" class="btn-close btn-close-sm" @click="errorMsg = ''"></button>
    </div>

    <!-- ═══ Table Card ═══ -->
    <div class="surface-card p-3 p-md-4 fade-in-up stagger-2">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" style="color: var(--color-primary)"></div>
        <p class="text-muted-soft mt-2 small">Loading transactions...</p>
      </div>

      <div v-else-if="!transactions.length" class="text-center text-muted-soft py-5">
        <i class="bi bi-inbox d-block mb-2" style="font-size: 2.5rem; opacity: 0.4"></i>
        <p class="mb-2">No transactions yet.</p>
        <p class="small mb-0">Add your first one or import a CSV/Excel file.</p>
      </div>

      <div v-else class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr class="text-muted-soft small text-uppercase">
              <th>Type</th>
              <th>Description</th>
              <th>Category</th>
              <th>Bank Account</th>
              <th>Date</th>
              <th class="text-end">Amount</th>
              <th class="text-end">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in transactions" :key="t.id">
              <!-- Type column -->
              <td>
                <div class="d-flex flex-wrap gap-1">
                  <span
                    class="badge"
                    :style="t.transactionType === 'CREDIT'
                      ? 'background:#d1fae5; color:#065f46; font-size:0.75rem'
                      : 'background:#fee2e2; color:#991b1b; font-size:0.75rem'"
                  >
                    <i
                      :class="t.transactionType === 'CREDIT'
                        ? 'bi bi-arrow-down-circle-fill'
                        : 'bi bi-arrow-up-circle-fill'"
                      class="me-1"
                    ></i>
                    {{ t.transactionType === 'CREDIT' ? 'Credit' : 'Debit' }}
                  </span>

                  <span
                    v-if="t.description?.startsWith('[Auto]')"
                    class="badge"
                    style="background:#fef9c3; color:#854d0e; font-size:0.7rem"
                  >
                    <i class="bi bi-robot me-1"></i>Auto
                  </span>

                  <span
                    v-if="t.recurring"
                    class="badge"
                    style="background:#e0e7ff; color:#3730a3; font-size:0.7rem"
                  >
                    <i class="bi bi-arrow-repeat me-1"></i>
                    Har {{ t.recurrenceDay }} tarikh
                  </span>
                </div>
              </td>

              <td class="fw-semibold">{{ t.description?.replace('[Auto] ', '') || '—' }}</td>
              <td><span class="chip">{{ t.categoryName || 'Uncategorized' }}</span></td>
              <td class="text-muted-soft small">{{ maskAccount(t) }}</td>
              <td class="text-muted-soft small">{{ formatDate(t.date) }}</td>

              <td class="text-end fw-semibold">
                <span :style="t.transactionType === 'CREDIT' ? 'color:#059669' : 'color:#dc2626'">
                  {{ t.transactionType === 'CREDIT' ? '+' : '-' }}{{ formatCurrency(t.amount, t.currency) }}
                </span>
              </td>

              <td class="text-end">
                <div class="d-flex justify-content-end gap-2">
                  <button class="btn btn-sm btn-expenzo-outline" @click="openEditModal(t)">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-outline-danger"
                    :disabled="deletingId === t.id"
                    @click="handleDelete(t.id)"
                  >
                    <span v-if="deletingId === t.id" class="spinner-border spinner-border-sm"></span>
                    <i v-else class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ Add / Edit Modal ═══ -->
    <AppModal v-model="showModal" :title="editingId ? 'Edit Transaction' : 'Add Transaction'">
      <form @submit.prevent="handleSubmit">

        <!-- Credit/Debit toggle -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Transaction Type</label>
          <div class="d-flex gap-2">
            <button
              type="button"
              class="btn flex-fill"
              :style="form.transactionType === 'CREDIT'
                ? 'background:#d1fae5; color:#065f46; border:2px solid #059669; font-weight:600'
                : 'background:#f9fafb; color:#6b7280; border:2px solid #e5e7eb'"
              :disabled="saving"
              @click="form.transactionType = 'CREDIT'"
            >
              <i class="bi bi-arrow-down-circle-fill me-1"></i> Credit (Income)
            </button>
            <button
              type="button"
              class="btn flex-fill"
              :style="form.transactionType === 'DEBIT'
                ? 'background:#fee2e2; color:#991b1b; border:2px solid #dc2626; font-weight:600'
                : 'background:#f9fafb; color:#6b7280; border:2px solid #e5e7eb'"
              :disabled="saving"
              @click="form.transactionType = 'DEBIT'"
            >
              <i class="bi bi-arrow-up-circle-fill me-1"></i> Debit (Expense)
            </button>
          </div>
        </div>

        <!-- Description -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Description</label>
          <textarea v-model="form.description" class="form-control" required :disabled="saving"></textarea>
        </div>

        <!-- Amount + Currency -->
        <div class="row g-2 mb-3">
          <div class="col-8">
            <label class="form-label fw-semibold">Amount</label>
            <input v-model="form.amount" type="number" step="0.01" min="0" class="form-control" required :disabled="saving" />
          </div>
          <div class="col-4">
            <label class="form-label fw-semibold">Currency</label>
            <input v-model="form.currency" type="text" class="form-control" required :disabled="saving" />
          </div>
        </div>

        <!-- Category -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Category</label>
          <select v-model="form.categoryId" class="form-select" required :disabled="saving">
            <option disabled value="">-- Select Category --</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>

        <!-- Bank Account -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Bank Account</label>
          <select v-model="form.bankAccountId" class="form-select" required :disabled="saving">
            <option disabled value="">-- Select Bank Account --</option>
            <option v-for="b in bankAccounts" :key="b.id" :value="b.id">
              {{ maskAccountOption(b) }}
            </option>
          </select>
        </div>

        <!-- Date -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Date</label>
          <input v-model="form.date" type="datetime-local" class="form-control" required :disabled="saving" />
        </div>

        <!-- Recurring toggle -->
        <div class="mb-2">
          <div class="form-check form-switch">
            <input
              class="form-check-input"
              type="checkbox"
              id="recurringSwitch"
              v-model="form.isRecurring"
              :disabled="saving"
            />
            <label class="form-check-label fw-semibold" for="recurringSwitch">
              Recurring Transaction
            </label>
          </div>
          <div class="form-text">Jaise: Salary har 1 tarikh ko, Rent har 5 tarikh ko</div>
        </div>

        <!-- Recurrence day -->
        <div class="mb-4" v-if="form.isRecurring">
          <label class="form-label fw-semibold">Har mahine kis tarikh ko? (1–28)</label>
          <input
            v-model.number="form.recurrenceDay"
            type="number"
            min="1"
            max="28"
            class="form-control"
            placeholder="e.g. 1 for 1st, 5 for 5th"
            :required="form.isRecurring"
            :disabled="saving"
          />
          <div class="form-text">
            <i class="bi bi-info-circle me-1"></i>
            Max 28 rakha hai taaki Feb mein bhi kaam kare.
          </div>
        </div>

        <button type="submit" class="btn btn-expenzo w-100" :disabled="saving">
          <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
          {{ editingId ? 'Save Changes' : 'Add Transaction' }}
        </button>
      </form>
    </AppModal>

    <!-- ═══ Import Modal ═══ -->
    <ImportTransactionsModal
      v-model="showImportModal"
      @imported="handleImported"
    />
  </div>
</template>