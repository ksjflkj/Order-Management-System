<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">营销优惠券管理</h2>
        <p class="page-desc">创建和维护平台各类折扣券、满减券及代金券等营销活动</p>
      </div>
    </div>

    <el-card class="main-card" shadow="never">
      <!-- 搜索与工具栏 -->
      <div class="toolbar">
        <div class="filter-group">
          <el-input v-model="query.name" placeholder="检索优惠券名称..." clearable class="filter-item search-input" @keyup.enter="handleSearch">
            <template #prefix>
              <span class="prefix-icon">🎟️</span>
            </template>
          </el-input>
          
          <el-select v-model="query.type" placeholder="全部卡券类型" clearable class="filter-item type-select" @change="handleSearch">
            <el-option label="🌟 折扣券 (打折)" value="DISCOUNT" />
            <el-option label="💰 满减券 (直减)" value="REDUCE" />
            <el-option label="💵 代金券 (无门槛)" value="CASH" />
          </el-select>

          <el-button type="primary" class="search-btn" @click="handleSearch">
            检索卡券
          </el-button>
        </div>

        <div class="action-group">
          <el-button @click="handleSearch" plain class="refresh-btn">
            刷新
          </el-button>
          <el-button type="primary" class="add-btn" @click="handleCreate">
            ➕ 增发优惠券
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="couponList" v-loading="loading" class="data-table"
                :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '54px', borderBottom: '1px solid #e2e8f0' }"
                :row-style="{ height: '68px' }">
        
        <el-table-column label="优惠券基本信息" min-width="260">
          <template #default="{ row }">
            <div class="coupon-cell">
              <div class="coupon-icon" :class="'icon-' + row.type.toLowerCase()">
                {{ getCouponIcon(row.type) }}
              </div>
              <div class="coupon-main">
                <div class="coupon-name">{{ row.name }}</div>
                <div class="coupon-type-tag">
                  <el-tag :type="getCouponTagType(row.type)" size="small" effect="plain" round>
                    {{ getCouponTypeName(row.type) }}
                  </el-tag>
                  <span class="coupon-value-text" v-if="row.type === 'DISCOUNT'">{{ formatDiscount(row.value) }} 折优惠</span>
                  <span class="coupon-value-text" v-else>抵扣 ¥{{ row.value }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="使用门槛" width="140" align="center">
          <template #default="{ row }">
            <div class="threshold-cell">
              <span class="amount-val" v-if="row.minAmount > 0">满 ¥{{ row.minAmount }} 可用</span>
              <span class="no-threshold" v-else>无金额门槛</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="库存并发放状态" width="180" align="center">
          <template #default="{ row }">
            <div class="stock-cell">
              <div class="stock-item">
                <span class="stock-label">发放限制:</span>
                <span class="stock-val">{{ row.totalCount === 0 ? '不限量' : row.totalCount + ' 张' }}</span>
              </div>
              <div class="stock-item">
                <span class="stock-label">剩余库存:</span>
                <span class="stock-val remain-val" :class="{'low-stock': row.remainCount <= 10 && row.totalCount > 0}">
                  {{ row.totalCount === 0 ? '充足' : row.remainCount + ' 张' }}
                </span>
              </div>
              <div class="stock-item limit-item">
                <span class="limit-val">🆔 每人限领 {{ row.eachLimit }} 张</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="活动效期" width="160" align="center">
          <template #default="{ row }">
            <div class="time-cell">
              <div class="time-row"><span class="time-dot start"></span>{{ formatTime(row.startTime) }}</div>
              <div class="time-row"><span class="time-dot end"></span>{{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" effect="light" class="status-tag" round>
              <span class="status-dot" :class="row.status === 'ACTIVE' ? 'dot-success' : 'dot-danger'"></span>
              {{ row.status === 'ACTIVE' ? '生效中' : '已失效' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="管理操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" class="action-btn" @click="handleEdit(row)">编辑配置</el-button>
              <el-divider direction="vertical" class="action-divider" />
              <el-button link type="danger" class="action-btn" @click="handleDelete(row.id)">作废</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon">🎟️</span>
            <p class="empty-text">后台暂无配置任何优惠券数据</p>
          </div>
        </template>
      </el-table>

      <div class="pagination-container" v-if="total > 0 || couponList.length > 0">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券配置' : '增发新优惠券'" width="600px" class="custom-dialog" destroy-on-close :close-on-click-modal="false">
      <el-form :model="form" label-width="110px" class="coupon-form" label-position="left">
        <div class="form-section-title">基础规则设定</div>
        <el-form-item label="卡券名称" required>
          <el-input v-model="form.name" placeholder="例如：春季特惠满100减20" />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="卡券类型" class="flex-1" required>
            <el-select v-model="form.type" placeholder="选择类型" class="full-width">
              <el-option label="折扣券 (打折)" value="DISCOUNT" />
              <el-option label="满减券 (直减)" value="REDUCE" />
              <el-option label="代金券 (无门槛)" value="CASH" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="核心面值" required class="value-item">
          <el-input-number v-model="form.value" :min="0" :precision="2" controls-position="right" class="value-input" />
          <div class="value-hint">
            <span v-if="form.type === 'DISCOUNT'" class="hint-discount">🎟️ 折扣计算: 如填 80 代表 <strong>8折</strong>，75 代表 <strong>7.5折</strong></span>
            <span v-else class="hint-money">💰 直接减免: 代表用户实付时直接扣减此 <strong>元</strong> 额度</span>
          </div>
        </el-form-item>

        <el-form-item label="使用门槛" required>
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" controls-position="right" />
          <span class="item-trailing-hint">元 (提示：填 0 则无门槛)</span>
        </el-form-item>

        <el-divider border-style="dashed" />
        <div class="form-section-title">发放与领取规则</div>

        <div class="form-row">
          <el-form-item label="发行总数量" class="flex-1" required>
            <el-input-number v-model="form.totalCount" :min="0" :step="100" controls-position="right" class="full-width" />
            <div class="mini-hint">0 表示无限量发行</div>
          </el-form-item>
          
          <el-form-item label="单人限领" class="flex-1" required>
            <el-input-number v-model="form.eachLimit" :min="1" controls-position="right" class="full-width" />
            <div class="mini-hint">每位用户最多领取张数</div>
          </el-form-item>
        </div>

        <el-divider border-style="dashed" />
        <div class="form-section-title">活动效期区间</div>

        <el-form-item label="有效时间段" required>
          <div class="date-picker-group">
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="领用生效时间" class="full-width" />
            <span class="date-separator">至</span>
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="自动失效时间" class="full-width" />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">抛弃更改</el-button>
          <el-button type="primary" @click="handleSubmit" class="save-btn" :loading="saving">确认投产部署</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponList, createCoupon, updateCoupon, deleteCoupon } from '@/api/marketing'

const couponList = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)

const query = reactive({ name: '', type: '', current: 1, size: 10 })

const handleSearch = () => { query.current = 1; loadData() }
const handleSizeChange = () => { query.current = 1; loadData() }
const handlePageChange = () => { loadData() }

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = ref({
  name: '',
  type: 'REDUCE',
  value: 10,
  minAmount: 0,
  totalCount: 100,
  eachLimit: 1,
  startTime: '',
  endTime: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCouponList(query)
    couponList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 格式化时间为友好输出，去除秒
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
  } catch (e) {
    return time
  }
}

const formatDiscount = (val) => {
    return parseFloat((val / 10).toFixed(1))
}

const getCouponIcon = (type) => {
    if (type === 'DISCOUNT') return '🌟'
    if (type === 'REDUCE') return '💰'
    return '💵'
}

const getCouponTypeName = (type) => {
    if (type === 'DISCOUNT') return '折扣券'
    if (type === 'REDUCE') return '满减券'
    return '代金券'
}

const getCouponTagType = (type) => {
    if (type === 'DISCOUNT') return 'warning'
    if (type === 'REDUCE') return 'danger'
    return 'primary'
}

const handleCreate = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    name: '',
    type: 'REDUCE',
    value: 10,
    minAmount: 0,
    totalCount: 100,
    eachLimit: 1,
    startTime: '',
    endTime: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.startTime || !form.value.endTime) {
      ElMessage.warning('请填写完整的核心信息（名称、效期）')
      return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateCoupon(editId.value, form.value)
      ElMessage.success('优惠券配置已更新')
    } else {
      await createCoupon(form.value)
      ElMessage.success('新优惠券已成功投产发放')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '配置同步失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定作废该优惠券吗？作废后不可恢复且用户将无法继续领取！', '作废严重警告', { 
        type: 'warning',
        confirmButtonText: '确定作废',
        cancelButtonText: '放弃操作',
        confirmButtonClass: 'el-button--danger'
    })
    await deleteCoupon(id)
    ElMessage.success('卡券已成功作废分离')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '作废接口阻断失败')
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

.search-input { width: 240px; }
.type-select { width: 180px; }

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

.add-btn:hover {
    background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

/* Data Table styling */
.data-table {
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.coupon-cell {
    display: flex;
    align-items: center;
    gap: 12px;
}

.coupon-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
}

.icon-discount { background: #fef3c7; color: #d97706; }
.icon-reduce { background: #fee2e2; color: #ef4444; }
.icon-cash { background: #e0e7ff; color: #4f46e5; }

.coupon-main {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.coupon-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
}

.coupon-type-tag {
    display: flex;
    align-items: center;
    gap: 8px;
}

.coupon-value-text {
    font-size: 13px;
    color: #ef4444;
    font-weight: 700;
}

.threshold-cell {
    font-weight: 500;
    color: #334155;
}

.no-threshold {
    color: #10b981;
    background: #d1fae5;
    padding: 2px 8px;
    border-radius: 12px;
    font-size: 12px;
}

.stock-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;
    text-align: left;
    display: inline-flex;
}

.stock-item {
    font-size: 12px;
    display: flex;
    justify-content: space-between;
    gap: 12px;
}

.stock-label { color: #64748b; }
.stock-val { color: #334155; font-weight: 500; }
.remain-val { color: #10b981; font-weight: 600; }
.low-stock { color: #ef4444; }

.limit-item {
    margin-top: 2px;
    padding-top: 2px;
    border-top: 1px dashed #e2e8f0;
}
.limit-val { color: #e98a8a; font-weight: 500; }

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
.dot-danger { background-color: #ef4444; }

.row-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
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

/* Dialog Form Styling */
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
.form-section-title::before {
    content: '';
    display: block;
    width: 4px;
    height: 14px;
    background: #3b82f6;
    border-radius: 2px;
}

.form-row {
    display: flex;
    gap: 16px;
}

.flex-1 { flex: 1; }
.full-width { width: 100%; }

.coupon-form :deep(.el-form-item__label) {
    font-weight: 600;
    color: #475569;
}

.value-item :deep(.el-form-item__content) {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
}

.value-input {
    width: 100%;
}

.value-hint {
    font-size: 13px;
    background: #fffbeb;
    padding: 6px 12px;
    border-radius: 6px;
    border: 1px solid #fde68a;
    width: 100%;
    box-sizing: border-box;
}

.hint-discount { color: #d97706; }
.hint-money { color: #cf222e; }

.item-trailing-hint {
    margin-left: 12px;
    font-size: 13px;
    color: #94a3b8;
}

.mini-hint {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
    line-height: 1.2;
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
