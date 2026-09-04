<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { productApi } from '../../api'
import ProductCard from '../../components/ProductCard.vue'
import { categoryLabel } from '../../utils/format'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const error = ref('')

// 新品上架（依 productId 倒序，取前 5 項）
const newArrivals = ref([])
// 熱銷商品（依營收，取前 5 項）
const bestSellers = ref([])
// 全部商品（首頁只顯示 5 項）
const allProducts = ref([])
const allProductsTotal = ref(0)

const categories = [
  { type: 'TOP', label: '上衣', img: '/images/Top.png' },
  { type: 'OUTER', label: '外套', img: '/images/coat.jpeg' },
  { type: 'BOTTOM', label: '下著', img: '/images/Bottoms.jpeg' },
  { type: 'DRESS', label: '洋裝', img: '/images/Dress.jpeg' },
  { type: 'HEADWEAR', label: '帽子/頭飾', img: '/images/head.png' },
]

const productSection = ref(null)
const newArrivalsSection = ref(null)

function scrollToProducts() {
  if (productSection.value) {
    productSection.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function pickCategory(type) {
  router.push({ path: '/store/products', query: { categoryType: type } })
}

// 新品上架：依 productId 倒序（較新商品排前面）
async function loadNewArrivals() {
  try {
    const res = await productApi.filter({ page: 0, sort: 'newest' })
    newArrivals.value = (res.content || []).slice(0, 5)
  } catch (e) {
    /* ignore */
  }
}

// 熱銷商品：依營收取前 5 項
async function loadBestSellers() {
  try {
    bestSellers.value = await productApi.topSelling(5)
  } catch (e) {
    /* ignore */
  }
}

// 全部商品：首頁只顯示前 5 項
// 後端 filter 每頁固定 10 筆無法只取 5，所以此處取第 1 頁前 5 筆
async function loadAllProducts() {
  try {
    const res = await productApi.filter({ page: 0 })
    allProducts.value = (res.content || []).slice(0, 5)
    allProductsTotal.value = res.totalElements || 0
  } catch (e) {
    /* ignore */
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([loadNewArrivals(), loadBestSellers(), loadAllProducts()])
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/store/product/${id}`)
}

function goMoreNew() {
  router.push({ path: '/store/products', query: { mode: 'new' } })
}

function goMoreAll() {
  router.push({ path: '/store/products', query: { mode: 'all' } })
}

// 依導覽 query 定位：?new=1 → 捲到新品上架；?go=products → 捲到全部商品區
watch(
  () => route.query,
  (q) => {
    if (q.new === '1') {
      if (newArrivalsSection.value) {
        newArrivalsSection.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
      } else {
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }
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
    <section class="hero" data-nav-section="home">
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
        <img src="/images/Key_Visual.png" alt="Coode LAB 新品形象" />
      </div>
    </section>

    <div v-if="error" class="alert alert-error">載入失敗：{{ error }}</div>

    <!-- 選購分類 -->
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

    <!-- 新品上架 -->
    <section ref="newArrivalsSection" class="section container-wide" data-nav-section="new">
      <div class="section-head row-head">
        <div>
          <p class="eyebrow">NEW ARRIVALS</p>
          <h2>新品上架</h2>
        </div>
        <button class="more-link" @click="goMoreNew">more →</button>
      </div>
      <div v-if="loading && newArrivals.length === 0" class="empty">載入中...</div>
      <div v-else class="grid-5 product-grid">
        <ProductCard
          v-for="p in newArrivals"
          :key="p.productId"
          :product="p"
          @detail="goDetail"
        />
      </div>
    </section>

    <!-- 熱銷商品 -->
    <section class="section container-wide">
      <div class="section-head row-head">
        <div>
          <p class="eyebrow">BEST SELLERS</p>
          <h2>熱銷商品</h2>
        </div>
      </div>
      <div v-if="loading && bestSellers.length === 0" class="empty">載入中...</div>
      <div v-else-if="bestSellers.length === 0" class="empty">暫無熱銷商品</div>
      <div v-else class="grid-5 product-grid">
        <ProductCard
          v-for="p in bestSellers"
          :key="p.productId"
          :product="p"
          @detail="goDetail"
        />
      </div>
    </section>

    <!-- 全部商品 -->
    <section ref="productSection" class="section container-wide" data-nav-section="products">
      <div class="section-head row-head">
        <div>
          <p class="eyebrow">ALL PRODUCTS</p>
          <h2>全部商品</h2>
        </div>
        <button class="more-link" @click="goMoreAll">more →</button>
      </div>
      <div v-if="loading && allProducts.length === 0" class="empty">載入中...</div>
      <div v-else-if="allProducts.length === 0" class="empty">沒有符合條件的商品</div>
      <div v-else class="grid-5 product-grid">
        <ProductCard
          v-for="p in allProducts"
          :key="p.productId"
          :product="p"
          @detail="goDetail"
        />
      </div>
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
  cursor: pointer;
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

/* 左右對齊的標題 + more 按鈕 */
.row-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  text-align: left;
}
.row-head .eyebrow {
  margin-bottom: 8px;
}
.row-head h2 {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: var(--ink);
}
.more-link {
  background: none;
  border: none;
  color: var(--muted);
  font-size: 14px;
  letter-spacing: 0.08em;
  cursor: pointer;
  padding: 0 0 8px;
}
.more-link:hover {
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
.section {
  padding-bottom: 40px;
}
.product-grid {
  row-gap: 32px;
  column-gap: 20px;
}
.grid-5 {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
}

/* ── RWD ── */
@media (max-width: 1000px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .hero-copy {
    padding: 56px 24px 48px;
  }
  .cat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .grid-5 {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 640px) {
  .container-wide {
    padding: 0 16px;
  }
  .cat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .grid-5 {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
