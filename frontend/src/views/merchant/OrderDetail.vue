<template>
    <div class="page-container" v-loading="loading">
        <!-- 页面顶部导航栏 -->
        <div class="page-header">
            <div class="header-left">
                <el-button class="back-btn" @click="goBack" plain>
                    <span class="back-icon">←</span> 返回订单列表
                </el-button>
                <el-divider direction="vertical" class="header-divider" />
                <div class="header-titles">
                    <h2 class="page-title">订单详情</h2>
                    <span class="order-no">订单号：<span class="no-text">{{ detail.orderNo || '加载中...' }}</span></span>
                </div>
            </div>
            <div class="header-right" v-if="detail.status">
                <!-- 待接单按钮 -->
                <el-button 
                    v-if="detail.status === 'PAID'" 
                    type="danger" 
                    round 
                    class="action-btn pulse-anim"
                    @click="handleAccept"
                >
                    <el-icon><Check /></el-icon>&nbsp;立即接单
                </el-button>

                <!-- 完成出餐按钮 -->
                <el-button 
                    v-if="detail.status === 'ACCEPTED'" 
                    type="success" 
                    round 
                    class="action-btn"
                    @click="handleComplete"
                >
                    <el-icon><Select /></el-icon>&nbsp;完成出餐
                </el-button>

                <!-- 退款审核按钮 -->
                <template v-if="detail.status === 'REFUND_PENDING'">
                    <el-button
                        type="success"
                        round
                        class="action-btn pulse-refund"
                        @click="handleAgreeRefund"
                    >
                        <el-icon><CircleCheck /></el-icon>&nbsp;同意退款
                    </el-button>
                    <el-button
                        type="danger"
                        plain
                        round
                        class="action-btn"
                        @click="handleRejectRefund"
                    >
                        <el-icon><CircleClose /></el-icon>&nbsp;拒绝退款
                    </el-button>
                </template>

                <div class="status-banner" :class="'status-' + (detail.status ? detail.status.toLowerCase().replace('_', '-') : '')">
                    {{ formatStatus(detail.status) }}
                </div>
            </div>
        </div>

        <el-row :gutter="24" class="content-row">
            <!-- 左侧：订单摘要卡片 -->
            <el-col :span="8">
                <el-card class="info-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <div class="header-icon-wrapper">
                                <span class="header-emoji">📝</span>
                            </div>
                            <span class="card-title">订单摘要</span>
                        </div>
                    </template>
                    <div class="info-list">
                        <div class="info-row">
                            <span class="info-label">下单用户</span>
                            <span class="info-value user-highlight">
                                <span class="user-avatar-mini">{{ detail.username ? detail.username.charAt(0).toUpperCase() : 'U' }}</span>
                                {{ detail.username || '-' }}
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">下单时间</span>
                            <span class="info-value">{{ detail.createTime || '-' }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">支付状态</span>
                            <span class="info-value">
                                <el-tag :type="paymentStatusType(detail.status)" size="small" effect="light" class="pay-tag">
                                    <span class="dot" :class="paymentStatusType(detail.status)"></span>
                                    {{ paymentStatusText(detail.status) }}
                                </el-tag>
                            </span>
                        </div>
                        <div class="info-row amount-row">
                            <span class="info-label">实付总额</span>
                            <span class="info-value highlight-amount">¥{{ detail.totalAmount || '0.00' }}</span>
                        </div>

                        <el-divider border-style="dashed" class="info-divider" />

                        <div class="remark-section">
                            <div class="remark-header">
                                <span class="remark-icon">💡</span> 
                                <span class="remark-title">买家备注</span>
                            </div>
                            <div class="remark-content" :class="{ 'empty-remark': !detail.remark }">
                                {{ detail.remark || '该订单暂无用户留言或备注信息。' }}
                            </div>
                        </div>

                        <!-- 配送信息区域 -->
                        <template v-if="detail.contactName || detail.deliveryAddress">
                            <el-divider border-style="dashed" class="info-divider" />
                            <div class="delivery-section">
                                <div class="remark-header">
                                    <span class="remark-icon">📍</span>
                                    <span class="remark-title">配送信息</span>
                                </div>
                                <div class="delivery-detail" v-if="detail.contactName">
                                    <span class="delivery-label">收货人：</span>
                                    <span class="delivery-value">{{ detail.contactName }}</span>
                                </div>
                                <div class="delivery-detail" v-if="detail.contactPhone">
                                    <span class="delivery-label">联系电话：</span>
                                    <span class="delivery-value phone-val">{{ detail.contactPhone }}</span>
                                </div>
                                <div class="delivery-detail" v-if="detail.deliveryAddress">
                                    <span class="delivery-label">配送地址：</span>
                                    <span class="delivery-value">{{ detail.deliveryAddress }}</span>
                                </div>
                            </div>
                        </template>

                        <!-- 退款信息区域 -->
                        <template v-if="detail.refundReason || detail.refundRejectReason">
                            <el-divider border-style="dashed" class="info-divider" />
                            <div class="refund-section">
                                <div class="remark-header">
                                    <span class="remark-icon">💸</span>
                                    <span class="remark-title" style="color:#ea580c;">退款信息</span>
                                </div>
                                <div class="refund-detail" v-if="detail.refundReason">
                                    <span class="refund-label">退款原因：</span>
                                    <span class="refund-value">{{ detail.refundReason || '未填写' }}</span>
                                </div>
                                <div class="refund-detail" v-if="detail.refundTime">
                                    <span class="refund-label">申请时间：</span>
                                    <span class="refund-value">{{ detail.refundTime }}</span>
                                </div>
                                <div class="refund-detail" v-if="detail.refundRejectReason">
                                    <span class="refund-label">拒绝原因：</span>
                                    <span class="refund-value" style="color:#ef4444;">{{ detail.refundRejectReason }}</span>
                                </div>
                            </div>
                        </template>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧：商品明细卡片 -->
            <el-col :span="16">
                <el-card class="items-card" shadow="never">
                    <template #header>
                        <div class="card-header items-header">
                            <div class="items-header-left">
                                <div class="header-icon-wrapper">
                                    <span class="header-emoji">🛍️</span>
                                </div>
                                <span class="card-title">商品明细</span>
                            </div>
                            <div class="item-count-badge">
                                共计 {{ totalQuantity }} 件菜品
                            </div>
                        </div>
                    </template>

                    <el-table :data="detail.items || []" style="width: 100%" class="items-table"
                        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '50px' }"
                        :row-style="{ height: '70px' }">
                        
                        <el-table-column label="包含商品" min-width="220">
                            <template #default="{ row }">
                                <div class="product-cell">
                                    <div class="product-avatar">
                                        {{ row.dishName ? row.dishName.charAt(0) : '🍱' }}
                                    </div>
                                    <div class="product-info">
                                        <div class="product-name">{{ row.dishName }}</div>
                                        <div v-if="row.dishTags" class="product-tags">
                                            <span class="spec-tag">{{ row.dishTags }}</span>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>

                        <el-table-column prop="price" label="商品单价" width="130" align="right">
                            <template #default="{ row }">
                                <span class="price-val">¥{{ row.price }}</span>
                            </template>
                        </el-table-column>

                        <el-table-column prop="quantity" label="购买数量" width="110" align="center">
                            <template #default="{ row }">
                                <span class="qty-val">× {{ row.quantity }}</span>
                            </template>
                        </el-table-column>

                        <el-table-column prop="amount" label="本项小计" width="140" align="right">
                            <template #default="{ row }">
                                <span class="amount-val">¥{{ row.amount }}</span>
                            </template>
                        </el-table-column>
                        
                        <template #empty>
                            <div class="empty-list">暂时没有明细数据</div>
                        </template>
                    </el-table>

                    <div class="table-footer">
                        <div class="summary-box">
                            <div class="summary-line">
                                <span class="summary-label">商品总件数：</span>
                                <span class="summary-val">{{ totalQuantity }} 件</span>
                            </div>
                            <div class="summary-line total-line">
                                <span class="summary-label">订单合计支付：</span>
                                <span class="total-amount-val">
                                    <span class="currency-symbol">¥</span>
                                    <span class="big-price">{{ detail.totalAmount || '0.00' }}</span>
                                </span>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMerchantOrderDetail, acceptMerchantOrder, completeMerchantOrder, agreeRefund, rejectRefund } from '@/api/merchantOrder'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Select, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const loading = ref(false)

