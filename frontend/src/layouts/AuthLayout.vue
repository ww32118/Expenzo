<script setup>
import ThemeToggle from '@/components/ThemeToggle.vue'
</script>

<template>
  <div class="auth-wrapper position-relative">
    <span class="auth-watermark">Expenzo</span>
    <div class="position-absolute top-0 end-0 p-3" style="z-index: 2">
      <ThemeToggle />
    </div>
    <router-view />
  </div>
</template>
