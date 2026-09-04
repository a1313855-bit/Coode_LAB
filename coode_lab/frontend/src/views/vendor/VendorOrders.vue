<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderItemApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatMoney, statusBadgeClass, statusLabel, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = currentVendorId()

const tabs = [
  { key: 'all', label: '全部訂單', statuses: [] },
  { key: 'pending', label: '待處理', statuses: ['PENDING'] },
  { key: 'processing', label: '處理中', statuses: ['PROCESSING'] },
  { key: 'shipped', label: '已出貨', statuses: ['SHIPPED'] },
  { key: 'completed', label: '已完成', statuses: ['RECEIVED'] },
  { key: 'cancelled', label: '已取消', statuses: ['CANCELLED'] },
]
const activeTab = ref('all')

const allItems = ref([])
const clientPage = ref(0)
const pageSize = 10
const loading = ref(false)
const error = ref('')

const orderStatusLabel = { PENDING: '待處理', PROCESSING: '處理中', SHIPPED: '已出貨', RECEIVED: '已完成', CANCELLED: '已取消' }

const manualStatusOptions = ['PENDING', 'PROCESSING', 'SHIPPED', 'CANCELLED']

const advanceButtonLabel = { PENDING: '開始處理', PROCESSING: '確認出貨' }

async function advanceAction(item) {
  try {
    await orderItemApi.advance(item.orderItemId, vendorId)
    error.value = ''
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

async function manualStatusAction(item, newStatus) {
  if (!window.confirm(`確定要將狀態改為「${orderStatusLabel[newStatus]}」嗎？`)) return
  try {
    await orderItemApi.vendorStatus(item.orderItemId, vendorId, { status: newStatus })
    error.value = ''
    await loadAll()
  } catch (e) {
    error.value = e.message
  }
}

const filteredItems = computed(() => {
  const tab = tabs.find((t) => t.key === activeTab.value)
  if (!tab.statuses.length) return allItems.value
  return allItems.value.filter((it) => tab.statuses.includes(it.status))
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
      map[t.key] = allItems.value.length
    } else {
      map[t.key] = allItems.value.filter((it) => t.statuses.includes(it.status)).length
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
      const res = await orderItemApi.byVendor(vendorId, page)
      all.push(...(res.content || []))
      totalPages = res.totalPages || 1
      page += 1
    } while (page < totalPages && page < 100)
    allItems.value = all
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

onMounted(loadAll)
</script>

<template>
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">VENDOR</div>
          <h1 class="vr-title">訂單管理</h1>
          <p class="vr-subtitle">查看並更新訂單商品狀態</p>
        </div>
      </div>
    </div>

    <div class="vr-tabs" role="tablist">
      <button
        v-for="t in tabs"
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
    <div v-else-if="allItems.length === 0" class="vr-empty">目前沒有訂單商品</div>
    <div v-else-if="filteredItems.length === 0" class="vr-empty">此分頁暫時沒有訂單</div>
    <div v-else class="vr-card">
      <table class="vr-table">
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
          <tr v-for="it in clientRows" :key="it.orderItemId">
            <td>{{ it.orderItemId }}</td>
            <td>
              {{ (it.variant && it.variant.product && it.variant.product.name) || '-' }}
              <div style="color: var(--vr-mut); font-size: 12px">
                {{ it.variant ? categoryLabel(it.variant.product.categoryType) + ' · ' + it.variant.color + ' / ' + it.variant.size : '-' }}
              </div>
            </td>
            <td>#{{ it.order.orderId }}</td>
            <td>{{ it.productQuantity }}</td>
            <td>{{ formatMoney(it.priceTotal) }}</td>
            <td>
              <div class="status-cell">
                <span v-if="it.status === 'PENDING'" class="vr-badge vr-badge-pending">{{ orderStatusLabel[it.status] || it.status }}</span>
                <span v-else-if="it.status === 'PROCESSING'" class="vr-badge vr-badge-pending">{{ orderStatusLabel[it.status] || it.status }}</span>
                <span v-else-if="it.status === 'SHIPPED'" class="vr-badge vr-badge-active">{{ orderStatusLabel[it.status] || it.status }}</span>
                <span v-else-if="it.status === 'RECEIVED'" class="vr-badge vr-badge-active">{{ orderStatusLabel[it.status] || it.status }}</span>
                <span v-else-if="it.status === 'CANCELLED'" class="vr-badge vr-badge-danger">{{ orderStatusLabel[it.status] || it.status }}</span>
                <span v-else class="vr-badge vr-badge-inactive">{{ orderStatusLabel[it.status] || it.status }}</span>
                <button
                  v-if="advanceButtonLabel[it.status]"
                  class="vr-btn vr-btn-sm vr-btn-primary"
                  @click="advanceAction(it)"
                >
                  {{ advanceButtonLabel[it.status] }}
                </button>
                <span v-if="it.status === 'SHIPPED'" style="color: var(--vr-mut); font-size: 13px; white-space: nowrap">等待買家確認收貨</span>
                <select
                  class="status-select"
                  :value="it.status"
                  @change="(e) => manualStatusAction(it, e.target.value)"
                >
                  <option disabled value="">修改狀態</option>
                  <option v-for="s in manualStatusOptions" :key="s" :value="s">{{ orderStatusLabel[s] }}</option>
                </select>
              </div>
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
  flex-wrap: wrap;
}
.status-select {
  padding: 6px 8px;
  border: 1px solid var(--vr-line);
  border-radius: 6px;
  font-size: 13px;
}
</style>
