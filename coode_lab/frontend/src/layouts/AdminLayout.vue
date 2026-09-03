<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { clearAuth } from '../composables/auth'

const router = useRouter()

function logout() {
  clearAuth()
  router.push('/store')
}
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink to="/admin" class="brand">Coode LAB 管理台</RouterLink>
      <div class="side-label">管理</div>
      <RouterLink to="/admin/users">會員管理</RouterLink>
      <RouterLink to="/admin/vendors">廠商管理</RouterLink>
      <RouterLink to="/admin/admins">管理員</RouterLink>
      <RouterLink to="/admin/products">商品管理</RouterLink>
      <RouterLink to="/admin/orders">訂單管理</RouterLink>
      <RouterLink to="/admin/returns">退換貨管理</RouterLink>
      <div class="side-label">帳號</div>
      <button class="logout-btn" @click="logout">← 登出</button>
    </aside>
    <main class="admin-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.logout-btn {
  background: none;
  border: none;
  color: inherit;
  text-align: left;
  font: inherit;
  cursor: pointer;
  padding: 8px 12px;
  display: block;
  width: 100%;
  text-decoration: none;
}
.logout-btn:hover {
  opacity: 0.8;
}
</style>
