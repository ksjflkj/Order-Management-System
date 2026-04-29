<template>
    <div class="order-page-container">
        <div class="page-inner">
            <div class="page-header">
                <div class="icon-wrapper">
                    <el-icon><List /></el-icon>
                </div>
                <div class="header-text">
                    <h2 class="page-title">所有订单</h2>
                    <p class="page-desc">按状态有序分类，轻松追踪并管理您的每一次就餐体验。</p>
                </div>
            </div>

            <!-- Sleek Categorization Tabs -->
            <div class="tabs-container">
                <el-tabs v-model="query.status" class="premium-tabs" @tab-change="handleSearch">
                    <el-tab-pane label="全部订单" name=""></el-tab-pane>
                    <el-tab-pane label="待支付" name="PENDING_PAYMENT"></el-tab-pane>
                    <el-tab-pane label="已支付" name="PAID"></el-tab-pane>
                    <el-tab-pane label="退款中" name="REFUND_PENDING"></el-tab-pane>
                    <el-tab-pane label="退款驳回" name="REFUND_REJECTED"></el-tab-pane>
                    <el-tab-pane label="仲裁中" name="REFUND_ARBITRATING"></el-tab-pane>
                    <el-tab-pane label="已退款" name="REFUNDED"></el-tab-pane>
                    <el-tab-pane label="已完成" name="COMPLETED"></el-tab-pane>
                    <el-tab-pane label="已取消" name="CANCELLED"></el-tab-pane>
                </el-tabs>
            </div>

            <div v-if="orderList.length === 0" class="empty-box premium-card">
                <el-empty :description="`您还没有${formatEmptyDesc(query.status)}订单哦`" :image-size="160">
                    <button class="modern-btn btn-primary" @click="$router.push('/user/merchant-list')">去寻觅美食</button>
                </el-empty>
            </div>

            <div v-else class="order-list">
                <div v-for="item in orderList" :key="item.id" class="premium-card order-card">
                    <!-- Header -->
                    <div class="order-card-header">
                        <div class="shop-info">
                            <div class="shop-icon-circle"><el-icon><Shop /></el-icon></div>
                            <span class="shop-name">{{ item.shopName }}</span>
                            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                        </div>
                        <div class="status-badge" :class="item.status.toLowerCase()">
                            {{ formatStatus(item.status) }}
                        </div>
                    </div>

                    <!-- Body -->
                    <div class="order-card-body">
                        <div class="info-details">
                            <div class="info-row">
                                <span class="label">订单流水号</span>
                                <span class="value mono">{{ item.orderNo }}</span>
                            </div>
                            <div class="info-row">
                                <span class="label">收货人信息</span>
                                <span class="value">{{ item.contactName || '姓名未填' }} {{ item.contactPhone }}</span>
                            </div>
                            <div class="info-row">
                                <span class="label">配送至</span>
                                <span class="value address-text">{{ item.deliveryAddress || '地址未填写' }}</span>
                            </div>
                            <div class="info-row" v-if="item.remark">
                                <span class="label">买家备注</span>
                                <span class="value remark-text">{{ item.remark }}</span>
                            </div>
                        </div>
                        <!-- Price Showcase -->
                        <div class="price-showcase">
                            <span class="price-label">实付总金额</span>
                            <div class="price-number">
                                <span class="currency">¥</span>
                                <span class="strong-num">{{ item.totalAmount }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="divider-subtle"></div>

                    <!-- Footer -->
                    <div class="order-card-footer">
                        <div class="time-line">
                            <span class="time-label">下单时间：{{ item.createTime }}</span>
                            <div v-if="item.status === 'PENDING_PAYMENT' && item.countdown" class="timeout-pill pulse-warn">
                                <el-icon><Timer /></el-icon> 支付剩余: {{ item.countdown }}
                            </div>
                        </div>
                        <div class="action-buttons">
                            <button class="modern-btn btn-secondary" @click="goDetail(item.id)">订单详情</button>
                            <button v-if="item.status === 'PENDING_PAYMENT'" class="modern-btn btn-danger-soft" @click="handleCancel(item.id)">取消订单</button>
                            <button v-if="item.status === 'PENDING_PAYMENT'" class="modern-btn btn-primary" @click="handlePay(item.id)">立即支付</button>
                            <button v-if="item.status === 'RECEIVED'" class="modern-btn btn-refund" @click="handleApplyRefund(item)">
                                <el-icon style="margin-right:4px;"><RefreshLeft /></el-icon> 申请退款
                            </button>
                            <span v-if="item.status === 'REFUND_PENDING'" class="refund-pending-tip">
                                <el-icon><Loading /></el-icon> 退款审核中，请等待商家处理
                            </span>
                            <button v-if="item.status === 'COMPLETED'" class="modern-btn btn-success-soft" @click="goReview(item.id)">
                                <el-icon style="margin-right:4px;"><EditPen /></el-icon> 晒单评价
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="pagination-wrapper" v-if="total > query.size">
                <el-pagination
                    v-model:current-page="query.current"
                    v-model:page-size="query.size"
                    :page-sizes="[10, 20, 50]"
                    :total="total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange"
                    @current-change="handlePageChange"
                    background
                />
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, Shop, Timer, ArrowRight, EditPen, Check, RefreshLeft, Loading } from '@element-plus/icons-vue'
import { cancelOrder, getOrderList, payOrder, confirmReceived, applyRefund } from '@/api/order'

