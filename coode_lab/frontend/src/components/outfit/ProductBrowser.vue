<script setup>
import ProductCard from './ProductCard.vue'

defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  activeCategory: { type: String, default: 'ALL' },
  sortOption: { type: String, default: 'newest' },
  favoriteIds: { type: Array, default: () => [] },
})
const emit = defineEmits([
  'update:activeCategory',
  'update:sortOption',
  'try-on',
  'favorite',
  'detail',
])

const categories = [
  { key: 'ALL', label: '全部' },
  { key: 'TOP', label: '上衣' },
  { key: 'BOTTOM', label: '下身' },
  { key: 'OUTER', label: '外套' },
  { key: 'SHOES', label: '鞋子' },
  { key: 'ACCESSORY', label: '配件' },
]
</script>

<template>
  <section class="browser">
    <header class="browser-head">
      <h1>試衣間</h1>
      <p>挑選喜歡的單品，打造屬於你的穿搭風格！</p>
    </header>

    <div class="controls">
      <div class="chips">
        <button
          v-for="c in categories"
          :key="c.key"
          class="chip"
          :class="{ active: activeCategory === c.key }"
          @click="emit('update:activeCategory', c.key)"
        >
          {{ c.label }}
        </button>
      </div>
      <select
        class="sort"
        :value="sortOption"
        @change="emit('update:sortOption', $event.target.value)"
      >
        <option value="newest">最新上架</option>
        <option value="priceAsc">價格低 → 高</option>
        <option value="priceDesc">價格高 → 低</option>
      </select>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="grid">
      <div v-for="n in 6" :key="n" class="skeleton card"></div>
    </div>

    <div v-else-if="products.length === 0" class="empty">
      目前沒有符合條件的商品
    </div>

    <div v-else class="grid">
      <ProductCard
        v-for="p in products"
        :key="p.productId"
        :product="p"
        :is-favorite="favoriteIds.includes(p.productId)"
        @try-on="(product) => emit('try-on', product)"
        @favorite="(id) => emit('favorite', id)"
        @detail="(id) => emit('detail', id)"
      />
    </div>
  </section>
</template>

<style scoped>
.browser {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}
.browser-head h1 {
  font-size: 24px;
  letter-spacing: 1px;
}
.browser-head p {
  margin-top: 4px;
  color: var(--c-text-light);
  font-size: 13px;
}
.controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin: 14px 0;
}
.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  border: 1px solid var(--c-border);
  background: #fff;
  color: var(--c-text);
  border-radius: 999px;
  padding: 5px 14px;
  font-size: 13px;
}
.chip:hover {
  border-color: #f9a8d4;
  color: #db2777;
}
.chip.active {
  background: #ec4899;
  border-color: #ec4899;
  color: #fff;
  font-weight: 600;
}
.sort {
  margin-left: auto;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  padding: 5px 8px;
  background: #fff;
  font-size: 13px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
  overflow-y: auto;
  padding-right: 4px;
  flex: 1;
  min-height: 0;
}
.skeleton {
  height: 230px;
  background: linear-gradient(90deg, #f3f4f6, #fafafa, #f3f4f6);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}
@keyframes shimmer {
  from {
    background-position: 200% 0;
  }
  to {
    background-position: -200% 0;
  }
}
</style>