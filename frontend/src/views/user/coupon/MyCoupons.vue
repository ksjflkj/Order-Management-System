<template>
  <div class="coupon-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <el-icon color="#FFC300" size="20"><Wallet /></el-icon>
          <span>我的卡包</span>
        </div>
      </template>

      <div class="coupon-list">
        <div v-for="coupon in myCoupons" :key="coupon.id" class="coupon-card" :class="coupon.status">
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
              <div class="coupon-time">有效期至 {{ formatTime(coupon.endTime) }}</div>
            </div>
            <div class="right-status">
              <el-tag v-if="coupon.status === 'UNUSED'" type="primary" effect="light" round size="large" class="use-tag">可使用</el-tag>
              <div v-else-if="coupon.status === 'USED'" class="stamp used-stamp">已使用</div>
              <div v-else class="stamp expired-stamp">已过期</div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="myCoupons.length === 0" description="你的卡包空空如也" :image-size="140">
        <el-button type="primary" plain round @click="$router.push('/user/coupon/available')">去寻找优惠券</el-button>
      </el-empty>

      <div class="pagination-wrapper" v-if="myTotal > 0">
        <el-pagination
            background
            v-model:current-page="myQuery.current"
            v-model:page-size="myQuery.size"
            :page-sizes="[10, 20, 50]"
            :total="myTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="v => { myQuery.size = v; myQuery.current = 1; loadMyCoupons(); }"
            @current-change="v => { myQuery.current = v; loadMyCoupons(); }"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyCoupons } from '@/api/coupon'
import { Wallet } from '@element-plus/icons-vue'

const myCoupons = ref([])
const myQuery = reactive({ current: 1, size: 10, status: '' })
const myTotal = ref(0)

const loadMyCoupons = async () => {
  try {
    const res = await getMyCoupons(myQuery)
    myCoupons.value = res.data.records || []
    myTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString()
}

onMounted(() => {
  loadMyCoupons()
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
  position: relative;
}
.coupon-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
  border-color: #c6e2ff;
}
.coupon-card.USED, .coupon-card.EXPIRED {
  filter: grayscale(1);
  opacity: 0.65;
}
.coupon-card.USED:hover, .coupon-card.EXPIRED:hover {
  transform: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border-color: #ebeef5;
}

.coupon-left {
  width: 120px;
  background: linear-gradient(135deg, #FFC300, #FFD44D);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.coupon-card.USED .coupon-left, .coupon-card.EXPIRED .coupon-left {
  background: linear-gradient(135deg, #a0cfff, #c6e2ff);
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
  background: #fff;
  position: relative;
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
.right-status {
  display: flex;
  align-items: center;
  justify-content: center;
}
.use-tag {
  font-weight: bold;
}
.stamp {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  transform: rotate(-25deg);
  border: 2px solid;
}
.used-stamp {
  color: #909399;
  border-color: #909399;
}
.expired-stamp {
  color: #f56c6c;
  border-color: #f56c6c;
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
