<script setup>
import HeaderBar from './components/HeaderBar.vue'
</script>

<template>
  <div>
    <HeaderBar />
    <main class="container" style="margin-top: 16px;">
      <router-view />
    </main>
    <footer style="text-align:center; padding: 24px 0; color:#999; font-size:12px;">
      优选商城
    </footer>
  </div>
</template>