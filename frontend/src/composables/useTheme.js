import { ref, watchEffect } from 'vue'

const theme = ref(document.documentElement.getAttribute('data-theme') || 'light')

watchEffect(() => {
  document.documentElement.setAttribute('data-theme', theme.value)
  localStorage.setItem('expenzo-theme', theme.value)
})

export function useTheme() {
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }
  return { theme, toggleTheme }
}

export function cssVar(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim()
}
