<template>
    <div class="order-detail-page">
        <!-- Floating Back Button -->
        <div class="page-top-nav">
            <button class="back-nav-btn" @click="goBack">
                <el-icon><ArrowLeft /></el-icon> 返回订单列表
            </button>
        </div>

        <div v-if="detail.id" class="detail-content">
            
            <!-- Premium Status Banner -->
            <div class="status-banner" :class="getStatusTheme(detail.status)">
                <div class="status-info">
                    <el-icon class="status-icon"><component :is="getStatusIcon(detail.status)" /></el-icon>
                    <div class="status-text-wrap">
                        <h2 class="status-title">{{ formatStatus(detail.status) }}</h2>
                        <p class="status-desc" v-if="detail.status === 'PENDING_PAYMENT' && countdownText">
                            请在 <strong class="time-highlight">{{ countdownText }}</strong> 内完成支付，超时自动取消
                        </p>
                        <p class="status-desc" v-else-if="detail.status === 'CANCELLED'">订单已取消，如需购买请重新下单</p>
                        <p class="status-desc" v-else-if="detail.status === 'COMPLETED'">感谢对商家的支持，期待您的再次光临</p>
                        <p class="status-desc" v-else-if="detail.status === 'ACCEPTED' && detail.refundRejectReason">平台仲裁已驳回退款申请，订单将继续履约，请耐心等待送达</p>
                        <p class="status-desc" v-else-if="detail.status === 'ACCEPTED'">商家已接单，餐品正在飞速准备中，如遇问题可申请退款</p>
                        <p class="status-desc" v-else-if="detail.status === 'PAID'">订单已支付，系统正等待商家接单</p>
                        <p class="status-desc" v-else-if="detail.status === 'RECEIVED'">已确认收货，感谢您的耐心等待，欢迎晒单评价</p>
                        <p class="status-desc" v-else-if="detail.status === 'REFUND_PENDING'">退款申请审核中，请耐心等待商家处理</p>
                        <p class="status-desc" v-else-if="detail.status === 'REFUNDED'">退款已完成，款项将原路返回</p>
                        <p class="status-desc" v-else-if="detail.status === 'REFUND_REJECTED'">商家已拒绝退款，您可申请平台介入进行仲裁</p>
                        <p class="status-desc" v-else-if="detail.status === 'REFUND_ARBITRATING'">平台仲裁处理中，请耐心等待平台裁决</p>
                        <p class="status-desc" v-else>订单状态追踪中...</p>
                    </div>
                </div>
                
                <!-- Action buttons in banner -->
                <div class="banner-actions" v-if="detail.status === 'ACCEPTED' || detail.status === 'RECEIVED' || detail.status === 'REFUND_REJECTED'">
                    <!-- 商家拒绝后：申请平台介入 -->
                    <button v-if="detail.status === 'REFUND_REJECTED'" class="banner-btn btn-escalate-banner" @click="handleEscalate">
                        <el-icon><Warning /></el-icon> 申请平台介入
                    </button>
                    <!-- 进行中状态：退款入口 -->
                    <button v-if="detail.status === 'ACCEPTED' || detail.status === 'RECEIVED'" class="banner-btn btn-refund-banner" @click="handleApplyRefund">
                        <el-icon><RefreshLeft /></el-icon> 申请退款
                    </button>
                </div>
                
                <!-- Background decoration graphic -->
                <el-icon class="bg-decoration"><Wallet v-if="detail.status === 'PENDING_PAYMENT'"/><Van v-else-if="detail.status === 'ACCEPTED'"/><Check v-else/></el-icon>
            </div>

            <div class="info-layout">
                <!-- Left Column: Order Items (Receipt) -->
                <div class="main-col">
                    <div class="premium-card receipt-card">
                        <div class="receipt-header">
                            <div class="shop-name">
                                <el-icon class="shop-icon"><Shop /></el-icon>
                                <span>{{ detail.shopName }}</span>
                            </div>
                        </div>
                        
                        <div class="item-list">
                            <div class="dish-item" v-for="item in detail.items || []" :key="item.dishId">
                                <div class="dish-info">
                                    <h4 class="dish-name">{{ item.dishName }}</h4>
                                    <div v-if="item.dishTags" class="dish-tags">{{ item.dishTags }}</div>
                                </div>
                                <div class="dish-qty">x {{ item.quantity }}</div>
                                <div class="dish-amount">¥{{ item.amount }}</div>
                            </div>
                        </div>
                        
                        <div class="total-section">
                            <div class="total-label">实付总计：</div>
                            <div class="total-price-wrap">
                                <span class="currency">¥</span>
                                <span class="huge-val">{{ detail.totalAmount }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Right Column: Delivery & Order Meta -->
                <div class="side-col">
                    <div class="premium-card detail-card">
                        <div class="card-header">
                            <span class="header-indicator"></span> 配送信息
                        </div>
                        <div class="detail-grid">
                            <div class="grid-item">
                                <span class="label">配送地址：</span>
                                <span class="value">{{ detail.deliveryAddress || '暂无配送地址' }}</span>
                            </div>
                            <div class="grid-item">
                                <span class="label">联系人：</span>
                                <span class="value">{{ detail.contactName || '无' }}</span>
                            </div>
                            <div class="grid-item">
                                <span class="label">联系电话：</span>
                                <span class="value">{{ detail.contactPhone || '无' }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="premium-card detail-card">
                        <div class="card-header">
                            <span class="header-indicator" style="background:#3b82f6;"></span> 订单档案
                        </div>
                        <div class="detail-grid">
                            <div class="grid-item">
                                <span class="label">订单号：</span>
                                <span class="value order-no">{{ detail.orderNo }}</span>
                            </div>
                            <div class="grid-item">
                                <span class="label">下单时间：</span>
                                <span class="value">{{ detail.createTime }}</span>
                            </div>
                            <div class="grid-item">
                                <span class="label">订单备注：</span>
                                <span class="value remark-box">{{ detail.remark || '无备注信息' }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Refund Info Card -->
                <div class="premium-card detail-card" v-if="detail.refundReason || detail.refundRejectReason">
                    <div class="card-header">
                        <span class="header-indicator" style="background:#ea580c;"></span> 退款信息
                    </div>
                    <div class="detail-grid">
                        <div class="grid-item" v-if="detail.refundReason">
                            <span class="label">退款原因：</span>
                            <span class="value remark-box">{{ detail.refundReason }}</span>
                        </div>
                        <div class="grid-item" v-if="detail.refundTime">
                            <span class="label">申请时间：</span>
                            <span class="value">{{ detail.refundTime }}</span>
                        </div>
                        <div class="grid-item" v-if="detail.refundRejectReason">
                            <span class="label">拒绝原因：</span>
                            <span class="value" style="color:#ef4444;">{{ detail.refundRejectReason }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Shop, Clock, Check, Monitor, CircleClose, Warning, Wallet, Van, RefreshLeft, Checked, CreditCard } from '@element-plus/icons-vue'
import { getOrderDetail, applyRefund, escalateToArbitration } from '@/api/order'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const countdownText = ref('')
const timer = ref(null)

const goBack = () => {
    router.back()
}

const calculateCountdown = () => {
    if (detail.value.status !== 'PENDING_PAYMENT' || !detail.value.createTime) {
        countdownText.value = ''
        return
    }

    const now = new Date().getTime()
    const createTime = new Date(detail.value.createTime).getTime()
    const expireTime = createTime + 15 * 60 * 1000 // 15 mins
    const diff = expireTime - now

    if (diff > 0 && !isNaN(diff)) {
        const min = Math.floor(diff / 60000)
        const sec = Math.floor((diff % 60000) / 1000)
        countdownText.value = `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
    } else {
        countdownText.value = '00:00'
    }
}

const loadData = async () => {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200) {
        detail.value = res.data || {}
        calculateCountdown()
    }
}

const formatStatus = (status) => {
    if (status === 'PENDING_PAYMENT')     return '等待支付'
    if (status === 'PAID')                return '已成功支付'
    if (status === 'CANCELLED')           return '交易已取消'
    if (status === 'COMPLETED')           return '订单已完成'
    if (status === 'ACCEPTED')            return '商家已接单'
    if (status === 'RECEIVED')            return '已确认收货'
    if (status === 'REFUND_PENDING')      return '退款审核中'
    if (status === 'REFUNDED')            return '退款已完成'
    if (status === 'REFUND_REJECTED')     return '退款被拒绝'
    if (status === 'REFUND_ARBITRATING')  return '平台仲裁中'
    if (status === 'ARBITRATION_REJECTED') return '申诉已驳回'
    return status
}

const getStatusTheme = (status) => {
    const themeMap = {
        'PENDING_PAYMENT':      'theme-danger',
        'PAID':                 'theme-warning',
        'ACCEPTED':             'theme-primary',
        'RECEIVED':             'theme-teal',
        'REFUND_PENDING':       'theme-orange',
        'REFUNDED':             'theme-purple',
        'REFUND_REJECTED':      'theme-danger',
        'REFUND_ARBITRATING':   'theme-orange',
        'ARBITRATION_REJECTED': 'theme-slate',
        'COMPLETED':            'theme-success',
        'CANCELLED':            'theme-muted'
    }
    return themeMap[status] || 'theme-muted'
}

const getStatusIcon = (status) => {
    const iconMap = {
        'PENDING_PAYMENT':      Clock,
        'PAID':                 Check,
        'ACCEPTED':             Monitor,
        'RECEIVED':             Checked,
        'REFUND_PENDING':       RefreshLeft,
        'REFUNDED':             CreditCard,
        'REFUND_REJECTED':      CircleClose,
        'REFUND_ARBITRATING':   Warning,
        'ARBITRATION_REJECTED': CircleClose,
        'COMPLETED':            Check,
        'CANCELLED':            CircleClose
    }
    return iconMap[status] || Warning
}


const handleEscalate = async () => {
    try {
        await ElMessageBox.confirm(
            '申请平台介入后，将由平台客服进行仲裁，结果将以最终裁决为准。',
            '申请平台介入',
            { confirmButtonText: '确认申请', cancelButtonText: '再想想', type: 'warning' }
        )
        await escalateToArbitration(route.params.id)
        ElMessage.success('已提交平台介入申请，请耐心等待处理')
        loadData()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error(error.message || '申请失败')
    }
}

const handleApplyRefund = async () => {
    try {
        const { value: reason } = await ElMessageBox.prompt(
            `订单号：${detail.value.orderNo}，请输入退款原因（可不填）`,
            '申请退款',
            {
                confirmButtonText: '提交申请',
                cancelButtonText: '再想想',
                inputPlaceholder: '例如：商品质量问题 / 配送时间太长…',
                inputType: 'textarea',
            }
        )
        const res = await applyRefund(route.params.id, { reason: reason || '' })
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '退款申请失败')
        }
    }
}

onMounted(() => {
    loadData()
    timer.value = setInterval(calculateCountdown, 1000)
})

onUnmounted(() => {
    if (timer.value) {
        clearInterval(timer.value)
    }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.order-detail-page {
    animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    max-width: 1100px;
    margin: 0 auto;
    padding: 10px 20px 60px;
}

/* Nav */
.page-top-nav {
    margin-bottom: 20px;
}

.back-nav-btn {
    background: #ffffff;
    color: #475569;
    border: 1px solid #e2e8f0;
    padding: 8px 16px;
    border-radius: 100px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.02);
    transition: all 0.2s ease;
}

.back-nav-btn:hover {
    background: #FFC300;
    border-color: #FFC300;
    color: #222222;
    transform: translateX(-2px);
}

/* Status Banner */
.status-banner {
    position: relative;
    border-radius: 20px;
    padding: 40px;
    margin-bottom: 24px;
    overflow: hidden;
    color: #ffffff;
    box-shadow: 0 8px 24px rgba(0,0,0,0.06);
    display: flex;
    align-items: center;
}

.theme-danger { background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%); }
.theme-warning { background: linear-gradient(135deg, #FFC300 0%, #d97706 100%); color: #222222 !important; }
.theme-primary { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
.theme-success { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
.theme-muted { background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%); }

.status-info {
    position: relative;
    z-index: 2;
    display: flex;
    align-items: center;
    gap: 20px;
}

.status-icon {
    font-size: 56px;
    opacity: 0.95;
    background: rgba(255,255,255,0.2);
    padding: 12px;
    border-radius: 20px;
    backdrop-filter: blur(4px);
}

.theme-warning .status-icon { background: rgba(0,0,0,0.06); color: #222; }

.status-text-wrap { display: flex; flex-direction: column; gap: 8px; }
.status-title { margin: 0; font-size: 28px; font-weight: 800; font-family: 'Plus Jakarta Sans', system-ui; letter-spacing: 0.5px; }
.status-desc { margin: 0; font-size: 14px; font-weight: 500; opacity: 0.9; }
.theme-warning .status-desc { color: #333333; font-weight: 600; }

.time-highlight {
    background: rgba(255,255,255,0.25);
    padding: 2px 8px;
    border-radius: 6px;
    font-family: 'Plus Jakarta Sans', monospace;
    font-size: 15px;
    letter-spacing: 1px;
}

.bg-decoration {
    position: absolute;
    right: 20px;
    top: -20px;
    font-size: 180px;
    opacity: 0.15;
    z-index: 1;
    transform: rotate(15deg);
    pointer-events: none;
}
.theme-warning .bg-decoration { opacity: 0.08; color: #000; }

/* Layout Blocks */
.info-layout {
    display: flex;
    gap: 24px;
    align-items: flex-start;
}
.main-col { flex: 2; min-width: 0; }
.side-col { flex: 1; display: flex; flex-direction: column; gap: 24px; min-width: 320px; }

/* Premium Card Base */
.premium-card {
    background: #ffffff;
    border-radius: 20px;
    border: 1px solid #f1f5f9;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

/* Receipt Display */
.receipt-card { padding: 32px; }

.receipt-header {
    padding-bottom: 20px;
    margin-bottom: 20px;
    border-bottom: 2px dashed #e2e8f0;
}

.shop-name {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 18px;
    font-weight: 800;
    color: #1e293b;
}
.shop-icon { color: #3b82f6; font-size: 20px; }

.item-list { padding-bottom: 20px; }

.dish-item {
    display: flex;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f1f5f9;
}
.dish-item:last-child { border-bottom: none; }

.dish-info { flex: 1; }
.dish-name {
    margin: 0 0 6px 0;
    font-size: 15px;
    font-weight: 700;
    color: #334155;
}
.dish-tags { font-size: 12px; color: #94a3b8; font-weight: 500; }

.dish-qty { width: 60px; text-align: center; color: #64748b; font-size: 14px; font-weight: 600; font-family: 'Plus Jakarta Sans', monospace; }
.dish-amount { width: 80px; text-align: right; font-weight: 700; color: #1e293b; font-size: 15px; }

.total-section {
    padding-top: 24px;
    border-top: 2px solid #f1f5f9;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 12px;
}
.total-label { font-size: 14px; font-weight: 600; color: #64748b; }
.total-price-wrap { color: #e11d48; display: flex; align-items: baseline; }
.total-price-wrap .currency { margin-right: 4px; font-size: 16px; font-weight: 800; }
.total-price-wrap .huge-val { font-size: 32px; font-weight: 800; font-family: 'Plus Jakarta Sans', monospace; letter-spacing: -1px; }

/* Side Detail Cards */
.detail-card { padding: 24px; }
.card-header {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 800;
    color: #1e293b;
    margin-bottom: 20px;
}
.header-indicator {
    display: inline-block;
    width: 4px;
    height: 16px;
    background: #FFC300;
    border-radius: 4px;
    margin-right: 10px;
}

.detail-grid { display: flex; flex-direction: column; gap: 16px; }
.grid-item { display: flex; flex-direction: column; gap: 6px; }
.grid-item .label { font-size: 13px; color: #94a3b8; font-weight: 600; }
.grid-item .value { font-size: 14px; color: #334155; font-weight: 500; word-break: break-all; line-height: 1.5; }
.order-no { font-family: 'Plus Jakarta Sans', monospace; letter-spacing: 0.5px; }

.remark-box {
    background: #f8fafc;
    padding: 10px 14px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    font-size: 13px;
    color: #475569;
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(15px); }
    to { opacity: 1; transform: translateY(0); }
}

/* New status themes */
.theme-teal   { background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%); }
.theme-orange { background: linear-gradient(135deg, #f97316 0%, #ea580c 100%); }
.theme-purple { background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%); }
.theme-slate  { background: linear-gradient(135deg, #64748b 0%, #475569 100%); }

/* 平台介入按鈕 */
.btn-escalate-banner {
    background: rgba(255, 255, 255, 0.25);
    color: #ffffff;
    border: 1px solid rgba(255, 255, 255, 0.5);
    font-weight: 700;
}
.btn-escalate-banner:hover {
    background: rgba(255, 255, 255, 0.4);
    transform: translateY(-2px);
}

/* Banner action buttons */
.status-banner {
    flex-wrap: wrap;
    gap: 20px;
}

.banner-actions {
    position: relative;
    z-index: 2;
    display: flex;
    gap: 12px;
    margin-left: auto;
    flex-shrink: 0;
}

.banner-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    border: none;
    transition: all 0.2s ease;
    font-family: inherit;
    backdrop-filter: blur(4px);
}

.btn-confirm-banner {
    background: rgba(255, 255, 255, 0.25);
    color: #ffffff;
    border: 1px solid rgba(255, 255, 255, 0.4);
}
.btn-confirm-banner:hover {
    background: rgba(255, 255, 255, 0.4);
    transform: translateY(-2px);
}

.btn-refund-banner {
    background: rgba(0, 0, 0, 0.15);
    color: #ffffff;
    border: 1px solid rgba(255, 255, 255, 0.2);
}
.btn-refund-banner:hover {
    background: rgba(0, 0, 0, 0.25);
    transform: translateY(-2px);
}

@media (max-width: 800px) {
    .info-layout { flex-direction: column; }
    .main-col, .side-col { width: 100%; min-width: unset; }
    .banner-actions { margin-left: 0; }
}
</style>