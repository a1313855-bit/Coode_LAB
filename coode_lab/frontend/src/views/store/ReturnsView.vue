<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, returnRequestApi, returnItemApi } from '../../api'
import { formatDate, statusBadgeClass, statusLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'
import { currentUserId } from '../../composables/auth'

const userId = ref(currentUserId())
const returns = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

// 建立退貨
const showCreate = ref(false)
const myOrders = ref([])
const form = ref({
  orderId: '',
  requestType: 'RETURN',
  returnRequestQuantity: 1,
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await returnRequestApi.byUser(userId.value, page.value)
    returns.value = res.content || []
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

async function openCreate() {
  showCreate.value = true
  error.value = ''
  try {
    const res = await orderApi.byUser(userId.value, 0)
    myOrders.value = res.content || []
  } catch (e) {
    myOrders.value = []
  }
}

async function submit() {
  error.value = ''
  try {
    await returnRequestApi.create(
      userId.value,
      form.value.orderId,
      {
        requestType: form.value.requestType,
        returnRequestQuantity: Number(form.value.returnRequestQuantity),
      }
    )
    showCreate.value = false
    page.value = 0
    await load()
  } catch (e) {
    error.value = '建立失敗：' + e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header flex-between">
      <div>
        <h1>退換貨</h1>
        <p>查看與申請退換貨</p>
      </div>
      <div class="flex">
        <input v-model.number="userId" type="number" placeholder="會員 ID" class="uid" />
        <button class="btn btn-primary" @click="load">查詢</button>
        <button class="btn btn-success" @click="openCreate">+ 新增申請</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="returns.length === 0" class="empty">暫無退換貨申請</div>

    <div v-else class="card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>申請編號</th>
              <th>訂單</th>
              <th>類型</th>
              <th>數量</th>
              <th>狀態</th>
              <th>建立時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in returns" :key="r.returnRequestsId">
              <td>#{{ r.returnRequestsId }}</td>
              <td>#{{ r.order.orderId }}</td>
              <td>{{ statusLabel(r.requestType) }}</td>
              <td>{{ r.returnRequestQuantity }}</td>
              <td><span :class="['badge', statusBadgeClass(r.status)]">{{ statusLabel(r.status) }}</span></td>
              <td>{{ formatDate(r.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 建立申請 -->
    <div v-if="showCreate" class="modal-mask">
      <div class="modal">
        <h3>新增退換貨申請</h3>
        <div class="form-field">
          <label>選擇訂單</label>
          <select v-model="form.orderId">
            <option v-for="o in myOrders" :key="o.orderId" :value="o.orderId">
              訂單 #{{ o.orderId }}（{{ o.recipientName }}）
            </option>
          </select>
        </div>
        <div class="form-field">
          <label>類型</label>
          <select v-model="form.requestType">
            <option value="RETURN">退貨</option>
            <option value="EXCHANGE">換貨</option>
          </select>
        </div>
        <div class="form-field">
          <label>數量</label>
          <input v-model.number="form.returnRequestQuantity" type="number" min="1" />
        </div>
        <div class="flex">
          <button class="btn btn-primary" @click="submit">送出</button>
          <button class="btn" @click="showCreate = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.uid {
  width: 90px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
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
  margin-bottom: 16px;
}
</style>
