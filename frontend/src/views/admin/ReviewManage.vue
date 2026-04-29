<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">全域评价监管与违规申诉</h2>
        <p class="page-desc">实时监控全平台的商家服务与用户评价，对违规、恶意言论进行一键屏蔽与干预</p>
      </div>
    </div>

    <el-card class="main-card" shadow="never">
      <!-- 搜索与工具栏 -->
      <div class="toolbar">
        <div class="filter-group">
          <el-input v-model="query.shopName" placeholder="按商家名称查找..." clearable class="filter-item border-input" @keyup.enter="handleSearch">
            <template #prefix>
              <span class="prefix-icon">🏪</span>
            </template>
          </el-input>
          
          <el-input v-model="query.orderNo" placeholder="按关联订单号追溯..." clearable class="filter-item border-input" @keyup.enter="handleSearch">
            <template #prefix>
              <span class="prefix-icon">🧾</span>
            </template>
          </el-input>
          
          <el-button type="primary" class="search-btn" @click="handleSearch">
            联合检索
          </el-button>
        </div>

        <div class="action-group">
          <el-button @click="handleSearch" plain class="refresh-btn">
            拉取最新评价
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="reviewList" v-loading="loading" class="data-table"
                :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '54px', borderBottom: '1px solid #e2e8f0' }"
                :row-style="{ height: '80px' }">
        
        <el-table-column label="舆情发生客体" min-width="180">
          <template #default="{ row }">
            <div class="target-cell">
              <div class="target-card user-target">
                <span class="type-badge user">客</span>
                <span class="name-text">{{ row.nickname || '匿名用户' }}</span>
              </div>
              <div class="target-card shop-target">
                <span class="type-badge shop">商</span>
                <span class="name-text">{{ row.shopName || '未知商家' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="满意度评分" width="160" align="center">
          <template #default="{ row }">
            <div class="rating-cell">
              <el-rate :model-value="row.rating" disabled text-color="#ff9900" class="custom-rate" />
              <div class="score-text">{{ row.rating ? row.rating.toFixed(1) : '0.0' }} 分</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="核心言论舆情" min-width="320">
          <template #default="{ row }">
            <div class="comment-timeline">
              <!-- 初次评价 -->
              <div class="comment-node">
                <div class="node-tag initial">初评</div>
                <div class="node-content" :class="{'empty-text': !row.content}">
                    "{{ row.content || '该用户未留下任何文字' }}"
                </div>
              </div>
              
              <!-- 追评 -->
              <div class="comment-node" v-if="row.followUpContent">
                <div class="node-tag followup">追评</div>
                <div class="node-content text-orange">
                    "{{ row.followUpContent }}"
                </div>
              </div>

              <!-- 商家回复 -->
              <div class="comment-node" v-if="row.merchantReply">
                <div class="node-tag reply">店家回复</div>
                <div class="node-content text-blue">
                    "{{ row.merchantReply }}"
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="干预状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" class="status-tag" round>
              <span class="status-dot" :class="row.status === 1 ? 'dot-success' : 'dot-danger'"></span>
              {{ row.status === 1 ? '常规展示' : '已屏蔽' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="时间节点" width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="监管执行" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button 
                  v-if="row.status === 1" 
                  size="small" 
                  type="danger" 
                  plain 
                  class="action-btn block-btn"
                  @click="handleBlock(row, 0)">
                ⛔ 强行屏蔽
              </el-button>
              
              <el-button 
                  v-else 
                  size="small" 
                  type="success" 
                  plain 
                  class="action-btn restore-btn"
                  @click="handleBlock(row, 1)">
                ✅ 解除黑房
              </el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon">🛡️</span>
            <p class="empty-text">当前网络环境清朗，没有查询到任何舆情历史</p>
          </div>
        </template>
      </el-table>

      <div class="pagination-container" v-if="total > 0 || reviewList.length > 0">
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
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListReviews, adminBlockReview } from '@/api/review'

const reviewList = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ shopName: '', orderNo: '', current: 1, size: 10 })

const handleSearch = () => {
    query.current = 1
    loadData()
}

const handleSizeChange = () => {
    query.current = 1
    loadData()
}

const handlePageChange = () => {
    loadData()
}

const loadData = async () => {
    loading.value = true
    try {
        const res = await adminListReviews(query)
        if (res.code === 200) {
            reviewList.value = res.data.records || []
            total.value = res.data.total || 0
        }
    } catch (e) {
        ElMessage.error('舆情数据池加载失败')
    } finally {
        loading.value = false
    }
}

const handleBlock = async (row, status) => {
    const isBlocking = status === 0
    const actionText = isBlocking ? '强行屏蔽' : '取消屏蔽 (释放)'
    const warningMsg = isBlocking 
        ? '确认要对此包含违规或恶意言论的评价执行【强行屏蔽】吗？屏蔽后所有用户端（含商户）均无法查阅该记录！'
        : '解除屏蔽后该评价将重新在商铺页和用户端正常流转，确定没有违规风险并释放吗？'

    try {
        await ElMessageBox.confirm(warningMsg, '风控指令确认', { 
            type: isBlocking ? 'error' : 'warning',
            confirmButtonText: '确定' + actionText,
            cancelButtonText: '暂缓操作',
            confirmButtonClass: isBlocking ? 'el-button--danger' : 'el-button--success'
        })
        const res = await adminBlockReview(row.id, status)
        if (res.code === 200) {
            ElMessage.success(res.message || `已成功${actionText}`)
            loadData()
        } else {
            ElMessage.error(res.message || `执行${actionText}出现异常`)
        }
    } catch (e) {
        if (e !== 'cancel') ElMessage.error('系统中断操作')
    }
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

.border-input { width: 220px; }

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

.search-btn, .refresh-btn {
    border-radius: 8px;
    font-weight: 500;
    padding: 8px 18px;
    height: 34px;
}

.search-btn {
    background: #3b82f6;
    border: none;
}
.search-btn:hover { background: #2563eb; }

/* Table styles */
.data-table {
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.target-cell {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.target-card {
    display: flex;
    align-items: center;
    gap: 8px;
    background: #f8fafc;
    padding: 4px 10px;
    border-radius: 6px;
    border: 1px solid #f1f5f9;
}

.type-badge {
    font-size: 11px;
    font-weight: bold;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    color: white;
}

.type-badge.user { background: #6366f1; }
.type-badge.shop { background: #f59e0b; }

.name-text {
    font-size: 13px;
    font-weight: 600;
    color: #334155;
    max-width: 110px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.rating-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.custom-rate :deep(.el-rate__icon) {
    margin-right: 2px;
    font-size: 16px;
}

.score-text {
    font-size: 13px;
    font-weight: 700;
    color: #eab308;
    background: #fefce8;
    padding: 2px 10px;
    border-radius: 12px;
    border: 1px solid #fef08a;
}

/* 连续对话体样式 */
.comment-timeline {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 6px 0;
}

.comment-node {
    display: flex;
    align-items: flex-start;
    gap: 8px;
}

.node-tag {
    flex-shrink: 0;
    font-size: 11px;
    font-weight: 600;
    padding: 2px 6px;
    border-radius: 4px;
    margin-top: 2px;
}

.node-tag.initial { background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; }
.node-tag.followup { background: #fff7ed; color: #ea580c; border: 1px solid #fed7aa; }
.node-tag.reply { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }

.node-content {
    font-size: 13px;
    line-height: 1.5;
    color: #334155;
    word-break: break-all;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.empty-text { color: #94a3b8; font-style: italic; }
.text-orange { color: #c2410c; }
.text-blue { color: #1d4ed8; }

.status-tag {
    border: none;
    font-weight: 600;
    padding: 0 12px;
    border-radius: 20px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.dot-success { background-color: #10b981; }
.dot-danger { background-color: #ef4444; }

.time-text {
    color: #64748b;
    font-size: 13px;
}

.action-cell {
    display: flex;
    justify-content: center;
}

.action-btn {
    border-radius: 6px;
    font-weight: 600;
    padding: 6px 14px;
}

.block-btn {
    background: #fef2f2;
}

.block-btn:hover { background: #fee2e2; }

.restore-btn {
    background: #ecfdf5;
}

.empty-state {
    padding: 50px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #94a3b8;
}

.empty-icon { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
.empty-text { font-size: 15px; }

.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
}
</style>
