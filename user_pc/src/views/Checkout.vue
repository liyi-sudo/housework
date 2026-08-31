<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCart, getAddresses, getMyCoupons, submitOrder } from '../api'

const route = useRoute()
const router = useRouter()

const items = ref([])
const addresses = ref([])
const selectedAddr = ref(null)
const coupons = ref([])
const selectedCoupon = ref(null)
const remark = ref('')
const submitting = ref(false)

const ids = computed(() => (route.query.ids || '').split(',').filter(Boolean).map(Number))
const totalPrice = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2))

onMounted(async () => {
  const [cart, addr] = await Promise.all([getCart(), getAddresses()])
  items.value = cart.filter(i => ids.value.includes(i.id) && i.checked === 1)
  addresses.value = addr
  selectedAddr.value = addr.find(a => a.isDefault === 1) || addr[0] || null
  coupons.value = await getMyCoupons({ status: 0 })
})

const amount = computed(() => {
  const total = parseFloat(totalPrice.value)
  let discount = 0
  let price = total
  if (selectedCoupon.value && total >= selectedCoupon.value.threshold) {
    discount = Math.min(selectedCoupon.value.amount, total)
    price = total - discount
  }
  const freight = price >= 99 ? 0 : 8
  return { total: total.toFixed(2), discount: discount.toFixed(2), freight, pay: (price + freight).toFixed(2) }
})

async function doSubmit() {
  if (!selectedAddr.value) {
    alert('请先选择收货地址')
    return
  }
  submitting.value = true
  try {
    const created = await submitOrder({
      addressId: selectedAddr.value.id,
      cartIds: ids.value,
      userCouponId: selectedCoupon.value ? selectedCoupon.value.id : null,
      remark: remark.value
    })
    alert('下单成功，请前往支付')
    // 多店铺会拆成多单，跳第一单的收银台（其余在订单列表继续支付）
    if (created && created.length) {
      router.push(`/pay/${created[0].id}`)
    } else {
      router.push('/orders?status=0')
    }
  } catch (e) {
    alert(e.message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="checkout-layout">
    <div class="left">
      <div class="card page-section">
        <h3 class="section-title">收货地址</h3>
        <div v-for="a in addresses" :key="a.id" class="addr-item" :class="{ on: selectedAddr && selectedAddr.id === a.id }"
             @click="selectedAddr = a">
          <div>
            <span style="font-weight:700;">{{ a.receiverName }}</span>
            <span style="color:#999;margin-left:10px;">{{ a.receiverPhone }}</span>
            <span v-if="a.isDefault === 1" class="default-tag">默认</span>
          </div>
          <div style="font-size:13px;margin-top:4px;">{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</div>
        </div>
        <div v-if="!addresses.length" style="color:#999;">暂无地址，请先在个人中心添加</div>
        <router-link to="/address" class="btn-plain" style="display:inline-block;margin-top:12px;">管理地址</router-link>
      </div>

      <div class="card page-section">
        <h3 class="section-title">商品清单</h3>
        <div v-for="i in items" :key="i.id" style="display:flex;gap:12px;padding:10px 0;align-items:center;">
          <img :src="i.mainImage" style="width:60px;height:60px;border-radius:6px;object-fit:cover;" />
          <div style="flex:1;">
            <div>{{ i.goodsName }}</div>
            <div style="color:#999;font-size:12px;">{{ i.skuSpec }}</div>
          </div>
          <span>¥{{ i.price }}</span>
          <span>x{{ i.quantity }}</span>
        </div>
      </div>

      <div class="card page-section">
        <h3 class="section-title">优惠券</h3>
        <select v-model="selectedCoupon" class="input" style="max-width:320px;">
          <option :value="null">不使用优惠券</option>
          <option v-for="c in coupons" :key="c.id" :value="c">{{ c.name }}（满{{ c.threshold }}减{{ c.amount }}）</option>
        </select>
      </div>

      <div class="card">
        <h3 class="section-title">备注</h3>
        <input v-model="remark" class="input" placeholder="选填，给商家留言" />
      </div>
    </div>

    <div class="right">
      <div class="card" style="position:sticky;top:80px;">
        <h3>结算明细</h3>
        <div class="sum-row"><span>商品金额</span><span>¥{{ amount.total }}</span></div>
        <div class="sum-row"><span>优惠券</span><span>-¥{{ amount.discount }}</span></div>
        <div class="sum-row"><span>运费（模拟）</span><span>¥{{ amount.freight }}</span></div>
        <div class="sum-row total"><span>应付总额</span><span class="price" style="font-size:22px;">¥{{ amount.pay }}</span></div>
        <button class="btn-primary" style="width:100%;font-size:16px;padding:14px;" :disabled="submitting" @click="doSubmit">
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkout-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}
.addr-item {
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
}
.addr-item.on {
  border-color: #ff5000;
  background: #fff7f3;
}
.default-tag {
  background: #ff5000;
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 8px;
}
.sum-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
}
.sum-row.total {
  border-top: 1px dashed #eee;
  border-bottom: 1px dashed #eee;
  margin: 8px 0;
  align-items: center;
}
</style>