<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuth, clearAuth, currentUserId } from '../composables/auth'
import { cartApi, cartItemApi } from '../api'

const router = useRouter()
const route = useRoute()
const auth = useAuth()

const menuOpen = ref(false)
const cartCount = ref(0)
// 依捲動位置決定的導覽 active 狀態：首頁/精選/商品
const activeNav = ref('home')

const userMenuOpen = ref(false)
const searchOpen = ref(false)
const searchKeyword = ref('')
const selectedPatterns = ref([])
const selectedCategories = ref([])
const selectedStyles = ref([])
function onDocClick() {
  if (userMenuOpen.value) userMenuOpen.value = false
}
onMounted(() => {
  document.addEventListener('click', onDocClick)
  document.addEventListener('scroll', onScroll, { passive: true })
  refreshCartCount()
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('scroll', onScroll)
})

// 依捲動位置決定導覽 active（僅在首頁 /store）
function onScroll() {
  if (route.path !== '/store') return
  const anchors = document.querySelectorAll('[data-nav-section]')
  if (!anchors.length) return
  const marker = window.innerHeight * 0.35 // 畫面 35% 高度當基準線
  let current = 'home'
  anchors.forEach((el) => {
    const key = el.getAttribute('data-nav-section')
    if (el.getBoundingClientRect().top <= marker) {
      current = key
    } else if (current === 'home' && key === 'home' && el.getBoundingClientRect().top > marker && window.scrollY < 10) {
      current = 'home'
    }
  })
  activeNav.value = current
}

function logout() {
  clearAuth()
  menuOpen.value = false
  userMenuOpen.value = false
  router.push('/store')
}

