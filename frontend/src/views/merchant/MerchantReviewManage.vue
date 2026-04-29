<template>
    <div class="merchant-review-page">
        <div class="page-header">
            <h2 class="page-title"><el-icon><ChatLineRound /></el-icon> 店铺评价管理</h2>
            <div class="shop-selector">
                <span class="selector-label">当前店铺:</span>
                <el-select v-model="selectedMerchantId" placeholder="请选择店铺" style="width: 220px" @change="loadReviews" size="large">
                    <template #prefix><el-icon><Shop /></el-icon></template>
                    <el-option v-for="shop in shops" :key="shop.id" :label="shop.shopName" :value="shop.id" />
                </el-select>
            </div>
        </div>

        <el-card class="main-card" shadow="never" v-loading="loading">
            <el-empty v-if="!selectedMerchantId" description="请先在右上角选择要管理的店铺" :image-size="160" />
            <el-empty v-else-if="reviewList.length === 0" description="该店铺暂无顾客评价" :image-size="160" />

            <div v-else class="review-list">
                <el-card class="review-card" v-for="item in reviewList" :key="item.id" shadow="hover">
                    <div class="review-header">
                        <div class="user-info">
                            <el-avatar :size="48" :src="checkAvatarUrl(item.avatar)">
                                {{ (item.nickname || '匿').charAt(0) }}
                            </el-avatar>
                            <div class="user-meta">
                                <span class="nickname">{{ item.nickname || '匿名用户' }}</span>
                                <span class="review-time">{{ item.createTime }}</span>
                            </div>
                        </div>
                        <el-rate :model-value="item.rating" disabled show-score text-color="#ff9900" size="large" />
                    </div>

                    <p class="review-content">{{ item.content || '（此用户没有填写文字评价）' }}</p>

                    <!-- 追评展示 -->
                    <div class="follow-up-box" v-if="item.followUpContent">
                        <div class="follow-up-header">
                            <span class="follow-up-badge">追加评价</span>
                            <span class="reply-time">{{ item.followUpTime }}</span>
                        </div>
                        <div class="follow-up-text">{{ item.followUpContent }}</div>
                    </div>

                    <!-- 商家回复 -->
                    <div class="merchant-reply" v-if="item.merchantReply">
                        <div class="reply-header">
                            <span class="reply-badge">商家回复</span>
                            <span class="reply-time">{{ item.merchantReplyTime }}</span>
                        </div>
                        <p class="reply-text">{{ item.merchantReply }}</p>
                        <div class="reply-actions">
                            <el-button size="small" type="primary" plain round @click="openReply(item)">
                                <el-icon><EditPen /></el-icon> 修改回复
                            </el-button>
                        </div>
                    </div>
                    <div class="reply-area" v-else>
                        <el-input
                            v-model="replyMap[item.id]"
                            placeholder="回复顾客的评价..."
                            type="textarea"
                            :rows="3"
                            resize="none"
                        />
                        <div class="reply-submit-row">
                            <el-button type="primary" round @click="submitReply(item.id)">
                                <el-icon><Position /></el-icon> 提交回复
                            </el-button>
                        </div>
                    </div>
                </el-card>
            </div>
        </el-card>

        <!-- 修改回复弹窗 -->
        <el-dialog v-model="showReplyDialog" title="修改回复" width="500px" destroy-on-close class="custom-dialog">
            <el-input v-model="editReplyContent" type="textarea" :rows="5" placeholder="请输入对顾客的回复" resize="none" />
            <template #footer>
                <div class="dialog-footer">
                    <el-button round @click="showReplyDialog = false">取 消</el-button>
                    <el-button round type="primary" @click="submitEditReply">保存修改</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyMerchants } from '@/api/dish'
import { getMerchantReviewList, merchantReplyReview } from '@/api/review'
import { ChatLineRound, Shop, EditPen, Position } from '@element-plus/icons-vue'

const loading = ref(false)
const shops = ref([])
const selectedMerchantId = ref(null)
const reviewList = ref([])
const replyMap = reactive({})

