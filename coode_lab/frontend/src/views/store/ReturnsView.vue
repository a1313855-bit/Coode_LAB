<script setup>
import { ref, onMounted } from 'vue'
import { returnRequestApi, returnItemApi } from '../../api'
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

const returnStatusLocal = {
  PENDING_REVIEW: '待審核',
  APPROVED: '已核准',
  REJECTED: '已拒絕',
  AWAITING_SHIPBACK: '待寄回',
  SHIPPED_BACK: '已寄回',
  RECEIVED: '已收件',
  REFUNDING: '退款中',
  REFUNDED: '已退款',
  EXCHANGING: '換貨中',
  EXCHANGE_SHIPPED: '已出貨',
  EXCHANGED: '已換貨',
  CANCELLED: '已取消',
}

const toast = ref('')
let toastTimer = null

function showToast(text) {
  toast.value = text
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = ''
  }, 2600)
}

async function memberShippedBack(item) {
  try {
    await returnItemApi.memberShippedBack(item.returnItemId, userId)
    showToast('已確認寄回')
    load()
  } catch (e) {
    showToast('操作失敗：' + e.message)
  }
}

async function memberExchangeReceived(item) {
  try {
    await returnItemApi.memberExchangeReceived(item.returnItemId, userId)
    showToast('已確認收到換貨')
    load()
  } catch (e) {
    showToast('操作失敗：' + e.message)
  }
}

async function cancelReturn(r) {
  try {
    await returnRequestApi.cancel(r.returnRequestsId, userId)
    showToast('已取消申請')
    load()
  } catch (e) {
    showToast('操作失敗：' + e.message)
  }
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
              <th>操作</th>
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
                  {{ returnStatusLocal[r.returnItem ? r.returnItem.status : r.status] || statusLabel(r.returnItem ? r.returnItem.status : r.status) }}
                </span>
              </td>
              <td>{{ formatDate(r.createdAt) }}</td>
              <td class="actions">
                <button
                  v-if="r.returnItem && r.returnItem.status === 'AWAITING_SHIPBACK'"
                  class="btn btn-sm btn-primary"
                  @click="memberShippedBack(r.returnItem)"
                >
                  確認寄回
                </button>
                <button
                  v-if="r.returnItem && r.returnItem.status === 'EXCHANGE_SHIPPED'"
                  class="btn btn-sm btn-primary"
                  @click="memberExchangeReceived(r.returnItem)"
                >
                  確認收到換貨
                </button>
                <button
                  v-if="r.returnItem && r.returnItem.status === 'PENDING_REVIEW'"
                  class="btn btn-sm btn-danger"
                  @click="cancelReturn(r)"
                >
                  取消申請
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="toast" class="toast">{{ toast }}</div>
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
.actions {
  white-space: nowrap;
}
.toast {
  position: fixed;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  background: #161616;
  color: #fff;
  padding: 12px 20px;
  border-radius: 4px;
  font-size: 14px;
  z-index: 200;
  letter-spacing: 0.04em;
}
</style>