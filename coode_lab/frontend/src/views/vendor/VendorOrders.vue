<script setup>
import { ref, onMounted } from 'vue'
import { orderItemApi } from '../../api'
import { formatMoney, statusBadgeClass, statusLabel, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = ref(1)
const items = ref([])
const page = ref(0)
const totalPages = ref(1)
const status = ref('')
const loading = ref(false)
const error = ref('')

const statusOptions = ['PENDING', 'PROCESSING', 'SHIPPED', 'RECEIVED', 'COMPLETED']

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await orderItemApi.byVendor(vendorId.value, page.value, status.value)
    items.value = res.content || []
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

function applyFilter() {
  page.value = 0
  load()
}

async function changeStatus(it) {
  try {
    await orderItemApi.updateStatus(it.orderItemId, { status: it.status })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div class="page-header flex-between">
      <div>
        <h1>訂單處理</h1>
        <p>查看並更新訂單商品狀態</p>
      </div>
      <div class="flex">
        <input v-model.number="vendorId" type="number" placeholder="廠商 ID" class="uid" />
        <select v-model="status" class="filter">
          <option value="">全部狀態</option>
          <option v-for="s in statusOptions" :key="s" :value="s">{{ statusLabel(s) }}</option>
        </select>
        <button class="btn btn-primary" @click="applyFilter">查詢</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="items.length === 0" class="empty">目前沒有訂單商品</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>訂單項 ID</th>
              <th>商品</th>
              <th>訂單編號</th>
              <th>數量</th>
              <th>金額</th>
              <th>狀態</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="it in items" :key="it.orderItemId">
              <td>{{ it.orderItemId }}</td>
              <td>
                {{ it.product.name }}
                <div class="muted small">{{ categoryLabel(it.product.categoryType) }} {{ it.product.size }}</div>
              </td>
              <td>#{{ it.order.orderId }}</td>
              <td>{{ it.productQuantity }}</td>
              <td>{{ formatMoney(it.priceTotal) }}</td>
              <td>
                <select :value="it.status" class="status-select" @change="(e) => { it.status = e.target.value; changeStatus(it) }">
<option v-for="s in statusOptions" :key="s" :value="s">{{ statusLabel(s) }}</option>
                </select>
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
.uid {
  width: 70px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.filter {
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.status-select {
  padding: 6px 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
}
.small {
  font-size: 12px;
}
</style>
