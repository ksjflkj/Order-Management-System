<template>
  <div class="scroll-container">
    <div id="2_15501" class="vector-wrapper-2_15501">
      <div id="2_15501" class="Pixso-vector-2_15501"></div>
      <section class="login-panel">
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="brand">Aprycot</div>
          <h1 class="title">登录</h1>
          <p class="subtitle">登录以保持连接。</p>

          <label class="field-label label-email" for="account">用户名 / 手机号</label>
          <input
            id="account"
            v-model.trim="form.account"
            class="field-input"
            type="text"
            autocomplete="username"
            placeholder="请输入用户名或手机号"
          />

          <label class="field-label label-password" for="password">密码</label>
          <input
            id="password"
            v-model.trim="form.password"
            class="field-input"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
          />

          <div class="options-row">
            <label class="remember">
              <input v-model="rememberMe" type="checkbox" />
              <span>记住我</span>
            </label>
            <button type="button" class="text-link" aria-label="忘记密码" @click="router.push('/forgot-password')">忘记密码</button>
          </div>

          <button class="submit-btn" type="submit" aria-label="登录">登录</button>

          <p class="other-login">或使用以下方式登录</p>
          <div class="social-row">
            <span class="social g">G</span>
            <span class="social f">f</span>
            <span class="social t">t</span>
            <span class="social i">in</span>
          </div>

          <p class="signup-row">
            还没有账号？
            <button type="button" class="signup-link" @click="handleRegister">点击这里注册</button>
          </p>

          <p class="browse-row">
            <button type="button" class="browse-link" @click="handleBrowse">暂不登录，继续浏览 →</button>
          </p>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login } from '@/api/auth'
import { setToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const rememberMe = ref(false)

const form = reactive({
  account: '',
  password: ''
})

function getDefaultRouteByRole(role) {
  if (role === 'ADMIN') return '/admin/home'
  if (role === 'MERCHANT') return '/merchant/home'
  return '/user/home'
}

function validateForm() {
  if (!form.account) {
    ElMessage.error('请输入用户名或手机号')
    return false
  }
  if (!form.password) {
    ElMessage.error('请输入密码')
    return false
  }
  return true
}

const handleRegister = () => {
  router.push('/register')
}

// 暂不登录：游客 token 已由路由守卫自动申请，直接跳到商家列表
const handleBrowse = () => {
  router.push('/user/merchant-list')
}

const handleLogin = async () => {
  if (!validateForm()) return
  try {
    const res = await login({
      account: form.account,
      password: form.password
    })

    if (res.code === 200) {
      setToken(res.data.token)
      await userStore.fetchUserInfo()
      ElMessage.success('登录成功')
      // 优先跳回来源页（redirect query 参数），使用硬跳转确保状态干净
      const redirect = route.query.redirect
      const target = redirect ? decodeURIComponent(redirect) : getDefaultRouteByRole(userStore.userInfo?.role)
      window.location.href = target
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  }
}
</script>

<style scoped>
.scroll-container {
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background: #fffdf8;
}

.Pixso-vector-2_15501 {
  position: absolute;
  inset: 0;
  background-image: url('@/assets/Vector_2_15501.png');
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
}

.vector-wrapper-2_15501 {
  position: relative;
  width: 100vw;
  height: 100vh;
}

.login-panel {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.login-form {
  position: relative;
  width: min(420px, 86vw);
  margin-left: clamp(28px, 4.6vw, 92px);
  margin-top: clamp(44px, 8.5vh, 126px);
  color: #1f2a44;
  pointer-events: auto;
}

.brand {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(34px, 3.7vw, 54px);
  font-weight: 700;
  color: #1a2551;
  margin-bottom: 14px;
}

.title {
  margin: 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(42px, 4.2vw, 64px);
  font-weight: 700;
  line-height: 1.04;
  color: #1a2551;
}

.subtitle {
  margin: 8px 0 16px;
  color: #b9ab9a;
  font-size: clamp(12px, 1.1vw, 16px);
}

.field-label {
  display: block;
  color: #c4b29f;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(20px, 2.1vw, 36px);
  font-weight: 700;
}

.label-email {
  margin-bottom: 8px;
}

.label-password {
  margin: 8px 0 8px;
}

.field-input {
  width: 100%;
  height: clamp(42px, 5.5vh, 66px);
  border: 1.6px solid #ef9f54;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  padding: 0 20px;
  font-size: clamp(14px, 1.2vw, 20px);
  color: #28334d;
  outline: none;
  margin-bottom: 8px;
}

.field-input::placeholder {
  color: #c9c9c9;
}

.options-row {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #c4b198;
  font-size: clamp(12px, 1.1vw, 18px);
}

.remember input {
  width: 14px;
  height: 14px;
  margin: 0;
  cursor: pointer;
}

.text-link {
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  color: #ef9f54;
  font-size: clamp(12px, 1.1vw, 18px);
}

.submit-btn {
  margin-top: 14px;
  width: 100%;
  height: clamp(44px, 6.2vh, 72px);
  border-radius: 999px;
  border: none;
  background: #e59448;
  color: #fff;
  font-size: clamp(18px, 2vw, 38px);
  font-weight: 700;
  cursor: pointer;
}

.other-login {
  margin: 12px 0 8px;
  text-align: center;
  color: #b9ab9a;
  font-size: clamp(12px, 1.1vw, 18px);
}

.social-row {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.social {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.g {
  background: #db4437;
}

.f {
  background: #3b5998;
}

.t {
  background: #1da1f2;
}

.i {
  background: #0a66c2;
  font-size: 10px;
}

.signup-row {
  margin-top: 10px;
  text-align: center;
  color: #2a2f45;
  font-size: clamp(13px, 1.2vw, 24px);
}

.signup-link {
  border: none;
  background: transparent;
  color: #ef9f54;
  padding: 0 0 0 4px;
  font-size: inherit;
  cursor: pointer;
}

.browse-row {
  margin-top: 6px;
  text-align: center;
}

.browse-link {
  border: none;
  background: transparent;
  color: #b0b8cc;
  font-size: clamp(12px, 1.1vw, 18px);
  cursor: pointer;
  transition: color 0.2s;
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-style: dashed;
}

.browse-link:hover {
  color: #7a87a8;
}
</style>