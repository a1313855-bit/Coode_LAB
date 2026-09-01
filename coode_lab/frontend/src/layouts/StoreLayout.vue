<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuth, clearAuth } from '../composables/auth'

const router = useRouter()
const auth = useAuth()

function logout() {
  clearAuth()
  router.push('/store')
}
</script>

<template>
  <div>
    <header class="topbar">
      <div class="topbar-inner">
        <div class="topbar-left">
          <RouterLink to="/" class="brand">Coode LAB</RouterLink>
          <nav>
            <RouterLink to="/store">商城首頁</RouterLink>
            <RouterLink to="/outfits">我的穿搭</RouterLink>
            <RouterLink to="/orders">我的訂單</RouterLink>
            <RouterLink to="/returns">退換貨</RouterLink>
            <RouterLink to="/cart">購物車</RouterLink>
          </nav>
        </div>
        <div class="topbar-right">
          <template v-if="auth">
            <span class="hello">你好，{{ auth.name || auth.email }}</span>
            <RouterLink v-if="auth.role === 'vendor'" to="/vendor/dashboard" class="btn btn-sm">
              前往廠商後台
            </RouterLink>
            <RouterLink v-else-if="auth.role === 'admin'" to="/admin/users" class="btn btn-sm">
              前往管理員後台
            </RouterLink>
            <button class="btn btn-sm" @click="logout">登出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="btn btn-sm btn-primary">登入</RouterLink>
            <RouterLink to="/register" class="btn btn-sm">註冊</RouterLink>
          </template>
        </div>
      </div>
    </header>
    <RouterView />
  </div>
</template>

<style scoped>
.topbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.hello {
  font-size: 14px;
  color: var(--c-text-light);
}
</style>