<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '../../api'
import ProductCard from '../../components/ProductCard.vue'
import AppPagination from '../../components/AppPagination.vue'
import { categoryLabel } from '../../utils/format'

const router = useRouter()

const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const filters = ref({
  keyword: '',
  categoryType: '',
  style: '',
  color: '',
  size: '',
  pattern: '',
  minPrice: '',
  maxPrice: '',
})

const categories = ['TOP', 'OUTER', 'BOTTOM', 'SHOES', 'ACCESSORY']

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      page: page.value,
      ...filters.value,
    }
    const res = await productApi.filter(params)
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

function priceInput(key, e) {
  const raw = String(e.target.value).trim()
  filters.value[key] = raw === '' || Number(raw) < 0 ? '' : raw
}

function blockInvalidPriceKeys(e) {
  const k = e.key
  if (k === '-' || k === 'e' || k === 'E') e.preventDefault()
}

function goDetail(id) {
  router.push(`/store/product/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header">
      <h1>商城首頁</h1>
      <p>瀏覽精選商品，打造你的專屬穿搭</p>
    </div>

    <div class="card filter-panel">
      <div class="filter-top">
        <div class="search-wrap">
          <input
            v-model="filters.keyword"
            class="search-input"
            placeholder="搜尋商品名稱..."
            @keyup.enter="applySearch"
          />
          <button
            v-if="filters.keyword"
            type="button"
            class="clear-keyword"
            aria-label="清空搜尋文字"
            @click="clearKeyword"
          >
            ×
          </button>
        </div>
        <select v-model="filters.categoryType">
          <option value="">全部分類</option>
          <option v-for="c in categories" :key="c" :value="c">{{ categoryLabel(c) }}</option>
        </select>
        <select v-model="filters.style">
          <option value="">風格</option>
          <option value="休閒">休閒</option>
          <option value="街頭">街頭</option>
          <option value="機能">機能</option>
          <option value="運動">運動</option>
        </select>
        <select v-model="filters.color">
          <option value="">顏色</option>
          <option value="白">白</option>
          <option value="黑">黑</option>
          <option value="卡其">卡其</option>
        </select>
        <input
          :value="filters.minPrice"
          type="number"
          min="0"
          placeholder="最低價"
          class="price-input"
          @input="priceInput('minPrice', $event)"
          @keydown="blockInvalidPriceKeys"
        />
        <input
          :value="filters.maxPrice"
          type="number"
          min="0"
          placeholder="最高價"
          class="price-input"
          @input="priceInput('maxPrice', $event)"
          @keydown="blockInvalidPriceKeys"
        />
      </div>
      <div class="filter-actions">
        <button class="btn btn-primary" @click="applySearch">搜尋</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else-if="products.length === 0" class="empty">沒有符合條件的商品</div>

    <div v-else class="grid-4 product-grid">
      <ProductCard
        v-for="p in products"
        :key="p.productId"
        :product="p"
        @detail="goDetail"
      />
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />
  </div>
</template>

<style scoped>
.filter-panel {
  margin-bottom: 20px;
}
.filter-top {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.filter-top input,
.filter-top select {
  padding: 8px 10px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: #fff;
}
.search-wrap {
  position: relative;
  flex: 1;
  min-width: 180px;
}
.filter-top .search-input {
  width: 100%;
  padding: 8px 32px 8px 10px;
}
.search-input {
  flex: 1;
  min-width: 180px;
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
  background: #d1d5db;
  color: #fff;
  font-size: 14px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
}
.clear-keyword:hover {
  background: #9ca3af;
}
.price-input {
  width: 100px;
}
.filter-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}
</style>
