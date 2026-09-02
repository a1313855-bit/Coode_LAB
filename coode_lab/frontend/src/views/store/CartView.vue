<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi, cartItemApi, orderApi } from '../../api'
import { formatMoney } from '../../utils/format'
import { currentUserId } from '../../composables/auth'

const router = useRouter()
// items：目前顯示在表格中的列（搜尋後為搜尋結果）
const items = ref([])
// allItems：購物車全部商品（選購勾選與總價一律以這份為準）
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
    await cartItemApi.remove(cart.value.cartId, item.productId)
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
  }
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header">
      <h1>購物車</h1>
      <p>確認商品與數量，進行結帳</p>
    </div>

    <div v-if="message " class="alert alert-success">{{ message }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else-if="allItems.length === 0" class="empty">
      購物車是空的，<RouterLink to="/store">去逛逛</RouterLink>
    </div>

    <div v-else>
      <!-- 關鍵字搜尋 -->
      <div class="card search-bar">
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
      <div class="card">
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th class="check-col">
                  <input type="checkbox" :checked="allSelected" @change="toggleAll" />
                </th>
                <th>商品</th>
                <th>單價</th>
                <th>數量</th>
                <th>庫存</th>
                <th>小計</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in items"
                :key="item.cartItemId"
                :class="{ 'row-danger': overStock(item), 'row-muted': !selected.has(item.cartItemId) }"
              >
                <td class="check-col">
                  <input
                    type="checkbox"
                    :checked="selected.has(item.cartItemId)"
                    @change="toggleItem(item)"
                    :disabled="overStock(item)"
                  />
                </td>
                <td>{{ item.productName }}</td>
                <td>{{ formatMoney(item.price) }}</td>
                <td>
                  <input
                    v-model.number="item.productQuantity"
                    type="number"
                    min="1"
                    :max="item.stock"
                    class="qty"
                    @change="updateQty(item)"
                  />
                </td>
                <td>
                  <div v-if="overStock(item)" class="stock-over">
                    超過庫存（僅剩 {{ item.stock }} 件）
                  </div>
                  <div class="stock-left">剩餘庫存 {{ stockLeft(item) }}</div>
                </td>
                <td>{{ formatMoney(item.totalPrice) }}</td>
                <td>
                  <button class="btn btn-danger btn-sm" @click="remove(item)">移除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card checkout-bar">
        <div class="flex-between">
          <div>
            <span class="muted">{{ selectedItems.length }} 項商品</span>
            <span class="sum">{{ formatMoney(selectedSum) }}</span>
          </div>
          <div class="checkout-right">
            <span v-if="!canCheckout" class="muted stock-over-text">
              請勾選商品，且勾選商品數量不得超過庫存
            </span>
            <button class="btn btn-primary" :disabled="!canCheckout" @click="showCheckout = true">
              前往結帳
            </button>
          </div>
        </div>
      </div>

      <!-- 結帳表單 -->
      <div v-if="showCheckout" class="card">
        <h3>填寫收件資訊</h3>
        <div class="form-field">
          <label>會員 ID</label>
          <input v-model.number="checkout.userId" type="number" />
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
        <div class="flex">
          <button class="btn btn-primary" @click="submitOrder">確認下單</button>
          <button class="btn" @click="showCheckout = false">取消</button>
        </div>
      </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px 16px;
}
.search-wrap {
  position: relative;
  flex: 1;
}
.search-bar .search-input {
  width: 100%;
  padding: 8px 32px 8px 10px;
  box-sizing: border-box;
  border: 1px solid var(--c-border);
  border-radius: 6px;
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
  background: #d1d5db;
  color: #fff;
  font-size: 14px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
}
.clear-keyword:hover {
  background: #9ca3af;
}
.search-hint {
  font-size: 13px;
}
.qty {
  width: 60px;
  padding: 6px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
}
.sum {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-danger);
  margin-left: 10px;
}
.checkout-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stock-over {
  color: var(--c-danger);
  font-size: 13px;
  font-weight: 600;
}
.stock-left {
  font-size: 13px;
  color: var(--c-text-light);
}
.stock-over-text {
  color: var(--c-danger);
}
.row-danger td {
  background: rgba(239, 68, 68, 0.06);
}
.check-col {
  width: 36px;
  text-align: center;
}
.row-muted td {
  opacity: 0.55;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
