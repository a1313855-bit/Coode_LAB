<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { clearAuth } from '../composables/auth'
import '../assets/dashboard-common.css'

const router = useRouter()

function logout() {
  clearAuth()
  router.push('/store')
}
</script>

<template>
  <div class="vr-app admin-layout">
    <div class="admin-body">
      <aside class="admin-sidebar">
        <div class="brand">
          <span class="brand-mark">COODE</span>
          <span class="brand-text">管理後台</span>
        </div>
        <div class="side-label">管理</div>
        <nav class="side-nav">
          <RouterLink to="/admin/users" class="nav-link">會員管理</RouterLink>
          <RouterLink to="/admin/vendors" class="nav-link">廠商管理</RouterLink>
          <RouterLink to="/admin/admins" class="nav-link">管理員</RouterLink>
          <RouterLink to="/admin/products" class="nav-link">商品管理</RouterLink>
          <RouterLink to="/admin/orders" class="nav-link">訂單管理</RouterLink>
          <RouterLink to="/admin/returns" class="nav-link">退換貨管理</RouterLink>
        </nav>
        <div class="side-foot">
          <button class="logout-btn" @click="logout">← 登出</button>
        </div>
      </aside>

      <main class="admin-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #faf6f0 0%, #f3ece1 480px);
}
.admin-body {
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
  padding: 6px 10px 20px;
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
.side-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: #7a6b5c;
  font-weight: 700;
  padding: 6px 14px 10px;
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
.side-foot {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
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
}
</style>
