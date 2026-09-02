<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  currentName: { type: String, default: '' },
})
const emit = defineEmits(['save', 'cancel'])

const name = ref('')

watch(
  () => props.open,
  (v) => {
    if (v) name.value = props.currentName || ''
  },
)
</script>

<template>
  <div v-if="open" class="modal-mask" @click.self="emit('cancel')">
    <div class="modal">
      <h3>儲存穿搭</h3>
      <div class="form-field">
        <label>穿搭名稱</label>
        <input
          v-model="name"
          type="text"
          placeholder="例如：清新休閒風"
          maxlength="100"
          @keyup.enter="name.trim() && emit('save', name.trim())"
        />
      </div>
      <div class="row">
        <button class="btn btn-primary" :disabled="!name.trim()" @click="emit('save', name.trim())">
          儲存
        </button>
        <button class="btn" @click="emit('cancel')">取消</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  width: 380px;
  max-width: 92vw;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}
.modal h3 {
  margin-bottom: 16px;
}
.row {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>