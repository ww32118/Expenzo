// src/composables/useThemeColors.js
import { ref, onMounted, onUnmounted, computed } from 'vue'

/**
 * Reactive theme-aware color palettes.
 * Watches <html data-theme="light|dark"> and returns matching colors.
 *
 * Light Palette: #485696 · #E7E7E7 · #F9C784 · #FC7A1E · #F24C00
 * Dark  Palette: #273043 · #9197AE · #EFF6EE · #F02D3A · #DD0426
 */
export function useThemeColors() {
  const currentTheme = ref(
    document.documentElement.getAttribute('data-theme') || 'light'
  )

  let observer = null

  onMounted(() => {
    observer = new MutationObserver((mutations) => {
      mutations.forEach((m) => {
        if (m.attributeName === 'data-theme') {
          currentTheme.value =
            document.documentElement.getAttribute('data-theme') || 'light'
        }
      })
    })
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme'],
    })
  })

  onUnmounted(() => {
    if (observer) observer.disconnect()
  })

  const isDark = computed(() => currentTheme.value === 'dark')

  // ─── Chart Palette (Donut, Bar, Pie, etc.) ───────────────────
  const chartPalette = computed(() =>
    isDark.value
      ? [
          '#F02D3A', // flare red
          '#DD0426', // deep red
          '#9197AE', // muted slate
          '#EFF6EE', // cream
          '#F9C784', // amber accent
        ]
      : [
          '#485696', // indigo primary
          '#FC7A1E', // orange
          '#F9C784', // amber
          '#F24C00', // flare
          '#8993C4', // soft indigo
        ]
  )

  // ─── Semantic Colors ─────────────────────────────────────────
  const colors = computed(() =>
    isDark.value
      ? {
          // Backgrounds
          canvas: '#1D2434',
          surface: '#273043',
          surfaceRaised: '#2E3850',

          // Text
          ink: '#EFF6EE',
          inkSoft: '#CDD3EA',
          muted: '#9197AE',

          // Accents
          primary: '#9197AE',
          accent: '#F02D3A',
          accentDeep: '#DD0426',
          accentSoft: '#F9C784',

          // Status
          positive: '#4FBF82',
          negative: '#F02D3A',
          warning: '#F9C784',

          // Borders / dividers
          border: 'rgba(145, 151, 174, 0.20)',
          divider: 'rgba(239, 246, 238, 0.08)',

          // Chart-specific
          axis: '#9197AE',
          grid: 'rgba(145, 151, 174, 0.15)',
          tooltipBg: '#2E3850',
          tooltipText: '#EFF6EE',

          // Track (progress bar background)
          track: 'rgba(145, 151, 174, 0.25)',
        }
      : {
          // Backgrounds
          canvas: '#E7E7E7',
          surface: '#FFFFFF',
          surfaceRaised: '#FFFFFF',

          // Text
          ink: '#22284A',
          inkSoft: '#485696',
          muted: '#6A6F8F',

          // Accents
          primary: '#485696',
          accent: '#FC7A1E',
          accentDeep: '#F24C00',
          accentSoft: '#F9C784',

          // Status
          positive: '#2F8F5B',
          negative: '#F24C00',
          warning: '#FC7A1E',

          // Borders / dividers
          border: 'rgba(72, 86, 150, 0.14)',
          divider: 'rgba(72, 86, 150, 0.10)',

          // Chart-specific
          axis: '#6A6F8F',
          grid: 'rgba(72, 86, 150, 0.10)',
          tooltipBg: '#22284A',
          tooltipText: '#FFFFFF',

          // Track
          track: 'rgba(72, 86, 150, 0.15)',
        }
  )

  // ─── Gradients ───────────────────────────────────────────────
  const gradients = computed(() =>
    isDark.value
      ? {
          hero: 'linear-gradient(120deg, #1D2434 0%, #DD0426 100%)',
          card: 'linear-gradient(145deg, #273043 0%, #2E3850 100%)',
          accent: 'linear-gradient(90deg, #F02D3A 0%, #DD0426 100%)',
        }
      : {
          hero: 'linear-gradient(120deg, #485696 0%, #FC7A1E 100%)',
          card: 'linear-gradient(145deg, #FFFFFF 0%, #F9F9FB 100%)',
          accent: 'linear-gradient(90deg, #FC7A1E 0%, #F24C00 100%)',
        }
  )

  return {
    currentTheme,
    isDark,
    chartPalette,
    colors,
    gradients,
  }
}