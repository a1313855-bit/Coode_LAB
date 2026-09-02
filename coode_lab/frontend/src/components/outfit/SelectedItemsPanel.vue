<script setup>
import { reactive } from 'vue'
import { SLOTS, SLOT_LABELS, chosenVariantOf, variantColors, variantsByColor } from '../../api/outfitService'
import { formatMoney } from '../../utils/format'

defineProps({
  look: { type: Object, required: true },
  selectedProduct: { type: Object, default: null },
})
const emit = defineEmits(['remove-slot', 'choose-slot', 'view-product', 'preview', 'change-variant', 'save'])

// 縮圖讀取失敗時改用佔位，避免 broken image
const broken = reactive({})
const pickerOpen = reactive({})

function thumbSrc(product) {
  const variant = chosenVariantOf(product)
  const src = variant && variant.imagesJpg ? variant.imagesJpg : (product && product.imagesJpg)
  if (!src) return null
  if (broken[product.productId]) return null
  return src
}

function onThumbError(product) {
  broken[product.productId] = true
}

function togglePicker(slot) {
  pickerOpen[slot] = !pickerOpen[slot]
}

function selectVariant(product, variant) {
  emit('change-variant', product, variant)
}
</script>

<template>
  <section class="panel">
    <header class="panel-head">
      <h2>已選商品</h2>
      <span class="sub">點擊可查看商品頁</span>
    </header>

    <!-- 已選商品：僅顯示實際放入目前穿搭的商品（開始為空白） -->
    <div class="slot-list">
      <template v-for="slot in SLOTS" :key="slot">
        <div v-if="look[slot]" class="slot-row" @click="emit('preview', look[slot])">
          <div class="slot-topline">
            <span class="tag">{{ SLOT_LABELS[slot] }}</span>
            <button
              class="x"
              title="移除"
              @click.stop="emit('remove-slot', slot)"
            >
              ×
            </button>
          </div>

          <div class="slot-body">
            <div class="mini-thumb">
              <span v-if="!thumbSrc(look[slot])" class="mini-ph">{{ SLOT_LABELS[slot] }}</span>
              <img v-else :src="thumbSrc(look[slot])" alt="" @error="onThumbError(look[slot])" />
            </div>
            <div class="mini-info">
              <div class="name" :title="look[slot].name">{{ look[slot].name }}</div>
              <div class="variant-line">
                {{ (chosenVariantOf(look[slot]) || {}).color || '-' }} /
                {{ (chosenVariantOf(look[slot]) || {}).size || '-' }}
              </div>
              <div class="price">{{ formatMoney(look[slot].price) }}</div>
              <button class="link" @click.stop="togglePicker(slot)">
                {{ pickerOpen[slot] ? '收起顏色' : '換顏色' }}
              </button>
              <div v-if="pickerOpen[slot]" class="color-picker" @click.stop>
                <span
                  v-for="c in variantColors(look[slot])"
                  :key="c"
                  class="color-chip"
                  :class="{ active: c === (chosenVariantOf(look[slot]) || {}).color }"
                  @click="selectVariant(look[slot], variantsByColor(look[slot], c).find((v) => v.status === 'ACTIVE') || variantsByColor(look[slot], c)[0])"
                >
                  {{ c }}
                </span>
              </div>
              <button class="link" @click.stop="emit('view-product', look[slot])">
                查看商品頁 →
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- 完全無商品時的空白提示 -->
      <div v-if="!SLOTS.some((s) => look[s])" class="blank-state">
        <p class="muted small">尚無已選商品</p>
        <p class="muted tiny">點左側商品「試穿」加入此處</p>
      </div>
    </div>

    <!-- 商品預覽 -->
    <div v-if="selectedProduct" class="preview">
      <div class="preview-thumb">
        <span v-if="!thumbSrc(selectedProduct) || broken[selectedProduct.productId]" class="big-ph">
          {{ SLOT_LABELS[selectedProduct.categoryType] || selectedProduct.categoryType }}
        </span>
        <img
          v-else
          :src="thumbSrc(selectedProduct)"
          alt=""
          @error="broken[selectedProduct.productId] = true"
        />
      </div>
      <h4>{{ selectedProduct.name }}</h4>
      <div class="preview-price">{{ formatMoney(selectedProduct.price) }}</div>
      <dl class="preview-fields">
        <div><dt>廠商</dt><dd>{{ selectedProduct.vendorName || '-' }}</dd></div>
        <div><dt>顏色</dt><dd>{{ (chosenVariantOf(selectedProduct) || {}).color || '-' }}</dd></div>
        <div><dt>尺寸</dt><dd>{{ (chosenVariantOf(selectedProduct) || {}).size || '-' }}</dd></div>
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
  letter-spacing: 0.04em;
}
.sub {
  display: block;
  font-size: 12px;
  color: var(--muted);
}
.slot-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.slot-row {
  border: 1px solid var(--line);
  border-radius: 4px;
  padding: 10px 12px;
  cursor: pointer;
  background: var(--paper);
  transition: border-color 0.15s ease;
}
.slot-row:hover {
  border-color: var(--ink);
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
  letter-spacing: 0.04em;
  color: var(--ink);
  background: #f2f2f2;
  border-radius: 3px;
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
  color: var(--accent);
}
.slot-body {
  display: flex;
  gap: 10px;
}
.mini-thumb {
  width: 52px;
  height: 60px;
  border-radius: 4px;
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
  color: #b8b4af;
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
  color: var(--ink);
}
.variant-line {
  font-size: 12px;
  color: var(--muted);
}
.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}
.color-chip {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
  background: var(--paper);
  color: var(--ink);
  cursor: pointer;
}
.color-chip.active {
  border-color: var(--ink);
  color: var(--paper);
  background: var(--ink);
}
.link {
  border: none;
  background: none;
  color: var(--muted);
  font-size: 12px;
  padding: 0;
  margin-top: 4px;
}
.link:hover {
  color: var(--ink);
}
.blank-state {
  border: 1px dashed var(--line);
  border-radius: 4px;
  padding: 24px 12px;
  text-align: center;
}
.blank-state .tiny {
  font-size: 12px;
  margin-top: 4px;
}
.preview {
  border-top: 1px dashed var(--line);
  padding-top: 12px;
}
.preview-thumb {
  height: 120px;
  border-radius: 4px;
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
  color: var(--ink);
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
  color: var(--muted);
}
.preview-fields dd {
  font-size: 12px;
  font-weight: 600;
}
.preview-desc {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.btn-outline {
  background: var(--paper);
  color: var(--ink);
  border-color: var(--ink);
}
.btn-outline:hover {
  background: var(--ink);
  color: var(--paper);
}
.btn-save {
  margin-top: auto;
  background: var(--ink);
  border: 1px solid var(--ink);
  color: var(--paper);
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 0.08em;
  padding: 12px;
  border-radius: 4px;
  width: 100%;
}
.btn-save:hover {
  background: var(--ink-2);
  border-color: var(--ink-2);
  color: var(--paper);
}
</style>
