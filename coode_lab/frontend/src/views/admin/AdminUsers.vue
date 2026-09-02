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
  <div class="admin-content">
    <div class="page-header">
      <h1>會員管理</h1>
      <p>管理會員帳號</p>
    </div>

    <div class="card filter-bar">
      <input v-model="keyword" placeholder="搜尋姓名 / Email / 電話" @keyup.enter="applySearch" />
      <select v-model="status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">啟用中</option>
        <option value="INACTIVE">未啟用</option>
      </select>
      <button class="btn btn-primary" @click="applySearch">搜尋</button>
      <button class="btn" @click="keyword=''; status=''; applySearch()">清空</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
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
              <td><span :class="['badge', statusBadgeClass(u.status)]">{{ statusLabel(u.status) }}</span></td>
              <td>{{ formatDate(u.createdAt) }}</td>
              <td>
                <div class="flex">
                  <button class="btn btn-sm" @click="openEdit(u)">編輯</button>
                  <button class="btn btn-sm btn-danger" @click="toggle(u)">
                    {{ u.status === 'ACTIVE' ? '停用' : '啟用' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="showEdit" class="modal-mask">
      <div class="modal">
        <h3>編輯會員 #{{ editing.userId }}</h3>
        <div class="form-field"><label>Email</label><input v-model="form.email" /></div>
        <div class="form-field"><label>姓名</label><input v-model="form.name" /></div>
        <div class="form-field"><label>電話</label><input v-model="form.phone" /></div>
        <div class="form-row">
          <div class="form-field"><label>性別</label>
            <select v-model="form.gender"><option value="MALE">男</option><option value="FEMALE">女</option></select>
          </div>
          <div class="form-field"><label>生日</label><input v-model="form.birthday" type="date" /></div>
        </div>
        <div class="flex">
          <button class="btn btn-primary" @click="save">儲存</button>
          <button class="btn" @click="showEdit = false">取消</button>
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
