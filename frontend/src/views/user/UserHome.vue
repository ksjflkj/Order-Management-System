<template>
    <div class="user-home-container">
        <!-- Hero Carousel Section -->
        <div class="hero-section">
            <el-carousel :interval="4000" type="card" height="340px" class="premium-carousel">
                <el-carousel-item v-for="(path, index) in pathArr" :key="index">
                    <div class="carousel-img-wrapper">
                        <el-image :src="path" fit="cover" class="carousel-image" />
                        <div class="carousel-overlay"></div>
                    </div>
                </el-carousel-item>
            </el-carousel>
        </div>

        <!-- Semantic Greeting Zone -->
        <div class="greeting-zone">
            <h2 class="welcome-title">今天想吃点什么？</h2>
            <p class="welcome-desc">千品万店，我们只为你准时送达最热腾腾的舌尖美味。</p>
        </div>

        <!-- Diamond Quick Actions (金刚区) -->
        <div class="quick-actions-grid">
            
            <div class="action-card primary-action" @click="router.push('/user/merchant-list')">
                <div class="action-bg-blob"></div>
                <div class="action-content">
                    <div class="action-icon"><el-icon><Shop /></el-icon></div>
                    <div class="action-text">
                        <h3>开始点餐</h3>
                        <p>汇聚全城严选大牌外卖</p>
                    </div>
                </div>
                <div class="go-arrow"><el-icon><ArrowRight /></el-icon></div>
            </div>
            
            <div class="action-card secondary-action" @click="router.push('/user/orders')">
                <div class="action-bg-blob"></div>
                <div class="action-content">
                    <div class="action-icon"><el-icon><List /></el-icon></div>
                    <div class="action-text">
                        <h3>我的订单</h3>
                        <p>实时追踪骑手配送进度</p>
                    </div>
                </div>
                <div class="go-arrow"><el-icon><ArrowRight /></el-icon></div>
            </div>

            <div class="action-card tertiary-action" @click="router.push('/user/cart')">
                <div class="action-bg-blob"></div>
                <div class="action-content">
                    <div class="action-icon"><el-icon><ShoppingCart /></el-icon></div>
                    <div class="action-text">
                        <h3>购物车</h3>
                        <p>一键结算所有心动美食</p>
                    </div>
                </div>
                <div class="go-arrow"><el-icon><ArrowRight /></el-icon></div>
            </div>

            <div class="action-card quaternary-action" @click="router.push('/user/coupon/available')">
                <div class="action-bg-blob"></div>
                <div class="action-content">
                    <div class="action-icon"><el-icon><Ticket /></el-icon></div>
                    <div class="action-text">
                        <h3>领券中心</h3>
                        <p>天天神券准点免费疯抢</p>
                    </div>
                </div>
                <div class="go-arrow"><el-icon><ArrowRight /></el-icon></div>
            </div>

        </div>

        <!-- Optional Bottom Decor / Slogan -->
        <div class="slogan-area">
            <div class="slogan-badge">安全卫生 · 极速送达 · 品质甄选</div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, List, ShoppingCart, Ticket, ArrowRight } from '@element-plus/icons-vue'
import p1 from '@/assets/chunjuan.jpg'
import p2 from '@/assets/haishen.jpg'
import p3 from '@/assets/hanbao.jpg'
import p4 from '@/assets/longxia.jpg'
import p5 from '@/assets/mian.jpg'
import p6 from '@/assets/rou.jpg'
// import p7 from '@/assets/p7.png'

