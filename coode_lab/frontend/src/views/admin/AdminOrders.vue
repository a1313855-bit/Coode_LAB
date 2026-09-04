<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, orderItemApi } from '../../api'
import { formatMoney, formatDate } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'

const orders = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')
const keyword = ref('')

const expandedId = ref(null)
const orderItems = ref([])

const showEditOrder = ref(false)
const editOrder = ref(null)
const orderForm = ref({ recipientName: '', recipientPhone: '', recipientAddress: '' })

const showEditItem = ref(false)
const editItem = ref(null)
const itemForm = ref({ productQuantity: 1, status: '' })

const pendingStatusChange = ref(null)

const statusOptions = ['PENDING', 'PROCESSING', 'SHIPPED', 'RECEIVED', 'CANCELLED']
const statusLabelMap = {
  PENDING: '待處理',
  PROCESSING: '處理中',
  SHIPPED: '已出貨',
  RECEIVED: '已完成',
  CANCELLED: '已取消',
}

function applySearch() {
  page.value = 0
  load()
}
function clearKeyword() {
  keyword.value = ''
  page.value = 0
  load()
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await orderApi.all(page.value, keyword.value)
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

async function toggleItems(order) {
  if (expandedId.value === order.orderId) {
    expandedId.value = null
    orderItems.value = []
    return
  }
  expandedId.value = order.orderId
  try {
    const res = await orderItemApi.byOrder(order.orderId, 0)
    orderItems.value = res.content || []
  } catch (e) {
    orderItems.value = []
  }
}

function openEditOrder(o) {
  editOrder.value = o
  orderForm.value = {
    recipientName: o.recipientName,
    recipientPhone: o.recipientPhone,
    recipientAddress: o.recipientAddress,
  }
  showEditOrder.value = true
}

async function saveEditOrder() {
  error.value = ''
  try {
    await orderApi.updateRecipient(editOrder.value.orderId, orderForm.value)
    showEditOrder.value = false
    await load()
    await reloadItems()
  } catch (e) {
    error.value = e.message
  }
}

function openEditItem(it) {
  editItem.value = it
  itemForm.value = {
    productQuantity: it.productQuantity,
    status: it.status,
  }
  showEditItem.value = true
}

async function saveEditItem() {
  error.value = ''
  try {
    await orderItemApi.update(expandedId.value, editItem.value.orderItemId, {
      productQuantity: Number(itemForm.value.productQuantity),
      status: itemForm.value.status,
    })
    showEditItem.value = false
    await reloadItems()
  } catch (e) {
    error.value = e.message
  }
}

async function reloadItems() {
  if (expandedId.value != null) {
    try {
      const res = await orderItemApi.byOrder(expandedId.value, 0)
      orderItems.value = res.content || []
    } catch (e) {
      orderItems.value = []
    }
  }
}

function requestStatusChange(it) {
  pendingStatusChange.value = it
}

function cancelStatusChange() {
  pendingStatusChange.value = null
}

async function confirmStatusChange() {
  const it = pendingStatusChange.value
  pendingStatusChange.value = null
  error.value = ''
  try {
    await orderItemApi.adminStatus(it.orderItemId, { status: it._newStatus })
    await load()
    await reloadItems()
  } catch (e) {
    error.value = e.message
  }
}

function itemName(it) {
  return (it.variant && it.variant.product && it.variant.product.name) || '-'
}

function itemSpec(it) {
  if (!it.variant) return ''
  return `（${it.variant.color} / ${it.variant.size}）`
}

onMounted(load)
</script>

<template>
  <div class="vendor-report">
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">ADMIN</div>
          <h1 class="vr-title">訂單管理</h1>
          <p class="vr-subtitle">查看與管理全站訂單</p>
        </div>
      </div>
    </div>

    <div class="vr-filter-bar">
      <div class="vr-search-wrap">
        <input v-model="keyword" placeholder="搜尋訂單ID / 會員名稱 / 電子郵件" @keyup.enter="applySearch" />
        <button v-if="keyword" type="button" class="vr-clear-keyword" aria-label="清空搜尋文字" @click="clearKeyword">×</button>
      </div>
      <button class="vr-btn vr-btn-primary" @click="applySearch">搜尋</button>
    </div>

    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>
    <div v-else-if="orders.length === 0" class="vr-empty">暫無訂單</div>
    <div v-else class="vr-card">
      <table class="vr-table">
        <thead>
          <tr>
            <th>訂單編號</th>
            <th>會員名稱</th>
            <th>收件人（姓名）</th>
            <th>收件人（地址）</th>
            <th>收件人（電話）</th>
            <th>金額</th>
            <th>建立時間</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <template v-for="o in orders" :key="o.orderId">
            <tr @click="toggleItems(o)" class="order-row">
              <td>#{{ o.orderId }}</td>
              <td>{{ o.user.name }}</td>
              <td>{{ o.recipientName }}</td>
              <td>{{ o.recipientAddress }}</td>
              <td>{{ o.recipientPhone }}</td>
              <td>{{ formatMoney(o.sumTotal) }}</td>
              <td>{{ formatDate(o.createdAt) }}</td>
              <td>
                <div style="display: flex; gap: 6px; flex-wrap: wrap">
                  <button class="vr-btn vr-btn-sm vr-btn-outline" @click.stop="openEditOrder(o)">編輯訂單資訊</button>
                  <button class="vr-btn vr-btn-sm vr-btn-outline" @click.stop="toggleItems(o)">明細</button>
                </div>
              </td>
            </tr>
            <tr v-if="expandedId === o.orderId">
              <td colspan="8">
                <div v-if="orderItems.length === 0" class="muted">無明細</div>
                <div v-for="it in orderItems" :key="it.orderItemId" class="item">
                  <span>{{ itemName(it) }}</span>
                  <span class="muted small" v-if="it.variant">{{ itemSpec(it) }}</span>
                  <span>× {{ it.productQuantity }}</span>
                  <span>— {{ formatMoney(it.priceTotal) }}</span>
                  <span class="muted small">狀態：{{ statusLabelMap[it.status] || it.status }}</span>
                  <select v-model="it._newStatus" class="status-select">
                    <option v-for="s in statusOptions" :key="s" :value="s">{{ statusLabelMap[s] }}</option>
                  </select>
                  <button class="vr-btn vr-btn-sm vr-btn-outline" @click="requestStatusChange(it)">修改狀態</button>
                  <button class="vr-btn vr-btn-sm vr-btn-outline" @click="openEditItem(it)">編輯明細</button>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <div v-if="showEditOrder" class="vr-modal-mask">
      <div class="vr-modal">
        <h3>編輯訂單 #{{ editOrder.orderId }}</h3>
        <div class="vr-form-field"><label>收件人姓名</label><input v-model="orderForm.recipientName" /></div>
        <div class="vr-form-field"><label>收件人電話</label><input v-model="orderForm.recipientPhone" /></div>
        <div class="vr-form-field"><label>收件人地址</label><input v-model="orderForm.recipientAddress" /></div>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-outline" @click="showEditOrder = false">取消</button>
          <button class="vr-btn vr-btn-primary" @click="saveEditOrder">儲存</button>
        </div>
      </div>
    </div>

    <div v-if="showEditItem" class="vr-modal-mask">
      <div class="vr-modal">
        <h3>編輯明細 #{{ editItem.orderItemId }}</h3>
        <div class="vr-form-field"><label>商品</label><input :value="itemName(editItem)" disabled /></div>
        <div class="vr-form-field"><label>單價</label><input :value="formatMoney(editItem.price)" disabled /></div>
        <div class="vr-form-field"><label>數量</label><input v-model.number="itemForm.productQuantity" type="number" min="1" /></div>
        <div class="vr-form-field"><label>狀態</label>
          <select v-model="itemForm.status">
            <option value="PENDING">待處理</option>
            <option value="PROCESSING">處理中</option>
            <option value="SHIPPED">已出貨</option>
            <option value="ARRIVED">已到貨</option>
            <option value="RECEIVED">已收貨</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </div>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-outline" @click="showEditItem = false">取消</button>
          <button class="vr-btn vr-btn-primary" @click="saveEditItem">儲存</button>
        </div>
      </div>
    </div>

    <div v-if="pendingStatusChange" class="vr-modal-mask">
      <div class="vr-modal">
        <h3>確認修改狀態</h3>
        <p>確定要將「{{ itemName(pendingStatusChange) }}」的狀態改為「{{ statusLabelMap[pendingStatusChange._newStatus] }}」嗎？</p>
        <div class="vr-modal-actions">
          <button class="vr-btn vr-btn-outline" @click="cancelStatusChange">取消</button>
          <button class="vr-btn vr-btn-primary" @click="confirmStatusChange">確認</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.muted {
  color: var(--vr-mut);
}
.small {
  font-size: 12px;
}
.order-row {
  cursor: pointer;
}
.item {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
}
.status-select {
  padding: 6px 8px;
  border: 1px solid var(--vr-line);
  border-radius: 8px;
  font-size: 14px;
}
</style>
