<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 5 }
})
const emit = defineEmits(['update:modelValue'])

// 课程项目无文件服务器，采用外部图片服务模拟"晒图上传"
const pool = [
  'https://loremflickr.com/400/400/product?lock=901',
  'https://loremflickr.com/400/400/product?lock=902',
  'https://loremflickr.com/400/400/product?lock=903',
  'https://loremflickr.com/400/400/product?lock=904',
  'https://loremflickr.com/400/400/product?lock=905',
  'https://loremflickr.com/400/400/product?lock=906',
  'https://loremflickr.com/400/400/product?lock=907',
  'https://loremflickr.com/400/400/product?lock=908'
]

const selected = ref([...props.modelValue])
watch(() => props.modelValue, v => { selected.value = [...v] })

function toggle(url) {
  const i = selected.value.indexOf(url)
  if (i >= 0) {
    selected.value.splice(i, 1)
  } else if (selected.value.length < props.max) {
    selected.value.push(url)
  }
  emit('update:modelValue', [...selected.value])
}

function removeUrl(url) {
  selected.value = selected.value.filter(u => u !== url)
  emit('update:modelValue', [...selected.value])
}
</script>

<template>
  <div class="review-images">
    <div class="hint">晒图（最多 {{ max }} 张，点击选择；模拟上传，不接入真实文件服务器）</div>
    <div class="pool">
      <div v-for="u in pool" :key="u" class="pool-item" :class="{ on: selected.includes(u) }" @click="toggle(u)">
        <img :src="u" alt="可选图" />
        <span class="check" v-if="selected.includes(u)">✓</span>
      </div>
    </div>
    <div class="picked" v-if="selected.length">
      <span class="picked-tip">已选 {{ selected.length }} 张：</span>
      <div v-for="u in selected" :key="u" class="picked-item">
        <img :src="u" alt="已选图" />
        <span class="x" @click="removeUrl(u)">×</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hint {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.pool {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 10px;
}
.pool-item {
  position: relative;
  border: 2px solid #eee;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 1;
}
.pool-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.pool-item.on {
  border-color: #ff5000;
}
.pool-item .check {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: #ff5000;
  color: #fff;
  font-size: 12px;
  border-radius: 50%;
}
.picked {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.picked-tip {
  font-size: 12px;
  color: #666;
}
.picked-item {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #ddd;
}
.picked-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.picked-item .x {
  position: absolute;
  top: 0;
  right: 0;
  width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 11px;
  border-radius: 0 0 0 6px;
  cursor: pointer;
}
</style>
