<template>
    <div class="page-container" v-loading="loading">
        <!-- 页面顶部导航与状态条 -->
        <div class="page-header">
            <div class="header-left">
                <el-button class="back-btn" @click="goBack" plain>
                    <span class="back-icon">←</span> 退回订单大盘
                </el-button>
                <el-divider direction="vertical" class="header-divider" />
                <div class="header-titles">
                    <h2 class="page-title">平台订单全景详情</h2>
                    <span class="order-no">单号指纹指引：<span class="no-text">{{ detail.orderNo || '加载中...' }}</span></span>
                </div>
            </div>
            <div class="header-right" v-if="detail.status">
                <!-- 仲裁操作区：仅当的REFUND_ARBITRATING状态时显示 -->
                <div class="arb-group" v-if="detail.status === 'REFUND_ARBITRATING'">
                    <el-button type="success" size="small" round class="arb-btn arb-approve" @click="handleArbitration(true)">
                        支持退款
                    </el-button>
                    <el-button type="danger" size="small" plain round class="arb-btn arb-reject" @click="handleArbitration(false)">
                        驳回申诉
                    </el-button>
                </div>
                <div class="status-banner" :class="'status-' + (detail.status ? detail.status.toLowerCase() : '')">
                    <span class="status-indicator"></span>
                    {{ formatStatus(detail.status) }}
                </div>
            </div>
        </div>

        <!-- 顶部核心信息三区块 -->
        <el-row :gutter="20" class="core-info-row">
            <!-- 交易流水摘要 -->
            <el-col :span="8">
                <div class="core-card amount-card">
                    <div class="card-icon">💰</div>
                    <div class="card-content">
                        <div class="card-label">订单实收总额</div>
                        <div class="card-value highlight-money">
                            <span class="symbol">¥</span>{{ detail.totalAmount || '0.00' }}
                        </div>
                        <div class="card-footer">
                            创建于: {{ detail.createTime || '-' }}
                        </div>
                    </div>
                </div>
            </el-col>

            <!-- 买家信息池 -->
            <el-col :span="8">
                <div class="core-card user-card">
                    <div class="card-icon">👤</div>
                    <div class="card-content">
                        <div class="card-label">支付/买方账号</div>
                        <div class="card-value">{{ detail.username || '匿名买家' }}</div>
                        <div class="card-footer">
                            联系人: {{ detail.contactName || '未留名' }} &nbsp;|&nbsp; 电话: {{ detail.contactPhone || '未留' }}
                        </div>
                    </div>
                </div>
            </el-col>

            <!-- 卖家承接方 -->
            <el-col :span="8">
                <div class="core-card shop-card">
                    <div class="card-icon">🏪</div>
                    <div class="card-content">
                        <div class="card-label">履约/卖方商铺</div>
                        <div class="card-value">{{ detail.shopName || '未知商铺' }}</div>
                        <div class="card-footer">
                            责任归属商铺，由平台进行仲裁与监督
                        </div>
                    </div>
                </div>
            </el-col>
        </el-row>

        <el-row :gutter="24" class="content-row">
            <!-- 左侧：物流与备注卡片 -->
            <el-col :span="8">
                <el-card class="side-card" shadow="never">
                    <template #header>
                        <div class="section-header">
                            <span class="header-emoji">📍</span>
                            <span class="section-title">履约派送信息</span>
                        </div>
                    </template>
                    <div class="delivery-box">
                        <div class="delivery-label">最终配送地址</div>
                        <div class="delivery-address" :class="{ 'empty-address': !detail.deliveryAddress }">
                            {{ detail.deliveryAddress || '该用户未留下明确的配送地址信息，请核实系统设置。' }}
                        </div>
                    </div>

                    <el-divider border-style="dashed" />

                    <div class="section-header remark-header">
                        <span class="header-emoji">💡</span>
                        <span class="section-title">买家叮嘱/备注</span>
                    </div>
                    <div class="remark-box" :class="{ 'empty-remark': !detail.remark }">
                        "{{ detail.remark || '该订单目前暂无用户的特殊叮嘱或偏好备注。' }}"
                    </div>

                    <!-- 退款信息区块 -->
                    <template v-if="detail.refundReason || detail.refundRejectReason">
                        <el-divider border-style="dashed" />
                        <div class="section-header remark-header">
                            <span class="header-emoji">💸</span>
                            <span class="section-title" style="color:#ea580c;">退款纠纷记录</span>
                        </div>
                        <div class="refund-info-box">
                            <div class="refund-row" v-if="detail.refundReason">
                                <span class="refund-tag user-tag">用户</span>
                                <div class="refund-content">
                                    <div class="refund-label-text">退款原因</div>
                                    <div class="refund-value-text">{{ detail.refundReason }}</div>
                                </div>
                            </div>
                            <div class="refund-row meta-row" v-if="detail.refundTime">
                                <span class="refund-meta-label">申请时间：</span>
                                <span class="refund-meta-value">{{ detail.refundTime }}</span>
                            </div>
                            <div class="refund-row" v-if="detail.refundRejectReason">
                                <span class="refund-tag merchant-tag">商家/平台</span>
                                <div class="refund-content">
                                    <div class="refund-label-text">拒绝 / 裁决说明</div>
                                    <div class="refund-value-text reject-text">{{ detail.refundRejectReason }}</div>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-card>

            </el-col>

            <!-- 右侧：商品快照列表 -->
            <el-col :span="16">
                <el-card class="items-card" shadow="never">
                    <template #header>
                        <div class="section-header items-header">
                            <div class="items-header-left">
                                <span class="header-emoji">🛒</span>
                                <span class="section-title">订单交易快照 (商品列表)</span>
                            </div>
                            <div class="item-count-badge">
                                订单共包含 {{ totalQuantity }} 件商品
                            </div>
                        </div>
                    </template>

                    <el-table :data="detail.items || []" style="width: 100%" class="custom-table"
                        :header-cell-style="{ background: '#f8fafc', color: '#64748b', fontWeight: '600', height: '48px' }"
                        :row-style="{ height: '65px' }">
                        
                        <el-table-column label="商品溯源 (名称/规格)" min-width="200">
                            <template #default="{ row }">
                                <div class="item-cell">
                                    <div class="item-avatar">
                                        {{ row.dishName ? row.dishName.charAt(0) : '件' }}
                                    </div>
                                    <div class="item-info">
                                        <div class="item-name">{{ row.dishName }}</div>
                                        <div v-if="row.dishTags" class="item-tags">{{ row.dishTags }}</div>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        
                        <el-table-column prop="price" label="买方入手价" width="130" align="right">
                            <template #default="{ row }">
                                <span class="price-val">¥{{ row.price }}</span>
                            </template>
                        </el-table-column>
                        
                        <el-table-column prop="quantity" label="交易量" width="100" align="center">
                            <template #default="{ row }">
                                <span class="qty-val">x {{ row.quantity }}</span>
                            </template>
                        </el-table-column>
                        
                        <el-table-column prop="amount" label="分项结算款" width="130" align="right">
                            <template #default="{ row }">
                                <span class="amount-val">¥{{ row.amount }}</span>
                            </template>
                        </el-table-column>
                        
                        <template #empty>
                            <div class="empty-list">未抓取到商品行数据</div>
                        </template>
                    </el-table>

                    <div class="table-footer">
                        <div class="receipt-box">
                            <div class="receipt-row">
                                <span>商品总件数:</span>
                                <span class="receipt-val">{{ totalQuantity }} 件</span>
                            </div>
                            <el-divider border-style="dashed" style="margin: 12px 0;" />
                            <div class="receipt-row total-row">
                                <span>财务应结订单总额:</span>
                                <span class="receipt-total">¥<span class="big-num">{{ detail.totalAmount || '0.00' }}</span></span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrderDetail, resolveArbitration } from '@/api/adminOrder'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const loading = ref(false)

