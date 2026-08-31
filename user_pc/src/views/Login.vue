<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { sendSmsCode } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginMode = ref('password') // password | code
const phone = ref('')
const password = ref('')
const code = ref('')
const countdown = ref(0)
const error = ref('')
const loading = ref(false)
let timer = null

async function sendCode() {
  if (!/^1\d{10}$/.test(phone.value)) {
    error.value = '请输入正确的手机号'
    return
  }
  try {
    const code = await sendSmsCode(phone.value)
    error.value = ''
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
    alert('模拟验证码已发送：' + code)
  } catch (e) {
    error.value = e.message
  }
}

async function doLogin() {
  if (!/^1\d{10}$/.test(phone.value)) {
    error.value = '请输入正确的手机号'
    return
  }
  if (loginMode.value === 'password' && !password.value) {
    error.value = '请输入密码'
    return
  }
  if (loginMode.value === 'code' && !code.value) {
    error.value = '请输入验证码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    if (loginMode.value === 'code') {
      await userStore.login(phone.value, '', code.value)
    } else {
      await userStore.login(phone.value, password.value)
    }
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onUnmounted(() => timer && clearInterval(timer))
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 style="text-align:center;color:#ff5000;">优选商城</h2>
      <p style="text-align:center;color:#999;margin:8px 0 20px;">网页端 / 小程序共用同一账号</p>
      <div style="display:flex;border-bottom:1px solid #eee;margin-bottom:16px;">
        <div
          :class="['login-tab', { active: loginMode === 'password' }]"
          @click="loginMode = 'password'"
        >密码登录</div>
        <div
          :class="['login-tab', { active: loginMode === 'code' }]"
          @click="loginMode = 'code'"
        >验证码登录</div>
      </div>
      <div class="field">
        <label>手机号</label>
        <input v-model="phone" class="input" placeholder="请输入手机号" maxlength="11" />
      </div>
      <div v-if="loginMode === 'password'" class="field">
        <label>密码</label>
        <input v-model="password" type="password" class="input" placeholder="请输入密码" @keyup.enter="doLogin" />
      </div>
      <div v-else class="field">
        <label>验证码（模拟发送）</label>
        <div style="display:flex;gap:8px;">
          <input v-model="code" class="input" style="flex:1;" placeholder="6 位验证码" maxlength="6" @keyup.enter="doLogin" />
          <button class="btn-plain" style="width:120px;white-space:nowrap;" :disabled="countdown > 0" @click="sendCode">
            {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
          </button>
        </div>
      </div>
      <div v-if="error" style="color:#ff4d4f;font-size:13px;margin-bottom:10px;">{{ error }}</div>
      <button class="btn-primary" style="width:100%;font-size:16px;padding:12px;" :disabled="loading" @click="doLogin">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <div style="text-align:center;margin-top:14px;font-size:13px;">
        没有账号？<router-link to="/register" style="color:#ff5000;">立即注册</router-link>
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
.login-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
}
.login-tab.active {
  color: #ff5000;
  font-weight: bold;
  border-bottom-color: #ff5000;
}
</style>