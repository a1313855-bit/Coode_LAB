<script setup>
import { ref, computed, onMounted } from 'vue'
import { returnRequestApi, returnItemApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = currentVendorId()

const tabs = [
  { key: 'all', label: '全部申請', statuses: [] },
  { key: 'pending', label: '待審核', statuses: ['PENDING_REVIEW'] },
  { key: 'processing', label: '處理中', statuses: ['PROCESSING'] },
  { key: 'completed', label: '已完成', statuses: ['COMPLETED'] },
  { key: 'rejected', label: '已拒絕', statuses: ['REJECTED'] },
]
const activeTab = ref('all')

const allReturns = ref([])
const clientPage = ref(0)
const pageSize = 10
const loading = ref(false)
const error = ref('')

const returnItemStatuses = [
  'PENDING_REVIEW',
  'APPROVED',
  'PROCESSING',
  'PROCESSED',
  'ARRIVED',
  'COMPLETED',
  'REJECTED',
  'CANCELLED',
]

const returnStatusLabelMap = {
  PENDING_REVIEW: '審核中',
  APPROVED: '同意',
  PROCESSING: '待處理',
  PROCESSED: '已處理',
  ARRIVED: '已到貨',
  COMPLETED: '已完成',
  REJECTED: '已拒絕',
  CANCELLED: '已取消',
}
function returnStatusLabel(s) {
  return returnStatusLabelMap[String(s || '').toUpperCase()] || statusLabel(s)
}

const returnNextStatusMap = {
  PENDING_REVIEW: { status: 'PROCESSING', label: '開始處理' },
  PROCESSING: { status: 'PROCESSED', label: '已處理' },
  PROCESSED: { status: 'ARRIVED', label: '已到貨' },
  ARRIVED: { status: 'COMPLETED', label: '完成退款' },
}
function getNextReturnStatus(status) {
  return returnNextStatusMap[status] || null
}

async function advanceReturnStatus(r) {
  if (!r.returnItem) return
  const next = getNextReturnStatus(r.returnItem.status)
  if (!next) return
  if (!window.confirm(`確定要將此退貨商品狀態改為「${returnStatusLabel(next.status)}」嗎？`)) return
  r.returnItem.status = next.status
  await changeStatus(r)
}

const filteredItems = computed(() => {
  const tab = tabs.find((t) => t.key === activeTab.value)
  if (!tab.statuses.length) return allReturns.value
  return allReturns.value.filter((r) => r.returnItem && tab.statuses.includes(r.returnItem.status))
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize)))

const clientRows = computed(() => {
  const start = clientPage.value * pageSize
  return filteredItems.value.slice(start, start + pageSize)
})

const tabCounts = computed(() => {
  const map = {}
  for (const t of tabs) {
    if (!t.statuses.length) {
      map[t.key] = allReturns.value.length
    } else {
      map[t.key] = allReturns.value.filter((r) => r.returnItem && t.statuses.includes(r.returnItem.status)).length
    }
  }
  return map
})

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const all = []
    let page = 0
    let totalPages = 1
    do {
      const res = await returnRequestApi.byVendor(vendorId, page)
      all.push(...(res.content || []))
      totalPages = res.totalPages || 1
      page += 1
    } while (page < totalPages && page < 100)
    allReturns.value = all
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function changeTab(key) {
  if (key === activeTab.value) return
  activeTab.value = key
  clientPage.value = 0
}

function changePage(p) {
  clientPage.value = p
}

async function changeStatus(r) {
  if (!r.returnItem) return
  error.value = ''
  try {
    await returnItemApi.updateStatus(r.returnItem.returnItemId, { status: r.returnItem.status })
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="admin-content">
    <div class="tabs" role="tablist">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="tab"
        :class="{ active: activeTab === t.key }"
        @click="changeTab(t.key)"
      >
        {{ t.label }}
        <span class="tab-count">{{ tabCounts[t.key] }}</span>
      </button>
    </div>

    <p class="subtitle">審核會員的退換貨申請，處理狀態以退貨商品（ReturnItem）為準</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="allReturns.length === 0" class="empty">目前沒有退換貨申請</div>
    <div v-else-if="filteredItems.length === 0" class="empty">此分頁暫時沒有申請</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>申請編號</th>
              <th>商品</th>
              <th>會員</th>
              <th>訂單</th>
              <th>類型</th>
              <th>數量</th>
              <th>建立時間</th>
              <th>處理狀態</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in clientRows" :key="r.returnRequestsId">
              <td>#{{ r.returnRequestsId }}</td>
              <td>
                <template v-if="r.returnItem">
                  {{ r.returnItem.productName }}
                  <div class="muted small">{{ r.returnItem.color }} {{ r.returnItem.size }}</div>
                </template>
                <span v-else class="muted small">無商品資訊</span>
              </td>
              <td>{{ r.user.name }}</td>
              <td>#{{ r.order.orderId }}</td>
              <td>{{ statusLabel(r.requestType) }}</td>
              <td>{{ r.returnRequestQuantity }}</td>
              <td>{{ formatDate(r.createdAt) }}</td>
              <td>
                <div class="status-cell" v-if="r.returnItem">
                  <span :class="['badge', statusBadgeClass(r.returnItem.status)]">{{ returnStatusLabel(r.returnItem.status) }}</span>
                  <select
                    :value="r.returnItem.status"
                    class="status-select"
                    @change="(e) => { r.returnItem.status = e.target.value; changeStatus(r) }"
                  >
                    <option v-for="s in returnItemStatuses" :key="s" :value="s">{{ returnStatusLabel(s) }}</option>
                  </select>
                  <button
                    v-if="getNextReturnStatus(r.returnItem.status)"
                    class="btn btn-sm btn-next"
                    @click="advanceReturnStatus(r)"
                  >
                    {{ getNextReturnStatus(r.returnItem.status).label }} →
                  </button>
                </div>
                <span v-else class="muted small">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <AppPagination :page="clientPage" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.tab {
  padding: 9px 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: var(--c-text-light);
}
.tab.active {
  border-color: transparent;
  background: #db2777;
  color: #fff;
  font-weight: 700;
}
.tab-count {
  display: inline-block;
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.8;
}
.subtitle {
  color: var(--c-text-light);
  font-size: 14px;
  margin-bottom: 16px;
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
.btn-next {
  border-color: #bfdbfe;
  color: #1d4ed8;
  white-space: nowrap;
}
.btn-next:hover {
  background: #eff6ff;
  color: #1d4ed8;
}
.status-cell {
  flex-wrap: wrap;
}
.small {
  font-size: 12px;
}
</style>