<template>
    <div class="page-container">
        <!-- 页面顶部描述 -->
        <div class="page-header">
            <div class="header-content">
                <h2 class="page-title">入驻商家管理</h2>
                <p class="page-desc">审核、管理所有平台商户的入驻申请及日常经营状态</p>
            </div>
        </div>

        <el-card class="main-card" shadow="never">
            <!-- 搜索与工具栏 -->
            <div class="toolbar">
                <div class="filter-group">
                    <el-input v-model="query.shopName" placeholder="搜索店铺名称..." clearable class="filter-item search-input" @keyup.enter="handleSearch">
                        <template #prefix>
                            <span class="prefix-icon">🏪</span>
                        </template>
                    </el-input>
                    
                    <el-select v-model="query.status" placeholder="全部状态筛选" clearable class="filter-item status-select" @change="handleSearch">
                        <el-option label="⏳ 待审核" :value="0" />
                        <el-option label="🟢 正常经营" :value="1" />
                        <el-option label="🔴 已禁用" :value="2" />
                        <el-option label="⛔ 已拒绝" :value="3" />
                    </el-select>
                    
                    <el-button type="primary" class="search-btn" @click="handleSearch">
                        查询
                    </el-button>
                </div>

                <div class="action-group">
                    <el-button @click="handleSearch" class="refresh-btn" plain>
                        刷新数据
                    </el-button>
                </div>
            </div>

            <!-- 数据表格 -->
            <el-table :data="tableData" v-loading="loading" class="data-table"
                      :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '54px', borderBottom: '1px solid #e2e8f0' }"
                      :row-style="{ height: '68px' }">
                
                <el-table-column prop="id" label="商户 ID" width="100" align="center">
                    <template #default="{ row }">
                        <span class="id-text">#{{ row.id }}</span>
                    </template>
                </el-table-column>
                
                <el-table-column label="店铺与位置信息" min-width="240">
                    <template #default="{ row }">
                        <div class="shop-info">
                            <div class="shop-avatar">
                                {{ row.shopName ? row.shopName.charAt(0).toUpperCase() : '铺' }}
                            </div>
                            <div class="shop-details">
                                <span class="shop-name">{{ row.shopName }}</span>
                                <span class="shop-address">
                                    <span class="icon-map">📍</span> {{ row.address || '该商户尚未填写地址' }}
                                </span>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                
                <el-table-column prop="phone" label="联系电话" width="160">
                    <template #default="{ row }">
                        <span class="phone-text">📞 {{ row.phone || '-' }}</span>
                    </template>
                </el-table-column>
                
                <el-table-column label="综合评分" width="120" align="center">
                    <template #default="{ row }">
                        <div v-if="row.rating && Number(row.rating) > 0" class="rating-cell">
                            <span class="rating-icon">⭐</span>
                            <span class="rating-score">{{ formatRating(row.rating) }}</span>
                        </div>
                        <div v-else class="no-rating">暂无评分</div>
                    </template>
                </el-table-column>
                
                <el-table-column label="当前经营状态" width="140" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusTagType(row.status)" effect="light" class="status-tag" round>
                            <span class="status-dot" :class="'dot-' + statusTagType(row.status)"></span>
                            {{ formatStatus(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                
                <el-table-column prop="createTime" label="注册/申请时间" width="180" align="center">
                    <template #default="{ row }">
                        <span class="time-text">{{ row.createTime }}</span>
                    </template>
                </el-table-column>
                
                <el-table-column label="管理操作" width="220" align="center" fixed="right">
                    <template #default="{ row }">
                        <div class="row-actions">
                            <!-- 待审核状态 -->
                            <template v-if="row.status === 0">
                                <el-button size="small" type="success" class="action-btn" @click="handleAudit(row.id, 1)">通过审核</el-button>
                                <el-button size="small" type="danger" plain class="action-btn" @click="handleAudit(row.id, 3)">驳回入驻</el-button>
                            </template>
                            
                            <!-- 正常状态 -->
                            <template v-if="row.status === 1">
                                <el-button size="small" type="warning" plain class="action-btn" @click="handleUpdateStatus(row.id, 2)">封停账号</el-button>
                            </template>
                            
                            <!-- 禁用状态 -->
                            <template v-if="row.status === 2">
                                <el-button size="small" type="primary" plain class="action-btn" @click="handleUpdateStatus(row.id, 1)">解除封停</el-button>
                            </template>

                            <!-- 拒绝状态 -->
                            <template v-if="row.status === 3">
                                <span class="rejected-text">已驳回申请</span>
                            </template>
                        </div>
                    </template>
                </el-table-column>

                <template #empty>
                    <div class="empty-state">
                        <span class="empty-icon">🏢</span>
                        <p class="empty-text">后台暂无查找到相关商户数据</p>
                    </div>
                </template>
            </el-table>

            <div class="pagination-container">
                <el-pagination 
                    v-model:current-page="query.current"
                    v-model:page-size="query.size"
                    :page-sizes="[10, 20, 50]"
                    background 
                    layout="total, sizes, prev, pager, next, jumper" 
                    :total="total" 
                    @size-change="loadData"
                    @current-change="handlePageChange" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantPage, auditMerchant, updateMerchantStatus } from '@/api/adminMerchant'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
    shopName: '',
    status: null,
    current: 1,
    size: 10
})

