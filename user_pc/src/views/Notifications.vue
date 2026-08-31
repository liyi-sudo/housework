<script setup>
import { ref, onMounted } from 'vue'
import { getUserMessages } from '../api'

const list = ref([])
const loadError = ref('')

onMounted(async () => {
  try {
    list.value = await getUserMessages()
  } catch (e) {
    loadError.value = e.message || '加载失败'
  }
})
</script>

<template>
  <div class="card">
    <h3 class="section-title">消息通知（{{ list.length }}）</h3>
    <div v-if="loadError" style="color:#ff4d4f;padding:16px;">{{ loadError }}</div>
    <div v-else-if="list.length">
      <div v-for="m in list" :key="m.id" class="msg-item">
        <div class="msg-head">
          <span class="msg-type" :class="m.type === 'aftersale' ? 'aftersale' : ''">
            {{ m.type === 'aftersale' ? '售后' : '订单' }}
          </span>
          <span class="msg-title">{{ m.title }}</span>
          <span class="msg-time">{{ m.time }}</span>
        </div>
        <p class="msg-content">{{ m.content }}</p>
      </div>
    </div>
    <div v-else style="text-align:center;color:#999;padding:60px;">暂无消息</div>
  </div>
</template>

<style scoped>
.msg-item {
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.msg-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.msg-type {
  background: #ff5000;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.msg-type.aftersale {
  background: #ffb800;
}
.msg-title {
  font-weight: 600;
  font-size: 14px;
}
.msg-time {
  margin-left: auto;
  color: #999;
  font-size: 12px;
}
.msg-content {
  margin-top: 6px;
  color: #666;
  font-size: 13px;
}
</style>
