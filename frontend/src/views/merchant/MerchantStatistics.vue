<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">商户数据罗盘</h2>
        <p class="page-desc">实时监控商铺的营收流水、客单转化以及爆款菜品的销售动态</p>
      </div>
    </div>

    <!-- 顶层核心数据指标卡片 -->
    <el-row :gutter="24" class="overview-row">
      <el-col :span="12">
        <div class="dashboard-card stat-card conversion-card">
          <div class="stat-icon-bg">
            <span class="emoji">📈</span>
          </div>
          <div class="stat-content">
            <div class="stat-label">订单履约转化率</div>
            <div class="stat-value">
              <span class="num">{{ statistics.orderConversionRate || '0.0' }}</span>
              <span class="unit">%</span>
            </div>
            <div class="stat-footer">反映真实接单与完结占总单量的比率，代表您的客源消化力。</div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="dashboard-card stat-card refund-card">
          <div class="stat-icon-bg">
            <span class="emoji">📉</span>
          </div>
          <div class="stat-content">
            <div class="stat-label">订单流失 (退款/取消) 率</div>
            <div class="stat-value">
              <span class="num">{{ statistics.refundRate || '0.0' }}</span>
              <span class="unit">%</span>
            </div>
            <div class="stat-footer">反映支付后交易取消占总单量的比率，请务必控制在 5% 以下。</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表分析视图区 -->
    <el-row :gutter="24" class="charts-row">
      <el-col :span="16">
        <el-card shadow="never" class="dashboard-card chart-card">
          <template #header>
            <div class="card-title">
              <span class="title-icon">📊</span>
              近 7 日销售额与单量趋势
            </div>
          </template>
          <div ref="salesChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="dashboard-card chart-card">
          <template #header>
            <div class="card-title">
              <span class="title-icon">🔥</span>
              热销菜品 Top 5 榜单
            </div>
          </template>
          <div ref="hotDishesChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMerchantStatistics } from '@/api/merchantStatistics'

const statistics = ref({
  orderConversionRate: 0,
  refundRate: 0,
  dailySales: [],
  hotDishes: []
})

const salesChartRef = ref(null)
const hotDishesChartRef = ref(null)

const initSalesChart = () => {
  if (!salesChartRef.value) return
  const chart = echarts.init(salesChartRef.value)
  
  const dates = statistics.value.dailySales.map(item => item.date)
  const amounts = statistics.value.dailySales.map(item => item.totalAmount)
  const counts = statistics.value.dailySales.map(item => item.orderCount)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', crossStyle: { color: '#999' } },
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      padding: [10, 15],
      textStyle: { color: '#334155' },
      borderColor: '#e2e8f0',
      borderWidth: 1,
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    // 将图例放置在顶部正中央，并且与图表保持 20px 距离，防止重合
    legend: {
      data: ['销售营收金额', '履单数量'],
      top: 0,
      itemGap: 24,
      textStyle: { fontSize: 13, color: '#475569', fontWeight: 500 }
    },
    // 重新规划网格的边距，增大 top 和 bottom，彻底解决 X轴时间轴和图例的文字遮挡碰撞问题
    grid: {
      top: '12%',
      left: '2%',
      right: '2%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: dates,
        axisPointer: { type: 'shadow' },
        axisLine: { lineStyle: { color: '#cbd5e1' } },
        axisLabel: { color: '#64748b', fontSize: 12, margin: 12 }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '销售营收 (元)',
        nameTextStyle: { color: '#94a3b8', padding: [0, 0, 0, 30] },
        axisLabel: { formatter: '¥{value}', color: '#64748b' },
        splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }
      },
      {
        type: 'value',
        name: '订单量 (单)',
        nameTextStyle: { color: '#94a3b8', padding: [0, 30, 0, 0] },
        axisLabel: { formatter: '{value} 单', color: '#64748b' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '销售营收金额',
        type: 'bar',
        data: amounts,
        barMaxWidth: '32',
        itemStyle: { 
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#60a5fa' },
                { offset: 1, color: '#3b82f6' }
            ]),
            borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '履单数量',
        type: 'line',
        yAxisIndex: 1,
        data: counts,
        symbolSize: 8,
        itemStyle: { color: '#10b981', borderWidth: 2, borderColor: '#fff' },
        lineStyle: { width: 3, shadowColor: 'rgba(16,185,129,0.3)', shadowBlur: 8, shadowOffsetY: 4 },
        smooth: true
      }
    ]
  }
  chart.setOption(option)
}