const loadData = async () => {
    loading.value = true
    try {
        const res = await getAdminOrderDetail(route.params.id)
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

const totalQuantity = computed(() => {
    if (!detail.value.items) return 0
    return detail.value.items.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

const formatStatus = (status) => {
    const statusMap = {
        'PENDING_PAYMENT':       '资金未到账(待支付)',
        'PAID':                  '资金已锁定(已支付)',
        'ACCEPTED':              '商户履约中(已接单)',
        'RECEIVED':              '买家已确认收货',
        'COMPLETED':             '交易合规完结(已完成)',
        'REFUND_PENDING':        '退款仒裁中(申请中)',
        'REFUNDED':              '资金已退还(已退款)',
        'REFUND_REJECTED':       '商家拒绝退款',
        'REFUND_ARBITRATING':    '⚖️ 平台仲裁中',
        'ARBITRATION_REJECTED':  '申诉已驳回(终结)',
        'CANCELLED':             '流程节点终止(已取消)'
    }
    return statusMap[status] || status || '数据读取中...'
}

onMounted(() => {
    loadData()
})

const handleArbitration = async (approve) => {
    const actionText = approve ? '支持退款' : '驳回申诉'
    try {
        const { value: note } = await ElMessageBox.prompt(
            approve
                ? '确认平台支持用户退款？将归还库存及优惠券，操作不可撤销。'
                : '确认驳回用户申诉？用户将无法再次申诉，请谨慎操作。',
            `仲裁裁决 — ${actionText}`,
            {
                confirmButtonText: `确认${actionText}`,
                cancelButtonText: '再想想',
                type: approve ? 'success' : 'warning',
                inputPlaceholder: '裁决说明（可不填）',
                inputType: 'textarea'
            }
        )
        await resolveArbitration(route.params.id, { approve, note: note || '' })
        ElMessage.success(approve ? '已支持退款，款项将原路退回' : '已驳回申诉，订单纠纷终结')
        loadData()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
    }
}
</script>

<style scoped>
.page-container {
    padding: 24px;
    background-color: #f1f5f9;
    min-height: calc(100vh - 84px);
    box-sizing: border-box;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 顶部 Header */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #ffffff;
    padding: 20px 24px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    margin-bottom: 24px;
}

.header-left {
    display: flex;
    align-items: center;
}

.back-btn {
    border-radius: 8px;
    font-weight: 500;
}
.back-icon { margin-right: 6px; font-weight: bold; }

.header-divider { height: 32px; margin: 0 20px; border-left: 2px solid #e2e8f0; }

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
    font-size: 13px;
    color: #64748b;
    background: #f8fafc;
    padding: 4px 12px;
    border-radius: 20px;
    border: 1px solid #e2e8f0;
}

.no-text {
    font-weight: 700;
    color: #334155;
    font-family: monospace;
    font-size: 14px;
}

/* 右上角状态条 */
.status-banner {
    padding: 8px 18px;
    border-radius: 30px;
    font-weight: 600;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.status-indicator {
    width: 8px; height: 8px; border-radius: 50%;
    background: rgba(255,255,255,0.8);
    box-shadow: 0 0 8px rgba(255,255,255,0.8);
}

.status-pending_payment    { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; }
.status-paid               { background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; }
.status-accepted           { background: linear-gradient(135deg, #10b981, #059669); color: white; }
.status-received           { background: linear-gradient(135deg, #06b6d4, #0891b2); color: white; }
.status-completed          { background: linear-gradient(135deg, #78eb91, #52e259); color: #166534; }
.status-refund_pending     { background: linear-gradient(135deg, #f97316, #ea580c); color: white; }
.status-refunded           { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; }
.status-refund_rejected    { background: linear-gradient(135deg, #dc2626, #b91c1c); color: white; }
.status-refund_arbitrating { background: linear-gradient(135deg, #f06666, #eb856c); color: white; }
.status-arbitration_rejected { background: linear-gradient(135deg, #64748b, #475569); color: white; }
.status-cancelled          { background: linear-gradient(135deg, #94a3b8, #64748b); color: white; }

.arb-btn { padding: 8px 20px; font-weight: 600; }

/* Header-right 横向对齐 */
.header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
}

/* 仲裁操作按鈕组 */
.arb-group {
    display: flex;
    gap: 8px;
    align-items: center;
    background: rgba(139, 92, 246, 0.06);
    border: 1px solid rgba(139, 92, 246, 0.2);
    padding: 6px 10px;
    border-radius: 12px;
}

.arb-approve,
.arb-reject {
    font-weight: 600;
    font-size: 13px;
}

/* 核心信息横幅区 */
.core-info-row {
    margin-bottom: 24px;
}

.core-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
    border: 1px solid #f1f5f9;
    height: 100%;
    box-sizing: border-box;
}

.card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
}

.amount-card .card-icon { background: #fee2e2; color: #ef4444; }
.user-card .card-icon { background: #e0e7ff; color: #4f46e5; }
.shop-card .card-icon { background: #ffedd5; color: #ea580c; }

.card-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 6px;
    overflow: hidden;
}

.card-label {
    font-size: 13px;
    color: #64748b;
    font-weight: 500;
}

.card-value {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.highlight-money {
    color: #ef4444;
    font-size: 22px;
}

.highlight-money .symbol {
    font-size: 14px;
    margin-right: 2px;
}

.card-footer {
    font-size: 12px;
    color: #94a3b8;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* 主体下部布局 */
.content-row {
    display: flex;
    align-items: stretch;
}

.side-card, .items-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
    height: 100%;
}

:deep(.el-card__header) {
    padding: 16px 24px;
    border-bottom: 1px solid #f1f5f9;
}

:deep(.el-card__body) {
    padding: 24px;
}

.section-header {
    display: flex;
    align-items: center;
    gap: 8px;
}

.header-emoji {
    font-size: 18px;
}

.section-title {
    font-size: 16px;
    font-weight: 700;
    color: #334155;
}

/* 配送与备注区块 */
.delivery-box {
    margin-bottom: 20px;
}

.delivery-label {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 8px;
}

.delivery-address {
    font-size: 15px;
    color: #1e293b;
    line-height: 1.6;
    font-weight: 500;
    background: #f8fafc;
    padding: 16px;
    border-radius: 8px;
    border-left: 4px solid #3b82f6;
}

.empty-address {
    color: #94a3b8;
    border-left-color: #cbd5e1;
    font-style: italic;
}

.remark-header {
    margin-top: 24px;
    margin-bottom: 12px;
}

.remark-box {
    font-size: 14px;
    color: #475569;
    line-height: 1.6;
    background: #fffbeb;
    padding: 16px;
    border-radius: 8px;
    border: 1px solid #fde68a;
    font-weight: 500;
}

.empty-remark {
    background: #f1f5f9;
    border-color: #e2e8f0;
    color: #94a3b8;
    font-style: italic;
    font-weight: normal;
}

/* 退款纠纷信息区块 */
.refund-info-box {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.refund-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
}

.meta-row {
    font-size: 12px;
    color: #94a3b8;
    padding-left: 4px;
}

.refund-meta-label { font-weight: 600; }
.refund-meta-value { font-family: monospace; }

.refund-tag {
    flex-shrink: 0;
    font-size: 11px;
    font-weight: 700;
    padding: 2px 8px;
    border-radius: 6px;
    margin-top: 3px;
}

.user-tag    { background: #e0e7ff; color: #4338ca; }
.merchant-tag { background: #fee2e2; color: #b91c1c; }

.refund-content { flex: 1; }
.refund-label-text { font-size: 11px; color: #94a3b8; font-weight: 600; margin-bottom: 4px; }
.refund-value-text { font-size: 13px; color: #334155; line-height: 1.5; }
.reject-text { color: #b91c1c; }

/* 表格与快照区域 */
.items-header {
    justify-content: space-between;
    width: 100%;
}

.items-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.item-count-badge {
    background: #eef2ff;
    color: #4f46e5;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    border: 1px solid #c7d2fe;
}

.custom-table {
    border-radius: 8px;
    border: 1px solid #f1f5f9;
}

.item-cell {
    display: flex;
    align-items: center;
    gap: 12px;
}

.item-avatar {
    width: 40px;
    height: 40px;
    background: #f1f5f9;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #64748b;
    font-weight: bold;
}

.item-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.item-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 14px;
}

.item-tags {
    font-size: 12px;
    color: #64748b;
    background: #f8fafc;
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
    border: 1px solid #e2e8f0;
}

.price-val { color: #64748b; font-size: 14px; }
.qty-val {
    background: #f1f5f9;
    padding: 2px 8px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 500;
    color: #475569;
}
.amount-val { font-weight: 600; color: #1e293b; font-size: 14px; }

.empty-list {
    padding: 30px;
    color: #94a3b8;
}

/* 收据合计区域 */
.table-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;
}

.receipt-box {
    width: 320px;
    background: #f8fafc;
    border-radius: 8px;
    padding: 20px;
    border: 1px solid #e2e8f0;
}

.receipt-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    color: #64748b;
}

.receipt-val {
    color: #334155;
    font-weight: 600;
}

.total-row {
    color: #1e293b;
    font-weight: 700;
    font-size: 15px;
}

.receipt-total {
    color: #ef4444;
}

.receipt-total .big-num {
    font-size: 24px;
    font-weight: 800;
    margin-left: 2px;
}
</style>