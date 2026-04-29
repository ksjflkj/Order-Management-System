<template>
  <div class="settings-content-wrapper">
    <div class="settings-content-card">
      <div class="section-header">
        <h2 class="section-title">{{ isEdit ? '编辑收货地址' : '新增收货地址' }}</h2>
        <p class="section-desc">为了保证准确送达，请留下您真实的联系方式和详细地址。</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="modern-form"
      >
        <div class="form-section-title">收货人信息</div>
        <div class="form-grid">
          <el-form-item label="姓名" prop="contactName">
            <el-input v-model="form.contactName" placeholder="例如：张三" class="premium-input" />
          </el-form-item>
          <el-form-item label="手机号码" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入 11 位手机号码" class="premium-input" />
          </el-form-item>
        </div>

        <el-divider class="divider-subtle" />

        <div class="form-section-title">配送地址</div>
        <div class="form-grid">
          <el-form-item label="省份" prop="province">
            <el-input v-model="form.province" placeholder="例如：广东省" class="premium-input" />
          </el-form-item>
          <el-form-item label="城市" prop="city">
            <el-input v-model="form.city" placeholder="例如：深圳市" class="premium-input" />
          </el-form-item>
          <el-form-item label="区/县" prop="district">
            <el-input v-model="form.district" placeholder="例如：南山区" class="premium-input" />
          </el-form-item>
          <div class="spacer-empty"></div> <!-- Filler grid spot if needed, handled by CSS mostly -->
          
          <el-form-item label="详细地址" prop="detailAddress" class="full-width">
            <el-input
              v-model="form.detailAddress"
              type="textarea"
              :rows="3"
              placeholder="请输入街道、小区、楼栋及门牌号等"
              class="premium-input"
            />
          </el-form-item>
          <el-form-item label="设为默认选项" class="full-width switch-item">
            <div class="switch-box-content">
              <span class="switch-hint">下单时将优先使用此地址作为收货地址</span>
              <el-switch
                v-model="form.isDefault"
                :active-value="1"
                :inactive-value="0"
                style="--el-switch-on-color: #FFC300;"
              />
            </div>
          </el-form-item>
        </div>

        <div class="form-actions">
          <button type="button" class="modern-btn btn-secondary" @click="goBack">返回列表</button>
          <button type="button" class="modern-btn btn-primary" @click="handleSave" :disabled="saving">
            <template v-if="saving">
              <span class="loading-spinner"></span> 保存中...
            </template>
            <template v-else>
              保存收货地址
            </template>
          </button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAddressList, saveAddress } from '@/api/address'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

// Validation rules using async-validator format
const rules = {
  contactName: [{ required: true, message: '请完整输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请填写联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '暂不支持非正常位数的号码格式', trigger: 'blur' }
  ],
  detailAddress: [{ required: true, message: '楼栋/门牌号等详细地址必填', trigger: 'blur' }]
}

const goBack = () => router.push('/user/address')

const loadForEdit = async (id) => {
  try {
    const res = await getAddressList({ current: 1, size: 100 })
    if (res.code === 200) {
      const target = (res.data.records || []).find(a => a.id == id)
      if (target) {
        Object.assign(form, target)
      }
    }
  } catch (e) {
    ElMessage.error('无法加载该地址的数据')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const res = await saveAddress({ ...form })
        ElMessage.success(res.message || '收货信息已归档')
        router.push('/user/address')
      } catch (error) {
        ElMessage.error(error.message || '提交异常请重试')
      } finally {
        saving.value = false
      }
    }
  })
}

onMounted(() => {
  const editId = route.params.id
  if (editId) {
    form.id = editId
    loadForEdit(editId)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700&display=swap');

.settings-content-wrapper {
  animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
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
  margin-bottom: 40px;
}

.section-title {
  font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.section-desc {
  font-size: 15px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.divider-subtle {
  margin: 32px 0;
  border-color: #f1f5f9;
}

.form-section-title {
  font-size: 15px;
  font-weight: 700;
  color: #475569;
  letter-spacing: 0.5px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-section-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 16px;
  background: #FFC300; /* Accent */
  border-radius: 4px;
}

/* Form Grid */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 32px;
}

.full-width {
  grid-column: 1 / -1;
  margin-bottom: 0 !important;
}

.switch-item :deep(.el-form-item__content) {
  width: 100%;
}

.switch-box-content {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.switch-hint {
  color: #64748b;
  font-size: 14px;
}

/* Modern Form & Inputs */
.modern-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
  padding-bottom: 8px;
  line-height: 1.2;
}

.premium-input :deep(.el-input__wrapper),
.premium-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 10px 16px;
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.2s ease;
}

.premium-input :deep(.el-input__wrapper:hover),
.premium-input :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
  background-color: #ffffff;
}

.premium-input :deep(.el-input__wrapper.is-focus),
.premium-input :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 3px rgba(255, 195, 0, 0.15), 0 0 0 1px #FFC300 inset;
  background-color: #ffffff;
}

.premium-input :deep(.el-input__inner),
.premium-input :deep(.el-textarea__inner) {
  font-size: 15px;
  color: #0f172a;
}
.premium-input :deep(.el-input__inner::placeholder),
.premium-input :deep(.el-textarea__inner::placeholder) {
  color: #94a3b8;
}

/* Actions */
.form-actions {
  margin-top: 48px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
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
  min-width: 160px;
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
  color: #475569;
  border: 1px solid #cbd5e1;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-secondary:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
  margin-right: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
