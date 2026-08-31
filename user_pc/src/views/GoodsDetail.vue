<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail, getGoodsReviews, addToCart, favoriteStatus, addFavorite, removeFavorite, recordBrowse, getReviewTarget, publishReview } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goods = ref(null)
const skuList = ref([])
const selectedSku = ref(null)
const quantity = ref(1)
const reviewList = ref([])
const totalReviews = ref(0)
const fav = ref(false)
const popMsg = ref('')
const showReview = ref(false)
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewTarget = ref(null)

const soldOut = () => {
  if (!goods.value) return false
  const st = goods.value.stock
  return st == null || Number(st) === 0
}

onMounted(async () => {
  const id = route.params.id
  goods.value = await getGoodsDetail(id)
  skuList.value = goods.value.skuList || []
  if (skuList.value.length) selectedSku.value = skuList.value[0]
  loadReviews()
  if (userStore.isLogin) {
    fav.value = await favoriteStatus(id)
    await recordBrowse(id)
  }
})

async function loadReviews() {
  const res = await getGoodsReviews(route.params.id, { page: 1, size: 5 })
  reviewList.value = res.list
  totalReviews.value = res.total
}

function toggleFav() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (fav.value) {
    removeFavorite(goods.value.id)
    fav.value = false
  } else {
    addFavorite(goods.value.id)
    fav.value = true
  }
}

function changeQty(delta) {
  quantity.value = Math.max(1, Math.min(99, quantity.value + delta))
}

async function handleAddCart() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  await addToCart({ skuId: selectedSku.value.id, quantity: quantity.value })
  popMsg.value = '已加入购物车'
  setTimeout(() => popMsg.value = '', 1500)
}

function buyNow() {
  handleAddCart()
  setTimeout(() => router.push('/cart'), 300)
}

function goShop() {
  if (goods.value && goods.value.shopId) {
    router.push(`/shop/${goods.value.shopId}`)
  }
}

function showImg(url) {
  window.open(url, '_blank')
}

async function openGoodsReview() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const target = await getReviewTarget(goods.value.id)
  if (!target || !target.canReview) {
    popMsg.value = target && target.alreadyReviewed ? '您已评价过该商品' : '请先完成该商品的订单后再评价'
    setTimeout(() => popMsg.value = '', 2000)
    return
  }
  reviewTarget.value = target
  reviewRating.value = 5
  reviewContent.value = ''
  showReview.value = true
}

function closeReview() {
  showReview.value = false
}

async function submitGoodsReview() {
  if (!reviewContent.value.trim()) {
    popMsg.value = '请填写评价内容'
    setTimeout(() => popMsg.value = '', 2000)
    return
  }
  try {
    await publishReview({
      orderId: reviewTarget.value.orderId,
      orderItemId: reviewTarget.value.orderItemId,
      rating: reviewRating.value,
      content: reviewContent.value,
      images: ''
    })
    showReview.value = false
    popMsg.value = '评价成功'
    setTimeout(() => popMsg.value = '', 2000)
    loadReviews()
  } catch (e) {
    popMsg.value = e.message
    setTimeout(() => popMsg.value = '', 2000)
  }
}
</script>

