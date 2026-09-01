<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink to="/vendor" class="brand">廠商後台</RouterLink>
      <div class="side-label">管理</div>
      <RouterLink to="/vendor/dashboard">銷售儀表板</RouterLink>
      <RouterLink to="/vendor/products">商品管理</RouterLink>
      <RouterLink to="/vendor/low-stock">低庫存</RouterLink>
      <RouterLink to="/vendor/orders">訂單處理</RouterLink>
      <RouterLink to="/vendor/returns">退換貨處理</RouterLink>
      <div class="side-label">返回商城</div>
      <RouterLink to="/store">← 回到商城</RouterLink>
    </aside>
    <main class="admin-main">
      <RouterView />
    </main>
  </div>
</template>
