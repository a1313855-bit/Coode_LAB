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
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">ADMIN</div>
          <h1 class="vr-title">商品管理</h1>
          <p class="vr-subtitle">查看與管理全站商品</p>
        </div>
      </div>
    </div>

    <div class="vr-filter-bar">
      <div class="vr-search-wrap">
        <input v-model="filters.keyword" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
        <button v-if="filters.keyword" type="button" class="vr-clear-keyword" aria-label="清空搜尋文字" @click="clearKeyword">×</button>
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
      <button class="vr-btn vr-btn-primary" @click="applySearch">搜尋</button>
    </div>

    <div v-if="success" class="vr-alert-success">{{ success }}</div>
    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>
    <div v-else class="vr-card">
      <table class="vr-table">
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
            <td><span :class="['vr-badge', statusBadgeClass(p.status)]">{{ productStatusLabel(p.status) }}</span></td>
            <td><button class="vr-btn vr-btn-sm vr-btn-outline" @click="openEdit(p)">編輯</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="editing" class="vr-modal-mask" @click.self="closeEdit">
      <div class="vr-modal">
        <h3>編輯商品 #{{ editing.productId }}</h3>
        <div class="vr-form-field"><label>商品名稱</label><input v-model="form.name" /></div>
        <div class="vr-form-field"><label>描述</label><textarea v-model="form.description" rows="2"></textarea></div>
        <div class="vr-form-row">
          <div class="vr-form-field"><label>價格</label><input v-model.number="form.price" type="number" min="0" /></div>
          <div class="vr-form-field"><label>分類</label>
            <select v-model="form.categoryType">
              <option value="TOP">上衣</option>
              <option value="OUTER">外套</option>
              <option value="BOTTOM">褲子</option>
              <option value="DRESS">洋裝</option>
              <option value="HEADWEAR">帽子/頭飾</option>
            </select>
          </div>
        </div>
        <div class="vr-form-row">
          <div class="vr-form-field"><label>風格</label><input v-model="form.style" placeholder="韓系 / 休閒 / 正式" /></div>
          <div class="vr-form-field"><label>版型</label>
            <select v-model="form.pattern">
              <option value="MEN">男裝</option>
              <option value="WOMEN">女裝</option>
              <option value="KIDS">童裝</option>
            </select>
          </div>
        </div>
        <div class="vr-form-field"><label>狀態</label>
          <select v-model="form.status">
            <option value="ACTIVE">上架</option>
            <option value="DRAFT">待上架</option>
            <option value="INACTIVE">下架</option>
          </select>
        </div>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-outline" @click="closeEdit">取消</button>
          <button class="vr-btn vr-btn-primary" @click="saveEdit">儲存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.muted {
  color: var(--vr-mut);
}
.vendor {
  width: 120px !important;
}
.low-cell {
  color: var(--vr-down);
  font-weight: 700;
}
</style>
