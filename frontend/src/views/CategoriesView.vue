<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import categoryService from '@/services/categoryService'
import { logger } from '@/utils/logger'

// ─── State ─────────────────────────────────────────
const categories = ref([])
const loading = ref(false)
const errorMsg = ref('')
const showModal = ref(false)
const newCategoryName = ref('')
const saving = ref(false)
const deletingId = ref(null)
const nameInputRef = ref(null)

// ─── Lifecycle ─────────────────────────────────────
onMounted(async () => {
  await fetchCategories()
  document.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  // Ensure body scroll is restored if component unmounts with modal open
  document.body.style.overflow = ''
})

// Lock body scroll while modal is open
watch(showModal, (isOpen) => {
  document.body.style.overflow = isOpen ? 'hidden' : ''
})

// ─── Fetch Categories ──────────────────────────────
async function fetchCategories() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await categoryService.getAllCategories()
    logger.log('📊 Fetched categories:', data)
    categories.value = Array.isArray(data) ? data : []
  } catch (err) {
    if (err.code === 'ERR_CANCELED') return      // user logged out mid-request
    logger.error('❌ Fetch categories failed:', err)
    errorMsg.value = extractErrorMessage(err, 'Failed to load categories')
  } finally {
    loading.value = false
  }
}

// ─── Add Category ──────────────────────────────────
async function handleAddCategory() {
  const name = newCategoryName.value.trim()

  if (!name) {
    errorMsg.value = 'Category name is required'
    return
  }

  saving.value = true
  errorMsg.value = ''

  try {
    const newCategory = await categoryService.addCategory({ name })
    logger.log('✅ Category added:', newCategory)

    // Optimistic update — push to array without refetching
    if (newCategory?.id) {
      categories.value.push(newCategory)
    } else {
      // Fallback: refetch if backend didn't return the created object
      await fetchCategories()
    }

    // Reset + close
    newCategoryName.value = ''
    showModal.value = false
  } catch (err) {
    if (err.code === 'ERR_CANCELED') return
    logger.error('❌ Add category failed:', err)
    errorMsg.value = extractErrorMessage(err, 'Failed to add category')
  } finally {
    saving.value = false
  }
}

// ─── Delete Category ───────────────────────────────
async function handleDelete(id) {
  if (deletingId.value) return        // prevent double-click
  if (!confirm('Are you sure you want to delete this category?')) return

  deletingId.value = id
  errorMsg.value = ''

  try {
    await categoryService.deleteCategory(id)
    logger.log('✅ Category deleted:', id)

    // Remove from local array
    categories.value = categories.value.filter((c) => c.id !== id)
  } catch (err) {
    if (err.code === 'ERR_CANCELED') return
    logger.error('❌ Delete category failed:', err)
    errorMsg.value = extractErrorMessage(err, 'Failed to delete category')
  } finally {
    deletingId.value = null
  }
}

// ─── Modal Handlers ────────────────────────────────
function openModal() {
  newCategoryName.value = ''
  errorMsg.value = ''
  showModal.value = true

  // Focus the input on next tick (after modal renders)
  nextTick(() => {
    nameInputRef.value?.focus()
  })
}

function closeModal() {
  if (saving.value) return   // don't close while saving
  showModal.value = false
  newCategoryName.value = ''
}

function handleKeyDown(event) {
  if (event.key === 'Escape' && showModal.value) {
    closeModal()
  }
}

// ─── Helpers ───────────────────────────────────────
function extractErrorMessage(err, fallback) {
  const raw =
    err?.response?.data?.message ??
    err?.response?.data ??
    err?.message ??
    fallback
  return typeof raw === 'string' ? raw : fallback
}
</script>

