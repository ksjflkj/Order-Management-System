<template>
<div class="merchant-list-container">
    
    <!-- Premium Header Banner -->
    <div class="header-section">
        <div class="header-titles">
            <h1 class="page-title">发现全城好店</h1>
            <p class="page-subtitle">精选大牌餐饮，品质外卖安心达。今天想吃点什么？</p>
        </div>
        <div class="search-box">
            <el-input 
                v-model="query.shopName" 
                placeholder="搜索美食、商家名称..." 
                class="premium-search" 
                clearable
                @keyup.enter="loadData"
            >
                <template #prefix>
                    <el-icon style="font-size:18px"><Search /></el-icon>
                </template>
                <template #append>
                    <el-button class="search-btn" @click="loadData">搜 索</el-button>
                </template>
            </el-input>
        </div>
    </div>

    <!-- Merchant Grid -->
    <div class="grid-layout">
        <div 
            class="merchant-card" 
            v-for="item in tableData" 
            :key="item.id" 
            :class="{ 'is-closed': item.isOpen === 0 }"
            @click="goDetail(item)"
        >
            <div class="image-wrapper">
                <el-image 
                    v-if="item.shopLogo" 
                    :src="resolveUrl(item.shopLogo)" 
                    fit="cover" 
                    class="merchant-image" 
                >
                    <template #error>
                        <div class="image-placeholder">
                            <el-icon :size="48" color="#909399"><Picture /></el-icon>
                            <span class="placeholder-text">暂无图片</span>
                        </div>
                    </template>
                </el-image>
                <div v-else class="image-placeholder">
                    <el-icon :size="48" color="#909399"><Picture /></el-icon>
                    <span class="placeholder-text">暂无图片</span>
                </div>

                <!-- 打烊遮罩 -->
                <div class="closed-overlay" v-if="item.isOpen === 0">
                    <span class="closed-text">🌙 打烊中</span>
                </div>
            </div>
            
            <div class="merchant-content">
                <div class="shop-name-row">
                    <h3 class="shop-name" :title="item.shopName">{{ item.shopName }}</h3>
                    <span class="open-badge" v-if="item.isOpen !== 0">• 营业中</span>
                    <span class="closed-badge" v-else>暂停接单</span>
                </div>
                
                <!-- Meta Info (Rating, Sales) -->
                <div class="shop-meta">
                    <div class="rating-badge">
                        <el-icon><StarFilled /></el-icon>
                        <span class="rating-val" v-if="item.rating && Number(item.rating) > 0">{{ Number(item.rating).toFixed(1) }}</span>
                        <span class="rating-val" v-else>暂无评价</span>
                    </div>
                </div>
                
                <!-- Location Details -->
                <div class="info-group">
                    <div class="info-item">
                        <el-icon><Location /></el-icon>
                        <span class="text-ellipsis">{{ item.address || '暂未提供详细地址' }}</span>
                    </div>
                    <div class="info-item">
                        <el-icon><Phone /></el-icon>
                        <span>{{ item.phone || '暂无联系电话' }}</span>
                    </div>
                </div>

                <div class="footer-group">
                    <!-- Shop Promotion Tags -->
                    <div class="promo-tags">
                        <span class="tag tag-yellow">满减优惠</span>
                        <span class="tag tag-red">折扣菜</span>
                    </div>
                    <button 
                        class="go-shop-btn" 
                        :class="{ 'btn-closed': item.isOpen === 0 }"
                        @click.stop="goDetail(item)"
                    >
                        {{ item.isOpen === 0 ? '暂停接单' : '进店' }} <el-icon class="icon-right"><ArrowRight /></el-icon>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Empty State -->
    <el-empty v-if="tableData.length === 0" description="换个关键词试试？没有找到相关商家哦" />

    <!-- Pagination -->
    <div class="pagination-container" v-if="totalPage > 0">
        <el-pagination
            background
            layout="prev, pager, next, jumper"
            :current-page="query.current"
            :page-size="query.size"
            :total="total"
            @current-change="handlePageChange"
        />
    </div>
</div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserMerchantPage } from '@/api/userMerchant'
import { Search, Picture, Location, Phone, ArrowRight, StarFilled } from '@element-plus/icons-vue'

const router = useRouter()
const tableData = ref([])
const total = ref(0)

const resolveUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}

const query = reactive({
    shopName: '',
    current: 1,
    size: 10
})

const totalPage = computed(() => Math.ceil(total.value / query.size) || 1)

const loadData = async () => {
    const res = await getUserMerchantPage(query)
    if (res.code === 200) {
        tableData.value = res.data.records
        total.value = res.data.total
    }
}

const goDetail = (item) => {
    router.push(`/user/merchant/${item.id}`)
}

const handlePageChange = (page) => {
    query.current = page
    loadData()
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.merchant-list-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 24px;
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Premium Header Banner */
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
    padding: 40px 48px;
    border-radius: 24px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    margin-bottom: 40px;
    color: #ffffff;
}

