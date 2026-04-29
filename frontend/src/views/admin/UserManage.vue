<template>
    <div class="user-manage-page">
        <!-- Header -->
        <div class="page-header">
            <div class="title-with-icon">
                <div class="title-icon-wrap">
                    <el-icon><UserFilled /></el-icon>
                </div>
                <div>
                    <h1 class="page-title">用户管理</h1>
                    <p class="page-subtitle">管理平台注册用户，支持查询与禁用操作</p>
                </div>
            </div>
            <!-- 统计卡片 -->
            <div class="stat-cards">
                <div class="stat-card">
                    <span class="stat-num">{{ pagination.total }}</span>
                    <span class="stat-label">总用户数</span>
                </div>
                <div class="stat-card green">
                    <span class="stat-num">{{ normalCount }}</span>
                    <span class="stat-label">正常用户</span>
                </div>
                <div class="stat-card red">
                    <span class="stat-num">{{ bannedCount }}</span>
                    <span class="stat-label">已禁用</span>
                </div>
            </div>
        </div>

        <!-- 搜索栏 -->
        <el-card class="search-card" shadow="never">
            <div class="search-row">
                <el-input
                    v-model="query.username"
                    placeholder="搜索用户名..."
                    clearable
                    @keyup.enter="handleSearch"
                    style="width: 200px"
                >
                    <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-input
                    v-model="query.phone"
                    placeholder="搜索手机号..."
                    clearable
                    @keyup.enter="handleSearch"
                    style="width: 180px"
                >
                    <template #prefix><el-icon><Phone /></el-icon></template>
                </el-input>
                <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="handleSearch">
                    <el-option label="正常用户" :value="1" />
                    <el-option label="已禁用" :value="0" />
                </el-select>
                <el-button type="primary" round @click="handleSearch">
                    <el-icon><Search /></el-icon>&nbsp;查询
                </el-button>
                <el-button round @click="handleReset">重置</el-button>
            </div>
        </el-card>

        <!-- 用户表格 -->
        <el-card class="table-card" shadow="never">
            <el-table
                :data="tableData"
                v-loading="loading"
                row-class-name="table-row"
                header-cell-class-name="table-header-cell"
                style="width: 100%"
            >
                <template #empty>
                    <el-empty description="暂无用户数据" :image-size="120" />
                </template>

                <el-table-column label="用户信息" min-width="200">
                    <template #default="{ row }">
                        <div class="user-cell">
                            <el-avatar
                                :size="44"
                                :src="resolveUrl(row.avatar)"
                                class="user-avatar"
                            >
                                {{ (row.nickname || row.username || '?').charAt(0).toUpperCase() }}
                            </el-avatar>
                            <div class="user-texts">
                                <div class="user-name">{{ row.nickname || row.username }}</div>
                                <div class="user-sub">@{{ row.username }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="联系方式" min-width="160">
                    <template #default="{ row }">
                        <div class="contact-col">
                            <div class="contact-item" v-if="row.phone">
                                <el-icon><Phone /></el-icon> {{ row.phone }}
                            </div>
                            <div class="contact-item muted" v-else>暂无手机号</div>
                            <div class="contact-item" v-if="row.email">
                                <el-icon><Message /></el-icon> {{ row.email }}
                            </div>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="角色" width="110" align="center">
                    <template #default="{ row }">
                        <el-tag
                            :type="row.role === 'MERCHANT' ? 'warning' : 'info'"
                            effect="light"
                            round
                        >
                            {{ row.role === 'MERCHANT' ? '商家' : '普通用户' }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="账号状态" width="110" align="center">
                    <template #default="{ row }">
                        <el-tag
                            :type="row.status !== 1 ? 'danger' : 'success'"
                            effect="light"
                            round
                        >
                            {{ row.status !== 1 ? '已禁用' : '正常' }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="注册时间" width="160" align="center">
                    <template #default="{ row }">
                        <span class="time-text">{{ row.createTime }}</span>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="160" align="center" fixed="right">
                    <template #default="{ row }">
                        <!-- status=1 正常用户 → 显示"禁用"按钮，点击传 0 -->
                        <el-button
                            v-if="row.status === 1"
                            type="danger"
                            plain
                            round
                            size="small"
                            @click="handleToggleStatus(row, 0)"
                        >
                            <el-icon><Lock /></el-icon>&nbsp;禁用
                        </el-button>
                        <!-- status≠1 已禁用 → 显示"恢复"按钮，点击传 1 -->
                        <el-button
                            v-else
                            type="success"
                            plain
                            round
                            size="small"
                            @click="handleToggleStatus(row, 1)"
                        >
                            <el-icon><Unlock /></el-icon>&nbsp;恢复
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-wrap" v-if="pagination.total > 0">
                <el-pagination
                    v-model:current-page="query.current"
                    v-model:page-size="query.size"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pagination.total"
                    @size-change="loadData"
                    @current-change="loadData"
                    background
                />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Search, Phone, Message, Lock, Unlock } from '@element-plus/icons-vue'
import { getAdminUserPage, updateUserStatus } from '@/api/adminUser'

const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ total: 0 })

