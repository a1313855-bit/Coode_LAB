<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../api'
import { currentUserId, setAuth } from '../../composables/auth'
import { formatDate } from '../../utils/format'

const userId = currentUserId()

const user = ref(null)
const loading = ref(true)
const error = ref('')

const saving = ref(false)
const saveMsg = ref('')
const saveError = ref('')

const pw = ref({ currentPassword: '', newPassword: '', confirm: '' })
const savingPw = ref(false)
const pwMsg = ref('')
const pwError = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    user.value = await userApi.findById(userId)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  saveMsg.value = ''
  saveError.value = ''
  if (!user.value) return
  if (!user.value.name.trim()) {
    saveError.value = '姓名不可為空'
    return
  }
  saving.value = true
  try {
    const updated = await userApi.update(userId, {
      name: user.value.name,
      phone: user.value.phone || '',
      creditCard: user.value.creditCard || '',
      gender: user.value.gender || '',
      birthday: user.value.birthday || null,
    })
    user.value = updated
    // 同步更新標頭的會員名稱
    setAuth({ role: 'user', id: updated.userId, name: updated.name, email: updated.email })
    saveMsg.value = '會員資料已更新'
  } catch (e) {
    saveError.value = '更新失敗：' + e.message
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  pwMsg.value = ''
  pwError.value = ''
  if (!pw.value.currentPassword || !pw.value.newPassword) {
    pwError.value = '請填寫目前密碼與新密碼'
    return
  }
  if (pw.value.newPassword !== pw.value.confirm) {
    pwError.value = '兩次輸入的新密碼不一致'
    return
  }
  savingPw.value = true
  try {
    await userApi.changePassword(userId, {
      currentPassword: pw.value.currentPassword,
      newPassword: pw.value.newPassword,
    })
    pwMsg.value = '密碼已更新'
    pw.value = { currentPassword: '', newPassword: '', confirm: '' }
  } catch (e) {
    pwError.value = '變更失敗：' + e.message
  } finally {
    savingPw.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="account-page">
    <div class="container-wide">
      <div class="page-header">
        <h1>會員資訊</h1>
        <p>管理你的個人資料與密碼</p>
      </div>

      <div v-if="loading" class="empty">載入中...</div>
      <div v-else-if="error" class="alert alert-error">載入失敗：{{ error }}</div>

      <div v-else-if="user" class="account-grid">
        <!-- 基本資料 -->
        <section class="panel">
          <div class="panel-head">
            <h2>基本資料</h2>
          </div>
          <div class="panel-body">
            <div v-if="saveMsg" class="alert alert-success">{{ saveMsg }}</div>
            <div v-if="saveError" class="alert alert-error">{{ saveError }}</div>

            <div class="form-field">
              <label>Email（不可變更）</label>
              <input :value="user.email" readonly class="readonly" />
            </div>
            <div class="form-field">
              <label>姓名</label>
              <input v-model="user.name" placeholder="請輸入姓名" />
            </div>
            <div class="form-field">
              <label>電話</label>
              <input v-model="user.phone" placeholder="09xxxxxxxx" />
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>性別</label>
                <select v-model="user.gender">
                  <option value="MALE">男</option>
                  <option value="FEMALE">女</option>
                </select>
              </div>
              <div class="form-field">
                <label>生日</label>
                <input v-model="user.birthday" type="date" />
              </div>
            </div>
            <div class="form-field">
              <label>信用卡號（僅練習用途）</label>
              <input v-model="user.creditCard" placeholder="可留空" />
            </div>

            <div class="field-note">
              <span>加入時間</span>
              <b>{{ formatDate(user.createdAt) }}</b>
            </div>

            <div class="panel-actions">
              <button class="btn btn-primary" :disabled="saving" @click="saveProfile">
                {{ saving ? '儲存中...' : '儲存變更' }}
              </button>
            </div>
          </div>
        </section>

        <!-- 變更密碼 -->
        <section class="panel">
          <div class="panel-head">
            <h2>變更密碼</h2>
          </div>
          <div class="panel-body">
            <div v-if="pwMsg" class="alert alert-success">{{ pwMsg }}</div>
            <div v-if="pwError" class="alert alert-error">{{ pwError }}</div>

            <div class="form-field">
              <label>目前密碼</label>
              <input v-model="pw.currentPassword" type="password" placeholder="請輸入目前密碼" />
            </div>
            <div class="form-field">
              <label>新密碼</label>
              <input v-model="pw.newPassword" type="password" placeholder="請輸入新密碼" />
            </div>
            <div class="form-field">
              <label>確認新密碼</label>
              <input v-model="pw.confirm" type="password" placeholder="再次輸入新密碼" @keyup.enter="savePassword" />
            </div>

            <div class="panel-actions">
              <button class="btn btn-primary" :disabled="savingPw" @click="savePassword">
                {{ savingPw ? '更新中...' : '更新密碼' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container-wide {
  max-width: 1040px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}
.account-page {
  min-height: 60vh;
}
.account-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 20px;
  align-items: start;
}
.panel {
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
}
.panel-head {
  padding: 14px 20px;
  border-bottom: 1px solid var(--line);
  background: #faf9f7;
}
.panel-head h2 {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.08em;
}
.panel-body {
  padding: 20px;
}
.readonly {
  background: #f5f3f0 !important;
  color: var(--muted);
}
.field-note {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--muted);
  padding: 14px 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  margin-bottom: 18px;
}
.field-note b {
  color: var(--ink);
}
.panel-actions {
  margin-top: 18px;
}
.panel-actions .btn {
  width: 100%;
  letter-spacing: 0.12em;
}

@media (max-width: 820px) {
  .container-wide {
    padding: 24px 16px 56px;
  }
  .account-grid {
    grid-template-columns: 1fr;
  }
}
</style>