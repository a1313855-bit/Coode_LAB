<script setup>
import { ref, onMounted } from 'vue'
import { returnRequestApi } from '../../api'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = ref(1)
const returns = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const rrStatuses = ['PENDING', 'REVIEWED', 'CANCELLED']

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await returnRequestApi.byVendor(vendorId.value, page.value)
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

async function changeStatus(r) {
  try {
    await returnRequestApi.updateStatus(r.returnRequestsId, { status: r.status })
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
        <h1>退換貨處理</h1>
        <p>審核會員的退換貨申請</p>
      </div>
      <div class="flex">
        <input v-model.number="vendorId" type="number" placeholder="廠商 ID" class="uid" />
        <button class="btn btn-primary" @click="load">查詢</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="returns.length === 0" class="empty">目前沒有退換貨申請</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>申請編號</th>
              <th>會員</th>
              <th>訂單</th>
              <th>類型</th>
              <th>數量</th>
              <th>建立時間</th>
              <th>狀態</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in returns" :key="r.returnRequestsId">
              <td>#{{ r.returnRequestsId }}</td>
              <td>{{ r.user.name }}</td>
              <td>#{{ r.order.orderId }}</td>
              <td>{{ statusLabel(r.requestType) }}</td>
              <td>{{ r.returnRequestQuantity }}</td>
              <td>{{ formatDate(r.createdAt) }}</td>
              <td>
                <div class="status-cell">
                  <span :class="['badge', statusBadgeClass(r.status)]">{{ statusLabel(r.status) }}</span>
                  <select :value="r.status" class="status-select" @change="(e) => { r.status = e.target.value; changeStatus(r) }">
                    <option v-for="s in rrStatuses" :key="s" :value="s">{{ statusLabel(s) }}</option>
                  </select>
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
.uid {
  width: 70px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-select {
  padding: 6px 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
}
</style>
