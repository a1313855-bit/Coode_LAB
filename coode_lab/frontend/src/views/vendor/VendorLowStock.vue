<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../../api'
import { formatMoney, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = ref(1)
const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await productApi.lowStock(vendorId.value, page.value)
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

async function updateStock(p) {
  const v = prompt(`補貨「${p.name}」數量`, 10)
  if (v === null) return
  try {
    await productApi.updateStock(vendorId.value, p.productId, { stock: Number(v) })
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
        <h1>低庫存管理</h1>
        <p>庫存低於 10 的商品</p>
      </div>
      <div class="flex">
        <input v-model.number="vendorId" type="number" placeholder="廠商 ID" class="uid" />
        <button class="btn btn-primary" @click="load">查詢</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="products.length === 0" class="empty">目前沒有低庫存商品</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>商品</th>
              <th>分類</th>
              <th>庫存</th>
              <th>價格</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.productId">
              <td>{{ p.productId }}</td>
              <td>{{ p.name }}</td>
              <td>{{ categoryLabel(p.categoryType) }}</td>
              <td class="low-cell">{{ p.stock }}</td>
              <td>{{ formatMoney(p.price) }}</td>
              <td><button class="btn btn-sm btn-primary" @click="updateStock(p)">補貨</button></td>
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
.low-cell {
  color: var(--c-danger);
  font-weight: 700;
}
</style>
