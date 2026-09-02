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
  { key: 'processing', label: '處理中', statuses: ['SHIPPED', 'ARRIVED'] },
  { key: 'completed', label: '已完成', statuses: ['RECEIVED'] },
  { key: 'cancelled', label: '已取消', statuses: ['CANCELLED'] },
]
const activeTab = ref('all')

const allItems = ref([])
const clientPage = ref(0)
const pageSize = 10
const loading = ref(false)
const error = ref('')

const statusOptions = ['PENDING', 'PROCESSING', 'SHIPPED', 'ARRIVED', 'RECEIVED', 'COMPLETED', 'CANCELLED']

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

async function changeStatus(it) {
  error.value = ''
  try {
    await orderItemApi.updateStatus(it.orderItemId, { status: it.status })
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

    <p class="subtitle">查看並更新訂單商品狀態</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="allItems.length === 0" class="empty">目前沒有訂單商品</div>
    <div v-else-if="filteredItems.length === 0" class="empty">此分頁暫時沒有訂單</div>
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
            <tr v-for="it in clientRows" :key="it.orderItemId">
              <td>{{ it.orderItemId }}</td>
              <td>
                {{ it.product.name }}
                <div class="muted small">{{ categoryLabel(it.product.categoryType) }} {{ it.product.size }}</div>
              </td>
              <td>#{{ it.order.orderId }}</td>
              <td>{{ it.productQuantity }}</td>
              <td>{{ formatMoney(it.priceTotal) }}</td>
              <td>
                <div class="status-cell">
                  <span :class="['badge', statusBadgeClass(it.status)]">{{ statusLabel(it.status) }}</span>
                  <select :value="it.status" class="status-select" @change="(e) => { it.status = e.target.value; changeStatus(it) }">
                    <option v-for="s in statusOptions" :key="s" :value="s">{{ statusLabel(s) }}</option>
                  </select>
                </div>
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
.small {
  font-size: 12px;
}
</style>