<script setup>
import { reactive } from 'vue'
import { SLOTS, SLOT_LABELS } from '../../api/outfitService'
import { formatMoney } from '../../utils/format'

defineProps({
  look: { type: Object, required: true },
  selectedProduct: { type: Object, default: null },
})
const emit = defineEmits(['remove-slot', 'choose-slot', 'view-product', 'preview', 'save'])

// 縮圖讀取失敗時改用佔位，避免 broken image
const broken = reactive({})

function thumbSrc(product) {
  if (!product || !product.imagesJpg) return null
  if (broken[product.productId]) return null
  return product.imagesJpg
}

function onThumbError(product) {
  broken[product.productId] = true
}
</script>

<template>
  <section class="panel">
    <header class="panel-head">
      <h2>已選商品</h2>
      <span class="sub">點擊可查看商品頁</span>
    </header>

    <div class="slot-list">
      <div
        v-for="slot in SLOTS"
        :key="slot"
        class="slot-row"
        :class="{ empty: !look[slot] }"
        @click="look[slot] ? emit('preview', look[slot]) : emit('choose-slot', slot)"
      >
        <div class="slot-topline">
          <span class="tag">{{ SLOT_LABELS[slot] }}</span>
          <button
            v-if="look[slot]"
            class="x"
            title="移除"
            @click.stop="emit('remove-slot', slot)"
          >
            ×
          </button>
        </div>

        <div v-if="look[slot]" class="slot-body">
          <div class="mini-thumb">
            <span v-if="!thumbSrc(look[slot])" class="mini-ph">{{ SLOT_LABELS[slot] }}</span>
            <img v-else :src="thumbSrc(look[slot])" alt="" @error="onThumbError(look[slot])" />
          </div>
          <div class="mini-info">
            <div class="name" :title="look[slot].name">{{ look[slot].name }}</div>
            <div class="price">{{ formatMoney(look[slot].price) }}</div>
            <button class="link" @click.stop="emit('view-product', look[slot])">
              查看商品頁 →
            </button>
          </div>
        </div>

        <div v-else class="slot-empty">
          <span class="muted small">尚未選擇{{ SLOT_LABELS[slot] }}</span>
          <button class="link" @click.stop="emit('choose-slot', slot)">選擇商品</button>
        </div>
      </div>
    </div>

    <!-- 商品預覽 -->
    <div v-if="selectedProduct" class="preview">
      <div class="preview-thumb">
        <span v-if="!selectedProduct.imagesJpg || broken[selectedProduct.productId]" class="big-ph">
          {{ SLOT_LABELS[selectedProduct.categoryType] || selectedProduct.categoryType }}
        </span>
        <img
          v-else
          :src="selectedProduct.imagesJpg"
          alt=""
          @error="broken[selectedProduct.productId] = true"
        />
      </div>
      <h4>{{ selectedProduct.name }}</h4>
      <div class="preview-price">{{ formatMoney(selectedProduct.price) }}</div>
      <dl class="preview-fields">
        <div><dt>廠商</dt><dd>{{ selectedProduct.vendorName || '-' }}</dd></div>
        <div><dt>顏色</dt><dd>{{ selectedProduct.color || '-' }}</dd></div>
        <div><dt>尺寸</dt><dd>{{ selectedProduct.size || '-' }}</dd></div>
      </dl>
      <p class="preview-desc">{{ selectedProduct.description || '' }}</p>
      <button class="btn btn-block btn-outline" @click="emit('view-product', selectedProduct)">
        查看完整商品 →
      </button>
    </div>

    <button class="btn btn-save" @click="emit('save')">儲存穿搭</button>
  </section>
</template>

<style scoped>
.panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.panel-head h2 {
  font-size: 18px;
}
.sub {
  display: block;
  font-size: 12px;
  color: var(--c-text-light);
}
.slot-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.slot-row {
  border: 1px solid var(--c-border);
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  background: #fff;
}
.slot-row:hover {
  border-color: #f9a8d4;
}
.slot-row.empty {
  background: #fdfdfd;
}
.slot-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.tag {
  font-size: 12px;
  font-weight: 700;
  color: #db2777;
  background: #fdf2f8;
  border-radius: 6px;
  padding: 2px 8px;
}
.x {
  border: none;
  background: transparent;
  font-size: 16px;
  color: #b6b0b0;
  line-height: 1;
}
.x:hover {
  color: #ef4444;
}
.slot-body {
  display: flex;
  gap: 10px;
}
.mini-thumb {
  width: 52px;
  height: 60px;
  border-radius: 8px;
  background: #f6f6f6;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.mini-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.mini-ph {
  font-size: 11px;
  color: #c2bebe;
}
.mini-info {
  min-width: 0;
}
.name {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.price {
  font-size: 13px;
  font-weight: 800;
  color: #ec4899;
}
.link {
  border: none;
  background: none;
  color: var(--c-text-light);
  font-size: 12px;
  padding: 0;
  margin-top: 4px;
}
.link:hover {
  color: #db2777;
}
.slot-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.preview {
  border-top: 1px dashed var(--c-border);
  padding-top: 12px;
}
.preview-thumb {
  height: 120px;
  border-radius: 10px;
  background: #f6f6f6;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 8px;
}
.preview-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.big-ph {
  font-size: 26px;
  color: #d7d0d0;
  font-weight: 700;
}
.preview h4 {
  font-size: 15px;
}
.preview-price {
  font-size: 17px;
  font-weight: 800;
  color: #ec4899;
  margin: 2px 0 8px;
}
.preview-fields {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 6px;
}
.preview-fields dt {
  font-size: 11px;
  color: var(--c-text-light);
}
.preview-fields dd {
  font-size: 12px;
  font-weight: 600;
}
.preview-desc {
  font-size: 12px;
  color: var(--c-text-light);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.btn-outline {
  background: #fff;
  color: #db2777;
  border-color: #f9a8d4;
}
.btn-outline:hover {
  background: #fdf2f8;
  color: #db2777;
}
.btn-save {
  margin-top: auto;
  background: #ec4899;
  border: 1px solid #ec4899;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  padding: 12px;
  border-radius: 10px;
  width: 100%;
}
.btn-save:hover {
  background: #db2777;
  border-color: #db2777;
}
</style>