<template>
  <div class="apply-page">
    <div class="apply-container">
      <div class="apply-header">
        <h1 class="title">商家入驻申请</h1>
        <p class="subtitle">Join us and grow your business together</p>
      </div>

      <el-card class="apply-card" shadow="hover">
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules"
          label-position="top"
          size="large"
          class="apply-form"
        >
          <div class="form-row">
            <el-form-item label="店铺名称" prop="shopName" class="flex-1">
              <el-input v-model="form.shopName" placeholder="请输入店铺名称">
                <template #prefix>
                  <el-icon><Shop /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="联系电话" prop="phone" class="flex-1">
              <el-input v-model="form.phone" placeholder="请输入联系电话">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <el-form-item label="店铺地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入店铺详细地址，包含省市区">
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="店铺Logo (URL)" prop="shopLogo">
            <el-input v-model="form.shopLogo" placeholder="请输入 Logo 图片的完整网络地址">
              <template #prefix>
                <el-icon><Picture /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="营业执照 (编号/URL)" prop="businessLicense">
            <el-input v-model="form.businessLicense" placeholder="请输入营业执照编号或扫描件文件地址">
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="店铺描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="4" 
              placeholder="请简单描述您的店铺主营业务、特色等..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <div class="submit-action">
            <el-button 
              type="primary" 
              class="submit-btn" 
              @click="handleSubmit" 
              :loading="isSubmitting"
            >
              提交入驻申请
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { applyMerchant } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { Shop, Phone, Location, Picture, Document } from '@element-plus/icons-vue'

const formRef = ref(null)
const isSubmitting = ref(false)

const form = reactive({
    shopName: '',
    shopLogo: '',
    address: '',
    phone: '',
    description: '',
    businessLicense: ''
})

const rules = reactive({
    shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
    address: [{ required: true, message: '请输入店铺详细地址', trigger: 'blur' }],
    businessLicense: [{ required: true, message: '请输入营业执照信息', trigger: 'blur' }]
})

const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
        if (valid) {
            isSubmitting.value = true
            try {
                const res = await applyMerchant(form)
                ElMessage.success(res.message || '申请提交成功，请等待审核')
                formRef.value.resetFields()
            } catch (error) {
                ElMessage.error(error.message || '申请提交失败')
            } finally {
                isSubmitting.value = false
            }
        }
    })
}
</script>

<style scoped>
.apply-page {
    min-height: calc(100vh - 60px);
    background-color: #f5f7fa;
    display: flex;
    justify-content: center;
    padding: 40px 20px;
    animation: fadeIn 0.5s ease;
}

.apply-container {
    width: 100%;
    max-width: 800px;
}

.apply-header {
    text-align: center;
    margin-bottom: 30px;
}

.title {
    font-size: 32px;
    font-weight: 700;
    color: #303133;
    margin: 0 0 10px 0;
}

.subtitle {
    font-size: 16px;
    color: #909399;
    margin: 0;
    letter-spacing: 1px;
}

.apply-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
    padding: 20px 10px;
}

.apply-form {
    padding: 0 20px;
}

.form-row {
    display: flex;
    gap: 24px;
}

.flex-1 {
    flex: 1;
}

:deep(.el-form-item__label) {
    font-weight: 600;
    color: #303133;
    padding-bottom: 8px;
}

:deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e4e7ed inset;
    border-radius: 8px;
    padding: 4px 15px;
    transition: all 0.3s;
}

:deep(.el-textarea__inner) {
    border-radius: 8px;
    padding: 12px 15px;
    font-family: inherit;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
    box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 1px #FFC300 inset;
}

.submit-action {
    display: flex;
    justify-content: center;
    margin-top: 40px;
}

.submit-btn {
    width: 240px;
    height: 50px;
    font-size: 18px;
    border-radius: 25px;
    background: linear-gradient(135deg, #FFC300, #3a8ee6);
    border: none;
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
    transition: all 0.3s ease;
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
    .form-row {
        flex-direction: column;
        gap: 0;
    }
}
</style>