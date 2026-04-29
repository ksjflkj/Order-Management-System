<template>
  <div class="home-page">
    <h1>点餐管理系统</h1>
    <p>欢迎你，{{ userStore.userInfo?.username }}</p>
    <p>当前角色：{{ userStore.userInfo?.role }}</p>

    <button @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home-page {
  padding: 60px;
  text-align: center;
}
button {
  margin-top: 20px;
  width: 120px;
  height: 36px;
  cursor: pointer;
}
</style>