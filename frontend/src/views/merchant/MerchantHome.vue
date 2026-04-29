<template>
    <div class="dashboard-container">
        <!-- Dashboard Header -->
        <div class="dashboard-header">
            <div class="header-left">
                <div class="title-icon-wrapper">
                    <el-icon><Shop /></el-icon>
                </div>
                <div class="title-text">
                    <h2 class="sys-title">老板好，{{ userStore.userInfo?.username }}</h2>
                    <p class="sys-desc">今天又是生意兴隆的一天，快来看看店铺的实时接单情况吧！</p>
                </div>
            </div>
            <div class="header-right">
                <span class="refresh-time">状态: <span class="live-dot"></span> 自动接单中</span>
                <button class="modern-btn btn-primary" @click="fetchDashboardData">
                    <el-icon style="margin-right:6px; font-size:16px"><RefreshRight /></el-icon> 刷新面板
                </button>
            </div>
        </div>

        <h3 class="section-badge">今日核心战报</h3>
        <div class="metrics-grid priority-grid">
            
            <!-- Today Revenue Highlight -->
            <div class="metric-card gmv-card">
                <div class="card-bg-decoration">
                    <el-icon><Money /></el-icon>
                </div>
                <div class="gmv-title">今日实时预计收入 (元)</div>
                <div class="gmv-value">
                    <span class="currency">¥</span>
                    <span class="num">{{ todayRevenue.toFixed(2) }}</span>
                </div>
                <div class="gmv-footer">
                    资金直接入账，多劳多得，继续冲刺今日目标！
                </div>
            </div>

            <!-- Pending Orders (WARNING) -->
            <div class="metric-card urgent-card">
                <div class="card-top">
                    <div class="icon-wrap warning-icon">
                        <el-icon><BellFilled /></el-icon>
                    </div>
                    <span class="card-name">新订单待处理</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ pendingOrders }} <span class="unit">单</span></div>
                    <div class="sub-info">抓紧处理！超时可能会被买家投诉哦</div>
                </div>
            </div>

            <!-- Today Orders -->
            <div class="metric-card today-orders-card">
                <div class="card-top">
                    <div class="icon-wrap today-icon"><el-icon><Document /></el-icon></div>
                    <span class="card-name">今日新增订单</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ todayOrders }} <span class="unit">单</span></div>
                    <div class="sub-info">今天店里的客流量和转化率表现</div>
                </div>
            </div>
        </div>

        <h3 class="section-badge mt-40">店铺历史资产库</h3>
        <div class="metrics-grid">
            
            <!-- Total Revenue -->
            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-wallet"><el-icon><Wallet /></el-icon></div>
                    <span class="card-name">累计总营业额</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">
                        <span style="font-size:24px; color:#94a3b8; font-weight:600; margin-right:4px;">¥</span>
                        {{ totalRevenue.toFixed(2) }}
                    </div>
                    <div class="sub-info">入驻以来产生的全部流水数据</div>
                </div>
            </div>

            <!-- Total Orders -->
            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-order"><el-icon><List /></el-icon></div>
                    <span class="card-name">累计服务笔数</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ totalOrders }} <span class="unit">笔</span></div>
                    <div class="sub-info">每一笔交易都是一份食客的信任</div>
                </div>
            </div>

            <!-- Total Dishes -->
            <div class="metric-card standard-card">
                <div class="card-top">
                    <div class="icon-wrap color-goods"><el-icon><Goods /></el-icon></div>
                    <span class="card-name">当前在售菜品</span>
                </div>
                <div class="card-bottom">
                    <div class="value-num">{{ totalDishes }} <span class="unit">款</span></div>
                    <div class="sub-info">持续推陈出新能为您带来更多曝光</div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMerchantOrderList } from '@/api/merchantOrder'
