<template>
  <div class="settings-content-wrapper">
    <div class="settings-content-card">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">收货地址管理</h2>
          <p class="section-desc">修改或新增您的常用配送地址，下单时可快捷选用。</p>
        </div>
        <button type="button" class="modern-btn btn-primary" @click="goAdd">
          <el-icon style="margin-right: 6px;"><Plus /></el-icon>
          新增地址
        </button>
      </div>

      <div v-if="addressList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无收货地址记录" :image-size="160">
        </el-empty>
      </div>

      <div v-else class="address-list">
        <div
          v-for="item in addressList"
          :key="item.id"
          class="address-card"
          :class="{ 'is-default': item.isDefault === 1 }"
        >
          <div class="card-left">
            <div class="address-header">
              <span class="contact-name">{{ item.contactName }}</span>
              <span class="contact-phone">{{ item.contactPhone }}</span>
              <span v-if="item.isDefault === 1" class="default-tag">默认地址</span>
            </div>
            <div class="full-address">
              <el-icon class="addr-icon"><Location /></el-icon>
              <span>{{ item.province || '' }}{{ item.city || '' }}{{ item.district || '' }} {{ item.detailAddress }}</span>
            </div>
          </div>
          <div class="card-actions">
            <button type="button" class="action-btn edit-btn" @click="goEdit(item.id)">
              编辑
            </button>
            <div class="action-divider"></div>
            <button type="button" class="action-btn delete-btn" @click="handleDelete(item.id)">
              删除
            </button>
          </div>
        </div>
      </div>

      <div class="pagination-wrapper" v-if="total > query.size">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { deleteAddress, getAddressList } from '@/api/address'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const addressList = ref([])
const query = reactive({ current: 1, size: 10 })
const total = ref(0)
const loading = ref(false)

const goAdd = () => router.push('/user/address/add')
const goEdit = (id) => router.push(`/user/address/edit/${id}`)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAddressList(query)
    if (res.code === 200) {
      addressList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => {
  query.current = val
  loadData()
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要永久删除该地址吗？', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteAddress(id)
    ElMessage.success(res.message || '删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.settings-content-wrapper {
  animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  justify-content: center;
  width: 100%;
}

.settings-content-card {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  padding: 48px 56px;
  width: 100%;
  max-width: 1400px;
  border: 1px solid #f1f5f9;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.section-title {
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.section-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.empty-state {
  padding: 60px 0;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

/* Address Card Design */
.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 28px 32px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.address-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 4px;
  background: transparent;
  transition: all 0.3s;
}

.address-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.address-card.is-default {
  border-color: #a7f3d0;
  background: #f0fdf4;
}

.address-card.is-default::before {
  background: #10b981;
}

.card-left {
  flex: 1;
  padding-right: 16px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.contact-name {
  font-family: 'Plus Jakarta Sans', system-ui;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.contact-phone {
  font-family: 'Plus Jakarta Sans', monospace;
  font-size: 15px;
  color: #475569;
  font-weight: 600;
}

.default-tag {
  font-size: 12px;
  color: #059669;
  background: #d1fae5;
  padding: 4px 12px;
  border-radius: 8px;
  font-weight: 700;
}

.full-address {
  font-size: 15px;
  color: #64748b;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.6;
}

.addr-icon {
  flex-shrink: 0;
  margin-top: 4px;
  color: #94a3b8;
  font-size: 16px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.action-divider {
  width: 1px;
  height: 16px;
  background: #e2e8f0;
}

.action-btn {
  background: transparent;
  border: none;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  padding: 8px;
  transition: all 0.2s ease;
  border-radius: 6px;
}

.edit-btn {
  color: #FFC300;
}
.edit-btn:hover {
  background: #FFECB3;
}

.delete-btn {
  color: #ef4444;
}
.delete-btn:hover {
  background: #fef2f2;
}

/* Modern Buttons */
.modern-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  border: none;
  font-family: inherit;
  height: 48px;
}

.btn-primary {
  background: #FFC300;
  color: #222222;
  box-shadow: 0 4px 12px rgba(255, 195, 0, 0.25);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 195, 0, 0.35);
  background: #E5AF00;
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  .address-list {
    grid-template-columns: 1fr;
  }
}
</style>
