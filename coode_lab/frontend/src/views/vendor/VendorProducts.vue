<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../../api'
import { formatMoney, statusBadgeClass, statusLabel, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = ref(1)
const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const filters = ref({ keyword: '', status: '' })
const selected = ref([])

const showForm = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

function emptyForm() {
  return {
    name: '',
    pattern: '',
    categoryType: 'TOP',
    style: '',
    color: '',
    size: '',
    stock: 0,
    price: 0,
    description: '',
    imagesJpg: '',
    outfitPng: '',
    status: 'DRAFT',
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = { page: page.value, vendorId: vendorId.value, ...filters.value }
    const res = await productApi.vendorFilter(params)
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

function openCreate() {
  editing.value = null
  form.value = emptyForm()
  showForm.value = true
}

function openEdit(p) {
  editing.value = p
  form.value = {
    name: p.name,
    pattern: p.pattern,
    categoryType: p.categoryType,
    style: p.style,
    color: p.color,
    size: p.size,
    stock: p.stock,
    price: p.price,
    description: p.description,
    imagesJpg: p.imagesJpg,
    outfitPng: p.outfitPng,
    status: p.status,
  }
  showForm.value = true
}

async function submit() {
  error.value = ''
  try {
    if (editing.value) {
      await productApi.update(vendorId.value, editing.value.productId, {
        ...form.value,
        price: Number(form.value.price),
        stock: Number(form.value.stock),
      })
    } else {
      await productApi.create(vendorId.value, {
        ...form.value,
        price: Number(form.value.price),
        stock: Number(form.value.stock),
      })
    }
    showForm.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function toggleStatus(p) {
  try {
    if (p.status === 'ACTIVE') {
      await productApi.deactivate(vendorId.value, p.productId)
    } else {
      await productApi.activate(vendorId.value, p.productId)
    }
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function toggleSelect(id) {
  if (selected.value.includes(id)) {
    selected.value = selected.value.filter((x) => x !== id)
  } else {
    selected.value.push(id)
  }
}

async function batch(on) {
  if (selected.value.length === 0) return
  try {
    if (on) {
      await productApi.batchActivate(vendorId.value, selected.value)
    } else {
      await productApi.batchDeactivate(vendorId.value, selected.value)
    }
    selected.value = []
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function updateStock(p) {
  const v = prompt(`更新「${p.name}」庫存`, p.stock)
  if (v === null) return
  try {
    await productApi.updateStock(vendorId.value, p.productId, { stock: Number(v) })
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
        <h1>商品管理</h1>
        <p>管理本廠商的商品</p>
      </div>
      <div class="flex">
        <input v-model.number="vendorId" type="number" placeholder="廠商 ID" class="uid" />
        <button class="btn btn-success" @click="openCreate">+ 新增商品</button>
      </div>
    </div>

    <div class="card filter-bar">
      <input v-model="filters.keyword" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
      <select v-model="filters.status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">啟用中</option>
        <option value="DRAFT">草稿</option>
        <option value="INACTIVE">未啟用</option>
      </select>
      <button class="btn btn-primary" @click="applySearch">搜尋</button>
      <span class="spacer"></span>
      <button class="btn btn-sm" :disabled="selected.length === 0" @click="batch(true)">批次上架</button>
      <button class="btn btn-sm" :disabled="selected.length === 0" @click="batch(false)">批次下架</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th></th>
              <th>ID</th>
              <th>商品</th>
              <th>分類</th>
              <th>庫存</th>
              <th>價格</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.productId">
              <td><input type="checkbox" :checked="selected.includes(p.productId)" @change="toggleSelect(p.productId)" /></td>
              <td>{{ p.productId }}</td>
              <td>{{ p.name }}</td>
              <td>{{ categoryLabel(p.categoryType) }}</td>
              <td :class="{ 'low-cell': p.stock <= 10 }">{{ p.stock }}</td>
              <td>{{ formatMoney(p.price) }}</td>
              <td><span :class="['badge', statusBadgeClass(p.status)]">{{ statusLabel(p.status) }}</span></td>
              <td>
                <div class="flex">
                  <button class="btn btn-sm" @click="openEdit(p)">編輯</button>
                  <button class="btn btn-sm" @click="updateStock(p)">庫存</button>
                  <button class="btn btn-sm" @click="toggleStatus(p)">
                    {{ p.status === 'ACTIVE' ? '下架' : '上架' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 商品表單 -->
    <div v-if="showForm" class="modal-mask">
      <div class="modal product-form">
        <h3>{{ editing ? '編輯商品' : '新增商品' }}</h3>
        <div class="form-row">
          <div class="form-field"><label>名稱</label><input v-model="form.name" /></div>
          <div class="form-field"><label>分類</label>
            <select v-model="form.categoryType">
              <option value="TOP">上衣</option>
              <option value="OUTER">外套</option>
              <option value="BOTTOM">褲子</option>
              <option value="SHOES">鞋子</option>
              <option value="ACCESSORY">配件</option>
            </select>
          </div>
        </div>
        <div class="form-row-3">
          <div class="form-field"><label>花紋</label><input v-model="form.pattern" /></div>
          <div class="form-field"><label>風格</label><input v-model="form.style" /></div>
          <div class="form-field"><label>顏色</label><input v-model="form.color" /></div>
        </div>
        <div class="form-row-3">
          <div class="form-field"><label>尺寸</label><input v-model="form.size" /></div>
          <div class="form-field"><label>庫存</label><input v-model.number="form.stock" type="number" /></div>
          <div class="form-field"><label>價格</label><input v-model.number="form.price" type="number" /></div>
        </div>
        <div class="form-field"><label>描述</label><textarea v-model="form.description" rows="2"></textarea></div>
        <div class="form-row">
          <div class="form-field"><label>主圖 (imagesJpg)</label><input v-model="form.imagesJpg" /></div>
          <div class="form-field"><label>穿搭圖 (outfitPng)</label><input v-model="form.outfitPng" /></div>
        </div>
        <div class="form-field">
          <label>狀態</label>
          <select v-model="form.status">
            <option value="ACTIVE">啟用中（直接上架）</option>
            <option value="DRAFT">草稿（待上架）</option>
          </select>
        </div>
        <div class="flex">
          <button class="btn btn-primary" @click="submit">儲存</button>
          <button class="btn" @click="showForm = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.uid {
  width: 70px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
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
.spacer {
  flex: 1;
}
.low-cell {
  color: var(--c-danger);
  font-weight: 700;
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
  max-width: 90vw;
}
.product-form {
  width: 540px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal h3 {
  margin-bottom: 16px;
}
</style>
