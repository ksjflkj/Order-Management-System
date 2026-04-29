<template>
    <div class="review-page-container">
        <div class="page-inner">
            <div class="page-header">
                <h2 class="page-title">
                    <div class="icon-wrapper">
                        <el-icon><Comment /></el-icon>
                    </div>
                    我的评价足迹
                </h2>
                <p class="page-desc">记录您体验过的每一道美味，您的声音能帮助更多人。</p>
            </div>

            <div v-if="reviewList.length === 0 && total === 0" class="empty-box premium-card">
                <el-empty description="您还没留下过脚印呢，快去点评一下美味吧~" :image-size="160">
                    <button class="modern-btn btn-primary" @click="$router.push('/user/orders')">查看历史订单</button>
                </el-empty>
            </div>

            <div v-else class="review-cards-list">
                <div class="premium-card review-item" v-for="item in reviewList" :key="item.id">
                    
                    <div class="review-header">
                        <div class="shop-info">
                            <div class="shop-icon-circle"><el-icon><Shop /></el-icon></div>
                            <span class="shop-name">{{ item.shopName }}</span>
                            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                        </div>
                        <div class="review-meta">
                            <el-rate :model-value="item.rating" disabled show-score text-color="#fbbf24" class="custom-rate" />
                            <span class="review-time">{{ item.createTime }}</span>
                        </div>
                    </div>

                    <div class="review-body">
                        <p class="content-text">
                            <span class="quote-mark">"</span>
                            {{ item.content || '此用户没有填写评价内容' }}
                            <span class="quote-mark">"</span>
                        </p>
                    </div>

                    <div class="interaction-blocks" v-if="item.merchantReply || item.followUpContent || !item.followUpContent">
                        
                        <!-- 商家回复区域 -->
                        <div class="reply-card merchant-reply" v-if="item.merchantReply">
                            <div class="block-header">
                                <el-icon class="reply-icon"><Service /></el-icon>
                                <span>商家回应</span>
                                <span class="reply-time">{{ item.merchantReplyTime }}</span>
                            </div>
                            <p class="block-text">{{ item.merchantReply }}</p>
                        </div>

                        <!-- 追评区域 -->
                        <div class="reply-card my-follow-up" v-if="item.followUpContent">
                            <div class="block-header">
                                <el-icon class="reply-icon"><ChatLineRound /></el-icon>
                                <span>我的追评</span>
                                <span class="reply-time">{{ item.followUpTime }}</span>
                            </div>
                            <p class="block-text">{{ item.followUpContent }}</p>
                        </div>
                        
                        <!-- 添加追评区 -->
                        <div class="add-follow-up" v-if="!item.followUpContent">
                            <el-collapse class="custom-collapse">
                                <el-collapse-item name="1">
                                    <template #title>
                                        <div class="add-follow-title">
                                            <el-icon><EditPen /></el-icon> 补充几句体验？
                                        </div>
                                    </template>
                                    <div class="follow-up-form">
                                        <el-input
                                            v-model="followUpMap[item.id]"
                                            placeholder="过了几天，菜品口味/商家服务有新体验吗？"
                                            type="textarea"
                                            :rows="3"
                                            resize="none"
                                            class="premium-input"
                                        />
                                        <div class="form-actions">
                                            <button 
                                                class="modern-btn btn-primary" 
                                                @click="submitFollowUp(item.id)"
                                                :disabled="!followUpMap[item.id]?.trim()"
                                            >
                                                提交追评
                                            </button>
                                        </div>
                                    </div>
                                </el-collapse-item>
                            </el-collapse>
                        </div>

                    </div>
                </div>
            </div>

            <div class="pagination-wrapper" v-if="total > query.size">
                <el-pagination
                    background
                    v-model:current-page="query.current"
                    v-model:page-size="query.size"
                    :page-sizes="[10, 20, 50]"
                    :total="total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange"
                    @current-change="handlePageChange"
                />
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyReviews, followUpReview } from '@/api/review'
import { Comment, Shop, ArrowRight, Service, ChatLineRound, EditPen } from '@element-plus/icons-vue'

const reviewList = ref([])
const query = reactive({ current: 1, size: 10 })
const total = ref(0)
const followUpMap = reactive({})

const loadData = async () => {
    const res = await getMyReviews(query)
    if (res.code === 200) {
        reviewList.value = res.data.records || []
        total.value = res.data.total || 0
    }
}

const handleSizeChange = (val) => {
    query.size = val
    query.current = 1
    loadData()
}

const handlePageChange = (val) => {
    query.current = val
    loadData()
}

