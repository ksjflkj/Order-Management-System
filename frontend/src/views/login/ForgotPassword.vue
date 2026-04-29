<template>
  <div class="scroll-container">
    <div class="vector-wrapper">
      <div class="Pixso-vector"></div>
      <section class="panel">
        <div class="inner">
          <!-- Step 1: 输入手机号 + 验证码 -->
          <template v-if="step === 1">
            <div class="brand">Aprycot</div>
            <h1 class="title">找回密码</h1>
            <p class="subtitle">通过手机验证码重置您的账号密码。</p>

            <label class="field-label" for="phone">手机号</label>
            <input
              id="phone"
              v-model.trim="form.phone"
              class="field-input"
              type="tel"
              placeholder="请输入注册时的手机号"
              maxlength="11"
            />

            <label class="field-label" for="code" style="margin-top: 8px;">验证码</label>
            <div class="code-row">
              <input
                id="code"
                v-model.trim="form.code"
                class="field-input code-input"
                type="text"
                placeholder="6 位验证码"
                maxlength="6"
              />
              <button
                type="button"
                class="send-btn"
                :disabled="countdown > 0 || sendLoading"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
              </button>
            </div>

            <button class="submit-btn" :disabled="step1Loading" @click="goStep2">
              {{ step1Loading ? '验证中...' : '下一步' }}
            </button>
          </template>

          <!-- Step 2: 设置新密码 -->
          <template v-else>
            <div class="brand">Aprycot</div>
            <h1 class="title">设置新密码</h1>
            <p class="subtitle">手机号 <strong>{{ maskPhone(form.phone) }}</strong> 验证通过。</p>

            <label class="field-label" for="newPassword">新密码</label>
            <input
              id="newPassword"
              v-model.trim="form.newPassword"
              class="field-input"
              type="password"
              placeholder="请输入新密码（至少 6 位）"
              autocomplete="new-password"
            />

            <label class="field-label" for="confirmPassword" style="margin-top: 8px;">确认密码</label>
            <input
              id="confirmPassword"
              v-model.trim="form.confirmPassword"
              class="field-input"
              type="password"
              placeholder="请再次输入新密码"
              autocomplete="new-password"
            />

            <button class="submit-btn" :disabled="resetLoading" @click="handleReset">
              {{ resetLoading ? '重置中...' : '确认重置' }}
            </button>
          </template>

          <p class="back-row">
            <button type="button" class="back-link" @click="router.push('/login')">← 返回登录</button>
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendResetCode, resetPassword } from '@/api/auth'

const router = useRouter()
const step = ref(1)
const countdown = ref(0)
const sendLoading = ref(false)
const step1Loading = ref(false)
const resetLoading = ref(false)
let timer = null

const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    if (countdown.value <= 0) {
      clearInterval(timer)
      return
    }
    countdown.value--
  }, 1000)
}

const handleSendCode = async () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  sendLoading.value = true
  try {
    const res = await sendResetCode(form.phone)
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请注意查收')
      startCountdown()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sendLoading.value = false
  }
}

const goStep2 = () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!form.code || form.code.length !== 6) {
    ElMessage.warning('请输入 6 位验证码')
    return
  }
  // 验证码的真正校验交给后端的 reset-password 接口，前端此处仅做完整性检查
  step.value = 2
}

const handleReset = async () => {
  if (!form.newPassword || form.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  resetLoading.value = true
  try {
    const res = await resetPassword({
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功！请使用新密码登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '重置失败')
      // 验证码错误时退回第一步重新获取
      step.value = 1
      form.code = ''
    }
  } catch (e) {
    ElMessage.error(e.message || '重置失败')
    step.value = 1
    form.code = ''
  } finally {
    resetLoading.value = false
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

.Pixso-vector {
  position: absolute;
  inset: 0;
  background-image: url('@/assets/Vector_2_15501.png');
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
}

.vector-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
}

.panel {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.inner {
  position: relative;
  width: min(420px, 86vw);
  margin-left: clamp(28px, 4.6vw, 92px);
  margin-top: clamp(44px, 7vh, 110px);
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
  font-size: clamp(38px, 3.8vw, 56px);
  font-weight: 700;
  line-height: 1.06;
  color: #1a2551;
}

.subtitle {
  margin: 8px 0 16px;
  color: #b9ab9a;
  font-size: clamp(13px, 1.1vw, 16px);
}

.field-label {
  display: block;
  color: #c4b29f;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(18px, 1.8vw, 30px);
  font-weight: 700;
  margin-bottom: 8px;
}

.field-input {
  width: 100%;
  height: clamp(42px, 5.5vh, 62px);
  border: 1.6px solid #ef9f54;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  padding: 0 20px;
  font-size: clamp(14px, 1.2vw, 18px);
  color: #28334d;
  outline: none;
  box-sizing: border-box;
}

.field-input::placeholder { color: #c9c9c9; }

.code-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.code-input { flex: 1; }

.send-btn {
  flex-shrink: 0;
  height: clamp(42px, 5.5vh, 62px);
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  background: #e59448;
  color: #fff;
  font-size: clamp(13px, 1.1vw, 16px);
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.send-btn:disabled {
  background: #d4c5b3;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  background: #d4833a;
}

.submit-btn {
  margin-top: 20px;
  width: 100%;
  height: clamp(44px, 6.2vh, 68px);
  border-radius: 999px;
  border: none;
  background: #e59448;
  color: #fff;
  font-size: clamp(16px, 1.8vw, 32px);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:disabled {
  background: #d4c5b3;
  cursor: not-allowed;
}

.submit-btn:not(:disabled):hover {
  background: #d4833a;
}

.back-row {
  margin-top: 16px;
  text-align: center;
}

.back-link {
  border: none;
  background: transparent;
  color: #b0b8cc;
  font-size: clamp(12px, 1.1vw, 16px);
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-style: dashed;
  transition: color 0.2s;
}

.back-link:hover { color: #7a87a8; }
</style>