// 點首頁/Logo：回到首頁並捲到最上方（TOP 概念）
function goHome() {
  menuOpen.value = false
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function go(path) {
  menuOpen.value = false
  userMenuOpen.value = false
  router.push(path)
}

function toggleSearch() {
  searchOpen.value = !searchOpen.value
  if (searchOpen.value) {
    searchKeyword.value = ''
    selectedPatterns.value = []
    selectedCategories.value = []
    selectedStyles.value = []
  }
}

function togglePattern(v) {
  const i = selectedPatterns.value.indexOf(v)
  if (i === -1) selectedPatterns.value.push(v)
  else selectedPatterns.value.splice(i, 1)
}
function toggleCategory(v) {
  const i = selectedCategories.value.indexOf(v)
  if (i === -1) selectedCategories.value.push(v)
  else selectedCategories.value.splice(i, 1)
}
function toggleStyle(v) {
  const i = selectedStyles.value.indexOf(v)
  if (i === -1) selectedStyles.value.push(v)
  else selectedStyles.value.splice(i, 1)
}

function clearSearch() {
  searchKeyword.value = ''
  selectedPatterns.value = []
  selectedCategories.value = []
  selectedStyles.value = []
}

function submitSearch() {
  const q = searchKeyword.value.trim()
  const query = {}
  if (q) query.keyword = q
  if (selectedPatterns.value.length) query.pattern = selectedPatterns.value.join(',')
  if (selectedCategories.value.length) query.categoryType = selectedCategories.value.join(',')
  if (selectedStyles.value.length) query.style = selectedStyles.value.join(',')
  searchOpen.value = false
  router.push({ path: '/store/products', query })
}

async function refreshCartCount() {
  if (!auth.value || auth.value.role !== 'user') {
    cartCount.value = 0
    return
  }
  try {
    const cart = await cartApi.findByUserId(currentUserId())
    const c = await cartItemApi.count(cart.cartId)
    const n = c && typeof c === 'object' ? Number(c.count ?? c.total ?? 0) : Number(c ?? 0)
    cartCount.value = Number.isFinite(n) && n > 0 ? n : 0
  } catch (e) {
    cartCount.value = 0
  }
}

watch(() => route.fullPath, refreshCartCount)

// 導航完成後依捲動位置重算 active（等子元件渲染完）
watch(
  () => route.fullPath,
  () => {
    if (route.path !== '/store') {
      activeNav.value = 'home'
      return
    }
    nextTick(onScroll)
  },
)
</script>

<template>
  <div class="store-shell">
    <!-- 黑色促銷條 -->
    <div class="promo-strip">
      <p>夏季系列全新登場 · 滿 NT$2,000 即享免運優惠</p>
    </div>

    <!-- 白色 sticky 導覽 -->
    <header class="store-header">
      <div class="header-inner">
        <button class="burger" aria-label="選單" @click="menuOpen = !menuOpen">
          <span></span><span></span><span></span>
        </button>

        <RouterLink to="/store" class="logo" @click="goHome">
          <img src="/images/coode_lab_logo.png" alt="Coode LAB logo" class="logo-img" />
          <span class="logo-text">COODE&nbsp;LAB</span>
        </RouterLink>

        <nav class="store-nav">
          <RouterLink to="/store" :class="{ 'is-active': activeNav === 'home' }" @click="goHome">首頁</RouterLink>
          <RouterLink to="/store?new=1" :class="{ 'is-active': activeNav === 'new' }">精選</RouterLink>
          <RouterLink to="/store?go=products" :class="{ 'is-active': activeNav === 'products' }">商品</RouterLink>
        </nav>

        <div class="header-right">
          <template v-if="auth && auth.role === 'user'">
            <div
              class="user-menu"
              @mouseenter="userMenuOpen = true"
              @mouseleave="userMenuOpen = false"
            >
              <button
                class="user-trigger"
                :aria-expanded="userMenuOpen"
                @click.stop="userMenuOpen = !userMenuOpen"
              >
                你好，{{ auth.name || auth.email }}
                <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </button>
              <transition name="drop">
                <div v-if="userMenuOpen" class="user-dropdown">
                  <RouterLink to="/orders" class="drop-item" @click="userMenuOpen = false">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M6 4h12v16H6zM9 4V2m6 2V2M9 12h6m-6 4h6" />
                    </svg>
                    我的訂單
                  </RouterLink>
                  <RouterLink to="/returns" class="drop-item" @click="userMenuOpen = false">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M3 12a9 9 0 1 0 3-6.7L3 8" />
                      <path d="M3 3v5h5" />
                    </svg>
                    退貨申請
                  </RouterLink>
                  <RouterLink to="/outfits" class="drop-item" @click="userMenuOpen = false">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M6 3h4l1 3h7a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" />
                      <path d="M9 10h6m-6 4h6" />
                    </svg>
                    穿搭
                  </RouterLink>
                  <RouterLink to="/account" class="drop-item" @click="userMenuOpen = false">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6">
                      <circle cx="12" cy="8" r="4" />
                      <path d="M4 21c1.5-3.5 4.5-5 8-5s6.5 1.5 8 5" />
                    </svg>
                    會員資訊
                  </RouterLink>
                  <button class="drop-item drop-logout" @click="logout">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M15 4H5v16h10M9 12h11m-3-3l3 3-3 3" />
                    </svg>
                    登出
                  </button>
                </div>
              </transition>
            </div>
          </template>
          <template v-else-if="auth">
            <span class="hello">你好，{{ auth.name || auth.email }}</span>
            <RouterLink v-if="auth.role === 'vendor'" to="/vendor/dashboard" class="link-small">廠商後台</RouterLink>
            <RouterLink v-else-if="auth.role === 'admin'" to="/admin/users" class="link-small">管理員後台</RouterLink>
            <button class="link-small link-btn" @click="logout">登出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="link-small">登入</RouterLink>
            <RouterLink to="/register" class="link-small">註冊</RouterLink>
          </template>

          <button class="search-btn" aria-label="搜尋" @click="toggleSearch">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
          </button>

          <RouterLink to="/cart" class="cart-link" aria-label="購物車">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.6">
              <path d="M5 8h14l-1.2 11a2 2 0 0 1-2 1.8H8.2a2 2 0 0 1-2-1.8L5 8z" />
              <path d="M8.5 10V6.5a3.5 3.5 0 0 1 7 0V10" />
            </svg>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </RouterLink>
        </div>
      </div>

      <!-- 手機選單 -->
      <transition name="drawer">
        <nav v-if="menuOpen" class="mobile-nav">
          <RouterLink to="/store" class="mobile-link" @click="goHome">首頁</RouterLink>
          <RouterLink to="/store?new=1" class="mobile-link" @click="menuOpen = false">精選</RouterLink>
          <RouterLink to="/store?go=products" class="mobile-link" @click="menuOpen = false">商品</RouterLink>
          <button class="mobile-link mobile-action" @click="menuOpen = false; toggleSearch()">搜尋</button>
          <RouterLink to="/cart" class="mobile-link" @click="menuOpen = false">購物車</RouterLink>
          <template v-if="auth && auth.role === 'user'">
            <RouterLink to="/orders" class="mobile-link" @click="menuOpen = false">我的訂單</RouterLink>
            <RouterLink to="/returns" class="mobile-link" @click="menuOpen = false">退貨申請</RouterLink>
            <RouterLink to="/outfits" class="mobile-link" @click="menuOpen = false">穿搭</RouterLink>
            <RouterLink to="/account" class="mobile-link" @click="menuOpen = false">會員資訊</RouterLink>
            <button class="mobile-link mobile-action" @click="logout">登出</button>
          </template>
          <template v-else-if="auth">
            <RouterLink v-if="auth.role === 'vendor'" to="/vendor/dashboard" class="mobile-link" @click="menuOpen = false">廠商後台</RouterLink>
            <RouterLink v-else-if="auth.role === 'admin'" to="/admin/users" class="mobile-link" @click="menuOpen = false">管理員後台</RouterLink>
            <button class="mobile-link mobile-action" @click="logout">登出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="mobile-link" @click="menuOpen = false">登入</RouterLink>
            <RouterLink to="/register" class="mobile-link" @click="menuOpen = false">註冊</RouterLink>
          </template>
        </nav>
      </transition>
    </header>

    <!-- 搜尋彈窗 -->
    <transition name="fade">
      <div v-if="searchOpen" class="search-overlay" @click.self="searchOpen = false">
        <div class="search-panel">
          <div class="search-top">
            <svg class="search-top-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜尋商品名稱..."
              autofocus
              @keyup.enter="submitSearch"
            />
            <button v-if="searchKeyword || selectedPatterns.length || selectedCategories.length || selectedStyles.length" class="search-clear" @click="clearSearch">清除全部</button>
            <button class="search-close-x" @click="searchOpen = false">✕</button>
          </div>

          <div class="search-tags-section">
            <div class="search-tags-group">
              <p class="search-tags-label">款式</p>
              <div class="search-tags">
                <button class="search-tag" :class="{ 'is-active': selectedPatterns.includes('MEN') }" @click="togglePattern('MEN')">男裝</button>
                <button class="search-tag" :class="{ 'is-active': selectedPatterns.includes('WOMEN') }" @click="togglePattern('WOMEN')">女裝</button>
                <button class="search-tag" :class="{ 'is-active': selectedPatterns.includes('KIDS') }" @click="togglePattern('KIDS')">童裝</button>
              </div>
            </div>
            <div class="search-tags-group">
              <p class="search-tags-label">類別</p>
              <div class="search-tags">
                <button class="search-tag" :class="{ 'is-active': selectedCategories.includes('TOP') }" @click="toggleCategory('TOP')">上衣</button>
                <button class="search-tag" :class="{ 'is-active': selectedCategories.includes('OUTER') }" @click="toggleCategory('OUTER')">外套</button>
                <button class="search-tag" :class="{ 'is-active': selectedCategories.includes('BOTTOM') }" @click="toggleCategory('BOTTOM')">下著</button>
                <button class="search-tag" :class="{ 'is-active': selectedCategories.includes('DRESS') }" @click="toggleCategory('DRESS')">洋裝</button>
                <button class="search-tag" :class="{ 'is-active': selectedCategories.includes('HEADWEAR') }" @click="toggleCategory('HEADWEAR')">頭飾</button>
              </div>
            </div>
            <div class="search-tags-group">
              <p class="search-tags-label">風格</p>
              <div class="search-tags">
                <button class="search-tag" :class="{ 'is-active': selectedStyles.includes('日系') }" @click="toggleStyle('日系')">日系</button>
                <button class="search-tag" :class="{ 'is-active': selectedStyles.includes('韓系') }" @click="toggleStyle('韓系')">韓系</button>
                <button class="search-tag" :class="{ 'is-active': selectedStyles.includes('休閒') }" @click="toggleStyle('休閒')">休閒</button>
                <button class="search-tag" :class="{ 'is-active': selectedStyles.includes('機能') }" @click="toggleStyle('機能')">機能</button>
                <button class="search-tag" :class="{ 'is-active': selectedStyles.includes('正式') }" @click="toggleStyle('正式')">正式</button>
              </div>
            </div>
          </div>

          <button class="search-submit" @click="submitSearch">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            搜尋
          </button>
        </div>
      </div>
    </transition>

    <main class="store-main">
      <RouterView />
    </main>

    <!-- 黑底四欄 Footer -->
    <footer class="store-footer">
      <div class="footer-inner">
        <div class="footer-col footer-brand">
          <div class="footer-logo">Coode LAB</div>
          <p>以簡約剪裁與低彩度質感，打造每一天的城市穿搭。</p>
        </div>
        <div class="footer-col">
          <h4>購物指南</h4>
          <button class="footer-link" @click="go('/store')">配送與運費</button>
          <button class="footer-link" @click="go('/store')">尺寸說明</button>
          <button class="footer-link" @click="go('/returns')">退換貨政策</button>
        </div>
        <div class="footer-col">
          <h4>會員服務</h4>
          <button class="footer-link" @click="go('/orders')">我的訂單</button>
          <button class="footer-link" @click="go('/account')">會員資訊</button>
          <button class="footer-link" @click="go('/returns')">退換貨申請</button>
          <button class="footer-link" @click="go('/outfits')">穿搭試衣間</button>
        </div>
        <div class="footer-col">
          <h4>聯絡我們</h4>
          <p class="footer-line">service@coodelab.com</p>
          <p class="footer-line">02-2345-6789</p>
          <p class="footer-line">台北市信義區松高路 1 號 9 樓</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© {{ new Date().getFullYear() }} Coode LAB · 僅供練習用途</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.store-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #fff;
}

