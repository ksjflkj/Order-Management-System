<template>
    <div class="merchant-order-container">
        <!-- 顶部统计与搜索区 -->
        <div class="header-section">
            <div class="title-with-icon">
                <el-icon class="title-icon"><List /></el-icon>
                <span class="page-title">订单管理</span>
            </div>
            <div class="search-box">
                <el-input 
                    v-model="searchQuery" 
                    placeholder="输入订单号、用户名、备注..." 
                    class="search-input" 
                    clearable
                    @input="handleSearch"
                >
                    <template #prefix>
                        <el-icon><Search /></el-icon>
                    </template>
                </el-input>
            </div>
        </div>

        <!-- 订单分类 Tabs -->
        <el-card class="main-card" :body-style="{ padding: '0 24px' }">
            <el-tabs v-model="activeTab" class="order-tabs" @tab-change="handleTabChange">
                <el-tab-pane name="PAID">
                    <template #label>
                        <span>待接单</span>
                        <el-badge :value="paidCount" :max="99" class="tab-badge" v-if="paidCount > 0" />
                    </template>
                </el-tab-pane>
                <el-tab-pane name="ACCEPTED">
                    <template #label>
                        <span>出餐中</span>
                        <el-badge :value="acceptedCount" :max="99" type="warning" class="tab-badge" v-if="acceptedCount > 0" />
                    </template>
                </el-tab-pane>
                <el-tab-pane name="REFUND_PENDING">
                    <template #label>
                        <span>退款审核</span>
                        <el-badge :value="refundPendingCount" :max="99" type="danger" class="tab-badge" v-if="refundPendingCount > 0" />
                    </template>
                </el-tab-pane>
                <el-tab-pane label="待支付" name="PENDING_PAYMENT" />


                <el-tab-pane label="已退款" name="REFUNDED" />
                <el-tab-pane label="已完成" name="COMPLETED" />
                <el-tab-pane label="已取消" name="CANCELLED" />
                <el-tab-pane name="ALL">
                    <template #label>
                        <span style="color: #909399">全部订单</span>
                    </template>
                </el-tab-pane>
            </el-tabs>

            <div class="list-container">
                <el-empty v-if="paginatedList.length === 0" description="暂无符合条件的订单" :image-size="160" />

                <div v-else class="order-grid">
                    <el-card 
                        v-for="item in paginatedList" 
                        :key="item.id" 
                        class="order-card"
                        :class="{'is-urgent': item.status === 'PAID'}"
                        shadow="hover"
                        :body-style="{ padding: '0' }"
                    >
                        <div class="card-header">
                            <div class="header-left">
                                <span class="order-time">{{ item.createTime }}</span>
                                <span class="order-no" title="点击复制" @click="copyOrderNo(item.orderNo)">
                                    <el-icon><Ticket /></el-icon> {{ item.orderNo.substring(0, 16) }}...
                                </span>
                            </div>
                            <el-tag :type="statusTagType(item.status)" effect="light" class="status-tag" round>
                                {{ formatStatus(item.status) }}
                            </el-tag>
                        </div>

                        <div class="card-body">
                            <div class="user-info">
                                <div class="avatar-box"><el-icon><UserFilled /></el-icon></div>
                                <div class="info-texts">
                                    <div class="username">{{ item.username }}</div>
                                </div>
                                <div class="order-amount">
                                    <span class="symbol">¥</span>
                                    <span class="num">{{ item.totalAmount }}</span>
                                </div>
                            </div>
                            
                            <div class="remark-box" v-if="item.remark">
                                <el-icon class="remark-icon"><ChatDotSquare /></el-icon>
                                <span class="remark-text">{{ item.remark }}</span>
                            </div>
                        </div>

                        <div class="card-footer">
                            <el-button plain round size="small" @click="goDetail(item.id)">
                                查看详情
                            </el-button>
                            
                            <div class="action-btn-group">
                                <el-button 
                                    v-if="item.status === 'PAID'" 
                                    type="danger" 
                                    round 
                                    class="primary-action-btn pulse-anim"
                                    @click="handleAccept(item.id)"
                                >
                                    <el-icon><Check /></el-icon>&nbsp;立即接单
                                </el-button>

                                <el-button 
                                    v-if="item.status === 'ACCEPTED'" 
                                    type="success" 
                                    round 
                                    class="primary-action-btn"
                                    @click="handleComplete(item.id)"
                                >
                                    <el-icon><Select /></el-icon>&nbsp;完成出餐
                                </el-button>

                                <template v-if="item.status === 'REFUND_PENDING'">
                                    <el-button type="success" round size="small" class="primary-action-btn" @click="handleAgreeRefund(item.id)">
                                        ✔ 同意退款
                                    </el-button>
                                    <el-button type="danger" plain round size="small" @click="handleRejectRefund(item.id)">
                                        ✖ 拒绝
                                    </el-button>
                                </template>
                            </div>
                        </div>
                    </el-card>
                </div>

                <div class="pagination-wrapper" v-if="filteredList.length > 0">
                    <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :page-sizes="[12, 24, 48, 96]"
                        layout="total, sizes, prev, pager, next, jumper"
                        :total="filteredList.length"
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        background
                    />
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
    List, Search, Ticket, UserFilled, ChatDotSquare, Check, Select 
} from '@element-plus/icons-vue'
import {
    acceptMerchantOrder,
    completeMerchantOrder,
    getMerchantOrderList,
    agreeRefund,
    rejectRefund
} from '@/api/merchantOrder'


