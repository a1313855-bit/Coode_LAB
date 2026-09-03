<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, cartItemApi, cartApi } from '../../api'
import { categoryLabel, formatMoney, productImageUrl, statusLabel } from '../../utils/format'
import { currentUserId } from '../../composables/auth'
import productPlaceholder from '../../assets/coode-fashion/product-placeholder.svg'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(true)
const error = ref('')
const message = ref('')
const qty = ref(1)
const color = ref('')
const size = ref('')

// 依顏色分組的規格
const colors = computed(() => {
  if (!product.value) return []
  const seen = new Set()
  const list = []
  for (const v of product.value.variants || []) {
    if (!seen.has(v.color)) {
      seen.add(v.color)
      list.push(v.color)
    }
  }
  return list
})

const sizes = computed(() => {
  if (!color.value || !product.value) return []
  return (product.value.variants || []).filter((v) => v.color === color.value)
})

// 目前選中的規格
const selectedVariant = computed(() =>
  (sizes.value || []).find((v) => v.size === size.value && v.status === 'ACTIVE'),
)

function selectColor(c) {
  // 換顏色時清空已選尺寸
  size.value = ''
  color.value = c
}

async function load() {
  try {
    product.value = await productApi.byId(route.params.id)
    // 預設選第一個顏色及其第一個可販售尺寸
    const vs = product.value.variants || []
    if (vs.length > 0) {
      color.value = vs[0].color
      const active = vs.filter((v) => v.color === color.value && v.status === 'ACTIVE')
      size.value = active.length ? active[0].size : ''
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  message.value = ''
  error.value = ''
  if (!canAdd()) {
    error.value = '請選擇在庫且可販售的規格，數量不可超過庫存'
    return
  }
  try {
    // 取得登入會員的購物車（未登入時用測試帳號）
    const cart = await cartApi.findByUserId(currentUserId())
    const cartId = cart.cartId
    await cartItemApi.add({
      cartId,
      variantId: selectedVariant.value.variantId,
      productQuantity: qty.value,
    })
    await cartApi.updateTotalQuantity(cartId)
    message.value = '已加入購物車！'
  } catch (e) {
    error.value = '加入失敗：' + e.message
  }
}

function stock() {
  return Number(selectedVariant.value?.stock || 0)
}

// 數量必須在 1 ~ 目前庫存之間，且要有庫存、規格與商品皆 ACTIVE
function canAdd() {
  if (!selectedVariant.value) return false
  if (product.value.status !== 'ACTIVE') return false
  if (selectedVariant.value.status !== 'ACTIVE') return false
  return stock() > 0 && Number(qty.value) >= 1 && Number(qty.value) <= stock()
}

function imgSrc() {
  return productImageUrl(product.value) || productPlaceholder
}

onMounted(load)
</script>

<template>
  <div class="prod-detail">
    <div class="container-wide">
      <button class="btn-back" @click="router.push('/store')">← 回到商城</button>

      <div v-if="loading" class="empty">載入中...</div>
      <div v-else-if="error" class="alert alert-error">載入失敗：{{ error }}</div>

      <div v-else-if="product" class="detail">
        <div class="detail-thumb zoom-img">
          <img :src="imgSrc()" :alt="product.name" />
          <span v-if="product.status !== 'ACTIVE'" class="off-label">暫未上架</span>
        </div>

        <div class="detail-info">
          <p class="eyebrow">{{ categoryLabel(product.categoryType) }}</p>
          <h1>{{ product.name }}</h1>
          <div class="price">{{ formatMoney(product.price) }}</div>

          <div class="fields">
            <div class="field-item"><span>版型</span><b>{{ product.pattern ? statusLabel(product.pattern) : '-' }}</b></div>
            <div class="field-item"><span>風格</span><b>{{ product.style || '-' }}</b></div>
            <div class="field-item"><span>分類</span><b>{{ categoryLabel(product.categoryType) }}</b></div>
            <div class="field-item"><span>廠商</span><b>{{ product.vendorName || '-' }}</b></div>
          </div>

          <div class="variant-picker">
            <div class="vp-label">顏色</div>
            <div class="vp-options">
              <button
                v-for="c in colors"
                :key="c"
                type="button"
                class="vp-chip"
                :class="{ active: color === c }"
                @click="selectColor(c)"
              >
                {{ c }}
              </button>
            </div>

            <div class="vp-label">尺寸</div>
            <div class="vp-options">
              <button
                v-for="v in sizes"
                :key="v.variantId"
                type="button"
                class="vp-chip"
                :class="{ active: size === v.size, disabled: v.status !== 'ACTIVE' }"
                :disabled="v.status !== 'ACTIVE'"
                @click="size = v.size"
              >
                {{ v.size }}<span v-if="v.status !== 'ACTIVE'" class="chip-off">停售</span>
              </button>
              <span v-if="sizes.length === 0" class="muted small">此顏色暫無規格</span>
            </div>

            <div class="vp-stock">
              已選：{{ size || '未選擇' }}（<span :class="{ danger: selectedVariant && selectedVariant.stock === 0 }">
                庫存 {{ stock() }} 件
              </span>）
              <span v-if="selectedVariant && selectedVariant.stock === 0" class="muted small">已售罄</span>
            </div>
          </div>

          <p class="desc">{{ product.description || '暫無商品說明' }}</p>

          <div v-if="product.status === 'ACTIVE'" class="buy-row">
            <label class="qty-label">數量</label>
            <div class="stepper">
              <button class="step-btn" aria-label="減少" :disabled="qty <= 1" @click="qty = Math.max(1, qty - 1)">−</button>
              <input v-model.number="qty" type="number" min="1" :max="stock()" class="qty-input" />
              <button class="step-btn" aria-label="增加" :disabled="qty >= stock()" @click="qty = Math.min(stock(), qty + 1)">+</button>
            </div>
            <button class="btn btn-primary btn-lg" :disabled="!canAdd()" @click="addToCart">加入購物車</button>
            <span v-if="!canAdd()" class="stock-warn">請選擇有庫存且未停售的規格</span>
          </div>
          <div v-else class="off-note">此商品目前暫未上架</div>

          <div v-if="message" class="alert alert-success">{{ message }}</div>
          <div v-if="error" class="alert alert-error">{{ error }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container-wide {
  max-width: 1240px;
  margin: 0 auto;
  padding: 32px 24px 72px;
}
.btn-back {
  background: none;
  border: none;
  color: var(--muted);
  font-size: 13px;
  letter-spacing: 0.04em;
  padding: 0 0 20px;
  cursor: pointer;
}
.btn-back:hover {
  color: var(--ink);
}

.detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 56px;
  align-items: start;
}
.detail-thumb {
  position: relative;
  aspect-ratio: 4 / 5;
  background: #f2efea;
  border-radius: 2px;
}
.detail-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.off-label {
  position: absolute;
  top: 14px;
  left: 14px;
  background: var(--ink);
  color: #fff;
  font-size: 11px;
  letter-spacing: 0.14em;
  padding: 5px 12px;
  border-radius: 2px;
}

.detail-info {
  padding-top: 8px;
}
.eyebrow {
  font-size: 12px;
  letter-spacing: 0.28em;
  color: var(--accent);
  font-weight: 700;
  margin-bottom: 10px;
}
.detail-info h1 {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 0.02em;
  line-height: 1.25;
}
.price {
  font-size: 24px;
  font-weight: 800;
  color: var(--ink);
  margin: 14px 0 24px;
}

.fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  padding: 20px 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
}
.field-item span {
  display: block;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: var(--muted);
  margin-bottom: 2px;
}
.field-item b {
  font-size: 14px;
  font-weight: 600;
}
.desc {
  margin: 20px 0;
  color: #55514c;
  font-size: 14px;
  line-height: 1.9;
}

/* ── 規格選擇器 ── */
.variant-picker {
  padding: 18px 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
}
.vp-label {
  font-size: 11px;
  letter-spacing: 0.12em;
  color: var(--muted);
  margin: 10px 0 8px;
}
.vp-label:first-child {
  margin-top: 0;
}
.vp-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}
.vp-chip {
  min-width: 44px;
  padding: 8px 14px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
  font-size: 13px;
  color: var(--ink);
  cursor: pointer;
  position: relative;
  transition: all 0.15s ease;
}
.vp-chip:hover:not(:disabled) {
  border-color: var(--ink);
}
.vp-chip.active {
  border-color: var(--ink);
  background: var(--ink);
  color: #fff;
}
.vp-chip.disabled {
  color: #c9c6c1;
  background: #f5f3f0;
  cursor: not-allowed;
  text-decoration: line-through;
}
.chip-off {
  font-size: 10px;
  margin-left: 4px;
}
.vp-stock {
  font-size: 13px;
  color: var(--muted);
  margin-top: 8px;
}
.vp-stock .danger {
  color: var(--accent);
  font-weight: 600;
}

