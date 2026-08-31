<script setup>
import { useRouter } from 'vue-router'

defineProps({
  item: { type: Object, required: true }
})

const router = useRouter()
</script>

<template>
  <div class="goods-card" @click="router.push(`/goods/${item.id}`)">
    <img :src="item.mainImage" :alt="item.name" class="goods-img" />
    <div class="goods-info">
      <div class="goods-name">{{ item.name }}</div>
      <div class="goods-sub" v-if="item.subtitle">{{ item.subtitle }}</div>
      <div class="goods-bottom">
        <span class="price">¥{{ item.price }}</span>
        <span class="sales" v-if="item.sales">已售 {{ item.sales }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.goods-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.goods-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}
.goods-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
}
.goods-info {
  padding: 10px;
}
.goods-name {
  font-size: 14px;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.goods-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.sales {
  font-size: 12px;
  color: #999;
}
</style>