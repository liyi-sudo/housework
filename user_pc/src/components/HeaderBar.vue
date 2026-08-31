<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const keyword = ref('')

function onSearch() {
  router.push({ path: '/search', query: { keyword: keyword.value } })
}
</script>

<template>
  <header class="header">
    <div class="container header-inner">
      <div class="logo" @click="router.push('/')">优选商城</div>
      <div class="search-box">
        <input v-model="keyword" class="input" placeholder="搜索商品" @keyup.enter="onSearch" />
        <button class="btn-primary" @click="onSearch">搜索</button>
      </div>
      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/category">分类</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">我的订单</router-link>
        <template v-if="userStore.isLogin">
          <router-link to="/user">{{ userStore.userInfo?.nickname || '个人中心' }}</router-link>
          <a href="javascript:void(0)" @click="userStore.logout()">退出</a>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: #fff;
  border-bottom: 2px solid #ff5000;
}
.header-inner {
  display: flex;
  align-items: center;
  height: 60px;
  gap: 20px;
}
.logo {
  font-size: 22px;
  font-weight: 800;
  color: #ff5000;
  cursor: pointer;
  white-space: nowrap;
}
.search-box {
  display: flex;
  flex: 1;
  max-width: 420px;
  gap: 8px;
}
.nav {
  display: flex;
  gap: 16px;
  font-size: 14px;
  white-space: nowrap;
}
.nav a:hover {
  color: #ff5000;
}
</style>