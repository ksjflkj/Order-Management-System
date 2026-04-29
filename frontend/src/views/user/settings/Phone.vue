<template>
  <div class="settings-content-wrapper">
    <div class="settings-content-card">
      <div class="section-header">
        <h2 class="section-title">绑定手机</h2>
        <p class="section-desc">手机号用于登录、找回密码及接收重要通知。</p>
      </div>

      <div class="phone-status-card">
        <div class="phone-icon-wrapper">
          <el-icon :size="24" color="#FFC300"><Iphone /></el-icon>
        </div>
        <div class="phone-details">
          <span class="label">当前绑定手机号</span>
          <span class="value">{{ maskedPhone }}</span>
        </div>
        <div class="status-badge">已绑定</div>
      </div>
      
      <el-divider class="divider-subtle" />

      <h3 class="subsection-title">更改手机号</h3>

      <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" label-position="top" class="modern-form">
        <div class="form-grid">
          <el-form-item label="原手机号" prop="oldPhone" class="full-width">
            <el-input v-model="phoneForm.oldPhone" placeholder="请输入当前绑定的手机号" class="premium-input" />
          </el-form-item>
          
          <el-form-item label="新手机号" prop="phone" class="full-width">
            <el-input v-model="phoneForm.phone" placeholder="请输入新的手机号" class="premium-input" />
          </el-form-item>
          
          <el-form-item label="验证码" prop="code" class="full-width">
            <div class="code-input-group">
              <el-input v-model="phoneForm.code" placeholder="请输入6位验证码" maxlength="6" class="premium-input code-input" />
              <button 
                type="button" 
                class="modern-btn btn-secondary send-code-btn" 
                @click="handleSendCode" 
                :disabled="codeCountdown > 0"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s 后重试` : '获取验证码' }}
              </button>
            </div>
          </el-form-item>
        </div>

        <div class="form-actions">
          <button type="button" class="modern-btn btn-primary" @click="handleUpdatePhone" :disabled="phoneLoading">
            <template v-if="phoneLoading">
              <span class="loading-spinner"></span> 绑定中...
            </template>
            <template v-else>
              立即绑定
            </template>
          </button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Iphone } from '@element-plus/icons-vue'
import { getCurrentUser, sendPhoneCode, updatePhone } from '@/api/user'

const profileForm = reactive({
  phone: ''
})

const phoneFormRef = ref(null)
const phoneForm = reactive({
  oldPhone: '',
  phone: '',
  code: ''
})
const phoneLoading = ref(false)
const codeCountdown = ref(0)

// 手机号脱敏：保留前 3 位和后 4 位，中间替换为 ****
const maskedPhone = computed(() => {
  const phone = profileForm.phone
  if (!phone) return '尚未绑定'
  return phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
})

const phoneRules = {
  oldPhone: [
    { required: true, message: '请输入原手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value === phoneForm.oldPhone) {
          callback(new Error('新手机号不能与原手机号相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    if (res.code === 200) {
      profileForm.phone = res.data.phone
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

const handleSendCode = async () => {
  if (!phoneForm.oldPhone || !/^1[3-9]\d{9}$/.test(phoneForm.oldPhone)) {
    ElMessage.warning('请先填写正确的原手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的新手机号')
    return
  }
  try {
    const res = await sendPhoneCode(phoneForm.phone)
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const handleUpdatePhone = async () => {
  if (!phoneFormRef.value) return
  await phoneFormRef.value.validate(async (valid) => {
    if (valid) {
      phoneLoading.value = true
      try {
        const res = await updatePhone({
          oldPhone: phoneForm.oldPhone,
          phone: phoneForm.phone,
          code: phoneForm.code
        })
        if (res.code === 200) {
          ElMessage.success('手机号修改成功')
          profileForm.phone = phoneForm.phone
          phoneForm.oldPhone = ''
          phoneForm.phone = ''
          phoneForm.code = ''
        } else {
          ElMessage.error(res.message || '绑定失败')
        }
      } catch (error) {
        ElMessage.error('绑定失败')
      } finally {
        phoneLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.settings-content-wrapper {
  animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  padding: 16px;
  display: flex;
  justify-content: center;
  width: 100%;
}

.settings-content-card {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  padding: 48px 56px;
  width: 100%;
  max-width: 900px;
  border: 1px solid #f1f5f9;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.section-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* Phone Status Card */
.phone-status-card {
  background: linear-gradient(145deg, #fdfbfb 0%, #f1f5f9 100%);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.phone-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 195, 0, 0.15);
}

.phone-details {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.phone-details .label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.phone-details .value {
  font-family: 'Plus Jakarta Sans', monospace, sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 1px;
}

.status-badge {
  background: #dcfce7;
  color: #166534;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
}

.divider-subtle {
  margin: 32px 0;
  border-color: #f1f5f9;
}

.subsection-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
}

/* Form Grid */
.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.full-width {
  grid-column: 1 / -1;
  margin-bottom: 0 !important;
}

.code-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  white-space: nowrap;
  min-width: 140px;
  height: 48px;
  font-size: 15px;
}

/* Modern Form & Inputs */
.modern-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 14px;
  padding-bottom: 8px;
  line-height: 1.2;
}

.premium-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 16px;
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.2s ease;
}

.premium-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
  background-color: #ffffff;
}

.premium-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15), 0 0 0 1px #FFC300 inset;
  background-color: #ffffff;
}

.premium-input :deep(.el-input__inner) {
  font-size: 14px;
  color: #0f172a;
}

.premium-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

/* Actions */
.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: flex-start;
}

/* Modern Buttons */
.modern-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  border: none;
  font-family: inherit;
  height: 52px;
}

.btn-primary {
  background: #FFC300;
  color: #222222;
  box-shadow: 0 4px 12px rgba(255, 195, 0, 0.25);
  width: 100%;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 195, 0, 0.35);
  background: #E5AF00;
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #ffffff;
  color: #334155;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  height: 40px;
}

.btn-secondary:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #f1f5f9;
}

.loading-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
