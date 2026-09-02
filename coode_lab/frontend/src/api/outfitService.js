// ============================================================
// 試衣間資料層
//
// 所有 Product / Outfit / OutfitItem 的 API 呼叫都集中在這裡，
// 頁面與元件一律透過這層取得資料，
// 之後要替換成其他 Spring Boot API 時只改這裡。
// ============================================================
import { productApi, outfitApi, outfitItemApi } from './index'

// 穿搭位置（左右欄、Canvas 共用同一份順序）
export const SLOTS = ['HEADWEAR', 'UPPER_BODY', 'BOTTOM', 'FULL_BODY']

// 位置中文名稱
export const SLOT_LABELS = {
  HEADWEAR: '帽子/頭飾',
  UPPER_BODY: '上衣/外套',
  BOTTOM: '下身',
  FULL_BODY: '洋裝',
}

// 商品分類 → 穿搭位置
// UPPER_BODY 共用：TOP / OUTER / OUTERWEAR
// BOTTOM 共用：BOTTOM / PANTS / SKIRT
// FULL_BODY：DRESS
// HEADWEAR 共用：HEADWEAR / HAT
// ACCESSORY / SHOES 不支援試衣間
export const CATEGORY_TO_SLOT = {
  TOP: 'UPPER_BODY',
  OUTER: 'UPPER_BODY',
  OUTERWEAR: 'UPPER_BODY',
  BOTTOM: 'BOTTOM',
  PANTS: 'BOTTOM',
  SKIRT: 'BOTTOM',
  DRESS: 'FULL_BODY',
  HEADWEAR: 'HEADWEAR',
  HAT: 'HEADWEAR',
}

// 依商品分類取得穿搭位置；不支援時回傳 null
export function categoryToSlot(categoryType) {
  const c = String(categoryType || '').toUpperCase()
  return CATEGORY_TO_SLOT[c] || null
}

// 全新的空穿搭（三個固定 Slot）
export function emptyLook() {
  return { HEADWEAR: null, UPPER_BODY: null, BOTTOM: null, FULL_BODY: null }
}

// 計算一套穿搭的總價（只算有商品的 Slot）
export function lookTotal(look) {
  if (!look) return 0
  return Object.values(look).reduce(
    (sum, product) => sum + Number(product && product.price ? product.price : 0),
    0,
  )
}

// ============================================================
// 商品
// ============================================================

// 抓取目前所有「可販售（已上架 + 廠商啟用 + 合約有效）」的商品，
// 後端每頁固定 10 筆，因此自動往後翻頁抓完。
export async function fetchAllProducts() {
  const all = []
  let page = 0
  for (;;) {
    const res = await productApi.filter({ page })
    const content = res.content || []
    all.push(...content)
    const totalPages = res.totalPages || 1
    if (content.length === 0 || page >= totalPages - 1) break
    page += 1
  }
  return all
}

// 抓單一商品詳細資料
export function fetchProductById(productId) {
  return productApi.byId(productId)
}

// ============================================================
// 規格（顏色 × 尺寸）輔助
// ============================================================

// 商品的預設規格：第一個「可販售」的規格；沒有則第一個
export function defaultVariant(product) {
  if (!product || !Array.isArray(product.variants) || product.variants.length === 0) return null
  return product.variants.find((v) => v.status === 'ACTIVE') || product.variants[0]
}

// 目前選定的規格（會試穿時選擇的顏色/尺寸）
export function chosenVariantOf(product) {
  if (!product) return null
  return product.chosenVariant || defaultVariant(product)
}

// 商品的所有顏色（依規格去重）
export function variantColors(product) {
  if (!product || !Array.isArray(product.variants)) return []
  const seen = new Set()
  const list = []
  for (const v of product.variants) {
    if (!seen.has(v.color)) {
      seen.add(v.color)
      list.push(v.color)
    }
  }
  return list
}

// 指定顏色下的所有尺寸規格
export function variantsByColor(product, color) {
  if (!product || !Array.isArray(product.variants)) return []
  return product.variants.filter((v) => v.color === color)
}

// ============================================================
// 穿搭 (Outfit) / 穿搭商品 (OutfitItem)
// ============================================================

// 抓取使用者的所有穿搭（含 items），自動翻頁抓完
export async function fetchSavedOutfits(userId) {
  const all = []
  let page = 0
  for (;;) {
    const res = await outfitApi.byUser(userId, page)
    const content = res.content || []
    all.push(...content)
    const totalPages = res.totalPages || 1
    if (content.length === 0 || page >= totalPages - 1) break
    page += 1
  }
  return all
}

// 載入一套已儲存的穿搭，回傳 { outfitId, name, items, look }
// look 的每個 Slot 都塞入完整的 Product 資料，方便直接顯示。
export async function loadOutfit(outfitId) {
  const outfit = await outfitApi.byId(outfitId)
  const look = emptyLook()

  const itemBySlot = {}
  const ids = []
  for (const it of outfit.items || []) {
    const slot = it.slotType
    // 只回填目前的 4 個合法 Slot；SHOES / ACCESSORY 等舊資料直接忽略
    if (slot in look) {
      itemBySlot[slot] = it
      ids.push(it.productId)
    }
  }

  const detailById = {}
  await Promise.all(
    ids.map(async (id) => {
      try {
        detailById[id] = await productApi.byId(id)
      } catch (e) {
        detailById[id] = null
      }
    }),
  )

  for (const [slot, item] of Object.entries(itemBySlot)) {
    const product = detailById[item.productId]
    if (product) {
      // 記錄該筆穿搭原本指定的規格（顏色），供試穿圖還原
      const target = (product.variants || []).find((v) => v.variantId === item.variantId)
      if (target) product.chosenVariant = target
      look[slot] = product
    }
  }

  return {
    outfitId: outfit.outfitId,
    name: outfit.name,
    items: outfit.items || [],
    look,
  }
}

// 儲存目前穿搭：
// - editingOutfitId 有值 → 更新該套（重新命名 + 清空商品 + 重新加入）
// - 否則 → 建立新的 Outfit 再逐一加入 OutfitItem
// 沒有商品的 Slot 不會送出 API。
export async function saveLook(userId, name, look, editingOutfitId) {
  let outfitId = editingOutfitId

  if (outfitId) {
    await outfitApi.updateName(outfitId, { name })
    await outfitItemApi.clear(outfitId)
  } else {
    const created = await outfitApi.create({ userId, name })
    outfitId = created.outfitId
  }

  for (const slot of SLOTS) {
    const product = look[slot]
    if (!product) continue
    const variant = chosenVariantOf(product)
    if (!variant) continue
    // 由後端根據 Product.categoryType 決定 slot（避免前端自由指定 slotType 造成 405 / 不一致）
    await outfitItemApi.add(outfitId, product.productId, variant.variantId)
  }

  return outfitId
}

// 重新命名一套穿搭
export function renameOutfit(outfitId, name) {
  return outfitApi.updateName(outfitId, { name })
}

// 刪除一套穿搭
export function deleteOutfit(outfitId) {
  return outfitApi.remove(outfitId)
}