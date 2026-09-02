<script setup>
import { ref } from 'vue'

defineProps({
  outfits: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})
const emit = defineEmits(['load-outfit', 'delete-outfit', 'rename-outfit', 'new-outfit'])

const scrollEl = ref(null)
const menuOpen = ref(null)

function scroll(direction) {
  const el = scrollEl.value
  if (!el) return
  el.scrollBy({ left: direction * 260, behavior: 'smooth' })
}
</script>

<template>
  <section class="saved">
    <header class="saved-head">
      <h3>穿搭收藏</h3>
      <div class="nav-arrows">
        <button class="arrow" @click="scroll(-1)">←</button>
        <button class="arrow" @click="scroll(1)">→</button>
      </div>
    </header>

    <div v-if="loading" class="empty small muted">載入中...</div>
    <div v-else-if="outfits.length === 0" class="empty">
      <p class="muted">你還沒有儲存任何穿搭</p>
      <button class="btn btn-primary-outline" @click="emit('new-outfit')">＋ 建立第一套穿搭</button>
    </div>

    <div v-else ref="scrollEl" class="track">
      <div
        v-for="outfit in outfits"
        :key="outfit.outfitId"
        class="save-card"
        @click="emit('load-outfit', outfit)"
      >
        <div class="mini-preview">
          <div v-if="outfit.mini && outfit.mini.length">
            <div v-for="m in outfit.mini" :key="m.slot" class="mini-slot">
              <img v-if="m.png" :src="m.png" alt="" @error="m.png = null" />
              <span v-else>{{ m.label }}</span>
            </div>
          </div>
          <span v-else class="no-items">尚未放入商品</span>
        </div>
        <div class="card-foot">
          <div class="foot-name">
            <div class="name">{{ outfit.name }}</div>
          </div>
          <div class="dots" @click.stop>
            <button class="dot-btn" @click="menuOpen = menuOpen === outfit.outfitId ? null : outfit.outfitId">
              ⋯
            </button>
            <div v-if="menuOpen === outfit.outfitId" class="menu">
              <button @click.stop="emit('rename-outfit', outfit)">重新命名</button>
              <button class="danger" @click.stop="emit('delete-outfit', outfit)">刪除穿搭</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 新增穿搭 -->
      <div class="save-card new-card" @click="emit('new-outfit')">
        <div class="plus">＋</div>
        <div class="new-label">新增穿搭</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.saved-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.saved-head h3 {
  font-size: 17px;
}
.nav-arrows {
  display: flex;
  gap: 8px;
}
.arrow {
  width: 30px;
  height: 30px;
  border: 1px solid var(--line);
  border-radius: 50%;
  background: var(--paper);
  color: var(--ink);
}
.arrow:hover {
  border-color: var(--ink);
}
.track {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  padding-bottom: 6px;
}
.track::-webkit-scrollbar {
  height: 6px;
}
.track::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 999px;
}
.save-card {
  scroll-snap-align: start;
  min-width: 200px;
  width: 200px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--paper);
  padding: 10px;
  cursor: pointer;
  transition: 0.15s ease;
}
.save-card:hover {
  border-color: var(--ink);
  box-shadow: var(--shadow-hair);
}
.mini-preview {
  height: 150px;
  border-radius: 8px;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.mini-slot img {
  height: 120px;
  object-fit: contain;
}
.mini-slot span {
  font-size: 11px;
  color: #c2bebe;
}
.no-items {
  font-size: 12px;
  color: #c2bebe;
}
.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.name {
  font-size: 13px;
  font-weight: 600;
}
.dots {
  position: relative;
}
.dot-btn {
  border: none;
  background: none;
  font-size: 16px;
  color: var(--muted);
}
.menu {
  position: absolute;
  right: 0;
  bottom: 22px;
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 4px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  min-width: 110px;
  z-index: 10;
  overflow: hidden;
}
.menu button {
  display: block;
  width: 100%;
  border: none;
  background: none;
  text-align: left;
  padding: 8px 12px;
  font-size: 13px;
}
.menu button:hover {
  background: #f4f4f2;
}
.menu button.danger {
  color: var(--accent);
}
.new-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-style: dashed;
  color: var(--muted);
}
.new-card:hover {
  border-color: var(--ink);
  color: var(--ink);
}
.plus {
  font-size: 30px;
  line-height: 1;
}
.new-label {
  font-size: 13px;
  margin-top: 6px;
}
.btn-primary-outline {
  margin-top: 12px;
  border: 1px solid var(--ink);
  color: var(--ink);
  background: var(--paper);
}
.btn-primary-outline:hover {
  background: var(--ink);
  color: var(--paper);
}
</style>