const loadData = async () => {
    loading.value = true
    try {
        const res = await getMerchantOrderDetail(route.params.id)
        if (res.code === 200) {
            detail.value = res.data || {}
        }
    } finally {
        loading.value = false
    }
}

const goBack = () => {
    router.back()
}

const handleAccept = async () => {
    try {
        await ElMessageBox.confirm('确认立即接单吗？', '提示', { type: 'warning' })
        const res = await acceptMerchantOrder(route.params.id)
        ElMessage.success('接单成功')
        loadData() // 重新拉取以更新状态
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '接单失败')
        }
    }
}

const handleComplete = async () => {
    try {
        await ElMessageBox.confirm('确认该订单已制作完成吗？', '提示', { type: 'info' })
        const res = await completeMerchantOrder(route.params.id)
        ElMessage.success('订单出餐完成')
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '完成订单失败')
        }
    }
}

const handleAgreeRefund = async () => {
    try {
        await ElMessageBox.confirm(
            '同意该退款申请后，将自动归还库存及用户优惠券，操作不可撤销。',
            '同意退款确认',
            { type: 'warning', confirmButtonText: '确认同意', cancelButtonText: '再想想' }
        )
        const res = await agreeRefund(route.params.id)
        ElMessage.success(res.message || '已同意退款')
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '操作失败')
        }
    }
}

