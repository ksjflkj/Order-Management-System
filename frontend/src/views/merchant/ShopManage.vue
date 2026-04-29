<template>
    <div class="shop-manage-page">
        <div class="page-header">
            <h2 class="page-title"><el-icon><Shop /></el-icon> 店铺管理</h2>
            <el-button type="primary" round class="add-btn" @click="openApply">
                <el-icon><Plus /></el-icon> 申请新店铺
            </el-button>
        </div>

        <el-card class="main-card" shadow="never">
            <el-table 
                :data="tableData" 
                style="width: 100%"
                row-class-name="custom-table-row"
                header-cell-class-name="custom-header-cell"
            >
                <template #empty>
                    <el-empty description="暂无店铺数据，快去申请一家吧~" :image-size="120" />
                </template>
                
                <el-table-column label="店铺信息" min-width="250">
                    <template #default="{ row }">
                        <div class="shop-info-cell">
                            <el-image 
                                v-if="row.shopLogo" 
                                :src="row.shopLogo"
                                class="shop-avatar" 
                                fit="cover" 
                            >
                                <template #error>
                                    <div class="image-slot fallback"><el-icon><Picture /></el-icon></div>
                                </template>
                            </el-image>
                            <div v-else class="shop-avatar fallback"><el-icon><Shop /></el-icon></div>
                            <div class="info-content">
                                <div class="shop-name-row">
                                    <span class="shop-name">{{ row.shopName }}</span>
                                    <el-tag type="warning" effect="dark" v-if="row.rating" round class="rating-tag">
                                        <div class="rating-content">
                                            <el-icon><StarFilled /></el-icon>
                                            <span>{{ row.rating.toFixed(1) }}</span>
                                        </div>
                                    </el-tag>
                                </div>
                                <div class="shop-create-time">申请时间: {{ row.createTime }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="联系方式" min-width="180">
                    <template #default="{ row }">
                        <div class="contact-cell">
                            <div class="contact-item"><el-icon><Phone /></el-icon> {{ row.phone }}</div>
                            <div class="contact-item address-text" :title="row.address"><el-icon><Location /></el-icon> {{ row.address }}</div>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="审核状态" width="110" align="center">
                    <template #default="{ row }">
                        <el-tag :type="getStatusType(row.status)" effect="light" round size="large">
                            {{ getStatusText(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="营业状态" width="130" align="center">
                    <template #default="{ row }">
                        <template v-if="row.status === 1">
                            <el-switch
                                v-model="row.isOpen"
                                :active-value="1"
                                :inactive-value="0"
                                active-text="营业"
                                inactive-text="打烊"
                                inline-prompt
                                style="--el-switch-on-color:#10b981; --el-switch-off-color:#94a3b8;"
                                @change="(val) => handleToggleOpen(row, val)"
                            />
                        </template>
                        <span v-else style="color:#c0c4cc;font-size:13px;">—</span>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="160" align="center" fixed="right">
                    <template #default="{ row }">
                        <div class="action-buttons">
                            <el-button plain round size="small" type="primary" @click="openEdit(row)">
                                <el-icon><Edit /></el-icon> 编辑
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 申请新店铺对话框 -->
        <el-dialog 
            v-model="showForm" 
            :title="isEdit ? '编辑店铺信息' : '申请新店铺'" 
            width="600px" 
            destroy-on-close
            class="custom-dialog"
        >
            <el-form 
                ref="formRef" 
                :model="form" 
                :rules="rules" 
                label-position="top"
                size="large"
            >
                <div class="form-row">
                    <el-form-item label="店铺名称" prop="shopName" class="flex-1">
                        <el-input v-model="form.shopName" placeholder="请输入店铺名称">
                            <template #prefix><el-icon><Shop /></el-icon></template>
                        </el-input>
                    </el-form-item>
                    <el-form-item label="联系电话" prop="phone" class="flex-1">
                        <el-input v-model="form.phone" placeholder="请输入联系电话">
                            <template #prefix><el-icon><Phone /></el-icon></template>
                        </el-input>
                    </el-form-item>
                </div>
                
                <el-form-item label="店铺Logo" prop="shopLogo">
                    <!-- 输入框 + 上传按鈕 -->
                    <div class="logo-input-row">
                        <el-input v-model="form.shopLogo" placeholder="请输入带 http/https 的图片直链地址" style="flex:1">
                            <template #prefix><el-icon><Picture /></el-icon></template>
                        </el-input>
                        <el-upload
                            :show-file-list="false"
                            accept="image/*"
                            :before-upload="handleLogoUpload"
                        >
                            <el-button type="primary" plain :loading="uploading">
                                <el-icon><UploadFilled /></el-icon>
                                &nbsp;上传图片
                            </el-button>
                        </el-upload>
                    </div>

                    <!-- 实时预览区域 -->
                    <div class="logo-preview-area">
                        <div v-if="form.shopLogo" class="preview-wrapper">
                            <img
                                :src="resolveImageUrl(form.shopLogo)"
                                class="preview-img"
                                alt="预览"
                                @load="previewOk = true; previewFail = false"
                                @error="previewFail = true; previewOk = false"
                            />
                            <div v-if="previewOk" class="preview-badge ok">✔ 图片可正常显示</div>
                            <div v-if="previewFail" class="preview-badge fail">⚠️ 链接无法显示，可能有防盗链或地址有误</div>
                        </div>
                        <div v-else class="preview-placeholder">
                            <el-icon :size="28" color="#c0c4cc"><Picture /></el-icon>
                            <span>输入图片地址或上传后自动预览</span>
                        </div>

                        <div class="sample-links">
                            <span class="sample-label">示例图片（点击使用）：</span>
                            <span
                                v-for="s in sampleImages"
                                :key="s.url"
                                class="sample-chip"
                                @click="form.shopLogo = s.url"
                            >{{ s.label }}</span>
                        </div>
                    </div>
                </el-form-item>
                
                <el-form-item label="详细地址" prop="address">
                    <el-input v-model="form.address" placeholder="请输入真实的门牌号地址">
                        <template #prefix><el-icon><Location /></el-icon></template>
                    </el-input>
                </el-form-item>
                
                <el-form-item label="营业执照" prop="businessLicense">
                    <el-input v-model="form.businessLicense" placeholder="营业执照编号或扫描件URL" :disabled="isEdit">
                        <template #prefix><el-icon><Document /></el-icon></template>
                    </el-input>
                </el-form-item>
                
                <el-form-item label="店铺介绍" prop="description">
                    <el-input 
                        v-model="form.description" 
                        type="textarea" 
                        :rows="4" 
                        placeholder="向顾客介绍一下你的店铺吧..." 
                        maxlength="200"
                        show-word-limit
                        resize="none"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button round @click="showForm = false">取 消</el-button>
                    <el-button round type="primary" @click="handleSubmit" :loading="loading">
                        {{ isEdit ? '保存更改' : '提交申请' }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllMyMerchants, applyMerchant, updateMyMerchant, toggleShopOpen } from '@/api/merchant'
import { getToken } from '@/utils/auth'
import { Shop, Plus, Picture, UploadFilled, StarFilled, Phone, Location, Edit, Document } from '@element-plus/icons-vue'

const tableData = ref([])
const showForm = ref(false)
const loading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const previewOk = ref(false)
const previewFail = ref(false)
const uploading = ref(false)

// 将 /static/ 路径拼接为完整服务器地址
const resolveImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}

// 上传店铺Logo
const handleLogoUpload = async (file) => {
    uploading.value = true
    try {
        const formData = new FormData()
        formData.append('file', file)
        const res = await fetch('http://localhost:8080/api/upload/image', {
            method: 'POST',
            headers: { Authorization: `Bearer ${getToken()}` },
            body: formData
        })
        const data = await res.json()
        if (data.code === 200) {
            form.shopLogo = 'http://localhost:8080' + data.data
            previewOk.value = true
            previewFail.value = false
            ElMessage.success('图片上传成功')
        } else {
            ElMessage.error(data.message || '上传失败')
        }
    } catch (e) {
        ElMessage.error('上传失败，请检查网络')
    } finally {
        uploading.value = false
    }
    return false  // 阻止 el-upload 自动上传
}

// 幽默当切换 Logo URL 时重置预览状态
const resetPreview = () => {
    previewOk.value = false
    previewFail.value = false
}

const sampleImages = [
    { label: '米粉小吃', url: 'https://picsum.photos/seed/food1/200/200' },
    { label: '太阳钓鱼', url: 'https://picsum.photos/seed/fish/200/200' },
    { label: '咶嘿咖啡', url: 'https://picsum.photos/seed/cafe/200/200' },
    { label: '花圆蔚饥', url: 'https://picsum.photos/seed/pizza/200/200' },
]

const form = reactive({
    id: null,
    shopName: '',
    shopLogo: '',
    phone: '',
    address: '',
    businessLicense: '',
    description: '',
    status: null
})

const rules = {
    shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
    address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }],
    businessLicense: [{ required: true, message: '请输入营业执照编号', trigger: 'blur' }]
}

const getStatusType = (status) => {
    const map = {
        0: 'warning',   // 待审核
        1: 'success',   // 审核通过（正常）
        2: 'danger',    // 禁用/封停
        3: 'info'       // 已驳回入驻
    }
    return map[status] || 'info'
}

const getStatusText = (status) => {
    const map = {
        0: '待审核',
        1: '已通过',
        2: '已禁用',
        3: '已驳回'
    }
    return map[status] || '未知'
}

const loadData = async () => {
    try {
        const res = await getAllMyMerchants()
        if (res.code === 200) {
            tableData.value = res.data || []
        }
    } catch (error) {
        ElMessage.error('加载失败')
    }
}

const openApply = () => {
    isEdit.value = false
    resetPreview()
    Object.keys(form).forEach(key => {
        form[key] = key === 'status' ? null : ''
    })
    showForm.value = true
}

const openEdit = (row) => {
    isEdit.value = true
    resetPreview()
    form.id = row.id
    form.shopName = row.shopName || ''
    form.shopLogo = row.shopLogo || ''
    form.phone = row.phone || ''
    form.address = row.address || ''
    form.businessLicense = row.businessLicense || ''
    form.description = row.description || ''
    form.status = row.status
    showForm.value = true
}

const handleSubmit = async () => {
    if (!formRef.value) return
    try {
        await formRef.value.validate()
        loading.value = true
        
        if (isEdit.value) {
            const res = await updateMyMerchant(form)
            if (res.code === 200) {
                ElMessage.success('更新成功')
                showForm.value = false
                loadData()
            } else {
                ElMessage.error(res.message || '更新失败')
            }
        } else {
            const res = await applyMerchant(form)
            if (res.code === 200) {
                ElMessage.success('申请提交成功，请等待管理员审核')
                showForm.value = false
                loadData()
            } else {
                ElMessage.error(res.message || '提交失败')
            }
        }
    } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '提交失败')
    } finally {
        loading.value = false
    }
}

