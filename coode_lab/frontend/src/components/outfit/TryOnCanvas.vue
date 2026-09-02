<script setup>
import { reactive } from 'vue'
import { SLOT_LABELS, lookTotal } from '../../api/outfitService'
import { formatMoney } from '../../utils/format'

const props = defineProps({
  look: { type: Object, required: true },
  scale: { type: Number, default: 1 },
})

defineEmits(['clear', 'randomize', 'zoom-in', 'zoom-out', 'reset-zoom'])

const BASE_W = 340
const BASE_H = 430

// Canvas 圖層順序（由下而上）
const Z = { BOTTOM: 10, SHOES: 20, TOP: 20, OUTER: 30, ACCESSORY: 40 }

// 圖片讀取失敗時改用佔位，避免 broken image
const broken = reactive({})

function outfitSrc(slot, product) {
  if (!product || !product.outfitPng) return null
  if (broken[`${slot}:${product.productId}`]) return null
  return product.outfitPng
}

function onImgError(slot, product) {
  broken[`${slot}:${product.productId}`] = true
}

const stageWidth = () => BASE_W * props.scale
const stageHeight = () => BASE_H * props.scale
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
        <div v-if="look.ACCESSORY" class="c-slot slot-accessory" :style="{ zIndex: Z.ACCESSORY }">
          <img :src="outfitSrc('ACCESSORY', look.ACCESSORY)" alt="配件" @error="onImgError('ACCESSORY', look.ACCESSORY)" />
          <div v-if="!outfitSrc('ACCESSORY', look.ACCESSORY)" class="no-png">暫不支援試穿</div>
        </div>
        <div v-else class="c-slot slot-accessory empty-slot" :style="{ zIndex: Z.ACCESSORY }">
          {{ SLOT_LABELS.ACCESSORY }}
        </div>

        <div v-if="look.OUTER" class="c-slot slot-outer" :style="{ zIndex: Z.OUTER }">
          <img :src="outfitSrc('OUTER', look.OUTER)" alt="外套" @error="onImgError('OUTER', look.OUTER)" />
          <div v-if="!outfitSrc('OUTER', look.OUTER)" class="no-png">暫不支援試穿</div>
        </div>
        <div v-else class="c-slot slot-outer empty-slot" :style="{ zIndex: Z.OUTER }">
          {{ SLOT_LABELS.OUTER }}
        </div>

        <div v-if="look.TOP" class="c-slot slot-top" :style="{ zIndex: Z.TOP }">
          <img :src="outfitSrc('TOP', look.TOP)" alt="上衣" @error="onImgError('TOP', look.TOP)" />
          <div v-if="!outfitSrc('TOP', look.TOP)" class="no-png">暫不支援試穿</div>
        </div>
        <div v-else class="c-slot slot-top empty-slot" :style="{ zIndex: Z.TOP }">
          {{ SLOT_LABELS.TOP }}
        </div>

        <div v-if="look.BOTTOM" class="c-slot slot-bottom" :style="{ zIndex: Z.BOTTOM }">
          <img :src="outfitSrc('BOTTOM', look.BOTTOM)" alt="下身" @error="onImgError('BOTTOM', look.BOTTOM)" />
          <div v-if="!outfitSrc('BOTTOM', look.BOTTOM)" class="no-png">暫不支援試穿</div>
        </div>
        <div v-else class="c-slot slot-bottom empty-slot" :style="{ zIndex: Z.BOTTOM }">
          {{ SLOT_LABELS.BOTTOM }}
        </div>

        <div v-if="look.SHOES" class="c-slot slot-shoes" :style="{ zIndex: Z.SHOES }">
          <img :src="outfitSrc('SHOES', look.SHOES)" alt="鞋子" @error="onImgError('SHOES', look.SHOES)" />
          <div v-if="!outfitSrc('SHOES', look.SHOES)" class="no-png">暫不支援試穿</div>
        </div>
        <div v-else class="c-slot slot-shoes empty-slot" :style="{ zIndex: Z.SHOES }">
          {{ SLOT_LABELS.SHOES }}
        </div>
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
}
.total {
  margin-top: 2px;
  font-size: 13px;
  color: var(--c-text-light);
}
.total b {
  color: #ec4899;
  font-size: 16px;
}
.actions {
  display: flex;
  gap: 8px;
}
.stage {
  align-self: center;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
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
.empty-slot {
  border: 1px dashed #e3e3e3;
  border-radius: 10px;
  color: #c2bebe;
  font-size: 12px;
  background: #f7f7f7;
}
.no-png {
  color: #c2bebe;
  font-size: 11px;
}
.slot-accessory {
  top: 26px;
  right: 4px;
  width: 74px;
  height: 92px;
}
.slot-outer {
  top: 14px;
  left: 96px;
  width: 172px;
  height: 244px;
}
.slot-top {
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
}
.slot-shoes {
  bottom: 2px;
  left: 106px;
  width: 128px;
  height: 72px;
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
  color: var(--c-text-light);
}
</style>