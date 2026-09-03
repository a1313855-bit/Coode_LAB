<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { productApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatMoney, statusBadgeClass, statusLabel, categoryLabel, productStatusLabel } from '../../utils/format'
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
  pattern: '',
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
  { value: 'DRESS', label: '洋裝' },
  { value: 'HEADWEAR', label: '帽子/頭飾' },
]
const genderOptions = [
  { value: 'MEN', label: '男裝' },
  { value: 'WOMEN', label: '女裝' },
  { value: 'KIDS', label: '童裝' },
]

const showingFilters = computed(() => activeTab.value !== 'low')
const showingBatch = computed(() => activeTab.value !== 'low')

// 商品總庫存 = 所有規格庫存加總
function totalStock(p) {
  return (p.variants || []).reduce((s, v) => s + Number(v.stock || 0), 0)
}

// 商品有幾個顏色 / 尺寸
function colorCount(p) {
  const set = new Set((p.variants || []).map((v) => v.color))
  return set.size
}

function emptyForm() {
  return {
    name: '',
    pattern: 'MEN',
    categoryType: 'TOP',
    style: '',
    price: 0,
    description: '',
    imagesJpg: '',
    outfitPng: '',
    status: 'DRAFT',
    variants: [{ color: '', size: '', stock: 0, imagesJpg: '', outfitPng: '', status: 'ACTIVE' }],
  }
}

function emptyVariant() {
  return { color: '', size: '', stock: 0, imagesJpg: '', outfitPng: '', status: 'ACTIVE' }
}

function addVariantRow() {
  form.value.variants.push(emptyVariant())
}

function removeVariantRow(i) {
  form.value.variants.splice(i, 1)
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
        pattern: filters.pattern,
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
    pattern: p.pattern || 'MEN',
    categoryType: p.categoryType,
    style: p.style,
    price: Number(p.price || 0),
    description: p.description,
    imagesJpg: p.imagesJpg,
    outfitPng: p.outfitPng,
    status: p.status,
    variants: (p.variants || []).map((v) => ({
      color: v.color,
      size: v.size,
      stock: Number(v.stock || 0),
      imagesJpg: v.imagesJpg,
      outfitPng: v.outfitPng,
      status: v.status || 'ACTIVE',
    })),
  }
  showForm.value = true
}

