<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, getLogistics, cancelOrder, confirmOrder, applyAfterSale, publishReview, addToCart } from '../api'
import ReviewImages from '../components/ReviewImages.vue'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const logistics = ref(null)
const showAs = ref(false)
const showReview = ref(false)
const asType = ref(1)
const asReason = ref('')
const reviewItem = ref(null)
const rating = ref(5)
const reviewContent = ref('')
const reviewImages = ref([])

const statusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '售后中' }

onMounted(async () => {
  order.value = await getOrderDetail(route.params.id)
  if (order.value.status === 2 || order.value.status === 3) {
    try {
      logistics.value = await getLogistics(order.value.id)
    } catch (e) {
      // ignore
    }
  }
})

function goPay() {
  router.push(`/pay/${order.value.id}`)
}

async function doCancel() {
  if (!confirm('确定取消订单？')) return
  await cancelOrder(order.value.id)
  order.value = await getOrderDetail(route.params.id)
}

async function doConfirm() {
  if (!confirm('确认已收到货？')) return
  await confirmOrder(order.value.id)
  order.value = await getOrderDetail(route.params.id)
}

async function submitAfterSale() {
  try {
    await applyAfterSale({ orderId: order.value.id, type: asType.value, reason: asReason.value, images: '' })
    alert('售后申请已提交，请等待商家处理')
    showAs.value = false
    order.value = await getOrderDetail(route.params.id)
  } catch (e) {
    alert(e.message)
  }
}

async function reBuy() {
  for (const it of order.value.items || []) {
    await addToCart({ skuId: it.skuId, quantity: it.quantity })
  }
  router.push('/cart')
}

function openReview(item) {
  reviewItem.value = item
  reviewContent.value = ''
  reviewImages.value = []
  showReview.value = true
}

async function submitReview() {
  try {
    await publishReview({
      orderId: order.value.id,
      orderItemId: reviewItem.value.id,
      rating: rating.value,
      content: reviewContent.value,
      images: reviewImages.value.join(',')
    })
    alert('评价成功')
    showReview.value = false
    order.value = await getOrderDetail(route.params.id)
  } catch (e) {
    alert(e.message)
  }
}
</script>

<template>
  <div v-if="order" style="display:grid;grid-template-columns:2fr 1fr;gap:16px;">
    <div>
      <div class="card page-section">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h2>订单详情</h2>
          <span style="color:#ff5000;font-size:18px;font-weight:700;">{{ statusMap[order.status] }}</span>
        </div>
        <div style="margin-top:10px;color:#666;font-size:14px;">
          <p>订单号：{{ order.orderNo }}</p>
          <p>下单时间：{{ order.createTime }}</p>
          <p v-if="order.payTime">支付时间：{{ order.payTime }}</p>
          <p>收货地址：{{ order.addrSnapshot }}</p>
          <p v-if="order.remark">备注：{{ order.remark }}</p>
        </div>
        <div class="ops" v-if="order.status === 0">
          <button class="btn-primary" @click="goPay">立即支付</button>
          <button class="btn-plain" @click="doCancel">取消订单</button>
        </div>
        <div class="ops" v-if="order.status === 2">
          <button class="btn-primary" @click="doConfirm">确认收货</button>
        </div>
        <div class="ops" v-if="[1, 2, 3].includes(order.status)">
          <button class="btn-plain" @click="showAs = true">申请售后</button>
        </div>
        <div class="ops" v-if="order.status === 3 || order.status === 4">
          <button class="btn-primary" @click="reBuy">再次购买</button>
        </div>
      </div>

      <div class="card page-section">
        <h3 class="section-title">商品信息</h3>
        <div v-for="it in order.items" :key="it.id" class="item-row">
          <img :src="it.coverImage" class="item-img" />
          <div style="flex:1;">
            <div>{{ it.goodsName }}</div>
            <div style="color:#999;font-size:12px;">{{ it.skuSpec }}</div>
          </div>
          <span>¥{{ it.price }}</span>
          <span style="color:#999;">x{{ it.quantity }}</span>
          <button v-if="order.status === 3 && it.reviewStatus === 0" class="btn-plain" @click="openReview(it)">去评价</button>
        </div>
      </div>

      <div class="card" v-if="logistics && logistics.trace">
        <h3 class="section-title">物流信息（模拟）</h3>
        <p style="color:#999;font-size:13px;">{{ logistics.company }} {{ logistics.logisticsNo }}</p>
        <div class="trace-list">
          <div v-for="line in logistics.trace.split(';')" :key="line" class="trace-item">· {{ line }}</div>
        </div>
      </div>
    </div>

    <div>
      <div class="card" style="position:sticky;top:80px;">
        <h3>金额明细</h3>
        <div class="sum-row"><span>商品金额</span><span>¥{{ order.totalAmount }}</span></div>
        <div class="sum-row"><span>运费</span><span>¥{{ order.freight }}</span></div>
        <div class="sum-row"><span>优惠</span><span>-¥{{ order.discount }}</span></div>
        <div class="sum-row total"><span>实付</span><span class="price" style="font-size:20px;">¥{{ order.payAmount }}</span></div>
      </div>
    </div>

    <div class="modal" v-if="showAs">
      <div class="modal-body">
        <h3>申请售后</h3>
        <select v-model="asType" class="input" style="margin:12px 0;">
          <option :value="1">仅退款</option>
          <option :value="2">退货退款</option>
        </select>
        <textarea v-model="asReason" class="input" placeholder="请填写申请原因" style="min-height:80px;"></textarea>
        <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
          <button class="btn-plain" @click="showAs = false">取消</button>
          <button class="btn-primary" @click="submitAfterSale">提交</button>
        </div>
      </div>
    </div>

    <div class="modal" v-if="showReview">
      <div class="modal-body">
        <h3>评价商品：{{ reviewItem.goodsName }}</h3>
        <div style="margin:12px 0;">
          <span>评分：</span>
          <button v-for="i in 5" :key="i" class="star" :class="{ on: rating >= i }" @click="rating = i">★</button>
        </div>
        <textarea v-model="reviewContent" class="input" placeholder="分享你的使用体验" style="min-height:80px;"></textarea>
        <div style="margin-top:10px;">
          <ReviewImages v-model="reviewImages" />
        </div>
        <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
          <button class="btn-plain" @click="showReview = false">取消</button>
          <button class="btn-primary" @click="submitReview">提交评价</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ops {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}
.item-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.trace-list {
  margin-top: 8px;
}
.trace-item {
  padding: 6px 0;
  color: #666;
  font-size: 14px;
}
.sum-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
}
.sum-row.total {
  border-top: 1px dashed #eee;
  margin-top: 6px;
  padding-top: 12px;
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
}
.star {
  background: none;
  font-size: 22px;
  color: #ddd;
}
.star.on {
  color: #ff8000;
}
</style>