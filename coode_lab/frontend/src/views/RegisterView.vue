<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userApi } from '../api'
import { setAuth } from '../composables/auth'

const router = useRouter()
const route = useRoute()

const form = ref({
  name: '',
  email: '',
  phone: '',
  password: '',
  confirm: '',
  gender: 'MALE',
  birthday: '',
  creditCard: '',
})
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (!form.value.name || !form.value.email || !form.value.password) {
    error.value = '請填寫姓名、Email 與密碼'
    return
  }
  if (form.value.password !== form.value.confirm) {
    error.value = '兩次輸入的密碼不一致'
    return
  }
  loading.value = true
  try {
    const user = await userApi.register({
      name: form.value.name,
      email: form.value.email,
      phone: form.value.phone,
      password: form.value.password,
      gender: form.value.gender,
      birthday: form.value.birthday || null,
      creditCard: form.value.creditCard || null,
    })
    // 註冊成功自動登入（user 身份）並前往商城
    setAuth({ role: 'user', id: user.userId, name: user.name, email: user.email })
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' ? redirect : '/store')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <p class="brand">Coode LAB</p>
      <h1>註冊</h1>
      <p class="sub">建立會員帳號，開始購物</p>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <div class="form-row">
        <div class="form-field">
          <label>姓名</label>
          <input v-model="form.name" placeholder="姓名" />
        </div>
        <div class="form-field">
          <label>電話</label>
          <input v-model="form.phone" placeholder="09xxxxxxxx" />
        </div>
      </div>
      <div class="form-field">
        <label>Email</label>
        <input v-model="form.email" type="email" placeholder="example@coode.com" />
      </div>
      <div class="form-field">
        <label>性別</label>
        <select v-model="form.gender">
          <option value="MALE">男</option>
          <option value="FEMALE">女</option>
        </select>
      </div>
      <div class="form-field">
        <label>生日</label>
        <input v-model="form.birthday" type="date" />
      </div>
      <div class="form-field">
        <label>密碼</label>
        <input v-model="form.password" type="password" placeholder="請輸入密碼" />
      </div>
      <div class="form-field">
        <label>確認密碼</label>
        <input v-model="form.confirm" type="password" placeholder="再次輸入密碼" @keyup.enter="submit" />
      </div>

      <button class="btn btn-primary btn-block" :disabled="loading" @click="submit">
        {{ loading ? '註冊中...' : '註冊' }}
      </button>

      <p class="muted center">
        已經有帳號？
        <RouterLink :to="`/login${route.query.redirect ? '?redirect=' + route.query.redirect : ''}`">前往登入</RouterLink>
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
  max-width: 460px;
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