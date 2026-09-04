<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { productApi, uploadApi } from '../../api'
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
  sizeCustom: '',
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
const patternOptions = [
  { value: 'MEN', label: '男裝' },
  { value: 'WOMEN', label: '女裝' },
  { value: 'KIDS', label: '童裝' },
]
const colorOptions = ['白', '黑', '藍', '卡其', '軍綠', '灰', '米白']
const sizeOptions = ['XXS', 'XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL', 'F', 'U']

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

async function onFormImage(e, field) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  try {
    const res = await uploadApi.upload(file)
    if (res && res.url) form.value[field] = res.url
    else error.value = '圖片上傳失敗'
  } catch (err) {
    error.value = '圖片上傳失敗：' + err.message
  } finally {
    e.target.value = ''
  }
}

async function onVariantImage(e, i, field) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  try {
    const res = await uploadApi.upload(file)
    if (res && res.url) form.value.variants[i][field] = res.url
    else error.value = '圖片上傳失敗'
  } catch (err) {
    error.value = '圖片上傳失敗：' + err.message
  } finally {
    e.target.value = ''
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
        pattern: filters.pattern,
        color: filters.color,
        size: resolvedSize(),
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

function onSizeFilterChange() {
  if (filters.size !== '__custom__') {
    filters.sizeCustom = ''
  }
}

function resolvedSize() {
  return filters.size === '__custom__' ? filters.sizeCustom : filters.size
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

async function setToDraft(p) {
  try {
    await productApi.setToDraft(vendorId, p.productId)
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
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">VENDOR</div>
          <h1 class="vr-title">商品管理</h1>
          <p class="vr-subtitle">{{ activeTab === 'low' ? '低庫存（庫存 ≤ 5）；缺貨（庫存 = 0）' : '管理商品、規格（顏色×尺寸）庫存與販售狀態' }}</p>
        </div>
        <div class="vr-banner-controls">
          <button class="vr-btn vr-btn-primary" @click="openCreate">＋ 新增商品</button>
        </div>
      </div>
    </div>

    <div class="vr-tabs" role="tablist">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="vr-tab"
        :class="{ active: activeTab === t.key }"
        @click="changeTab(t.key)"
      >
        {{ t.label }}
        <span class="vr-tab-count">{{ counts[t.key === 'all' ? 'all' : (t.key === 'low' ? 'low' : t.status)] }}</span>
      </button>
    </div>

    <div v-if="showingFilters" class="vr-filter-bar">
      <input v-model="filters.keyword" placeholder="搜尋商品名稱" @keyup.enter="applySearch" />
      <select v-model="filters.categoryType">
        <option value="">全部分類</option>
        <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
      </select>
      <select v-model="filters.pattern">
        <option value="">全部版型</option>
        <option v-for="g in patternOptions" :key="g.value" :value="g.value">{{ g.label }}</option>
      </select>
      <select v-model="filters.color">
        <option value="">全部顏色</option>
        <option v-for="c in colorOptions" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="filters.size" @change="onSizeFilterChange">
        <option value="">全部尺寸</option>
        <option v-for="s in sizeOptions" :key="s" :value="s">{{ s }}</option>
        <option value="__custom__">其他（手動輸入）</option>
      </select>
      <input
        v-if="filters.size === '__custom__'"
        v-model="filters.sizeCustom"
        placeholder="輸入尺寸"
        class="size-custom"
        @keyup.enter="applySearch"
      />
      <input v-model.number="filters.minPrice" type="number" placeholder="最低價" class="price" />
      <input v-model.number="filters.maxPrice" type="number" placeholder="最高價" class="price" />
      <select v-if="activeTab === 'all'" v-model="filters.status">
        <option value="">全部狀態</option>
        <option value="ACTIVE">上架</option>
        <option value="DRAFT">待上架</option>
        <option value="INACTIVE">下架</option>
      </select>
      <button class="vr-btn vr-btn-sm vr-btn-primary" @click="applySearch">搜尋</button>
      <span class="spacer"></span>
      <button class="vr-btn vr-btn-sm vr-btn-outline" :disabled="selected.length === 0" @click="batch(true)">批次上架</button>
      <button class="vr-btn vr-btn-sm vr-btn-outline" :disabled="selected.length === 0" @click="batch(false)">批次下架</button>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>

    <div v-else-if="products.length === 0" class="vr-empty">目前沒有符合條件的商品</div>
    <div v-else class="vr-card">
      <table class="vr-table">
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
                <div style="color: var(--vr-mut); font-size: 12px">{{ statusLabel(p.pattern) }} · {{ categoryLabel(p.categoryType) }}</div>
              </td>
              <td>{{ (p.variants || []).length }}（{{ colorCount(p) }} 色）</td>
              <td :class="{ 'low-cell': totalStock(p) <= 5 }">
                {{ totalStock(p) }}
                <span v-if="totalStock(p) === 0" class="vr-badge vr-badge-danger">缺貨</span>
                <span v-else-if="totalStock(p) <= 5" class="vr-badge vr-badge-warning">低庫存</span>
              </td>
              <td>{{ formatMoney(p.price) }}</td>
              <td>
                <span v-if="p.status === 'ACTIVE'" class="vr-badge vr-badge-active">{{ productStatusLabel(p.status) }}</span>
                <span v-else-if="p.status === 'DRAFT'" class="vr-badge vr-badge-pending">{{ productStatusLabel(p.status) }}</span>
                <span v-else class="vr-badge vr-badge-inactive">{{ productStatusLabel(p.status) }}</span>
              </td>
              <td>
                <div style="display: flex; gap: 10px">
                  <button class="vr-btn vr-btn-sm vr-btn-outline" @click="openEdit(p)">編輯</button>
                  <button v-if="activeTab !== 'low'" class="vr-btn vr-btn-sm vr-btn-outline" @click="toggleStatus(p)">
                    {{ statusButtonLabel(activeTab, p) }}
                  </button>
                  <button
                    v-if="activeTab !== 'low' && p.status === 'INACTIVE'"
                    class="vr-btn vr-btn-sm vr-btn-outline"
                    @click="setToDraft(p)"
                  >
                    待上架
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
                        <span v-if="Number(v.stock) === 0" class="vr-badge vr-badge-danger">缺貨</span>
                        <span v-else-if="Number(v.stock) <= 5" class="vr-badge vr-badge-warning">低庫存</span>
                      </td>
                      <td>
                        <span v-if="v.status === 'ACTIVE'" class="vr-badge vr-badge-active">{{ statusLabel(v.status) }}</span>
                        <span v-else class="vr-badge vr-badge-inactive">{{ statusLabel(v.status) }}</span>
                      </td>
                      <td>
                        <div style="display: flex; gap: 10px">
                          <button class="vr-btn vr-btn-sm vr-btn-outline" @click="updateVariantStock(v)">修改庫存</button>
                          <button class="vr-btn vr-btn-sm vr-btn-outline" @click="toggleVariantStatus(v)">
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
                            class="vr-btn vr-btn-sm vr-btn-danger"
                            @click="batchVariantStatus(p, 'INACTIVE')"
                          >
                            停售
                          </button>
                          <button
                            class="vr-btn vr-btn-sm vr-btn-outline"
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

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 商品表單 -->
    <div v-if="showForm" class="vr-modal-mask">
      <div class="vr-modal product-form">
        <h3>{{ editing ? '編輯商品' : '新增商品' }}</h3>
        <div class="vr-form-row">
          <div class="vr-form-field"><label>商品名稱</label><input v-model="form.name" /></div>
          <div class="vr-form-field"><label>男裝 / 女裝 / 童裝</label>
            <select v-model="form.pattern">
              <option v-for="g in patternOptions" :key="g.value" :value="g.value">{{ g.label }}</option>
            </select>
          </div>
        </div>
        <div class="vr-form-row">
          <div class="vr-form-field"><label>商品分類</label>
            <select v-model="form.categoryType">
              <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
          </div>
          <div class="vr-form-field"><label>Style</label><input v-model="form.style" /></div>
        </div>
        <div class="vr-form-row">
          <div class="vr-form-field"><label>價格</label><input v-model.number="form.price" type="number" /></div>
        </div>
        <div class="vr-form-field"><label>商品說明</label><textarea v-model="form.description" rows="2"></textarea></div>
        <div class="vr-form-row">
          <div class="vr-form-field">
            <label>封面圖</label>
            <div class="img-picker">
              <template v-if="!form.imagesJpg">
                <label class="img-btn">
                  <input type="file" accept="image/*" @change="onFormImage($event, 'imagesJpg')" />
                  選擇檔案
                </label>
              </template>
              <div v-else class="img-prev">
                <img :src="form.imagesJpg" alt="封面圖" />
                <button type="button" class="img-remove" @click="form.imagesJpg = ''">移除</button>
              </div>
            </div>
          </div>
          <div class="vr-form-field">
            <label>Outfit 圖</label>
            <div class="img-picker">
              <template v-if="!form.outfitPng">
                <label class="img-btn">
                  <input type="file" accept="image/*" @change="onFormImage($event, 'outfitPng')" />
                  選擇檔案
                </label>
              </template>
              <div v-else class="img-prev">
                <img :src="form.outfitPng" alt="Outfit 圖" />
                <button type="button" class="img-remove" @click="form.outfitPng = ''">移除</button>
              </div>
            </div>
          </div>
        </div>
        <div v-if="error && showForm" class="vr-alert form-error">{{ error }}</div>

        <!-- 規格編輯 -->
        <div class="variant-editor">
          <div class="ve-head">
            <label>規格（顏色 × 尺寸 × 庫存）</label>
            <button class="vr-btn vr-btn-sm vr-btn-outline" @click="addVariantRow">＋ 新增規格</button>
          </div>
          <div
            v-for="(v, i) in form.variants"
            :key="i"
            class="ve-block"
          >
            <div class="ve-row">
              <input v-model="v.color" placeholder="顏色（如 白）" class="ve-input ve-color" />
              <input v-model="v.size" placeholder="尺寸（如 M）" class="ve-input ve-size" />
              <input v-model.number="v.stock" type="number" placeholder="庫存" class="ve-stock" />
              <button class="vr-btn vr-btn-sm vr-btn-danger" :disabled="form.variants.length <= 1" @click="removeVariantRow(i)">
                刪除
              </button>
            </div>
            <div class="ve-imgs">
              <div class="img-picker img-picker-sm">
                <span class="img-label">商品圖</span>
                <template v-if="!v.imagesJpg">
                  <label class="img-btn">
                    <input type="file" accept="image/*" @change="onVariantImage($event, i, 'imagesJpg')" />
                    選擇
                  </label>
                </template>
                <div v-else class="img-prev">
                  <img :src="v.imagesJpg" alt="商品圖" />
                  <button type="button" class="img-remove" @click="v.imagesJpg = ''">移除</button>
                </div>
              </div>
              <div class="img-picker img-picker-sm">
                <span class="img-label">試穿圖</span>
                <template v-if="!v.outfitPng">
                  <label class="img-btn">
                    <input type="file" accept="image/*" @change="onVariantImage($event, i, 'outfitPng')" />
                    選擇
                  </label>
                </template>
                <div v-else class="img-prev">
                  <img :src="v.outfitPng" alt="試穿圖" />
                  <button type="button" class="img-remove" @click="v.outfitPng = ''">移除</button>
                </div>
              </div>
            </div>
          </div>
          <div class="ve-hint" style="color: var(--vr-mut); font-size: 12px; margin-top: 2px">請新增至少一種以上的(顏色, 尺寸)規格</div>
        </div>

        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-primary" @click="submit">儲存</button>
          <button class="vr-btn vr-btn-outline" @click="showForm = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.price {
  width: 90px;
}
.size-custom {
  width: 100px;
}
.spacer {
  flex: 1;
}
.low-cell {
  color: var(--vr-down);
  font-weight: 700;
}
.link-expand {
  border: none;
  background: none;
  font-size: 13px;
  color: var(--vr-mut);
  cursor: pointer;
  margin-right: 2px;
}
.product-row:hover td {
  background: var(--vr-cream);
}
.variant-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--vr-cream);
  font-size: 13px;
}
.variant-table th {
  text-align: left;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--vr-mut);
  border-bottom: 1px solid var(--vr-line);
}
.variant-table td {
  padding: 6px 10px;
  border-bottom: 1px solid var(--vr-line);
}
.batch-suspend-row td {
  background: var(--vr-cream);
}
.batch-suspend {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.bs-label {
  font-size: 12px;
  color: var(--vr-mut);
  white-space: nowrap;
}
.batch-suspend select {
  padding: 5px 8px;
  border: 1px solid var(--vr-line);
  border-radius: 6px;
  font-size: 13px;
  background: var(--vr-paper);
}
.product-form {
  width: 720px;
  max-height: 90vh;
  overflow-y: auto;
}
.variant-editor {
  border: 1px solid var(--vr-line);
  border-radius: 10px;
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
.ve-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border: 1px dashed var(--vr-line);
  border-radius: 8px;
  background: var(--vr-paper);
}
.ve-row {
  display: grid;
  grid-template-columns: 96px 96px 96px auto;
  gap: 8px;
  align-items: center;
}
.ve-row input {
  padding: 7px 8px;
  border: 1px solid var(--vr-line);
  border-radius: 6px;
  font-size: 13px;
  min-width: 0;
}
.ve-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.img-picker {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.img-picker-sm .img-label {
  font-size: 12px;
  color: var(--vr-mut);
}
.img-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 14px;
  border: 1px dashed var(--vr-line);
  border-radius: 8px;
  background: var(--vr-cream);
  color: var(--vr-mut);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.img-btn:hover {
  border-color: var(--vr-brn);
  color: var(--vr-brn);
}
.img-btn input[type='file'] {
  display: none;
}
.img-prev {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.img-prev img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--vr-line);
  background: var(--vr-cream);
}
.img-remove {
  border: none;
  background: none;
  color: var(--vr-down);
  font-size: 12px;
  cursor: pointer;
  padding: 2px 0;
}
.form-error {
  margin-bottom: 8px;
}
.ve-hint {
  margin-top: 2px;
}
.vr-badge-warning {
  background: #fef3c7;
  color: #92400e;
}
</style>