/* ── 促銷條 ── */
.promo-strip {
  background: #111111;
  color: #fff;
  text-align: center;
  font-size: 12px;
  letter-spacing: 0.14em;
  padding: 9px 16px;
}
.promo-strip p {
  margin: 0;
}

/* ── Header ── */
.store-header {
  position: sticky;
  top: 0;
  z-index: 60;
  background: #fff;
  border-bottom: 1px solid var(--line);
}
.header-inner {
  max-width: 1240px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  padding: 14px 24px;
}
.logo {
  justify-self: start;
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo-img {
  height: 32px;
  display: block;
}
.logo-text {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: var(--ink);
  line-height: 1;
}
.logo:hover {
  color: var(--ink);
}
.store-nav {
  display: flex;
  align-items: center;
  gap: 30px;
}
.store-nav a {
  font-size: 13px;
  letter-spacing: 0.16em;
  color: #3c3a37;
  position: relative;
  padding: 4px 0;
}
.store-nav a:hover {
  color: var(--ink);
}
.store-nav a.is-active {
  color: var(--ink);
}
.store-nav a.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  background: var(--ink);
}
.nav-link:hover {
  color: var(--ink);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
  justify-self: end;
}
.hello {
  font-size: 13px;
  color: var(--muted);
}
/* ── 會員下拉選單 ── */
.user-menu {
  position: relative;
}
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #3c3a37;
  font-size: 13px;
  letter-spacing: 0.06em;
  padding: 8px 2px;
  cursor: pointer;
  border-bottom: 1px solid transparent;
  transition: color 0.15s ease;
}
.user-trigger:hover {
  color: var(--ink);
}
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 190px;
  background: #fff;
  border: 1px solid var(--line);
  border-top: 2px solid var(--ink);
  border-radius: 4px;
  box-shadow: var(--shadow-soft);
  padding: 6px 0;
  z-index: 80;
}
.drop-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  text-align: left;
  padding: 11px 16px;
  font-size: 13px;
  letter-spacing: 0.1em;
  color: #3c3a37;
  background: none;
  border: none;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}
