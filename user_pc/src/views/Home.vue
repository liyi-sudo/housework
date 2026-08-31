<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBanners, getNotices, getCategories, getGoodsList } from '../api'
import GoodsCard from '../components/GoodsCard.vue'

const router = useRouter()
const banners = ref([])
const notices = ref([])
const categories = ref([])
const goodsList = ref([])
const current = ref(0)
const noticeIdx = ref(0)
const loadError = ref('')
let timer = null

onMounted(async () => {
  try {
    const [b, n, c, g] = await Promise.all([
      getBanners(),
      getNotices(),
      getCategories(),
      getGoodsList({ page: 1, size: 8 })
    ])
    banners.value = b
    notices.value = n
    categories.value = c.filter(x => x.parentId === 0)
    goodsList.value = g.list
    timer = setInterval(() => {
      if (banners.value.length > 1) current.value = (current.value + 1) % banners.value.length
      if (notices.value.length > 1) noticeIdx.value = (noticeIdx.value + 1) % notices.value.length
    }, 4000)
  } catch (e) {
    loadError.value = `数据加载失败：${e.message}。`
  }
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div>
    <div v-if="loadError" style="background:#fff3f0;border:1px solid #ffd4c8;color:#ff4d4f;border-radius:8px;padding:16px;margin-bottom:20px;">
      {{ loadError }}
    </div>

    <div v-if="notices.length" class="notice card page-section" @click="noticeIdx = (noticeIdx + 1) % notices.length">
      <span class="notice-label">公告</span>
      <div class="notice-body">
        <Transition name="fade" mode="out-in">
          <div class="notice-content" :key="notices.length ? noticeIdx % notices.length : 0" v-if="notices.length && notices[noticeIdx % notices.length].content">{{ notices[noticeIdx % notices.length].content }}</div>
        </Transition>
      </div>
    </div>

    <div class="banner-wrap page-section" v-if="banners.length">
      <img v-for="(b, i) in banners" :key="b.id"
           :src="b.image" class="banner-item"
           :class="{ active: i === current }" @click="b.link && router.push(`/goods/${b.link}`)" />
      <div class="banner-dots">
        <span v-for="(b, i) in banners" :key="i" class="dot" :class="{ on: i === current }" @click="current = i"></span>
      </div>
    </div>

    <div class="cats card page-section">
      <div v-for="c in categories" :key="c.id" class="cat-item" @click="router.push({ path: '/category', query: { cid: c.id } })">
        <div class="cat-avatar">{{ c.name.slice(0, 1) }}</div>
        <div class="cat-name">{{ c.name }}</div>
      </div>
    </div>

    <div class="coupon-strip card page-section" @click="router.push('/coupons')">
      <span class="coupon-icon">¥</span>
      <span class="coupon-text">新人专享 · 领券下单更划算</span>
      <span class="coupon-btn">去领券 ›</span>
    </div>

    <div class="page-section">
      <h3 class="section-title">精选推荐</h3>
      <div class="goods-grid">
        <GoodsCard v-for="g in goodsList" :key="g.id" :item="g" />
      </div>
      <div v-if="!goodsList.length" style="text-align:center;color:#999;padding:40px;">暂无商品</div>
    </div>
  </div>
</template>

<style scoped>
.notice {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
}
.notice-label {
  flex-shrink: 0;
  background: #ff5000;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  align-self: flex-start;
  margin-top: 2px;
}
.notice-body {
  flex: 1;
  min-width: 0;
}
.notice-content {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
.banner-wrap {
  position: relative;
  overflow: hidden;
  height: 240px;
  border-radius: 8px;
}
.banner-item {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.6s;
  cursor: pointer;
}
.banner-item.active {
  opacity: 1;
}
.banner-dots {
  position: absolute;
  bottom: 12px;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 8px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
}
.dot.on {
  background: #ff5000;
}
.cats {
  display: flex;
  justify-content: space-around;
  padding: 18px;
}
.cat-item {
  text-align: center;
  cursor: pointer;
}
.cat-avatar {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #fff3ee;
  color: #ff5000;
  font-size: 22px;
  font-weight: 700;
  line-height: 54px;
  margin-bottom: 6px;
}
.cat-item:hover .cat-avatar {
  background: #ff5000;
  color: #fff;
}
.cat-name {
  font-size: 13px;
}
.coupon-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  cursor: pointer;
  background: linear-gradient(90deg, #fff7f3, #fff);
}
.coupon-strip:hover .coupon-btn {
  background: #ff5000;
  color: #fff;
}
.coupon-icon {
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 50%;
  background: #ff5000;
  color: #fff;
  font-weight: 700;
}
.coupon-text {
  font-size: 14px;
  font-weight: 600;
  color: #ff5000;
}
.coupon-btn {
  margin-left: auto;
  padding: 6px 16px;
  border: 1px solid #ff5000;
  color: #ff5000;
  border-radius: 16px;
  font-size: 13px;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
</style>