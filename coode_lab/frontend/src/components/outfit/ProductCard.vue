<script setup>
import { ref } from 'vue'
import { categoryLabel, formatMoney } from '../../utils/format'

const props = defineProps({
  product: { type: Object, required: true },
  isFavorite: { type: Boolean, default: false },
})
const emit = defineEmits(['try-on', 'favorite', 'detail'])

const imgFailed = ref(false)
</script>

<template>
  <div class="card product-card">
    <div class="thumb" @click="emit('detail', product.productId)">
      <span v-if="!product.imagesJpg || imgFailed" class="thumb-placeholder">
        {{ categoryLabel(product.categoryType) }}
      </span>
      <img
        v-else
        :src="product.imagesJpg"
        :alt="product.name"
        loading="lazy"
        @error="imgFailed = true"
      />
      <button
        class="heart"
        :class="{ active: isFavorite }"
        :title="isFavorite ? '取消收藏' : '收藏'"
        @click.stop="emit('favorite', product.productId)"
      >
        {{ isFavorite ? '♥' : '♡' }}
      </button>
    </div>
    <div class="info">
      <div class="name" :title="product.name" @click="emit('detail', product.productId)">
        {{ product.name }}
      </div>
      <div class="meta muted">{{ product.color || '-' }}</div>
      <div class="bottom">
        <span class="price">{{ formatMoney(product.price) }}</span>
        <button class="tryon" @click="emit('try-on', product)">試穿</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.thumb {
  position: relative;
  aspect-ratio: 4 / 4.4;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-placeholder {
  font-size: 30px;
  font-weight: 700;
  color: #d7d3d3;
  letter-spacing: 2px;
}
.heart {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid var(--c-border);
  background: #fff;
  color: #aaa;
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.heart:hover {
  color: #ec4899;
  border-color: #ec4899;
}
.heart.active {
  color: #ec4899;
  border-color: #ec4899;
}
.info {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.name:hover {
  color: #db2777;
}
.meta {
  font-size: 12px;
}
.bottom {
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  font-weight: 800;
  color: #ec4899;
  font-size: 15px;
}
.tryon {
  border: 1px solid #f9a8d4;
  background: #fdf2f8;
  color: #db2777;
  border-radius: 999px;
  padding: 5px 16px;
  font-size: 13px;
  font-weight: 600;
}
.tryon:hover {
  background: #ec4899;
  color: #fff;
  border-color: #ec4899;
}
</style>