<script setup>
import { computed, ref, watch } from 'vue'
import { categoryLabel, formatMoney } from '../../utils/format'
import { chosenVariantOf, variantsByColor, variantColors } from '../../api/outfitService'

const props = defineProps({
  product: { type: Object, required: true },
  isFavorite: { type: Boolean, default: false },
})
const emit = defineEmits(['try-on', 'favorite', 'detail'])

const imgFailed = ref(false)

const colors = computed(() => variantColors(props.product))

const currentVariant = computed(() => chosenVariantOf(props.product))
const currentColor = computed(() => (currentVariant.value && currentVariant.value.color) || null)

const thumbSrc = computed(() => {
  const v = currentVariant.value
  return (v && v.imagesJpg) || props.product.imagesJpg || ''
})

watch(thumbSrc, () => {
  imgFailed.value = false
})

function selectColor(color) {
  const vs = variantsByColor(props.product, color)
  if (!vs.length) return
  props.product.chosenVariant = vs.find((v) => v.status === 'ACTIVE') || vs[0]
}

function tryOn() {
  emit('try-on', props.product)
}
</script>

<template>
  <div class="card product-card">
    <div class="thumb" @click="emit('detail', product.productId)">
      <span v-if="!thumbSrc || imgFailed" class="thumb-placeholder">
        {{ categoryLabel(product.categoryType) }}
      </span>
      <img
        v-else
        :src="thumbSrc"
        :alt="product.name"
        loading="lazy"
        @error="imgFailed = true"
      />
      <button
        class="heart"
        :class="{ active: isFavorite }"
        :title="isFavorite ? '取消收藏' : '收藏'"
        @click.stop="emit('favorite', product.productId)"
      >
        {{ isFavorite ? '♥' : '♡' }}
      </button>
    </div>
    <div class="info">
      <div class="name" :title="product.name" @click="emit('detail', product.productId)">
        {{ product.name }}
      </div>
      <div v-if="colors.length > 0" class="colors">
        <button
          v-for="c in colors"
          :key="c"
          class="color-pill"
          :class="{ active: c === currentColor }"
          :title="c"
          @click.stop="selectColor(c)"
        >
          {{ c }}
        </button>
      </div>
      <div class="bottom">
        <span class="price">{{ formatMoney(product.price) }}</span>
        <button class="tryon" @click="tryOn">試穿</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  padding: 0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  align-self: start;
  min-width: 0;
}
.thumb {
  position: relative;
  width: 100%;
  height: 140px;
  min-height: 140px;
  flex-shrink: 0;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  border-radius: var(--radius) var(--radius) 0 0;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
}
.thumb-placeholder {
  font-size: 30px;
  font-weight: 700;
  color: #d7d3d3;
  letter-spacing: 2px;
}
.heart {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid var(--line);
  background: var(--paper);
  color: #aaa;
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.heart:hover {
  color: var(--accent);
  border-color: var(--accent);
}
.heart.active {
  color: var(--accent);
  border-color: var(--accent);
}
.info {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.name:hover {
  color: var(--ink);
}
.colors {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 6px;
}
.color-pill {
  border: 1px solid var(--line);
  background: var(--paper);
  color: var(--ink);
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
  line-height: 1.5;
  cursor: pointer;
  transition: all 0.15s ease;
}
.color-pill:hover {
  border-color: var(--ink);
}
.color-pill.active {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--paper);
  font-weight: 600;
}
.bottom {
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: nowrap;
}
.price {
  font-weight: 800;
  color: var(--ink);
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}
.tryon {
  flex-shrink: 0;
  white-space: nowrap;
  border: 1px solid var(--ink);
  background: var(--paper);
  color: var(--ink);
  border-radius: 999px;
  padding: 5px 16px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.15s ease;
}
.tryon:hover {
  background: var(--ink);
  color: var(--paper);
  border-color: var(--ink);
}
</style>