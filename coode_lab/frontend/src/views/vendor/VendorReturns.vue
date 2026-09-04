<script setup>
import { ref, computed, onMounted } from 'vue'
import { returnRequestApi, returnItemApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = computed(() => currentVendorId())

const retStatusLabel = {
  PENDING_REVIEW: '待審核',
  APPROVED: '審核通過',
  REJECTED: '已拒絕',
  AWAITING_SHIPBACK: '待寄回',
  SHIPPED_BACK: '已寄回',
  RECEIVED: '已收件',
  REFUNDING: '退款中',
  REFUNDED: '退款完成',
  EXCHANGING: '換貨中',
  EXCHANGE_SHIPPED: '換貨商品已出貨',
  EXCHANGED: '換貨完成',
  CANCELLED: '已取消',
}

function rLabel(s) {
  return retStatusLabel[String(s || '').toUpperCase()] || statusLabel(s)
}

const allTabs = [
  { key: 'all', label: '全部申請', statuses: [] },
  { key: 'pending', label: '待審核', statuses: ['PENDING_REVIEW'] },
  { key: 'approved', label: '已通過', statuses: ['APPROVED', 'RECEIVED', 'REFUNDING', 'EXCHANGING', 'EXCHANGE_SHIPPED'] },
  { key: 'completed', label: '已完成', statuses: ['REFUNDED', 'EXCHANGED'] },
  { key: 'rejected', label: '已拒絕', statuses: ['REJECTED'] },
]

const activeTab = ref('all')

const allReturns = ref([])
const clientPage = ref(0)
const pageSize = 10
const loading = ref(false)
const error = ref('')

const filteredItems = computed(() => {
  const tab = allTabs.find((t) => t.key === activeTab.value)
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
  for (const t of allTabs) {
    if (!t.statuses.length) {
      map[t.key] = allReturns.value.length
    } else {
      map[t.key] = allReturns.value.filter((r) => r.returnItem && t.statuses.includes(r.returnItem.status)).length
    }
  }
  return map
})

async function loadAll() {
  if (!vendorId.value) return
  loading.value = true
  error.value = ''
  try {
    const all = []
    let page = 0
    let tp = 1
    do {
      const res = await returnRequestApi.byVendor(vendorId.value, page)
      all.push(...(res.content || []))
      tp = res.totalPages || 1
      page += 1
    } while (page < tp && page < 100)
    for (const r of all) {
      if (r.returnItem && r.picture && !r.returnItem.picture) {
        r.returnItem.picture = r.picture
      }
    }
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

const reviewDecision = ref('')

async function doReview(r) {
  if (!r.returnItem || !vendorId.value) return
  if (!reviewDecision.value) {
    window.alert('請先選擇審核結果')
    return
  }
  if (!window.confirm(`確定要「${reviewDecision.value === 'APPROVED' ? '通過' : '拒絕'}」此申請？`)) return
  error.value = ''
  try {
    await returnItemApi.review(r.returnItem.returnItemId, vendorId.value, reviewDecision.value)
    reviewDecision.value = ''
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

async function doVendorAdvance(r) {
  if (!r.returnItem || !vendorId.value) return
  const nextLabel = getNextActionLabel(r.returnItem.status)
  if (!nextLabel) return
  if (!window.confirm(`確定要執行「${nextLabel}」？`)) return
  error.value = ''
  try {
    await returnItemApi.vendorAdvance(r.returnItem.returnItemId, vendorId.value)
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

async function doStartExchange(r) {
  if (!r.returnItem || !vendorId.value) return
  if (!window.confirm('確定要開始換貨？')) return
  error.value = ''
  try {
    await returnItemApi.vendorStatus(r.returnItem.returnItemId, vendorId.value, { status: 'EXCHANGING' })
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

const manualStatusTarget = ref('')

async function doManualStatus(r) {
  if (!r.returnItem || !vendorId.value) return
  if (!manualStatusTarget.value) {
    window.alert('請先選擇目標狀態')
    return
  }
  const label = rLabel(manualStatusTarget.value)
  if (!window.confirm(`確定要將狀態修改為「${label}」？`)) return
  error.value = ''
  try {
    await returnItemApi.vendorStatus(r.returnItem.returnItemId, vendorId.value, { status: manualStatusTarget.value })
    manualStatusTarget.value = ''
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

function getNextActionLabel(status) {
  const map = {
    APPROVED: '確認收件',
    RECEIVED: '完成退款',
    REFUNDING: '完成退款',
    EXCHANGING: '確認換貨商品出貨',
    EXCHANGE_SHIPPED: '',
  }
  return map[status] || null
}

const allStatusOptions = Object.keys(retStatusLabel)

onMounted(loadAll)
</script>

<template>
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">VENDOR</div>
          <h1 class="vr-title">退換貨管理</h1>
          <p class="vr-subtitle">審核會員的退換貨申請，處理狀態以退貨商品（ReturnItem）為準</p>
        </div>
      </div>
    </div>

    <div class="vr-tabs" role="tablist">
      <button
        v-for="t in allTabs"
        :key="t.key"
        class="vr-tab"
        :class="{ active: activeTab === t.key }"
        @click="changeTab(t.key)"
      >
        {{ t.label }}
        <span class="vr-tab-count">{{ tabCounts[t.key] }}</span>
      </button>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>
    <div v-else-if="allReturns.length === 0" class="vr-empty">目前沒有退換貨申請</div>
    <div v-else-if="filteredItems.length === 0" class="vr-empty">此分頁暫時沒有申請</div>
    <div v-else class="vr-card">
      <table class="vr-table">
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
                <div style="color: var(--vr-mut); font-size: 12px">{{ r.returnItem.color }} {{ r.returnItem.size }}</div>
              </template>
              <span v-else style="color: var(--vr-mut); font-size: 12px">無商品資訊</span>
            </td>
            <td>{{ r.user.name }}</td>
            <td>#{{ r.order.orderId }}</td>
            <td>{{ statusLabel(r.requestType) }}</td>
            <td>{{ r.returnRequestQuantity }}</td>
            <td>{{ formatDate(r.createdAt) }}</td>
            <td>
              <template v-if="r.returnItem">
                <div class="status-cell">
                  <span v-if="r.returnItem.status === 'PENDING_REVIEW'" class="vr-badge vr-badge-pending">{{ rLabel(r.returnItem.status) }}</span>
                  <span v-else-if="r.returnItem.status === 'APPROVED' || r.returnItem.status === 'RECEIVED' || r.returnItem.status === 'REFUNDING' || r.returnItem.status === 'EXCHANGING' || r.returnItem.status === 'EXCHANGE_SHIPPED'" class="vr-badge vr-badge-active">{{ rLabel(r.returnItem.status) }}</span>
                  <span v-else-if="r.returnItem.status === 'REFUNDED' || r.returnItem.status === 'EXCHANGED'" class="vr-badge vr-badge-active">{{ rLabel(r.returnItem.status) }}</span>
                  <span v-else-if="r.returnItem.status === 'REJECTED'" class="vr-badge vr-badge-danger">{{ rLabel(r.returnItem.status) }}</span>
                  <span v-else class="vr-badge vr-badge-inactive">{{ rLabel(r.returnItem.status) }}</span>
                </div>

                <div class="action-row" v-if="r.returnItem.status === 'PENDING_REVIEW'">
                  <select v-model="reviewDecision" class="status-select">
                    <option value="" disabled>選擇</option>
                    <option value="APPROVED">通過</option>
                    <option value="REJECTED">拒絕</option>
                  </select>
                  <button class="vr-btn vr-btn-sm vr-btn-primary" @click="doReview(r)">審核</button>
                </div>

                <div class="action-row" v-if="r.returnItem.status === 'APPROVED'">
                  <button class="vr-btn vr-btn-sm vr-btn-primary" @click="doVendorAdvance(r)">確認收件</button>
                </div>

                <div class="action-row" v-if="r.returnItem.status === 'RECEIVED'">
                  <button class="vr-btn vr-btn-sm vr-btn-primary" @click="doVendorAdvance(r)">開始退款</button>
                  <button class="vr-btn vr-btn-sm vr-btn-warning" @click="doStartExchange(r)">開始換貨</button>
                </div>

                <div class="action-row" v-if="r.returnItem.status === 'REFUNDING'">
                  <button class="vr-btn vr-btn-sm vr-btn-primary" @click="doVendorAdvance(r)">完成退款</button>
                </div>

                <div class="action-row" v-if="r.returnItem.status === 'EXCHANGING'">
                  <button class="vr-btn vr-btn-sm vr-btn-primary" @click="doVendorAdvance(r)">確認換貨商品出貨</button>
                </div>

                <div class="action-row" v-if="r.returnItem.status === 'EXCHANGE_SHIPPED'">
                  <span style="color: var(--vr-mut); font-size: 12px">等待買家確認收到換貨</span>
                </div>

                <div class="action-row manual-status">
                  <select v-model="manualStatusTarget" class="status-select">
                    <option value="" disabled>修改狀態</option>
                    <option v-for="s in allStatusOptions" :key="s" :value="s">{{ rLabel(s) }}</option>
                  </select>
                  <button class="vr-btn vr-btn-sm vr-btn-primary" @click="doManualStatus(r)">修改</button>
                </div>
              </template>
              <span v-else style="color: var(--vr-mut); font-size: 12px">-</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination :page="clientPage" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.action-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}
.manual-status {
  margin-top: 8px;
}
.status-select {
  padding: 6px 8px;
  border: 1px solid var(--vr-line);
  border-radius: 6px;
  font-size: 13px;
}
</style>