const submitFollowUp = async (reviewId) => {
    const content = followUpMap[reviewId]
    if (!content || !content.trim()) {
        ElMessage.warning('请输入追评内容')
        return
    }
    try {
        const res = await followUpReview(reviewId, content.trim())
        if (res.code === 200) {
            ElMessage.success('追评已成功提交')
            followUpMap[reviewId] = ''
            loadData()
        } else {
            ElMessage.error(res.message || '追评提交失败')
        }
    } catch (e) {
        ElMessage.error(e.message || '追评提交发生异常')
    }
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700&display=swap');

.review-page-container {
    padding: 40px 48px;
    background: #f5f7fa;
    min-height: calc(100vh - 60px);
}

.page-inner {
    max-width: 1300px;
    margin: 0 auto;
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.page-header {
    margin-bottom: 40px;
}

.page-title {
    font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
    font-size: 28px;
    font-weight: 700;
    color: #0f172a;
    display: flex;
    align-items: center;
    gap: 16px;
    margin: 0 0 12px 0;
    letter-spacing: -0.02em;
}

.icon-wrapper {
    background: #ffffff;
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14px;
    font-size: 24px;
    color: #222222; /* Accent brand */
    box-shadow: 0 4px 16px rgba(255, 195, 0, 0.15);
}

.page-desc {
    font-size: 15px;
    color: #64748b;
    margin: 0;
}

.premium-card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
    border: 1px solid #f1f5f9;
    padding: 36px 40px;
    margin-bottom: 24px;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.empty-box {
    padding: 80px 0;
}

.review-item:hover {
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
    border-color: #e2e8f0;
}

/* Header Area */
.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px dashed #e2e8f0;
    padding-bottom: 20px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
}

.shop-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 12px;
}

.shop-icon-circle {
    width: 36px;
    height: 36px;
    background: #FFC300;
    color: #222222;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
}

.shop-name {
    font-family: 'Plus Jakarta Sans', system-ui;
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    transition: color 0.2s;
}

.shop-info:hover .shop-name {
    color: #FFC300;
}

.arrow-icon {
    font-size: 14px;
    color: #94a3b8;
}

.review-meta {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
}

.review-time {
    font-size: 13px;
    color: #94a3b8;
    font-family: 'Plus Jakarta Sans', monospace;
    letter-spacing: 0.5px;
}

.custom-rate :deep(.el-rate__text) {
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 700;
}

/* Content Area */
.review-body {
    margin-bottom: 24px;
}

.content-text {
    margin: 0;
    color: #334155;
    font-size: 16px;
    line-height: 1.8;
    position: relative;
    padding: 0 8px;
}

.quote-mark {
    color: #cbd5e1;
    font-family: Georgia, serif;
    font-size: 28px;
    line-height: 0;
    position: relative;
    top: 8px;
}

/* Interaction Blocks */
.interaction-blocks {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.reply-card {
    border-radius: 12px;
    padding: 16px 20px;
    font-size: 14px;
    position: relative;
    overflow: hidden;
}

.reply-card.merchant-reply {
    background: #f8fafc;
    border-left: 4px solid #10b981; /* Green */
}

.reply-card.my-follow-up {
    background: #f4f8fe;
    border-left: 4px solid #FFC300; /* Indigo */
}

.block-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}
.block-header span {
    font-weight: 700;
    font-size: 14px;
}

.merchant-reply .block-header {
    color: #059669;
}
.my-follow-up .block-header {
    color: #E5AF00;
}

.reply-icon {
    font-size: 16px;
}

.reply-time {
    margin-left: auto;
    font-size: 12px;
    font-weight: 500;
    color: #94a3b8;
}

.block-text {
    margin: 0;
    color: #475569;
    line-height: 1.6;
}

/* Add follow up form */
.custom-collapse {
    border-top: none;
    border-bottom: none;
}
.custom-collapse :deep(.el-collapse-item__header) {
    border-bottom: none;
    background: transparent;
    height: auto;
    line-height: inherit;
}
.custom-collapse :deep(.el-collapse-item__wrap) {
    border-bottom: none;
    background: transparent;
}

.add-follow-title {
    color: #64748b;
    font-size: 14px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: #f8fafc;
    border-radius: 8px;
    transition: all 0.2s;
    width: 100%;
}
.add-follow-title:hover {
    color: #FFC300;
    background: #FFF8E1;
}

.follow-up-form {
    padding-top: 16px;
}

.premium-input :deep(.el-textarea__inner) {
    border-radius: 12px;
    padding: 12px 16px;
    background-color: #f8fafc;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    transition: all 0.2s ease;
    font-size: 14px;
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
    margin-top: 16px;
}

/* Buttons */
.modern-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 24px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    border: none;
    height: 40px;
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
    opacity: 0.6;
    background: #94a3b8;
    box-shadow: none;
    transform: none;
    cursor: not-allowed;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 32px;
    padding-bottom: 32px;
}

/* Animations */
@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>