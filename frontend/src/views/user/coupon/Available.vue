<template>
  <div class="coupon-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <el-icon color="#ff6b6b" size="20"><Discount /></el-icon>
          <span>领券中心</span>
        </div>
      </template>

      <div class="coupon-list">
        <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card">
          <div class="coupon-left">
            <div class="coupon-value">
              <span v-if="coupon.type === 'DISCOUNT'">{{ coupon.value / 10 }}<small>折</small></span>
              <span v-else><small>¥</small>{{ coupon.value }}</span>
            </div>
            <div class="coupon-type">
              {{ coupon.type === 'DISCOUNT' ? '折扣券' : coupon.type === 'REDUCE' ? '满减券' : '代金券' }}
            </div>
          </div>
          <div class="coupon-right">
            <div class="right-info">
              <div class="coupon-name">{{ coupon.name }}</div>
              <div class="coupon-condition">满{{ coupon.minAmount }}元可用</div>
              <div class="coupon-time">{{ formatTime(coupon.endTime) }} 到期</div>
              <div class="coupon-stock" v-if="coupon.remainCount > 0">限量剩余 {{ coupon.remainCount }} 张</div>
              <div class="coupon-stock empty-stock" v-else>已被抢光</div>
            </div>
            <div class="right-action">
              <el-button 
                v-if="!coupon.received" 
                type="primary" 
                class="receive-btn"
                :disabled="coupon.remainCount <= 0"
                @click="handleReceive(coupon.id)"
              >
                {{ coupon.remainCount > 0 ? '立即领取' : '已抢光' }}
              </el-button>
              <el-button v-else type="info" class="received-btn" disabled>已领取</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="availableCoupons.length === 0" description="一大波优惠券正在赶来的路上..." :image-size="140" />
      
      <div class="pagination-wrapper" v-if="availableTotal > 0">
        <el-pagination
            background
            v-model:current-page="availableQuery.current"
            v-model:page-size="availableQuery.size"
            :page-sizes="[10, 20, 50]"
            :total="availableTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="v => { availableQuery.size = v; availableQuery.current = 1; loadAvailableCoupons(); }"
            @current-change="v => { availableQuery.current = v; loadAvailableCoupons(); }"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCoupons, receiveCoupon } from '@/api/coupon'
import { Discount } from '@element-plus/icons-vue'

const availableCoupons = ref([])
const availableQuery = reactive({ current: 1, size: 10 })
const availableTotal = ref(0)

const loadAvailableCoupons = async () => {
  try {
    const res = await getAvailableCoupons(availableQuery)
    availableCoupons.value = res.data.records || []
    availableTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

const handleReceive = async (id) => {
  try {
    await receiveCoupon(id)
    ElMessage.success('领取成功')
    loadAvailableCoupons()
  } catch (e) {
    ElMessage.error(e.message || '领取失败')
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString()
}

onMounted(() => {
  loadAvailableCoupons()
})
</script>

<style scoped>
.coupon-page {
  padding: 16px 24px;
  animation: fadeIn 0.4s ease-out;
}
.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}
.card-header {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}
.coupon-card {
  display: flex;
  height: 124px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  overflow: hidden;
}
.coupon-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.15);
  border-color: #ffcccc;
}
.coupon-left {
  width: 120px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.coupon-left::after {
  content: '';
  position: absolute;
  right: -2px;
  top: 0;
  bottom: 0;
  width: 0;
  border-right: 4px dotted #fff;
}
.coupon-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1.1;
  display: flex;
  align-items: baseline;
}
.coupon-value small {
  font-size: 16px;
  margin: 0 2px;
}
.coupon-type {
  font-size: 12px;
  opacity: 0.95;
  margin-top: 8px;
  background: rgba(255, 255, 255, 0.25);
  padding: 3px 10px;
  border-radius: 12px;
}
.coupon-right {
  flex: 1;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  background: #fff;
}
.right-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  justify-content: center;
}
.coupon-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.coupon-condition, .coupon-time {
  font-size: 12px;
  color: #909399;
}
.coupon-stock {
  font-size: 12px;
  color: #ff6b6b;
  margin-top: 2px;
}
.coupon-stock.empty-stock {
  color: #909399;
}
.right-action {
  display: flex;
  align-items: center;
  padding-left: 10px;
}
.receive-btn {
  border-radius: 20px;
  padding: 10px 18px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  border: none;
  font-weight: 600;
  transition: all 0.3s;
}
.receive-btn:hover {
  opacity: 0.9;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}
.received-btn {
  border-radius: 20px;
  padding: 10px 18px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
