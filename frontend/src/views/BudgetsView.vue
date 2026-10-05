<script setup>
import { ref, computed, onMounted } from 'vue'
import budgetService from '@/services/budgetService'
import categoryService from '@/services/categoryService'
import transactionService from '@/services/transactionService'
import AppModal from '@/components/AppModal.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// ─── State ──────────────────────────────────────────────────
const budgets = ref([])
const categories = ref([])
const transactions = ref([])
const loading = ref(true)
const saving = ref(false)
const errorMsg = ref('')
const showModal = ref(false)
const editingId = ref(null)

const currentMonth = new Date().getMonth() + 1
const currentYear = new Date().getFullYear()

const form = ref({
  categoryId: '',
  month: currentMonth,
  year: currentYear,
  monthlyLimit: '',
})

// ─── Helpers ───────────────────────────────────────────────
function formatCurrency(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value || 0)
}

const monthNames = [
  'Jan','Feb','Mar','Apr','May','Jun',
  'Jul','Aug','Sep','Oct','Nov','Dec',
]

function monthLabel(m) {
  return monthNames[(Number(m) - 1)] || m
}

// ─── Per-category spend (only DEBIT counts) ───────────────
const categorySpendMap = computed(() => {
  const map = new Map()
  transactions.value
    .filter((t) => t.transactionType === 'DEBIT')
    .forEach((t) => {
      const key = t.categoryName || 'Uncategorized'
      map.set(key, (map.get(key) || 0) + Number(t.amount || 0))
    })
  return map
})

// ─── Budgets enriched with actual spend and overspend flag ─
const enrichedBudgets = computed(() =>
  budgets.value.map((b) => {
    const catName =
      b.categoryName ||
      categories.value.find((c) => c.id === b.categoryId)?.name ||
      '—'
    const spent = categorySpendMap.value.get(catName) || 0
    const limit = Number(b.monthlyLimit || 0)
    const overspent = spent > limit
    const pct = limit > 0 ? Math.min((spent / limit) * 100, 100) : 0
    return { ...b, catName, spent, limit, overspent, pct }
  })
)

// ─── Summary stats ────────────────────────────────────────
const totalLimit = computed(() =>
  enrichedBudgets.value.reduce((s, b) => s + b.limit, 0)
)
const totalSpent = computed(() =>
  enrichedBudgets.value.reduce((s, b) => s + b.spent, 0)
)
const overspentCount = computed(
  () => enrichedBudgets.value.filter((b) => b.overspent).length
)

// ─── Modal helpers ────────────────────────────────────────
function openAdd() {
  editingId.value = null
  form.value = {
    categoryId: categories.value[0]?.id || '',
    month: currentMonth,
    year: currentYear,
    monthlyLimit: '',
  }
  showModal.value = true
}

function openEdit(b) {
  editingId.value = b.id
  form.value = {
    categoryId: b.categoryId || categories.value.find((c) => c.name === b.categoryName)?.id || '',
    month: b.month,
    year: b.year,
    monthlyLimit: b.monthlyLimit,
  }
  showModal.value = true
}

