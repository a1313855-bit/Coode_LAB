<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi, cartItemApi, orderApi } from '../../api'
import { formatMoney } from '../../utils/format'
import { currentUserId } from '../../composables/auth'

const router = useRouter()
const items = ref([])
const cart = ref(null)
const loading = ref(true)
const message = ref('')
const error = ref('')

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
    items.value = res.content || []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function updateQty(item) {
  try {
    await cartItemApi.update(item.cartItemId, { productQuantity: item.productQuantity })
    // 重新計算單價
    item.totalPrice = round(item.price * item.productQuantity)
    await cartApi.updateTotalQuantity(cart.value.cartId)
    message.value = '已更新數量'
  } catch (e) {
    error.value = e.message
  }
}

async function remove(item) {
  try {
    await cartItemApi.remove(item.cartItemId)
    await cartApi.updateTotalQuantity(cart.value.cartId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function round(n) {
  return Math.round(n * 100) / 100
}

function total() {
  return items.value.reduce((s, i) => s + Number(i.totalPrice || 0), 0)
}

async function submitOrder() {
  error.value = ''
  message.value = ''
  try {
    const order = await orderApi.create({
      userId: Number(checkout.value.userId),
      cartItemIds: items.value.map((i) => i.cartItemId),
      recipientName: checkout.value.recipientName,
      recipientPhone: checkout.value.recipientPhone,
      recipientAddress: checkout.value.recipientAddress,
    })
    message.value = `訂單建立成功！訂單編號 #${order.orderId}`
    showCheckout.value = false
    await cartApi.updateTotalQuantity(cart.value.cartId)
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

    <div v-else-if="items.length === 0" class="empty">
      購物車是空的，<RouterLink to="/store">去逛逛</RouterLink>
    </div>

    <div v-else>
      <div class="card">
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>商品</th>
                <th>單價</th>
                <th>數量</th>
                <th>小計</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.cartItemId">
                <td>{{ item.productName }}</td>
                <td>{{ formatMoney(item.price) }}</td>
                <td>
                  <input
                    v-model.number="item.productQuantity"
                    type="number"
                    min="1"
                    class="qty"
                    @change="updateQty(item)"
                  />
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
            <span class="muted">總計</span>
            <span class="sum">{{ formatMoney(total()) }}</span>
          </div>
          <button class="btn btn-primary" @click="showCheckout = true">結帳</button>
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
    </div>
  </div>
</template>

<style scoped>
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
</style>
