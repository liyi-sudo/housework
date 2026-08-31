<script setup>
import { ref } from 'vue'
import { changePassword } from '../api'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const oldPassword = ref('')
const newPassword = ref('')
const confirm = ref('')
const error = ref('')

async function save() {
  if (newPassword.value.length < 6) {
    error.value = '新密码至少 6 位'
    return
  }
  if (newPassword.value !== confirm.value) {
    error.value = '两次密码输入不一致'
    return
  }
  try {
    await changePassword({ oldPassword: oldPassword.value, newPassword: newPassword.value })
    alert('密码修改成功，请重新登录')
    await userStore.logout()
    router.push('/login')
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <div class="pwd-page">
    <div class="card" style="max-width:420px;margin:0 auto;padding:28px;">
      <h3 class="section-title">修改密码</h3>
      <div class="field"><label>原密码</label><input v-model="oldPassword" type="password" class="input" /></div>
      <div class="field"><label>新密码</label><input v-model="newPassword" type="password" class="input" /></div>
      <div class="field"><label>确认新密码</label><input v-model="confirm" type="password" class="input" /></div>
      <div v-if="error" style="color:#ff4d4f;font-size:13px;margin-bottom:10px;">{{ error }}</div>
      <button class="btn-primary" style="width:100%;" @click="save">确认修改</button>
    </div>
  </div>
</template>

<style scoped>
.pwd-page {
  padding: 40px 0;
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
</style>