const router = useRouter()
const orderList = ref([])
const query = reactive({ current: 1, size: 10, status: '' })
const total = ref(0)
const timer = ref(null)

const calculateCountdowns = () => {
    const now = new Date().getTime()
    let hasPending = false
    
    orderList.value.forEach(item => {
        if (item.status === 'PENDING_PAYMENT') {
            hasPending = true
            const createTime = new Date(item.createTime).getTime()
            const expireTime = createTime + 15 * 60 * 1000 // 15分钟超时
            const diff = expireTime - now
            
            if (diff > 0 && !isNaN(diff)) {
                const min = Math.floor(diff / 60000)
                const sec = Math.floor((diff % 60000) / 1000)
                item.countdown = `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
            } else {
                item.countdown = '00:00'
            }
        }
    })
    
    if (hasPending) {
        orderList.value = [...orderList.value] // Trigger reactivity fully
    }
}

const loadData = async () => {
    const res = await getOrderList(query)
    if (res.code === 200) {
        orderList.value = res.data.records || []
        total.value = res.data.total || 0
        calculateCountdowns()
    }
}

const handleSearch = () => {
    query.current = 1
    loadData()
}

const handleSizeChange = (val) => {
    query.size = val
    handleSearch()
}

const handlePageChange = (val) => {
    query.current = val
    loadData()
}

const goDetail = (id) => {
    router.push(`/user/orders/${id}`)
}

const goReview = (id) => {
    router.push(`/user/orders/${id}/review`)
}

const handlePay = async (id) => {
    try {
        const res = await payOrder(id)
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        ElMessage.error(error.message || '支付渠道通讯失败')
    }
}

const handleCancel = async (id) => {
    try {
        await ElMessageBox.confirm('是否确定要取消该笔订单？', '操作确认', { 
            confirmButtonText: '确定取消',
            cancelButtonText: '我再想想',
            type: 'warning' 
        })
        const res = await cancelOrder(id)
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '取消订单异常')
        }
    }
}

const handleConfirmReceived = async (id) => {
    try {
        await ElMessageBox.confirm('确认已收到货品？确认后无法撤销。', '确认收货', {
            confirmButtonText: '确认收货',
            cancelButtonText: '再想想',
            type: 'success'
        })
        const res = await confirmReceived(id)
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '操作失败')
        }
    }
}

const handleApplyRefund = async (item) => {
    try {
        const { value: reason } = await ElMessageBox.prompt(
            `订单号：${item.orderNo}，请输入退款原因（可不填）`,
            '申请退款',
            {
                confirmButtonText: '提交申请',
                cancelButtonText: '再想想',
                inputPlaceholder: '例如：商品质量问题 / 配送时间太长…',
                inputType: 'textarea',
            }
        )
        const res = await applyRefund(item.id, { reason: reason || '' })
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '退款申请失败')
        }
    }
}

const formatStatus = (status) => {
    if (status === 'PENDING_PAYMENT') return '等待支付'
    if (status === 'PAID') return '已成功付款'
    if (status === 'ACCEPTED') return '商家已火速接单'
    if (status === 'RECEIVED') return '已确认收货'
    if (status === 'REFUND_PENDING') return '退款审核中'
    if (status === 'REFUND_REJECTED') return '退款已驳回'
    if (status === 'REFUND_ARBITRATING') return '仲裁中'
    if (status === 'ARBITRATION_REJECTED') return '申诉已驳回'
    if (status === 'REFUNDED') return '已退款'
    if (status === 'COMPLETED') return '订单已完成'
    if (status === 'CANCELLED') return '订单已取消'
    return status
}

const formatEmptyDesc = (status) => {
    if (status === 'PENDING_PAYMENT') return '待支付的'
    if (status === 'PAID') return '已支付的'
    if (status === 'ACCEPTED') return '商家接单的'
    if (status === 'RECEIVED') return '已收货的'
    if (status === 'REFUND_PENDING') return '退款中的'
    if (status === 'REFUND_REJECTED') return '退款被驳回的'
    if (status === 'REFUND_ARBITRATING') return '仲裁中的'
    if (status === 'ARBITRATION_REJECTED') return '申诉被驳回的'
    if (status === 'REFUNDED') return '已退款的'
    if (status === 'COMPLETED') return '已完成的'
    if (status === 'CANCELLED') return '取消的'
    return '相关的'
}

onMounted(() => {
    loadData()
    timer.value = setInterval(calculateCountdowns, 1000)
})

onUnmounted(() => {
    if (timer.value) {
        clearInterval(timer.value)
    }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.order-page-container {
    padding: 50px 48px;
    background: #f5f7fa;
    min-height: calc(100vh - 60px);
}

.page-inner {
    max-width: 1200px;
    margin: 0 auto;
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.page-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
}

.icon-wrapper {
    background: #ffffff;
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 16px;
    font-size: 28px;
    color: #222222; /* SaaS primary */
    box-shadow: 0 4px 16px rgba(255, 195, 0, 0.15);
    flex-shrink: 0;
}

.page-title {
    font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
    font-size: 28px;
    font-weight: 700;
    color: #0f172a;
    margin: 0 0 6px 0;
    letter-spacing: -0.02em;
}

.page-desc {
    font-size: 15px;
    color: #64748b;
    margin: 0;
}

.tabs-container {
    margin-bottom: 32px;
}

.premium-tabs :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: #e2e8f0;
}
.premium-tabs :deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: 600;
    color: #64748b;
    height: 50px;
    line-height: 50px;
}
.premium-tabs :deep(.el-tabs__item.is-active) {
    color: #222222; font-weight: 700;
}
.premium-tabs :deep(.el-tabs__active-bar) {
    background-color: #FFC300;
    height: 3px;
    border-radius: 3px;
}

.premium-card {
    background: #ffffff;
    border-radius: 24px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
    border: 1px solid #f1f5f9;
}

.empty-box {
    padding: 80px 0;
}

.order-list {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.order-card {
    padding: 24px 32px;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.order-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
    border-color: #e2e8f0;
}

/* Header */
.order-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 16px;
    border-bottom: 1px dashed #e2e8f0;
    margin-bottom: 20px;
}

.shop-info {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
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

/* Status Badges Customization */
.status-badge {
    padding: 4px 14px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 700;
}
.status-badge.pending_payment { background: #fffbeb; color: #d97706; }
.status-badge.paid { background: #eff6ff; color: #2563eb; }
.status-badge.accepted { background: #ecfdf5; color: #059669; }
.status-badge.received { background: #f0fdf4; color: #16a34a; }
.status-badge.refund_pending { background: #fff7ed; color: #ea580c; }
.status-badge.refund_rejected { background: #fef2f2; color: #dc2626; }
.status-badge.refund_arbitrating { background: #f5f3ff; color: #7c3aed; }
.status-badge.arbitration_rejected { background: #f1f5f9; color: #475569; }
.status-badge.refunded { background: #faf5ff; color: #7c3aed; }
.status-badge.completed { background: #f0fdf4; color: #16a34a; }
.status-badge.cancelled { background: #f1f5f9; color: #64748b; }

/* Body Area */
.order-card-body {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 4px 0 12px 0;
}

.info-details {
    display: flex;
    flex-direction: column;
    gap: 12px;
    flex: 1;
    padding-right: 32px;
}

.info-row {
    display: flex;
    align-items: baseline;
    font-size: 15px;
}

.info-row .label {
    width: 80px;
    flex-shrink: 0;
    color: #64748b;
    font-weight: 500;
}

.info-row .value {
    color: #334155;
    font-weight: 600;
}

.info-row .mono {
    font-family: 'Plus Jakarta Sans', monospace;
    color: #94a3b8;
}

.address-text {
    color: #64748b;
    font-weight: 500;
}

.remark-text {
    background: #fff8f1;
    color: #ea580c;
    padding: 2px 10px;
    border-radius: 6px;
    font-size: 13px;
    margin-left: -6px;
}

/* Price block */
.price-showcase {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    justify-content: center;
    background: #FFF8E1; /* Soft pink tint */
    padding: 16px 24px;
    border-radius: 16px;
    min-width: 140px;
    border: 1px solid #FFECB3;
}

.price-label {
    font-size: 13px;
    color: #555555;
    font-weight: 600;
    margin-bottom: 2px;
}

.price-number {
    display: flex;
    align-items: baseline;
    color: #FF4A26; /* Brand Pink strong */
}

.currency {
    font-size: 16px;
    font-weight: 700;
    margin-right: 2px;
}

.strong-num {
    font-family: 'Plus Jakarta Sans', monospace;
    font-size: 30px;
    font-weight: 800;
    letter-spacing: -1px;
}

.divider-subtle {
    height: 1px;
    background: #f1f5f9;
    margin: 16px 0;
    width: 100%;
}

/* Footer Section */
.order-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.time-line {
    display: flex;
    align-items: center;
    gap: 16px;
}

.time-label {
    font-size: 14px;
    color: #94a3b8;
    font-family: 'Plus Jakarta Sans', monospace;
}

.timeout-pill {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 700;
    padding: 4px 12px;
    border-radius: 20px;
}

.pulse-warn {
    background: #fef2f2;
    color: #ef4444;
    border: 1px solid #fee2e2;
    animation: pulseRed 2s infinite;
}

@keyframes pulseRed {
    0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
    70% { box-shadow: 0 0 0 6px rgba(239, 68, 68, 0); }
    100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

/* Actions Buttons */
.action-buttons {
    display: flex;
    gap: 12px;
}

.modern-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 22px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    border: none;
    font-family: inherit;
    height: 40px;
}

.btn-primary {
    background: #FFC300; /* Bold Call to Action */
    color: #222222;
    box-shadow: 0 4px 12px rgba(255, 195, 0, 0.25);
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 195, 0, 0.35);
    background: #E5AF00;
}

.btn-secondary {
    background: #ffffff;
    color: #475569;
    border: 1px solid #cbd5e1;
}

.btn-secondary:hover {
    background: #f8fafc;
    border-color: #94a3b8;
    color: #1e293b;
}

.btn-danger-soft {
    background: #fef2f2;
    color: #ef4444;
}

.btn-danger-soft:hover {
    background: #fee2e2;
}

.btn-success-soft {
    background: #f0fdf4;
    color: #16a34a;
    border: 1px solid #dcfce7;
}

.btn-success-soft:hover {
    background: #dcfce7;
    border-color: #bbf7d0;
    transform: translateY(-1px);
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 40px;
    padding-bottom: 32px;
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.btn-confirm {
    background: #ecfdf5;
    color: #059669;
    border: 1px solid #6ee7b7;
}
.btn-confirm:hover {
    background: #d1fae5;
    border-color: #34d399;
    transform: translateY(-1px);
}

.btn-refund {
    background: #fff7ed;
    color: #ea580c;
    border: 1px solid #fed7aa;
}
.btn-refund:hover {
    background: #ffedd5;
    border-color: #fb923c;
    transform: translateY(-1px);
}

.refund-pending-tip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #ea580c;
    background: #fff7ed;
    padding: 6px 14px;
    border-radius: 20px;
    border: 1px solid #fed7aa;
    animation: pulseOrange 2s infinite;
}

@keyframes pulseOrange {
    0%   { box-shadow: 0 0 0 0 rgba(234, 88, 12, 0.3); }
    70%  { box-shadow: 0 0 0 6px rgba(234, 88, 12, 0); }
    100% { box-shadow: 0 0 0 0 rgba(234, 88, 12, 0); }
}
</style>