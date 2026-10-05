<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'

const props = defineProps({
  points: { type: Array, default: () => [] }, // [{ label, value }]
  height: { type: Number, default: 140 },
})

const animated = ref(false)
const maxValue = computed(() => Math.max(...props.points.map((p) => p.value), 1))

function triggerAnimation() {
  animated.value = false
  nextTick(() => requestAnimationFrame(() => (animated.value = true)))
}

onMounted(triggerAnimation)
watch(() => props.points, triggerAnimation, { deep: true })
</script>

<template>
  <div class="trend-chart" :style="{ height: height + 'px' }">
    <div
      v-for="(p, i) in points"
      :key="p.label"
      class="trend-col"
      :title="`${p.label}: ${p.valueLabel ?? p.value}`"
    >
      <div class="trend-bar-track">
        <div
          class="trend-bar"
          :style="{
            height: animated ? (p.value / maxValue) * 100 + '%' : '0%',
            transitionDelay: i * 0.05 + 's',
          }"
        ></div>
      </div>
      <span class="trend-label">{{ p.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 0.6rem;
  width: 100%;
}
.trend-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  min-width: 0;
}
.trend-bar-track {
  flex: 1;
  width: 100%;
  max-width: 30px;
  display: flex;
  align-items: flex-end;
  margin: 0 auto;
}
.trend-bar {
  width: 100%;
  border-radius: 6px 6px 3px 3px;
  background: linear-gradient(180deg, var(--color-accent-orange), var(--color-primary));
  transition: height 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 3px;
}
.trend-label {
  margin-top: 0.4rem;
  font-size: 0.7rem;
  color: var(--color-muted);
  white-space: nowrap;
}
</style>
