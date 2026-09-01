<script setup>
import { RouterLink, RouterView } from 'vue-router'
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
      <div class="side-label">返回商城</div>
      <RouterLink to="/store">← 回到商城</RouterLink>
    </aside>
    <main class="admin-main">
      <RouterView />
    </main>
  </div>
</template>
