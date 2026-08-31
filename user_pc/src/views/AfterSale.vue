<script setup>
import { ref, onMounted } from 'vue'
import { getAfterSales } from '../api'

const lists = ref([])
const typeMap = { 1: '仅退款', 2: '退货退款' }
const statusMap = { 0: '待商家处理', 1: '已同意', 2: '已驳回', 3: '已完成' }

onMounted(async () => {
  lists.value = await getAfterSales()
})
</script>

<template>
  <div class="card">
    <h3 class="section-title">我的售后</h3>
    <div v-for="a in lists" :key="a.id" class="as-card">
      <div style="display:flex;justify-content:space-between;">
        <span>{{ typeMap[a.type] }}</span>
        <span style="color:#ff5000;font-weight:700;">{{ statusMap[a.status] }}</span>
      </div>
      <div style="color:#666;font-size:13px;margin:6px 0;">原因：{{ a.reason }}</div>
      <div style="color:#666;font-size:13px;">退款金额：¥{{ a.refundAmount }}</div>
      <div style="color:#999;font-size:12px;margin-top:4px;">{{ a.createTime }}</div>
      <div v-if="a.reply" style="color:#ff5000;font-size:13px;margin-top:6px;">商家回复：{{ a.reply }}</div>
    </div>
    <div v-if="!lists.length" style="text-align:center;color:#999;padding:60px;">暂无售后记录</div>
  </div>
</template>

<style scoped>
.as-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
}
</style>