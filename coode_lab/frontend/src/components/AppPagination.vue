<script setup>
defineProps({
  page: { type: Number, default: 0 },
  totalPages: { type: Number, default: 1 },
})
const emit = defineEmits(['change'])
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button class="btn btn-sm" :disabled="page <= 0" @click="emit('change', page - 1)">
      ‹ 上一頁
    </button>
    <span class="page-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
    <button class="btn btn-sm" :disabled="page >= totalPages - 1" @click="emit('change', page + 1)">
      下一頁 ›
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin: 20px 0;
}
.page-info {
  font-size: 14px;
  color: var(--c-text-light);
}
</style>
