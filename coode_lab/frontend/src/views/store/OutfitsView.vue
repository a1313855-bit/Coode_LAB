<script setup>
import { ref, onMounted } from 'vue'
import { outfitApi, outfitItemApi, productApi } from '../../api'
import { slotLabel, categoryLabel } from '../../utils/format'
import AppPagination from '../../components/AppPagination.vue'
import { currentUserId } from '../../composables/auth'

const userId = ref(currentUserId())
const outfits = ref([])
const page = ref(0)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

// 建立穿搭
const showCreate = ref(false)
const newName = ref('')

// 加入商品到某一套穿搭
const targetOutfit = ref(null)
const showPicker = ref(false)
const availableProducts = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await outfitApi.byUser(userId.value, page.value)
    outfits.value = res.content || []
    page.value = res.page || 0
    totalPages.value = res.totalPages || 1
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  page.value = p
  load()
}

async function createOutfit() {
  error.value = ''
  try {
    await outfitApi.create({ userId: userId.value, name: newName.value })
    showCreate.value = false
    newName.value = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function deleteOutfit(o) {
  if (!confirm(`確定刪除穿搭「${o.name}」？`)) return
  try {
    await outfitApi.remove(o.outfitId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function openPicker(o) {
  targetOutfit.value = o
  showPicker.value = true
  error.value = ''
  try {
    const res = await productApi.available(0)
    availableProducts.value = res.content || []
  } catch (e) {
    availableProducts.value = []
  }
}

async function addToOutfit(p) {
  try {
    await outfitItemApi.add(targetOutfit.value.outfitId, p.productId)
    showPicker.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function clearOutfit(o) {
  if (!confirm(`清空穿搭「${o.name}」所有商品？`)) return
  try {
    await outfitItemApi.clear(o.outfitId)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function thumbs(o) {
  return (o.items || []).map((i) => i.slotType)
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-header flex-between">
      <div>
        <h1>我的穿搭</h1>
        <p>搭配並儲存你的專屬造型</p>
      </div>
      <div class="flex">
        <input v-model.number="userId" type="number" placeholder="會員 ID" class="uid" />
        <button class="btn btn-primary" @click="load">查詢</button>
        <button class="btn btn-success" @click="showCreate = true">+ 新增穿搭</button>
      </div>
    </div>

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading" class="empty">載入中...</div>
    <div v-else-if="outfits.length === 0" class="empty">還沒有穿搭，建立第一套吧！</div>

    <div v-else class="grid-3">
      <div v-for="o in outfits" :key="o.outfitId" class="card outfit">
        <div class="outfit-head">
          <b>{{ o.name }}</b>
          <div class="flex">
            <button class="btn btn-sm" @click="openPicker(o)">+ 加商品</button>
            <button class="btn btn-sm" @click="clearOutfit(o)">清空</button>
            <button class="btn btn-sm btn-danger" @click="deleteOutfit(o)">刪除</button>
          </div>
        </div>
        <div class="slots">
          <div v-if="!o.items || o.items.length === 0" class="muted small">尚未放入商品</div>
          <div v-for="it in o.items" :key="it.outfititemsId" class="slot">
            <span class="slot-tag">{{ slotLabel(it.slotType) }}</span>
            <span>商品 #{{ it.productId }}</span>
          </div>
        </div>
      </div>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="changePage" />

    <!-- 新增穿搭 -->
    <div v-if="showCreate" class="modal-mask">
      <div class="modal">
        <h3>新增穿搭</h3>
        <div class="form-field">
          <label>穿搭名稱</label>
          <input v-model="newName" placeholder="例如：夏日清爽" />
        </div>
        <div class="flex">
          <button class="btn btn-primary" @click="createOutfit">建立</button>
          <button class="btn" @click="showCreate = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 挑選商品 -->
    <div v-if="showPicker" class="modal-mask">
      <div class="modal wide">
        <h3>加入商品到「{{ targetOutfit.name }}」</h3>
        <div v-if="availableProducts.length === 0" class="muted">目前沒有可上架商品</div>
        <div class="picker-list">
          <div
            v-for="p in availableProducts"
            :key="p.productId"
            class="picker-item"
            @click="addToOutfit(p)"
          >
            <b>{{ p.name }}</b>
            <span class="muted small">{{ categoryLabel(p.categoryType) }}</span>
          </div>
        </div>
        <div class="flex">
          <button class="btn" @click="showPicker = false">關閉</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.uid {
  width: 90px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.outfit-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.slots {
  margin-top: 8px;
}
.slot {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-top: 1px solid var(--c-border);
  font-size: 14px;
}
.slot-tag {
  background: #eff6ff;
  color: var(--c-primary);
  border-radius: 6px;
  padding: 0 8px;
  font-size: 12px;
}
.small {
  font-size: 12px;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
  width: 420px;
  max-width: 90vw;
}
.modal.wide {
  width: 520px;
}
.modal h3 {
  margin-bottom: 16px;
}
.picker-list {
  max-height: 300px;
  overflow-y: auto;
}
.picker-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}
.picker-item:hover {
  border-color: var(--c-primary);
  background: #eff6ff;
}
</style>
