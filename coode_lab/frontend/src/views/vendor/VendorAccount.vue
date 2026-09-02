<script setup>
import { ref, onMounted } from 'vue'
import { vendorApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'

const vendorId = currentVendorId()

const tab = ref('profile')
const detail = ref(null)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const saved = ref('')

const form = ref({ vendorName: '', email: '' })

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

async function save() {
  saving.value = true
  error.value = ''
  saved.value = ''
  try {
    await vendorApi.update(vendorId, {
      vendorName: form.value.vendorName,
      email: form.value.email,
    })
    saved.value = '資料已更新'
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
    <div class="tabs" role="tablist">
      <button class="tab" :class="{ active: tab === 'profile' }" @click="tab = 'profile'">基本資料</button>
      <button class="tab" :class="{ active: tab === 'settings' }" @click="tab = 'settings'">帳號設定</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="saved" class="alert alert-success">{{ saved }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <template v-else-if="detail">
      <div class="grid-2">
        <div class="card">
          <h3>基本資料</h3>
          <div class="info-grid">
            <div class="info-row"><span class="muted">廠商名稱</span><b>{{ detail.vendorName }}</b></div>
            <div class="info-row"><span class="muted">Email</span><b>{{ detail.email }}</b></div>
            <div class="info-row">
              <span class="muted">帳號狀態</span>
              <span :class="['badge', statusBadgeClass(detail.status)]">{{ statusLabel(detail.status) }}</span>
            </div>
            <div class="info-row"><span class="muted">啟用時間</span><b>{{ formatDate(detail.activatedAt) }}</b></div>
            <div class="info-row"><span class="muted">建立時間</span><b>{{ formatDate(detail.createdAt) }}</b></div>
          </div>
          <div class="form-group">
            <div class="form-field"><label>廠商名稱</label><input v-model="form.vendorName" /></div>
            <div class="form-field"><label>Email</label><input v-model="form.email" /></div>
            <button class="btn btn-primary" :disabled="saving" @click="save">{{ saving ? '儲存中...' : '儲存變更' }}</button>
          </div>
        </div>

        <div class="card">
          <h3>帳號設定</h3>
          <p class="hint">
            可用來調整登入資訊。變更 Email 後，之後請以新的 Email 登入廠商後台。
          </p>
          <div class="form-field">
            <label>登入 Email</label>
            <input v-model="form.email" />
          </div>
          <div class="form-field">
            <label>密碼</label>
            <input type="password" value="••••••••" disabled />
          </div>
          <p class="hint muted">
            變更密碼請聯絡系統管理員（目前廠商後台不開放自行修改密碼）。
          </p>
          <button class="btn btn-primary" :disabled="saving" @click="save">{{ saving ? '儲存中...' : '儲存變更' }}</button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
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
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
h3 {
  margin-bottom: 14px;
}
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--c-border);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}
.form-field label {
  font-size: 13px;
  color: var(--c-text-light);
}
.form-field input {
  padding: 8px 10px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.hint {
  font-size: 13px;
  color: var(--c-text);
  margin-bottom: 14px;
}
.hint.muted {
  color: var(--c-text-light);
}
@media (max-width: 900px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>