// ─── CRUD ─────────────────────────────────────────────────
async function loadAll() {
  loading.value = true
  try {
    const [bud, cat, tx] = await Promise.all([
      budgetService.getAll(),
      categoryService.getAllCategories(),
      transactionService.getAll(),
    ])
    const uid = authStore.user?.id
    budgets.value = bud.filter((b) => b.userId === uid)
    categories.value = cat.filter((c) => c.userId === uid)
    transactions.value = tx.filter((t) => t.userId === uid)
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  saving.value = true
  errorMsg.value = ''
  const payload = {
    categoryId: Number(form.value.categoryId),
    month: Number(form.value.month),
    year: Number(form.value.year),
    monthlyLimit: Number(form.value.monthlyLimit),
    userId: authStore.user?.id,
  }
  try {
    if (editingId.value) {
      await budgetService.update(editingId.value, payload)
    } else {
      await budgetService.add(payload)
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
  if (!confirm('Delete this budget?')) return
  try {
    await budgetService.remove(id)
    await loadAll()
  } catch (err) {
    errorMsg.value = err.message
  }
}

onMounted(loadAll)
</script>

<template>
  <div>
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2 fade-in-up">
      <div>
        <h1 class="font-brand page-title mb-1">Budgets</h1>
        <p class="text-muted-soft mb-0">Set monthly limits per category.</p>
      </div>
      <button class="btn btn-expenzo" @click="openAdd">
        <i class="bi bi-plus-lg me-1"></i> Add Budget
      </button>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">{{ errorMsg }}</div>

    <!-- ⚠️ Global overspend alert -->
    <div
      v-if="!loading && overspentCount"
      class="alert mb-4 fade-in-up"
      style="background:#fef2f2; border:1.5px solid #fca5a5; border-radius:var(--radius-md); padding:1rem 1.25rem"
    >
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-exclamation-triangle-fill" style="color:#dc2626; font-size:1.1rem"></i>
        <span class="fw-bold" style="color:#dc2626">
          {{ overspentCount }} budget{{ overspentCount > 1 ? 's' : '' }} exceeded this month!
        </span>
        <span class="small ms-1" style="color:#7f1d1d">Review the cards below for details.</span>
      </div>
    </div>

    <!-- Summary bar -->
    <div v-if="!loading && budgets.length" class="row g-3 mb-4 fade-in-up stagger-1">
      <div class="col-6 col-md-3">
        <div class="surface-card p-3 text-center">
          <p class="text-muted-soft small text-uppercase fw-semibold mb-1">Total Budget</p>
          <span class="fw-bold font-mono" style="color:var(--color-primary)">{{ formatCurrency(totalLimit) }}</span>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="surface-card p-3 text-center">
          <p class="text-muted-soft small text-uppercase fw-semibold mb-1">Total Spent</p>
          <span class="fw-bold font-mono" :style="totalSpent > totalLimit ? 'color:#dc2626' : 'color:var(--color-accent-flare)'">
            {{ formatCurrency(totalSpent) }}
          </span>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="surface-card p-3 text-center">
          <p class="text-muted-soft small text-uppercase fw-semibold mb-1">Remaining</p>
          <span class="fw-bold font-mono" :style="totalLimit - totalSpent < 0 ? 'color:#dc2626' : 'color:#059669'">
            {{ formatCurrency(Math.max(totalLimit - totalSpent, 0)) }}
          </span>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="surface-card p-3 text-center">
          <p class="text-muted-soft small text-uppercase fw-semibold mb-1">Overspent</p>
          <span class="fw-bold font-mono" :style="overspentCount ? 'color:#dc2626' : 'color:#059669'">
            {{ overspentCount }} categor{{ overspentCount === 1 ? 'y' : 'ies' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border" style="color: var(--color-primary)"></div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!budgets.length" class="surface-card p-5 text-center text-muted-soft fade-in-up stagger-2">
      <i class="bi bi-wallet2 d-block mb-2" style="font-size:2.5rem; opacity:0.4"></i>
      <p class="mb-0">No budgets yet. Add one to start tracking!</p>
    </div>

    <!-- Budget cards grid -->
    <div v-else class="row g-3">
      <div
        v-for="(b, idx) in enrichedBudgets"
        :key="b.id"
        class="col-12 col-md-6 col-lg-4 fade-in-up"
        :style="`animation-delay: ${(idx % 6) * 0.07}s`"
      >
        <div
          class="surface-card p-4 h-100"
          :style="b.overspent
            ? 'border: 2px solid #fca5a5; background: #fff5f5'
            : 'border: 2px solid transparent'"
        >
          <!-- Card header -->
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
              <!-- Category chip + Overspent badge -->
              <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
                <span class="chip fw-semibold">{{ b.catName }}</span>
                <span
                  v-if="b.overspent"
                  class="badge"
                  style="background:#fee2e2; color:#dc2626; font-size:0.7rem; padding:0.3em 0.6em"
                >
                  <i class="bi bi-exclamation-triangle-fill me-1"></i>Overspent
                </span>
              </div>
              <p class="text-muted-soft small mb-0">
                {{ monthLabel(b.month) }} {{ b.year }}
              </p>
            </div>

            <!-- Actions -->
            <div class="d-flex gap-1">
              <button class="btn btn-sm btn-expenzo-outline" @click="openEdit(b)">
                <i class="bi bi-pencil"></i>
              </button>
              <button class="btn btn-sm btn-outline-danger" @click="handleDelete(b.id)">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>

          <!-- Spend vs Limit numbers -->
          <div class="d-flex justify-content-between align-items-baseline mb-2">
            <span class="fw-bold font-mono" :style="b.overspent ? 'color:#dc2626' : 'color:var(--color-primary); font-size:1.15rem'">
              {{ formatCurrency(b.spent) }}
            </span>
            <span class="small text-muted-soft">/ {{ formatCurrency(b.limit) }}</span>
          </div>

          <!-- Progress bar -->
          <div
            class="rounded-pill mb-2"
            style="height: 8px; background: #e5e7eb; overflow: hidden"
          >
            <div
              class="rounded-pill h-100"
              :style="{
                width: b.pct + '%',
                background: b.overspent ? '#ef4444' : b.pct > 80 ? '#f59e0b' : 'var(--color-primary)',
                transition: 'width 0.6s ease'
              }"
            ></div>
          </div>

          <!-- Remaining / Over amount -->
          <div class="small" :style="b.overspent ? 'color:#dc2626; font-weight:600' : 'color:var(--color-text-muted)'">
            <span v-if="b.overspent">
              <i class="bi bi-exclamation-circle-fill me-1"></i>
              ₹{{ Math.round(b.spent - b.limit).toLocaleString('en-IN') }} over budget!
            </span>
            <span v-else>
              {{ formatCurrency(b.limit - b.spent) }} remaining ({{ Math.round(b.pct) }}% used)
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Add / Edit Modal -->
    <AppModal v-model="showModal" :title="editingId ? 'Edit Budget' : 'Add Budget'">
      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label fw-semibold">Category</label>
          <select v-model="form.categoryId" class="form-select" required>
            <option disabled value="">-- Select Category --</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>

        <div class="row g-2 mb-3">
          <div class="col-6">
            <label class="form-label fw-semibold">Month</label>
            <select v-model.number="form.month" class="form-select" required>
              <option v-for="(name, i) in monthNames" :key="i + 1" :value="i + 1">{{ name }}</option>
            </select>
          </div>
          <div class="col-6">
            <label class="form-label fw-semibold">Year</label>
            <input v-model.number="form.year" type="number" class="form-control" required min="2020" max="2099" />
          </div>
        </div>

        <div class="mb-4">
          <label class="form-label fw-semibold">Monthly Limit (₹)</label>
          <input v-model.number="form.monthlyLimit" type="number" class="form-control" required min="1" step="1" placeholder="e.g. 5000" />
        </div>

        <button type="submit" class="btn btn-expenzo w-100" :disabled="saving">
          <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
          {{ editingId ? 'Save Changes' : 'Create Budget' }}
        </button>
      </form>
    </AppModal>
  </div>
</template>
