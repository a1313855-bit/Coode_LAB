<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  SLOTS,
  SLOT_LABELS,
  categoryToSlot,
  emptyLook,
  lookTotal,
  fetchAllProducts,
  fetchSavedOutfits,
  loadOutfit as loadOutfitData,
  saveLook,
  renameOutfit,
  deleteOutfit,
  defaultVariant,
  chosenVariantOf,
} from '../../api/outfitService'
import { currentUserId, isLoggedIn } from '../../composables/auth'
import { cartApi, cartItemApi } from '../../api'
import ProductBrowser from '../../components/outfit/ProductBrowser.vue'
import TryOnCanvas from '../../components/outfit/TryOnCanvas.vue'
import SelectedItemsPanel from '../../components/outfit/SelectedItemsPanel.vue'
import SaveOutfitModal from '../../components/outfit/SaveOutfitModal.vue'
import SavedOutfitCarousel from '../../components/outfit/SavedOutfitCarousel.vue'

const router = useRouter()

// TODO: 正式版要從登入會員取得 userId
const userId = ref(currentUserId())

// ── 左側商品
const products = ref([])
const loadingProducts = ref(false)
const activeCategory = ref('ALL')
const sortOption = ref('newest')
const productMap = ref(new Map())

// ── 中間試穿 / 右側已選
const look = reactive(emptyLook())
const selectedProduct = ref(null)
const canvasScale = ref(1)

// ── 下方收藏
const savedOutfits = ref([])
const loadingOutfits = ref(false)
const editingOutfitId = ref(null)
const editingOutfitName = ref('')

// ── 收藏愛心（後端尚未串接，先保留前端狀態）
const favoriteIds = reactive(new Set())

// ── Modal / 提示
const showSaveModal = ref(false)
const toast = ref('')
const errorMsg = ref('')
let toastTimer = null

const browserCol = ref(null)

// ============================================================
// 商品
// ============================================================

async function fetchProducts() {
  loadingProducts.value = true
  errorMsg.value = ''
  try {
    const list = await fetchAllProducts()
    products.value = list
    productMap.value = new Map(list.map((p) => [p.productId, p]))
  } catch (e) {
    errorMsg.value = '商品載入失敗，請稍後再試'
  } finally {
    loadingProducts.value = false
  }
}

// 依目前分類 + 排序顯示的商品
const displayedProducts = computed(() => {
  let list = products.value
  if (activeCategory.value !== 'ALL') {
    list = list.filter((p) => p.categoryType === activeCategory.value)
  }
  if (sortOption.value === 'priceAsc') {
    list = [...list].sort((a, b) => Number(a.price) - Number(b.price))
  } else if (sortOption.value === 'priceDesc') {
    list = [...list].sort((a, b) => Number(b.price) - Number(a.price))
  }
  return list
})

// ============================================================
// 試穿互動
// ============================================================

// 目前總價
const currentTotal = computed(() => lookTotal(look))

// 點「試穿」：依商品分類自動放進對應 Slot；同類商品直接替換
// FULL_BODY（洋裝）與 UPPER_BODY / BOTTOM 互斥
function tryOn(product) {
  const slot = categoryToSlot(product.categoryType)
  if (!slot) {
    showToast('此分類不支援試穿')
    return
  }
  if (!product.chosenVariant) product.chosenVariant = defaultVariant(product)
  if (slot === 'FULL_BODY') {
    look.UPPER_BODY = null
    look.BOTTOM = null
  } else if (slot === 'UPPER_BODY' || slot === 'BOTTOM') {
    look.FULL_BODY = null
  }
  look[slot] = product
  selectedProduct.value = product
  showToast(`已將「${product.name}」放入${SLOT_LABELS[slot]}`)
}

// 使用者切換某套商品所選的規格（顏色/尺寸）
function changeVariant(product, variant) {
  product.chosenVariant = variant
  selectedProduct.value = product
  showToast(`已切換至 ${variant.color} / ${variant.size}`)
}

