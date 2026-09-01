import { ref, computed } from 'vue'

// 登入狀態存放在 localStorage，reload 後仍能保持登入
const KEY = 'coode_lab.auth'

const state = ref(load())

function load() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    const data = JSON.parse(raw)
    if (!data || !data.role) return null
    return data
  } catch (e) {
    return null
  }
}

function save() {
  if (state.value) {
    localStorage.setItem(KEY, JSON.stringify(state.value))
  } else {
    localStorage.removeItem(KEY)
  }
}

// 設定登入狀態：role = 'user' | 'vendor' | 'admin'
export function setAuth({ role, id, name, email }) {
  state.value = { role, id, name, email }
  save()
}

export function clearAuth() {
  state.value = null
  save()
}

export function useAuth() {
  return state
}

export const isLoggedIn = computed(() => !!state.value)

export const roleHome = {
  user: '/store',
  vendor: '/vendor/dashboard',
  admin: '/admin/users',
}

export function homeFor(role) {
  return roleHome[role] || '/store'
}

// 商城頁面使用的會員 ID：登入為 user 時用登入者的 ID，否則退回測試帳號 1
export function currentUserId() {
  if (state.value && state.value.role === 'user') return state.value.id
  return 1
}