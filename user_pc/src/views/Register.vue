<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendSmsCode } from '../api'

const router = useRouter()
const phone = ref('')
const password = ref('')
const confirm = ref('')
const nickname = ref('')
const code = ref('')
const countdown = ref(0)
const error = ref('')
let timer = null

async function sendCode() {
  if (!/^1\d{10}$/.test(phone.value)) {
    error.value = '请输入正确的手机号'
    return
  }
  try {
    const data = await sendSmsCode(phone.value)
    error.value = ''
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
    alert('模拟验证码已发送：' + data.code)
  } catch (e) {
    error.value = e.message
  }
}

async function doRegister() {
  if (!/^1\d{10}$/.test(phone.value)) {
    error.value = '请输入正确的手机号'
    return
  }
  if (!code.value) {
    error.value = '请输入验证码'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码长度至少 6 位'
    return
  }
  if (password.value !== confirm.value) {
    error.value = '两次密码输入不一致'
    return
  }
  try {
    await register({ phone: phone.value, password: password.value, nickname: nickname.value, code: code.value })
    alert('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    error.value = e.message
  }
}

onUnmounted(() => timer && clearInterval(timer))
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 style="text-align:center;color:#ff5000;">注册新账号</h2>
      <div class="field">
        <label>手机号</label>
        <input v-model="phone" class="input" placeholder="请输入手机号" maxlength="11" />
      </div>
      <div class="field">
        <label>昵称</label>
        <input v-model="nickname" class="input" placeholder="选填" />
      </div>
      <div class="field">
        <label>验证码（模拟发送）</label>
        <div style="display:flex;gap:8px;">
          <input v-model="code" class="input" style="flex:1;" placeholder="6 位验证码" maxlength="6" />
          <button class="btn-plain" style="width:120px;white-space:nowrap;" :disabled="countdown > 0" @click="sendCode">
            {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
          </button>
        </div>
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="password" type="password" class="input" placeholder="至少 6 位" />
      </div>
      <div class="field">
        <label>确认密码</label>
        <input v-model="confirm" type="password" class="input" placeholder="再次输入密码" />
      </div>
      <div v-if="error" style="color:#ff4d4f;font-size:13px;margin-bottom:10px;">{{ error }}</div>
      <button class="btn-primary" style="width:100%;font-size:16px;padding:12px;" @click="doRegister">注册</button>
      <div style="text-align:center;margin-top:14px;font-size:13px;">
        已有账号？<router-link to="/login" style="color:#ff5000;">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.auth-card {
  width: 380px;
  padding: 32px;
}
.field {
  margin-bottom: 16px;
}
.field label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
</style>