const showReplyDialog = ref(false)
const editReplyReviewId = ref(null)
const editReplyContent = ref('')

const loadShops = async () => {
    const res = await getMyMerchants()
    if (res.code === 200) {
        shops.value = res.data || []
        if (shops.value.length > 0) {
            selectedMerchantId.value = shops.value[0].id
            loadReviews()
        }
    }
}

const loadReviews = async () => {
    if (!selectedMerchantId.value) return
    loading.value = true
    try {
        const res = await getMerchantReviewList(selectedMerchantId.value)
        if (res.code === 200) {
            reviewList.value = res.data || []
        }
    } catch (e) {
        ElMessage.error('加载评价失败')
    } finally {
        loading.value = false
    }
}

const submitReply = async (reviewId) => {
    const reply = replyMap[reviewId]
    if (!reply || !reply.trim()) {
        ElMessage.warning('请输入回复内容')
        return
    }
    try {
        const res = await merchantReplyReview(reviewId, reply.trim())
        if (res.code === 200) {
            ElMessage.success('回复成功')
            loadReviews()
        } else {
            ElMessage.error(res.message || '回复失败')
        }
    } catch (e) {
        ElMessage.error(e.message || '回复失败')
    }
}

const openReply = (item) => {
    editReplyReviewId.value = item.id
    editReplyContent.value = item.merchantReply || ''
    showReplyDialog.value = true
}

const submitEditReply = async () => {
    if (!editReplyContent.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
    }
    try {
        const res = await merchantReplyReview(editReplyReviewId.value, editReplyContent.value.trim())
        if (res.code === 200) {
            ElMessage.success('修改成功')
            showReplyDialog.value = false
            loadReviews()
        } else {
            ElMessage.error(res.message || '修改失败')
        }
    } catch (e) {
        ElMessage.error(e.message || '修改失败')
    }
}

const checkAvatarUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}

onMounted(() => {
    loadShops()
})
</script>

<style scoped>
.merchant-review-page {
    padding: 24px;
    animation: fadeIn 0.4s ease;
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
.shop-selector {
    display: flex;
    align-items: center;
    gap: 12px;
}
.selector-label {
    font-size: 14px;
    color: #606266;
    font-weight: bold;
}
.main-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 4px 16px rgba(0,0,0,0.04);
    min-height: 400px;
}
.review-list {
    display: grid;
    gap: 20px;
}
.review-card {
    border-radius: 12px;
    border: 1px solid #ebeef5;
    transition: all 0.3s ease;
}
.review-card:hover {
    box-shadow: 0 8px 24px rgba(0,0,0,0.06);
    transform: translateY(-2px);
}
.review-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
}
.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
}
.user-meta {
    display: flex;
    flex-direction: column;
}
.nickname {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
}
.review-time {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
}
.review-content {
    font-size: 15px;
    color: #303133;
    line-height: 1.6;
    margin: 0 0 16px 0;
}
.follow-up-box {
    background: #fdf6ec;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
}
.follow-up-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}
.follow-up-badge {
    background: #e6a23c;
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
}
.follow-up-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
}
.merchant-reply {
    background: #f0f9eb;
    border-radius: 8px;
    padding: 16px;
}
.reply-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}
.reply-badge {
    background: #67c23a;
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
}
.reply-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
    margin: 0 0 12px 0;
}
.reply-time {
    font-size: 12px;
    color: #909399;
}
.reply-actions {
    display: flex;
    justify-content: flex-end;
}
.reply-area {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 8px;
    margin-top: 16px;
    border: 1px dashed #e4e7ed;
}
.reply-submit-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
}
:deep(.custom-dialog .el-dialog__header) {
    border-bottom: 1px solid #ebeef5;
    margin-right: 0;
    padding-bottom: 20px;
}
:deep(.custom-dialog .el-dialog__title) {
    font-weight: bold;
}
:deep(.custom-dialog .el-dialog__footer) {
    border-top: 1px solid #ebeef5;
    padding-top: 20px;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
