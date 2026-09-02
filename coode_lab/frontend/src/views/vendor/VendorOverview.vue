<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { productApi, vendorApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatDate, statusBadgeClass, statusLabel, categoryLabel } from '../../utils/format'

const vendorId = currentVendorId()

const counts = ref({ all: 0, ACTIVE: 0, DRAFT: 0, low: 0 })
const vendor = ref(null)
const lowProducts = ref([])
const loading = ref(true)
const error = ref('')

const storeStatus = computed(() => {
  if (!vendor.value) return 'unknown'
  if (vendor.value.status !== 'ACTIVE') return 'suspended'
  return remainingDays.value > 0 ? 'active' : 'expired'
})

const contractValid = computed(() => storeStatus.value === 'active')

const remainingDays = computed(() => {
  if (!vendor.value?.contractExpiresAt) return null
  const end = new Date(String(vendor.value.contractExpiresAt).replace(' ', 'T'))
  if (Number.isNaN(end.getTime())) return null
  return Math.max(0, Math.ceil((end.getTime() - Date.now()) / 86400000))
})

const storeStatusText = computed(() => {
  const map = {
    active: '商店正常營運中',
    suspended: '商店已停權或停用',
    expired: '合約已到期，請儘速續約',
    unknown: '資料載入中',
  }
  return map[storeStatus.value]
})

// 總庫存 = 所有規格庫存加總
function totalStock(p) {
  return (p.variants || []).reduce((s, v) => s + Number(v.stock || 0), 0)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [cnt, v, low] = await Promise.all([
      Promise.all([
        productApi.vendorFilter({ page: 0, vendorId }),
        productApi.vendorFilter({ page: 0, vendorId, status: 'ACTIVE' }),
        productApi.vendorFilter({ page: 0, vendorId, status: 'DRAFT' }),
        productApi.lowStock(vendorId, 0),
      ]),
      vendorApi.byId(vendorId),
      productApi.lowStock(vendorId, 0),
    ])
    counts.value = {
      all: cnt[0].totalElements || 0,
      ACTIVE: cnt[1].totalElements || 0,
      DRAFT: cnt[2].totalElements || 0,
      low: cnt[3].totalElements || 0,
    }
    vendor.value = v
    lowProducts.value = low.content || []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="overview">
    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <template v-else-if="vendor">
      <div class="grid-4 kpis">
        <div class="card kpi">
          <div class="kpi-label">商品總數</div>
          <div class="kpi-value">{{ counts.all }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
        <div class="card kpi">
          <div class="kpi-label">已上架商品數</div>
          <div class="kpi-value kpi-ok">{{ counts.ACTIVE }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
        <div class="card kpi">
          <div class="kpi-label">待上架商品數</div>
          <div class="kpi-value kpi-warn">{{ counts.DRAFT }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
        <div class="card kpi">
          <div class="kpi-label">低庫存商品數</div>
          <div class="kpi-value" :class="{ 'kpi-danger': counts.low > 0 }">{{ counts.low }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
      </div>

      <div class="grid-2">
        <div class="card">
          <h3>商店目前狀態</h3>
          <div class="status-line">
            <span :class="['badge', contractValid ? 'badge-success' : 'badge-danger']">
              {{ statusLabel(vendor.status) }}
            </span>
            <span class="status-text" :class="{ danger: !contractValid }">{{ storeStatusText }}</span>
          </div>
          <div class="info-grid">
            <div class="info-row"><span class="muted">廠商名稱</span><b>{{ vendor.vendorName }}</b></div>
            <div class="info-row"><span class="muted">Email</span><b>{{ vendor.email }}</b></div>
            <div class="info-row"><span class="muted">啟用時間</span><b>{{ formatDate(vendor.activatedAt) }}</b></div>
          </div>
        </div>

        <div class="card">
          <h3>合約到期資訊</h3>
          <div class="info-grid">
            <div class="info-row"><span class="muted">合約狀態</span><b>{{ contractValid ? '合約有效' : '合約已到期' }}</b></div>
            <div class="info-row"><span class="muted">合約到期時間</span><b>{{ formatDate(vendor.contractExpiresAt) }}</b></div>
            <div class="info-row">
              <span class="muted">剩餘合約天數</span>
              <b :class="{ danger: !contractValid }">{{ remainingDays }} 天</b>
            </div>
          </div>
          <div class="meter">
            <div class="meter-bar" :class="{ danger: !contractValid }" :style="{ width: contractValid ? '100%' : '12%' }"></div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-head">
          <h3>近期商品庫存狀況</h3>
          <RouterLink to="/vendor/products" class="kpi-link">前往商品管理 →</RouterLink>
        </div>
        <div v-if="lowProducts.length === 0" class="empty">目前沒有低於庫存警戒線的商品</div>
        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>商品</th>
                <th>分類</th>
                <th>庫存</th>
                <th>狀態</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in lowProducts" :key="p.productId">
                <td>{{ p.name }}</td>
                <td>{{ categoryLabel(p.categoryType) }}</td>
                <td :class="{ 'low-cell': totalStock(p) <= 5 }">{{ totalStock(p) }}</td>
                <td>
                  <span v-if="totalStock(p) === 0" class="badge badge-danger">缺貨</span>
                  <span v-else class="badge badge-warning">低庫存</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.kpi-label {
  font-size: 13px;
  color: var(--c-text-light);
}
.kpi-value {
  font-size: 28px;
  font-weight: 800;
  margin: 6px 0;
}
.kpi-ok {
  color: var(--c-success);
}
.kpi-warn {
  color: #eab308;
}
.kpi-danger {
  color: var(--c-danger);
}
.kpi-link {
  font-size: 13px;
  color: var(--c-primary);
  text-decoration: none;
}
.kpi-link:hover {
  text-decoration: underline;
}
h3 {
  margin-bottom: 14px;
}
.status-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.status-text {
  font-size: 14px;
  font-weight: 700;
}
.status-text.danger {
  color: var(--c-danger);
}
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}
.info-row b.danger {
  color: var(--c-danger);
}
.meter {
  height: 8px;
  background: var(--c-border);
  border-radius: 99px;
  margin-top: 18px;
  overflow: hidden;
}
.meter-bar {
  height: 100%;
  background: var(--c-success);
  border-radius: 99px;
}
.meter-bar.danger {
  background: var(--c-danger);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.low-cell {
  color: var(--c-danger);
  font-weight: 700;
}
@media (max-width: 900px) {
  .grid-4,
  .grid-2 {
    grid-template-columns: 1fr 1fr;
  }
}
</style>