<template>
  <div class="settings-content-wrapper">
    <div class="settings-content-card">
      <div class="section-header">
        <h2 class="section-title">个人信息</h2>
        <p class="section-desc">更新您的个人照片和基本信息。</p>
      </div>

      <el-form :model="profileForm" label-position="top" class="modern-form">
        <div class="avatar-section">
          <div class="avatar-container">
            <el-avatar :size="96" :src="checkAvatarUrl(profileForm.avatar) || defaultAvatar" class="premium-avatar">
              <span class="avatar-fallback">{{ profileForm.nickname?.charAt(0) || profileForm.username?.charAt(0) || 'U' }}</span>
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon :size="24" color="#fff"><Camera /></el-icon>
            </div>
          </div>
          <div class="avatar-info">
            <h3 class="avatar-title">您的头像</h3>
            <p class="avatar-tip">支持 JPG、PNG 或 GIF 格式。最大限制为 2MB。</p>
            <el-upload
              class="avatar-upload"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleAvatarChange"
              accept="image/*"
            >
              <button type="button" class="modern-btn btn-secondary">
                更换头像
              </button>
            </el-upload>
          </div>
        </div>

        <el-divider class="divider-subtle" />

        <div class="form-grid">
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled class="premium-input" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入您的邮箱地址" class="premium-input" />
          </el-form-item>
          <el-form-item label="昵称" class="full-width">
            <el-input v-model="profileForm.nickname" placeholder="请输入您的昵称" maxlength="20" show-word-limit class="premium-input" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <button type="button" class="modern-btn btn-primary" @click="handleUpdateProfile" :disabled="profileLoading">
            <template v-if="profileLoading">
              <el-icon class="is-loading" style="margin-right: 8px"><Loading /></el-icon> 保存中...
            </template>
            <template v-else>
              保存修改
            </template>
          </button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Loading } from '@element-plus/icons-vue'
import { getCurrentUser, updateUserInfo, uploadAvatar } from '@/api/user'
import defaultAvatar from '@/assets/vue.svg'

const profileForm = reactive({
  id: '',
  username: '',
  nickname: '',
  phone: '',
  email: '',
  avatar: ''
})
const profileLoading = ref(false)
const uploadLoading = ref(false)

const checkAvatarUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    if (url.startsWith('file://') || /^[A-Za-z]:/.test(url)) return ''
    return url
}

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    if (res.code === 200) {
      Object.assign(profileForm, res.data)
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

const handleUpdateProfile = async () => {
  profileLoading.value = true
  try {
    const res = await updateUserInfo({
      nickname: profileForm.nickname,
      email: profileForm.email,
      avatar: profileForm.avatar
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    profileLoading.value = false
  }
}

const handleAvatarChange = async (file) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2
  if (!isImage) return ElMessage.error('只能上传图片文件')
  if (!isLt2M) return ElMessage.error('图片大小不能超过2MB')
  
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await uploadAvatar(formData)
    if (res.code === 200) {
      profileForm.avatar = res.data
      ElMessage.success('头像上传成功')
      await handleUpdateProfile()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploadLoading.value = false
  }
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
  max-width: 1100px;
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

/* Avatar Section */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 32px;
}

.avatar-container {
  position: relative;
  border-radius: 50%;
  border: 4px solid #ffffff;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.avatar-container:hover {
  transform: scale(1.02);
}

.premium-avatar {
  display: block;
}

.avatar-fallback {
  font-size: 36px;
  font-weight: 600;
  color: #64748b;
  background: #f1f5f9;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.avatar-tip {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.divider-subtle {
  margin: 32px 0;
  border-color: #f1f5f9;
}

/* Form Grid */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.full-width {
  grid-column: 1 / -1;
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

.premium-input.is-disabled :deep(.el-input__wrapper) {
  background-color: #f1f5f9;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

/* Actions */
.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: flex-end;
}

/* Modern Buttons */
.modern-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 36px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  border: none;
  font-family: inherit;
  height: 48px;
}

.btn-primary {
  background: #FFC300; /* SaaS Dashboard Primary Color - UI/UX Pro Max */
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

.btn-secondary {
  background: #ffffff;
  color: #334155;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-secondary:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