<template>
  <div v-if="goods" class="detail-layout">
    <div class="card gallery">
      <img :src="goods.mainImage" class="main-img" />
    </div>

    <div class="card info">
      <h1 class="title">{{ goods.name }}</h1>
      <p class="subtitle" v-if="goods.subtitle">{{ goods.subtitle }}</p>
      <div class="price-box">
        <span class="price" style="font-size:28px;">¥{{ selectedSku ? selectedSku.price : goods.price }}</span>
        <span style="margin-left:16px;color:#999;font-size:13px;">已售 {{ goods.sales }} 件</span>
        <span v-if="soldOut()" style="margin-left:16px;color:#ff5000;font-size:13px;">已售罄</span>
        <span v-else style="margin-left:16px;color:#999;font-size:13px;">库存 {{ goods.stock }} 件</span>
      </div>

      <div class="sku-block" v-if="skuList.length">
        <div class="sku-title">选择规格：</div>
        <div class="sku-list">
          <div v-for="s in skuList" :key="s.id" class="sku-item"
               :class="{ on: selectedSku && selectedSku.id === s.id }"
               @click="selectedSku = s">
            {{ s.spec }}（¥{{ s.price }}）
          </div>
        </div>
      </div>

      <div class="qty-row">
        <span>数量：</span>
        <button @click="changeQty(-1)">-</button>
        <span class="qty-num">{{ quantity }}</span>
        <button @click="changeQty(1)">+</button>
      </div>

      <div class="actions">
        <button class="btn-primary" :class="{ disabled: soldOut() }" :disabled="soldOut()" @click="handleAddCart">加入购物车</button>
        <button class="btn-primary outline" :class="{ disabled: soldOut() }" :disabled="soldOut()" @click="buyNow">立即购买</button>
        <button class="btn-plain" @click="toggleFav">{{ fav ? '♥ 已收藏' : '♡ 收藏' }}</button>
      </div>
      <div style="margin-top:12px;color:#999;font-size:12px;">
        店铺：<span class="shop-link" @click="goShop">{{ goods.shopName || '官方店' }} ›</span>
      </div>
    </div>

    <div class="card reviews" style="grid-column: span 2;">
      <div class="review-head">
        <h3 class="section-title">商品评价（{{ totalReviews }}）</h3>
        <button class="btn-plain write-review" @click="openGoodsReview">写评价</button>
      </div>
      <div v-for="r in reviewList" :key="r.id" class="review-item">
        <div>
          <span class="review-nick">{{ r.nickname || '匿名用户' }}</span>
          <span class="review-star">{{ '★'.repeat(r.rating) }}</span>
          <span class="review-time">{{ r.createTime }}</span>
        </div>
        <p style="margin-top:6px;white-space:pre-line;">{{ r.content }}</p>
        <div class="review-imgs" v-if="r.images">
          <img v-for="u in r.images.split(',')" :key="u" :src="u" class="review-img" @click="showImg(u)" />
        </div>
        <p v-if="r.reply" style="color:#999;font-size:13px;">商家回复：{{ r.reply }}</p>
      </div>
      <div v-if="!reviewList.length" style="color:#999;padding:20px;">暂无评价</div>
    </div>

    <div class="pop-msg" v-if="popMsg">{{ popMsg }}</div>
  </div>

  <div class="modal" v-if="showReview">
    <div class="modal-body">
      <h3>评价商品：{{ goods.name }}</h3>
      <div style="margin:12px 0;">
        <span>评分：</span>
        <button v-for="i in 5" :key="i" class="star" :class="{ on: reviewRating >= i }" @click="reviewRating = i">★</button>
      </div>
      <textarea v-model="reviewContent" class="input" placeholder="分享您的使用体验（必填）" style="min-height:80px;"></textarea>
      <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
        <button class="btn-plain" @click="closeReview">取消</button>
        <button class="btn-primary" @click="submitGoodsReview">提交评价</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.main-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  object-fit: cover;
}
.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.thumb {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
}
.title {
  font-size: 20px;
}
.subtitle {
  color: #999;
  margin: 8px 0;
}
.price-box {
  background: #fff7f3;
  padding: 12px;
  border-radius: 8px;
  margin: 12px 0;
}
.sku-title {
  font-size: 14px;
  margin-bottom: 8px;
}
.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.sku-item {
  padding: 8px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}
.sku-item.on {
  border-color: #ff5000;
  color: #ff5000;
  background: #fff7f3;
}
.qty-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 16px 0;
}
.qty-row button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  font-size: 16px;
}
.qty-num {
  font-size: 16px;
  min-width: 30px;
  text-align: center;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
.actions .outline {
  background: #ff591d;
}
.actions .disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.shop-link {
  color: #ff5000;
  cursor: pointer;
}
.shop-link:hover {
  text-decoration: underline;
}
.pop-msg {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 10px 20px;
  border-radius: 6px;
  z-index: 99;
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.review-nick {
  font-weight: 600;
}
.review-star {
  color: #ff8000;
  margin: 0 8px;
}
.review-time {
  color: #999;
  font-size: 12px;
}
.review-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.review-img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  cursor: zoom-in;
}
.review-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.write-review {
  padding: 5px 14px;
  border: 1px solid #ff5000;
  color: #ff5000;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.write-review:hover {
  background: #fff7f3;
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal-body {
  background: #fff;
  padding: 24px;
  border-radius: 10px;
  width: 420px;
  max-width: 90vw;
}
.star {
  background: none;
  font-size: 22px;
  color: #ddd;
}
.star.on {
  color: #ff8000;
}
.input {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 10px;
  font-size: 14px;
  box-sizing: border-box;
  resize: vertical;
}
</style>