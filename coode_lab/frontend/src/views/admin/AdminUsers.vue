<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../api'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const users = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')
const keyword = ref('')
const status = ref('')

const showEdit = ref(false)
const editing = ref(null)
const form = ref({ email: '', name: '', phone: '', gender: '', birthday: '' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    if (keyword.value) {
      const list = await userApi.search(keyword.value)
      users.value = list || []
      totalPages.value = 1
    } else if (status.value) {
      const list = await userApi.findByStatus(status.value)
      users.value = list || []
      totalPages.value = 1
    } else {
      const res = await userApi.findAllPaged(page.value)
      users.value = res.content || []
      page.value = res.page || 0
      totalPages.value = res.totalPages || 1
    }
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

function openEdit(u) {
  editing.value = u
  form.value = {
    email: u.email,
    name: u.name,
    phone: u.phone,
    gender: u.gender,
    birthday: u.birthday,
  }
  showEdit.value = true
}

async function save() {
  error.value = ''
  try {
    await userApi.update(editing.value.userId, form.value)
    showEdit.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function toggle(u) {
  try {
    await userApi.toggleStatus(u.userId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function changePassword(u) {
  const pw = prompt(`重新設定 ${u.name || u.email} 的密碼`)
  if (!pw) return
  try {
    await userApi.changePassword(u.userId, {
      oldPassword: '',
      newPassword: pw,
      confirmPassword: pw,
    })
    alert('密碼已更新')
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">ADMIN</div>
          <h1 class="vr-title">會員管理</h1>
          <p class="vr-subtitle">管理會員帳號</p>
        </div>
      </div>
    </div>

    <div class="vr-filter-bar">
      <div class="vr-search-wrap">
        <input v-model="keyword" placeholder="搜尋姓名 / Email / 電話" @keyup.enter="applySearch" />
        <button v-if="keyword" type="button" class="vr-clear-keyword" aria-label="清空搜尋文字" @click="clearKeyword">×</button>
      </div>
      <select v-model="status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">啟用中</option>
        <option value="INACTIVE">未啟用</option>
      </select>
      <button class="vr-btn vr-btn-primary" @click="applySearch">搜尋</button>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>
    <div v-else class="vr-card">
      <table class="vr-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>Email</th>
            <th>電話</th>
            <th>性別</th>
            <th>生日</th>
            <th>狀態</th>
            <th>註冊時間</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.userId">
            <td>{{ u.userId }}</td>
            <td>{{ u.name }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.phone }}</td>
            <td>{{ statusLabel(u.gender) }}</td>
            <td>{{ u.birthday }}</td>
            <td><span :class="['vr-badge', statusBadgeClass(u.status)]">{{ statusLabel(u.status) }}</span></td>
            <td>{{ formatDate(u.createdAt) }}</td>
            <td>
              <div style="display: flex; gap: 6px; flex-wrap: wrap">
                <button class="vr-btn vr-btn-sm vr-btn-outline" @click="openEdit(u)">編輯</button>
                <button class="vr-btn vr-btn-sm vr-btn-danger" @click="toggle(u)">
                  {{ u.status === 'ACTIVE' ? '停用' : '啟用' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="showEdit" class="vr-modal-mask">
      <div class="vr-modal">
        <h3>編輯會員 #{{ editing.userId }}</h3>
        <div class="vr-form-field"><label>Email</label><input v-model="form.email" /></div>
        <div class="vr-form-field"><label>姓名</label><input v-model="form.name" /></div>
        <div class="vr-form-field"><label>電話</label><input v-model="form.phone" /></div>
        <div class="vr-form-row">
          <div class="vr-form-field"><label>性別</label>
            <select v-model="form.gender"><option value="MALE">男</option><option value="FEMALE">女</option></select>
          </div>
          <div class="vr-form-field"><label>生日</label><input v-model="form.birthday" type="date" /></div>
        </div>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-outline" @click="showEdit = false">取消</button>
          <button class="vr-btn vr-btn-primary" @click="save">儲存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
