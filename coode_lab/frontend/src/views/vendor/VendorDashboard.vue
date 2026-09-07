<script setup>
import { ref, computed, onMounted } from 'vue'
import { reportApi } from '../../api'
import { formatMoney } from '../../utils/format'
import { currentVendorId } from '../../composables/auth'

// ─────────────────────────────────────────────
// 資料
// ─────────────────────────────────────────────
const vendorId = ref(currentVendorId())
const data = ref(null)
const loading = ref(false)
const error = ref('')
const period = ref('MONTH')

const PERIODS = [
  { value: 'DAY', label: '今日' },
  { value: 'WEEK', label: '本週' },
  { value: 'MONTH', label: '本月' },
  { value: 'YEAR', label: '本年' },
]

// Vendor 群組狀態 → 中文（不能用 statusLabel，那是 OrderItem raw 用）
const STATUS_GROUPS = {
  NOT_SHIPPED: '未出貨',
  IN_TRANSIT: '運送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

// ─────────────────────────────────────────────
// 載入
// ─────────────────────────────────────────────
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

function selectPeriod(p) {
  period.value = p
  load()
}

onMounted(load)

// ─────────────────────────────────────────────
// helpers
// ─────────────────────────────────────────────
function periodLabel(v) {
  const found = PERIODS.find((x) => x.value === v)
  return found ? found.label : ''
}

// growthRate 已是百分比（12.5 = 12.5%），直接顯示，不做 *100
function growthText(g) {
  if (g === null || g === undefined) return '-'
  const v = Number(g)
  if (Number.isNaN(v)) return '-'
  const sign = v > 0 ? '+' : ''
  return `${sign}${v.toFixed(1)}%`
}

function groupLabel(status) {
  return STATUS_GROUPS[status] || status || '其他'
}

const averageOrder = computed(() => {
  if (!data.value) return 0
  const rev = Number(data.value.revenue?.value || 0)
  const count = Number(data.value.orderCount?.value || 0)
  if (count <= 0) return 0
  return rev / count
})

const statusTotal = computed(() => {
  if (!data.value || !data.value.salesStatus) return 0
  return data.value.salesStatus.reduce((s, x) => s + Number(x.count || 0), 0)
})

// ─────────────────────────────────────────────
// SVG 銷售趨勢線圖
// ─────────────────────────────────────────────
const LINE_W = 560
const LINE_H = 240
const LINE_PAD = 36
const LINE_PAD_BOTTOM = 52

// 依目前 period 回傳圖表用的 { values:[], labels:[] }
// DAY → 24 小時、WEEK → 7 天、MONTH → 30 天、YEAR → 12 月
const chartData = computed(() => {
  if (!data.value) return { values: [], labels: [] }

  // DAY：使用逐小時資料（24 點）
  if (period.value === 'DAY') {
    const hourly = data.value.hourlyTrend
    if (!hourly || hourly.length === 0) return { values: [], labels: [] }
    return {
      values: hourly.map((d) => Number(d.amount) || 0),
      labels: hourly.map((d, i) => `${i}:00`),
    }
  }

  if (!data.value.salesTrend) return { values: [], labels: [] }

  if (period.value === 'YEAR') {
    const monthly = new Array(12).fill(0)
    for (const d of data.value.salesTrend) {
      const dt = new Date(d.date)
      const m = dt.getMonth()
      monthly[m] += Number(d.amount) || 0
    }
    return {
      values: monthly,
      labels: monthly.map((_, i) => `${i + 1}月`),
    }
  }

  // WEEK / MONTH：直接用 daily trend
  return {
    values: data.value.salesTrend.map((d) => Number(d.amount) || 0),
    labels: data.value.salesTrend.map((d) => {
      const dt = new Date(d.date)
      return `${dt.getMonth() + 1}/${dt.getDate()}`
    }),
  }
})

const trendPath = computed(() => {
  const { values, labels } = chartData.value
  if (values.length === 0) return { line: '', area: '', points: [], labels: [] }
  const max = Math.max(...values.map((v) => v || 0), 1)
  const n = values.length
  const usableW = LINE_W - LINE_PAD * 2
  const usableH = LINE_H - LINE_PAD - LINE_PAD_BOTTOM
  const coords = values.map((v, i) => {
    const x = LINE_PAD + (n === 1 ? usableW / 2 : (i * usableW) / (n - 1))
    const y = LINE_H - LINE_PAD_BOTTOM - (v / max) * usableH
    return [x, y]
  })
  const d = coords.map((c, i) => `${i === 0 ? 'M' : 'L'}${c[0].toFixed(1)},${c[1].toFixed(1)}`).join(' ')
  const area = `${d} L${coords[coords.length - 1][0].toFixed(1)},${LINE_H - LINE_PAD_BOTTOM} L${coords[0][0].toFixed(1)},${LINE_H - LINE_PAD_BOTTOM} Z`
  return { line: d, area, points: coords, labels }
})

// ─────────────────────────────────────────────
// SVG 訂單狀態圓餅圖
// ─────────────────────────────────────────────
const DONUT_R = 52
const DONUT_W = 140
const donutSegments = computed(() => {
  if (!data.value || !data.value.salesStatus) return []
  // 圖例：always 顯示四種狀態
  return data.value.salesStatus.map((s) => {
    const count = Number(s.count || 0)
    const frac = statusTotal.value > 0 ? count / statusTotal.value : 0
    return {
      ...s,
      label: groupLabel(s.status),
      count,
      color: donutColor(s.status),
      frac,
    }
  })
})

// SVG 弧段：只畫 count>0 的；frac=1.0 時拆兩段半圓（SVG A 指令起終點相同畫不出弧）
const donutArcs = computed(() => {
  let angle = -Math.PI / 2
  const result = []
  for (const s of donutSegments.value.filter((s) => s.count > 0)) {
    const start = angle
    const sweep = s.frac * Math.PI * 2
    const cx = DONUT_W / 2
    const cy = DONUT_W / 2
    if (s.frac >= 1) {
      // 整個圓：拆成兩段半圓
      const mid1 = start + Math.PI
      const x1 = cx + DONUT_R * Math.cos(start)
      const y1 = cy + DONUT_R * Math.sin(start)
      const xm = cx + DONUT_R * Math.cos(mid1)
      const ym = cy + DONUT_R * Math.sin(mid1)
      result.push({ ...s, path: `M${cx},${cy} L${x1.toFixed(1)},${y1.toFixed(1)} A${DONUT_R},${DONUT_R} 0 0 1 ${xm.toFixed(1)},${ym.toFixed(1)} Z` })
      result.push({ ...s, path: `M${cx},${cy} L${xm.toFixed(1)},${ym.toFixed(1)} A${DONUT_R},${DONUT_R} 0 0 1 ${x1.toFixed(1)},${y1.toFixed(1)} Z` })
    } else {
      const end = start + sweep
      const large = sweep > Math.PI ? 1 : 0
      const x1 = cx + DONUT_R * Math.cos(start)
      const y1 = cy + DONUT_R * Math.sin(start)
      const x2 = cx + DONUT_R * Math.cos(end)
      const y2 = cy + DONUT_R * Math.sin(end)
      result.push({ ...s, path: `M${cx},${cy} L${x1.toFixed(1)},${y1.toFixed(1)} A${DONUT_R},${DONUT_R} 0 ${large} 1 ${x2.toFixed(1)},${y2.toFixed(1)} Z` })
      angle = end
    }
    if (s.frac >= 1) angle = start + Math.PI * 2
  }
  return result
})

function donutColor(status) {
  const map = {
    NOT_SHIPPED: '#c9986b',
    IN_TRANSIT: '#8a6b52',
    COMPLETED: '#6b4f3a',
    CANCELLED: '#c9b7a5',
  }
  return map[status] || '#b0835a'
}

// X 軸標籤太密時跳過顯示（WEEK/每年顯示全部，MONTH 每隔幾天顯示一次）
function showEvery(i, total) {
  if (total <= 8) return true
  const step = Math.ceil(total / 8)
  return i % step === 0 || i === total - 1
}

// ─────────────────────────────────────────────
// CSV 匯出（UTF-8 BOM）
// ─────────────────────────────────────────────
function exportCsv() {
  if (!data.value) return
  const rows = []
  rows.push(['銷售報表', '', ''])
  rows.push(['廠商ID', vendorId.value, ''])
  rows.push(['期間', periodLabel(period.value), ''])
  rows.push([])
  rows.push(['指標', '數值', '成長率'])
  rows.push(['營業額', data.value.revenue?.value ?? '', growthText(data.value.revenue?.growthRate)])
  rows.push(['訂單數', data.value.orderCount?.value ?? 0, growthText(data.value.orderCount?.growthRate)])
  rows.push(['銷售件數', data.value.unitsSold?.value ?? 0, growthText(data.value.unitsSold?.growthRate)])
  rows.push(['退貨量', data.value.returnQuantity?.value ?? 0, growthText(data.value.returnQuantity?.growthRate)])
  rows.push([])
  rows.push(['每日銷售', '日期', '金額'])
  if (data.value.salesTrend) {
    for (const t of data.value.salesTrend) rows.push(['', t.date, t.amount])
  }
  rows.push([])
  rows.push(['訂單狀態', '數量', ''])
  if (data.value.salesStatus) {
    for (const s of data.value.salesStatus) rows.push(['', groupLabel(s.status), s.count])
  }
  rows.push([])
  rows.push(['熱銷商品（依金額）', '數量', '金額'])
  if (data.value.topProductsByAmount) {
    for (const p of data.value.topProductsByAmount) rows.push(['', p.name, `${p.quantity}`, p.amount])
  }
  const csv = rows
    .map((r) => r.map((c) => `"${String(c ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\r\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `sales-report-vendor${vendorId.value}-${period.value}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="vendor-report">
    <!-- ── 頁首 Banner ── -->
    <div class="vr-banner">
      <div class="vr-banner-inner">
        <div>
          <div class="vr-eyebrow">FASHION · SALES</div>
          <h1 class="vr-title">銷售總覽</h1>
          <p class="vr-subtitle">廠商銷售數據儀表板 · {{ periodLabel(period) }}</p>
        </div>
        <div class="vr-banner-controls">
          <div class="vr-period-group">
            <button
              v-for="p in PERIODS"
              :key="p.value"
              class="vr-period-btn"
              :class="{ active: period === p.value }"
              @click="selectPeriod(p.value)"
            >
              {{ p.label }}
            </button>
          </div>
          <div class="vr-actions">
            <button class="vr-btn vr-btn-outline" @click="exportCsv">匯出 CSV</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── 錯誤 / 載入 ── -->
    <div v-if="error" class="vr-alert">{{ error }}</div>
    <div v-if="loading" class="vr-empty">載入中...</div>

    <template v-else-if="data">
      <!-- ── KPI 卡片 ── -->
      <div class="vr-kpis">
        <div class="vr-card vr-kpi">
          <div class="vr-kpi-label">營業額</div>
          <div class="vr-kpi-value">{{ formatMoney(data.revenue.value) }}</div>
        </div>

        <div class="vr-card vr-kpi">
          <div class="vr-kpi-label">訂單數</div>
          <div class="vr-kpi-value">{{ data.orderCount.value ?? 0 }}</div>
        </div>

        <div class="vr-card vr-kpi">
          <div class="vr-kpi-label">平均客單價</div>
          <div class="vr-kpi-value">{{ formatMoney(averageOrder) }}</div>
        </div>

        <div class="vr-card vr-kpi">
          <div class="vr-kpi-label">銷售件數</div>
          <div class="vr-kpi-value">{{ data.unitsSold.value ?? 0 }}</div>
        </div>

        <div class="vr-card vr-kpi">
          <div class="vr-kpi-label">退貨量</div>
          <div class="vr-kpi-value">{{ data.returnQuantity.value ?? 0 }}</div>
        </div>
      </div>

      <!-- ── 圖表區 ── -->
      <div class="vr-charts">
        <!-- 銷售趨勢 -->
        <div class="vr-card vr-chart-wide">
          <div class="vr-card-head">
            <h3 class="vr-card-title">銷售趨勢</h3>
            <span class="vr-card-tag">{{ periodLabel(period) }}</span>
          </div>
          <svg
            v-if="trendPath.points.length > 0"
            :viewBox="`0 0 ${LINE_W} ${LINE_H}`"
            class="vr-line"
            preserveAspectRatio="xMidYMid meet"
          >
            <defs>
              <linearGradient id="vrGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#b0835a" stop-opacity="0.35" />
                <stop offset="100%" stop-color="#b0835a" stop-opacity="0" />
              </linearGradient>
            </defs>
            <path :d="trendPath.area" fill="url(#vrGrad)" />
            <path :d="trendPath.line" fill="none" stroke="#8a6b52" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
            <circle
              v-for="(c, i) in trendPath.points"
              :key="i"
              :cx="c[0]"
              :cy="c[1]"
              r="3"
              fill="#fff"
              stroke="#8a6b52"
              stroke-width="1.5"
            />
            <!-- X 軸標籤 -->
            <text
              v-for="(label, i) in trendPath.labels"
              :key="'lbl' + i"
              :x="trendPath.points[i][0]"
              :y="LINE_H - LINE_PAD_BOTTOM + 18"
              text-anchor="middle"
              class="vr-xlabel"
              :style="{ display: (showEvery(i, trendPath.labels.length)) ? 'block' : 'none' }"
            >{{ label }}</text>
          </svg>
          <div v-else class="vr-empty small">無資料</div>
        </div>

        <!-- 訂單狀態圓餅 -->
        <div class="vr-card vr-chart-donut">
          <div class="vr-card-head">
            <h3 class="vr-card-title">訂單狀態</h3>
          </div>
          <div class="vr-donut-wrap">
            <svg :viewBox="`0 0 ${DONUT_W} ${DONUT_W}`" class="vr-donut">
              <path
                v-for="(seg, i) in donutArcs"
                :key="i"
                :d="seg.path"
                :fill="seg.color"
              />
            </svg>
            <div class="vr-legend">
              <div v-for="(seg, i) in donutSegments" :key="'l' + i" class="vr-legend-row">
                <span class="vr-dot" :style="{ background: seg.color }"></span>
                <span class="vr-legend-label">{{ seg.label }}</span>
                <span class="vr-legend-value">{{ seg.count }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 排行 + 退貨 ── -->
      <div class="vr-ranks">
        <!-- 營收排行榜（依營收=數量×單價） -->
        <div class="vr-card">
          <div class="vr-card-head">
            <h3 class="vr-card-title">營收排行榜</h3>
            <span class="vr-card-tag">依營收</span>
          </div>
          <div v-if="!data.topProductsByAmount || data.topProductsByAmount.length === 0" class="vr-empty small">
            無資料
          </div>
          <div v-else class="vr-toplist">
            <div v-for="(p, i) in data.topProductsByAmount" :key="p.productId" class="vr-top-row">
              <span class="vr-rank" :class="{ top: i === 0 }">{{ i + 1 }}</span>
              <span class="vr-top-name">{{ p.name }}</span>
              <span class="vr-top-amount">{{ formatMoney(p.amount) }}</span>
              <span class="vr-top-qty">×{{ p.quantity }}</span>
            </div>
          </div>
        </div>

        <!-- 銷量排行榜（依數量） -->
        <div class="vr-card">
          <div class="vr-card-head">
            <h3 class="vr-card-title">銷量排行榜</h3>
            <span class="vr-card-tag">依數量</span>
          </div>
          <div v-if="!data.topProductsByQuantity || data.topProductsByQuantity.length === 0" class="vr-empty small">
            無資料
          </div>
          <div v-else class="vr-toplist">
            <div v-for="(p, i) in data.topProductsByQuantity" :key="p.productId" class="vr-top-row">
              <span class="vr-rank" :class="{ top: i === 0 }">{{ i + 1 }}</span>
              <span class="vr-top-name">{{ p.name }}</span>
              <span class="vr-top-qty">×{{ p.quantity }}</span>
              <span class="vr-top-amount">{{ formatMoney(p.amount) }}</span>
            </div>
          </div>
        </div>

        <!-- 退貨狀況 -->
        <div class="vr-card">
          <div class="vr-card-head">
            <h3 class="vr-card-title">退貨狀況</h3>
            <span class="vr-card-tag">RETURN</span>
          </div>
          <template v-if="data.returnSummary">
            <div class="vr-status-row"><span>申請總數</span><b>{{ data.returnSummary.appliedCount }}</b></div>
            <div class="vr-status-row"><span>待審核</span><b>{{ data.returnSummary.pendingReview }}</b></div>
            <div class="vr-status-row"><span>已核准</span><b>{{ data.returnSummary.approved }}</b></div>
            <div class="vr-status-row"><span>已拒絕</span><b>{{ data.returnSummary.rejected }}</b></div>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════
   Fashion Dashboard —— vendor-report-*
   （scoped，獨立色票，不污染其他頁面）
   ═══════════════════════════════════════ */
.vendor-report {
  --vr-cream: #faf6f0;
  --vr-cream-2: #f3ece1;
  --vr-brown: #6b4f3a;
  --vr-brown-dk: #4d3827;
  --vr-brown-mid: #8a6b52;
  --vr-sand: #c9986b;
  --vr-line: #e6dccd;
  --vr-down: #c0392b;
  --vr-ink: #3d3a36;
  --vr-mut: #94897a;

  background: linear-gradient(180deg, var(--vr-cream) 0%, var(--vr-cream-2) 480px);
  border-radius: 16px;
  padding: 4px;
  color: var(--vr-ink);
  font-family: 'PingFang TC', 'Microsoft JhengHei', system-ui, sans-serif;
}

/* ── Banner ── */
.vr-banner {
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(120deg, #7c5a3f 0%, #b98a5f 55%, #d7b48c 100%);
  padding: 0 28px;
}
.vr-banner-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 132px;
  gap: 20px;
  flex-wrap: wrap;
}
.vr-eyebrow {
  letter-spacing: 2.5px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 700;
}
.vr-title {
  margin: 4px 0 4px;
  font-size: 30px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.5px;
}
.vr-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}
.vr-banner-controls {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}
.vr-period-group {
  display: flex;
  gap: 6px;
  background: rgba(255, 255, 255, 0.14);
  padding: 5px;
  border-radius: 12px;
}
.vr-period-btn {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.85);
  padding: 7px 14px;
  border-radius: 9px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
}
.vr-period-btn.active {
  background: #fff;
  color: var(--vr-brown-dk);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}
.vr-actions {
  display: flex;
  gap: 8px;
}
.vr-btn {
  border: none;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.vr-btn-dark {
  background: #2f2219;
  color: #fff;
}
.vr-btn-dark:hover {
  background: #000;
}
.vr-btn-outline {
  background: rgba(255, 255, 255, 0.92);
  color: var(--vr-brown-dk);
  border: 1px solid #fff;
}
.vr-btn-outline:hover {
  background: #fff;
}

/* ── 卡片 ── */
.vr-card {
  background: #fff;
  border: 1px solid var(--vr-line);
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(90, 70, 50, 0.06);
  padding: 18px;
}
.vr-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.vr-card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--vr-brown-dk);
}
.vr-card-tag {
  font-size: 11px;
  color: var(--vr-brown-mid);
  background: var(--vr-cream-2);
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 700;
}

/* ── KPI ── */
.vr-kpis {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-top: 18px;
}
.vr-kpi-label {
  font-size: 13px;
  color: var(--vr-mut);
  font-weight: 600;
}
.vr-kpi-value {
  font-size: 23px;
  font-weight: 900;
  color: var(--vr-brown-dk);
  margin: 6px 0 8px;
}

/* ── 圖表 ── */
.vr-charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
  margin-top: 14px;
}
.vr-line {
  width: 100%;
  height: 240px;
  display: block;
}
.vr-xlabel {
  font-size: 11px;
  fill: #94897a;
  font-family: 'PingFang TC', 'Microsoft JhengHei', system-ui, sans-serif;
}
.vr-donut-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
}
.vr-donut {
  width: 120px;
  height: 120px;
  flex: 0 0 auto;
}
.vr-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.vr-legend-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.vr-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  flex: 0 0 auto;
}
.vr-legend-label {
  color: var(--vr-ink);
}
.vr-legend-value {
  margin-left: auto;
  font-weight: 800;
  color: var(--vr-brown-dk);
}

