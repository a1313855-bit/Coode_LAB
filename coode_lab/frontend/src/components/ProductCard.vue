<script setup>
import { computed } from 'vue'
import productPlaceholder from '../assets/coode-fashion/product-placeholder.svg'
import { categoryLabel, formatMoney, productImageUrl } from '../utils/format'

const props = defineProps({
  product: { type: Object, required: true },
})
const emit = defineEmits(['detail'])

// 總庫存 = 所有規格庫存加總
const totalStock = computed(() =>
  (props.product.variants || []).reduce((s, v) => s + Number(v.stock || 0), 0),
)
// 所有顏色（依規格去重）
const allColors = computed(() => {
  const seen = new Set()
  ;(props.product.variants || []).forEach((v) => v.color && seen.add(v.color))
  return [...seen]
})

function imgSrc() {
  return productImageUrl(props.product) || productPlaceholder
}
function clickCard() {
  emit('detail', props.product.productId)
}
</script>

<template>
  <article class="product-card" @click="clickCard">
    <div class="thumb zoom-img">
      <img :src="imgSrc()" :alt="product.name" loading="lazy" />
      <!-- Quick View -->
      <div class="quickview" @click.stop="clickCard">
        <span>Quick View</span>
      </div>
      <span v-if="totalStock <= 0" class="soldout">售完</span>
      <span v-else-if="totalStock < 5" class="lowstock">僅剩 {{ totalStock }} 件</span>
    </div>
    <div class="info">
      <div class="cat">{{ categoryLabel(product.categoryType) }}</div>
      <h3 class="name">{{ product.name }}</h3>
      <div class="meta">{{ [product.style, allColors.join('/')].filter(Boolean).join(' / ') }}</div>
      <div class="price">{{ formatMoney(product.price) }}</div>
    </div>
  </article>
</template>

<style scoped>
.product-card {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  background: transparent;
}

/* 4:5 圖片 */
.thumb {
  position: relative;
  aspect-ratio: 4 / 5;
  background: #f2efea;
  border-radius: 2px;
  overflow: hidden;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.quickview {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 42px;
  background: rgba(23, 23, 23, 0.9);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.25s ease;
}
.product-card:hover .quickview {
  opacity: 1;
  transform: translateY(0);
}
.soldout,
.lowstock {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #fff;
  color: var(--ink);
  font-size: 11px;
  letter-spacing: 0.08em;
  padding: 3px 9px;
  border-radius: 2px;
}
.soldout {
  background: var(--ink);
  color: #fff;
}

.info {
  padding: 12px 2px 6px;
}
.cat {
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--muted);
  margin-bottom: 4px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.price {
  margin-top: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}
</style>