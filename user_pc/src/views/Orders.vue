<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrders, cancelOrder, confirmOrder, addToCart } from '../api'

const route = useRoute()
const router = useRouter()
const statusTabs = [
  { label: '全部', value: null },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '售后', value: 5 }
]
const active = ref(route.query.status ? Number(route.query.status) : null)
const orders = ref([])
const page = ref(1)
const total = ref(0)

async function load() {
  const res = await getOrders({ status: active.value, page: page.value, size: 5 })
  orders.value = res.list
  total.value = res.total
}

function switchTab(v) {
  active.value = v
  page.value = 1
  load()
}

function goPay(id) {
  router.push(`/pay/${id}`)
}

async function doCancel(id) {
  if (!confirm('确定取消该订单？')) return
  await cancelOrder(id)
  load()
}

async function doConfirm(id) {
  if (!confirm('确认已收到货？')) return
  await confirmOrder(id)
  load()
}

async function reBuy(o) {
  for (const it of o.items || []) {
    await addToCart({ skuId: it.skuId, quantity: it.quantity })
  }
  router.push('/cart')
}

watch(() => route.query.status, v => {
  if (v !== undefined) {
    active.value = Number(v)
    load()
  }
})

onMounted(load)
</script>

<template>
  <div class="card">
    <h3 class="section-title">我的订单</h3>
    <div class="tabs">
      <button v-for="t in statusTabs" :key="String(t.value)" class="tab" :class="{ on: active === t.value }" @click="switchTab(t.value)">
        {{ t.label }}
      </button>
    </div>

    <div v-for="o in orders" :key="o.id" class="order-card">
      <div class="order-head">
        <span style="color:#999;font-size:13px;">订单号：{{ o.orderNo }}</span>
        <span style="color:#999;font-size:13px;">{{ o.createTime }}</span>
        <span style="color:#ff5000;font-weight:700;">{{ o.statusText }}</span>
      </div>
      <div class="order-body" @click="router.push(`/order/${o.id}`)">
        <img v-for="it in o.items.slice(0, 3)" :key="it.id" :src="it.coverImage" class="order-img" />
        <div class="order-info">
          <div>{{ o.shopName }}</div>
          <div style="color:#999;font-size:12px;">共 {{ o.items.reduce((s, x) => s + x.quantity, 0) }} 件商品</div>
        </div>
        <div class="price">¥{{ o.payAmount }}</div>
      </div>
      <div class="order-actions">
        <template v-if="o.status === 0">
          <button class="btn-primary" @click="goPay(o.id)">立即支付</button>
          <button class="btn-plain" @click="doCancel(o.id)">取消订单</button>
        </template>
        <template v-else-if="o.status === 2">
          <button class="btn-primary" @click="doConfirm(o.id)">确认收货</button>
        </template>
        <button v-if="o.status === 3 || o.status === 4" class="btn-plain" @click="reBuy(o)">再次购买</button>
        <button class="btn-plain" @click="router.push(`/order/${o.id}`)">查看详情</button>
      </div>
    </div>

    <div v-if="!orders.length" style="text-align:center;color:#999;padding:60px;">暂无订单</div>
    <div class="pager" v-if="total > 5">
      <button class="btn-plain" :disabled="page <= 1" @click="page--; load()">上一页</button>
      <span>{{ page }} / {{ Math.ceil(total / 5) }}</span>
      <button class="btn-plain" :disabled="page >= Math.ceil(total / 5)" @click="page++; load()">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.tab {
  padding: 8px 18px;
  border-radius: 20px;
  background: #f5f5f5;
  font-size: 14px;
}
.tab.on {
  background: #ff5000;
  color: #fff;
}
.order-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 14px;
}
.order-head {
  display: flex;
  gap: 20px;
  align-items: center;
  padding: 10px 14px;
  background: #fafafa;
  border-radius: 8px 8px 0 0;
}
.order-body {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px;
  cursor: pointer;
}
.order-img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
}
.order-info {
  flex: 1;
}
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 14px 14px;
}
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}
</style>