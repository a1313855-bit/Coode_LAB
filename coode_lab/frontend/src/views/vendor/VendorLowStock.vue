<script setup>
import { ref, computed, onMounted } from 'vue'
import { productApi } from '../../api'
import { currentVendorId } from '../../composables/auth'
import { formatMoney, productImageUrl } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const vendorId = currentVendorId()

const products = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')
const saved = ref('')

// 低庫存規格：庫存 ≤ 5（含缺貨 0）
const lowRows = computed(() => {
  const rows = []
  for (const p of products.value) {
    for (const v of p.variants || []) {
      const stock = Number(v.stock || 0)
      if (stock <= 5) {
        rows.push({
          productId: p.productId,
          productName: p.name,
          image: p.imagesJpg,
          variantId: v.variantId,
          color: v.color,
          size: v.size,
          stock,
        })
      }
    }
  }
  return rows
})

const replenishTarget = ref(null)
const replenishQty = ref(10)

function openReplenish(row) {
  replenishTarget.value = row
  replenishQty.value = 10
}

async function confirmReplenish() {
  const qty = Number(replenishQty.value)
  if (!Number.isInteger(qty) || qty < 1) {
    error.value = '補貨數量必須為大於 0 的整數'
    return
  }
  error.value = ''
  saved.value = ''
  try {
    await productApi.replenishVariant(vendorId, replenishTarget.value.variantId, { quantity: qty })
    saved.value = `${replenishTarget.value.productName}（${replenishTarget.value.color} / ${replenishTarget.value.size}）補貨成功`
    replenishTarget.value = null
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await productApi.lowStock(vendorId, page.value)
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

onMounted(load)
</script>

<template>
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">VENDOR</div>
          <h1 class="vr-title">低庫存管理</h1>
          <p class="vr-subtitle">低庫存（庫存 ≤ 5）；缺貨（庫存 = 0）</p>
        </div>
      </div>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="saved" class="vr-alert-success">{{ saved }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>

    <div v-else-if="lowRows.length === 0" class="vr-empty">目前沒有低庫存或缺貨的規格</div>
    <div v-else class="vr-card">
      <table class="vr-table">
        <thead>
          <tr>
            <th>商品</th>
            <th>圖片</th>
            <th>顏色</th>
            <th>尺寸</th>
            <th>庫存</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(r, i) in lowRows" :key="i">
            <td>{{ r.productName }}</td>
            <td><img class="vr-thumb" :src="productImageUrl(r.image)" alt="" /></td>
            <td>{{ r.color }}</td>
            <td>{{ r.size }}</td>
            <td :class="{ 'low-cell': r.stock <= 5 }">{{ r.stock }}</td>
            <td>
              <span v-if="r.stock === 0" class="vr-badge vr-badge-danger">缺貨</span>
              <span v-else class="vr-badge vr-badge-warning">低庫存</span>
            </td>
            <td>
              <button class="vr-btn vr-btn-sm vr-btn-primary" @click="openReplenish(r)">補貨</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 補貨 modal -->
    <div v-if="replenishTarget" class="vr-modal-mask">
      <div class="vr-modal replenish-modal">
        <h3>補貨</h3>
        <div class="rm-row"><span style="color: var(--vr-mut)">商品</span><b>{{ replenishTarget.productName }}</b></div>
        <div class="rm-row"><span style="color: var(--vr-mut)">規格</span><b>{{ replenishTarget.color }} / {{ replenishTarget.size }}</b></div>
        <div class="rm-row"><span style="color: var(--vr-mut)">目前庫存</span><b :class="{ 'low-cell': replenishTarget.stock <= 5 }">{{ replenishTarget.stock }}</b></div>
        <div class="vr-form-field">
          <label>本次補貨數量</label>
          <input v-model.number="replenishQty" type="number" min="1" />
        </div>
        <div class="rm-row calc">
          <span style="color: var(--vr-mut)">補貨後庫存</span>
          <b>{{ replenishTarget.stock + (Number(replenishQty) || 0) }}</b>
        </div>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-primary" @click="confirmReplenish">確認補貨</button>
          <button class="vr-btn vr-btn-outline" @click="replenishTarget = null">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.low-cell {
  color: var(--vr-down);
  font-weight: 700;
}
.replenish-modal {
  width: 420px;
  max-width: 92vw;
}
.rm-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--vr-line);
  font-size: 14px;
}
.rm-row.calc {
  margin: 6px 0 14px;
  border-bottom: none;
  font-size: 16px;
  font-weight: 700;
}
.vr-badge-warning {
  background: #fef3c7;
  color: #92400e;
}
</style>
