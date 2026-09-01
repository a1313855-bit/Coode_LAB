<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, orderItemApi } from '../../api'
import { formatMoney, formatDate } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const orders = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const expandedId = ref(null)
const orderItems = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await orderApi.all(page.value)
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
  <div class="admin-content">
    <div class="page-header">
      <h1>訂單管理</h1>
      <p>查看全站訂單</p>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="orders.length === 0" class="empty">暫無訂單</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>訂單號</th>
              <th>會員</th>
              <th>收件人</th>
              <th>聯絡電話</th>
              <th>總金額</th>
              <th>建立時間</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in orders" :key="o.orderId">
              <td>#{{ o.orderId }}</td>
              <td>{{ o.user.name }}</td>
              <td>{{ o.recipientName }}</td>
              <td>{{ o.recipientPhone }}</td>
              <td>{{ formatMoney(o.sumTotal) }}</td>
              <td>{{ formatDate(o.createdAt) }}</td>
              <td><button class="btn btn-sm" @click="toggleItems(o.orderId)">明細</button></td>
            </tr>
            <tr v-if="expandedId === o.orderId">
              <td colspan="7">
                <div v-if="orderItems.length === 0" class="muted">無明細</div>
                <div v-for="it in orderItems" :key="it.orderItemId" class="item">
                  {{ it.product.name }} × {{ it.productQuantity }} — {{ formatMoney(it.priceTotal) }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.item {
  padding: 4px 0;
  font-size: 14px;
}
</style>
