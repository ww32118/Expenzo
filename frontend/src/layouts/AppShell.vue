<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(false)

const navItems = [
  { to: '/', label: 'Dashboard', icon: 'bi-speedometer2' },
  { to: '/transactions', label: 'Transactions', icon: 'bi-arrow-left-right' },
  { to: '/categories', label: 'Categories', icon: 'bi-tags' },
  { to: '/budgets', label: 'Budgets', icon: 'bi-piggy-bank' },
  { to: '/bank-accounts', label: 'Bank Accounts', icon: 'bi-bank' },
  { to: '/export', label: 'Export', icon: 'bi-download' },
]

function logout() {
  authStore.logout()
  router.push({ name: 'login' })
}

// ✅ Profile image URL (fallback if empty)
const profileImage = computed(() => {
  const u = authStore.user
  return u?.image || u?.responseDTO?.image || ''
})

const initials = computed(() => {
  const name = authStore.displayName || 'G'
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase())
    .join('')
})

// ✅ Handle broken image URL → fallback to initials
const imageError = ref(false)
function onImageError() {
  imageError.value = true
}
</script>

<template>
  <div class="app-shell d-flex">
    <div class="sidebar-overlay" :class="{ show: sidebarOpen }" @click="sidebarOpen = false"></div>

    <aside class="app-sidebar" :class="{ open: sidebarOpen }">
      <div class="brand">
        <i class="bi bi-wallet2"></i>
        <span>Expenzo</span>
      </div>

      <!-- ✅ Sidebar Profile Card -->
      <div class="sidebar-profile">
        <div class="sidebar-profile-avatar">
          <img
            v-if="profileImage && !imageError"
            :src="profileImage"
            :alt="authStore.displayName"
            @error="onImageError"
          />
          <span v-else>{{ initials }}</span>
        </div>
        <div class="sidebar-profile-info">
          <p class="sidebar-profile-name">{{ authStore.displayName }}</p>
          <p class="sidebar-profile-role">Personal</p>
        </div>
      </div>

      <nav class="flex-grow-1">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link-item"
          active-class="active"
          exact-active-class="active"
          @click="sidebarOpen = false"
        >
          <i :class="`bi ${item.icon}`"></i>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <router-link to="/profile" class="nav-link-item" active-class="active">
        <i class="bi bi-person-circle"></i>
        <span>Profile</span>
      </router-link>
      <button
        type="button"
        class="nav-link-item border-0 bg-transparent text-start w-100"
        @click="logout"
      >
        <i class="bi bi-box-arrow-right"></i>
        <span>Logout</span>
      </button>
    </aside>

    <div class="app-main flex-grow-1">
      <header class="app-topbar">
        <div class="d-flex align-items-center gap-3">
          <button
            type="button"
            class="btn btn-link topbar-toggle d-md-none p-0"
            @click="sidebarOpen = !sidebarOpen"
          >
            <i class="bi bi-list fs-3"></i>
          </button>
          <span class="fw-semibold font-display d-none d-sm-inline topbar-greeting">
            Hi, {{ authStore.displayName }} 👋
          </span>
          <span class="fw-semibold font-display d-sm-none topbar-greeting">Hi 👋</span>
        </div>

        <div class="d-flex align-items-center gap-3">
          <ThemeToggle />

          <!-- ✅ Topbar Avatar (Image or Initials) -->
          <router-link to="/profile" class="avatar-chip" title="Profile">
            <img
              v-if="profileImage && !imageError"
              :src="profileImage"
              :alt="authStore.displayName"
              @error="onImageError"
            />
            <span v-else>{{ initials }}</span>
          </router-link>
        </div>
      </header>

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>