const router = useRouter()
const orderList = ref([])

// State
const activeTab = ref('PAID') // 默认进入先看待接单
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(12)

// Computed
const paidCount = computed(() => orderList.value.filter(o => o.status === 'PAID').length)
const acceptedCount = computed(() => orderList.value.filter(o => o.status === 'ACCEPTED').length)
const refundPendingCount = computed(() => orderList.value.filter(o => o.status === 'REFUND_PENDING').length)

const filteredList = computed(() => {
    let result = orderList.value

    // 1. 状态过滤
    if (activeTab.value !== 'ALL') {
        result = result.filter(o => o.status === activeTab.value)
    }

    // 2. 搜索过滤
    if (searchQuery.value) {
        const keyword = searchQuery.value.toLowerCase()
        result = result.filter(o => 
            (o.orderNo && o.orderNo.toLowerCase().includes(keyword)) ||
            (o.username && o.username.toLowerCase().includes(keyword)) ||
            (o.remark && o.remark.toLowerCase().includes(keyword))
        )
    }

    return result
})

const paginatedList = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredList.value.slice(start, end)
})

// Handlers
const handleTabChange = () => {
    currentPage.value = 1
}

const handleSearch = () => {
    currentPage.value = 1
}

const handleSizeChange = (val) => {
    pageSize.value = val
    currentPage.value = 1
}

const handleCurrentChange = (val) => {
    currentPage.value = val
}

const loadData = async () => {
    const res = await getMerchantOrderList()
    if (res.code === 200) {
        // 倒序排列，新的在前
        orderList.value = (res.data || []).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }
}

const goDetail = (id) => {
    router.push(`/merchant/orders/${id}`)
}

const copyOrderNo = (no) => {
    navigator.clipboard.writeText(no).then(() => {
        ElMessage.success('订单号已复制')
    }).catch(() => {
        ElMessage.error('复制失败，请手动选择')
    })
}

const handleAccept = async (id) => {
    try {
        const res = await acceptMerchantOrder(id)
        ElMessage.success('接单成功')
        loadData()
    } catch (error) {
        ElMessage.error(error.message || '接单失败')
    }
}

const handleComplete = async (id) => {
    try {
        const res = await completeMerchantOrder(id)
        ElMessage.success('订单已完成')
        loadData()
    } catch (error) {
        ElMessage.error(error.message || '完成订单失败')
    }
}

const handleAgreeRefund = async (id) => {
    try {
        await ElMessageBox.confirm(
            '同意该退款申请后，将自动归还库存及用户优惠券，操作不可撤销。',
            '同意退款确认',
            { type: 'warning', confirmButtonText: '确认同意', cancelButtonText: '再想想' }
        )
        const res = await agreeRefund(id)
        ElMessage.success(res.message || '已同意退款')
        loadData()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
    }
}

