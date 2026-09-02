<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { productApi } from '../../api'
import ProductCard from '../../components/ProductCard.vue'
import AppPagination from '../../components/AppPagination.vue'
import { categoryLabel } from '../../utils/format'
import heroImg from '../../assets/coode-fashion/hero.svg'
import catTop from '../../assets/coode-fashion/cat-top.svg'
import catOuter from '../../assets/coode-fashion/cat-outer.svg'
import catBottom from '../../assets/coode-fashion/cat-bottom.svg'

const router = useRouter()
const route = useRoute()

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

const categories = [
  { type: 'TOP', label: '上衣', img: catTop },
  { type: 'OUTER', label: '外套', img: catOuter },
  { type: 'BOTTOM', label: '下著', img: catBottom },
  { type: 'DRESS', label: '洋裝', img: catTop },
  { type: 'HEADWEAR', label: '帽子/頭飾', img: catOuter },
]

const productSection = ref(null)

function scrollToProducts() {
  if (productSection.value) {
    productSection.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

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

function pickCategory(type) {
  filters.value.categoryType = type
  page.value = 0
  load()
  setTimeout(scrollToProducts, 50)
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

// 依導覽 query 定位：?new=1 → 捲到頂端（精選/Hero）；?go=products → 捲到商品區
watch(
  () => route.query,
  (q) => {
    if (q.new === '1') {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } else if (q.go === 'products' && productSection.value) {
      productSection.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  },
  { immediate: true },
)

onMounted(load)
</script>

<template>
  <div class="store-home">
    <!-- Hero：NEW ARRIVALS -->
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">NEW ARRIVALS</p>
        <h1 class="hero-title">城市日常<br />風格提案</h1>
        <p class="hero-sub">以低彩度質感布料與簡約剪裁，建構你的每一天。</p>
        <button class="btn-shop" @click="scrollToProducts">
          SHOP NOW
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 12h16M13 5l7 7-7 7" />
          </svg>
        </button>
      </div>
      <div class="hero-fig zoom-img">
        <img :src="heroImg" alt="Coode LAB 新品形象" />
      </div>
    </section>

    <!-- 服務特色 -->
    <section class="features">
      <div class="feature-item">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M3 5h13l3 4v9H3z" />
        </svg>
        <div><strong>滿額免運</strong><span>消費滿 NT$2,000 享免運</span></div>
      </div>
      <div class="feature-item">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4 4h4l2 6-2.5 1.5a11 11 0 0 0 5 5L14 14l6 2v4a2 2 0 0 1-2 2A16 16 0 0 1 2 6a2 2 0 0 1 2-2z" />
        </svg>
        <div><strong>七天鑑賞</strong><span>退換貨流程簡單</span></div>
      </div>
      <div class="feature-item">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 21s-7-4.5-9.5-9A5.5 5.5 0 0 1 12 6a5.5 5.5 0 0 1 9.5 6c-2.5 4.5-9.5 9-9.5 9z" />
          <circle cx="12" cy="10" r="1.6" />
        </svg>
        <div><strong>會員穿搭</strong><span>專屬造型搭配建議</span></div>
      </div>
      <div class="feature-item">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="4" y="10" width="16" height="11" rx="2" />
          <path d="M8 10V7a4 4 0 0 1 8 0v3" />
        </svg>
        <div><strong>安心結帳</strong><span>支援多種付款方式</span></div>
      </div>
    </section>

    <!-- 分類圖塊 -->
    <section class="cat-section container-wide">
      <div class="section-head">
        <p class="eyebrow">SHOP BY CATEGORY</p>
        <h2>選購分類</h2>
      </div>
      <div class="cat-grid">
        <button
          v-for="c in categories"
          :key="c.type"
          class="cat-tile zoom-img"
          @click="pickCategory(c.type)"
        >
          <img :src="c.img" :alt="c.label" loading="lazy" />
          <span class="cat-label">{{ c.label }}</span>
        </button>
      </div>
    </section>

    <!-- 商品列表 -->
    <section ref="productSection" class="product-section container-wide">
      <div class="section-head">
        <p class="eyebrow">THE COLLECTION</p>
        <h2>{{ filters.categoryType ? categoryLabel(filters.categoryType) : '全部商品' }}</h2>
      </div>

      <!-- 篩選列 -->
      <div class="filter-panel">
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
          <option v-for="c in categories" :key="c.type" :value="c.type">{{ c.label }}</option>
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
        <button class="btn btn-primary" @click="applySearch">搜尋</button>
      </div>

      <div v-if="error" class="alert alert-error">載入失敗：{{ error }}</div>
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

      <AppPagination
        v-if="products.length > 0"
        :page="page"
        :total-pages="totalPages"
        @change="changePage"
      />
    </section>
  </div>
</template>

<style scoped>
.container-wide {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ── Hero ── */
.hero {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  background: #f6f3ee;
  min-height: 480px;
}
.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 64px 48px 64px max(24px, calc((100vw - 1240px) / 2 + 24px));
}
.eyebrow {
  font-size: 12px;
  letter-spacing: 0.32em;
  color: var(--accent);
  margin-bottom: 16px;
  font-weight: 700;
}
.hero-title {
  font-size: clamp(36px, 5vw, 60px);
  font-weight: 800;
  letter-spacing: 0.04em;
  line-height: 1.15;
  color: var(--ink);
}
.hero-sub {
  margin-top: 18px;
  color: #6f6b65;
  font-size: 15px;
  max-width: 420px;
}
.btn-shop {
  margin-top: 32px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  align-self: flex-start;
  background: var(--ink);
  color: #fff;
  border: none;
  padding: 14px 34px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.24em;
  transition: background 0.2s ease;
}
.btn-shop:hover {
  background: #2c2c2c;
  color: #fff;
}
.hero-fig {
  position: relative;
  min-height: 100%;
}
.hero-fig img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ── 服務特色 ── */
.features {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-bottom: 1px solid var(--line);
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 26px 24px;
  border-right: 1px solid var(--line);
}
.feature-item:last-child {
  border-right: none;
}
.feature-item svg {
  color: var(--ink);
  flex-shrink: 0;
}
.feature-item strong {
  display: block;
  font-size: 14px;
  color: var(--ink);
  letter-spacing: 0.04em;
}
.feature-item span {
  font-size: 12px;
  color: var(--muted);
}

/* ── Section 標題 ── */
.section-head {
  text-align: center;
  padding: 64px 0 36px;
}
.section-head .eyebrow {
  margin-bottom: 8px;
}
.section-head h2 {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: var(--ink);
}

/* ── 分類圖塊 ── */
.cat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
.cat-tile {
  position: relative;
  aspect-ratio: 4 / 5;
  border: none;
  padding: 0;
  background: #f2efea;
  border-radius: 2px;
  overflow: hidden;
  cursor: pointer;
}
.cat-tile img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cat-label {
  position: absolute;
  left: 14px;
  bottom: 14px;
  background: #fff;
  color: var(--ink);
  font-size: 12px;
  letter-spacing: 0.18em;
  padding: 7px 14px;
  border-radius: 2px;
  transition: all 0.2s ease;
}
.cat-tile:hover .cat-label {
  background: var(--ink);
  color: #fff;
}

/* ── 商品區 ── */
.product-section {
  padding-bottom: 80px;
}
.filter-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fbfaf8;
}
.filter-panel input,
.filter-panel select {
  padding: 9px 12px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
  color: var(--ink);
  font-size: 13px;
}
.filter-panel input:focus,
.filter-panel select:focus {
  outline: none;
  border-color: var(--ink);
}
.filter-panel .btn {
  margin-left: auto;
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
.price-input {
  width: 108px;
}
.product-grid {
  row-gap: 32px;
  column-gap: 20px;
}

/* ── RWD ── */
@media (max-width: 1000px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .hero-copy {
    padding: 56px 24px 48px;
  }
  .features {
    grid-template-columns: repeat(2, 1fr);
  }
  .feature-item:nth-child(2) {
    border-right: none;
  }
  .feature-item:nth-child(n + 3) {
    border-top: 1px solid var(--line);
  }
  .cat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 640px) {
  .container-wide {
    padding: 0 16px;
  }
  .features {
    grid-template-columns: 1fr 1fr;
  }
  .feature-item {
    padding: 20px 16px;
    gap: 10px;
  }
  .cat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .product-grid {
    gap: 20px 12px;
  }
  .filter-panel .btn {
    margin-left: 0;
    width: 100%;
  }
}
</style>