// 移除單一 Slot
function removeSlot(slot) {
  if (look[slot] && selectedProduct.value && selectedProduct.value.productId === look[slot].productId) {
    selectedProduct.value = null
  }
  look[slot] = null
}

// 清空目前穿搭
function clearCurrentLook() {
  Object.assign(look, emptyLook())
  selectedProduct.value = null
  showToast('已清空目前穿搭')
}

// 隨機搭配：UPPER_BODY / BOTTOM 必選，HEADWEAR / FULL_BODY 隨機
function randomizeOutfit() {
  const bySlot = (slot) =>
    products.value.filter((p) => categoryToSlot(p.categoryType) === slot)

  const pick = (list) => {
    if (!list.length) return null
    const p = list[Math.floor(Math.random() * list.length)]
    p.chosenVariant = p.chosenVariant || defaultVariant(p)
    return p
  }

  const next = emptyLook()
  next.UPPER_BODY = pick(bySlot('UPPER_BODY'))
  next.BOTTOM = pick(bySlot('BOTTOM'))
  if (Math.random() < 0.5) next.HEADWEAR = pick(bySlot('HEADWEAR'))
  if (Math.random() < 0.3) next.FULL_BODY = pick(bySlot('FULL_BODY'))
  // FULL_BODY 存在時，UPPER_BODY / BOTTOM 互斥
  if (next.FULL_BODY) {
    next.UPPER_BODY = null
    next.BOTTOM = null
  }

  Object.assign(look, next)
  selectedProduct.value = null
  showToast('已產生隨機搭配')
}

// Canvas 縮放
function zoomIn() {
  canvasScale.value = Math.min(1.4, Math.round((canvasScale.value + 0.1) * 10) / 10)
}
function zoomOut() {
  canvasScale.value = Math.max(0.7, Math.round((canvasScale.value - 0.1) * 10) / 10)
}
function resetZoom() {
  canvasScale.value = 1
}

// 查看商品頁
function openProductDetail(product) {
  router.push(`/store/product/${product.productId}`)
}

// 加入購物車：把目前已選的所有商品各1件加入購物車
async function addChosenToCart() {
  const items = chosenItems()
    .filter((slot) => look[slot])
    .map((slot) => look[slot])
  if (!items.length) {
    errorMsg.value = '請至少選擇一件商品'
    return
  }
  // 未登入：導去登入頁
  if (!isLoggedIn.value) {
    router.push({ path: '/login', query: { redirect: '/outfits' } })
    return
  }
  try {
    const cart = await cartApi.findByUserId(userId.value)
    if (!cart || !cart.cartId) {
      errorMsg.value = '找不到購物車，請先登入'
      return
    }
    let added = 0
    for (const p of items) {
      const v = chosenVariantOf(p)
      if (!v || !v.variantId) continue
      try {
        await cartItemApi.add({ cartId: cart.cartId, variantId: v.variantId, productQuantity: 1 })
        added++
      } catch (e) {
        // 同規格可能已在購物車（後端會更新數量），不中斷
      }
    }
    showToast(`已將 ${added} 件商品加入購物車`)
  } catch (e) {
    errorMsg.value = '加入購物車失敗：' + e.message
  }
}

