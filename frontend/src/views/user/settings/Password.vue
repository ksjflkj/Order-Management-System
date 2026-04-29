<template>
  <div class="settings-content-wrapper">
    <div class="settings-content-card">
      <div class="section-header">
        <h2 class="section-title">安全设置</h2>
        <p class="section-desc">修改您的登录密码。请使用包含字母、数字的强密码来保护您的账号安全。</p>
      </div>

      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top" class="modern-form">
        <div class="form-grid">
          <el-form-item label="原密码" prop="oldPassword" class="full-width">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前使用的密码" show-password class="premium-input" />
          </el-form-item>
          
          <el-divider class="divider-subtle full-width" style="margin: 12px 0;" />

          <el-form-item label="新密码" prop="newPassword" class="full-width">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新的登录密码" show-password class="premium-input" />
            <div class="password-hint">密码长度须在6-20个字符之间</div>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword" class="full-width">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password class="premium-input" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <button type="button" class="modern-btn btn-primary" @click="handleChangePassword" :disabled="passwordLoading">
            <template v-if="passwordLoading">
              <span class="loading-spinner"></span> 提交中...
            </template>
            <template v-else>
              确认修改密码
            </template>
          </button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'

const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordLoading = ref(false)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        const res = await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          ElMessage.error(res.message || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}
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
  margin-bottom: 36px;
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

.divider-subtle {
  border-color: #f1f5f9;
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

.password-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
}

/* Actions */
.form-actions {
  margin-top: 40px;
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
  width: 100%;
  height: 52px;
}

.btn-primary {
  background: #FFC300;
  color: #222222;
  box-shadow: 0 4px 12px rgba(255, 195, 0, 0.25);
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