async function submit() {
  error.value = ''
  try {
    const body = {
      ...form.value,
      price: Number(form.value.price),
      variants: form.value.variants.map((v) => ({
        color: v.color,
        size: v.size,
        stock: Number(v.stock || 0),
        imagesJpg: v.imagesJpg,
        outfitPng: v.outfitPng,
        status: v.status || 'ACTIVE',
      })),
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

// 更新單一規格庫存
async function updateVariantStock(v) {
  const input = prompt(`更新「${v.color} / ${v.size}」庫存`, v.stock)
  if (input === null) return
  const val = Number(input)
  if (Number.isNaN(val) || val < 0) {
    error.value = '庫存必須為不小於 0 的數字'
    return
  }
  try {
    await productApi.updateVariantStock(vendorId, v.variantId, { stock: val })
    v.stock = val
    await Promise.all([load(), refreshCounts()])
  } catch (e) {
    error.value = e.message
  }
}

// 停售 / 恢復單一規格
async function toggleVariantStatus(v) {
  const next = v.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await productApi.updateVariantStatus(vendorId, v.variantId, { status: next })
    v.status = next
    await load()
  } catch (e) {
    error.value = e.message
  }
}

// 整批停售 / 恢復：依顏色 或 依尺寸（可同時指定 → 視為單一規格組合）
async function batchVariantStatus(p, status) {
  const scope = batchScope[p.productId] || {}
  const body = {
    color: scope.color || undefined,
    size: scope.size || undefined,
    status,
  }
  if (!body.color && !body.size) {
    error.value = '請先選擇要整批套用的顏色或尺寸'
    return
  }
  const label = [body.color, body.size].filter(Boolean).join(' + ')
  if (!window.confirm(`確定要將「${p.name}」中「${label}」的規格全部設為${status === 'ACTIVE' ? '可販售' : '停售'}嗎？`)) {
    return
  }
  try {
    await productApi.batchVariantStatus(vendorId, p.productId, body)
    await Promise.all([load(), refreshCounts()])
    error.value = ''
  } catch (e) {
    error.value = e.message
  }
}

// 商品展開（看規格）
const expandedId = ref(null)
function toggleExpand(p) {
  expandedId.value = expandedId.value === p.productId ? null : p.productId
}

// 每個商品可選進行整批操作的顏色 / 尺寸
const batchScope = reactive({})
function setBatchColor(p, value) {
  if (!batchScope[p.productId]) batchScope[p.productId] = {}
  batchScope[p.productId].color = value
}
function setBatchSize(p, value) {
  if (!batchScope[p.productId]) batchScope[p.productId] = {}
  batchScope[p.productId].size = value
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
        {{ activeTab === 'low' ? '商品底下有任一規格庫存 ≤ 5 即顯示（含缺貨商品）' : '管理商品、規格（顏色×尺寸）庫存與販售狀態' }}
      </p>
      <button class="btn btn-primary" @click="openCreate">＋ 新增商品</button>
    </div>

    <div v-if="showingFilters" class="card filter-bar">
      <input v-model="filters.keyword" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
      <select v-model="filters.categoryType">
        <option value="">全部分類</option>
        <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
      </select>
      <select v-model="filters.pattern">
        <option value="">全部性別</option>
        <option v-for="g in genderOptions" :key="g.value" :value="g.value">{{ g.label }}</option>
      </select>
      <input v-model="filters.color" placeholder="顏色" />
      <input v-model="filters.size" placeholder="尺寸" />
      <input v-model.number="filters.minPrice" type="number" placeholder="最低價" class="price" />
      <input v-model.number="filters.maxPrice" type="number" placeholder="最高價" class="price" />
      <select v-if="activeTab === 'all'" v-model="filters.status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">上架</option>
        <option value="DRAFT">待上架</option>
        <option value="INACTIVE">下架</option>
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
              <th>規格數</th>
              <th>總庫存</th>
              <th>價格</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="p in products" :key="p.productId">
              <tr class="product-row">
                <td v-if="showingBatch">
                  <input type="checkbox" :checked="selected.includes(p.productId)" @change="toggleSelect(p.productId)" />
                </td>
                <td>
                  <button class="link-expand" @click="toggleExpand(p)">
                    {{ expandedId === p.productId ? '▾' : '▸' }}
                  </button>
                  {{ p.name }}
                  <div class="muted small">{{ statusLabel(p.pattern) }} · {{ categoryLabel(p.categoryType) }}</div>
                </td>
                <td>{{ (p.variants || []).length }}（{{ colorCount(p) }} 色）</td>
                <td :class="{ 'low-cell': totalStock(p) <= 5 }">
                  {{ totalStock(p) }}
                  <span v-if="totalStock(p) === 0" class="badge badge-danger">缺貨</span>
                  <span v-else-if="totalStock(p) <= 5" class="badge badge-warning">低庫存</span>
                </td>
                <td>{{ formatMoney(p.price) }}</td>
                <td><span :class="['badge', statusBadgeClass(p.status)]">{{ productStatusLabel(p.status) }}</span></td>
                <td>
                  <div class="flex">
                    <button class="btn btn-sm" @click="openEdit(p)">編輯</button>
                    <button v-if="activeTab !== 'low'" class="btn btn-sm" @click="toggleStatus(p)">
                      {{ statusButtonLabel(activeTab, p) }}
                    </button>
                  </div>
                </td>
              </tr>

              <!-- 規格明細列 -->
              <tr v-if="expandedId === p.productId" class="variant-row">
                <td :colspan="showingBatch ? 7 : 6">
                  <table class="variant-table">
                    <thead>
                      <tr>
                        <th>顏色</th>
                        <th>尺寸</th>
                        <th>庫存</th>
                        <th>規格狀態</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="v in p.variants || []" :key="v.variantId">
                        <td>{{ v.color }}</td>
                        <td>{{ v.size }}</td>
                        <td :class="{ 'low-cell': Number(v.stock) <= 5 }">
                          {{ v.stock }}
                          <span v-if="Number(v.stock) === 0" class="badge badge-danger">缺貨</span>
                          <span v-else-if="Number(v.stock) <= 5" class="badge badge-warning">低庫存</span>
                        </td>
                        <td>
                          <span :class="['badge', statusBadgeClass(v.status)]">{{ statusLabel(v.status) }}</span>
                        </td>
                        <td>
                          <div class="flex">
                            <button class="btn btn-sm" @click="updateVariantStock(v)">修改庫存</button>
                            <button class="btn btn-sm" @click="toggleVariantStatus(v)">
                              {{ v.status === 'ACTIVE' ? '停售' : '恢復' }}
                            </button>
                          </div>
                        </td>
                      </tr>
                      <!-- 整批停售 / 恢復 -->
                      <tr class="batch-suspend-row">
                        <td colspan="5">
                          <div class="batch-suspend">
                            <span class="bs-label">整批停售 / 恢復：</span>
                            <select
                              :value="(batchScope[p.productId] || {}).color || ''"
                              @change="setBatchColor(p, $event.target.value)"
                            >
                              <option value="">所有顏色</option>
                              <option v-for="c in [...new Set((p.variants || []).map((v) => v.color))]" :key="c" :value="c">{{ c }}</option>
                            </select>
                            <select
                              :value="(batchScope[p.productId] || {}).size || ''"
                              @change="setBatchSize(p, $event.target.value)"
                            >
                              <option value="">所有尺寸</option>
                              <option v-for="s in [...new Set((p.variants || []).map((v) => v.size))]" :key="s" :value="s">{{ s }}</option>
                            </select>
                            <button
                              class="btn btn-sm btn-danger-v"
                              @click="batchVariantStatus(p, 'INACTIVE')"
                            >
                              停售
                            </button>
                            <button
                              class="btn btn-sm"
                              @click="batchVariantStatus(p, 'ACTIVE')"
                            >
                              恢復
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
            </template>
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
            <select v-model="form.pattern">
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
        <div class="form-row">
          <div class="form-field"><label>價格</label><input v-model.number="form.price" type="number" /></div>
        </div>
        <div class="form-field"><label>商品說明</label><textarea v-model="form.description" rows="2"></textarea></div>
        <div class="form-row">
          <div class="form-field"><label>封面圖</label><input v-model="form.imagesJpg" placeholder="imagesJpg" /></div>
          <div class="form-field"><label>Outfit 圖</label><input v-model="form.outfitPng" placeholder="outfitPng" /></div>
        </div>

        <!-- 規格編輯 -->
        <div class="variant-editor">
          <div class="ve-head">
            <label>規格（顏色 × 尺寸 × 庫存）</label>
            <button class="btn btn-sm" @click="addVariantRow">＋ 新增規格</button>
          </div>
          <div
            v-for="(v, i) in form.variants"
            :key="i"
            class="ve-row"
          >
            <input v-model="v.color" placeholder="顏色（如 白）" class="ve-input" />
            <input v-model="v.size" placeholder="尺寸（如 M）" class="ve-input" />
            <input v-model.number="v.stock" type="number" placeholder="庫存" class="ve-stock" />
            <input v-model="v.imagesJpg" placeholder="商品圖" class="ve-img" />
            <input v-model="v.outfitPng" placeholder="試穿圖" class="ve-img" />
            <button class="btn btn-sm danger-btn" :disabled="form.variants.length <= 1" @click="removeVariantRow(i)">
              刪除
            </button>
          </div>
          <div class="ve-hint muted small">至少需一種規格；後端會以（顏色, 尺寸）唯一組合管理庫存與販售狀態</div>
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
.link-expand {
  border: none;
  background: none;
  font-size: 13px;
  color: var(--c-text-light);
  cursor: pointer;
  margin-right: 2px;
}
.product-row:hover {
  background: #fdf9fb;
}
.variant-table {
  width: 100%;
  border-collapse: collapse;
  background: #fafafa;
  font-size: 13px;
}
.variant-table th {
  text-align: left;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--c-text-light);
  border-bottom: 1px solid var(--c-border);
}
.variant-table td {
  padding: 6px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.batch-suspend-row td {
  background: #fdf9fb;
}
.batch-suspend {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.bs-label {
  font-size: 12px;
  color: var(--c-text-light);
  white-space: nowrap;
}
.batch-suspend select {
  padding: 5px 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  font-size: 13px;
  background: #fff;
}
.btn-danger-v {
  border-color: #fecaca;
  color: #dc2626;
}
.btn-danger-v:hover {
  background: #fef2f2;
  color: #dc2626;
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
  width: 720px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal h3 {
  margin-bottom: 16px;
}
.variant-editor {
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 12px;
  margin: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ve-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.ve-head label {
  font-size: 13px;
  font-weight: 600;
}
.ve-row {
  display: grid;
  grid-template-columns: 90px 90px 70px 1fr 1fr auto;
  gap: 8px;
}
.ve-row input {
  padding: 7px 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  font-size: 13px;
}
.ve-hint {
  margin-top: 2px;
}
.danger-btn {
  border-color: #fecaca;
  color: #dc2626;
}
.danger-btn:hover {
  background: #fef2f2;
  color: #dc2626;
}
</style>