const initHotDishesChart = () => {
  if (!hotDishesChartRef.value) return
  const chart = echarts.init(hotDishesChartRef.value)

  const data = statistics.value.hotDishes.map(item => ({
    name: item.dishName,
    value: item.salesCount
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b} <br/>售出: {c}份 ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      padding: [10, 15],
      textStyle: { color: '#334155' },
      borderColor: '#e2e8f0',
      borderWidth: 1,
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    legend: {
      orient: 'horizontal',
      bottom: '0%', /* 贴着底部 */
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#64748b' }
    },
    color: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
    series: [
      {
        name: '热销菜品',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '42%'], /* 饼图稍微上移给图例留空间 */
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: '#1e293b'
          }
        },
        labelLine: {
          show: false
        },
        data: data.length > 0 ? data : [{name: '暂无销售数据', value: 0}]
      }
    ]
  }
  chart.setOption(option)
}

const loadData = async () => {
  try {
    const res = await getMerchantStatistics()
    if (res.code === 200) {
      if(res.data) {
         statistics.value = res.data
      }
      await nextTick()
      initSalesChart()
      initHotDishesChart()
    }
  } catch (error) {
    console.error('获取罗盘统计数据失败:', error)
  }
}

onMounted(() => {
  loadData()
  
  const resizeHandler = () => {
    if (salesChartRef.value) echarts.getInstanceByDom(salesChartRef.value)?.resize()
    if (hotDishesChartRef.value) echarts.getInstanceByDom(hotDishesChartRef.value)?.resize()
  }
  window.addEventListener('resize', resizeHandler)
  
  // Cleanup
  return () => window.removeEventListener('resize', resizeHandler)
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
    margin-bottom: 24px;
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

.dashboard-card {
    border-radius: 12px;
    border: none;
    background: #ffffff;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
}

.overview-row {
    margin-bottom: 24px;
}

.stat-card {
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 20px;
    position: relative;
    overflow: hidden;
    height: 100%;
    box-sizing: border-box;
    transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.stat-icon-bg {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    flex-shrink: 0;
    z-index: 1;
}

.conversion-card .stat-icon-bg { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
.refund-card .stat-icon-bg { background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); }

.stat-content {
    display: flex;
    flex-direction: column;
    z-index: 1;
    flex: 1;
}

.stat-label {
    font-size: 14px;
    color: #64748b;
    font-weight: 600;
    margin-bottom: 6px;
}

.stat-value {
    display: flex;
    align-items: baseline;
    gap: 4px;
    font-family: monospace;
    margin-bottom: 8px;
}

.conversion-card .num { color: #059669; }
.refund-card .num { color: #dc2626; }

.stat-value .num {
    font-size: 36px;
    font-weight: 800;
    letter-spacing: -1px;
    line-height: 1;
}

.stat-value .unit {
    font-size: 18px;
    font-weight: 600;
    color: #94a3b8;
}

.stat-footer {
    font-size: 12px;
    color: #94a3b8;
    line-height: 1.5;
}

/* 图表区样式 */
.chart-card {
    height: 100%;
}

:deep(.chart-card .el-card__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #f8fafc;
}

:deep(.chart-card .el-card__body) {
    padding: 24px;
}

.card-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    display: flex;
    align-items: center;
    gap: 8px;
}

.title-icon {
    font-size: 20px;
}

.chart-container {
    height: 380px;
    width: 100%;
}
</style>
