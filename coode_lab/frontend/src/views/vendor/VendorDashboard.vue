<script setup>
import { ref, onMounted } from 'vue'
import { reportApi } from '../../api'
import { formatMoney, formatDate, statusLabel } from '../../utils/format'

const vendorId = ref(1)
const data = ref(null)
const loading = ref(false)
const error = ref('')
const period = ref('MONTH')

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await reportApi.vendorDashboard(vendorId.value, { period: period.value })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function growthText(g) {
  if (g === null || g === undefined) return '-'
  const v = Number(g)
  const sign = v >= 0 ? '+' : ''
  return `${sign}${(v * 100).toFixed(1)}%`
}

onMounted(load)
</script>

<template>
  <div class="admin-content">
    <div class="page-header flex-between">
      <div>
        <h1>銷售報表</h1>
        <p>廠商銷售數據總覽</p>
      </div>
      <div class="flex">
        <input v-model.number="vendorId" type="number" placeholder="廠商 ID" class="uid" />
        <select v-model="period" class="period">
          <option value="DAY">日</option>
          <option value="WEEK">週</option>
          <option value="MONTH">月</option>
          <option value="QUARTER">季</option>
          <option value="YEAR">年</option>
        </select>
        <button class="btn btn-primary" @click="load">查詢</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <div v-else-if="data">
      <!-- KPI -->
      <div class="grid-4 kpis">
        <div class="card kpi">
          <div class="kpi-label">營業額</div>
          <div class="kpi-value">{{ formatMoney(data.revenue.value) }}</div>
          <div class="kpi-growth">成長 {{ growthText(data.revenue.growthRate) }}</div>
        </div>
        <div class="card kpi">
          <div class="kpi-label">訂單數</div>
          <div class="kpi-value">{{ data.orderCount.value ?? 0 }}</div>
          <div class="kpi-growth">成長 {{ growthText(data.orderCount.growthRate) }}</div>
        </div>
        <div class="card kpi">
          <div class="kpi-label">銷售件數</div>
          <div class="kpi-value">{{ data.unitsSold.value ?? 0 }}</div>
          <div class="kpi-growth">成長 {{ growthText(data.unitsSold.growthRate) }}</div>
        </div>
        <div class="card kpi">
          <div class="kpi-label">退貨量</div>
          <div class="kpi-value">{{ data.returnQuantity.value ?? 0 }}</div>
          <div class="kpi-growth">成長 {{ growthText(data.returnQuantity.growthRate) }}</div>
        </div>
      </div>

      <div class="grid-3 charts">
        <!-- 銷售趨勢 -->
        <div class="card">
          <h3>銷售趨勢</h3>
          <div v-if="!data.salesTrend || data.salesTrend.length === 0" class="muted small">無資料</div>
          <div v-else class="trend">
            <div v-for="(d, i) in data.salesTrend" :key="i" class="trend-row">
              <span class="muted">{{ d.date }}</span>
              <span>{{ formatMoney(d.amount) }}</span>
            </div>
          </div>
        </div>

        <!-- 訂單狀態 -->
        <div class="card">
          <h3>訂單狀態</h3>
          <div v-if="!data.salesStatus || data.salesStatus.length === 0" class="muted small">無資料</div>
          <div v-else>
            <div v-for="(s, i) in data.salesStatus" :key="i" class="status-row">
              <span>{{ statusLabel(s.status) }}</span>
              <b>{{ s.count }}</b>
            </div>
          </div>
        </div>

        <!-- 退貨狀況 -->
        <div class="card">
          <h3>退貨狀況</h3>
          <template v-if="data.returnSummary">
            <div class="status-row"><span>申請總數</span><b>{{ data.returnSummary.appliedCount }}</b></div>
            <div class="status-row"><span>待審核</span><b>{{ data.returnSummary.pendingReview }}</b></div>
            <div class="status-row"><span>已核准</span><b>{{ data.returnSummary.approved }}</b></div>
            <div class="status-row"><span>已拒絕</span><b>{{ data.returnSummary.rejected }}</b></div>
          </template>
        </div>
      </div>

      <div class="grid-2 ranks">
        <!-- 熱銷排行 -->
        <div class="card">
          <h3>熱銷商品（依數量）</h3>
          <div v-if="!data.topProductsByQuantity || data.topProductsByQuantity.length === 0" class="muted small">無資料</div>
          <table v-else class="data-table">
            <thead><tr><th>商品</th><th>數量</th><th>金額</th></tr></thead>
            <tbody>
              <tr v-for="p in data.topProductsByQuantity" :key="p.productId">
                <td>{{ p.name }}</td>
                <td>{{ p.quantity }}</td>
                <td>{{ formatMoney(p.amount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="card">
          <h3>熱銷商品（依金額）</h3>
          <div v-if="!data.topProductsByAmount || data.topProductsByAmount.length === 0" class="muted small">無資料</div>
          <table v-else class="data-table">
            <thead><tr><th>商品</th><th>金額</th><th>數量</th></tr></thead>
            <tbody>
              <tr v-for="p in data.topProductsByAmount" :key="p.productId">
                <td>{{ p.name }}</td>
                <td>{{ formatMoney(p.amount) }}</td>
                <td>{{ p.quantity }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.uid {
  width: 70px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.period {
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.kpi-label {
  font-size: 13px;
  color: var(--c-text-light);
}
.kpi-value {
  font-size: 24px;
  font-weight: 800;
  margin: 4px 0;
}
.kpi-growth {
  font-size: 12px;
  color: var(--c-success);
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}
h3 {
  margin-bottom: 12px;
}
.trend-row,
.status-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-top: 1px solid var(--c-border);
  font-size: 14px;
}
.small {
  font-size: 12px;
}
</style>
