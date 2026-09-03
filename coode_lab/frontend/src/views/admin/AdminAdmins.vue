<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { useAuth } from '../../composables/auth'
import AppPagination from '../../components/AppPagination.vue'

const admins = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const showForm = ref(false)
const editing = ref(null)
const form = ref({ email: '', password: '' })

const auth = useAuth().value

function isSelf(a) {
  return auth && a.email === auth.email
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await adminApi.all(page.value)
    admins.value = res.content || []
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

function openCreate() {
  editing.value = null
  form.value = { email: '', password: '' }
  showForm.value = true
}
function openEdit(a) {
  editing.value = a
  form.value = { email: a.email, password: '' }
  showForm.value = true
}
async function save() {
  error.value = ''
  try {
    if (editing.value) {
      await adminApi.update(editing.value.adminId, { email: form.value.email })
    } else {
      await adminApi.create(form.value)
    }
    showForm.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function changePassword(a) {
  const pw = prompt(`重新設定管理員 ${a.email} 的密碼`)
  if (!pw) return
  try {
    await adminApi.changePassword(a.adminId, { newPassword: pw })
    alert('密碼已更新')
  } catch (e) {
    error.value = e.message
  }
}

async function remove(a) {
  if (isSelf(a)) {
    alert('不能刪除自己的帳號')
    return
  }
  if (!confirm(`確定刪除管理員 ${a.email}？`)) return
  try {
    await adminApi.remove(a.adminId)
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
        <h1>管理員帳號</h1>
        <p>管理後台管理員</p>
      </div>
      <button class="btn btn-success" @click="openCreate">+ 新增管理員</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in admins" :key="a.adminId">
              <td>{{ a.adminId }}</td>
              <td>{{ a.email }}</td>
              <td>
                <div class="flex">
                  <button class="btn btn-sm" @click="openEdit(a)">編輯</button>
                  <button class="btn btn-sm" @click="changePassword(a)">改密碼</button>
                  <button class="btn btn-sm btn-danger" :disabled="isSelf(a)" :title="isSelf(a) ? '不能刪除自己的帳號' : ''" @click="remove(a)">刪除</button>
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
        <h3>{{ editing ? '編輯管理員' : '新增管理員' }}</h3>
        <div class="form-field"><label>Email</label><input v-model="form.email" /></div>
        <div v-if="!editing" class="form-field"><label>密碼</label><input v-model="form.password" /></div>
        <div class="flex">
          <button class="btn btn-primary" @click="save">儲存</button>
          <button class="btn" @click="showForm = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
  width: 400px;
  max-width: 90vw;
}
.modal h3 {
  margin-bottom: 16px;
}
</style>
