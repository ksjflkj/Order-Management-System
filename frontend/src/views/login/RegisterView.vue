<template>
  <div class="scroll-container">
    <div id="2_15501" class="vector-wrapper-2_15501">
      <div id="2_15501" class="Pixso-vector-2_15501"></div>
      <section class="login-panel">
        <form class="login-form register-form" @submit.prevent="handleRegister">
          <div class="brand">Aprycot</div>
          <h1 class="title">注册</h1>
          <p class="subtitle">欢迎加入我们，请填写注册信息。</p>

          <label class="field-label" for="username">用户名</label>
          <input
            id="username"
            v-model.trim="form.username"
            class="field-input"
            type="text"
            autocomplete="username"
            placeholder="请输入用户名"
          />

          <label class="field-label" for="password">密码</label>
          <input
            id="password"
            v-model.trim="form.password"
            class="field-input"
            type="password"
            autocomplete="new-password"
            placeholder="请输入密码"
          />

          <label class="field-label" for="confirmPassword">确认密码</label>
          <input
            id="confirmPassword"
            v-model.trim="form.confirmPassword"
            class="field-input"
            type="password"
            autocomplete="new-password"
            placeholder="请再次填写密码以确认"
          />

          <label class="field-label" for="phone">手机号 <span class="required-star">*</span></label>
          <div class="phone-row">
            <input
              id="phone"
              v-model.trim="form.phone"
              class="field-input phone-input"
              type="tel"
              placeholder="请输入手机号"
            />
            <button
              type="button"
              class="send-code-btn"
              @click="handleSendCode"
              :disabled="codeCountdown > 0 || sendingCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s 后重试` : '获取验证码' }}
            </button>
          </div>

          <label class="field-label" for="code">验证码 <span class="required-star">*</span></label>
          <input
            id="code"
            v-model.trim="form.code"
            class="field-input"
            type="text"
            maxlength="6"
            placeholder="请输入 6 位短信验证码"
          />

          <label class="field-label" for="email">邮箱</label>
          <input
            id="email"
            v-model.trim="form.email"
            class="field-input"
            type="email"
            placeholder="可选项"
          />

          <button class="submit-btn" type="submit" aria-label="注册" :disabled="loading">
            {{ loading ? '正在注册...' : '立即注册' }}
          </button>

          <p class="signup-row">
            已经拥有账号？
            <button type="button" class="signup-link" @click="goToLogin">点击这里直接登录</button>
          </p>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendRegisterCode } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  code: '',
  email: ''
})

function validateForm() {
  if (!form.username) {
    ElMessage.error('用户名不可为空')
    return false
  }
  if (!form.password) {
    ElMessage.error('密码不可为空')
    return false
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码必须保持一致')
    return false
  }
  if (!form.phone) {
    ElMessage.error('手机号不可为空')
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('手机号格式不正确')
    return false
  }
  if (!form.code) {
    ElMessage.error('请输入短信验证码')
    return false
  }
  if (!/^\d{6}$/.test(form.code)) {
    ElMessage.error('验证码为 6 位数字')
    return false
  }
  return true
}

const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  sendingCode.value = true
  try {
    const res = await sendRegisterCode(form.phone)
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请注意查收')
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) clearInterval(timer)
      }, 1000)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  if (!validateForm()) return
  loading.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password,
      phone: form.phone,
      code: form.code,
      email: form.email || undefined
    })
    if (res.code === 200 || !res.code) {
      ElMessage.success(res.message || '账号注册成功，将带您前往登录页')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.message || '注册未能成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '注册接口请求被阻断')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
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
  width: min(460px, 86vw);
  margin-left: clamp(28px, 4.6vw, 92px);
  margin-top: clamp(24px, 4.5vh, 60px); 
  color: #1f2a44;
  pointer-events: auto;
}

.brand {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(28px, 3vw, 42px);
  font-weight: 700;
  color: #1a2551;
  margin-bottom: 8px;
}

.title {
  margin: 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(36px, 3.8vw, 54px);
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
  font-size: clamp(16px, 1.8vw, 26px);
  font-weight: 700;
  margin-bottom: 6px;
  margin-top: 6px;
}

.field-input {
  width: 100%;
  height: clamp(38px, 4.5vh, 52px);
  border: 1.6px solid #ef9f54;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  padding: 0 16px;
  font-size: clamp(13px, 1.1vw, 16px);
  color: #28334d;
  outline: none;
  box-sizing: border-box;
  margin-bottom: 4px;
}

.field-input::placeholder {
  color: #c9c9c9;
}

.split-inputs {
    display: flex;
    gap: 12px;
}

.input-half {
    flex: 1;
}

.submit-btn {
  margin-top: 24px;
  width: 100%;
  height: clamp(40px, 5.5vh, 64px);
  border-radius: 999px;
  border: none;
  background: #e59448;
  color: #fff;
  font-size: clamp(16px, 1.8vw, 32px);
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s;
}

.submit-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
    opacity: 0.9;
}

.signup-row {
  margin-top: 16px;
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
  font-weight: bold;
  cursor: pointer;
}

/* 必填星号 */
.required-star {
  color: #e53e3e;
  font-size: 0.75em;
  margin-left: 4px;
  vertical-align: middle;
}

/* 手机号 + 发码按钮同行布局 */
.phone-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.phone-input {
  flex: 1;
  margin-bottom: 0;
}

.send-code-btn {
  flex-shrink: 0;
  height: clamp(38px, 4.5vh, 52px);
  padding: 0 14px;
  border: 1.6px solid #ef9f54;
  border-radius: 999px;
  background: rgba(229, 148, 72, 0.12);
  color: #e59448;
  font-size: clamp(12px, 1vw, 14px);
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.send-code-btn:hover:not(:disabled) {
  background: #e59448;
  color: #fff;
}

.send-code-btn:disabled {
  border-color: #d0ccc8;
  color: #b0ada9;
  background: transparent;
  cursor: not-allowed;
}
</style>
