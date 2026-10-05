<script setup>
import { ref, computed, onMounted } from 'vue'
import transactionService from '@/services/transactionService'
import categoryService from '@/services/categoryService'
import budgetService from '@/services/budgetService'
import DonutChart from '@/components/charts/DonutChart.vue'
import BudgetBarChart from '@/components/charts/BudgetBarChart.vue'
import TrendBarChart from '@/components/charts/TrendBarChart.vue'
import { useCountUp } from '@/composables/useCountUp'
import { useThemeColors } from '@/composables/useThemeColors'
import { useAuthStore } from '@/stores/auth'

// ─── Stores & State ─────────────────────────────────────────
const authStore = useAuthStore()
const { chartPalette } = useThemeColors()

const transactions = ref([])
const categories = ref([])
const budgets = ref([])
const loading = ref(true)
const errorMsg = ref('')

// ─── Personalized Greeting ─────────────────────────────────
const userName = computed(
  () => authStore.user?.name || authStore.user?.username || 'Guest'
)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 17) return 'Good afternoon'
  return 'Good evening'
})

// ─── Helpers ───────────────────────────────────────────────
function formatCurrency(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value || 0)
}

// ─── Stat Totals ───────────────────────────────────────────
const totalSpend = computed(() =>
  transactions.value
    .filter((t) => t.transactionType === 'DEBIT')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0)
)

const totalIncome = computed(() =>
  transactions.value
    .filter((t) => t.transactionType === 'CREDIT')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0)
)

const totalBudget = computed(() =>
  budgets.value.reduce((sum, b) => sum + Number(b.monthlyLimit || 0), 0)
)

const transactionCount = computed(() => transactions.value.length)

const avgTransaction = computed(() =>
  transactionCount.value ? totalSpend.value / transactionCount.value : 0
)

// ─── Animated Count-Ups ───────────────────────────────────
const animSpend = useCountUp(totalSpend)
const animBudget = useCountUp(totalBudget)
const animIncome = useCountUp(totalIncome)
const animAvg = useCountUp(avgTransaction)

const budgetUsedPct = computed(() =>
  totalBudget.value > 0
    ? Math.min((totalSpend.value / totalBudget.value) * 100, 999)
    : 0
)

// ─── Category Spend (only DEBIT counts as "spend") ────────
const categorySpend = computed(() => {
  const map = new Map()
  transactions.value
    .filter((t) => t.transactionType === 'DEBIT')
    .forEach((t) => {
      const key = t.categoryName || 'Uncategorized'
      map.set(key, (map.get(key) || 0) + Number(t.amount || 0))
    })
  return [...map.entries()]
    .map(([name, amount], i) => ({
      label: name,
      value: amount,
      valueLabel: formatCurrency(amount),
      color: chartPalette.value[i % chartPalette.value.length],
    }))
    .sort((a, b) => b.value - a.value)
})

// ─── Budget vs Actual ─────────────────────────────────────
const budgetVsActual = computed(() => {
  const spendMap = new Map(categorySpend.value.map((c) => [c.label, c.value]))
  return budgets.value.map((b, i) => {
    const catName =
      b.categoryName ||
      categories.value.find((c) => c.id === b.categoryId)?.name ||
      b.month
    const spent = spendMap.get(catName) || 0
    const budget = Number(b.monthlyLimit || 0)
    const overspent = spent > budget
    return {
      label: catName,
      spent,
      budget,
      spentLabel: formatCurrency(spent),
      budgetLabel: formatCurrency(b.monthlyLimit),
      color: overspent
        ? '#ef4444'
        : chartPalette.value[i % chartPalette.value.length],
      overspent,
    }
  })
})

// ─── Overspent categories — for alert banner ──────────────
const overspentCategories = computed(() =>
  budgetVsActual.value.filter((b) => b.overspent)
)

// ─── 6-Month Trend (only DEBIT) ───────────────────────────
const monthlyTrend = computed(() => {
  const now = new Date()
  const buckets = []
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    buckets.push({
      key: `${d.getFullYear()}-${d.getMonth()}`,
      label: d.toLocaleString('en', { month: 'short' }),
      value: 0,
    })
  }
  transactions.value
    .filter((t) => t.transactionType === 'DEBIT')
    .forEach((t) => {
      if (!t.date) return
      const d = new Date(t.date)
      const key = `${d.getFullYear()}-${d.getMonth()}`
      const bucket = buckets.find((b) => b.key === key)
      if (bucket) bucket.value += Number(t.amount || 0)
    })
  return buckets.map((b) => ({ ...b, valueLabel: formatCurrency(b.value) }))
})