/* ── 排行 / 退貨 ── */
.vr-ranks {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 14px;
  margin-top: 14px;
}
.vr-toplist {
  display: flex;
  flex-direction: column;
}
.vr-top-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  border-top: 1px solid var(--vr-line);
  font-size: 13px;
}
.vr-rank {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: var(--vr-cream-2);
  color: var(--vr-brown-mid);
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}
.vr-rank.top {
  background: var(--vr-brown);
  color: #fff;
}
.vr-top-name {
  flex: 1;
  font-weight: 600;
  color: var(--vr-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.vr-top-qty {
  color: var(--vr-mut);
  font-size: 12px;
  flex: 0 0 auto;
}
.vr-top-amount {
  font-weight: 800;
  color: var(--vr-brown-dk);
  flex: 0 0 auto;
}
.vr-status-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-top: 1px solid var(--vr-line);
  font-size: 14px;
  color: var(--vr-ink);
}
.vr-status-row b {
  color: var(--vr-brown-dk);
}

/* ── misc ── */
.vr-alert {
  margin-top: 14px;
  background: #fbecec;
  color: var(--vr-down);
  border: 1px solid #f0c4c4;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
}
.vr-empty {
  padding: 26px 0;
  text-align: center;
  color: var(--vr-mut);
  font-size: 14px;
}
.vr-empty.small {
  padding: 14px 0;
  font-size: 12px;
}

/* ── 響應式 ── */
@media (max-width: 1080px) {
  .vr-kpis {
    grid-template-columns: repeat(2, 1fr);
  }
  .vr-charts,
  .vr-ranks {
    grid-template-columns: 1fr;
  }
}
</style>
