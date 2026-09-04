<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '../../api'
import ProductCard from '../../components/ProductCard.vue'
import AppPagination from '../../components/AppPagination.vue'

const route = useRoute()
const router = useRouter()

const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

// 依 query 決定模式：?mode=new（近1個月新上架）或 ?mode=all（全部商品）
const mode = ref('all')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = { page: page.value }
    if (mode.value === 'new') params.sort = 'newest'
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

watch(
  () => route.query.mode,
  (m) => {
    mode.value = m === 'new' ? 'new' : 'all'
    page.value = 0
    load()
  },
)

onMounted(() => {
  mode.value = route.query.mode === 'new' ? 'new' : 'all'
  load()
})
</script>

<template>
  <div class="container-wide">
    <div class="page-head">
      <p class="eyebrow">{{ mode === 'new' ? 'NEW ARRIVALS' : 'ALL PRODUCTS' }}</p>
      <h1>{{ mode === 'new' ? '新品上架' : '全部商品' }}</h1>
      <p class="sub">{{ mode === 'new' ? '近一個月上架的所有新商品' : '網站上的所有商品' }}</p>
    </div>

    <div v-if="error" class="alert alert-error">載入失敗：{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="products.length === 0" class="empty">沒有商品</div>

    <div v-else class="product-grid grid-5">
      <ProductCard
        v-for="p in products"
        :key="p.productId"
        :product="p"
        @detail="(id) => router.push(`/store/product/${id}`)"
      />
    </div>

    <AppPagination
      v-if="products.length > 0"
      :page="page"
      :total-pages="totalPages"
      @change="changePage"
    />
  </div>
</template>

<style scoped>
.container-wide {
  max-width: 1240px;
  margin: 0 auto;
  padding: 48px 24px 80px;
}
.page-head {
  text-align: center;
  padding-bottom: 36px;
}
.eyebrow {
  font-size: 12px;
  letter-spacing: 0.32em;
  color: var(--accent);
  margin-bottom: 8px;
  font-weight: 700;
}
.page-head h1 {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0.1em;
  color: var(--ink);
}
.sub {
  margin-top: 8px;
  color: var(--muted);
  font-size: 14px;
}
.product-grid {
  row-gap: 32px;
  column-gap: 20px;
}
.grid-5 {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
}
@media (max-width: 1000px) {
  .grid-5 {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 640px) {
  .grid-5 {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>