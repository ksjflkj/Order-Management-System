<template>
    <div class="dashboard-container">
        <!-- Dashboard Header -->
        <div class="dashboard-header">
            <div class="header-left">
                <div class="title-icon-wrapper">
                    <el-icon><Trophy /></el-icon>
                </div>
                <div class="title-text">
                    <h2 class="sys-title">管理数据大盘</h2>
                    <p class="sys-desc">实时掌控全平台各项运营核心指标，数据驱动业务增长。</p>
                </div>
            </div>
            <div class="header-right">
                <span class="refresh-time">数据状态: <span class="live-dot"></span> 实时连接</span>
                <button class="modern-btn btn-primary" @click="loadData">
                    <el-icon style="margin-right:6px; font-size:16px"><RefreshRight /></el-icon> 手动刷新
                </button>
            </div>
        </div>

        <!-- Metrics Grid Layout -->
        <div class="metrics-grid">
            
            <!-- GMV Highlight Premium Card (Meituan Brand Style) -->
            <div class="metric-card gmv-card">
                <div class="card-bg-decoration">
                    <el-icon><DataLine /></el-icon>
                </div>
                <div class="gmv-title">平台累计总成交额 (GMV)</div>
                <div class="gmv-value">
                    <span class="currency">¥</span>
                    <span class="num">{{ dashboard.totalSalesAmount ?? 0 }}</span>
                </div>
                <div class="gmv-footer">
                    稳定高效的商业变现通道，持续创造营收价值。
                </div>
            </div>

            <!-- Standard Metric Cards -->
            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-user"><el-icon><User /></el-icon></div>
                    <span class="card-name">平台注册用户</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ dashboard.userCount ?? 0 }} <span class="unit">名</span></div>
                    <div class="sub-info">海量用户群构建强大生态体系</div>
                </div>
            </div>

            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-shop"><el-icon><Shop /></el-icon></div>
                    <span class="card-name">入驻合作商家</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ dashboard.merchantCount ?? 0 }} <span class="unit">家</span></div>
                    <div class="sub-info">提供覆盖全城的服务与支撑</div>
                </div>
            </div>

            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-goods"><el-icon><Goods /></el-icon></div>
                    <span class="card-name">平台美食佳肴</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ dashboard.dishCount ?? 0 }} <span class="unit">道</span></div>
                    <div class="sub-info">海量SKU丰富平台消费选择</div>
                </div>
            </div>

            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-order"><el-icon><List /></el-icon></div>
                    <span class="card-name">累计撮合订单</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ dashboard.orderCount ?? 0 }} <span class="unit">笔</span></div>
                    <div class="sub-info">系统高并发稳定承接所有的交易流水</div>
                </div>
            </div>

            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-success"><el-icon><CircleCheck /></el-icon></div>
                    <span class="card-name">成功支付订单</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ dashboard.paidOrderCount ?? 0 }} <span class="unit">笔</span></div>
                    <div class="sub-info">转化为实际营收的核准业绩</div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getAdminDashboard } from '@/api/adminDashboard'
import { Trophy, RefreshRight, DataLine, User, Shop, Goods, List, CircleCheck } from '@element-plus/icons-vue'

const dashboard = ref({})

