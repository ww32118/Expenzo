<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'

const props = defineProps({
  series: { type: Array, default: () => [] }, // [{ label, value, color }]
  size: { type: Number, default: 200 },
  thickness: { type: Number, default: 26 },
  centerLabel: { type: String, default: 'Total' },
  centerValue: { type: String, default: '' },
})

const radius = computed(() => props.size / 2 - props.thickness / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)
const total = computed(() => props.series.reduce((s, d) => s + (d.value || 0), 0) || 1)

const activeIndex = ref(-1)
const animated = ref(false)

const segments = computed(() => {
  let cumulative = 0
  return props.series.map((d) => {
    const pct = (d.value || 0) / total.value
    const length = animated.value ? pct * circumference.value : 0
    const offset = -cumulative * circumference.value
    cumulative += pct
    return { ...d, length, offset, pct }
  })
})

function triggerAnimation() {
  animated.value = false
  nextTick(() => requestAnimationFrame(() => (animated.value = true)))
}

onMounted(triggerAnimation)
watch(() => props.series, triggerAnimation, { deep: true })
</script>

<template>
  <div class="donut-chart-wrap">
    <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
      <circle
        :cx="size / 2"
        :cy="size / 2"
        :r="radius"
        fill="none"
        stroke="var(--color-border)"
        :stroke-width="thickness"
      />
      <g :transform="`rotate(-90 ${size / 2} ${size / 2})`">
        <circle
          v-for="(seg, i) in segments"
          :key="seg.label"
          :cx="size / 2"
          :cy="size / 2"
          :r="radius"
          fill="none"
          :stroke="seg.color"
          :stroke-width="activeIndex === i ? thickness + 4 : thickness"
          stroke-linecap="round"
          :style="{
            strokeDasharray: `${seg.length}px ${circumference - seg.length}px`,
            strokeDashoffset: `${seg.offset}px`,
            transition: `stroke-dasharray 0.9s cubic-bezier(.4,0,.2,1) ${i * 0.08}s, stroke-width 0.2s ease`,
            cursor: 'pointer',
          }"
          @mouseenter="activeIndex = i"
          @mouseleave="activeIndex = -1"
        />
      </g>
    </svg>
    <div class="donut-center text-center">
      <template v-if="activeIndex >= 0 && segments[activeIndex]">
        <p class="donut-center-label mb-0">{{ segments[activeIndex].label }}</p>
        <p class="donut-center-value mb-0">{{ segments[activeIndex].valueLabel }}</p>
      </template>
      <template v-else>
        <p class="donut-center-label mb-0">{{ centerLabel }}</p>
        <p class="donut-center-value mb-0">{{ centerValue }}</p>
      </template>
    </div>
  </div>
</template>

<style scoped>
.donut-chart-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  max-width: 70%;
}
.donut-center-label {
  font-size: 0.75rem;
  color: var(--color-muted);
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.03em;
}
.donut-center-value {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 1.1rem;
  color: var(--color-ink);
}
</style>