.drop-item:hover {
  background: var(--paper-soft);
  color: var(--ink);
}
.drop-logout {
  color: var(--accent);
}
.drop-logout:hover {
  background: #fdf3f2;
  color: var(--accent);
}
.drop-enter-active,
.drop-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}
.drop-enter-from,
.drop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
.link-small {
  font-size: 12px;
  letter-spacing: 0.1em;
  color: #3c3a37;
  background: none;
  border: none;
  padding: 0;
}
.link-small:hover {
  color: var(--ink);
}
.link-btn {
  cursor: pointer;
}
.cart-link {
  position: relative;
  color: var(--ink);
  display: flex;
}
.cart-badge {
  position: absolute;
  top: -7px;
  right: -10px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── 搜尋按鈕 ── */
.search-btn {
  background: none;
  border: none;
  color: #3c3a37;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color 0.15s;
}
.search-btn:hover {
  color: var(--ink);
}

/* ── 搜尋彈窗 ── */
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  padding-top: 80px;
}
.search-panel {
  width: 100%;
  max-width: 560px;
  background: #fff;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
  padding: 24px 28px 28px;
  max-height: calc(100vh - 80px);
  overflow-y: auto;
}
.search-top {
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #e8e4dd;
  padding-bottom: 14px;
  margin-bottom: 20px;
}
.search-top-icon {
  flex-shrink: 0;
  color: #b0a89e;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--ink);
  background: transparent;
}
.search-input::placeholder {
  color: #b0a89e;
}
.search-clear {
  background: none;
  border: none;
  color: #b0a89e;
  font-size: 15px;
  cursor: pointer;
  padding: 2px 4px;
  line-height: 1;
  transition: color 0.15s;
}
.search-clear:hover {
  color: var(--ink);
}
.search-close-x {
  background: none;
  border: none;
  color: #b0a89e;
  font-size: 18px;
  cursor: pointer;
  padding: 2px 4px;
  line-height: 1;
  margin-left: 4px;
  transition: color 0.15s;
}
.search-close-x:hover {
  color: var(--ink);
}

