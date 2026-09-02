<script setup>
import { ref, computed, onMounted } from 'vue'
import { vendorApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'

const vendorId = currentVendorId()

const detail = ref(null)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const saved = ref('')

const form = ref({ vendorName: '', email: '' })

const canSaveName = computed(() => {
  const name = form.value.vendorName?.trim()
  return !!name && name !== (detail.value?.vendorName || '')
})

const contractValid = computed(() => {
  if (!detail.value) return false
  if (detail.value.status !== 'ACTIVE') return false
  const end = new Date(String(detail.value.contractExpiresAt).replace(' ', 'T'))
  if (Number.isNaN(end.getTime())) return false
  return end.getTime() > Date.now()
})

const remainingDays = computed(() => {
  if (!detail.value?.contractExpiresAt) return null
  const end = new Date(String(detail.value.contractExpiresAt).replace(' ', 'T'))
  if (Number.isNaN(end.getTime())) return null
  return Math.max(0, Math.ceil((end.getTime() - Date.now()) / 86400000))
})

const contractStatusText = computed(() => {
  if (!detail.value) return '-'
  if (detail.value.status !== 'ACTIVE') return '無效合約（帳號非啟用狀態）'
  return contractValid.value ? '合約有效' : '合約已到期'
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    detail.value = await vendorApi.byId(vendorId)
    form.value = { vendorName: detail.value.vendorName, email: detail.value.email }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

// 只允許修改廠商名稱；Email 為不可變更資訊，仍隨請求送給後端
async function saveName() {
  const name = form.value.vendorName?.trim()
  if (!name) return
  saving.value = true
  error.value = ''
  saved.value = ''
  try {
    await vendorApi.update(vendorId, {
      vendorName: name,
      email: detail.value.email,
    })
    saved.value = '廠商名稱已更新'
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="saved" class="alert alert-success">{{ saved }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <template v-else-if="detail">
      <!-- 基本資料 -->
      <div class="card">
        <h3>基本資料</h3>
        <div class="info-grid">
          <div class="name-edit">
            <div class="name-fields">
              <label for="vendor-name">廠商名稱</label>
              <input id="vendor-name" v-model="form.vendorName" @keyup.enter="saveName" />
            </div>
            <button class="btn btn-primary" :disabled="saving || !canSaveName" @click="saveName">
              {{ saving ? '修改中...' : '修改名稱' }}
            </button>
          </div>
          <div class="info-row"><span class="muted">Email</span><b>{{ detail.email }}</b></div>
          <div class="info-row"><span class="muted">啟用時間</span><b>{{ formatDate(detail.activatedAt) }}</b></div>
          <div class="info-row"><span class="muted">建立時間</span><b>{{ formatDate(detail.createdAt) }}</b></div>
        </div>
        <p class="hint muted">除廠商名稱外，其餘欄位皆為不可變更資訊。</p>
      </div>

      <!-- 合約資訊 -->
      <div class="card contract">
        <h3>合約資訊</h3>
        <div class="contract-head">
          <span :class="['badge', contractValid ? 'badge-success' : 'badge-danger']">
            {{ statusLabel(detail.status) }}
          </span>
          <b class="cs-value" :class="{ danger: !contractValid }">{{ contractStatusText }}</b>
        </div>
        <div class="contract-row">
          <div class="kpi">
            <span class="kpi-label">啟用時間</span>
            <span class="kpi-value">{{ formatDate(detail.activatedAt) }}</span>
          </div>
          <div class="kpi">
            <span class="kpi-label">合約到期時間</span>
            <span class="kpi-value" :class="{ danger: !contractValid }">{{ formatDate(detail.contractExpiresAt) }}</span>
          </div>
          <div class="kpi">
            <span class="kpi-label">剩餘合約天數</span>
            <span class="kpi-value" :class="{ danger: !contractValid }">
              {{ remainingDays }}
              <small>天</small>
            </span>
          </div>
        </div>
        <p class="hint muted contract-note">
          續約與停權由系統管理員在管理員後台處理。若你的合約即將到期，請聯絡店家管理員洽詢續約事宜。
        </p>
      </div>
    </template>
  </div>
</template>

<style scoped>
h3 {
  margin-bottom: 14px;
}
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--c-border);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.name-edit {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 480px;
}
.name-fields {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.name-fields label {
  font-size: 13px;
  color: var(--c-text-light);
}
.name-fields input {
  padding: 9px 12px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  font-size: 14px;
}
.name-fields input:focus {
  outline: none;
  border-color: var(--c-text);
}
.hint {
  font-size: 13px;
  color: var(--c-text);
  margin-bottom: 14px;
}
.hint.muted {
  color: var(--c-text-light);
}
/* 合約資訊（原「合約資訊」頁內容已合併到此） */
.contract {
  margin-top: 16px;
  border-left: 4px solid var(--c-success);
}
.contract .cs-value.danger {
  color: var(--c-danger);
}
.contract-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.cs-value {
  font-size: 18px;
  font-weight: 800;
}
.contract-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.kpi {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 16px;
  text-align: center;
}
.kpi-label {
  display: block;
  font-size: 13px;
  color: var(--c-text-light);
  margin-bottom: 8px;
}
.kpi-value {
  font-size: 18px;
  font-weight: 800;
}
.kpi-value.danger {
  color: var(--c-danger);
}
.kpi-value small {
  font-size: 12px;
  color: var(--c-text-light);
  font-weight: 400;
}
.contract-note {
  margin: 18px 0 0;
}
@media (max-width: 900px) {
  .name-edit {
    flex-direction: column;
    align-items: stretch;
  }
  .name-edit .btn {
    width: 100%;
  }
  .contract-row {
    grid-template-columns: 1fr;
  }
  .contract {
    border-left: none;
    border-top: 4px solid var(--c-success);
  }
}
</style>