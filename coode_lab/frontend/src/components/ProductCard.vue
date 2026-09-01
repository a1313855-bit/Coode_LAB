<script setup>
import { categoryLabel, formatMoney } from '../utils/format'

defineProps({
  product: { type: Object, required: true },
})
const emit = defineEmits(['detail'])
</script>

<template>
  <div class="product-card" @click="emit('detail', product.productId)">
    <div class="thumb">
      <span v-if="!product.imagesJpg" class="thumb-placeholder">{{ product.categoryType }}</span>
      <img v-else :src="product.imagesJpg" :alt="product.name" />
    </div>
    <div class="info">
      <div class="category muted">{{ categoryLabel(product.categoryType) }}</div>
      <div class="name">{{ product.name }}</div>
      <div class="meta muted">
        <span>{{ product.style || '-' }} / {{ product.color || '-' }} / {{ product.size || '-' }}</span>
      </div>
      <div class="price">{{ formatMoney(product.price) }}</div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: 0.15s ease;
}
.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.thumb {
  height: 160px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-placeholder {
  font-size: 40px;
  color: #cbd5e1;
  font-weight: 700;
  text-transform: uppercase;
}
.info {
  padding: 12px 14px;
}
.category {
  font-size: 12px;
}
.name {
  font-weight: 600;
  margin: 2px 0 4px;
  min-height: 22px;
}
.meta {
  font-size: 12px;
}
.price {
  margin-top: 6px;
  font-weight: 700;
  color: var(--c-danger);
}
</style>
