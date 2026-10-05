<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import AppShell from '@/layouts/AppShell.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { isApiLoading } from '@/services/api'

const authStore = useAuthStore()
const route = useRoute()

onMounted(() => {
  authStore.restoreSession()
})

// Resolve layout based on route.meta.layout
// 'app'  → AppShell    (sidebar + topbar) — for protected pages
// 'auth' → AuthLayout  (centered card)    — for login/register/etc.
const currentLayout = computed(() => {
  const name = route.meta.layout || 'app'
  const layouts = {
    app: AppShell,
    auth: AuthLayout,
  }
  return layouts[name] || AppShell
})
</script>

<template>
  <transition name="fade">
    <div v-if="isApiLoading" class="top-progress-bar"></div>
  </transition>

  <ErrorBoundary>
    <component :is="currentLayout" />
  </ErrorBoundary>
</template>

<style>
.top-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  width: 100%;
  background: linear-gradient(90deg, #ff7a00, #8b5cf6);
  background-size: 200% 100%;
  animation: loadingPulse 1.5s ease-in-out infinite;
  z-index: 9999;
  pointer-events: none;
}

@keyframes loadingPulse {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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