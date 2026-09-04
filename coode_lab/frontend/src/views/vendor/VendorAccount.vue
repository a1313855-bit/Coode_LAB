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

// 修改密碼
const pwForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })
const savingPw = ref(false)

const canSavePw = computed(() => {
  return !!(pwForm.value.currentPassword && pwForm.value.newPassword
    && pwForm.value.newPassword === pwForm.value.confirmPassword)
})

async function savePassword() {
  if (pwForm.value.newPassword.length < 6) {
    error.value = '新密碼長度至少 6 碼'
    return
  }
  savingPw.value = true
  error.value = ''
  saved.value = ''
  try {
    await vendorApi.changePassword(vendorId, {
      currentPassword: pwForm.value.currentPassword,
      newPassword: pwForm.value.newPassword,
    })
    saved.value = '密碼已更新'
    pwForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    error.value = e.message
  } finally {
    savingPw.value = false
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
          <h1 class="vr-title">帳號設定</h1>
          <p class="vr-subtitle">管理帳號資料、密碼與合約資訊</p>
        </div>
      </div>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="saved" class="vr-alert-success">{{ saved }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>

    <template v-else-if="detail">
      <!-- 基本資料 -->
      <div class="vr-card">
        <h3>基本資料</h3>
        <div class="vr-info-grid" style="margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--vr-line)">
          <div class="name-edit">
            <div class="name-fields">
              <label for="vendor-name">廠商名稱</label>
              <input id="vendor-name" v-model="form.vendorName" @keyup.enter="saveName" />
            </div>
            <button class="vr-btn vr-btn-primary" :disabled="saving || !canSaveName" @click="saveName">
              {{ saving ? '修改中...' : '修改名稱' }}
            </button>
          </div>
          <div class="vr-info-row"><span class="vr-label">Email</span><span class="vr-val">{{ detail.email }}</span></div>
          <div class="vr-info-row"><span class="vr-label">啟用時間</span><span class="vr-val">{{ formatDate(detail.activatedAt) }}</span></div>
          <div class="vr-info-row"><span class="vr-label">建立時間</span><span class="vr-val">{{ formatDate(detail.createdAt) }}</span></div>
        </div>
      </div>

      <!-- 修改密碼 -->
      <div class="vr-card pw-card">
        <h3>修改密碼</h3>
        <div class="pw-fields">
          <div class="pw-field">
            <label>目前密碼</label>
            <input v-model="pwForm.currentPassword" type="password" placeholder="請輸入目前密碼" />
          </div>
          <div class="pw-field">
            <label>新密碼</label>
            <input v-model="pwForm.newPassword" type="password" placeholder="請輸入新密碼" />
          </div>
          <div class="pw-field">
            <label>確認新密碼</label>
            <input v-model="pwForm.confirmPassword" type="password" placeholder="再次輸入新密碼" @keyup.enter="savePassword" />
          </div>
          <button class="vr-btn vr-btn-primary" :disabled="savingPw || !canSavePw" @click="savePassword">
            {{ savingPw ? '修改中...' : '修改密碼' }}
          </button>
        </div>
      </div>

      <!-- 合約資訊 -->
      <div class="vr-card contract">
        <h3>合約資訊</h3>
        <div class="contract-head">
          <span :class="['vr-badge', contractValid ? 'vr-badge-active' : 'vr-badge-danger']">
            {{ statusLabel(detail.status) }}
          </span>
          <b class="cs-value" :class="{ danger: !contractValid }">{{ contractStatusText }}</b>
        </div>
        <div class="contract-row">
          <div class="kpi">
            <span class="vr-kpi-label">啟用時間</span>
            <span class="vr-kpi-value">{{ formatDate(detail.activatedAt) }}</span>
          </div>
          <div class="kpi">
            <span class="vr-kpi-label">合約到期時間</span>
            <span class="vr-kpi-value" :class="{ danger: !contractValid }">{{ formatDate(detail.contractExpiresAt) }}</span>
          </div>
          <div class="kpi">
            <span class="vr-kpi-label">剩餘合約天數</span>
            <span class="vr-kpi-value" :class="{ danger: !contractValid }">
              {{ remainingDays }}
              <small>天</small>
            </span>
          </div>
        </div>
        <p style="font-size: 13px; color: var(--vr-mut); margin: 18px 0 0">
          若你的合約即將到期，請聯絡店家管理員洽詢續約事宜。
        </p>
      </div>
    </template>
  </div>
</template>

<style scoped>
h3 {
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 800;
  color: var(--vr-brown-dk);
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
  color: var(--vr-mut);
}
.name-fields input {
  padding: 9px 12px;
  border: 1px solid var(--vr-line);
  border-radius: 10px;
  font-size: 14px;
  color: var(--vr-ink);
}
.name-fields input:focus {
  outline: none;
  border-color: var(--vr-brown-mid);
}
.pw-card {
  margin-top: 16px;
}
.pw-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 480px;
  margin-bottom: 8px;
}
.pw-fields label {
  font-size: 13px;
  color: var(--vr-mut);
  display: block;
  margin-bottom: 6px;
}
.pw-fields input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--vr-line);
  border-radius: 10px;
  font-size: 14px;
  color: var(--vr-ink);
}
.pw-fields input:focus {
  outline: none;
  border-color: var(--vr-brown-mid);
}
.pw-fields .vr-btn {
  align-self: flex-start;
}
/* 合約資訊 */
.contract {
  margin-top: 16px;
  border-left: 4px solid var(--vr-up);
}
.contract .cs-value.danger {
  color: var(--vr-down);
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
  border: 1px solid var(--vr-line);
  border-radius: 10px;
  padding: 16px;
  text-align: center;
}
.kpi small {
  font-size: 12px;
  color: var(--vr-mut);
  font-weight: 400;
}
@media (max-width: 900px) {
  .name-edit {
    flex-direction: column;
    align-items: stretch;
  }
  .name-edit .vr-btn {
    width: 100%;
  }
  .contract-row {
    grid-template-columns: 1fr;
  }
  .contract {
    border-left: none;
    border-top: 4px solid var(--vr-up);
  }
}
</style>