const handleRejectRefund = async (id) => {
    try {
        const { value: rejectReason } = await ElMessageBox.prompt(
            '请输入拒绝退款的理由（可不填）',
            '拒绝退款',
            {
                confirmButtonText: '确认拒绝',
                cancelButtonText: '取消',
                inputPlaceholder: '例如：商品已制作完成，不支持退款',
                inputType: 'textarea',
            }
        )
        const res = await rejectRefund(id, { rejectReason: rejectReason || '' })
        ElMessage.success(res.message || '已拒绝退款申请')
        loadData()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
    }
}

const formatStatus = (status) => {
    if (status === 'PENDING_PAYMENT') return '等买家付款'
    if (status === 'PAID')           return '待接单'
    if (status === 'ACCEPTED')       return '出餐中'
    if (status === 'RECEIVED')       return '已收货'
    if (status === 'REFUND_PENDING') return '退款审核中'
    if (status === 'REFUNDED')       return '已退款'
    if (status === 'COMPLETED')      return '已完成'
    if (status === 'CANCELLED')      return '已取消'
    return status
}

const statusTagType = (status) => {
    if (status === 'PENDING_PAYMENT') return 'info'
    if (status === 'PAID')           return 'danger'
    if (status === 'ACCEPTED')       return 'warning'
    if (status === 'RECEIVED')       return 'success'
    if (status === 'REFUND_PENDING') return 'danger'
    if (status === 'REFUNDED')       return ''
    if (status === 'COMPLETED')      return 'success'
    if (status === 'CANCELLED')      return 'info'
    return ''
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.merchant-order-container {
    padding: 24px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
}

.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background: #fff;
    padding: 20px 24px;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.title-with-icon {
    display: flex;
    align-items: center;
    gap: 12px;
}

.title-icon {
    font-size: 24px;
    color: #409eff;
    background: #e6f1fe;
    padding: 8px;
    border-radius: 8px;
}

.page-title {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
}

.search-box {
    width: 320px;
}

.main-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.order-tabs :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: #ebeef5;
}

.order-tabs :deep(.el-tabs__item) {
    font-size: 16px;
    height: 60px;
    line-height: 60px;
}

.tab-badge {
    margin-left: 4px;
    margin-top: -2px;
}

.list-container {
    padding: 20px 0;
}

.order-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 20px;
}

.order-card {
    border-radius: 12px;
    border: 1px solid #ebeef5;
    transition: all 0.3s ease;
}

.order-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08) !important;
}

.order-card.is-urgent {
    border: 2px solid #fbc4c4;
    background: linear-gradient(to bottom, #fffafb, #ffffff);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f2f5;
    background: #fafafc;
}

.order-card.is-urgent .card-header {
    background: #fef0f0;
    border-bottom: 1px solid #fde2e2;
}

.header-left {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.order-time {
    font-size: 13px;
    color: #909399;
}

.order-no {
    font-size: 13px;
    color: #606266;
    font-family: monospace;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
}

.order-no:hover {
    color: #409eff;
}

.card-body {
    padding: 20px;
}

.user-info {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
}

.avatar-box {
    width: 40px;
    height: 40px;
    background: #e6f1fe;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #409eff;
    margin-right: 12px;
}

.info-texts {
    flex: 1;
}

.username {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.order-amount {
    color: #f56c6c;
}

.symbol {
    font-size: 14px;
    margin-right: 2px;
}

.num {
    font-size: 24px;
    font-weight: 700;
}

.remark-box {
    background: #fdf6ec;
    border-radius: 8px;
    padding: 10px 14px;
    display: flex;
    align-items: flex-start;
    gap: 8px;
    color: #e6a23c;
}

.remark-icon {
    font-size: 16px;
    margin-top: 2px;
}

.remark-text {
    font-size: 13px;
    line-height: 1.5;
}

.card-footer {
    padding: 12px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #f0f2f5;
}

.primary-action-btn {
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

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 30px;
}
</style>