const router = useRouter()
const pathArr = ref([p1, p2, p3, p4, p5, p6])
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.user-home-container {
    max-width: 1300px;
    margin: 0 auto;
    padding: 10px 20px 60px 20px;
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Carousel Adjustments */
.hero-section {
    margin-bottom: 40px;
}

.premium-carousel {
    border-radius: 24px;
    overflow: hidden;
}

.carousel-img-wrapper {
    width: 100%;
    height: 100%;
    border-radius: 24px;
    overflow: hidden;
    position: relative;
    box-shadow: 0 10px 30px rgba(0,0,0,0.08);
}

.carousel-image {
    width: 100%;
    height: 100%;
    transition: transform 0.5s ease;
}

.carousel-img-wrapper:hover .carousel-image {
    transform: scale(1.03);
}

.carousel-overlay {
    position: absolute;
    bottom: 0; left: 0; right: 0;
    height: 40%;
    background: linear-gradient(to top, rgba(0,0,0,0.15), transparent);
    pointer-events: none;
}

/* Greeting Zone */
.greeting-zone {
    text-align: center;
    margin-bottom: 40px;
}

.welcome-title {
    font-family: 'Plus Jakarta Sans', system-ui;
    font-size: 32px;
    font-weight: 800;
    color: #1a1a1a;
    margin: 0 0 12px 0;
    letter-spacing: 0.5px;
}

.welcome-desc {
    font-size: 16px;
    color: #64748b;
    margin: 0;
    font-weight: 500;
}

/* Quick Actions Grid */
.quick-actions-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    margin-bottom: 60px;
}

.action-card {
    position: relative;
    border-radius: 20px;
    padding: 32px 24px;
    cursor: pointer;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
    transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 1px solid rgba(255,255,255,0.4);
}

.action-card:hover {
    transform: translateY(-6px);
}

.action-bg-blob {
    position: absolute;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    filter: blur(40px);
    z-index: 0;
    top: -20px;
    right: -20px;
    opacity: 0.5;
    transition: all 0.4s;
}

.action-card:hover .action-bg-blob {
    transform: scale(1.3);
    opacity: 0.8;
}

.action-content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.action-icon {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    background: #ffffff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.action-text h3 {
    margin: 0 0 6px 0;
    font-size: 20px;
    font-weight: 800;
    font-family: 'Plus Jakarta Sans', system-ui;
    color: #1e293b;
}

.action-text p {
    margin: 0;
    font-size: 13px;
    color: #64748b;
    font-weight: 600;
}

.go-arrow {
    position: relative;
    z-index: 1;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #1e293b;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    transition: transform 0.3s;
}

.action-card:hover .go-arrow {
    transform: translateX(4px);
}

/* Card Themes */
.primary-action {
    background: linear-gradient(135deg, #FFEFBA 0%, #FFFFFF 100%);
}
.primary-action .action-bg-blob { background: #FFC300; }
.primary-action .action-icon { color: #d97706; }
.primary-action:hover { box-shadow: 0 12px 24px rgba(255, 195, 0, 0.15); border-color: #FFC300; }

.secondary-action {
    background: linear-gradient(135deg, #E0E7FF 0%, #FFFFFF 100%);
}
.secondary-action .action-bg-blob { background: #6366f1; }
.secondary-action .action-icon { color: #4338ca; }
.secondary-action:hover { box-shadow: 0 12px 24px rgba(99, 102, 241, 0.15); border-color: #a5b4fc; }

.tertiary-action {
    background: linear-gradient(135deg, #FDE68A 0%, #FFFFFF 100%);
}
.tertiary-action .action-bg-blob { background: #f59e0b; }
.tertiary-action .action-icon { color: #b45309; }
.tertiary-action:hover { box-shadow: 0 12px 24px rgba(245, 158, 11, 0.15); border-color: #fcd34d; }

.quaternary-action {
    background: linear-gradient(135deg, #D1FAE5 0%, #FFFFFF 100%);
}
.quaternary-action .action-bg-blob { background: #10b981; }
.quaternary-action .action-icon { color: #047857; }
.quaternary-action:hover { box-shadow: 0 12px 24px rgba(16, 185, 129, 0.15); border-color: #a7f3d0; }

/* Slogan Area */
.slogan-area {
    display: flex;
    justify-content: center;
}

.slogan-badge {
    background: #FFC300;
    color: #222222;
    padding: 10px 30px;
    border-radius: 30px;
    font-weight: 700;
    font-size: 14px;
    letter-spacing: 1px;
    box-shadow: 0 4px 12px rgba(255, 195, 0, 0.3);
}

/* Responsiveness */
@media (max-width: 1200px) {
    .quick-actions-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}
@media (max-width: 600px) {
    .quick-actions-grid {
        grid-template-columns: 1fr;
    }
    .action-card {
        padding: 24px 20px;
    }
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
