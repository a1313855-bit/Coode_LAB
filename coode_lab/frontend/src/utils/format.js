// ============================================================
// 共用格式化 / 顯示輔助
// ============================================================

export function formatMoney(value) {
  if (value === null || value === undefined) return '-'
  return 'NT$ ' + Number(value).toLocaleString('zh-TW', { minimumFractionDigits: 0 })
}

export function formatDate(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

export function statusLabel(status) {
  const map = {
    ACTIVE: '啟用中',
    ACTIVATED: '已啟用',
    ENABLED: '已啟用',
    SUSPENDED: '已停權',
    DEACTIVATED: '已停用',
    DISABLED: '已停用',
    INACTIVE: '未啟用',
    DRAFT: '草稿',
    PENDING: '待處理',
    PENDING_REVIEW: '待審核',
    REVIEWED: '已審核',
    APPROVED: '已核准',
    REJECTED: '已拒絕',
    PROCESSING: '處理中',
    RECEIVED: '已收貨',
    SHIPPED: '已出貨',
    ARRIVED: '已到貨',
    PROCESSED: '已處理',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    RETURN: '退貨',
    EXCHANGE: '換貨',
    LOW_STOCK: '庫存不足',
    MALE: '男',
    FEMALE: '女',
    KIDS: '童裝',
    MEN: '男裝',
    WOMEN: '女裝',
  }
  if (status == null) return '-'
  return map[String(status).toUpperCase()] || String(status)
}

export function statusBadgeClass(status) {
  const map = {
    ACTIVE: 'badge-success',
    ACTIVATED: 'badge-success',
    ENABLED: 'badge-success',
    SUSPENDED: 'badge-danger',
    DEACTIVATED: 'badge-danger',
    DISABLED: 'badge-danger',
    INACTIVE: 'badge-muted',
    DRAFT: 'badge-muted',
    PENDING: 'badge-warning',
    PENDING_REVIEW: 'badge-warning',
    REVIEWED: 'badge-info',
    APPROVED: 'badge-success',
    REJECTED: 'badge-danger',
    COMPLETED: 'badge-success',
    PROCESSING: 'badge-info',
    RECEIVED: 'badge-success',
    SHIPPED: 'badge-info',
    ARRIVED: 'badge-info',
    PROCESSED: 'badge-info',
    RETURN: 'badge-warning',
    EXCHANGE: 'badge-info',
    MALE: 'badge-info',
    FEMALE: 'badge-warning',
    KIDS: 'badge-warning',
    LOW_STOCK: 'badge-danger',
  }
  if (status == null) return 'badge-muted'
  return map[String(status).toUpperCase()] || 'badge-info'
}

export function categoryLabel(type) {
  const map = {
    TOP: '上衣',
    OUTER: '外套',
    OUTERWEAR: '外套',
    BOTTOM: '褲子',
    DRESS: '洋裝',
    HEADWEAR: '帽子/頭飾',
    HAT: '帽子/頭飾',
  }
  return map[String(type || '').toUpperCase()] || type || '-'
}

export function slotLabel(slot) {
  const map = {
    HEADWEAR: '帽子/頭飾',
    UPPER_BODY: '上半身',
    BOTTOM: '下半身',
    FULL_BODY: '洋裝',
  }
  return map[String(slot || '').toUpperCase()] || slot || '-'
}

// 顯示商品圖片的輔助：無真實圖片時用底圖，避免壞圖
export function productImage(product, fallbackText) {
  if (product && product.imagesJpg) {
    return product.imagesJpg
  }
  return null
}

// 商品圖 URL 解析：
// - data URI / http(s) / 絕對路徑 → 直接顯示（後端提供的真實圖片）
// - 其他（如檔名 "xxx.jpg"）→ 回傳 null，介面改用本地 placeholder
export function productImageUrl(product) {
  if (!product || !product.imagesJpg) return null
  const src = String(product.imagesJpg).trim()
  if (!src) return null
  if (/^(data:|https?:|\/)/i.test(src)) return src
  return null
}