import { getDishPage } from '@/api/dish'
import { ElMessage } from 'element-plus'
import { Money, Document, Wallet, List, Goods, Shop, RefreshRight, BellFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()

const todayRevenue = ref(0)
const totalRevenue = ref(0)
const todayOrders = ref(0)
const totalOrders = ref(0)
const pendingOrders = ref(0)
const totalDishes = ref(0)

const fetchDashboardData = async () => {
    try {
        // 1. 获取订单数据
        const orderRes = await getMerchantOrderList()
        if (orderRes.code === 200) {
            const orders = orderRes.data || []

            let tRevenue = 0
            let tdRevenue = 0
            let tOrders = orders.length
            let tdOrders = 0
            let pOrders = 0

            const today1 = new Date().toISOString().split('T')[0]
            const today2 = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '-')

            // 订单状态：PENDING_PAYMENT-待支付, PAID-已支付, ACCEPTED-已接单, COMPLETED-已完成, CANCELLED-已取消
            const completedStatuses = ['COMPLETED', 'PAID', 'ACCEPTED']
        
            orders.forEach(order => {
                let orderDate = ''
                if (order.createTime) {
                    orderDate = order.createTime.split('T')[0].split(' ')[0]
                }

                const isToday = (orderDate === today1 || orderDate === today2)

                if (isToday) {
                    tdOrders++
                }

                if (order.status === 'PENDING_PAYMENT' || order.status === 'PAID' || order.status === '0') {
                    pOrders++
                }

                if (completedStatuses.includes(order.status) || order.status === '4') {
                    tRevenue += (order.totalAmount || 0)
                    if (isToday) {
                        tdRevenue += (order.totalAmount || 0)
                    }
                }
            })

            totalOrders.value = tOrders
            todayOrders.value = tdOrders
            pendingOrders.value = pOrders
            totalRevenue.value = tRevenue
            todayRevenue.value = tdRevenue
        }

        // 2. 获取菜品数据
        const dishRes = await getDishPage({ page: 1, pageSize: 1, status: 1 })
        if (dishRes.code === 200) {
            totalDishes.value = dishRes.data.total || 0
        }

    } catch (error) {
        console.error('Failed to fetch dashboard data:', error)
        ElMessage.error('获取统计数据失败，请重试')
    }
}

onMounted(() => {
    fetchDashboardData()
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
    margin-bottom: 30px;
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

/* Sections */
.section-badge {
    font-size: 18px;
    font-weight: 800;
    color: #0f172a;
    margin-bottom: 20px;
    padding-left: 12px;
    border-left: 4px solid #FFC300;
    line-height: 1;
}

.mt-40 {
    margin-top: 40px;
}

/* Grid Layout */
.metrics-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-auto-rows: minmax(160px, auto);
    gap: 24px;
}

/* Priority Row Adjustments */
@media (min-width: 1400px) {
    .priority-grid {
        grid-template-columns: 2fr 1fr 1fr;
    }
}

/* GMV Highlight Card */
.gmv-card {
    background: linear-gradient(135deg, #FFC300 0%, #FFB000 100%);
    border-radius: 24px;
    padding: 36px 40px;
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
    right: -10px;
    bottom: -20px;
    font-size: 160px;
    color: rgba(255, 255, 255, 0.25);
    transform: rotate(-15deg);
    pointer-events: none;
}

.gmv-title {
    font-size: 16px;
    font-weight: 700;
    margin-bottom: 12px;
    opacity: 0.85;
}

.gmv-value {
    display: flex;
    align-items: baseline;
    margin-bottom: 20px;
}

.currency {
    font-size: 28px;
    font-weight: 800;
    margin-right: 6px;
}

.gmv-value .num {
    font-family: 'Plus Jakarta Sans', monospace;
    font-size: 56px;
    font-weight: 800;
    line-height: 1;
    letter-spacing: -2px;
}

.gmv-footer {
    font-size: 14px;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.3);
    padding: 8px 14px;
    border-radius: 10px;
    display: inline-block;
    align-self: flex-start;
    backdrop-filter: blur(4px);
}

/* Urgent Card */
.urgent-card {
    background: linear-gradient(180deg, #ffffff 0%, #fff1f2 100%);
    border: 1px solid #ffe4e6;
    box-shadow: 0 8px 24px rgba(225, 29, 72, 0.06);
}

.urgent-card:hover { border-color: #fda4af; }

.warning-icon {
    background: #ffe4e6;
    color: #e11d48;
    animation: ringBell 2s infinite ease-in-out;
}

@keyframes ringBell {
    0%, 100% { transform: rotate(0deg); }
    10% { transform: rotate(15deg); }
    20% { transform: rotate(-10deg); }
    30% { transform: rotate(5deg); }
    40% { transform: rotate(-5deg); }
    50% { transform: rotate(0deg); }
}

.urgent-card .value-num {
    color: #be123c !important;
}

/* Today Orders Card */
.today-orders-card {
    background: #ffffff;
    border: 1px solid #f1f5f9;
}
.today-icon { background: #eff6ff; color: #2563eb; }

/* Standard Metric Cards */
.standard-card, .urgent-card, .today-orders-card {
    border-radius: 24px;
    padding: 32px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    transition: all 0.3s ease;
}

.standard-card {
    background: #ffffff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
    border: 1px solid #f1f5f9;
}

.standard-card:hover, .urgent-card:hover, .today-orders-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
}
.standard-card:hover { border-color: #e2e8f0; }

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
.color-wallet { background: #fdf4ff; color: #c026d3; }
.color-order { background: #f0fdf4; color: #16a34a; }
.color-goods { background: #fefce8; color: #ca8a04; }

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
    font-size: 13px;
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