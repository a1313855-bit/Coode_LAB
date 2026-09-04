<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { clearAuth } from '../composables/auth'
import '../assets/dashboard-common.css'

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
  <div class="vr-app vendor-shell">
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
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #faf6f0 0%, #f3ece1 480px);
}
.vendor-body {
  flex: 1;
  display: flex;
  align-items: stretch;
}
.admin-sidebar {
  width: 220px;
  flex: 0 0 220px;
  background: linear-gradient(180deg, #3d2e22 0%, #2a1e15 100%);
  color: #c9b8a5;
  display: flex;
  flex-direction: column;
  padding: 18px 12px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 14px;
}
.brand-mark {
  color: #fff;
  font-weight: 900;
  font-size: 18px;
  letter-spacing: 0.5px;
}
.brand-text {
  font-size: 13px;
  color: #c9b8a5;
}
.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nav-link {
  display: block;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  color: #b8a898;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.nav-link.router-link-active {
  background: rgba(185, 138, 95, 0.25);
  color: #fff;
  font-weight: 700;
}
.logout-btn {
  background: none;
  border: none;
  color: #8a7a6c;
  text-align: left;
  font: inherit;
  cursor: pointer;
  padding: 10px 14px;
  display: block;
  width: 100%;
  text-decoration: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
  margin-top: auto;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 16px;
}
.logout-btn:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.admin-main {
  flex: 1;
  padding: 24px;
  background: linear-gradient(180deg, #faf6f0 0%, #f3ece1 480px);
  overflow-x: auto;
}</style>