.buy-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}
.qty-label {
  font-size: 13px;
  letter-spacing: 0.08em;
  color: var(--muted);
}
.stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 4px;
  overflow: hidden;
}
.step-btn {
  width: 36px;
  height: 42px;
  border: none;
  background: #fff;
  font-size: 18px;
  color: var(--ink);
  transition: background 0.15s ease;
}
.step-btn:hover:not(:disabled) {
  background: var(--paper-soft);
}
.step-btn:disabled {
  color: #c9c6c1;
  cursor: not-allowed;
}
.step-btn:disabled:hover:not(:disabled) {
  background: #fff;
}
.qty-input {
  width: 56px;
  height: 42px;
  border: none;
  border-left: 1px solid var(--line);
  border-right: 1px solid var(--line);
  text-align: center;
  font-size: 14px;
  -moz-appearance: textfield;
  appearance: textfield;
}
.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.btn-lg {
  padding: 12px 34px;
  letter-spacing: 0.16em;
}
.stock-warn {
  width: 100%;
  color: var(--accent);
  font-size: 13px;
}
.off-note {
  color: var(--muted);
  font-size: 14px;
}

@media (max-width: 820px) {
  .detail {
    grid-template-columns: 1fr;
    gap: 28px;
  }
  .container-wide {
    padding: 20px 16px 48px;
  }
}
@media (max-width: 480px) {
  .fields {
    grid-template-columns: 1fr;
  }
  .buy-row {
    align-items: stretch;
  }
  .btn-lg {
    width: 100%;
  }
}
</style>