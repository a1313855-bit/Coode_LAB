// ============================================================
// 統一 API 呼叫模組
//
// 透過 Vite dev proxy 將請求轉發到 Spring Boot (localhost:8080)。
// 所有方法都回傳 data（若 Response 是空 body 則回傳 null）。
// ============================================================

async function request(url, options = {}, base = '') {
  const res = await fetch(base + url, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  if (!res.ok) {
    // 404 / 400 等錯誤，拋出包含狀態的錯誤
    let message = `HTTP ${res.status}`
    try {
      const body = await res.json()
      if (body && (body.message || body.error)) message = body.message || body.error
    } catch (e) {
      /* ignore */
    }
    const err = new Error(message)
    err.status = res.status
    throw err
  }

  if (res.status === 204) return null
  const text = await res.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch (e) {
    // 2xx 但回傳內容無法解析成 JSON 時，回傳 null 避免前端直接報 JSON 語法錯誤
    return null
  }
}

function buildQuery(params) {
  const sp = new URLSearchParams()
  if (!params) return ''
  for (const [k, v] of Object.entries(params)) {
    if (v === null || v === undefined || v === '') continue
    sp.append(k, v)
  }
  const qs = sp.toString()
  return qs ? `?${qs}` : ''
}

const get = (url, params, base) => request(url + buildQuery(params), { method: 'GET' }, base)
const post = (url, body, base) => request(url, { method: 'POST', body: JSON.stringify(body) }, base)
const put = (url, body, base) => request(url, { method: 'PUT', body: JSON.stringify(body) }, base)
const patch = (url, body, base) => request(url, { method: 'PATCH', body: JSON.stringify(body) }, base)
const del = (url, base) => request(url, { method: 'DELETE' }, base)

// ============================================================
// 會員 (User) — 商城端 / 管理員端
// ============================================================
export const userApi = {
  login: (body) => post('/api/users/login', body),
  register: (body) => post('/api/users', body),
  checkEmail: (email) => get('/api/users/check-email', { email }),
  findByEmail: (email) => get('/api/users/email', { email }),
  findById: (userId) => get(`/api/users/${userId}`),
  findAllPaged: (page) => get('/api/users/page', { page }),
  findByStatus: (status) => get('/api/users/status', { status }),
  search: (keyword) => get('/api/users/search', { keyword }),
  update: (userId, body) => patch(`/api/users/${userId}`, body),
  toggleStatus: (userId) => patch(`/api/users/${userId}/deactivate`),
  changePassword: (userId, body) => patch(`/api/users/${userId}/password`, body),
}

// ============================================================
// 購物車 (Cart)
// ============================================================
export const cartApi = {
  findByCartId: (cartId) => get(`/api/carts/${cartId}`),
  findByUserId: (userId) => get(`/api/carts/user/${userId}`),
  totalQuantity: (cartId) => get(`/api/carts/${cartId}/total-quantity`),
  updateTotalQuantity: (cartId) => patch(`/api/carts/${cartId}/total-quantity`),
}

// ============================================================
// 購物車商品 (CartItem)
// ============================================================
export const cartItemApi = {
  list: (cartId, page) => get('/api/cart-items/cart/' + cartId, { page }),
  findOne: (cartId, productId) => get(`/api/cart-items/cart/${cartId}/product/${productId}`),
  count: (cartId) => get(`/api/cart-items/cart/${cartId}/count`),
  search: (cartId, keyword, page) => get(`/api/cart-items/cart/${cartId}/search`, { keyword, page }),
  add: (body) => post('/api/cart-items', body),
  update: (cartItemId, body) => patch(`/api/cart-items/${cartItemId}`, body),
  remove: (cartId, productId) => del(`/api/cart-items/cart/${cartId}/product/${productId}`),
}

// ============================================================
// 商品 (Product)
// ============================================================
export const productApi = {
  all: (page) => get('/coode_lab/products', { page }),
  available: (page) => get('/coode_lab/products/available', { page }),
  byId: (productId) => get(`/coode_lab/products/${productId}`),
  byVendor: (vendorId, page) => get(`/coode_lab/products/vendor/${vendorId}`, { page }),
  lowStock: (vendorId, page) => get(`/coode_lab/products/vendor/${vendorId}/low-stock`, { page }),
  filter: (p) => get('/coode_lab/products/filter', p),
  adminFilter: (p) => get('/coode_lab/products/admin/filter', p),
  vendorFilter: (p) => get('/coode_lab/products/vendor/filter', p),
  create: (vendorId, body) => post(`/coode_lab/products/vendor/${vendorId}`, body),
  update: (vendorId, productId, body) =>
    put(`/coode_lab/products/vendor/${vendorId}/${productId}`, body),
  updateStock: (vendorId, productId, body) =>
    patch(`/coode_lab/products/vendor/${vendorId}/${productId}/stock`, body),
  activate: (vendorId, productId) =>
    patch(`/coode_lab/products/vendor/${vendorId}/${productId}/activate`),
  deactivate: (vendorId, productId) =>
    patch(`/coode_lab/products/vendor/${vendorId}/${productId}/deactivate`),
  batchActivate: (vendorId, productIds) =>
    patch(`/coode_lab/products/vendor/${vendorId}/batch-activate`, { productIds }),
  batchDeactivate: (vendorId, productIds) =>
    patch(`/coode_lab/products/vendor/${vendorId}/batch-deactivate`, { productIds }),
}

// ============================================================
// 廠商 (Vendor)
// ============================================================
export const vendorApi = {
  all: (page) => get('/coode_lab/vendors', { page }),
  byId: (vendorId) => get(`/coode_lab/vendors/${vendorId}`),
  filter: (page, keyword, status) =>
    get('/coode_lab/vendors/filter', { page, keyword, status }),
  login: (body) => post('/coode_lab/vendors/login', body),
  create: (body) => post('/coode_lab/vendors', body),
  update: (vendorId, body) => put(`/coode_lab/vendors/${vendorId}`, body),
  activate: (vendorId, body) => put(`/coode_lab/vendors/${vendorId}/activate`, body),
  suspend: (vendorId) => put(`/coode_lab/vendors/${vendorId}/suspend`),
  reactivate: (vendorId) => put(`/coode_lab/vendors/${vendorId}/reactivate`),
  renewContract: (vendorId, body) =>
    put(`/coode_lab/vendors/${vendorId}/renew-contract`, body),
}

// ============================================================
// 管理員 (Admin)
// ============================================================
export const adminApi = {
  login: (body) => post('/api/admins/login', body),
  create: (body) => post('/api/admins', body),
  byId: (adminId) => get(`/api/admins/${adminId}`),
  all: (page) => get('/api/admins', { page }),
  update: (adminId, body) => put(`/api/admins/${adminId}`, body),
  changePassword: (adminId, body) => patch(`/api/admins/${adminId}/password`, body),
  remove: (adminId) => del(`/api/admins/${adminId}`),
}

// ============================================================
// 訂單 (Order)
// ============================================================
export const orderApi = {
  create: (body) => post('/orders', body),
  byUser: (userId, page) => get('/orders/user', { userId, page }),
  byId: (id) => get(`/orders/${id}`),
  all: (page) => get('/orders/all', { page }),
  updateRecipient: (id, body) => put(`/orders/${id}/recipient`, body),
}

// ============================================================
// 訂單明細 (OrderItem)
// ============================================================
export const orderItemApi = {
  create: (orderId, body) => post(`/orders/${orderId}/items`, body),
  byOrder: (orderId, page) => get(`/orders/${orderId}/items`, { page }),
  updateStatus: (orderItemId, body) => put(`/orders/${orderItemId}/status`, body),
  byVendor: (vendorId, page, status) =>
    get(`/orders/${vendorId}/items/vendor/${vendorId}`, { page, status }),
}

// ============================================================
// 穿搭 (Outfit) / 穿搭商品 (OutfitItem)
// ============================================================
export const outfitApi = {
  create: (body) => post('/api/outfits', body),
  byId: (outfitId) => get(`/api/outfits/${outfitId}`),
  byUser: (userId, page) => get(`/api/outfits/user/${userId}`, { page }),
  updateName: (outfitId, body) => patch(`/api/outfits/${outfitId}`, body),
  remove: (outfitId) => del(`/api/outfits/${outfitId}`),
}

export const outfitItemApi = {
  items: (outfitId, page) => get(`/api/outfit-items/outfits/${outfitId}`, { page }),
  add: (outfitId, productId) =>
    post(`/api/outfit-items/outfits/${outfitId}/products/${productId}`),
  addSlot: (outfitId, body) => post(`/api/outfit-items/outfits/${outfitId}`, body),
  replace: (outfitId, productId) =>
    put(`/api/outfit-items/outfits/${outfitId}/products/${productId}`),
  remove: (outfitItemId) => del(`/api/outfit-items/${outfitItemId}`),
  clear: (outfitId) => del(`/api/outfit-items/outfits/${outfitId}`),
}

// ============================================================
// 退換貨 (ReturnRequest) / 退換貨明細 (ReturnItem)
// ============================================================
export const returnRequestApi = {
  create: (userId, orderId, body) =>
    post(`/return-requests/user/${userId}/order/${orderId}`, body),
  byId: (id) => get(`/return-requests/${id}`),
  all: (page) => get('/return-requests/all', { page }),
  byUser: (userId, page) => get(`/return-requests/user/${userId}`, { page }),
  byVendor: (vendorId, page) => get(`/return-requests/vendor/${vendorId}`, { page }),
  updateStatus: (id, body) => put(`/return-requests/${id}/status`, body),
}

// ReturnItem 綁在 /return-requests/{returnRequestId}/items
// updateQuantity / updateStatus 的 @PathVariable 需要 returnRequestId，
// 但 Service 內部只使用 returnItemId，因此 path 中 id 可填佔位值（實際由 service 邏輯使用 body 內欄位）。
export const returnItemApi = {
  add: (returnRequestId, body) =>
    post(`/return-requests/${returnRequestId}/items`, body),
  byReturnRequest: (returnRequestId) =>
    get(`/return-requests/${returnRequestId}/items`),
  updateQuantity: (returnItemId, body) =>
    put(`/return-requests/-/items/${returnItemId}/quantity`, body),
  updateStatus: (returnItemId, body) =>
    put(`/return-requests/-/items/${returnItemId}/status`, body),
}

// ============================================================
// 報表 (Report) — 廠商儀表板
// ============================================================
export const reportApi = {
  vendorDashboard: (vendorId, query) =>
    get(`/reports/vendor/${vendorId}/dashboard`, query),
}

export default { userApi, cartApi, cartItemApi, productApi, vendorApi, adminApi, orderApi, orderItemApi, outfitApi, outfitItemApi, returnRequestApi, returnItemApi, reportApi }
