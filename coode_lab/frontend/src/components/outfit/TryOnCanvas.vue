<script setup>
import { reactive } from 'vue'
import { SLOT_LABELS, lookTotal, chosenVariantOf } from '../../api/outfitService'
import { formatMoney } from '../../utils/format'

const props = defineProps({
  look: { type: Object, required: true },
  scale: { type: Number, default: 1 },
})

defineEmits(['clear', 'randomize', 'zoom-in', 'zoom-out', 'reset-zoom'])

const BASE_W = 340
const BASE_H = 430

// Canvas 圖層順序（由下而上）
const Z = { BOTTOM: 10, UPPER_BODY: 20, FULL_BODY: 30, HEADWEAR: 40 }

// 圖片讀取失敗時改用佔位，避免 broken image
const broken = reactive({})

function outfitSrc(slot, product) {
  if (!product || product.unavailable) return null
  const variant = chosenVariantOf(product)
  if (broken[`${slot}:${product.productId}`]) return null
  // 依所選規格顯示對應試穿圖；無規格圖則退回商品層級圖
  return variant && variant.outfitPng ? variant.outfitPng : (product.outfitPng || null)
}

function onImgError(slot, product) {
  broken[`${slot}:${product.productId}`] = true
}

const stageWidth = () => BASE_W * props.scale
const stageHeight = () => BASE_H * props.scale

// 丹寧牛仔短裙（id 12）：改成較小比例並下移，使腰線接在上衣下方
function bottomStyle() {
  const p = props.look.BOTTOM
  if (p && p.productId === 12) {
    return { transform: 'translateY(26px) scale(0.9)', transformOrigin: 'center top' }
  }
  return {}
}
</script>

<template>
  <section class="canvas-card card">
    <header class="canvas-head">
      <div>
        <h2>目前穿搭</h2>
        <div class="total">總價：<b>{{ formatMoney(lookTotal(look)) }}</b></div>
      </div>
      <div class="actions">
        <button class="btn btn-sm" @click="$emit('clear')">清空穿搭</button>
        <button class="btn btn-sm" @click="$emit('randomize')">隨機搭配</button>
      </div>
    </header>

    <div class="stage" :style="{ width: stageWidth() + 'px', height: stageHeight() + 'px' }">
      <div class="canvas" :style="{ transform: `scale(${scale})` }">
        <!-- HEADWEAR（帽子/頭飾） -->
        <div v-if="look.HEADWEAR" class="c-slot slot-headwear" :style="{ zIndex: Z.HEADWEAR }">
          <img v-if="!look.HEADWEAR.unavailable" :src="outfitSrc('HEADWEAR', look.HEADWEAR)" alt="帽子/頭飾" @error="onImgError('HEADWEAR', look.HEADWEAR)" />
          <div v-if="look.HEADWEAR.unavailable" class="no-png unavail">商品未上架</div>
          <div v-else-if="!outfitSrc('HEADWEAR', look.HEADWEAR)" class="no-png">{{ SLOT_LABELS.HEADWEAR }}</div>
        </div>

        <!-- FULL_BODY（洋裝）：佔據上半身 + 下半身 -->
        <div v-if="look.FULL_BODY" class="c-slot slot-full-body" :style="{ zIndex: Z.FULL_BODY }">
          <img v-if="!look.FULL_BODY.unavailable" :src="outfitSrc('FULL_BODY', look.FULL_BODY)" alt="洋裝" @error="onImgError('FULL_BODY', look.FULL_BODY)" />
          <div v-if="look.FULL_BODY.unavailable" class="no-png unavail">商品未上架</div>
          <div v-else-if="!outfitSrc('FULL_BODY', look.FULL_BODY)" class="no-png">暫不支援試穿</div>
        </div>

        <!-- UPPER_BODY（上衣/外套）：FULL_BODY 存在時隱藏 -->
        <template v-if="!look.FULL_BODY">
          <div v-if="look.UPPER_BODY" class="c-slot slot-upper" :style="{ zIndex: Z.UPPER_BODY }">
            <img v-if="!look.UPPER_BODY.unavailable" :src="outfitSrc('UPPER_BODY', look.UPPER_BODY)" alt="上衣/外套" @error="onImgError('UPPER_BODY', look.UPPER_BODY)" />
            <div v-if="look.UPPER_BODY.unavailable" class="no-png unavail">商品未上架</div>
            <div v-else-if="!outfitSrc('UPPER_BODY', look.UPPER_BODY)" class="no-png">暫不支援試穿</div>
          </div>

          <!-- BOTTOM（下身）：FULL_BODY 存在時隱藏 -->
          <div v-if="look.BOTTOM" class="c-slot slot-bottom" :style="[{ zIndex: Z.BOTTOM }, bottomStyle()]">
            <img v-if="!look.BOTTOM.unavailable" :src="outfitSrc('BOTTOM', look.BOTTOM)" alt="下身" @error="onImgError('BOTTOM', look.BOTTOM)" />
            <div v-if="look.BOTTOM.unavailable" class="no-png unavail">商品未上架</div>
            <div v-else-if="!outfitSrc('BOTTOM', look.BOTTOM)" class="no-png">暫不支援試穿</div>
          </div>
        </template>
      </div>
    </div>

    <div class="zoom">
      <button class="btn btn-sm" @click="$emit('zoom-out')">－</button>
      <span class="zoom-value">{{ Math.round(scale * 100) }}%</span>
      <button class="btn btn-sm" @click="$emit('zoom-in')">＋</button>
      <button class="btn btn-sm" @click="$emit('reset-zoom')">Reset</button>
    </div>
  </section>
</template>

<style scoped>
.canvas-card {
  background: #fff;
  display: flex;
  flex-direction: column;
}
.canvas-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}
.canvas-head h2 {
  font-size: 18px;
  letter-spacing: 0.04em;
}
.total {
  margin-top: 2px;
  font-size: 13px;
  color: var(--muted);
}
.total b {
  color: var(--ink);
  font-size: 16px;
}
.actions {
  display: flex;
  gap: 8px;
}
.stage {
  align-self: center;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fbfbfb;
  position: relative;
  touch-action: pan-y;
}
.canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 340px;
  height: 430px;
  transform-origin: top left;
}
.c-slot {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
}
.c-slot img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  pointer-events: none;
}
.no-png {
  color: #b8b4af;
  font-size: 11px;
}
.no-png.unavail {
  color: var(--accent);
  font-weight: 700;
  font-size: 12px;
  border: 1px solid var(--accent);
  border-radius: 3px;
  padding: 4px 8px;
  background: #fff;
}
.slot-headwear {
  top: 4px;
  left: 122px;
  width: 96px;
  height: 60px;
}
.slot-full-body {
  top: 60px;
  left: 96px;
  width: 168px;
  height: 330px;
}
.slot-upper {
  top: 66px;
  left: 108px;
  width: 132px;
  height: 162px;
}
.slot-bottom {
  top: 182px;
  left: 92px;
  width: 160px;
  height: 182px;
  transform: scale(1.25);
  transform-origin: center top;
}
.zoom {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.zoom-value {
  min-width: 48px;
  text-align: center;
  font-size: 13px;
  color: var(--muted);
}
</style>