.page-title {
    margin: 0;
    font-size: 32px;
    font-weight: 800;
    font-family: 'Plus Jakarta Sans', system-ui;
    color: #FFC300;
    letter-spacing: 1px;
    margin-bottom: 8px;
}

.page-subtitle {
    margin: 0;
    font-size: 15px;
    font-weight: 500;
    color: #a1a1aa;
}

.search-box {
    width: 480px;
}

.premium-search:deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px transparent inset !important;
    background: #ffffff;
    border-radius: 100px 0 0 100px;
    padding-left: 16px;
    height: 52px;
}

.premium-search:deep(.el-input-group__append) {
    background-color: #FFC300;
    color: #222222;
    border: none;
    border-radius: 0 100px 100px 0;
    box-shadow: none;
}

.search-btn {
    height: 52px;
    border: none;
    background: transparent;
    font-weight: 800;
    font-size: 16px;
    width: 100px;
    color: #222222;
}

.search-btn:hover {
    color: #000;
}

/* Grid Layout */
.grid-layout {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 24px;
    margin-bottom: 40px;
}

/* Premium Merchant Card */
.merchant-card {
    background: #ffffff;
    border-radius: 20px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    cursor: pointer;
    border: 1px solid #f1f5f9;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.merchant-card.is-closed {
    opacity: 0.75;
    filter: grayscale(30%);
    cursor: default;
}

.merchant-card:not(.is-closed):hover {
    transform: translateY(-6px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
    border-color: #e2e8f0;
}

.closed-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(2px);
}
.closed-text {
    color: #fff;
    font-size: 22px;
    font-weight: 700;
    letter-spacing: 2px;
    text-shadow: 0 2px 8px rgba(0,0,0,0.5);
}

.shop-name-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}
.open-badge {
    font-size: 12px;
    color: #10b981;
    font-weight: 700;
    white-space: nowrap;
}
.closed-badge {
    font-size: 12px;
    color: #94a3b8;
    background: #f1f5f9;
    padding: 2px 8px;
    border-radius: 99px;
    white-space: nowrap;
}
.btn-closed {
    background: #94a3b8 !important;
    color: #fff !important;
    cursor: default;
    pointer-events: none;
}

.image-wrapper {
    width: 100%;
    height: 200px;
    background: #f8fafc;
    position: relative;
    overflow: hidden;
}

.merchant-image {
    width: 100%;
    height: 100%;
    transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.merchant-card:hover .merchant-image {
    transform: scale(1.05);
}

.delivery-time-tag {
    position: absolute;
    bottom: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.7);
    color: #ffffff;
    padding: 6px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    backdrop-filter: blur(4px);
}

.image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #94a3b8;
}

.placeholder-text {
    margin-top: 10px;
    font-size: 14px;
    font-weight: 500;
}

.merchant-content {
    padding: 20px 24px;
}

.shop-name {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 800;
    font-family: 'Plus Jakarta Sans', system-ui;
    color: #1e293b;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Meta Data */
.shop-meta {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    font-size: 13px;
    font-weight: 600;
}

.rating-badge {
    display: flex;
    align-items: center;
    color: #f59e0b;
}

.rating-badge .el-icon {
    margin-right: 4px;
    font-size: 16px;
}

.rating-val {
    font-size: 14px;
    font-weight: 800;
}

.meta-divider {
    color: #cbd5e1;
    margin: 0 8px;
}

.delivery-type {
    background: #FFC300;
    color: #222222;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 700;
}

.delivery-fee {
    color: #64748b;
}

/* Info Group */
.info-group {
    background: #f8fafc;
    padding: 12px 14px;
    border-radius: 12px;
    margin-bottom: 16px;
}

.info-item {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #64748b;
    font-weight: 500;
    margin-bottom: 6px;
}

.info-item:last-child {
    margin-bottom: 0;
}

.info-item .el-icon {
    margin-right: 8px;
    color: #94a3b8;
    font-size: 15px;
}

.text-ellipsis {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
}

/* Footer Section */
.footer-group {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.promo-tags {
    display: flex;
    gap: 6px;
}

.tag {
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 4px;
    border: 1px solid;
    font-weight: 600;
}

.tag-yellow {
    color: #d97706;
    background: #fef3c7;
    border-color: #fde68a;
}

.tag-red {
    color: #e11d48;
    background: #ffe4e6;
    border-color: #fecdd3;
}

/* Custom Go Button */
.go-shop-btn {
    background: #FFC300;
    color: #222222;
    border: none;
    padding: 8px 16px;
    border-radius: 100px;
    font-weight: 700;
    font-size: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: all 0.2s;
}

.go-shop-btn .icon-right {
    margin-left: 4px;
    transition: transform 0.2s;
}

.go-shop-btn:hover {
    background: #ffce33;
}

.go-shop-btn:hover .icon-right {
    transform: translateX(3px);
}

.pagination-container {
    display: flex;
    justify-content: center;
    padding: 24px 0;
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
    .header-section {
        flex-direction: column;
        align-items: flex-start;
        padding: 24px;
        gap: 20px;
    }
    .search-box {
        width: 100%;
    }
}
</style>
