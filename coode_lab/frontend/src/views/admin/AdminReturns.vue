<script setup>
import { ref, computed, onMounted } from 'vue'
import { returnRequestApi, returnItemApi } from '../../api'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const returns = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const allStatuses = [
  'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'AWAITING_SHIPBACK', 'SHIPPED_BACK',
  'RECEIVED', 'REFUNDING', 'REFUNDED', 'EXCHANGING', 'EXCHANGE_SHIPPED', 'EXCHANGED', 'CANCELLED',
]

const statusLocal = {
  PENDING_REVIEW: '待審核', APPROVED: '已核准', REJECTED: '已拒絕',
  AWAITING_SHIPBACK: '待寄回', SHIPPED_BACK: '已寄回', RECEIVED: '已收件',
  REFUNDING: '退款中', REFUNDED: '已退款', EXCHANGING: '換貨中',
  EXCHANGE_SHIPPED: '已出貨', EXCHANGED: '已換貨', CANCELLED: '已取消',
}

const activeTab = ref('all')
const tabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待審核', statuses: ['PENDING_REVIEW'] },
  { key: 'processing', label: '處理中', statuses: ['APPROVED', 'AWAITING_SHIPBACK', 'SHIPPED_BACK', 'RECEIVED', 'REFUNDING', 'EXCHANGING', 'EXCHANGE_SHIPPED'] },
  { key: 'completed', label: '已完成', statuses: ['REFUNDED', 'EXCHANGED'] },
  { key: 'rejected', label: '已拒絕/取消', statuses: ['REJECTED', 'CANCELLED'] },
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await returnRequestApi.all(page.value)
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

const filtered = computed(() => {
  if (activeTab.value === 'all') return returns.value
  const tab = tabs.find((t) => t.key === activeTab.value)
  if (!tab || !tab.statuses) return returns.value
  return returns.value.filter((r) => tab.statuses.includes(r.returnItem ? r.returnItem.status : ''))
})

const pageSize = 10
const paged = computed(() => {
  const start = (page.value) * pageSize
  return filtered.value.slice(start, start + pageSize)
})
const filteredTotalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))

function changeFilteredPage(p) {
  page.value = p
}

const showModal = ref(false)
const selectedReturn = ref(null)
const adminStatusValue = ref('')

function openDetail(r) {
  selectedReturn.value = r
  adminStatusValue.value = r.returnItem ? r.returnItem.status : ''
  showModal.value = true
}

