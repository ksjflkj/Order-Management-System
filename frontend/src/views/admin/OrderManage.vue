<template>
    <div class="page-container">
        <!-- 页面顶部描述 -->
        <div class="page-header">
            <div class="header-content">
                <h2 class="page-title">平台订单管理</h2>
                <p class="page-desc">全局监控与管理平台产生的所有交易订单状态</p>
            </div>
        </div>

        <el-card class="main-card" shadow="never">
            <!-- 搜索与工具栏 -->
            <div class="toolbar">
                <div class="filter-group">
                    <el-input v-model="query.orderNo" placeholder="搜索订单编号..." clearable class="filter-item search-input" @keyup.enter="handleSearch">
                        <template #prefix>
                            <span class="prefix-icon">🔍</span>
                        </template>
                    </el-input>
                    
                    <el-select v-model="query.status" placeholder="全流水状态筛选" clearable class="filter-item status-select" @change="handleSearch">
                        <el-option label="⏳ 待支付" value="PENDING_PAYMENT" />
                        <el-option label="💳 已支付" value="PAID" />
                        <el-option label="👨‍🍳 商家已接单" value="ACCEPTED" />
                        <el-option label="✅ 已确认收货" value="RECEIVED" />
                        <el-option label="🎉 订单已完成" value="COMPLETED" />
                        <el-option label="🔁 退款申请中" value="REFUND_PENDING" />
                        <el-option label="💸 已退款" value="REFUNDED" />
                        <el-option label="⛔ 退款被拒绝" value="REFUND_REJECTED" />
                        <el-option label="⚖️ 平台仲裁中" value="REFUND_ARBITRATING" />
                        <el-option label="🔚 申诉已驳回" value="ARBITRATION_REJECTED" />
                        <el-option label="🚫 取消交易" value="CANCELLED" />
                    </el-select>
                    
                    <el-button type="primary" class="search-btn" @click="handleSearch">
                        检索订单
                    </el-button>
                </div>

                <div class="action-group">
                    <el-button @click="handleSearch" class="refresh-btn" plain>
                        刷新数据
                    </el-button>
                </div>
            </div>

            <!-- 数据表格 -->
            <el-table :data="orderList" v-loading="loading" class="data-table"
                      :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '54px', borderBottom: '1px solid #e2e8f0' }"
                      :row-style="{ height: '68px' }">
                
                <el-table-column label="订单编号" min-width="240">
                    <template #default="{ row }">
                        <div class="order-no-cell">
                            <span class="icon-receipt">🧾</span>
                            <span class="order-no-text">{{ row.orderNo }}</span>
                        </div>
                    </template>
                </el-table-column>
                
                <el-table-column label="交易双方" min-width="220">
                    <template #default="{ row }">
                        <div class="parties-cell">
                            <div class="party-item user-party">
                                <span class="party-label">买</span>
                                <span class="party-name">{{ row.username || '未知用户' }}</span>
                            </div>
                            <div class="party-divider">⇌</div>
                            <div class="party-item shop-party">
                                <span class="party-label">卖</span>
                                <span class="party-name">{{ row.shopName || '未知商家' }}</span>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                
                <el-table-column prop="totalAmount" label="订单金额" width="140" align="right">
                    <template #default="{ row }">
                        <div class="amount-cell">
                            <span class="amount-symbol">¥</span>
                            <span class="amount-value">{{ row.totalAmount }}</span>
                        </div>
                    </template>
                </el-table-column>
                
                <el-table-column label="交易状态" width="160" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusTagType(row.status)" effect="light" class="status-tag" round>
                            <span class="status-dot" :class="'dot-' + statusTagType(row.status)"></span>
                            {{ formatStatus(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                
                <el-table-column label="备注说明" min-width="160" show-overflow-tooltip>
                    <template #default="{ row }">
                        <span class="remark-text" :class="{'empty-remark': !row.remark}">{{ row.remark || '无备注' }}</span>
                    </template>
                </el-table-column>
                
                <el-table-column prop="createTime" label="下单时间" width="180" align="center">
                    <template #default="{ row }">
                        <span class="time-text">{{ row.createTime }}</span>
                    </template>
                </el-table-column>
                
                <el-table-column label="平台管理" width="140" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" class="action-btn" @click="goDetail(row.id)">
                            查看详情 <span class="arrow-icon">→</span>
                        </el-button>
                    </template>
                </el-table-column>
                
                <template #empty>
                    <div class="empty-state">
                        <span class="empty-icon">📂</span>
                        <p class="empty-text">后台暂无查询到匹配的订单数据</p>
                    </div>
                </template>
            </el-table>

            <div class="pagination-container" v-if="total > 0 || orderList.length > 0">
                <el-pagination 
                    v-model:current-page="query.current"
                    v-model:page-size="query.size"
                    :page-sizes="[10, 20, 50]"
                    background 
                    layout="total, sizes, prev, pager, next, jumper" 
                    :total="total" 
                    @size-change="handleSizeChange"
                    @current-change="handlePageChange" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminOrderList } from '@/api/adminOrder'

const router = useRouter()
const orderList = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
    orderNo: '',
    status: '',
    current: 1,
    size: 10
})

