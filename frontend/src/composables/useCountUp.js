import { ref, watch, onMounted } from 'vue'

/**
 * Animates a number from 0 up to `target` (a ref or getter) over `duration` ms.
 * Returns a ref with the current animated value — bind it straight into your template.
 */
export function useCountUp(targetRef, duration = 900) {
  const current = ref(0)
  let rafId = null

  function animateTo(target) {
    cancelAnimationFrame(rafId)
    const start = current.value
    const delta = target - start
    const startTime = performance.now()

    function tick(now) {
      const elapsed = now - startTime
      const progress = Math.min(elapsed / duration, 1)
      // ease-out cubic
      const eased = 1 - Math.pow(1 - progress, 3)
      current.value = start + delta * eased
      if (progress < 1) {
        rafId = requestAnimationFrame(tick)
      } else {
        current.value = target
      }
    }
    rafId = requestAnimationFrame(tick)
  }

  onMounted(() => animateTo(typeof targetRef === 'function' ? targetRef() : targetRef.value))

  watch(
    () => (typeof targetRef === 'function' ? targetRef() : targetRef.value),
    (val) => animateTo(val || 0),
  )

  return current
}
