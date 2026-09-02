<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, cartItemApi, cartApi } from '../../api'
import { categoryLabel, formatMoney } from '../../utils/format'
import { currentUserId } from '../../composables/auth'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(true)
const error = ref('')
const message = ref('')
const qty = ref(1)

async function load() {
  try {
    product.value = await productApi.byId(route.params.id)
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
    error.value = '數量超過目前庫存或商品無庫存'
    return
  }
  try {
    // 取得登入會員的購物車（未登入時用測試帳號）
    const cart = await cartApi.findByUserId(currentUserId())
    const cartId = cart.cartId
    await cartItemApi.add({
      cartId,
      productId: product.value.productId,
      productQuantity: qty.value,
    })
    await cartApi.updateTotalQuantity(cartId)
    message.value = '已加入購物車！'
  } catch (e) {
    error.value = '加入失敗：' + e.message
  }
}

function stock() {
  return Number(product.value?.stock || 0)
}

// 數量必須在 1 ~ 目前庫存之間，且要有庫存
function canAdd() {
  return stock() > 0 && Number(qty.value) >= 1 && Number(qty.value) <= stock()
}

onMounted(load)
</script>

<template>
  <div class="container">
    <button class="btn btn-sm" @click="router.push('/store')">← 回到商城</button>

    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="error" class="alert alert-error">{{ error }}</div>

    <div v-else-if="product" class="card detail">
      <div class="detail-thumb">
        <span v-if="!product.imagesJpg" class="big-placeholder">{{ categoryLabel(product.categoryType) }}</span>
        <img v-else :src="product.imagesJpg" :alt="product.name" />
      </div>
      <div class="detail-info">
        <div class="category muted">{{ categoryLabel(product.categoryType) }}</div>
        <h1>{{ product.name }}</h1>
        <div class="price">{{ formatMoney(product.price) }}</div>
        <div class="fields">
          <p><b>廠商：</b>{{ product.vendorName }}</p>
          <p><b>樣式：</b>{{ product.pattern }}</p>
          <p><b>風格：</b>{{ product.style }}</p>
          <p><b>顏色：</b>{{ product.color }}</p>
          <p><b>尺寸：</b>{{ product.size }}</p>
          <p><b>庫存：</b>{{ product.stock }}</p>
        </div>
        <p class="desc">{{ product.description }}</p>

        <div v-if="product.status === 'ACTIVE'" class="buy-row">
          <label>數量</label>
          <input v-model.number="qty" type="number" min="1" :max="stock()" class="qty-input" />
          <button class="btn btn-primary" :disabled="!canAdd()" @click="addToCart">加入購物車</button>
          <span v-if="!canAdd()" class="muted stock-warn">
            超過庫存或無庫存，無法加入
          </span>
        </div>
        <div v-else class="muted">此商品目前暫未上架</div>

        <div v-if="message" class="alert alert-success">{{ message }}</div>
        <div v-if="error" class="alert alert-error">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.detail-thumb {
  background: #f3f4f6;
  border-radius: var(--radius);
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.detail-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.big-placeholder {
  font-size: 90px;
  color: #cbd5e1;
  font-weight: 800;
}
.category {
  color: var(--c-text-light);
}
.detail-info h1 {
  font-size: 26px;
  margin: 4px 0;
}
.price {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-danger);
  margin: 10px 0;
}
.fields p {
  margin: 2px 0;
  font-size: 14px;
}
.desc {
  margin: 12px 0;
  color: var(--c-text-light);
}
.buy-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}
.qty-input {
  width: 70px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.stock-warn {
  color: var(--c-danger);
  font-size: 13px;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
@media (max-width: 700px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>
