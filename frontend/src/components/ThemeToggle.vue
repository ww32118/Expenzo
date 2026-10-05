<script setup>
import { ref } from 'vue'

const theme = ref(document.documentElement.getAttribute('data-theme') || 'light')

function toggle() {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme.value)
  localStorage.setItem('expenzo-theme', theme.value)
}
</script>

<template>
  <button
    type="button"
    class="theme-toggle"
    :aria-label="theme === 'light' ? 'Switch to dark mode' : 'Switch to light mode'"
    :title="theme === 'light' ? 'Switch to dark mode' : 'Switch to light mode'"
    @click="toggle"
  >
    <span class="knob">
      <i :class="theme === 'light' ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
    </span>
  </button>
</template>
