<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api'

const router = useRouter()

const form = ref({
  email: '',
  password: '',
  confirm: '',
})
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (!form.value.email) {
    error.value = '請填寫 Email'
    return
  }
  if (!form.value.password) {
    error.value = '請輸入新密碼'
    return
  }
  if (form.value.password.length < 6) {
    error.value = '新密碼長度至少 6 碼'
    return
  }
  if (form.value.password !== form.value.confirm) {
    error.value = '兩次輸入的新密碼不一致'
    return
  }
  loading.value = true
  try {
    await userApi.resetPassword({
      email: form.value.email,
      newPassword: form.value.password,
    })
    router.push('/login')
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
      <h1>修改密碼</h1>
      <p class="sub">輸入 Email 並設定新密碼</p>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <div class="form-field">
        <label>Email</label>
        <input v-model="form.email" type="email" placeholder="example@coode.com" />
      </div>
      <div class="form-field">
        <label>新密碼</label>
        <input v-model="form.password" type="password" placeholder="請輸入新密碼" />
      </div>
      <div class="form-field">
        <label>確認新密碼</label>
        <input v-model="form.confirm" type="password" placeholder="再次輸入新密碼" @keyup.enter="submit" />
      </div>

      <button class="btn btn-primary btn-block" :disabled="loading" @click="submit">
        {{ loading ? '送出中...' : '確認修改' }}
      </button>

      <p class="muted center">
        想起密碼了？
        <RouterLink to="/login">返回登入</RouterLink>
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
