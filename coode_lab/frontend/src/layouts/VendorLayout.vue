<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { clearAuth } from '../composables/auth'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => route.meta.title || '')

const nav = [
  { to: '/vendor/dashboard', label: '首頁總覽' },
  { to: '/vendor/products', label: '商品管理' },
  { to: '/vendor/orders', label: '訂單管理' },
  { to: '/vendor/returns', label: '退換貨管理' },
  { to: '/vendor/reports', label: '銷售報表' },
  { to: '/vendor/low-stock', label: '低庫存管理' },
  { to: '/vendor/account', label: '帳號資訊' },
]

function logout() {
  clearAuth()
  router.push('/login')
}
</script>

<template>
  <div class="vendor-shell">
    <header class="topbar">
      <div class="page-title">{{ pageTitle }}</div>
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
          <button class="logout-btn" @click="logout">← 登出</button>
        </nav>
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
.logout-btn {
  background: none;
  border: none;
  color: #8a94a6;
  text-align: left;
  font: inherit;
  cursor: pointer;
  padding: 10px 12px;
  display: block;
  width: 100%;
  text-decoration: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.logout-btn:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.admin-main {
  flex: 1;
  padding: 24px;
  background: #f6f7f9;
  overflow-x: auto;
}
</style>