const handleToggleOpen = async (row, newVal) => {
    const willOpen = newVal === 1
    const text = willOpen ? '开始营业' : '暂停营业（打烊）'
    try {
        await ElMessageBox.confirm(`确定要${text}吗？`, '营业状态切换', {
            type: 'warning',
            confirmButtonText: '确认',
            cancelButtonText: '取消'
        })
        const res = await toggleShopOpen(row.id, willOpen)
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        // 用户取消，把 switch 视图回滚
        row.isOpen = willOpen ? 0 : 1
        if (error !== 'cancel') {
            ElMessage.error(error.message || '操作失败')
        }
    }
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.shop-manage-page {
    padding: 24px;
    animation: fadeIn 0.5s ease;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.page-title {
    margin: 0;
    font-size: 24px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
}
.add-btn {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    transition: all 0.3s;
}
.add-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
.main-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}
:deep(.custom-header-cell) {
    background-color: #f8f9fa !important;
    color: #606266;
    font-weight: 600;
    border-bottom: 2px solid #ebeef5;
    padding: 12px 0;
}
:deep(.el-table__row) {
    transition: all 0.3s ease;
}
:deep(.el-table__row:hover > td) {
    background-color: #f2f6fc !important;
}

.shop-info-cell {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 8px 0;
}
.shop-avatar {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.fallback {
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 32px;
}
.image-slot {
    width: 100%;
    height: 100%;
}
.info-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 6px;
}
.shop-name-row {
    display: flex;
    align-items: center;
    gap: 8px;
}
.shop-name {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
}
.rating-tag {
    height: 26px;
    padding: 0 12px;
    display: inline-flex;
    align-items: center;
}
.rating-content {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    font-weight: bold;
}
.shop-create-time {
    font-size: 12px;
    color: #909399;
}
.contact-cell {
    display: flex;
    flex-direction: column;
    gap: 6px;
    color: #606266;
    font-size: 14px;
}
.contact-item {
    display: flex;
    align-items: center;
    gap: 6px;
}
.contact-item .el-icon {
    color: #909399;
}
.address-text {
    width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
    align-items: center;
}

.form-row {
    display: flex;
    gap: 24px;
}
.flex-1 {
    flex: 1;
}

/* --- Logo 输入行 --- */
.logo-input-row {
    display: flex;
    gap: 10px;
    align-items: center;
    width: 100%;
}

/* --- Logo 预览区 --- */
.logo-preview-area {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.preview-wrapper {
    position: relative;
    width: 120px;
}
.preview-img {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: 10px;
    border: 2px solid #ebeef5;
    display: block;
}
.preview-badge {
    position: absolute;
    bottom: -8px;
    left: 50%;
    transform: translateX(-50%);
    white-space: nowrap;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 999px;
    font-weight: 600;
}
.preview-badge.ok {
    background: #f0f9eb;
    color: #67c23a;
    border: 1px solid #b3e19d;
}
.preview-badge.fail {
    background: #fef0f0;
    color: #f56c6c;
    border: 1px solid #fbc4c4;
}
.preview-placeholder {
    width: 120px;
    height: 120px;
    border-radius: 10px;
    border: 2px dashed #dcdfe6;
    background: #fafafa;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    color: #c0c4cc;
    font-size: 12px;
}
.sample-links {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 4px;
}
.sample-label {
    font-size: 12px;
    color: #909399;
}
.sample-chip {
    font-size: 12px;
    padding: 3px 10px;
    border-radius: 999px;
    background: #f0f2f5;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #e4e7ed;
}
.sample-chip:hover {
    background: #ecf5ff;
    color: #409eff;
    border-color: #c6e2ff;
}

:deep(.custom-dialog .el-dialog__header) {
    margin-right: 0;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;
}
:deep(.custom-dialog .el-dialog__title) {
    font-weight: 600;
}
:deep(.custom-dialog .el-dialog__footer) {
    border-top: 1px solid #ebeef5;
    padding-top: 20px;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(15px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
