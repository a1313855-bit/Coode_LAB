<script setup>
import { ref, onMounted } from 'vue'
import { returnRequestApi } from '../../api'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'
import { currentUserId } from '../../composables/auth'

const userId = currentUserId()
const returns = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await returnRequestApi.byUser(userId, page.value)
    returns.value = res.content || []
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

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header">
      <div>
        <h1>退換貨</h1>
        <p>查看與申請退換貨</p>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="returns.length === 0" class="empty">暫無退換貨申請</div>

    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>申請編號</th>
              <th>訂單</th>
              <th>商品名稱</th>
              <th>照片</th>
              <th>類型</th>
              <th>數量</th>
              <th>狀態</th>
              <th>建立時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in returns" :key="r.returnRequestsId">
              <td>#{{ r.returnRequestsId }}</td>
              <td>#{{ r.order.orderId }}</td>
              <td>{{ (r.returnItem && r.returnItem.productName) || '-' }}</td>
              <td>
                <img
                  v-if="r.returnItem && r.returnItem.picture"
                  :src="r.returnItem.picture"
                  class="thumb"
                  alt="退貨照片"
                />
                <span v-else class="muted small">-</span>
              </td>
              <td>{{ statusLabel(r.requestType) }}</td>
              <td>{{ r.returnRequestQuantity }}</td>
              <td>
                <span :class="['badge', statusBadgeClass(r.returnItem ? r.returnItem.status : r.status)]">
                  {{ statusLabel(r.returnItem ? r.returnItem.status : r.status) }}
                </span>
              </td>
              <td>{{ formatDate(r.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.thumb {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid var(--line);
  background: #f2efea;
}
.small {
  font-size: 12px;
}
</style>