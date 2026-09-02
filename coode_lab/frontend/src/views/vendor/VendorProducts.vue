<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { productApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatMoney, statusBadgeClass, statusLabel, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = currentVendorId()

const tabs = [
  { key: 'all', label: '所有商品', status: '' },
  { key: 'draft', label: '待上架', status: 'DRAFT' },
  { key: 'active', label: '已上架', status: 'ACTIVE' },
  { key: 'inactive', label: '已下架', status: 'INACTIVE' },
  { key: 'low', label: '低庫存', status: null },
]
const activeTab = ref('all')

const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const filters = reactive({
  keyword: '',
  categoryType: '',
  gender: '',
  color: '',
  size: '',
  minPrice: '',
  maxPrice: '',
  status: '',
})
const selected = ref([])

const counts = reactive({ all: 0, DRAFT: 0, ACTIVE: 0, INACTIVE: 0, low: 0 })

const showForm = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

const categoryOptions = [
  { value: 'TOP', label: '上衣' },
  { value: 'OUTER', label: '外套' },
  { value: 'BOTTOM', label: '褲子' },
  { value: 'SHOES', label: '鞋子' },
  { value: 'ACCESSORY', label: '配件' },
]
const genderOptions = [
  { value: 'MEN', label: '男裝' },
  { value: 'WOMEN', label: '女裝' },
  { value: 'KIDS', label: '童裝' },
]

const showingFilters = computed(() => activeTab.value !== 'low')
const showingBatch = computed(() => activeTab.value !== 'low')

function emptyForm() {
  return {
    name: '',
    pattern: '',
    categoryType: 'TOP',
    gender: 'MEN',
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
    const tab = tabs.find((t) => t.key === activeTab.value)
    let res
    if (tab.key === 'low') {
      res = await productApi.lowStock(vendorId, page.value)
    } else {
      const status = tab.key === 'all' ? filters.status : tab.status
      res = await productApi.vendorFilter({
        page: page.value,
        vendorId,
        keyword: filters.keyword,
        categoryType: filters.categoryType,
        gender: filters.gender,
        color: filters.color,
        size: filters.size,
        minPrice: filters.minPrice,
        maxPrice: filters.maxPrice,
        status,
      })
    }
    products.value = res.content || []
    page.value = res.page || 0
    totalPages.value = res.totalPages || 1
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function refreshCounts() {
  try {
    const requests = [
      productApi.vendorFilter({ page: 0, vendorId }),
      productApi.vendorFilter({ page: 0, vendorId, status: 'DRAFT' }),
      productApi.vendorFilter({ page: 0, vendorId, status: 'ACTIVE' }),
      productApi.vendorFilter({ page: 0, vendorId, status: 'INACTIVE' }),
      productApi.lowStock(vendorId, 0),
    ]
    const res = await Promise.all(requests)
    counts.all = res[0].totalElements || 0
    counts.DRAFT = res[1].totalElements || 0
    counts.ACTIVE = res[2].totalElements || 0
    counts.INACTIVE = res[3].totalElements || 0
    counts.low = res[4].totalElements || 0
  } catch (e) {
    /* ignore count failures */
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

function changeTab(key) {
  if (key === activeTab.value) return
  activeTab.value = key
  page.value = 0
  selected.value = []
  refreshCounts()
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
    gender: p.gender || 'MEN',
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
    const body = {
      ...form.value,
      price: Number(form.value.price),
      stock: Number(form.value.stock),
    }
    if (editing.value) {
      await productApi.update(vendorId, editing.value.productId, body)
    } else {
      await productApi.create(vendorId, body)
    }
    showForm.value = false
    await Promise.all([load(), refreshCounts()])
  } catch (e) {
    error.value = e.message
  }
}

async function toggleStatus(p) {
  try {
    if (p.status === 'ACTIVE') {
      await productApi.deactivate(vendorId, p.productId)
    } else {
      await productApi.activate(vendorId, p.productId)
    }
    await Promise.all([load(), refreshCounts()])
  } catch (e) {
    error.value = e.message
  }
}

async function updateStock(p) {
  const v = prompt(`更新「${p.name}」庫存`, p.stock)
  if (v === null) return
  try {
    await productApi.updateStock(vendorId, p.productId, { stock: Number(v) })
    await Promise.all([load(), refreshCounts()])
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

const allSelected = computed(() => {
  const list = products.value.map((p) => p.productId)
  return list.length > 0 && list.every((id) => selected.value.includes(id))
})

function toggleSelectAll() {
  if (allSelected.value) {
    selected.value = []
  } else {
    selected.value = products.value.map((p) => p.productId)
  }
}

async function batch(on) {
  if (selected.value.length === 0) return
  try {
    if (on) {
      await productApi.batchActivate(vendorId, selected.value)
    } else {
      await productApi.batchDeactivate(vendorId, selected.value)
    }
    selected.value = []
    await Promise.all([load(), refreshCounts()])
  } catch (e) {
    error.value = e.message
  }
}

function statusButtonLabel(tabKey, p) {
  if (tabKey === 'draft') return '上架'
  if (tabKey === 'active') return '下架'
  if (tabKey === 'inactive') return '重新上架'
  return p.status === 'ACTIVE' ? '下架' : '上架'
}

onMounted(() => {
  refreshCounts()
  load()
})
</script>

<template>
  <div class="admin-content">
    <div class="tabs" role="tablist">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="tab"
        :class="{ active: activeTab === t.key }"
        @click="changeTab(t.key)"
      >
        {{ t.label }}
        <span class="tab-count">{{ counts[t.key === 'all' ? 'all' : (t.key === 'low' ? 'low' : t.status)] }}</span>
      </button>
    </div>

    <div class="page-header">
      <p class="subtitle">
        {{ activeTab === 'low' ? '庫存 ≤ 5 的商品（含缺貨商品）' : '管理你的商品庫存與上架狀態' }}
      </p>
      <button class="btn btn-primary" @click="openCreate">＋ 新增商品</button>
    </div>

    <div v-if="showingFilters" class="card filter-bar">
      <input v-model="filters.keyword" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
      <select v-model="filters.categoryType">
        <option value="">全部分類</option>
        <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
      </select>
      <select v-model="filters.gender">
        <option value="">全部性別</option>
        <option v-for="g in genderOptions" :key="g.value" :value="g.value">{{ g.label }}</option>
      </select>
      <input v-model="filters.color" placeholder="顏色" />
      <input v-model="filters.size" placeholder="尺寸" />
      <input v-model.number="filters.minPrice" type="number" placeholder="最低價" class="price" />
      <input v-model.number="filters.maxPrice" type="number" placeholder="最高價" class="price" />
      <select v-if="activeTab === 'all'" v-model="filters.status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">啟用中</option>
        <option value="DRAFT">草稿</option>
        <option value="INACTIVE">未啟用</option>
      </select>
      <button class="btn btn-sm btn-primary" @click="applySearch">搜尋</button>
      <span class="spacer"></span>
      <button class="btn btn-sm" :disabled="selected.length === 0" @click="batch(true)">批次上架</button>
      <button class="btn btn-sm" :disabled="selected.length === 0" @click="batch(false)">批次下架</button>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else-if="products.length === 0" class="empty">目前沒有符合條件的商品</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th v-if="showingBatch" width="36">
                <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" />
              </th>
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
              <td v-if="showingBatch">
                <input type="checkbox" :checked="selected.includes(p.productId)" @change="toggleSelect(p.productId)" />
              </td>
              <td>
                {{ p.name }}
                <div class="muted small">{{ statusLabel(p.gender) }} · {{ categoryLabel(p.categoryType) }}</div>
              </td>
              <td>{{ categoryLabel(p.categoryType) }}</td>
              <td :class="{ 'low-cell': p.stock <= 5 }">
                {{ p.stock }}
                <span v-if="p.stock === 0" class="badge badge-danger">缺貨</span>
                <span v-else-if="p.stock <= 5" class="badge badge-warning">低庫存</span>
              </td>
              <td>{{ formatMoney(p.price) }}</td>
              <td><span :class="['badge', statusBadgeClass(p.status)]">{{ statusLabel(p.status) }}</span></td>
              <td>
                <div class="flex">
                  <button class="btn btn-sm" :class="{ 'btn-primary': activeTab === 'low' }" @click="openEdit(p)">
                    {{ activeTab === 'low' ? '前往編輯' : '編輯' }}
                  </button>
                  <button class="btn btn-sm" @click="updateStock(p)">
                    {{ activeTab === 'low' ? '補貨' : '庫存' }}
                  </button>
                  <button v-if="activeTab !== 'low'" class="btn btn-sm" @click="toggleStatus(p)">
                    {{ statusButtonLabel(activeTab, p) }}
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
          <div class="form-field"><label>商品名稱</label><input v-model="form.name" /></div>
          <div class="form-field"><label>男裝 / 女裝 / 童裝</label>
            <select v-model="form.gender">
              <option v-for="g in genderOptions" :key="g.value" :value="g.value">{{ g.label }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-field"><label>商品分類</label>
            <select v-model="form.categoryType">
              <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
          </div>
          <div class="form-field"><label>Style</label><input v-model="form.style" /></div>
        </div>
        <div class="form-row-3">
          <div class="form-field"><label>花紋</label><input v-model="form.pattern" /></div>
          <div class="form-field"><label>顏色</label><input v-model="form.color" /></div>
          <div class="form-field"><label>尺寸</label><input v-model="form.size" /></div>
        </div>
        <div class="form-row">
          <div class="form-field"><label>庫存</label><input v-model.number="form.stock" type="number" /></div>
          <div class="form-field"><label>價格</label><input v-model.number="form.price" type="number" /></div>
        </div>
        <div class="form-field"><label>商品說明</label><textarea v-model="form.description" rows="2"></textarea></div>
        <div class="form-row">
          <div class="form-field"><label>商品圖片</label><input v-model="form.imagesJpg" placeholder="imagesJpg" /></div>
          <div class="form-field"><label>Outfit 圖片</label><input v-model="form.outfitPng" placeholder="outfitPng" /></div>
        </div>
        <div class="form-field">
          <label>上架方式</label>
          <select v-model="form.status">
            <option value="ACTIVE">直接上架</option>
            <option value="DRAFT">待上架</option>
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
.tab-count {
  display: inline-block;
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.8;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.subtitle {
  color: var(--c-text-light);
  font-size: 14px;
}
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.filter-bar input,
.filter-bar select {
  padding: 8px 10px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.filter-bar input {
  min-width: 130px;
}
.price {
  width: 90px;
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