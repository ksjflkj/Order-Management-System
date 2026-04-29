<template>
    <div class="review-page-container">
        <div class="page-inner">
            <div class="premium-card form-wrap">
                <div class="page-header">
                    <div class="icon-wrapper">
                        <el-icon><Comment /></el-icon>
                    </div>
                    <div class="header-text">
                        <h2 class="page-title">发表您的评价</h2>
                        <p class="page-desc">告诉大家这单点的怎么样，客观的评价能帮助更多食客。</p>
                    </div>
                </div>

                <div class="divider-subtle"></div>

                <el-form ref="reviewFormRef" :model="form" :rules="rules" label-position="top" class="modern-form">
                    <el-form-item label="综合评分" prop="rating" class="rating-item">
                        <el-rate 
                            v-model="form.rating" 
                            size="large" 
                            show-score 
                            text-color="#f59e0b"
                            :colors="['#fcd34d', '#fbbf24', '#f59e0b']" 
                        />
                    </el-form-item>
                    
                    <el-form-item label="评价内容" prop="content">
                        <el-input 
                            v-model="form.content" 
                            type="textarea" 
                            :rows="6" 
                            placeholder="请从菜品口味、包装、配送速度等方面说说您的感受吧..." 
                            class="premium-input"
                            resize="none"
                        />
                    </el-form-item>
                    
                    <div class="form-actions">
                        <button type="button" class="modern-btn btn-secondary" @click="router.back()">暂不评价</button>
                        <button type="button" class="modern-btn btn-primary" @click="handleSubmit">
                            <el-icon style="margin-right: 6px;"><Check /></el-icon> 
                            提交评价
                        </button>
                    </div>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { saveReview } from '@/api/review'
import { ElMessage } from 'element-plus'
import { Comment, Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const reviewFormRef = ref(null)

const rules = reactive({
    rating: [{ required: true, message: '请给出一个基础评分哦', trigger: 'change' }],
    content: [{ required: true, message: '不想随便评价？至少写几个字吧', trigger: 'blur' }]
})

const form = reactive({
    orderId: Number(route.params.id),
    rating: 5,
    content: ''
})

const handleSubmit = async () => {
    if (!reviewFormRef.value) return
    await reviewFormRef.value.validate(async (valid) => {
        if (valid) {
            try {
                const res = await saveReview(form)
                ElMessage.success(res.message || '感谢您的评价！')
                router.push('/user/orders')
            } catch (error) {
                ElMessage.error(error.message || '评价上传失败，请稍后再试')
            }
        }
    })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700&display=swap');

.review-page-container {
    padding: 60px 20px;
    background: #f5f7fa;
    min-height: calc(100vh - 60px);
    display: flex;
    justify-content: center;
    align-items: flex-start;
}

.page-inner {
    width: 100%;
    max-width: 1100px;
    animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.premium-card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
    border: 1px solid #f1f5f9;
    padding: 48px;
}

.page-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
}

.icon-wrapper {
    background: #FFC300;
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 16px;
    font-size: 28px;
    color: #222222; /* Accent pink */
    flex-shrink: 0;
}

.page-title {
    font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
    font-size: 24px;
    font-weight: 700;
    color: #0f172a;
    margin: 0 0 6px 0;
}

.page-desc {
    font-size: 14px;
    color: #64748b;
    margin: 0;
}

.divider-subtle {
    height: 1px;
    background: #f1f5f9;
    margin: 24px 0 32px 0;
    width: 100%;
}

.modern-form :deep(.el-form-item__label) {
    font-weight: 600;
    color: #334155;
    font-size: 15px;
    padding-bottom: 12px;
}

.rating-item :deep(.el-rate) {
    height: auto;
}
.rating-item :deep(.el-rate__icon) {
    font-size: 32px;
}

.premium-input :deep(.el-textarea__inner) {
    border-radius: 12px;
    padding: 16px 20px;
    background-color: #f8fafc;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    transition: all 0.2s ease;
    font-size: 15px;
    line-height: 1.6;
}

.premium-input :deep(.el-textarea__inner:hover) {
    box-shadow: 0 0 0 1px #cbd5e1 inset;
    background-color: #ffffff;
}

.premium-input :deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 3px rgba(255, 195, 0, 0.15), 0 0 0 1px #FFC300 inset;
    background-color: #ffffff;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    margin-top: 40px;
}

/* Modern Buttons */
.modern-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 12px 32px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    border: none;
    font-family: inherit;
    height: 48px;
}

.btn-primary {
    background: #FFC300;
    color: #222222;
    box-shadow: 0 4px 12px rgba(255, 195, 0, 0.25);
    min-width: 140px;
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 195, 0, 0.35);
    background: #E5AF00;
}

.btn-primary:active {
    transform: translateY(0);
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

/* Animations */
@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>