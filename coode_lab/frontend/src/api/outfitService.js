// ============================================================
// 試衣間資料層
//
// 所有 Product / Outfit / OutfitItem 的 API 呼叫都集中在這裡，
// 頁面與元件一律透過這層取得資料，
// 之後要替換成其他 Spring Boot API 時只改這裡。
// ============================================================
import { productApi, outfitApi, outfitItemApi } from './index'

// 穿搭位置（左右欄、Canvas 共用同一份順序）
export const SLOTS = ['TOP', 'OUTER', 'BOTTOM', 'SHOES', 'ACCESSORY']

// 位置中文名稱
export const SLOT_LABELS = {
  TOP: '上衣',
  OUTER: '外套',
  BOTTOM: '下身',
  SHOES: '鞋子',
  ACCESSORY: '配件',
}

// 商品分類 → 穿搭位置
export const CATEGORY_TO_SLOT = {
  TOP: 'TOP',
  OUTER: 'OUTER',
  OUTERWEAR: 'OUTER',
  BOTTOM: 'BOTTOM',
  SHOES: 'SHOES',
  ACCESSORY: 'ACCESSORY',
}

// 依商品分類取得穿搭位置；不支援時回傳 null
export function categoryToSlot(categoryType) {
  const c = String(categoryType || '').toUpperCase()
  return CATEGORY_TO_SLOT[c] || null
}

// 全新的空穿搭（五個固定 Slot）
export function emptyLook() {
  return { TOP: null, OUTER: null, BOTTOM: null, SHOES: null, ACCESSORY: null }
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
    // 相容舊資料：UPPER_BODY 視為 TOP
    const slot = it.slotType === 'UPPER_BODY' ? 'TOP' : it.slotType
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
    if (product) look[slot] = product
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
    await outfitItemApi.addSlot(outfitId, {
      productId: product.productId,
      slotType: slot,
    })
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