async function applyAdminStatus() {
  if (!selectedReturn.value || !selectedReturn.value.returnItem) return
  try {
    await returnItemApi.adminStatus(selectedReturn.value.returnItem.returnItemId, {
      status: adminStatusValue.value,
    })
    showModal.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div class="page-header">
      <div>
        <h1>退換貨管理</h1>
        <p class="subtitle">管理全平台所有會員的退換貨申請</p>
      </div>
    </div>

    <div class="tabs" role="tablist">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="tab"
        :class="{ active: activeTab === t.key }"
        @click="activeTab = t.key; page = 0"
      >
        {{ t.label }}
      </button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="paged.length === 0" class="empty">暫無退換貨申請</div>

    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>申請編號</th>
              <th>訂單</th>
              <th>會員</th>
              <th>所屬廠商</th>
              <th>類型</th>
              <th>申請商品</th>
              <th>規格</th>
              <th>數量</th>
              <th>原因</th>
              <th>狀態</th>
              <th>申請時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in paged" :key="r.returnRequestsId">
              <td>#{{ r.returnRequestsId }}</td>
              <td>#{{ r.order ? r.order.orderId : '-' }}</td>
              <td>{{ r.user ? r.user.name : '-' }}</td>
              <td>{{ r.vendor ? r.vendor.vendorName : '-' }}</td>
              <td>{{ statusLabel(r.requestType) }}</td>
              <td>{{ r.returnItem ? r.returnItem.productName : '-' }}</td>
              <td>
                <span v-if="r.returnItem">{{ r.returnItem.color }} / {{ r.returnItem.size }}</span>
                <span v-else>-</span>
              </td>
              <td>{{ r.returnRequestQuantity }}</td>
              <td class="small">{{ r.returnItem && r.returnItem.reason ? r.returnItem.reason : '-' }}</td>
              <td>
                <span :class="['badge', statusBadgeClass(r.returnItem ? r.returnItem.status : r.status)]">
                  {{ statusLocal[r.returnItem ? r.returnItem.status : r.status] || statusLabel(r.returnItem ? r.returnItem.status : r.status) }}
                </span>
              </td>
              <td>{{ formatDate(r.createdAt) }}</td>
              <td>
                <button class="btn btn-sm" @click="openDetail(r)">詳情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppPagination
      v-if="activeTab === 'all'"
      :page="page"
      :total-pages="totalPages"
      @change="changePage"
    />
    <AppPagination
      v-else
      :page="page"
      :total-pages="filteredTotalPages"
      @change="changeFilteredPage"
    />

    <!-- 詳情 Modal -->
    <div v-if="showModal && selectedReturn" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>退換貨詳情 #{{ selectedReturn.returnRequestsId }}</h3>
        <div class="detail-grid">
          <div class="detail-row"><span class="label">訂單編號</span><span>#{{ selectedReturn.order ? selectedReturn.order.orderId : '-' }}</span></div>
          <div class="detail-row"><span class="label">會員</span><span>{{ selectedReturn.user ? selectedReturn.user.name : '-' }}</span></div>
          <div class="detail-row"><span class="label">所屬廠商</span><span>{{ selectedReturn.vendor ? selectedReturn.vendor.vendorName : '-' }}</span></div>
          <div class="detail-row"><span class="label">類型</span><span>{{ statusLabel(selectedReturn.requestType) }}</span></div>
          <div class="detail-row"><span class="label">申請商品</span><span>{{ selectedReturn.returnItem ? selectedReturn.returnItem.productName : '-' }}</span></div>
          <div class="detail-row"><span class="label">規格</span><span v-if="selectedReturn.returnItem">{{ selectedReturn.returnItem.color }} / {{ selectedReturn.returnItem.size }}</span><span v-else>-</span></div>
          <div class="detail-row"><span class="label">數量</span><span>{{ selectedReturn.returnRequestQuantity }}</span></div>
          <div class="detail-row"><span class="label">原因</span><span>{{ selectedReturn.returnItem && selectedReturn.returnItem.reason ? selectedReturn.returnItem.reason : '-' }}</span></div>
          <div class="detail-row"><span class="label">描述</span><span>{{ selectedReturn.returnItem && selectedReturn.returnItem.description ? selectedReturn.returnItem.description : '-' }}</span></div>
          <div class="detail-row">
            <span class="label">照片</span>
            <img v-if="selectedReturn.returnItem && selectedReturn.returnItem.picture" :src="selectedReturn.returnItem.picture" class="detail-thumb" />
            <span v-else>無</span>
          </div>
          <div class="detail-row"><span class="label">申請時間</span><span>{{ formatDate(selectedReturn.createdAt) }}</span></div>
        </div>

        <div class="admin-action">
          <label>修改狀態（管理員權限）</label>
          <div class="flex gap-8">
            <select v-model="adminStatusValue">
              <option v-for="s in allStatuses" :key="s" :value="s">{{ statusLocal[s] || s }}</option>
            </select>
            <button class="btn btn-primary" @click="applyAdminStatus">套用</button>
          </div>
        </div>

        <div style="margin-top: 16px">
          <button class="btn" @click="showModal = false">關閉</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 16px;
}
.subtitle {
  color: var(--c-text-light);
  font-size: 14px;
}
.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
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
.small {
  font-size: 12px;
  max-width: 160px;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
  width: 560px;
  max-width: 90vw;
  max-height: 85vh;
  overflow-y: auto;
}
.modal h3 {
  margin-bottom: 16px;
}
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}
.detail-row {
  display: flex;
  gap: 12px;
}
.detail-row .label {
  min-width: 80px;
  font-weight: 600;
  color: var(--c-text-light);
  font-size: 13px;
}
.detail-thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid var(--line);
}
.admin-action {
  border-top: 1px solid var(--c-border);
  padding-top: 16px;
}
.admin-action label {
  font-weight: 600;
  font-size: 13px;
  display: block;
  margin-bottom: 8px;
}
.admin-action select {
  padding: 7px 10px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  font-size: 13px;
  flex: 1;
}
.gap-8 {
  gap: 8px;
}
</style>
