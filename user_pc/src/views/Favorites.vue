<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, removeFavorite } from '../api'

const router = useRouter()
const list = ref([])

onMounted(async () => {
  const res = await getFavorites({ page: 1, size: 50 })
  list.value = res.list
})

async function remove(f) {
  await removeFavorite(f.goodsId)
  // 后端按 goodsId 删除，前端同样按 goodsId 过滤，避免字段错位
  list.value = list.value.filter(x => x.goodsId !== f.goodsId)
}
</script>

<template>
  <div class="card">
    <h3 class="section-title">我的收藏（{{ list.length }}）</h3>
    <div class="grid">
      <div v-for="f in list" :key="f.id" class="item" @click="router.push(`/goods/${f.goodsId}`)">
        <img :src="f.mainImage" class="img" />
        <div class="name">{{ f.goodsName }}</div>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span class="price">¥{{ f.price }}</span>
          <a href="javascript:void(0)" style="color:#999;font-size:12px;" @click.stop="remove(f)">取消收藏</a>
        </div>
      </div>
    </div>
    <div v-if="!list.length" style="text-align:center;color:#999;padding:60px;">还没有收藏任何商品</div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
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
  padding: 8px 10px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>