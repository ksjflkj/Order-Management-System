<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">满减活动管理</h2>
        <p class="page-desc">配置平台级满减策略，支持灵活设置阶梯式满减规则与活动效期</p>
      </div>
    </div>

    <el-card class="main-card" shadow="never">
      <!-- 搜索与工具栏 -->
      <div class="toolbar">
        <div class="filter-group">
          <el-input v-model="query.name" placeholder="输入活动名称进行检索..." clearable class="filter-item search-input" @keyup.enter="handleSearch">
            <template #prefix>
              <span class="prefix-icon">🎉</span>
            </template>
          </el-input>
          
          <el-button type="primary" class="search-btn" @click="handleSearch">
            检索活动
          </el-button>
        </div>

        <div class="action-group">
          <el-button @click="handleSearch" plain class="refresh-btn">
            刷新
          </el-button>
          <el-button type="primary" class="add-btn" @click="handleCreate">
            ➕ 创设新活动
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="activityList" v-loading="loading" class="data-table"
                :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '54px', borderBottom: '1px solid #e2e8f0' }"
                :row-style="{ height: '68px' }">
        
        <el-table-column label="活动名称及说明" min-width="240">
          <template #default="{ row }">
            <div class="activity-cell">
              <div class="activity-icon">🎁</div>
              <div class="activity-info">
                <div class="activity-name">{{ row.name }}</div>
                <div class="activity-desc" :title="row.description">{{ row.description || '无补充文字说明' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="应用范围" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.merchantId ? 'warning' : 'danger'" effect="light" size="small" class="scope-tag" round>
              {{ row.merchantId ? '特定商户' : '全平台通用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="rules" label="阶梯满减规则" min-width="200">
          <template #default="{ row }">
            <div class="rules-cell">
              <div class="rule-chips">
                <span class="rule-chip" v-for="(rule, index) in parseRules(row.rules)" :key="index">
                  满 <span class="num">{{ rule.threshold }}</span> 减 <span class="num red">{{ rule.reduce }}</span>
                </span>
                <span v-if="!row.rules || row.rules === '[]'" class="no-rule">未配置规则</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="活动生效周期" width="160" align="center">
          <template #default="{ row }">
            <div class="time-cell">
              <div class="time-row"><span class="time-dot start"></span>{{ formatTime(row.startTime) }}</div>
              <div class="time-row"><span class="time-dot end"></span>{{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="light" class="status-tag" round>
              <span class="status-dot" :class="row.status === 'ACTIVE' ? 'dot-success' : 'dot-info'"></span>
              {{ row.status === 'ACTIVE' ? '活动进行中' : '已归档/禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="管理操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" class="action-btn" @click="handleEdit(row)">编辑</el-button>
              <el-divider direction="vertical" class="action-divider" />
              <el-button link type="danger" class="action-btn" @click="handleDelete(row.id)">撤销活动</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon">🎈</span>
            <p class="empty-text">后台暂未设置任何满减活动</p>
          </div>
        </template>
      </el-table>

      <div class="pagination-container" v-if="total > 0 || activityList.length > 0">
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

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '变动活动配置' : '发布新满减活动'" width="650px" class="custom-dialog" destroy-on-close :close-on-click-modal="false">
      <el-form :model="form" label-width="110px" class="activity-form" label-position="left">
        <div class="form-section-title">活动基础信息</div>
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" placeholder="例如：夏日狂欢全场满减" />
        </el-form-item>
        
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="填写活动对客展示文案或内部备注说明..." resize="none" />
        </el-form-item>
        
        <el-form-item label="应用归属方">
          <el-select v-model="form.merchantId" placeholder="选择归属商户 (留空代表平台通用规则)" class="full-width" clearable>
            <el-option label="✨ 平台级别通用活动" :value="null" />
            <!-- 可以后续添加商户列表 -->
          </el-select>
        </el-form-item>

        <el-divider border-style="dashed" />
        
        <div class="form-section-title">
            阶梯满减规则
            <span class="title-sub">(支持配置多级满减)</span>
        </div>
        <div class="rules-container">
          <div v-for="(rule, index) in rulesList" :key="index" class="rule-card">
            <div class="rule-index">阶梯 {{ index + 1 }}</div>
            <div class="rule-inputs">
                <span class="rule-label">消费满</span>
                <el-input-number v-model="rule.threshold" :min="0" :precision="2" controls-position="right" class="rule-num-input" />
                <span class="rule-label">元，立减</span>
                <el-input-number v-model="rule.reduce" :min="0" :precision="2" controls-position="right" class="rule-num-input" />
                <span class="rule-label">元</span>
            </div>
            <el-button type="danger" plain circle icon="Delete" class="rule-del-btn" @click="removeRule(index)">
                ×
            </el-button>
          </div>
          
          <el-button dashed plain class="add-rule-btn" @click="addRule">
            ➕ 追加满减阶梯
          </el-button>
        </div>

        <el-divider border-style="dashed" />
        <div class="form-section-title">活动生失效时间</div>

        <el-form-item label="运营排期" required>
          <div class="date-picker-group">
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="开档时间" class="full-width" />
            <span class="date-separator">至</span>
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="撤档时间" class="full-width" />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="save-btn" :loading="saving">保存配置并发布</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityList, createActivity, updateActivity, deleteActivity } from '@/api/marketing'

const activityList = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)

const query = reactive({ name: '', current: 1, size: 10 })

const handleSearch = () => { query.current = 1; loadData() }
const handleSizeChange = () => { query.current = 1; loadData() }
const handlePageChange = () => { loadData() }

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = ref({
  name: '',
  description: '',
  rules: '',
  merchantId: null,
  startTime: '',
  endTime: ''
})

const rulesList = ref([{ threshold: 50, reduce: 5 }])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getActivityList(query)
    activityList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  try {
    const date = new Date(time)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const h = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${d} ${h}:${min}`
  } catch(e) {
    return time
  }
}

const parseRules = (rules) => {
  if (!rules) return []
  try {
    const arr = JSON.parse(rules)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const addRule = () => {
  rulesList.value.push({ threshold: 0, reduce: 0 })
}

const removeRule = (index) => {
  rulesList.value.splice(index, 1)
}

const handleCreate = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    name: '',
    description: '',
    rules: '',
    merchantId: null,
    startTime: '',
    endTime: ''
  }
  rulesList.value = [{ threshold: 50, reduce: 5 }]
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  try {
    rulesList.value = JSON.parse(row.rules)
  } catch {
    rulesList.value = [{ threshold: 50, reduce: 5 }]
  }
  if (!rulesList.value || rulesList.value.length === 0) {
      rulesList.value = [{ threshold: 0, reduce: 0 }]
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.startTime || !form.value.endTime) {
      ElMessage.warning('名称与生失效时间不能为空')
      return
  }
  
  if (rulesList.value.length === 0) {
      ElMessage.warning('请至少配置一项满减规则')
      return
  }

  saving.value = true
  try {
    const data = {
      ...form.value,
      rules: JSON.stringify(rulesList.value)
    }
    if (isEdit.value) {
      await updateActivity(editId.value, data)
      ElMessage.success('活动信息已更新')
    } else {
      await createActivity(data)
      ElMessage.success('满减活动创设成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作异常中断')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('这会导致正在享受优惠的用户活动中断，确认要撤销吗？', '销毁确认', { 
        type: 'error',
        confirmButtonText: '极其确定',
        cancelButtonText: '取消操作',
        confirmButtonClass: 'el-button--danger'
    })
    await deleteActivity(id)
    ElMessage.success('活动已从平台撤销')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '撤销操作失败')
    }
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

.search-input { width: 280px; }

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

.search-btn, .add-btn, .refresh-btn {
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

.add-btn {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
    border: none;
}
.add-btn:hover { background: linear-gradient(135deg, #059669 0%, #047857 100%); }

/* Table styles */
.data-table {
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.activity-cell {
    display: flex;
    align-items: center;
    gap: 12px;
}

.activity-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
    background: #fff1f2;
    color: #e11d48;
}

.activity-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.activity-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
}

.activity-desc {
    font-size: 13px;
    color: #64748b;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 200px;
}

.scope-tag {
    font-weight: 600;
    border: none;
}

.rules-cell {
    display: flex;
    flex-direction: column;
}

.rule-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.rule-chip {
    background: #fef2f2;
    border: 1px solid #fecaca;
    color: #ef4444;
    font-size: 12px;
    padding: 3px 8px;
    border-radius: 4px;
    white-space: nowrap;
}

.rule-chip .num {
    font-weight: 700;
    font-size: 13px;
    font-family: monospace;
}
.rule-chip .red { color: #dc2626; }

.no-rule {
    color: #94a3b8;
    font-size: 12px;
    font-style: italic;
}

.time-cell {
    display: flex;
    flex-direction: column;
    gap: 6px;
    text-align: left;
    display: inline-flex;
}

.time-row {
    font-size: 13px;
    color: #475569;
    font-family: monospace;
    display: flex;
    align-items: center;
    gap: 6px;
}

.time-dot {
    width: 6px; height: 6px; border-radius: 50%;
}
.time-dot.start { background-color: #10b981; }
.time-dot.end { background-color: #ef4444; }

.status-tag {
    border: none;
    font-weight: 600;
    padding: 0 12px;
    border-radius: 20px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.status-dot {
    width: 6px; height: 6px; border-radius: 50%;
}
.dot-success { background-color: #10b981; }
.dot-info { background-color: #94a3b8; }

.row-actions {
    display: flex;
    align-items: center;
    justify-content: center;
}

.action-btn { font-weight: 600; }
.action-divider { margin: 0 4px; }

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

/* Dialog & Rule Form Styling */
:deep(.custom-dialog) {
    border-radius: 12px;
    overflow: hidden;
}

:deep(.custom-dialog .el-dialog__header) {
    margin-right: 0;
    padding: 20px 24px;
    border-bottom: 1px solid #f8fafc;
    background: #f8fafc;
    font-weight: 700;
}

:deep(.custom-dialog .el-dialog__body) {
    padding: 24px;
}

:deep(.custom-dialog .el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #f1f5f9;
}

.form-section-title {
    font-size: 14px;
    font-weight: 700;
    color: #3b82f6;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
}
.form-section-title.title-sub {
    font-size: 12px; color: #94a3b8; font-weight: normal; margin-left: 6px;
}
.form-section-title::before {
    content: '';
    display: block;
    width: 4px;
    height: 14px;
    background: #3b82f6;
    border-radius: 2px;
}

.rules-container {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 24px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.rule-card {
    display: flex;
    align-items: center;
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 12px 16px;
    gap: 16px;
    transition: all 0.2s;
}
.rule-card:hover { border-color: #cbd5e1; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }

.rule-index {
    font-size: 12px;
    font-weight: 600;
    color: #64748b;
    background: #f1f5f9;
    padding: 2px 8px;
    border-radius: 10px;
}

.rule-inputs {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
}

.rule-label { font-size: 13px; color: #334155; }
.rule-num-input { width: 110px; }

.rule-del-btn {
    width: 28px; height: 28px; min-height: 28px;
    font-size: 16px;
    display: flex; align-items: center; justify-content: center;
}

.add-rule-btn {
    align-self: flex-start;
    border-style: dashed;
    margin-top: 4px;
    border-radius: 8px;
}

.date-picker-group {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
}

.date-separator {
    color: #94a3b8;
    font-weight: 500;
}

.full-width { width: 100%; }
.activity-form :deep(.el-form-item__label) {
    font-weight: 600;
    color: #475569;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.cancel-btn, .save-btn {
    border-radius: 8px;
    padding: 8px 20px;
    font-weight: 500;
}
</style>