const handleSizeChange = () => {
    query.current = 1
    loadData()
}

const handlePageChange = () => {
    loadData()
}

const handleSearch = () => {
    query.current = 1
    loadData()
}

const loadData = async () => {
    loading.value = true
    try {
        const params = {
            ...query,
            status: query.status || null
        }
        const res = await getAdminOrderList(params)
        if (res.code === 200) {
            orderList.value = res.data.records || []
            total.value = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}

const goDetail = (id) => {
    router.push(`/admin/orders/${id}`)
}

const formatStatus = (status) => {
    if (status === 'PENDING_PAYMENT')      return '待买家支付'
    if (status === 'PAID')                 return '已全额支付'
    if (status === 'ACCEPTED')             return '商家已接单'
    if (status === 'RECEIVED')             return '已确认收货'
    if (status === 'COMPLETED')            return '订单已完成'
    if (status === 'REFUND_PENDING')       return '退款申请中'
    if (status === 'REFUNDED')             return '已退款'
    if (status === 'REFUND_REJECTED')      return '退款被拒绝'
    if (status === 'REFUND_ARBITRATING')   return '平台仲裁中'
    if (status === 'ARBITRATION_REJECTED') return '申诉已驳回'
    if (status === 'CANCELLED')            return '交易已取消'
    return status
}

const statusTagType = (status) => {
    if (status === 'PENDING_PAYMENT')      return 'warning'
    if (status === 'PAID')                 return 'primary'
    if (status === 'ACCEPTED')             return 'primary'
    if (status === 'RECEIVED')             return 'success'
    if (status === 'COMPLETED')            return 'success'
    if (status === 'REFUND_PENDING')       return 'danger'
    if (status === 'REFUNDED')             return 'danger'
    if (status === 'REFUND_REJECTED')      return 'danger'
    if (status === 'REFUND_ARBITRATING')   return 'warning'
    if (status === 'ARBITRATION_REJECTED') return 'info'
    if (status === 'CANCELLED')            return 'info'
    return ''
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

.page-header {
    margin-bottom: 20px;
}

.header-content h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #1e293b;
}

.page-desc {
    margin: 0;
    font-size: 14px;
    color: #64748b;
}

.main-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
}

.toolbar {
    display: flex;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 20px;
}

.filter-group, .action-group {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
}

.search-input { width: 280px; }
.status-select { width: 180px; }

:deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
}

:deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5) inset !important;
}

.search-btn {
    border-radius: 8px;
    padding: 8px 20px;
    background: #3b82f6;
    border: none;
    font-weight: 500;
}

.search-btn:hover {
    background: #2563eb;
}

.refresh-btn {
    border-radius: 8px;
    font-weight: 500;
}

/* Data Table styling */
.data-table {
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.order-no-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.icon-receipt {
    font-size: 16px;
    color: #64748b;
}

.order-no-text {
    font-family: monospace;
    font-size: 14px;
    color: #334155;
    background: #f1f5f9;
    padding: 4px 10px;
    border-radius: 6px;
    letter-spacing: 0.5px;
}

.parties-cell {
    display: flex;
    align-items: center;
    gap: 12px;
    background: #f8fafc;
    padding: 6px 12px;
    border-radius: 8px;
    border: 1px solid #f1f5f9;
}

.party-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

.party-label {
    font-size: 11px;
    font-weight: 600;
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.user-party .party-label { background: #e0e7ff; color: #4f46e5; }
.shop-party .party-label { background: #ffedd5; color: #ea580c; }

.party-name {
    font-size: 13px;
    font-weight: 500;
    color: #1e293b;
    max-width: 90px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.party-divider {
    color: #cbd5e1;
    font-size: 14px;
    font-weight: bold;
}

.amount-cell {
    display: inline-flex;
    align-items: baseline;
    gap: 2px;
}

.amount-symbol {
    font-size: 13px;
    color: #ef4444;
}

.amount-value {
    color: #ef4444;
    font-weight: 700;
    font-size: 16px;
}

.status-tag {
    border: none;
    font-weight: 600;
    padding: 0 14px;
    border-radius: 20px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
}

.dot-primary { background-color: #3b82f6; }
.dot-success { background-color: #10b981; }
.dot-warning { background-color: #f59e0b; }
.dot-danger { background-color: #ef4444; }
.dot-info { background-color: #94a3b8; }

.remark-text {
    font-size: 13px;
    color: #475569;
}

.empty-remark {
    color: #94a3b8;
    font-style: italic;
}

.time-text {
    color: #64748b;
    font-size: 13px;
}

.action-btn {
    font-weight: 600;
    font-size: 13px;
}

.arrow-icon {
    font-size: 14px;
    margin-left: 2px;
    transition: transform 0.2s;
}

.action-btn:hover .arrow-icon {
    transform: translateX(4px);
}

.empty-state {
    padding: 50px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #94a3b8;
}

.empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
}

.empty-text {
    font-size: 15px;
}

.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
}
</style>