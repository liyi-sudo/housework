<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getGoodsList } from '../api'
import GoodsCard from '../components/GoodsCard.vue'

const route = useRoute()
const categories = ref([])
const goodsList = ref([])
const total = ref(0)
const activeCat = ref(route.query.cid ? Number(route.query.cid) : null)
const page = ref(1)
const size = 8

const current = computed(() =>
  categories.value.find(c => c.id === activeCat.value)
)

async function load() {
  categories.value = (await getCategories()).filter(c => c.parentId === 0)
  if (!activeCat.value && categories.value.length) {
    activeCat.value = categories.value[0].id
  }
}

async function loadGoods() {
  const res = await getGoodsList({ categoryId: activeCat.value, page: page.value, size })
  goodsList.value = res.list
  total.value = res.total
}

function selectCat(id) {
  activeCat.value = id
  page.value = 1
  loadGoods()
}

watch(() => route.query.cid, v => {
  if (v) {
    activeCat.value = Number(v)
    loadGoods()
  }
})

onMounted(async () => {
  await load()
  await loadGoods()
})
</script>

<template>
  <div class="category-layout card">
    <aside class="side">
      <div v-for="p in categories" :key="p.id" class="parent"
           :class="{ on: activeCat === p.id }" @click="selectCat(p.id)">
        {{ p.name }}
      </div>
    </aside>
    <div class="main">
      <h3 class="section-title">{{ (current && current.name) || '全部' }} · 商品列表（{{ total }} 件）</h3>
      <div class="goods-grid">
        <GoodsCard v-for="g in goodsList" :key="g.id" :item="g" />
      </div>
      <div v-if="!goodsList.length" style="text-align:center;color:#999;padding:60px;">该分类暂无商品</div>
      <div class="pager" v-if="total > size">
        <button class="btn-plain" :disabled="page <= 1" @click="page--; loadGoods()">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / size) }}</span>
        <button class="btn-plain" :disabled="page >= Math.ceil(total / size)" @click="page++; loadGoods()">下一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-layout {
  display: flex;
  min-height: 600px;
}
.side {
  width: 160px;
  border-right: 1px solid #f0f0f0;
}
.parent {
  padding: 14px 20px;
  cursor: pointer;
  font-size: 14px;
}
.parent:hover {
  color: #ff5000;
}
.parent.on {
  color: #ff5000;
  font-weight: 700;
  background: #fff7f3;
  border-right: 3px solid #ff5000;
}
.main {
  flex: 1;
  padding: 16px 20px;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}
</style>