const loadData = async () => {
    loading.value = true
    try {
        const res = await getMerchantPage(query)
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
        } else {
            ElMessage.error(res.message || '获取数据失败')
        }
    } catch (error) {
        ElMessage.error('查询请求失败')
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    query.current = 1
    loadData()
}

const handleAudit = (id, status) => {
    const actionText = status === 1 ? '通过' : '驳回'
    ElMessageBox.confirm(`确认要${actionText}该商户的入驻申请吗？`, '审核确认', {
        type: status === 1 ? 'success' : 'warning',
        confirmButtonText: '确定审核',
        cancelButtonText: '取消',
        confirmButtonClass: status === 1 ? 'el-button--success' : 'el-button--danger'
    }).then(async () => {
        try {
            const res = await auditMerchant({ id, status })
            if (res.code === 200) {
                ElMessage.success(res.message || `已${actionText}申请`)
                loadData()
            } else {
                ElMessage.error(res.message || '操作失败')
            }
        } catch (error) {
            ElMessage.error('操作异常')
        }
    }).catch(() => {})
}

const handleUpdateStatus = (id, status) => {
    const actionText = status === 2 ? '封停/禁用' : '解除封停'
    ElMessageBox.confirm(`确定要 对该账号进行 [${actionText}] 操作吗？`, '账号管护警告', {
        type: 'warning',
        confirmButtonText: '确定执行',
        cancelButtonText: '取消',
        confirmButtonClass: status === 2 ? 'el-button--warning' : 'el-button--primary'
    }).then(async () => {
        try {
            const res = await updateMerchantStatus({ id, status })
            if (res.code === 200) {
                ElMessage.success(res.message || `状态已更新为主`)
                loadData()
            } else {
                ElMessage.error(res.message || '状态更新失败')
            }
        } catch (error) {
            ElMessage.error('请求异常')
        }
    }).catch(() => {})
}

const handlePageChange = (page) => {
    query.current = page
    loadData()
}

const formatStatus = (status) => {
    if (status === 0) return '待审核'
    if (status === 1) return '正常'
    if (status === 2) return '已封禁'
    if (status === 3) return '已驳回'
    return '状态异常'
}

const statusTagType = (status) => {
    if (status === 0) return 'warning'
    if (status === 1) return 'success'
    if (status === 2) return 'danger'
    if (status === 3) return 'info'
    return ''
}

const formatRating = (rating) => {
    if (!rating || Number(rating) === 0) return '暂无评分'
    return Number(rating).toFixed(1)
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

.search-input { width: 260px; }
.status-select { width: 160px; }

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

.id-text {
    font-family: monospace;
    font-size: 14px;
    color: #64748b;
    background: #f1f5f9;
    padding: 2px 6px;
    border-radius: 4px;
}

.shop-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.shop-avatar {
    width: 44px;
    height: 44px;
    background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
    color: #4f46e5;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 600;
    flex-shrink: 0;
}

.shop-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.shop-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
}

.shop-address {
    font-size: 13px;
    color: #64748b;
    display: flex;
    align-items: center;
    gap: 4px;
}

.icon-map {
    font-size: 12px;
}

.phone-text {
    font-size: 14px;
    color: #475569;
    font-weight: 500;
}

.rating-cell {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    background: #fffbeb;
    padding: 4px 10px;
    border-radius: 20px;
    border: 1px solid #fde68a;
}

.rating-icon {
    font-size: 12px;
}

.rating-score {
    color: #d97706;
    font-weight: 700;
    font-size: 14px;
}

.no-rating {
    color: #94a3b8;
    font-size: 13px;
}

.status-tag {
    border: none;
    font-weight: 500;
    padding: 0 12px;
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

.dot-success { background-color: #10b981; }
.dot-warning { background-color: #f59e0b; }
.dot-danger { background-color: #ef4444; }
.dot-info { background-color: #94a3b8; }

.time-text {
    color: #64748b;
    font-size: 13px;
}

.row-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.action-btn {
    border-radius: 6px;
}

.rejected-text {
    color: #94a3b8;
    font-size: 13px;
    font-style: italic;
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