/* ── 標籤區 ── */
.search-tags-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.search-tags-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.search-tags-label {
  font-size: 11px;
  letter-spacing: 0.18em;
  color: #b0a89e;
  text-transform: uppercase;
  font-weight: 600;
  margin: 0;
}
.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.search-tag {
  border: 1px solid #d5cfc5;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 13px;
  letter-spacing: 0.06em;
  color: #3c3a37;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
}
.search-tag:hover {
  background: var(--ink);
  color: #fff;
  border-color: var(--ink);
}
.search-tag.is-active {
  background: var(--ink);
  color: #fff;
  border-color: var(--ink);
}

/* ── 搜尋按鈕 ── */
.search-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  margin-top: 24px;
  padding: 12px 0;
  border: none;
  border-radius: 8px;
  background: var(--ink);
  color: #fff;
  font-size: 14px;
  letter-spacing: 0.1em;
  cursor: pointer;
  transition: opacity 0.15s;
}
.search-submit:hover {
  opacity: 0.85;
}

/* ── 搜尋彈窗動畫 ── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.burger {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  padding: 4px;
}
.burger span {
  width: 20px;
  height: 2px;
  background: var(--ink);
}

/* 手機選單 */
.mobile-nav {
  display: none;
  flex-direction: column;
  border-top: 1px solid var(--line);
  padding: 6px 24px 12px;
  background: #fff;
}
.mobile-link {
  padding: 12px 0;
  font-size: 14px;
  letter-spacing: 0.12em;
  border-bottom: 1px solid #f1efec;
  text-align: left;
  background: none;
  border-left: none;
  border-right: none;
  border-top: none;
  cursor: pointer;
}
.mobile-link:last-child {
  border-bottom: none;
}
.mobile-action {
  color: var(--accent);
}
.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.18s ease;
}
.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

/* ── Main ── */
.store-main {
  flex: 1;
}

/* ── Footer ── */
.store-footer {
  background: #161616;
  color: #cfcfcf;
}
.footer-inner {
  max-width: 1240px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1.2fr;
  gap: 32px;
  padding: 56px 24px 40px;
}
.footer-logo {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.22em;
  color: #fff;
  margin-bottom: 14px;
}
.footer-brand p {
  font-size: 13px;
  line-height: 1.8;
  color: #a7a49f;
  max-width: 260px;
}
.footer-col h4 {
  color: #fff;
  font-size: 12px;
  letter-spacing: 0.18em;
  margin-bottom: 16px;
  font-weight: 600;
}
.footer-link {
  display: block;
  background: none;
  border: none;
  color: #a7a49f;
  font-size: 13px;
  padding: 5px 0;
  text-align: left;
  cursor: pointer;
}
.footer-link:hover {
  color: #fff;
}
.footer-line {
  font-size: 13px;
  color: #a7a49f;
  margin: 6px 0;
}
.footer-bottom {
  border-top: 1px solid #2c2c2c;
  padding: 18px 24px;
  text-align: center;
}
.footer-bottom p {
  font-size: 12px;
  color: #8f8d89;
}

/* ── RWD ── */
@media (max-width: 900px) {
  .header-inner {
    grid-template-columns: auto 1fr auto;
  }
  .store-nav {
    display: none;
  }
  .burger {
    display: flex;
  }
  .hello {
    display: none;
  }
  .user-menu {
    display: none;
  }
  .link-small {
    display: none;
  }
  .mobile-nav {
    display: flex;
  }
  .footer-inner {
    grid-template-columns: 1fr 1fr;
    gap: 28px;
  }
}
@media (max-width: 560px) {
  .header-inner {
    padding: 12px 16px;
    grid-template-columns: auto 1fr auto;
  }
  .footer-inner {
    grid-template-columns: 1fr;
    padding: 40px 20px 28px;
  }
}
</style>