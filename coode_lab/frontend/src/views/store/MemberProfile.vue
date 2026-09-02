<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi, cartApi, cartItemApi } from '../../api'
import { formatDate, statusLabel } from '../../utils/format'
import { currentUserId } from '../../composables/auth'

const router = useRouter()

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const message = ref('')

// ╔═══════════════╗
// ║ 個人資料 ║
// ╚═══════════════╝
const profile = ref({
  userId: null,
  email: '',
  name: '',
  phone: '',
  gender: 'MALE',
  birthday: '',
  creditCard: '',
  picture: '',
  status: '',
  createdAt: '',
})

// ╔═══════════════╗
// ║ 修改密碼 ║
// ╚═══════════════╝
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirm: '',
})

// ╔═══════════════╗
// ║ 會員購物車概況 ║
// ╚═══════════════╝
const cart = ref(null)
const cartCount = ref(0)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const user = await userApi.findById(currentUserId())
    profile.value = {
      userId: user.userId,
      email: user.email,
      name: user.name || '',
      phone: user.phone || '',
      gender: user.gender || 'MALE',
      birthday: user.birthday ? String(user.birthday).slice(0, 10) : '',
      creditCard: user.creditCard || '',
      picture: user.picture || '',
      status: user.status || '',
      createdAt: user.createdAt || '',
    }
    await loadCart()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function loadCart() {
  try {
    cart.value = await cartApi.findByUserId(currentUserId())
    if (cart.value) {
      cartCount.value = cart.value.totalQuantity || 0
    }
  } catch (e) {
    cartCount.value = 0
  }
}

async function saveProfile() {
  error.value = ''
  message.value = ''
  if (!profile.value.name) {
    error.value = '請填寫姓名'
    return
  }
  saving.value = true
  try {
    await userApi.update(profile.value.userId, {
      name: profile.value.name,
      phone: profile.value.phone,
      gender: profile.value.gender,
      birthday: profile.value.birthday || null,
      creditCard: profile.value.creditCard || null,
      picture: profile.value.picture || null,
    })
    message.value = '資料已更新'
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  error.value = ''
  message.value = ''
  const { oldPassword, newPassword, confirm } = passwordForm.value
  if (!oldPassword || !newPassword) {
    error.value = '請填寫舊密碼與新密碼'
    return
  }
  if (newPassword !== confirm) {
    error.value = '兩次輸入的新密碼不一致'
    return
  }
  saving.value = true
  try {
    await userApi.changePassword(profile.value.userId, {
      oldPassword,
      newPassword,
    })
    passwordForm.value = { oldPassword: '', newPassword: '', confirm: '' }
    message.value = '密碼已更新'
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header">
      <h1>會員中心</h1>
      <p>管理個人資料與帳號安全</p>
    </div>

    <div v-if="message" class="alert alert-success">{{ message }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>

    <template v-else>
      <!-- 概覽 -->
      <div class="card overview">
        <div class="avatar">
          <img v-if="profile.picture" :src="profile.picture" alt="avatar" />
          <span v-else class="avatar-placeholder">{{ (profile.name || 'U').slice(0, 1) }}</span>
        </div>
        <div class="overview-info">
          <div class="name">{{ profile.name || '未設定姓名' }}</div>
          <div class="email muted">{{ profile.email }}</div>
          <span class="badge" :class="profile.status === 'ACTIVE' ? 'badge-success' : 'badge-muted'">
            {{ statusLabel(profile.status) }}
          </span>
          <div class="joined muted">加入時間：{{ formatDate(profile.createdAt) }}</div>
        </div>
        <div class="overview-summary">
          <RouterLink to="/cart" class="stat">
            <div class="stat-num">{{ cartCount }}</div>
            <div class="stat-label">購物車商品</div>
          </RouterLink>
        </div>
      </div>

      <div class="grid-2">
        <!-- 個人資料 -->
        <div class="card">
          <h3>個人資料</h3>
          <p class="muted">Email 為登入帳號，不可修改</p>

          <div class="form-field">
            <label>Email</label>
            <input :value="profile.email" disabled />
          </div>
          <div class="form-row">
            <div class="form-field">
              <label>姓名</label>
              <input v-model="profile.name" placeholder="請輸入姓名" />
            </div>
            <div class="form-field">
              <label>電話</label>
              <input v-model="profile.phone" placeholder="09xxxxxxxx" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-field">
              <label>性別</label>
              <select v-model="profile.gender">
                <option value="MALE">男</option>
                <option value="FEMALE">女</option>
              </select>
            </div>
            <div class="form-field">
              <label>生日</label>
              <input v-model="profile.birthday" type="date" />
            </div>
          </div>
          <div class="form-field">
            <label>信用卡資訊</label>
            <input v-model="profile.creditCard" placeholder="僅供結帳用，請輸入卡號" />
          </div>
          <div class="form-field">
            <label>大頭貼網址</label>
            <input v-model="profile.picture" placeholder="https://..." />
          </div>

          <button class="btn btn-primary" :disabled="saving" @click="saveProfile">
            {{ saving ? '儲存中...' : '儲存變更' }}
          </button>
        </div>

        <!-- 修改密碼 -->
        <div class="card">
          <h3>修改密碼</h3>
          <p class="muted">定期更換密碼，保護帳號安全</p>

          <div class="form-field">
            <label>目前密碼</label>
            <input v-model="passwordForm.oldPassword" type="password" placeholder="輸入目前密碼" />
          </div>
          <div class="form-field">
            <label>新密碼</label>
            <input v-model="passwordForm.newPassword" type="password" placeholder="輸入新密碼" />
          </div>
          <div class="form-field">
            <label>確認新密碼</label>
            <input
              v-model="passwordForm.confirm"
              type="password"
              placeholder="再次輸入新密碼"
              @keyup.enter="changePassword"
            />
          </div>

          <button class="btn btn-primary" :disabled="saving" @click="changePassword">
            {{ saving ? '更新中...' : '更新密碼' }}
          </button>

          <div class="quick-links">
            <RouterLink to="/orders">查看我的訂單 →</RouterLink>
            <RouterLink to="/returns">退換貨服務 →</RouterLink>
            <RouterLink to="/outfits">我的穿搭 →</RouterLink>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.overview {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.avatar img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--c-primary);
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}
.overview-info {
  flex: 1;
  min-width: 200px;
}
.overview-info .name {
  font-size: 20px;
  font-weight: 800;
}
.overview-info .email {
  font-size: 14px;
}
.overview-info .badge {
  margin-top: 4px;
}
.overview-info .joined {
  font-size: 12px;
  margin-top: 4px;
}
.overview-summary {
  display: flex;
  gap: 14px;
}
.stat {
  text-align: center;
  background: #f9fafb;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 12px 22px;
}
.stat-num {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-primary);
}
.stat-label {
  font-size: 12px;
  color: var(--c-text-light);
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.card h3 {
  font-size: 16px;
  margin-bottom: 4px;
}
.card .muted {
  font-size: 13px;
  margin-bottom: 14px;
}
.quick-links {
  margin-top: 20px;
  border-top: 1px dashed var(--c-border);
  padding-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
}
@media (max-width: 800px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>