const loadData = async () => {
    const res = await getAdminDashboard()
    if (res.code === 200) {
        dashboard.value = res.data || {}
    }
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.dashboard-container {
    padding: 10px 16px 40px 16px;
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Header Config */
.dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.title-icon-wrapper {
    width: 64px;
    height: 64px;
    background: #FFC300;
    color: #222222;
    font-size: 32px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 24px rgba(255, 195, 0, 0.3);
}

.title-text .sys-title {
    font-size: 28px;
    font-family: 'Plus Jakarta Sans', system-ui;
    font-weight: 800;
    color: #1a1a1a;
    margin: 0 0 6px 0;
    letter-spacing: -0.02em;
}

.title-text .sys-desc {
    margin: 0;
    color: #64748b;
    font-size: 15px;
    font-weight: 500;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 24px;
}

.refresh-time {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #222222;
    font-weight: 600;
    background: #ffffff;
    padding: 8px 16px;
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.live-dot {
    width: 8px;
    height: 8px;
    background-color: #10b981;
    border-radius: 50%;
    box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2);
    animation: blink 2s infinite;
}

@keyframes blink {
    0% { opacity: 1; }
    50% { opacity: 0.3; }
    100% { opacity: 1; }
}

/* Button */
.modern-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0 28px;
    height: 48px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    border: none;
    font-family: 'Plus Jakarta Sans', system-ui;
}
.btn-primary {
    background: #FFC300;
    color: #222222;
    box-shadow: 0 6px 16px rgba(255, 195, 0, 0.3);
}
.btn-primary:hover {
    background: #ffce33;
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(255, 195, 0, 0.4);
}
.btn-primary:active {
    transform: translateY(0);
}

/* Grid Layout */
.metrics-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-auto-rows: minmax(160px, auto);
    gap: 24px;
}

/* GMV Highlight Card (Spans 2 columns) */
.gmv-card {
    grid-column: span 2;
    background: linear-gradient(135deg, #FFC300 0%, #FFB000 100%);
    border-radius: 24px;
    padding: 40px;
    color: #222222;
    position: relative;
    overflow: hidden;
    box-shadow: 0 12px 32px rgba(255, 195, 0, 0.25);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.card-bg-decoration {
    position: absolute;
    right: -20px;
    bottom: -30px;
    font-size: 200px;
    color: rgba(255, 255, 255, 0.2);
    transform: rotate(-15deg);
    pointer-events: none;
}

.gmv-title {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 20px;
    opacity: 0.85;
}

.gmv-value {
    display: flex;
    align-items: baseline;
    margin-bottom: 24px;
}

.currency {
    font-size: 32px;
    font-weight: 800;
    margin-right: 8px;
}

.gmv-value .num {
    font-family: 'Plus Jakarta Sans', monospace;
    font-size: 64px;
    font-weight: 800;
    line-height: 1;
    letter-spacing: -2px;
}

.gmv-footer {
    font-size: 15px;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.3);
    padding: 10px 16px;
    border-radius: 12px;
    display: inline-block;
    align-self: flex-start;
    backdrop-filter: blur(4px);
}

/* Standard Metric Cards */
.standard-card {
    background: #ffffff;
    border-radius: 24px;
    padding: 32px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
    border: 1px solid #f1f5f9;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    transition: all 0.3s ease;
}

.standard-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
    border-color: #e2e8f0;
}

.card-top {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
}

.icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
}

/* Colorful Icon Wraps */
.color-user { background: #f0fdf4; color: #16a34a; }
.color-shop { background: #eff6ff; color: #2563eb; }
.color-goods { background: #fdf4ff; color: #d946ef; }
.color-order { background: #fffbeb; color: #d97706; }
.color-success { background: #fee2e2; color: #dc2626; }

.card-name {
    font-size: 16px;
    font-weight: 700;
    color: #475569;
}

.card-bottom .value-num {
    font-family: 'Plus Jakarta Sans', monospace;
    font-size: 38px;
    font-weight: 800;
    color: #0f172a;
    line-height: 1.2;
    margin-bottom: 8px;
}

.card-bottom .unit {
    font-size: 16px;
    color: #94a3b8;
    font-weight: 600;
}

.sub-info {
    font-size: 14px;
    color: #94a3b8;
    font-weight: 500;
}

/* Ensure responsiveness */
@media (max-width: 1400px) {
    .metrics-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    .gmv-card {
        grid-column: span 2;
    }
}

@media (max-width: 900px) {
    .metrics-grid {
        grid-template-columns: 1fr;
    }
    .gmv-card {
        grid-column: span 1;
    }
}

/* Animations */
@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>