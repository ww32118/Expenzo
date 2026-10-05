<script setup>
import { ref, reactive, onMounted } from 'vue'
import bankAccountService, { ACCOUNT_TYPES } from '@/services/bankAccountService'
import { useAuthStore } from '@/stores/auth'
import AppModal from '@/components/AppModal.vue'

const authStore = useAuthStore()

const accounts = ref([])
const loading = ref(true)
const errorMsg = ref('')
const saving = ref(false)

const showModal = ref(false)
const editingId = ref(null)
const form = reactive({
  bankName: '',
  bankAccountNumber: '',
  accountHolderName: '',
  ifscCode: '',
  accountType: ACCOUNT_TYPES[0],
  balance: 0, // NAYA
})

function resetForm() {
  editingId.value = null
  form.bankName = ''
  form.bankAccountNumber = ''
  form.accountHolderName = ''
  form.ifscCode = ''
  form.accountType = ACCOUNT_TYPES[0]
  form.balance = 0 // NAYA
}

function openAddModal() {
  resetForm()
  showModal.value = true
}

function openEditModal(a) {
  editingId.value = a.id ?? null
  form.bankName = a.bankName
  form.bankAccountNumber = a.bankAccountNumber
  form.accountHolderName = a.accountHolderName
  form.ifscCode = a.ifscCode
  form.accountType = a.accountType
  form.balance = a.balance ?? 0 // NAYA
  showModal.value = true
}

async function loadAll() {
  loading.value = true
  try {
    const data = await bankAccountService.getAll()
    accounts.value = data.filter(
      account => account.userId === authStore.user.id
    )
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  saving.value = true
  errorMsg.value = ''
  const payload = { id: editingId.value, ...form, userId: authStore.userId }
  try {
    if (editingId.value) {
      await bankAccountService.update(payload)
    } else {
      await bankAccountService.add(payload)
    }
    showModal.value = false
    await loadAll()
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  if (!id) return
  if (!confirm('Delete this bank account?')) return
  try {
    await bankAccountService.remove(id)
    await loadAll()
  } catch (err) {
    errorMsg.value = err.message
  }
}

function maskAccountNumber(num) {
  if (!num) return '—'
  const str = String(num)
  return str.length > 4 ? '•••• ' + str.slice(-4) : str
}

onMounted(() => {
  loadAll()
})
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2 fade-in-up">
      <div>
        <h1 class="font-brand page-title mb-1">Bank Accounts</h1>
        <p class="text-muted-soft mb-0">Linked accounts for your transactions.</p>
      </div>
      <button class="btn btn-expenzo" @click="openAddModal">
        <i class="bi bi-plus-lg me-1"></i> Add Account
      </button>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">{{ errorMsg }}</div>
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border" style="color: var(--color-primary)"></div>
    </div>

    <div v-else class="row g-3 fade-in-up stagger-2">
      <div v-if="!accounts.length" class="text-center text-muted-soft py-5 w-100">
        No bank accounts linked yet.
      </div>

      <div v-for="(a, idx) in accounts" :key="a.id ?? idx" class="col-sm-6 col-lg-4">
        <div
          class="surface-card surface-card--interactive p-4"
          style="background: linear-gradient(135deg, var(--color-primary), var(--color-accent-orange)); color: #fff"
        >
          <!-- Top row: icon + account type chip -->
          <div class="d-flex justify-content-between align-items-start">
            <i class="bi bi-bank fs-3"></i>
            <span class="chip" style="background: rgba(255,255,255,0.25); color: #fff">
              {{ a.accountType }}
            </span>
          </div>

          <!-- Balance — prominently displayed -->
          <div class="mt-3 mb-2">
            <p class="mb-0 small opacity-75">Current Balance</p>
            <h3 class="mb-0 fw-bold">
              ₹{{ a.balance != null ? Number(a.balance).toFixed(2) : '0.00' }}
            </h3>
          </div>

          <!-- Account details -->
          <p class="font-mono fs-6 mb-1 opacity-75">A/C: {{ maskAccountNumber(a.bankAccountNumber) }}</p>
          <p class="mb-0 fw-semibold">{{ a.bankName }}</p>
          <p class="mb-0 small opacity-75">{{ a.accountHolderName }}</p>

          <!-- Action buttons -->
          <div class="d-flex gap-2 mt-3">
            <button class="btn btn-sm btn-light" @click="openEditModal(a)">
              <i class="bi bi-pencil"></i>
            </button>
            <button
              class="btn btn-sm btn-outline-light"
              :disabled="!a.id"
              @click="handleDelete(a.id)"
            >
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Add / Edit Modal -->
    <AppModal v-model="showModal" :title="editingId ? 'Edit Bank Account' : 'Add Bank Account'">
      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label fw-semibold">Bank name</label>
          <input v-model="form.bankName" type="text" class="form-control" required />
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">Account holder name</label>
          <input v-model="form.accountHolderName" type="text" class="form-control" required />
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">Account number</label>
          <input v-model="form.bankAccountNumber" type="text" class="form-control" required />
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">IFSC code</label>
          <input v-model="form.ifscCode" type="text" class="form-control" required />
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">Account type</label>
          <select v-model="form.accountType" class="form-select" required>
            <option v-for="t in ACCOUNT_TYPES" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>

        <!-- NAYA: Initial / current balance input -->
        <div class="mb-4">
          <label class="form-label fw-semibold">
            {{ editingId ? 'Current Balance (₹)' : 'Initial Balance (₹)' }}
          </label>
          <input
            v-model.number="form.balance"
            type="number"
            step="0.01"
            min="0"
            class="form-control"
            required
          />
          <div class="form-text" v-if="!editingId">
            Yaha account mein already kitna paisa hai wo daalo.
          </div>
        </div>

        <button type="submit" class="btn btn-expenzo w-100" :disabled="saving">
          <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
          {{ editingId ? 'Save Changes' : 'Add Account' }}
        </button>
      </form>
    </AppModal>
  </div>
</template>
