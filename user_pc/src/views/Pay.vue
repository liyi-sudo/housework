<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder } from '../api'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const method = ref('BALANCE')
const countdown = ref(0)
const expired = ref(false)
const paying = ref(false)
let timer = null

function deadlineTime() {
  // 服务端时间为 yyyy-MM-dd HH:mm:ss，把 - 转 / 以便各浏览器解析
  return new Date(order.value.createTime.replace(/-/g, '/')).getTime() + 30 * 60 * 1000
}

function updateCountdown() {
  if (!order.value || order.value.status !== 0) return
  const remain = Math.max(0, Math.floor((deadlineTime() - Date.now()) / 1000))
  countdown.value = remain
  if (remain <= 0) {
    expired.value = true
    stopTimer()
  }
}

function startTimer() {
  stopTimer()
  timer = setInterval(updateCountdown, 1000)
}

function stopTimer() {
  if (timer) clearInterval(timer)
  timer = null
}

function formatTime(sec) {
  const m = String(Math.floor(sec / 60)).padStart(2, '0')
  const s = String(sec % 60).padStart(2, '0')
  return `${m}:${s}`
}

async function refresh() {
  order.value = await getOrderDetail(route.params.id)
  if (order.value.status === 0) {
    expired.value = false
    updateCountdown()
    startTimer()
  } else {
    expired.value = true
    stopTimer()
  }
}

async function doPay() {
  if (expired.value) {
    alert('订单已超时自动取消，无法支付')
    return
  }
  paying.value = true
  try {
    await payOrder(order.value.id, method.value)
    stopTimer()
    alert('支付成功')
    router.replace(`/order/${order.value.id}`)
  } catch (e) {
    alert(e.message)
  } finally {
    paying.value = false
  }
}

async function doCancel() {
  if (!confirm('确定取消该订单？')) return
  await cancelOrder(order.value.id)
  stopTimer()
  router.replace('/orders')
}

onMounted(refresh)
onUnmounted(stopTimer)
</script>

<template>
  <div v-if="order" class="pay-page">
    <div class="pay-card card">
      <h2 style="text-align:center;color:#ff5000;">收银台</h2>
      <div class="pay-row">
        <span>订单号</span>
        <span style="color:#666;">{{ order.orderNo }}</span>
      </div>
      <div class="pay-row">
        <span>应付金额</span>
        <span class="price" style="font-size:26px;">¥{{ order.payAmount }}</span>
      </div>

      <div class="pay-status" :class="{ danger: expired }">
        <template v-if="order.status === 0 && !expired">
          支付剩余时间：<b style="color:#ff5000;">{{ formatTime(countdown) }}</b>
        </template>
        <template v-else-if="expired">订单已超时自动取消（无法支付）</template>
        <template v-else>订单状态：{{ order.statusText }}，无需支付</template>
      </div>

      <div v-if="order.status === 0 && !expired" class="pay-methods">
        <div class="pay-method" :class="{ on: method === 'BALANCE' }" @click="method = 'BALANCE'">
          <input type="radio" :checked="method === 'BALANCE'" />
          <div>
            <div style="font-weight:600;">模拟余额</div>
            <div style="color:#999;font-size:12px;">账户余额直接扣减</div>
          </div>
        </div>
        <div class="pay-method" :class="{ on: method === 'CARD' }" @click="method = 'CARD'">
          <input type="radio" :checked="method === 'CARD'" />
          <div>
            <div style="font-weight:600;">模拟银行卡</div>
            <div style="color:#999;font-size:12px;">银行卡快捷支付</div>
          </div>
        </div>
      </div>

      <div class="pay-actions">
        <button v-if="order.status === 0" class="btn-primary" style="flex:1;font-size:16px;padding:12px;" :disabled="paying || expired" @click="doPay">
          {{ paying ? '支付中...' : '立即支付' }}
        </button>
        <button v-if="order.status === 0" class="btn-plain" style="flex:1;" @click="doCancel">取消订单</button>
        <button v-else class="btn-plain" style="flex:1;" @click="router.push(`/order/${order.id}`)">查看订单</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pay-page {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.pay-card {
  width: 420px;
  padding: 28px;
}
.pay-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed #f0f0f0;
  font-size: 14px;
}
.pay-status {
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  background: #fff7e6;
  color: #666;
  font-size: 14px;
  text-align: center;
}
.pay-status.danger {
  background: #fff1f0;
  color: #ff4d4f;
}
.pay-methods {
  margin-top: 16px;
}
.pay-method {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 14px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  font-size: 14px;
}
.pay-method.on {
  border-color: #ff5000;
  background: #fff7e6;
}
.pay-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
</style>
