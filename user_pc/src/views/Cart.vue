<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, updateCartQty, deleteCartItem, checkCart, getClaimableCoupons, getMyCoupons } from '../api'

const router = useRouter()
const items = ref([])
const allChecked = ref(false)
const claimableCount = ref(0)
const usableCount = ref(0)

const checkedItems = computed(() => items.value.filter(i => i.checked === 1))
const totalPrice = computed(() =>
  checkedItems.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2)
)

onMounted(load)

async function load() {
  const [list, claimable, mine] = await Promise.all([
    getCart(),
    getClaimableCoupons(),
    getMyCoupons({})
  ])
  items.value = list
  claimableCount.value = claimable.length
  usableCount.value = mine.filter(x => x.status === 0).length
  syncAllChecked()
}

function syncAllChecked() {
  const valid = items.value.filter(i => i.goodsStatus === 1)
  allChecked.value = valid.length > 0 && valid.every(i => i.checked === 1)
}

async function toggleItem(item) {
  await checkCart(item.checked === 1 ? 0 : 1, [item.id])
  item.checked = item.checked === 1 ? 0 : 1
  syncAllChecked()
}

async function toggleAll() {
  const target = allChecked.value ? 0 : 1
  await checkCart(target, null)
  items.value.forEach(i => { i.checked = target })
  allChecked.value = !!target
}

async function changeQty(item, delta) {
  const q = Math.max(1, item.quantity + delta)
  if (q > item.stock) {
    alert('库存不足')
    return
  }
  await updateCartQty(item.id, q)
  item.quantity = q
}

async function remove(item) {
  if (!confirm('确定删除该商品？')) return
  await deleteCartItem(item.id)
  items.value = items.value.filter(i => i.id !== item.id)
  syncAllChecked()
}

function checkout() {
  const ids = checkedItems.value.map(i => i.id)
  if (!ids.length) {
    alert('请先选择商品')
    return
  }
  router.push({ path: '/checkout', query: { ids: ids.join(',') } })
}
</script>

<template>
  <div class="card">
    <h3 class="section-title">购物车</h3>
    <div v-if="!items.length" style="text-align:center;color:#999;padding:80px;">
      <p style="font-size:40px;">🛒</p>
      <p>购物车是空的，去逛逛吧</p>
    </div>

    <template v-else>
      <div class="coupon-strip" @click="router.push('/coupons')">
        <span class="coupon-label">优惠券</span>
        <span v-if="claimableCount || usableCount">
          可领取 {{ claimableCount }} 张 · 已有 {{ usableCount }} 张可用
        </span>
        <span v-else>暂无可用优惠券</span>
        <span class="coupon-go">去领券 ›</span>
      </div>

      <div class="cart-row head">
        <span><input type="checkbox" :checked="allChecked" @change="toggleAll" /> 全选</span>
        <span style="flex:1;">商品</span>
        <span>单价</span>
        <span>数量</span>
        <span>小计</span>
        <span>操作</span>
      </div>

      <div v-for="i in items" :key="i.id" class="cart-row">
        <span>
          <input type="checkbox" :checked="i.checked === 1" :disabled="i.goodsStatus !== 1" @change="toggleItem(i)" />
        </span>
        <span style="flex:1;display:flex;align-items:center;gap:12px;">
          <img :src="i.mainImage" class="row-img" />
          <div>
            <div class="row-name">{{ i.goodsName }}</div>
            <div style="color:#999;font-size:12px;">{{ i.skuSpec }}</div>
            <div v-if="i.goodsStatus !== 1" style="color:#999;">已失效</div>
          </div>
        </span>
        <span class="price">¥{{ i.price }}</span>
        <span class="qty">
          <button @click="changeQty(i, -1)">-</button>
          <span>{{ i.quantity }}</span>
          <button @click="changeQty(i, 1)">+</button>
        </span>
        <span class="price">¥{{ (i.price * i.quantity).toFixed(2) }}</span>
        <span><a href="javascript:void(0)" @click="remove(i)">删除</a></span>
      </div>

      <div class="cart-bottom">
        <span style="flex:1;">共 {{ checkedItems.length }} 件</span>
        <span class="price" style="font-size:20px;">合计：¥{{ totalPrice }}</span>
        <button class="btn-primary" style="font-size:16px;padding:12px 40px;" @click="checkout">去结算</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 8px;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}
.coupon-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  margin-bottom: 8px;
  background: #fff7f3;
  border: 1px dashed #ffd4c0;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}
.coupon-strip:hover {
  border-color: #ff5000;
}
.coupon-label {
  background: #ff5000;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.coupon-go {
  margin-left: auto;
  color: #ff5000;
  font-weight: 600;
}
.cart-row.head {
  color: #999;
  font-size: 13px;
}
.row-img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
}
.row-name {
  font-size: 14px;
}
.qty {
  display: flex;
  align-items: center;
  gap: 8px;
}
.qty button {
  width: 26px;
  height: 26px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
}
.cart-bottom {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 8px;
}
</style>