const query = reactive({
    username: '',
    phone: '',
    status: null,
    current: 1,
    size: 10
})

const normalCount = computed(() => tableData.value.filter(u => u.status === 1).length)
const bannedCount = computed(() => tableData.value.filter(u => u.status !== 1).length)

const resolveUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}

const loadData = async () => {
    loading.value = true
    try {
        const res = await getAdminUserPage(query)
        if (res.code === 200) {
            tableData.value = res.data.records || []
            pagination.total = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    query.current = 1
    loadData()
}

const handleReset = () => {
    query.username = ''
    query.phone = ''
    query.status = null
    query.current = 1
    loadData()
}

const handleToggleStatus = async (row, newStatus) => {
    // newStatus: 0=禁用 1=恢复（正常）
    const action = newStatus === 0 ? '禁用' : '恢复'
    const userName = row.nickname || row.username
    try {
        await ElMessageBox.confirm(
            `确定要${action}用户「${userName}」吗？${newStatus === 0 ? '禁用后该用户将无法登录。' : ''}`,
            `${action}用户`,
            {
                type: newStatus === 1 ? 'warning' : 'info',
                confirmButtonText: `确认${action}`,
                cancelButtonText: '取消'
            }
        )
        const res = await updateUserStatus(row.id, newStatus)
        ElMessage.success(res.message || `${action}成功`)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '操作失败')
        }
    }
}

onMounted(() => loadData())
</script>

<style scoped>
.user-manage-page {
    padding: 24px;
    background: #f5f7fa;
    min-height: calc(100vh - 60px);
    animation: fadeSlideUp 0.5s ease;
}

.page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    padding: 28px 32px;
    border-radius: 16px;
    margin-bottom: 20px;
    color: #fff;
}

.title-with-icon {
    display: flex;
    align-items: center;
    gap: 16px;
}

.title-icon-wrap {
    width: 52px;
    height: 52px;
    background: rgba(255,255,255,0.15);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    backdrop-filter: blur(4px);
}

.page-title {
    margin: 0 0 4px 0;
    font-size: 22px;
    font-weight: 800;
    color: #f8fafc;
}

.page-subtitle {
    margin: 0;
    font-size: 13px;
    color: #94a3b8;
}

.stat-cards {
    display: flex;
    gap: 16px;
}

.stat-card {
    background: rgba(255,255,255,0.1);
    border-radius: 12px;
    padding: 16px 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 90px;
    backdrop-filter: blur(4px);
    border: 1px solid rgba(255,255,255,0.08);
}

.stat-card.green { background: rgba(16, 185, 129, 0.2); }
.stat-card.red   { background: rgba(239, 68, 68, 0.2); }

.stat-num {
    font-size: 28px;
    font-weight: 800;
    color: #fff;
    line-height: 1;
}

.stat-label {
    font-size: 12px;
    color: #cbd5e1;
    margin-top: 6px;
}

.search-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    margin-bottom: 16px;
}

.search-row {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
}

.table-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

:deep(.table-header-cell) {
    background: #f8fafc !important;
    color: #475569;
    font-weight: 700;
    border-bottom: 2px solid #e2e8f0;
}

:deep(.el-table__row) {
    transition: background 0.2s;
}

:deep(.el-table__row:hover > td) {
    background: #f0f9ff !important;
}

.user-cell {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 6px 0;
}

.user-avatar {
    flex-shrink: 0;
    font-weight: 700;
    font-size: 16px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
}

.user-texts { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-size: 15px; font-weight: 700; color: #1e293b; }
.user-sub  { font-size: 12px; color: #94a3b8; }

.contact-col { display: flex; flex-direction: column; gap: 4px; font-size: 13px; }
.contact-item {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #475569;
}
.contact-item .el-icon { color: #94a3b8; font-size: 14px; }
.muted { color: #cbd5e1; }

.time-text { font-size: 13px; color: #64748b; }

.pagination-wrap {
    display: flex;
    justify-content: center;
    padding: 24px 0 8px;
}

@keyframes fadeSlideUp {
    from { opacity: 0; transform: translateY(16px); }
    to   { opacity: 1; transform: translateY(0); }
}
</style>
