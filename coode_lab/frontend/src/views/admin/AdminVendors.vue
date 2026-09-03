<script setup>
import { ref, onMounted } from 'vue'
import { vendorApi } from '../../api'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendors = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')
const keyword = ref('')
const status = ref('')

const showForm = ref(false)
const editing = ref(null)
const form = ref({ vendorName: '', email: '', password: '', newPassword: '' })

const showContract = ref(false)
const contractVendor = ref(null)
const contractDate = ref('')

const showRenew = ref(false)
const renewVendor = ref(null)
const renewDate = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await vendorApi.filter(page.value, keyword.value, status.value)
    vendors.value = res.content || []
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
function applySearch() {
  page.value = 0
  load()
}
function clearKeyword() {
  keyword.value = ''
  page.value = 0
  load()
}

function openCreate() {
  editing.value = null
  form.value = { vendorName: '', email: '', password: '', newPassword: '' }
  showForm.value = true
}
function openEdit(v) {
  editing.value = v
  form.value = { vendorName: v.vendorName, email: v.email, password: '', newPassword: '' }
  showForm.value = true
}
async function save() {
  error.value = ''
  try {
    if (editing.value) {
      await vendorApi.update(editing.value.vendorId, {
        vendorName: form.value.vendorName,
        email: form.value.email,
      })
      if (form.value.newPassword) {
        await vendorApi.resetPassword(editing.value.vendorId, {
          newPassword: form.value.newPassword,
        })
      }
    } else {
      await vendorApi.create(form.value)
    }
    showForm.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function activate(v) {
  // 啟用需設定啟用時間與合約到期日
  contractVendor.value = v
  contractDate.value = new Date(Date.now() + 365 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  showContract.value = true
}

async function confirmActivate() {
  try {
    const expires = new Date(contractDate.value + 'T00:00:00').toISOString()
    await vendorApi.activate(contractVendor.value.vendorId, {
      activatedAt: new Date().toISOString(),
      contractExpiresAt: expires,
    })
    showContract.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function suspend(v) {
  if (!confirm(`確定停權廠商「${v.vendorName}」？`)) return
  try {
    await vendorApi.suspend(v.vendorId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function reactivate(v) {
  try {
    await vendorApi.reactivate(v.vendorId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function renewContract(v) {
  renewVendor.value = v
  const base = v.contractExpiresAt || new Date(Date.now() + 365 * 24 * 3600 * 1000).toISOString()
  renewDate.value = String(base).slice(0, 10)
  showRenew.value = true
}

async function confirmRenew() {
  if (!renewDate.value) {
    error.value = '請選擇續約到期日'
    return
  }
  try {
    await vendorApi.renewContract(renewVendor.value.vendorId, {
      contractExpiresAt: new Date(renewDate.value + 'T00:00:00').toISOString(),
    })
    showRenew.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div class="page-header flex-between">
      <div>
        <h1>廠商管理</h1>
        <p>管理合作廠商</p>
      </div>
      <button class="btn btn-success" @click="openCreate">+ 新增廠商</button>
    </div>

    <div class="card filter-bar">
      <div class="search-wrap">
        <input v-model="keyword" class="search-input" placeholder="搜尋廠商名稱 / Email" @keyup.enter="applySearch" />
        <button v-if="keyword" type="button" class="clear-keyword" aria-label="清空搜尋文字" @click="clearKeyword">×</button>
      </div>
      <select v-model="status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">啟用中</option>
        <option value="SUSPENDED">已停權</option>
        <option value="INACTIVE">未啟用</option>
      </select>
      <button class="btn btn-primary" @click="applySearch">搜尋</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>廠商名稱</th>
              <th>Email</th>
              <th>狀態</th>
              <th>合約啟用</th>
              <th>合約到期</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in vendors" :key="v.vendorId">
              <td>{{ v.vendorId }}</td>
              <td>{{ v.vendorName }}</td>
              <td>{{ v.email }}</td>
              <td><span :class="['badge', statusBadgeClass(v.status)]">{{ statusLabel(v.status) }}</span></td>
              <td>{{ formatDate(v.activatedAt) }}</td>
              <td>{{ formatDate(v.contractExpiresAt) }}</td>
              <td>
                <div class="flex">
                  <button class="btn btn-sm" @click="openEdit(v)">編輯</button>
                  <button v-if="v.status !== 'ACTIVE'" class="btn btn-sm btn-success" @click="activate(v)">啟用</button>
                  <button v-if="v.status === 'SUSPENDED'" class="btn btn-sm" @click="reactivate(v)">恢復</button>
                  <button v-if="v.status === 'ACTIVE'" class="btn btn-sm btn-danger" @click="suspend(v)">停權</button>
                  <button v-if="v.status === 'ACTIVE'" class="btn btn-sm" @click="renewContract(v)">續約</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="showForm" class="modal-mask">
      <div class="modal">
        <h3>{{ editing ? '編輯廠商' : '新增廠商' }}</h3>
        <div class="form-field"><label>廠商名稱</label><input v-model="form.vendorName" /></div>
        <div class="form-field"><label>Email</label><input v-model="form.email" /></div>
        <div v-if="!editing" class="form-field"><label>密碼</label><input v-model="form.password" type="password" /></div>
        <div v-if="editing" class="form-field"><label>重設密碼（留空不修改）</label><input v-model="form.newPassword" type="password" placeholder="輸入新密碼" /></div>
        <div class="flex">
          <button class="btn btn-primary" @click="save">儲存</button>
          <button class="btn" @click="showForm = false">取消</button>
        </div>
      </div>
    </div>

    <div v-if="showContract" class="modal-mask">
      <div class="modal">
        <h3>啟用廠商「{{ contractVendor.vendorName }}」</h3>
        <div class="form-field"><label>合約到期日</label><input v-model="contractDate" type="date" /></div>
        <div class="flex">
          <button class="btn btn-primary" @click="confirmActivate">確認啟用</button>
          <button class="btn" @click="showContract = false">取消</button>
        </div>
      </div>
    </div>

    <div v-if="showRenew" class="modal-mask">
      <div class="modal">
        <h3>續約廠商「{{ renewVendor.vendorName }}」</h3>
        <div class="form-field"><label>續約到期日</label><input v-model="renewDate" type="date" /></div>
        <div class="flex">
          <button class="btn btn-primary" @click="confirmRenew">確認續約</button>
          <button class="btn" @click="showRenew = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.search-wrap {
  position: relative;
  flex: 1;
  min-width: 200px;
}
.search-input {
  width: 100%;
  padding-right: 32px !important;
}
.clear-keyword {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: #d3cfc9;
  color: #fff;
  font-size: 14px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
}
.clear-keyword:hover {
  background: var(--ink);
}
.filter-bar input {
  padding: 8px 10px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  min-width: 180px;
}
.filter-bar select {
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
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
  width: 420px;
  max-width: 90vw;
}
.modal h3 {
  margin-bottom: 16px;
}
</style>
