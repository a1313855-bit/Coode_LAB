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
  <div class="admin-content">
    <div class="page-header">
      <p class="subtitle">列出所有低庫存（庫存 ≤ 5）與缺貨（庫存 = 0）的商品規格，可直接補貨。</p>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="saved" class="alert alert-success">{{ saved }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else-if="lowRows.length === 0" class="empty">目前沒有低庫存或缺貨的規格</div>
    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
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
              <td><img class="thumb" :src="productImageUrl(r.image)" alt="" /></td>
              <td>{{ r.color }}</td>
              <td>{{ r.size }}</td>
              <td :class="{ 'low-cell': r.stock <= 5 }">{{ r.stock }}</td>
              <td>
                <span :class="['badge', r.stock === 0 ? 'badge-danger' : 'badge-warning']">
                  {{ r.stock === 0 ? '缺貨' : '低庫存' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-primary" @click="openReplenish(r)">補貨</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 補貨 modal -->
    <div v-if="replenishTarget" class="modal-mask">
      <div class="modal replenish-modal">
        <h3>補貨</h3>
        <div class="rm-row"><span class="muted">商品</span><b>{{ replenishTarget.productName }}</b></div>
        <div class="rm-row"><span class="muted">規格</span><b>{{ replenishTarget.color }} / {{ replenishTarget.size }}</b></div>
        <div class="rm-row"><span class="muted">目前庫存</span><b :class="{ 'low-cell': replenishTarget.stock <= 5 }">{{ replenishTarget.stock }}</b></div>
        <div class="form-field">
          <label>本次補貨數量</label>
          <input v-model.number="replenishQty" type="number" min="1" />
        </div>
        <div class="rm-row calc">
          <span class="muted">補貨後庫存</span>
          <b>{{ replenishTarget.stock + (Number(replenishQty) || 0) }}</b>
        </div>
        <div class="flex">
          <button class="btn btn-primary" @click="confirmReplenish">確認補貨</button>
          <button class="btn" @click="replenishTarget = null">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 14px;
}
.subtitle {
  color: var(--c-text-light);
  font-size: 14px;
}
.thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--c-border);
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
  width: 420px;
  max-width: 92vw;
}
.modal h3 {
  margin-bottom: 16px;
}
.rm-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--c-border);
  font-size: 14px;
}
.rm-row.calc {
  margin: 6px 0 14px;
  border-bottom: none;
  font-size: 16px;
  font-weight: 700;
}
.muted {
  color: var(--c-text-light);
}
.form-field {
  margin: 12px 0;
}
.form-field label {
  font-size: 13px;
  color: var(--c-text-light);
  display: block;
  margin-bottom: 6px;
}
.form-field input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  font-size: 14px;
}
.flex {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}
</style>
