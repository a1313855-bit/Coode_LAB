<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { cartApi, cartItemApi, orderApi } from '../../api'
import { formatMoney } from '../../utils/format'
import { currentUserId } from '../../composables/auth'

const router = useRouter()
// items：目前顯示的列（搜尋後為搜尋結果）
const items = ref([])
// allItems：購物車全部商品（勾選與總價一律以這份為準）
const allItems = ref([])
const cart = ref(null)
const loading = ref(true)
const message = ref('')
const error = ref('')
const searching = ref(false)
const keyword = ref('')

// 選購（勾選）狀態：預設全部勾選
const selected = reactive(new Set())

// 結帳資訊
const showCheckout = ref(false)
const submitting = ref(false)
const checkout = ref({
  userId: currentUserId(),
  recipientName: '',
  recipientPhone: '',
  recipientAddress: '',
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    cart.value = await cartApi.findByUserId(currentUserId())
    const res = await cartItemApi.list(cart.value.cartId, 0)
    allItems.value = res.content || []
    items.value = allItems.value
    searching.value = false
    // 載入後預設全選
    selected.clear()
    allItems.value.forEach((i) => selected.add(i.cartItemId))
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

// 依關鍵字搜尋購物車商品（呼叫 Controller 的 findCartItemByKeyword）
async function search() {
  const kw = keyword.value.trim()
  if (!cart.value || !kw) {
    await load()
    return
  }
  loading.value = true
  error.value = ''
  searching.value = true
  try {
    const res = await cartItemApi.search(cart.value.cartId, kw, 0)
    items.value = res.content || []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function clearSearch() {
  keyword.value = ''
  load()
}

function toggleItem(item) {
  if (selected.has(item.cartItemId)) selected.delete(item.cartItemId)
  else selected.add(item.cartItemId)
}

const allSelected = computed(
  () => items.value.length > 0 && items.value.every((i) => selected.has(i.cartItemId)),
)

function toggleAll() {
  if (allSelected.value) selected.clear()
  else items.value.forEach((i) => selected.add(i.cartItemId))
}

// 被勾選的商品（以購物車全部商品為準）
const selectedItems = computed(() => allItems.value.filter((i) => selected.has(i.cartItemId)))

// 被勾選商品的總價
const selectedSum = computed(() =>
  selectedItems.value.reduce((s, i) => s + Number(i.totalPrice || 0), 0),
)

async function updateQty(item) {
  // 數量連動庫存：最少 1，最多不得超過目前庫存
  if (item.productQuantity == null || item.productQuantity < 1) {
    item.productQuantity = 1
  }
  const maxQty = Number(item.stock || 0)
  if (maxQty > 0 && item.productQuantity > maxQty) {
    item.productQuantity = maxQty
    message.value = `此商品剩餘庫存只有 ${maxQty} 件，數量已自動調整`
    error.value = ''
  } else if (maxQty <= 0) {
    error.value = '此商品已無庫存，請先移除'
    return
  }
  try {
    await cartItemApi.update(item.cartItemId, { productQuantity: item.productQuantity })
    // 重新計算單價
    item.totalPrice = round(item.price * item.productQuantity)
    // 同步整份購物車清單的數量與小計
    const full = allItems.value.find((i) => i.cartItemId === item.cartItemId)
    if (full && full !== item) {
      full.productQuantity = item.productQuantity
      full.totalPrice = item.totalPrice
      full.stock = item.stock
    }
    await cartApi.updateTotalQuantity(cart.value.cartId)
    if (!message.value) message.value = '已更新數量'
  } catch (e) {
    error.value = e.message
  }
}

async function remove(item) {
  try {
    await cartItemApi.remove(cart.value.cartId, item.variantId)
    await cartApi.updateTotalQuantity(cart.value.cartId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function round(n) {
  return Math.round(n * 100) / 100
}

// 該商品目前剩餘庫存（扣除購物車數量）
function stockLeft(item) {
  return Math.max(0, Number(item.stock || 0) - Number(item.productQuantity || 0))
}

// 購物車數量是否已超過庫存
function overStock(item) {
  return Number(item.stock || 0) < Number(item.productQuantity || 0)
}

// 只要有任一被勾選商品超過庫存就不能結帳
const canCheckout = computed(
  () =>
    selectedItems.value.length > 0 &&
    selectedItems.value.every((i) => !overStock(i)),
)

async function submitOrder() {
  error.value = ''
  message.value = ''
  submitting.value = true
  try {
    const order = await orderApi.create({
      userId: Number(checkout.value.userId),
      // 只結帳被勾選的商品；後端會同時建立訂單明細、扣庫存並刪除購物車內容
      cartItemIds: selectedItems.value.map((i) => i.cartItemId),
      recipientName: checkout.value.recipientName,
      recipientPhone: checkout.value.recipientPhone,
      recipientAddress: checkout.value.recipientAddress,
    })
    message.value = `訂單建立成功！訂單編號 #${order.orderId}`
    showCheckout.value = false
    await load()
    router.push('/orders')
  } catch (e) {
    error.value = '結帳失敗：' + e.message
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="cart-page">
    <div class="container-wide">
      <div class="page-header">
        <h1>購物車</h1>
        <p>確認商品與數量，進行結帳</p>
      </div>

      <div v-if="message" class="alert alert-success">{{ message }}</div>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="loading" class="empty">載入中...</div>

      <div v-else-if="allItems.length === 0" class="empty-state">
        <p class="empty-title">購物車是空的</p>
        <p class="muted">去看看今天的新品吧</p>
        <RouterLink to="/store" class="btn btn-primary empty-btn">前往商城</RouterLink>
      </div>

      <div v-else>
        <!-- 關鍵字搜尋 -->
        <div class="search-bar">
          <div class="search-wrap">
            <input
              v-model="keyword"
              type="text"
              placeholder="搜尋購物車商品關鍵字"
              class="search-input"
              @keyup.enter="search"
            />
            <button
              v-if="keyword"
              type="button"
              class="clear-keyword"
              aria-label="清空搜尋文字"
              @click="clearSearch"
            >
              ×
            </button>
          </div>
          <button class="btn btn-primary" @click="search">搜尋</button>
          <span v-if="searching" class="muted search-hint">搜尋「{{ keyword }}」的結果</span>
        </div>

        <div v-if="items.length === 0" class="empty">
          沒有符合「{{ keyword }}」的商品
        </div>

        <template v-else>
          <div class="cart-list">
            <!-- 全選列 -->
            <div class="cart-list-head">
              <label class="check-all">
                <input type="checkbox" :checked="allSelected" @change="toggleAll" />
                全選（顯示列）
              </label>
              <span class="muted small">勾選商品後點「前往結帳」</span>
            </div>

            <div
              v-for="item in items"
              :key="item.cartItemId"
              class="cart-row"
              :class="{ 'row-danger': overStock(item), 'row-muted': !selected.has(item.cartItemId) }"
            >
              <input
                type="checkbox"
                class="row-check"
                :checked="selected.has(item.cartItemId)"
                :disabled="overStock(item)"
                @change="toggleItem(item)"
              />

              <div class="row-thumb">{{ (item.productName || 'C')[0] }}</div>

              <div class="row-main">
                <p class="row-name">{{ item.productName }}</p>
                <p class="row-variant">規格：{{ item.color || '-' }} / {{ item.size || '-' }}</p>
                <p class="row-price">{{ formatMoney(item.price) }}</p>
                <span v-if="item.productStatus !== 'ACTIVE' || item.variantStatus !== 'ACTIVE'" class="off-label-v">
                  停售
                </span>
              </div>

              <div class="row-qty">
                <div class="stepper">
                  <button
                    class="step-btn"
                    aria-label="減少"
                    :disabled="item.productQuantity <= 1 || overStock(item)"
                    @click="item.productQuantity = Math.max(1, item.productQuantity - 1); updateQty(item)"
                  >
                    −
                  </button>
                  <input
                    v-model.number="item.productQuantity"
                    type="number"
                    min="1"
                    :max="item.stock"
                    class="qty-input"
                    @change="updateQty(item)"
                  />
                  <button
                    class="step-btn"
                    aria-label="增加"
                    :disabled="Number(item.productQuantity) >= Number(item.stock || 0)"
                    @click="item.productQuantity = Math.min(Number(item.stock || 0), item.productQuantity + 1); updateQty(item)"
                  >
                    +
                  </button>
                </div>
                <div v-if="overStock(item)" class="stock-over">
                  超過庫存（僅剩 {{ item.stock }} 件）
                </div>
                <div class="stock-left">剩餘庫存 {{ stockLeft(item) }}</div>
              </div>

              <div class="row-sub">
                <span class="sub-label">小計</span>
                <span class="sub-value">{{ formatMoney(item.totalPrice) }}</span>
              </div>

              <button class="btn-remove" aria-label="移除" @click="remove(item)">移除</button>
            </div>
          </div>

          <!-- 結帳摘要（只含勾選商品） -->
          <div class="summary">
            <div class="summary-left">
              <span class="muted">{{ selectedItems.length }} 項商品</span>
              <span class="sum">{{ formatMoney(selectedSum) }}</span>
            </div>
            <div class="summary-right">
              <span v-if="!canCheckout" class="muted stock-over-text">
                請勾選商品，且勾選商品數量不得超過庫存
              </span>
              <button class="btn btn-primary btn-checkout" :disabled="!canCheckout" @click="showCheckout = true">
                前往結帳
              </button>
            </div>
          </div>
        </template>
      </div>

      <!-- 結帳表單 -->
      <div v-if="showCheckout" class="checkout-panel">
        <div class="panel-head">
          <h3>填寫收件資訊</h3>
          <button class="panel-close" aria-label="關閉" @click="showCheckout = false">×</button>
        </div>

        <div class="panel-body">
          <div class="order-summary-box">
            <p class="box-title">訂單明細（僅勾選商品）</p>
            <div v-for="i in selectedItems" :key="i.cartItemId" class="box-line">
              <span class="box-name">{{ i.productName }} × {{ i.productQuantity }}</span>
              <span>{{ formatMoney(i.totalPrice) }}</span>
            </div>
            <div class="box-total">
              <span>合計</span>
              <b>{{ formatMoney(selectedSum) }}</b>
            </div>
          </div>

          <div class="form-field">
            <label>會員 ID</label>
            <input v-model.number="checkout.userId" type="number" readonly class="readonly" />
          </div>
          <div class="form-field">
            <label>收件人</label>
            <input v-model="checkout.recipientName" />
          </div>
          <div class="form-field">
            <label>聯絡電話</label>
            <input v-model="checkout.recipientPhone" />
          </div>
          <div class="form-field">
            <label>收件地址</label>
            <input v-model="checkout.recipientAddress" />
          </div>
          <div class="flex panel-actions">
            <button class="btn btn-primary" :disabled="submitting" @click="submitOrder">
              {{ submitting ? '建立中...' : '確認下單' }}
            </button>
            <button class="btn" @click="showCheckout = false">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container-wide {
  max-width: 1040px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}
.cart-page {
  min-height: 60vh;
}

/* ── 搜尋列 ── */
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fbfaf8;
}
.search-wrap {
  position: relative;
  flex: 1;
}
.search-input {
  width: 100%;
  padding: 9px 34px 9px 12px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
}
.search-input:focus {
  outline: none;
  border-color: var(--ink);
}
.clear-keyword {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: #d3cfc9;
  color: #fff;
  font-size: 14px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
}
.clear-keyword:hover {
  background: var(--ink);
}
.search-hint {
  font-size: 13px;
}

/* ── 購物車列表 ── */
.cart-list {
  border: 1px solid var(--line);
  border-radius: 4px;
  overflow: hidden;
}
.cart-list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #faf9f7;
  border-bottom: 1px solid var(--line);
}
.check-all {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}
.small {
  font-size: 12px;
}
.cart-row {
  display: grid;
  grid-template-columns: 28px 64px 1fr auto auto 48px;
  gap: 16px;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--line);
  background: #fff;
  transition: background 0.15s ease;
}
.cart-row:last-child {
  border-bottom: none;
}
.row-check {
  width: 16px;
  height: 16px;
  accent-color: var(--ink);
}
.row-thumb {
  width: 64px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2efea;
  color: #b9b2a7;
  font-size: 22px;
  font-weight: 800;
  border-radius: 2px;
}
.row-name {
  font-size: 14px;
  font-weight: 600;
}
.row-price {
  font-size: 13px;
  color: var(--muted);
  margin-top: 3px;
}
.row-variant {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
}
.off-label-v {
  display: inline-block;
  margin-top: 6px;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 2px;
}
.stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 4px;
  overflow: hidden;
}
.step-btn {
  width: 30px;
  height: 34px;
  border: none;
  background: #fff;
  font-size: 16px;
  color: var(--ink);
}
.step-btn:disabled {
  color: #cfccc7;
  cursor: not-allowed;
}
.qty-input {
  width: 48px;
  height: 34px;
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
.stock-over {
  color: var(--accent);
  font-size: 12px;
  font-weight: 600;
  margin-top: 4px;
}
.stock-left {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
}
.row-sub {
  text-align: right;
}
.sub-label {
  display: block;
  font-size: 11px;
  letter-spacing: 0.1em;
  color: var(--muted);
}
.sub-value {
  font-size: 15px;
  font-weight: 700;
}
.btn-remove {
  background: none;
  border: none;
  color: var(--muted);
  font-size: 13px;
  text-decoration: underline;
  text-underline-offset: 3px;
  padding: 4px 0;
  cursor: pointer;
}
.btn-remove:hover {
  color: var(--accent);
}
.row-danger {
  background: #fdf3f2;
}
.row-danger .row-thumb {
  background: #f5e0dc;
}
.row-muted {
  opacity: 0.5;
}

/* ── 摘要列 ── */
.summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
}
.summary-left {
  display: flex;
  align-items: baseline;
  gap: 14px;
}
.sum {
  font-size: 26px;
  font-weight: 800;
  color: var(--ink);
}
.summary-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stock-over-text {
  color: var(--accent);
  font-size: 13px;
}
.btn-checkout {
  padding: 12px 28px;
  letter-spacing: 0.14em;
}

/* ── 結帳面板 ── */
.checkout-panel {
  margin-top: 20px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--line);
  background: #faf9f7;
}
.panel-head h3 {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.06em;
}
.panel-close {
  background: none;
  border: none;
  font-size: 22px;
  color: var(--muted);
  line-height: 1;
  cursor: pointer;
}
.panel-body {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 24px;
  padding: 24px;
}
.order-summary-box {
  background: #faf9f7;
  border: 1px solid var(--line);
  border-radius: 4px;
  padding: 16px;
  align-self: start;
}
.box-title {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  margin-bottom: 12px;
}
.box-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  color: #55514c;
  padding: 5px 0;
}
.box-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.box-total {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid var(--line);
  margin-top: 8px;
  padding-top: 12px;
  font-size: 15px;
}
.box-total b {
  font-size: 18px;
  font-weight: 800;
}
.readonly {
  background: #f5f3f0 !important;
}
.panel-actions {
  grid-column: 1 / -1;
}

/* ── 空狀態 ── */
.empty-state {
  text-align: center;
  padding: 80px 0;
}
.empty-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.1em;
  margin-bottom: 6px;
}
.empty-btn {
  margin-top: 24px;
}

/* ── RWD ── */
@media (max-width: 860px) {
  .container-wide {
    padding: 24px 16px 56px;
  }
  .cart-row {
    grid-template-columns: 22px 56px 1fr auto;
    gap: 12px;
  }
  .row-thumb {
    width: 56px;
    height: 70px;
  }
  .row-check {
    grid-column: 1;
    grid-row: 1;
  }
  .row-thumb {
    grid-column: 2;
    grid-row: 1;
  }
  .row-main {
    grid-column: 3;
    grid-row: 1;
  }
  .row-qty {
    grid-column: 4;
    grid-row: 1;
  }
  .btn-remove {
    grid-column: 4;
    grid-row: 2;
    justify-self: end;
  }
  .row-sub {
    display: none;
  }
  .panel-body {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 560px) {
  .summary {
    flex-direction: column;
    align-items: stretch;
  }
  .summary-right {
    flex-direction: column;
    align-items: stretch;
  }
  .btn-checkout {
    width: 100%;
  }
  .cart-row {
    grid-template-columns: 22px 1fr auto;
  }
  .row-check {
    grid-column: 1;
    grid-row: 1;
  }
  .row-thumb {
    display: none;
  }
  .row-main {
    grid-column: 2;
    grid-row: 1;
  }
  .row-qty {
    grid-column: 2;
    grid-row: 2;
    justify-self: start;
  }
  .btn-remove {
    grid-column: 3;
    grid-row: 1;
  }
}
</style>