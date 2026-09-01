<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, orderItemApi } from '../../api'
import { formatMoney, formatDate } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'
import { currentUserId } from '../../composables/auth'

const userId = ref(currentUserId())
const orders = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

// 展開查看明細
const expandedId = ref(null)
const orderItems = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await orderApi.byUser(userId.value, page.value)
    orders.value = res.content || []
    page.value = res.page || 0
    totalPages.value = res.totalPages || 1
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  page.value = p
  load()
}

async function toggleItems(orderId) {
  if (expandedId.value === orderId) {
    expandedId.value = null
    return
  }
  expandedId.value = orderId
  try {
    const res = await orderItemApi.byOrder(orderId, 0)
    orderItems.value = res.content || []
  } catch (e) {
    orderItems.value = []
  }
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header flex-between">
      <div>
        <h1>我的訂單</h1>
        <p>查看訂單與明細</p>
      </div>
      <div class="flex">
        <input v-model.number="userId" type="number" placeholder="會員 ID" class="uid" />
        <button class="btn btn-primary" @click="load">查詢</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="orders.length === 0" class="empty">暫無訂單</div>

    <div v-else class="order-list">
      <div v-for="o in orders" :key="o.orderId" class="card order">
        <div class="order-head" @click="toggleItems(o.orderId)">
          <div>
            <b>訂單 #{{ o.orderId }}</b>
            <div class="muted small">建立於 {{ formatDate(o.createdAt) }}</div>
          </div>
          <div class="text-right">
            <div class="sum">{{ formatMoney(o.sumTotal) }}</div>
            <div class="muted small">{{ o.user.name }} · {{ o.recipientName }}</div>
          </div>
        </div>
        <div class="muted small recipient">
          收件：{{ o.recipientName }} {{ o.recipientPhone }} · {{ o.recipientAddress }}
        </div>
        <div v-if="expandedId === o.orderId" class="items">
          <div v-if="orderItems.length === 0" class="muted">無明細資料</div>
          <div v-for="it in orderItems" :key="it.orderItemId" class="item">
            <span>{{ it.product.name }}</span>
            <span>{{ it.productQuantity }} 件</span>
            <span>{{ formatMoney(it.priceTotal) }}</span>
          </div>
        </div>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.uid {
  width: 90px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}
.small {
  font-size: 12px;
}
.sum {
  font-weight: 700;
  color: var(--c-danger);
}
.recipient {
  margin-top: 8px;
}
.items {
  margin-top: 12px;
  border-top: 1px solid var(--c-border);
  padding-top: 10px;
}
.item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
}
</style>
