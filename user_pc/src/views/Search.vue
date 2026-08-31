<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getGoodsList } from '../api'
import GoodsCard from '../components/GoodsCard.vue'

const route = useRoute()
const keyword = ref('')
const sort = ref('default')
const minPrice = ref('')
const maxPrice = ref('')
const goodsList = ref([])
const total = ref(0)
const page = ref(1)
const size = 12

const sorts = [
  { key: 'default', label: '综合' },
  { key: 'sales', label: '销量' },
  { key: 'price_asc', label: '价格↑' },
  { key: 'price_desc', label: '价格↓' },
  { key: 'new', label: '新品' }
]

async function load() {
  const res = await getGoodsList({
    keyword: keyword.value,
    sort: sort.value,
    minPrice: minPrice.value || undefined,
    maxPrice: maxPrice.value || undefined,
    page: page.value,
    size
  })
  goodsList.value = res.list
  total.value = res.total
}

function applyPrice() {
  page.value = 1
  load()
}

function changeSort(s) {
  sort.value = s
  page.value = 1
  load()
}

watch(() => route.query.keyword, v => {
  keyword.value = v || ''
  page.value = 1
  load()
})

onMounted(() => {
  keyword.value = route.query.keyword || ''
  load()
})
</script>

<template>
  <div>
    <div class="card" style="display:flex;gap:16px;align-items:center;margin-bottom:16px;flex-wrap:wrap;">
      <span style="font-weight:700;">搜索结果：<span style="color:#ff5000;">{{ keyword || '全部' }}</span>（{{ total }} 件）</span>
      <div class="price-filter">
        <input v-model="minPrice" type="number" min="0" placeholder="最低价" class="price-input" @keyup.enter="applyPrice" />
        <span style="color:#999;">—</span>
        <input v-model="maxPrice" type="number" min="0" placeholder="最高价" class="price-input" @keyup.enter="applyPrice" />
        <button class="sort-btn on" @click="applyPrice">筛选</button>
      </div>
      <div style="display:flex;gap:10px;">
        <button v-for="s in sorts" :key="s.key" class="sort-btn" :class="{ on: sort === s.key }" @click="changeSort(s.key)">
          {{ s.label }}
        </button>
      </div>
    </div>

    <div class="goods-grid">
      <GoodsCard v-for="g in goodsList" :key="g.id" :item="g" />
    </div>
    <div v-if="!goodsList.length" style="text-align:center;color:#999;padding:60px;">没有找到相关商品</div>
  </div>
</template>

<style scoped>
.sort-btn {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
}
.sort-btn.on {
  border-color: #ff5000;
  color: #ff5000;
}
.price-filter {
  display: flex;
  align-items: center;
  gap: 6px;
}
.price-input {
  width: 88px;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
</style>