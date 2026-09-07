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
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">ADMIN</div>
          <h1 class="vr-title">管理員帳號</h1>
          <p class="vr-subtitle">管理後台管理員</p>
        </div>
        <div class="vr-banner-controls">
          <button class="vr-btn vr-btn-primary" @click="openCreate">+ 新增管理員</button>
        </div>
      </div>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>
    <div v-else class="vr-card">
      <table class="vr-table">
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
              <div style="display: flex; gap: 6px; flex-wrap: wrap">
                <button class="vr-btn vr-btn-sm vr-btn-outline" @click="openEdit(a)">編輯</button>
                <button class="vr-btn vr-btn-sm vr-btn-outline" @click="changePassword(a)">改密碼</button>
                <button class="vr-btn vr-btn-sm vr-btn-danger" :disabled="isSelf(a)" :title="isSelf(a) ? '不能刪除自己的帳號' : ''" @click="remove(a)">刪除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="showForm" class="vr-modal-mask">
      <div class="vr-modal">
        <h3>{{ editing ? '編輯管理員' : '新增管理員' }}</h3>
        <div class="vr-form-field"><label>Email</label><input v-model="form.email" /></div>
        <div v-if="!editing" class="vr-form-field"><label>密碼</label><input v-model="form.password" /></div>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-outline" @click="showForm = false">取消</button>
          <button class="vr-btn vr-btn-primary" @click="save">儲存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