// ─── Recent Transactions ──────────────────────────────────
const recentTransactions = computed(() =>
  [...transactions.value]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 6)
)

// ─── Lifecycle ────────────────────────────────────────────
onMounted(async () => {
  try {
    const [tx, cat, bud] = await Promise.all([
      transactionService.getAll(),
      categoryService.getAllCategories(),
      budgetService.getAll(),
    ])

    const currentUserId = authStore.user?.id
    transactions.value = tx.filter((t) => t.userId === currentUserId)
    categories.value = cat.filter((c) => c.userId === currentUserId)
    budgets.value = bud.filter((b) => b.userId === currentUserId)
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <!-- Hero banner -->
    <div
      class="hero-watermark-wrap d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3 p-4 p-md-4 fade-in-up"
      style="background: var(--hero-gradient); border-radius: var(--radius-lg); box-shadow: var(--shadow-lift)"
    >
      <span class="hero-watermark">₹</span>
      <div style="position: relative; z-index: 1">
        <h1 class="font-brand page-title mb-1" style="color: #fff">
          {{ greeting }}, {{ userName }} 👋
        </h1>
        <p class="mb-0" style="color: rgba(255, 255, 255, 0.85)">
          Here's your spending overview at a glance.
        </p>
      </div>
      <router-link to="/transactions" class="btn btn-expenzo" style="position: relative; z-index: 1">
        <i class="bi bi-plus-lg me-1"></i> Add Transaction
      </router-link>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">{{ errorMsg }}</div>

    <!-- ⚠️ Overspend Alert Banner -->
    <div
      v-if="overspentCategories.length"
      class="alert mb-4 fade-in-up"
      style="background:#fef2f2; border:1.5px solid #fca5a5; border-radius:var(--radius-md); padding:1rem 1.25rem"
    >
      <div class="d-flex align-items-center gap-2 mb-1">
        <i class="bi bi-exclamation-triangle-fill" style="color:#dc2626; font-size:1.1rem"></i>
        <span class="fw-bold" style="color:#dc2626">Budget Exceeded!</span>
      </div>
      <p class="mb-0 small" style="color:#7f1d1d">
        <span v-for="(cat, i) in overspentCategories" :key="cat.label">
          <strong>{{ cat.label }}</strong>
          — spent {{ cat.spentLabel }} out of {{ cat.budgetLabel }} budget
          (₹{{ Math.round(cat.spent - cat.budget).toLocaleString('en-IN') }} over)
          <span v-if="i < overspentCategories.length - 1"> &nbsp;•&nbsp; </span>
        </span>
      </p>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border" style="color: var(--color-primary)"></div>
    </div>

    <template v-else>
      <!-- Stat cards -->
      <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
          <div class="surface-card surface-card--interactive p-4 h-100 fade-in-up stagger-1">
            <p class="text-muted-soft mb-1 small text-uppercase fw-semibold">Total Spent</p>
            <h2 class="font-mono fw-bold mb-0" style="color: var(--color-accent-flare)">
              {{ formatCurrency(animSpend) }}
            </h2>
          </div>
        </div>
        <div class="col-6 col-md-3">
          <div class="surface-card surface-card--interactive p-4 h-100 fade-in-up stagger-2">
            <p class="text-muted-soft mb-1 small text-uppercase fw-semibold">Monthly Budget</p>
            <h2 class="font-mono fw-bold mb-0" style="color: var(--color-primary)">
              {{ formatCurrency(animBudget) }}
            </h2>
            <span v-if="totalBudget" class="small" :style="budgetUsedPct >= 100 ? 'color:#dc2626;font-weight:600' : 'color:var(--color-text-muted)'">
              {{ budgetUsedPct.toFixed(0) }}% used
              <span v-if="budgetUsedPct >= 100"> ⚠️</span>
            </span>
          </div>
        </div>
        <div class="col-6 col-md-3">
          <div class="surface-card surface-card--interactive p-4 h-100 fade-in-up stagger-3">
            <p class="text-muted-soft mb-1 small text-uppercase fw-semibold">Total Income</p>
            <h2 class="font-mono fw-bold mb-0" style="color:#059669">
              {{ formatCurrency(animIncome) }}
            </h2>
          </div>
        </div>
        <div class="col-6 col-md-3">
          <div class="surface-card surface-card--interactive p-4 h-100 fade-in-up stagger-4">
            <p class="text-muted-soft mb-1 small text-uppercase fw-semibold">Avg / Transaction</p>
            <h2 class="font-mono fw-bold mb-0">{{ formatCurrency(animAvg) }}</h2>
          </div>
        </div>
      </div>

      <!-- Charts row -->
      <div class="row g-3 mb-4">
        <div class="col-lg-5">
          <div class="surface-card shadow-chart p-4 h-100 fade-in-up stagger-2">
            <h5 class="font-display fw-semibold mb-3">Spend by Category</h5>
            <div v-if="categorySpend.length" class="d-flex flex-column align-items-center">
              <DonutChart
                :series="categorySpend"
                :size="200"
                :thickness="26"
                center-label="Total Spent"
                :center-value="formatCurrency(totalSpend)"
              />
              <div class="d-flex flex-wrap justify-content-center gap-3 mt-4">
                <div v-for="c in categorySpend" :key="c.label" class="d-flex align-items-center gap-2 small">
                  <span class="d-inline-block rounded-circle" style="width:10px;height:10px" :style="{ backgroundColor: c.color }"></span>
                  <span class="text-muted-soft">{{ c.label }}</span>
                </div>
              </div>
            </div>
            <p v-else class="text-muted-soft small text-center py-5 mb-0">
              No transactions yet — add one to see this chart.
            </p>
          </div>
        </div>

        <div class="col-lg-7">
          <div class="surface-card shadow-chart p-4 h-100 fade-in-up stagger-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="font-display fw-semibold mb-0">Budget vs Actual</h5>
              <!-- Overspent count badge -->
              <span
                v-if="overspentCategories.length"
                class="badge"
                style="background:#fee2e2;color:#dc2626;font-size:0.75rem;padding:0.4em 0.75em"
              >
                <i class="bi bi-exclamation-triangle-fill me-1"></i>
                {{ overspentCategories.length }} overspent
              </span>
            </div>
            <BudgetBarChart v-if="budgetVsActual.length" :items="budgetVsActual" />
            <p v-else class="text-muted-soft small text-center py-5 mb-0">
              Set a budget to compare it against your spend.
            </p>

            <!-- Per-category overspend detail -->
            <div
              v-for="cat in overspentCategories"
              :key="cat.label"
              class="d-flex align-items-center gap-2 mt-2 p-2 rounded"
              style="background:#fef2f2;border:1px solid #fecaca"
            >
              <i class="bi bi-exclamation-circle-fill" style="color:#dc2626"></i>
              <span class="small" style="color:#7f1d1d">
                <strong>{{ cat.label }}</strong> — {{ cat.spentLabel }} spent vs {{ cat.budgetLabel }} budget
                <span class="fw-bold"> (+₹{{ Math.round(cat.spent - cat.budget).toLocaleString('en-IN') }} over)</span>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 6-Month Trend -->
      <div class="row g-3 mb-4">
        <div class="col-12">
          <div class="surface-card shadow-chart p-4 fade-in-up stagger-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="font-display fw-semibold mb-0">6-Month Spend Trend</h5>
              <span class="text-muted-soft small">Total per month</span>
            </div>
            <TrendBarChart :points="monthlyTrend" :height="140" />
          </div>
        </div>
      </div>

      <!-- Recent Transactions -->
      <div class="surface-card p-4 fade-in-up stagger-5">
        <h5 class="font-display fw-semibold mb-3">Recent Transactions</h5>
        <div v-if="!recentTransactions.length" class="text-muted-soft small">Nothing here yet.</div>
        <div
          v-for="t in recentTransactions"
          :key="t.date + t.description"
          class="d-flex justify-content-between align-items-center py-2 border-bottom"
        >
          <div>
            <p class="mb-0 fw-semibold">{{ t.description || 'Untitled' }}</p>
            <span class="chip">{{ t.categoryName || 'Uncategorized' }}</span>
          </div>
          <span
            class="fw-semibold"
            :style="t.transactionType === 'CREDIT' ? 'color:#059669' : 'color:#dc2626'"
          >
            {{ t.transactionType === 'CREDIT' ? '+' : '-' }}{{ formatCurrency(t.amount) }}
          </span>
        </div>
      </div>
    </template>
  </div>
</template>
