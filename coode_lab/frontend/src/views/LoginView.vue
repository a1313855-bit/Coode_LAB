<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userApi, vendorApi, adminApi } from '../api'
import { setAuth, homeFor } from '../composables/auth'

const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const role = await detectRole(email.value, password.value)
    if (role) {
      // 登入成功 → 依角色決定去向
      const redirect = route.query.redirect
      const onlyUser = redirect && role === 'user'
      router.push(onlyUser ? redirect : homeFor(role))
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

// 依「帳號存在於哪個資料表且密碼正確」判斷登入角色
async function detectRole(mail, pw) {
  const tries = [
    { role: 'admin', fn: () => adminApi.login({ email: mail, password: pw }) },
    { role: 'vendor', fn: () => vendorApi.login({ email: mail, password: pw }) },
    { role: 'user', fn: () => userApi.login({ email: mail, password: pw }) },
  ]
  let meaningful = ''
  let lastError = ''
  for (const t of tries) {
    try {
      const data = await t.fn()
      setAuth({ role: t.role, id: pickId(t.role, data), name: pickName(t.role, data), email: mail })
      return t.role
    } catch (err) {
      lastError = err.message || ''
      const msg = String(lastError)
      // 記錄「帳號存在但密碼錯誤／未啟用」這類較有意義的錯誤
      if (msg && !msg.includes('不存在') && !msg.includes('尚未設定')) {
        meaningful = msg
      }
    }
  }
  // 全部失敗：有具體錯誤（如密碼錯誤）就顯示該錯誤，否則顯示查無帳號
  throw new Error(meaningful || '查無此帳號，或帳號與密碼不符')
}

function pickId(role, data) {
  if (!data) return null
  if (role === 'user') return data.userId
  if (role === 'vendor') return data.vendorId
  return data.adminId
}
function pickName(role, data) {
  if (!data) return ''
  if (role === 'user') return data.name
  if (role === 'vendor') return data.vendorName
  return data.email
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <p class="brand">Coode LAB</p>
      <h1>登入</h1>
      <p class="sub">登入後依帳號身份前往對應後台</p>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <div class="form-field">
        <label>Email</label>
        <input v-model="email" type="email" placeholder="example@coode.com" @keyup.enter="submit" />
      </div>
      <div class="form-field">
        <label>密碼</label>
        <input v-model="password" type="password" placeholder="請輸入密碼" @keyup.enter="submit" />
      </div>

      <button class="btn btn-primary btn-block" :disabled="loading" @click="submit">
        {{ loading ? '登入中...' : '登入' }}
      </button>

      <p class="muted center">
        還沒有帳號？
        <RouterLink :to="`/register${route.query.redirect ? '?redirect=' + route.query.redirect : ''}`">立即註冊</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  min-height: 78vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  background: var(--paper-soft);
}
.auth-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 40px 34px;
  box-shadow: var(--shadow-soft);
}
.brand {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.24em;
  text-align: center;
  margin-bottom: 20px;
}
.auth-card h1 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-align: center;
}
.sub {
  text-align: center;
  color: var(--muted);
  font-size: 13px;
  margin: 6px 0 24px;
}
.center {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
}
</style>