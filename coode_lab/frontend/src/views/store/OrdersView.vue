<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, orderItemApi, returnRequestApi } from '../../api'
import { formatMoney, formatDate } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'
import { currentUserId } from '../../composables/auth'

const userId = currentUserId()
const orders = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

// 展開查看明細
const expandedId = ref(null)
const orderItems = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await orderApi.byUser(userId, page.value)
    orders.value = res.content || []
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

async function toggleItems(orderId) {
  if (expandedId.value === orderId) {
    expandedId.value = null
    return
  }
  expandedId.value = orderId
  try {
    const res = await orderItemApi.byOrder(orderId, 0)
    orderItems.value = res.content || []
  } catch (e) {
    orderItems.value = []
  }
}

// ============================================================
// 退換貨申請（從訂單明細發起）
// ============================================================
const returnModal = ref(null) // { orderId, itemId, productName, available }
const rForm = ref({ requestType: 'RETURN', requestQuantity: 1, picture: '' })
const submitting = ref(false)
const returnError = ref('')

const toast = ref('')
let toastTimer = null

function openReturn(orderId, it) {
  returnModal.value = {
    orderId,
    itemId: it.orderItemId,
    productName: it.product.name,
    available: it.productQuantity,
  }
  rForm.value = { requestType: 'RETURN', requestQuantity: 1, picture: '' }
  returnError.value = ''
}

function closeReturn() {
  returnModal.value = null
}

function clampQuantity() {
  if (!returnModal.value) return
  const available = returnModal.value.available
  if (!rForm.value.requestQuantity || rForm.value.requestQuantity < 1) rForm.value.requestQuantity = 1
  if (rForm.value.requestQuantity > available) rForm.value.requestQuantity = available
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showToast('請上傳圖片檔案')
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    const img = new Image()
    img.onload = () => {
      const maxSide = 400
      let { width, height } = img
      if (width > maxSide || height > maxSide) {
        const scale = maxSide / Math.max(width, height)
        width = Math.round(width * scale)
        height = Math.round(height * scale)
      }
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      canvas.getContext('2d').drawImage(img, 0, 0, width, height)
      rForm.value.picture = canvas.toDataURL('image/jpeg', 0.6)
    }
    img.src = reader.result
  }
  reader.readAsDataURL(file)
}

async function submitReturn() {
  if (!returnModal.value) return
  if (!rForm.value.requestQuantity || rForm.value.requestQuantity < 1 || rForm.value.requestQuantity > returnModal.value.available) {
    returnError.value = `數量必須介於 1 ~ ${returnModal.value.available} 件`
    return
  }
  submitting.value = true
  returnError.value = ''
  try {
    await returnRequestApi.create(userId, returnModal.value.orderId, {
      requestType: rForm.value.requestType,
      orderItemId: returnModal.value.itemId,
      requestQuantity: rForm.value.requestQuantity,
      picture: rForm.value.picture || '',
    })
    const orderId = returnModal.value.orderId
    closeReturn()
    showToast('退換貨申請已送出，廠商將進行審核')
    if (expandedId.value === orderId) {
      const res = await orderItemApi.byOrder(orderId, 0)
      orderItems.value = res.content || []
    }
  } catch (e) {
    returnError.value = '送出失敗：' + e.message
  } finally {
    submitting.value = false
  }
}

function showToast(text) {
  toast.value = text
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = ''
  }, 2600)
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header">
      <div>
        <h1>我的訂單</h1>
        <p>查看訂單與明細</p>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="orders.length === 0" class="empty">暫無訂單</div>

    <div v-else class="order-list">
      <div v-for="o in orders" :key="o.orderId" class="card order">
        <div class="order-head" @click="toggleItems(o.orderId)">
          <div>
            <b>訂單 #{{ o.orderId }}</b>
            <div class="muted small">建立於 {{ formatDate(o.createdAt) }}</div>
          </div>
          <div class="text-right">
            <div class="sum">{{ formatMoney(o.sumTotal) }}</div>
            <div class="muted small">{{ o.user.name }} · {{ o.recipientName }}</div>
          </div>
        </div>
        <div class="muted small recipient">
          收件：{{ o.recipientName }} {{ o.recipientPhone }} · {{ o.recipientAddress }}
        </div>
        <div v-if="expandedId === o.orderId" class="items">
          <div v-if="orderItems.length === 0" class="muted">無明細資料</div>
          <div v-for="it in orderItems" :key="it.orderItemId" class="item">
            <span>
              {{ it.product.name }}
              <span class="muted small">x{{ it.productQuantity }}</span>
            </span>
            <span>{{ formatMoney(it.priceTotal) }}</span>
            <button class="btn btn-sm" @click="openReturn(o.orderId, it)">
              退換貨
            </button>
          </div>
        </div>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 退換貨申請單 -->
    <div v-if="returnModal" class="modal-mask">
      <div class="modal">
        <h3>申請退換貨</h3>
        <div class="muted small mb">
          訂單 #{{ returnModal.orderId }} · {{ returnModal.productName }}（購買 x{{ returnModal.available }}，最多可申請 {{ returnModal.available }} 件）
        </div>

        <div class="form-field">
          <label>類型</label>
          <select v-model="rForm.requestType">
            <option value="RETURN">退貨</option>
            <option value="EXCHANGE">換貨</option>
          </select>
        </div>

        <div class="form-field">
          <label>數量（1 ~ {{ returnModal.available }} 件，不可大於訂單明細數量）</label>
          <input
            v-model.number="rForm.requestQuantity"
            type="number"
            min="1"
            :max="returnModal.available"
            @input="clampQuantity"
          />
        </div>

        <div class="form-field">
          <label>上傳照片（選填）</label>
          <input type="file" accept="image/*" @change="onFileChange" />
          <img
            v-if="rForm.picture"
            :src="rForm.picture"
            class="upload-preview"
            alt="退貨照片"
          />
        </div>

        <div v-if="returnError" class="alert alert-error">{{ returnError }}</div>

        <div class="flex">
          <button class="btn btn-primary" :disabled="submitting" @click="submitReturn">送出</button>
          <button class="btn" @click="closeReturn">取消</button>
        </div>
      </div>
    </div>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>

<style scoped>
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}
.small {
  font-size: 12px;
}
.sum {
  font-weight: 700;
  color: var(--c-danger);
}
.recipient {
  margin-top: 8px;
}
.items {
  margin-top: 12px;
  border-top: 1px solid var(--c-border);
  padding-top: 10px;
}
.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  font-size: 14px;
  border-bottom: 1px dashed var(--c-border);
}
.item > span:first-child {
  flex: 1;
}
.mb {
  margin-bottom: 14px;
}
.upload-preview {
  margin-top: 10px;
  display: block;
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--c-border);
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
  max-width: 90vw;
}
.modal h3 {
  margin-bottom: 8px;
}
.toast {
  position: fixed;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: #fff;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 200;
}
</style>