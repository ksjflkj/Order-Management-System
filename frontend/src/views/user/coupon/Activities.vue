<template>
  <div class="coupon-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <el-icon color="#00f2fe" size="20"><Calendar /></el-icon>
          <span>本期活动</span>
        </div>
      </template>

      <div class="coupon-list">
        <div v-for="act in activeActivities" :key="act.id" class="coupon-card activity-card">
          <div class="coupon-left">
            <div class="coupon-value" style="font-size: 16px;">
              <span>{{ act.merchantName || '平台活动' }}</span>
            </div>
            <div class="coupon-type">满减活动</div>
          </div>
          <div class="coupon-right">
            <div class="right-info">
              <div class="coupon-name">{{ act.name }}</div>
              <div class="coupon-condition">{{ act.description }}</div>
              <div class="coupon-time">{{ formatTime(act.startTime) }} - {{ formatTime(act.endTime) }}</div>
            </div>
            <div class="right-action">
              <el-tag type="success" effect="plain" round>火热进行中</el-tag>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="activeActivities.length === 0" description="暂无进行中的活动" :image-size="140" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getActiveActivities } from '@/api/coupon'
import { Calendar } from '@element-plus/icons-vue'

const activeActivities = ref([])

const loadActiveActivities = async () => {
  try {
    const res = await getActiveActivities()
    activeActivities.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载活动失败')
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString()
}

onMounted(() => {
  loadActiveActivities()
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
.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 242, 254, 0.2);
  border-color: #a1c4fd;
}
.coupon-left {
  width: 130px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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
  font-weight: bold;
  text-align: center;
  word-break: break-all;
  padding: 0 10px;
}
.coupon-value span {
  font-size: 18px;
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
.right-action {
  display: flex;
  align-items: center;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
