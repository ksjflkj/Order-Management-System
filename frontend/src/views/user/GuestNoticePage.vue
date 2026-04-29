<template>
  <div class="guest-notice-wrapper">
    <div class="guest-card">
      <div class="lock-icon">🔐</div>
      <h2 class="notice-title">当前未登录</h2>
      <p class="notice-desc">登录后即可查看此功能，立即登录体验完整服务</p>

      <div class="btn-group">
        <button class="btn-primary" @click="goLogin">立即登录</button>
        <button class="btn-secondary" @click="goRegister">去注册账号</button>
      </div>

      <p class="or-browse" @click="goHome">
        <span>继续浏览商家列表 →</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 原来要访问的路径，登录后跳回去
const from = route.query.from || '/user/merchant-list'

const goLogin = () => {
  router.push(`/login?redirect=${encodeURIComponent(from)}`)
}

const goRegister = () => {
  router.push('/register')
}

const goHome = () => {
  router.push('/user/merchant-list')
}
</script>

<style scoped>
.guest-notice-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #f0f4ff 0%, #fdf6ee 100%);
  padding: 40px 20px;
}

.guest-card {
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 8px 40px rgba(100, 120, 200, 0.12);
  padding: 56px 60px;
  max-width: 460px;
  width: 100%;
  text-align: center;
  animation: fadeSlideUp 0.4s ease;
}

@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.lock-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.08); }
}

.notice-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a2551;
  margin: 0 0 12px;
}

.notice-desc {
  font-size: 15px;
  color: #8a97b5;
  margin: 0 0 36px;
  line-height: 1.6;
}

.btn-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.btn-primary {
  width: 100%;
  height: 48px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #e59448, #d4792a);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
  box-shadow: 0 4px 16px rgba(229, 148, 72, 0.35);
}

.btn-primary:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.btn-secondary {
  width: 100%;
  height: 48px;
  border-radius: 999px;
  border: 2px solid #e59448;
  background: transparent;
  color: #e59448;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: rgba(229, 148, 72, 0.08);
  transform: translateY(-1px);
}

.or-browse {
  margin: 0;
  font-size: 14px;
  color: #aab0c4;
  cursor: pointer;
  transition: color 0.2s;
}

.or-browse:hover {
  color: #6b7aaa;
}

.or-browse span {
  border-bottom: 1px dashed currentColor;
}
</style>