// 右側「選擇商品」：切換左側分類並捲回商品區
// 這裡收到的 slot 是 UPPER_BODY / BOTTOM / HEADWEAR / FULL_BODY，
// 要轉成左側的實際商品分類。
function chooseSlot(slot) {
  const map = {
    UPPER_BODY: 'TOP',
    BOTTOM: 'BOTTOM',
    HEADWEAR: 'HEADWEAR',
    FULL_BODY: 'DRESS',
  }
  activeCategory.value = map[slot] || 'ALL'
  browserCol.value && browserCol.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 收藏愛心（待串後端收藏 API）
function toggleFavorite(productId) {
  if (favoriteIds.has(productId)) favoriteIds.delete(productId)
  else favoriteIds.add(productId)
}

// ============================================================
// 儲存 / 收藏穿搭
// ============================================================

async function fetchSavedOutfitsData() {
  loadingOutfits.value = true
  errorMsg.value = ''
  try {
    const list = await fetchSavedOutfits(userId.value)
    savedOutfits.value = list.map(augmentOutfit)
  } catch (e) {
    errorMsg.value = '穿搭載入失敗，請稍後再試'
  } finally {
    loadingOutfits.value = false
  }
}

// 把 OutfitResponse.items（含規格資訊）加上試穿縮圖，供下方 Carousel 預覽
function augmentOutfit(outfit) {
  const mini = []
  for (const it of outfit.items || []) {
    const slot = it.slotType
    if (!SLOT_LABELS[slot]) continue
    const png = it.variantOutfitPng || (it.productName && productMap.value.get(it.productId)?.outfitPng) || null
    mini.push({
      slot,
      label: SLOT_LABELS[slot],
      png,
    })
  }
  return { ...outfit, mini }
}

// 點收藏卡 → 載入整套穿搭
async function loadOutfit(outfit) {
  try {
    const data = await loadOutfitData(outfit.outfitId)
    const loaded = { ...data.look }
    // 互斥規則：洋裝存在時，脫下上衣與下身（防舊資料不一致）
    if (loaded.FULL_BODY) {
      loaded.UPPER_BODY = null
      loaded.BOTTOM = null
    }
    Object.assign(look, loaded)
    editingOutfitId.value = data.outfitId
    editingOutfitName.value = data.name
    selectedProduct.value = null
    showToast(`已載入「${data.name}」`)
  } catch (e) {
    errorMsg.value = '載入穿搭失敗：' + e.message
  }
}

// 點「儲存穿搭」CTA
function openSaveModal() {
  if (!chosenItems().length) {
    errorMsg.value = '請至少選擇一件商品再儲存穿搭'
    showToast('')
    return
  }
  errorMsg.value = ''
  showSaveModal.value = true
}

function chosenItems() {
  return SLOTS.filter((slot) => look[slot])
}

// Modal 確定儲存
async function confirmSave(name) {
  try {
    await saveLook(userId.value, name, { ...look }, editingOutfitId.value)
    editingOutfitName.value = name
    showSaveModal.value = false
    showToast(editingOutfitId.value ? '穿搭已更新' : '穿搭已儲存')
    await fetchSavedOutfitsData()
  } catch (e) {
    errorMsg.value = '儲存失敗：' + e.message
  }
}

async function renameOutfitAction(outfit) {
  const name = window.prompt(`重新命名「${outfit.name}」`, outfit.name)
  if (!name || !name.trim()) return
  try {
    await renameOutfit(outfit.outfitId, name.trim())
    if (editingOutfitId.value === outfit.outfitId) editingOutfitName.value = name.trim()
    showToast('已重新命名')
    await fetchSavedOutfitsData()
  } catch (e) {
    errorMsg.value = '重新命名失敗：' + e.message
  }
}

async function deleteOutfitAction(outfit) {
  if (!window.confirm(`確定刪除穿搭「${outfit.name}」？`)) return
  try {
    await deleteOutfit(outfit.outfitId)
    if (editingOutfitId.value === outfit.outfitId) {
      Object.assign(look, emptyLook())
      editingOutfitId.value = null
      editingOutfitName.value = ''
      selectedProduct.value = null
    }
    showToast('已刪除穿搭')
    await fetchSavedOutfitsData()
  } catch (e) {
    errorMsg.value = '刪除失敗：' + e.message
  }
}

// 新增另一套穿搭
function newOutfit() {
  Object.assign(look, emptyLook())
  editingOutfitId.value = null
  editingOutfitName.value = ''
  selectedProduct.value = null
  showToast('開始建立新的穿搭')
  if (window.scrollTo) window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ============================================================
// 提示
// ============================================================

function showToast(text) {
  toast.value = text
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = ''
  }, 2600)
}

