<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

function close() {
  emit('update:modelValue', false)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-backdrop-custom" @click.self="close">
      <div class="modal-box surface-card">
        <div class="d-flex justify-content-between align-items-center px-4 py-3 border-bottom">
          <h5 class="font-display fw-semibold mb-0">{{ title }}</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <div class="p-4">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background: rgba(20, 24, 40, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 1rem;
}
.modal-box {
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}
</style>
