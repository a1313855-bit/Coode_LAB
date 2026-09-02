<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuth, clearAuth, currentVendorId } from '../composables/auth'
import { vendorApi } from '../api'

const route = useRoute()
const router = useRouter()
const auth = useAuth()

const pageTitle = computed(() => route.meta.title || '')

const vendorName = ref(auth.value?.role === 'vendor' ? auth.value.name : '')

const nav = [
  { to: '/vendor/dashboard', label: '首頁總覽' },
  { to: '/vendor/products', label: '商品管理' },
  { to: '/vendor/orders', label: '訂單管理' },
  { to: '/vendor/returns', label: '退換貨管理' },
  { to: '/vendor/reports', label: '銷售報表' },
  { to: '/vendor/account', label: '帳號資訊' },
  { to: '/vendor/contract', label: '合約資訊' },
]

function logout() {
  clearAuth()
  router.push('/login')
}

onMounted(async () => {
  try {
    const data = await vendorApi.byId(currentVendorId())
    if (data && data.vendorName) vendorName.value = data.vendorName
  } catch (e) {
    /* keep auth name */
  }
})
</script>

<template>
  <div class="vendor-shell">
    <header class="topbar">
      <div class="page-title">{{ pageTitle }}</div>
      <div class="topbar-right">
        <span class="vendor-name">{{ vendorName }}</span>
        <button class="btn btn-sm" @click="logout">登出</button>
      </div>
    </header>

    <div class="vendor-body">
      <aside class="admin-sidebar">
        <div class="brand">
          <span class="brand-mark">COODE</span>
          <span class="brand-text">廠商後台</span>
        </div>
        <nav class="side-nav">
          <RouterLink
            v-for="item in nav"
            :key="item.to"
            :to="item.to"
            class="nav-link"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
        <div class="side-foot">
          <RouterLink to="/store" class="nav-link back-link">← 回到商城</RouterLink>
        </div>
      </aside>

      <main class="admin-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.vendor-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.topbar {
  height: 56px;
  flex: 0 0 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--c-border);
}
.page-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--c-text);
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.vendor-name {
  font-size: 14px;
  color: var(--c-text-light);
}
.vendor-body {
  flex: 1;
  display: flex;
  align-items: stretch;
}
.admin-sidebar {
  width: 220px;
  flex: 0 0 220px;
  background: #1f2430;
  color: #aeb6c4;
  display: flex;
  flex-direction: column;
  padding: 18px 12px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px 20px;
}
.brand-mark {
  color: #fff;
  font-weight: 900;
  font-size: 18px;
  letter-spacing: 0.5px;
}
.brand-text {
  font-size: 14px;
  color: #cbd2df;
}
.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nav-link {
  display: block;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: #aeb6c4;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.nav-link.router-link-active {
  background: #db2777;
  color: #fff;
  font-weight: 700;
}
.side-foot {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.back-link {
  color: #8a94a6;
}
.admin-main {
  flex: 1;
  padding: 24px;
  background: #f6f7f9;
  overflow-x: auto;
}
</style>