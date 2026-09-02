<script setup>
import { ref, computed, onMounted } from 'vue'
import { vendorApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'

const vendorId = currentVendorId()

const vendor = ref(null)
const loading = ref(false)
const error = ref('')

const contractValid = computed(() => {
  if (!vendor.value) return false
  if (vendor.value.status !== 'ACTIVE') return false
  const end = new Date(String(vendor.value.contractExpiresAt).replace(' ', 'T'))
  if (Number.isNaN(end.getTime())) return false
  return end.getTime() > Date.now()
})

const remainingDays = computed(() => {
  if (!vendor.value?.contractExpiresAt) return null
  const end = new Date(String(vendor.value.contractExpiresAt).replace(' ', 'T'))
  if (Number.isNaN(end.getTime())) return null
  return Math.max(0, Math.ceil((end.getTime() - Date.now()) / 86400000))
})

const contractStatusText = computed(() => {
  if (!vendor.value) return '-'
  if (vendor.value.status !== 'ACTIVE') return '無效合約（帳號非啟用狀態）'
  return contractValid.value ? '合約有效' : '合約已到期'
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    vendor.value = await vendorApi.byId(vendorId)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else-if="vendor" class="contract-wrap">
      <div class="card highlight" :class="{ invalid: !contractValid }">
        <div class="hc-label">合約狀態</div>
        <div class="hc-value">
          <span :class="['badge', contractValid ? 'badge-success' : 'badge-danger']">
            {{ statusLabel(vendor.status) }}
          </span>
          {{ contractStatusText }}
        </div>
      </div>

      <div class="grid-3">
        <div class="card item">
          <div class="kpi-label">啟用時間</div>
          <div class="kpi-value">{{ formatDate(vendor.activatedAt) }}</div>
        </div>
        <div class="card item">
          <div class="kpi-label">合約到期時間</div>
          <div class="kpi-value" :class="{ danger: !contractValid }">{{ formatDate(vendor.contractExpiresAt) }}</div>
        </div>
        <div class="card item">
          <div class="kpi-label">剩餘合約天數</div>
          <div class="kpi-value" :class="{ danger: !contractValid }">
            {{ remainingDays }}
            <small class="unit">天</small>
          </div>
        </div>
      </div>

      <div class="card note">
        <h3>如何續約？</h3>
        <p>
          續約與停權由系統管理員在管理員後台處理。若你的合約即將到期，請聯絡店家管理員洽詢續約事宜。
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.contract-wrap {
  max-width: 860px;
}
.highlight {
  margin-bottom: 16px;
  padding: 20px 24px;
  border-left: 4px solid var(--c-success);
}
.highlight.invalid {
  border-left-color: var(--c-danger);
}
.hc-label {
  font-size: 13px;
  color: var(--c-text-light);
  margin-bottom: 8px;
}
.hc-value {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 800;
}
.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.item {
  text-align: center;
  padding: 20px 16px;
}
.kpi-label {
  font-size: 13px;
  color: var(--c-text-light);
  margin-bottom: 10px;
}
.kpi-value {
  font-size: 20px;
  font-weight: 800;
}
.kpi-value.danger {
  color: var(--c-danger);
}
.unit {
  font-size: 12px;
  color: var(--c-text-light);
  font-weight: 400;
}
.note h3 {
  margin-bottom: 8px;
}
.note p {
  font-size: 14px;
  color: var(--c-text-light);
  line-height: 1.6;
}
@media (max-width: 900px) {
  .grid-3 {
    grid-template-columns: 1fr;
  }
}
</style>