onMounted(async () => {
  await fetchProducts()
  await fetchSavedOutfitsData()
})

onBeforeUnmount(() => {
  if (toastTimer) clearTimeout(toastTimer)
})
</script>

<template>
  <div class="outfit-page">
    <div class="outfit-inner">
      <transition name="fade">
        <div v-if="toast" class="toast">{{ toast }}</div>
      </transition>
      <div v-if="errorMsg" class="alert alert-error page-alert">{{ errorMsg }}</div>

      <div class="outfit-grid">
        <aside ref="browserCol" class="browser-col">
          <ProductBrowser
            :products="displayedProducts"
            :loading="loadingProducts"
            :active-category="activeCategory"
            :sort-option="sortOption"
            :favorite-ids="[...favoriteIds]"
            @update:active-category="(v) => (activeCategory = v)"
            @update:sort-option="(v) => (sortOption = v)"
            @try-on="tryOn"
            @favorite="toggleFavorite"
            @detail="(id) => openProductDetail({ productId: id })"
          />
        </aside>

        <section class="canvas-col">
          <TryOnCanvas
            :look="look"
            :scale="canvasScale"
            @clear="clearCurrentLook"
            @randomize="randomizeOutfit"
            @zoom-in="zoomIn"
            @zoom-out="zoomOut"
            @reset-zoom="resetZoom"
          />
        </section>

        <aside class="panel-col">
          <SelectedItemsPanel
            :look="look"
            :selected-product="selectedProduct"
            @remove-slot="removeSlot"
            @choose-slot="chooseSlot"
            @view-product="openProductDetail"
            @preview="(p) => (selectedProduct = p)"
            @change-variant="changeVariant"
            @save="openSaveModal"
            @add-to-cart="addChosenToCart"
          />
        </aside>
      </div>

      <div class="saved-section card">
        <SavedOutfitCarousel
          :outfits="savedOutfits"
          :loading="loadingOutfits"
          @load-outfit="loadOutfit"
          @delete-outfit="deleteOutfitAction"
          @rename-outfit="renameOutfitAction"
          @new-outfit="newOutfit"
        />
      </div>
    </div>

    <SaveOutfitModal
      :open="showSaveModal"
      :current-name="editingOutfitName"
      @save="confirmSave"
      @cancel="showSaveModal = false"
    />
  </div>
</template>

<style scoped>
.outfit-page {
  background: #fff;
  min-height: 100vh;
}
.outfit-inner {
  max-width: 1380px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  position: relative;
}
.page-alert {
  margin-bottom: 14px;
}
.col-head {
  font-size: 18px;
}

/* ── 三欄 ── */
.outfit-grid {
  display: grid;
  grid-template-columns: 380px minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
}
.browser-col {
  position: sticky;
  top: 76px;
  height: calc(100vh - 100px);
  min-height: 0;
}
.canvas-col {
  min-width: 0;
}
.panel-col {
  position: sticky;
  top: 76px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  padding-right: 2px;
}

/* ── 下方收藏 ── */
.saved-section {
  margin-top: 28px;
  padding: 16px;
}

/* ── Toast ── */
.toast {
  position: fixed;
  top: 70px;
  left: 50%;
  transform: translateX(-50%);
  background: #161616;
  color: #fff;
  padding: 10px 18px;
  border-radius: 4px;
  font-size: 13px;
  z-index: 200;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  letter-spacing: 0.04em;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -6px);
}

/* ── Responsive ── */
@media (max-width: 1120px) {
  .outfit-grid {
    grid-template-columns: 1fr;
  }
  .browser-col {
    position: static;
    height: auto;
  }
  .panel-col {
    position: static;
    max-height: none;
    overflow: visible;
  }
}
</style>