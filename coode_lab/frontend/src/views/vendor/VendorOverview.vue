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
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">VENDOR</div>
          <h1 class="vr-title">總覽</h1>
          <p class="vr-subtitle">查看商店狀態、商品統計與合約資訊</p>
        </div>
      </div>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>

    <template v-else-if="vendor">
      <div class="vr-kpis">
        <div class="vr-card">
          <div class="vr-kpi-label">商品總數</div>
          <div class="vr-kpi-value">{{ counts.all }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
        <div class="vr-card">
          <div class="vr-kpi-label">已上架商品數</div>
          <div class="vr-kpi-value" style="color: var(--vr-up)">{{ counts.ACTIVE }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
        <div class="vr-card">
          <div class="vr-kpi-label">待上架商品數</div>
          <div class="vr-kpi-value" style="color: #eab308">{{ counts.DRAFT }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
        <div class="vr-card">
          <div class="vr-kpi-label">低庫存商品數</div>
          <div class="vr-kpi-value" :class="{ danger: counts.low > 0 }">{{ counts.low }}</div>
          <RouterLink to="/vendor/products" class="kpi-link">前往管理</RouterLink>
        </div>
      </div>

      <div class="vr-grid-2">
        <div class="vr-card">
          <h3>商店目前狀態</h3>
          <div class="status-line">
            <span :class="['vr-badge', contractValid ? 'vr-badge-active' : 'vr-badge-danger']">
              {{ statusLabel(vendor.status) }}
            </span>
            <span class="status-text" :class="{ danger: !contractValid }">{{ storeStatusText }}</span>
          </div>
          <div class="vr-info-grid">
            <div class="vr-info-row"><span class="vr-label">廠商名稱</span><span class="vr-val">{{ vendor.vendorName }}</span></div>
            <div class="vr-info-row"><span class="vr-label">Email</span><span class="vr-val">{{ vendor.email }}</span></div>
            <div class="vr-info-row"><span class="vr-label">啟用時間</span><span class="vr-val">{{ formatDate(vendor.activatedAt) }}</span></div>
          </div>
        </div>

        <div class="vr-card">
          <h3>合約到期資訊</h3>
          <div class="vr-info-grid">
            <div class="vr-info-row"><span class="vr-label">合約狀態</span><span class="vr-val">{{ contractValid ? '合約有效' : '合約已到期' }}</span></div>
            <div class="vr-info-row"><span class="vr-label">合約到期時間</span><span class="vr-val">{{ formatDate(vendor.contractExpiresAt) }}</span></div>
            <div class="vr-info-row">
              <span class="vr-label">剩餘合約天數</span>
              <span class="vr-val" :class="{ danger: !contractValid }">{{ remainingDays }} 天</span>
            </div>
          </div>
          <div class="vr-meter" style="margin-top: 18px">
            <div class="vr-meter-bar" :class="{ danger: !contractValid }" :style="{ width: contractValid ? '100%' : '12%' }"></div>
          </div>
        </div>
      </div>

      <div class="vr-card">
        <div class="vr-card-head">
          <h3 class="vr-card-title">近期商品庫存狀況</h3>
          <RouterLink to="/vendor/products" class="kpi-link">前往商品管理 →</RouterLink>
        </div>
        <div v-if="lowProducts.length === 0" class="vr-empty">目前沒有低於庫存警戒線的商品</div>
        <div v-else>
          <table class="vr-table">
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
                  <span v-if="totalStock(p) === 0" class="vr-badge vr-badge-danger">缺貨</span>
                  <span v-else class="vr-badge vr-badge-warning">低庫存</span>
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
.kpi-link {
  font-size: 13px;
  color: var(--vr-brown);
  text-decoration: none;
}
.kpi-link:hover {
  text-decoration: underline;
}
h3 {
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 800;
  color: var(--vr-brown-dk);
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
  color: var(--vr-ink);
}
.status-text.danger {
  color: var(--vr-down);
}
.low-cell {
  color: var(--vr-down);
  font-weight: 700;
}
.vr-badge-warning {
  background: #fef3c7;
  color: #92400e;
}
</style>
