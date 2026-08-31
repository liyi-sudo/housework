<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { updateUserInfo } from '../api'

const router = useRouter()
const userStore = useUserStore()
const nickname = ref('')
const gender = ref(0)
const editing = ref(false)

const menus = [
  { label: '我的订单', path: '/orders' },
  { label: '消息通知', path: '/notifications' },
  { label: '我的售后', path: '/aftersales' },
  { label: '我的评价', path: '/reviews' },
  { label: '我的收藏', path: '/favorites' },
  { label: '浏览记录', path: '/history' },
  { label: '我的优惠券', path: '/coupons' },
  { label: '收货地址', path: '/address' },
  { label: '修改密码', path: '/user/password' }
]

onMounted(async () => {
  await userStore.fetchInfo()
  nickname.value = userStore.userInfo?.nickname || ''
  gender.value = userStore.userInfo?.gender || 0
})

async function save() {
  await updateUserInfo({ nickname: nickname.value, gender: gender.value, avatar: userStore.userInfo?.avatar })
  await userStore.fetchInfo()
  editing.value = false
}
</script>

<template>
  <div class="user-layout">
    <div class="profile card">
      <img :src="userStore.userInfo?.avatar || 'https://loremflickr.com/100/100/portrait?lock=950'" class="avatar" />
      <div>
        <h2>{{ userStore.userInfo?.nickname }}</h2>
        <p style="color:#999;font-size:13px;">{{ userStore.userInfo?.phone }}</p>
      </div>
      <button class="btn-plain" @click="editing = !editing">{{ editing ? '取消' : '编辑资料' }}</button>
    </div>

    <div class="card edit" v-if="editing">
      <div class="field">
        <label>昵称</label>
        <input v-model="nickname" class="input" />
      </div>
      <div class="field">
        <label>性别</label>
        <select v-model="gender" class="input">
          <option :value="0">保密</option>
          <option :value="1">男</option>
          <option :value="2">女</option>
        </select>
      </div>
      <button class="btn-primary" @click="save">保存</button>
    </div>

    <div class="menu-grid">
      <router-link v-for="m in menus" :key="m.path" :to="m.path" class="menu-item card">{{ m.label }}</router-link>
    </div>
  </div>
</template>

<style scoped>
.user-layout {
  max-width: 760px;
  margin: 0 auto;
}
.profile {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 16px;
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
}
.edit {
  padding: 20px;
  margin-bottom: 16px;
}
.field {
  margin-bottom: 14px;
}
.field label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.menu-item {
  text-align: center;
  padding: 24px 10px;
  font-size: 14px;
}
.menu-item:hover {
  color: #ff5000;
}
</style>