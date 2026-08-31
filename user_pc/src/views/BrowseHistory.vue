<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBrowseHistory, clearBrowseHistory } from '../api'

const router = useRouter()
const list = ref([])

onMounted(async () => {
  list.value = await getBrowseHistory()
})

async function clearAll() {
  if (!confirm('确定清空浏览记录？')) return
  await clearBrowseHistory()
  list.value = []
}
</script>

<template>
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;">
      <h3 class="section-title" style="margin:0;">浏览记录（{{ list.length }}）</h3>
      <button v-if="list.length" class="btn-plain" @click="clearAll">清空记录</button>
    </div>
    <div class="grid">
      <div v-for="h in list" :key="h.id" class="item" @click="router.push(`/goods/${h.goodsId}`)">
        <img :src="h.mainImage" class="img" />
        <div class="name">{{ h.goodsName }}</div>
        <div class="price">¥{{ h.price }}</div>
      </div>
    </div>
    <div v-if="!list.length" style="text-align:center;color:#999;padding:60px;">暂无浏览记录</div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-top: 14px;
}
.item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}
.name {
  font-size: 13px;
  padding: 8px 10px 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.price {
  padding: 0 10px 10px;
  font-size: 14px;
}
</style>