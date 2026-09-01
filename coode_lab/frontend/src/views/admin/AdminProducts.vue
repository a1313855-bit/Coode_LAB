<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../../api'
import { formatMoney, statusBadgeClass, statusLabel, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const filters = ref({ keyword: '', status: '', vendorId: '', categoryType: '' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await productApi.adminFilter({ page: page.value, ...filters.value })
    products.value = res.content || []
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
function applySearch() {
  page.value = 0
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div class="page-header">
      <h1>商品管理</h1>
      <p>查看與管理全站商品</p>
    </div>

    <div class="card filter-bar">
      <input v-model="filters.keyword" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
      <select v-model="filters.categoryType">
        <option value="">全部分類</option>
        <option value="TOP">上衣</option>
        <option value="OUTER">外套</option>
        <option value="BOTTOM">褲子</option>
        <option value="SHOES">鞋子</option>
        <option value="ACCESSORY">配件</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">啟用中</option>
        <option value="DRAFT">草稿</option>
        <option value="INACTIVE">未啟用</option>
      </select>
      <input v-model.number="filters.vendorId" type="number" placeholder="廠商 ID" class="vendor" />
      <button class="btn btn-primary" @click="applySearch">搜尋</button>
      <button class="btn" @click="filters={keyword:'',status:'',vendorId:'',categoryType:''}; applySearch()">清空</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>商品</th>
              <th>分類</th>
              <th>廠商</th>
              <th>庫存</th>
              <th>價格</th>
              <th>狀態</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.productId">
              <td>{{ p.productId }}</td>
              <td>{{ p.name }}</td>
              <td>{{ categoryLabel(p.categoryType) }}</td>
              <td>{{ p.vendorName }} <span class="muted">(#{{ p.vendorId }})</span></td>
              <td :class="{ 'low-cell': p.stock <= 10 }">{{ p.stock }}</td>
              <td>{{ formatMoney(p.price) }}</td>
              <td><span :class="['badge', statusBadgeClass(p.status)]">{{ statusLabel(p.status) }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.filter-bar input {
  padding: 8px 10px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  min-width: 160px;
}
.filter-bar select {
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.vendor {
  width: 90px !important;
}
.low-cell {
  color: var(--c-danger);
  font-weight: 700;
}
</style>
