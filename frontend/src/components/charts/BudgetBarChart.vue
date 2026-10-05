<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] }, // [{ label, spent, budget, color }]
})

const animated = ref(false)

function triggerAnimation() {
  animated.value = false
  nextTick(() => requestAnimationFrame(() => (animated.value = true)))
}

function widthPct(item) {
  if (!item.budget) return item.spent > 0 ? 100 : 0
  return Math.min((item.spent / item.budget) * 100, 100)
}

function isOver(item) {
  return item.budget > 0 && item.spent > item.budget
}

onMounted(triggerAnimation)
watch(() => props.items, triggerAnimation, { deep: true })
</script>

<template>
  <div class="budget-bars">
    <div v-for="(item, i) in items" :key="item.label" class="budget-bar-row">
      <div class="d-flex justify-content-between align-items-baseline mb-1">
        <span class="fw-semibold small d-flex align-items-center gap-2">
          <span class="dot" :style="{ backgroundColor: item.color }"></span>
          {{ item.label }}
        </span>
        <span class="small font-mono" :class="isOver(item) ? 'text-over' : 'text-muted-soft'">
          {{ item.spentLabel }} <span v-if="item.budget">/ {{ item.budgetLabel }}</span>
          <i v-if="isOver(item)" class="bi bi-exclamation-triangle-fill ms-1"></i>
        </span>
      </div>
      <div class="budget-track">
        <div
          class="budget-fill"
          :style="{
            width: animated ? widthPct(item) + '%' : '0%',
            backgroundColor: isOver(item) ? 'var(--color-negative)' : item.color,
            transitionDelay: i * 0.06 + 's',
          }"
        ></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.budget-bar-row {
  margin-bottom: 1rem;
}
.budget-bar-row:last-child {
  margin-bottom: 0;
}
.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  display: inline-block;
}
.budget-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: var(--color-border);
  overflow: hidden;
}
.budget-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.85s cubic-bezier(0.4, 0, 0.2, 1);
}
.text-over {
  color: var(--color-negative);
  font-weight: 600;
}
</style>