const handleRejectRefund = async () => {
    try {
        const { value: rejectReason } = await ElMessageBox.prompt(
            '请输入拒绝退款的理由（可选，用户将看到此理由）',
            '拒绝退款',
            {
                confirmButtonText: '确认拒绝',
                cancelButtonText: '取消',
                inputPlaceholder: '例如：商品已制作完成，不支持退款',
                inputType: 'textarea',
            }
        )
        const res = await rejectRefund(route.params.id, { rejectReason: rejectReason || '' })
        ElMessage.success(res.message || '已拒绝退款申请')
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '操作失败')
        }
    }
}

const totalQuantity = computed(() => {
    if (!detail.value.items) return 0
    return detail.value.items.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

const formatStatus = (status) => {
    const statusMap = {
        'PENDING_PAYMENT':       '等待买家付款',
        'PAID':                  '买家已付款',
        'ACCEPTED':              '商家已接单',
        'RECEIVED':              '买家已确认收货',
        'REFUND_PENDING':        '⚠️ 退款审核中',
        'REFUNDED':              '已退款',
        'REFUND_REJECTED':       '退款已拒绝',
        'REFUND_ARBITRATING':    '⚖️ 平台仲裁中',
        'ARBITRATION_REJECTED':  '申诉已驳回',
        'COMPLETED':             '订单已圆满完成',
        'CANCELLED':             '订单已被取消'
    }
    return statusMap[status] || status || '加载中...'
}

const paymentStatusType = (status) => {
    if (status === 'PENDING_PAYMENT') return 'warning'
    if (status === 'CANCELLED') return 'info'
    return 'success'
}

const paymentStatusText = (status) => {
    if (status === 'PENDING_PAYMENT') return '未支付等待中'
    if (status === 'CANCELLED') return '已取消交易'
    return '已完成支付'
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.page-container {
    padding: 24px;
    background-color: #f1f5f9;
    min-height: calc(100vh - 84px);
    box-sizing: border-box;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 页面头部 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background: #ffffff;
    padding: 20px 24px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.header-left {
    display: flex;
    align-items: center;
}

.back-btn {
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    color: #475569;
    padding: 8px 16px;
    font-weight: 500;
}
.back-btn:hover {
    color: #3b82f6;
    border-color: #bfdbfe;
    background-color: #eff6ff;
}
.back-icon {
    margin-right: 6px;
    font-size: 14px;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}

.action-btn {
    padding: 8px 24px;
    font-weight: 600;
}

.pulse-anim {
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
    70% { box-shadow: 0 0 0 10px rgba(245, 108, 108, 0); }
    100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}

.header-divider {
    height: 32px;
    margin: 0 20px;
    border-left: 2px solid #e2e8f0;
}

.header-titles {
    display: flex;
    align-items: center;
    gap: 16px;
}

.page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
}

.order-no {
    font-size: 14px;
    color: #64748b;
    background: #f8fafc;
    padding: 4px 12px;
    border-radius: 20px;
    border: 1px solid #e2e8f0;
}

.no-text {
    font-weight: 600;
    color: #334155;
    font-family: monospace;
}

.status-banner {
    padding: 8px 20px;
    border-radius: 30px;
    font-weight: 600;
    font-size: 15px;
    letter-spacing: 0.5px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.status-pending_payment  { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; }
.status-paid             { background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; }
.status-accepted         { background: linear-gradient(135deg, #10b981, #059669); color: white; }
.status-received         { background: linear-gradient(135deg, #14b8a6, #0d9488); color: white; }
.status-refund-pending   { background: linear-gradient(135deg, #f97316, #ea580c); color: white; animation: pulseRefund 2s infinite; }
.status-refunded         { background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: white; }
.status-completed        { background: linear-gradient(135deg, #10b981, #059669); color: white; }
.status-cancelled        { background: linear-gradient(135deg, #94a3b8, #64748b); color: white; }

.pulse-refund { animation: pulseRefund 2s infinite; }
@keyframes pulseRefund {
    0%   { box-shadow: 0 0 0 0 rgba(234, 88, 12, 0.4); }
    70%  { box-shadow: 0 0 0 10px rgba(234, 88, 12, 0); }
    100% { box-shadow: 0 0 0 0 rgba(234, 88, 12, 0); }
}

.refund-section {
    background: #fff7ed;
    border-radius: 10px;
    padding: 16px;
    border: 1px solid #fed7aa;
}

.refund-detail {
    display: flex;
    gap: 8px;
    margin-top: 8px;
    font-size: 14px;
    line-height: 1.6;
}
.refund-label { color: #92400e; font-weight: 600; white-space: nowrap; }
.refund-value { color: #1e293b; }

/* 配送信息区域 */
.delivery-section {
    background: #f0f9ff;
    border-radius: 10px;
    padding: 16px;
    border: 1px solid #bae6fd;
}

.delivery-detail {
    display: flex;
    gap: 8px;
    margin-top: 8px;
    font-size: 14px;
    line-height: 1.6;
    align-items: flex-start;
}
.delivery-label { color: #0369a1; font-weight: 600; white-space: nowrap; flex-shrink: 0; }
.delivery-value { color: #1e293b; line-height: 1.5; }
.phone-val { font-family: monospace; font-size: 15px; font-weight: 600; color: #0c4a6e; letter-spacing: 0.5px; }

/* 卡片通用样式 */
.content-row {
    display: flex;
    align-items: stretch;
}

.info-card, .items-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
    height: 100%;
}

:deep(.el-card__header) {
    padding: 16px 24px;
    border-bottom: 1px solid #f1f5f9;
}

:deep(.el-card__body) {
    padding: 24px;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 10px;
}

.items-header {
    justify-content: space-between;
    width: 100%;
}
.items-header-left {
    display: flex;
    align-items: center;
    gap: 10px;
}

.header-icon-wrapper {
    width: 32px;
    height: 32px;
    background: #f1f5f9;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.header-emoji {
    font-size: 18px;
}

.card-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
}

.item-count-badge {
    background: #eff6ff;
    color: #3b82f6;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    border: 1px solid #bfdbfe;
}

/* 摘要列表样式 */
.info-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
}

.info-label {
    color: #64748b;
    font-size: 14px;
}

.info-value {
    color: #1e293b;
    font-weight: 500;
    font-size: 14px;
}

.user-highlight {
    display: flex;
    align-items: center;
    gap: 8px;
}

.user-avatar-mini {
    width: 24px;
    height: 24px;
    background: #e2e8f0;
    color: #475569;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: bold;
}

.pay-tag {
    border-radius: 12px;
    border: none;
    padding: 0 10px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
}
.dot.success { background-color: #10b981; }
.dot.warning { background-color: #f59e0b; }
.dot.info { background-color: #94a3b8; }

.amount-row {
    margin-top: 8px;
    padding-top: 16px;
    border-top: 1px dashed #e2e8f0;
}

.highlight-amount {
    font-size: 20px;
    font-weight: 700;
    color: #ef4444;
}

.info-divider {
    margin: 24px 0;
}

.remark-section {
    background: #f8fafc;
    border-radius: 10px;
    padding: 16px;
    border: 1px solid #f1f5f9;
}

.remark-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    gap: 6px;
}

.remark-title {
    font-weight: 600;
    color: #475569;
    font-size: 14px;
}

.remark-content {
    color: #334155;
    font-size: 14px;
    line-height: 1.6;
}

.empty-remark {
    color: #94a3b8;
    font-style: italic;
}

/* 表格样式 */
.items-table {
    border-radius: 8px;
    border: 1px solid #f1f5f9;
}

.product-cell {
    display: flex;
    align-items: center;
    gap: 12px;
}

.product-avatar {
    width: 44px;
    height: 44px;
    background: #f1f5f9;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: #64748b;
    font-weight: 600;
}

.product-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.product-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 14px;
}

.spec-tag {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    color: #64748b;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
}

.price-val {
    color: #64748b;
}

.qty-val {
    background: #f1f5f9;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 500;
    color: #475569;
}

.amount-val {
    font-weight: 600;
    color: #1e293b;
}

.empty-list {
    padding: 30px;
    color: #94a3b8;
}

/* 底部结算 */
.table-footer {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
}

.summary-box {
    width: 300px;
    background: #f8fafc;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.summary-line {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.summary-label {
    color: #64748b;
    font-size: 14px;
}

.summary-val {
    color: #334155;
    font-weight: 500;
}

.total-line {
    margin-top: 8px;
    padding-top: 16px;
    border-top: 1px dashed #cbd5e1;
}

.total-line .summary-label {
    color: #1e293b;
    font-weight: 600;
    font-size: 15px;
}

.total-amount-val {
    color: #ef4444;
}

.currency-symbol {
    font-size: 16px;
    font-weight: bold;
    margin-right: 2px;
}

.big-price {
    font-size: 26px;
    font-weight: 800;
}
</style>