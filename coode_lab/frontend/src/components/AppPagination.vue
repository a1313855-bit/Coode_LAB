<script setup>
defineProps({
  page: { type: Number, default: 0 },
  totalPages: { type: Number, default: 1 },
})
const emit = defineEmits(['change'])
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      class="page-btn"
      :disabled="page <= 0"
      aria-label="上一頁"
      @click="emit('change', page - 1)"
    >
      ←
    </button>
    <span class="page-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
    <button
      class="page-btn"
      :disabled="page >= totalPages - 1"
      aria-label="下一頁"
      @click="emit('change', page + 1)"
    >
      →
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 28px 0 8px;
}
.page-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
  font-size: 15px;
  border-radius: 4px;
  transition: all 0.15s ease;
}
.page-btn:hover:not(:disabled) {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}
.page-btn:disabled {
  color: #cfccc7;
  cursor: not-allowed;
  background: #fff;
}
.page-info {
  font-size: 13px;
  color: var(--muted);
  letter-spacing: 0.06em;
}
</style>