<template>
  <div class="categories-page p-4">
    <!-- ─── Header ───────────────────────────────── -->
    <div class="d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-3 mb-4">
      <div>
        <h1 class="font-brand mb-1">Categories</h1>
        <p class="text-muted-soft mb-0">Organize where your money goes.</p>
      </div>
      <button class="btn btn-expenzo w-100 w-sm-auto" @click="openModal">
        <i class="bi bi-plus-lg me-2"></i>
        Add Category
      </button>
    </div>

    <!-- ─── Error Alert ──────────────────────────── -->
    <div v-if="errorMsg" class="alert alert-danger py-2 small d-flex align-items-center justify-content-between">
      <span>
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        {{ errorMsg }}
      </span>
      <button type="button" class="btn-close btn-close-sm" @click="errorMsg = ''"></button>
    </div>

    <!-- ─── Loading State ────────────────────────── -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-warning" role="status"></div>
      <p class="mt-3 text-muted-soft">Loading categories...</p>
    </div>

    <!-- ─── Empty State ──────────────────────────── -->
    <div v-else-if="categories.length === 0" class="text-center py-5">
      <i class="bi bi-tags fs-1 text-muted-soft"></i>
      <p class="mt-3 text-muted-soft">
        No categories yet — create one to start tagging transactions.
      </p>
      <button class="btn btn-expenzo mt-2" @click="openModal">
        <i class="bi bi-plus-lg me-2"></i>
        Create Your First Category
      </button>
    </div>

    <!-- ─── Categories Grid ──────────────────────── -->
    <div v-else class="row g-3">
      <div
        v-for="category in categories"
        :key="category.id"
        class="col-12 col-sm-6 col-md-4 col-lg-3"
      >
        <div class="category-card">
          <div class="d-flex justify-content-between align-items-start">
            <div class="flex-grow-1 min-w-0">
              <i class="bi bi-tag-fill category-icon"></i>
              <h5 class="mt-2 mb-0 text-truncate" :title="category.name">
                {{ category.name }}
              </h5>
              <small class="text-muted-soft">ID: {{ category.id }}</small>
            </div>
            <button
              class="btn btn-sm btn-outline-danger ms-2 flex-shrink-0"
              :disabled="deletingId === category.id"
              :title="`Delete ${category.name}`"
              @click="handleDelete(category.id)"
            >
              <span
                v-if="deletingId === category.id"
                class="spinner-border spinner-border-sm"
                role="status"
              ></span>
              <i v-else class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ─── Add Category Modal ───────────────────── -->
    <transition name="fade">
      <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
        <div class="modal-content-card">
          <h4 class="mb-3">Add New Category</h4>

          <form @submit.prevent="handleAddCategory">
            <div class="mb-3">
              <label class="form-label fw-semibold">Category Name</label>
              <input
                ref="nameInputRef"
                v-model="newCategoryName"
                type="text"
                class="form-control"
                placeholder="e.g., Food, Transport, Bills"
                required
                maxlength="50"
                :disabled="saving"
              />
              <small class="text-muted-soft">{{ newCategoryName.length }}/50</small>
            </div>

            <div class="d-flex gap-2">
              <button
                type="button"
                class="btn btn-outline-secondary flex-fill"
                :disabled="saving"
                @click="closeModal"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="btn btn-expenzo flex-fill"
                :disabled="saving || !newCategoryName.trim()"
              >
                <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                {{ saving ? 'Saving...' : 'Add Category' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.category-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  border: 1px solid #f0f0f0;
  height: 100%;
}

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--color-accent-orange);
}

.category-icon {
  font-size: 1.8rem;
  color: var(--color-accent-orange);
}

/* Fix text truncation in flexbox */
.min-w-0 {
  min-width: 0;
}

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}

.modal-content-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 400px;
  width: 100%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-height: 90vh;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Mobile tweak — full-width buttons with better spacing */
@media (max-width: 576px) {
  .w-sm-auto {
    width: auto;
  }
  .modal-content-card {
    padding: 20px;
  }
}

@media (min-width: 577px) {
  .w-sm-auto {
    width: auto !important;
  }
}
</style>