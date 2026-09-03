<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../../api'
import { formatMoney, statusBadgeClass, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')
const success = ref('')

const filters = ref({ keyword: '', status: '', vendorName: '', categoryType: '' })

const editing = ref(null)
const form = ref({ name: '', description: '', price: 0, categoryType: 'TOP', style: '', pattern: '', status: 'ACTIVE' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await productApi.adminFilter({ page: page.value, ...filters.value })
    products.value = res.content || []
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
  filters.value.keyword = ''
  page.value = 0
  load()
}

function totalStock(p) {
  return (p.variants || []).reduce((s, v) => s + Number(v.stock || 0), 0)
}

function productStatusLabel(status) {
  const map = { ACTIVE: '上架', DRAFT: '待上架', INACTIVE: '下架' }
  return map[String(status || '').toUpperCase()] || status || '-'
}

function openEdit(p) {
  editing.value = p
  form.value = {
    name: p.name,
    description: p.description || '',
    price: p.price,
    categoryType: p.categoryType,
    style: p.style || '',
    pattern: p.pattern || 'MEN',
    status: p.status,
  }
}

function closeEdit() {
  editing.value = null
}

async function saveEdit() {
  error.value = ''
  success.value = ''
  try {
    await productApi.adminUpdate(editing.value.productId, form.value)
    success.value = '已更新「' + editing.value.name + '」'
    editing.value = null
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div class="page-header">
      <h1>商品管理</h1>
      <p>查看與管理全站商品</p>
    </div>

    <div class="card filter-bar">
      <div class="search-wrap">
        <input v-model="filters.keyword" class="search-input" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
        <button v-if="filters.keyword" type="button" class="clear-keyword" aria-label="清空搜尋文字" @click="clearKeyword">×</button>
      </div>
      <select v-model="filters.categoryType">
        <option value="">全部分類</option>
        <option value="TOP">上衣</option>
        <option value="OUTER">外套</option>
        <option value="BOTTOM">褲子</option>
        <option value="DRESS">洋裝</option>
        <option value="HEADWEAR">帽子/頭飾</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">上架</option>
        <option value="DRAFT">待上架</option>
        <option value="INACTIVE">下架</option>
      </select>
      <input v-model="filters.vendorName" placeholder="廠商名稱" class="vendor" />
      <button class="btn btn-primary" @click="applySearch">搜尋</button>
    </div>

    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>商品</th>
              <th>分類</th>
              <th>廠商</th>
              <th>庫存</th>
              <th>價格</th>
              <th>狀態</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.productId">
              <td>{{ p.productId }}</td>
              <td>{{ p.name }}</td>
              <td>{{ categoryLabel(p.categoryType) }}</td>
              <td>{{ p.vendorName }} <span class="muted">(#{{ p.vendorId }})</span></td>
              <td :class="{ 'low-cell': totalStock(p) <= 10 }">{{ totalStock(p) }}</td>
              <td>{{ formatMoney(p.price) }}</td>
              <td><span :class="['badge', statusBadgeClass(p.status)]">{{ productStatusLabel(p.status) }}</span></td>
              <td><button class="btn btn-sm" @click="openEdit(p)">編輯</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="editing" class="modal-overlay" @click.self="closeEdit">
      <div class="modal">
        <h3>編輯商品 #{{ editing.productId }}</h3>
        <div class="form-field"><label>商品名稱</label><input v-model="form.name" /></div>
        <div class="form-field"><label>描述</label><textarea v-model="form.description" rows="2"></textarea></div>
        <div class="form-row">
          <div class="form-field"><label>價格</label><input v-model.number="form.price" type="number" min="0" /></div>
          <div class="form-field"><label>分類</label>
            <select v-model="form.categoryType">
              <option value="TOP">上衣</option>
              <option value="OUTER">外套</option>
              <option value="BOTTOM">褲子</option>
              <option value="DRESS">洋裝</option>
              <option value="HEADWEAR">帽子/頭飾</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-field"><label>風格</label><input v-model="form.style" placeholder="韓系 / 休閒 / 正式" /></div>
          <div class="form-field"><label>版型</label>
            <select v-model="form.pattern">
              <option value="MEN">男裝</option>
              <option value="WOMEN">女裝</option>
              <option value="KIDS">童裝</option>
            </select>
          </div>
        </div>
        <div class="form-field"><label>狀態</label>
          <select v-model="form.status">
            <option value="ACTIVE">上架</option>
            <option value="DRAFT">待上架</option>
            <option value="INACTIVE">下架</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="closeEdit">取消</button>
          <button class="btn btn-primary" @click="saveEdit">儲存</button>
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
  min-width: 160px;
}
.filter-bar select {
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.vendor {
  width: 120px !important;
}
.low-cell {
  color: var(--c-danger);
  font-weight: 700;
}
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: var(--paper, #fff);
  border: 1px solid var(--line, #e5e5e5);
  border-radius: 12px;
  padding: 28px 32px;
  width: 480px; max-width: 90vw;
  max-height: 85vh; overflow-y: auto;
}
.modal h3 { margin: 0 0 16px; }
.form-field { margin-bottom: 12px; }
.form-field label { display: block; font-size: 13px; margin-bottom: 4px; font-weight: 600; }
.form-field input, .form-field select, .form-field textarea {
  width: 100%; padding: 8px 10px;
  border: 1px solid var(--c-border, #ccc);
  border-radius: 8px; box-sizing: border-box;
}
.form-row { display: flex; gap: 12px; }
.form-row .form-field { flex: 1; }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.alert-success { color: #16a34a; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 8px 12px; margin-bottom: 12px; }
</style>
