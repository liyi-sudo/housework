<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getShop, getGoodsList } from '../api'
import GoodsCard from '../components/GoodsCard.vue'

const route = useRoute()
const shop = ref(null)
const goodsList = ref([])
const total = ref(0)
const page = ref(1)
const size = 12
const loadError = ref('')

onMounted(async () => {
  try {
    shop.value = await getShop(route.params.id)
    await load()
  } catch (e) {
    loadError.value = e.message || '店铺不存在'
  }
})

async function load() {
  const res = await getGoodsList({ shopId: shop.value.id, page: page.value, size })
  goodsList.value = res.list
  total.value = res.total
}
</script>

<template>
  <div>
    <div v-if="loadError" style="background:#fff3f0;border:1px solid #ffd4c8;color:#ff4d4f;border-radius:8px;padding:16px;">
      {{ loadError }}
    </div>

    <div v-else-if="shop" class="shop-head card page-section">
      <img v-if="shop.logo" :src="shop.logo" class="shop-logo" />
      <div>
        <h2 class="shop-name">{{ shop.name }}</h2>
        <p v-if="shop.intro" class="shop-intro">{{ shop.intro }}</p>
        <div class="shop-goods-count">在售商品 {{ total }} 件</div>
      </div>
    </div>

    <div class="page-section" v-if="shop">
      <h3 class="section-title">店铺商品</h3>
      <div class="goods-grid">
        <GoodsCard v-for="g in goodsList" :key="g.id" :item="g" />
      </div>
      <div v-if="!goodsList.length" style="text-align:center;color:#999;padding:40px;">该店铺暂无在售商品</div>
    </div>
  </div>
</template>

<style scoped>
.shop-head {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 22px;
}
.shop-logo {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
  background: #f5f5f5;
}
.shop-name {
  font-size: 22px;
}
.shop-intro {
  color: #999;
  font-size: 13px;
  margin: 8px 0;
}
.shop-goods-count {
  font-size: 12px;
  color: #ff5000;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
</style>
