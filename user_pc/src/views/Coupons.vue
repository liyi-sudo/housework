<script setup>
import { ref, onMounted } from 'vue'
import { getClaimableCoupons, claimCoupon, getMyCoupons } from '../api'

const claimable = ref([])
const mine = ref([])

onMounted(load)

async function load() {
  const [c, m] = await Promise.all([getClaimableCoupons(), getMyCoupons({})])
  claimable.value = c
  mine.value = m.map(x => ({ ...x, _type: 'mine' }))
}

async function claim(id) {
  try {
    await claimCoupon(id)
    alert('领取成功')
    await load()
  } catch (e) {
    alert(e.message)
  }
}
</script>

<template>
  <div>
    <div class="card page-section">
      <h3 class="section-title">可领取优惠券</h3>
      <div class="coupon-list">
        <div v-for="c in claimable" :key="c.id" class="coupon-row">
          <div class="amount">¥{{ Math.floor(c.amount) }}</div>
          <div style="flex:1;">
            <div style="font-weight:700;">{{ c.name }}</div>
            <div style="color:#999;font-size:12px;">满 {{ c.threshold }} 可用</div>
          </div>
          <button class="btn-primary" :class="{ done: c.claimed }" :disabled="c.claimed" @click="claim(c.id)">
            {{ c.claimed ? '已领取' : '领取' }}
          </button>
        </div>
      </div>
      <div v-if="!claimable.length" style="color:#999;text-align:center;padding:30px;">暂无可领取的优惠券</div>
    </div>

    <div class="card">
      <h3 class="section-title">我的优惠券</h3>
      <div v-for="m in mine" :key="m.id" class="coupon-row">
        <div class="amount used" v-if="m.status === 1">已使用</div>
        <div class="amount" v-else>¥{{ Math.floor(m.amount) }}</div>
        <div style="flex:1;">
          <div style="font-weight:700;">{{ m.name }}</div>
          <div style="color:#999;font-size:12px;">满 {{ m.threshold }} 可用 · 有效期至 {{ (m.endTime || '').split(' ')[0] }}</div>
        </div>
        <span v-if="m.status === 0" style="color:#ff5000;">未使用</span>
        <span v-else-if="m.status === 2" style="color:#999;">已过期</span>
      </div>
      <div v-if="!mine.length" style="color:#999;text-align:center;padding:30px;">暂无优惠券</div>
    </div>
  </div>
</template>

<style scoped>
.coupon-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border: 1px dashed #ddd;
  border-radius: 8px;
  margin-bottom: 10px;
}
.amount {
  width: 90px;
  text-align: center;
  color: #ff5000;
  font-size: 20px;
  font-weight: 800;
  background: #fff7f3;
  border-radius: 6px;
  padding: 10px 0;
}
.amount.used {
  color: #999;
  background: #f5f5f5;
  font-size: 13px;
}
.btn-primary.done {
  background: #ccc;
  cursor: not-allowed;
  border-color: #ccc;
}
</style>