<template>
    <div class="merchant-detail-container">
        <!-- 商家头部：全宽 banner + 下方信息行 -->
        <div class="merchant-header-card">
            <!-- 全宽 Banner -->
            <div class="merchant-banner">
                <!-- 返回上一页按钮 -->
                <button class="back-nav-btn" @click="router.push('/user/merchant-list')">
                    <el-icon><ArrowLeft /></el-icon> 返回商家列表
                </button>

                <el-image 
                    v-if="merchant.shopLogo" 
                    :src="resolveUrl(merchant.shopLogo)" 
                    fit="cover"
                    class="banner-image" 
                />
                <div class="banner-overlay"></div>
                <div v-if="!merchant.shopLogo" class="banner-placeholder">
                    <el-icon :size="64" color="rgba(255,255,255,0.2)"><PictureFilled /></el-icon>
                    <span class="placeholder-text">暂未提供招牌图片</span>
                </div>
            </div>

            <!-- 信息行 -->
            <div class="merchant-info-row">
                <div class="info-left">
                    <div class="info-top">
                        <h1 class="shop-title">{{ merchant.shopName }}</h1>
                        <el-tag v-if="merchant.isOpen !== 0" color="#FFC300" style="color:#222; border:none; font-weight:800; padding: 0 10px;" size="small">营业中</el-tag>
                        <el-tag v-else type="info" effect="dark" size="small" style="font-weight:700;">🌙 打烊中</el-tag>
                    </div>
                    <div class="info-rating">
                        <el-rate :model-value="Number(merchant.rating) || 0" disabled text-color="#FFC300" />
                        <span class="score-text">{{ formatRating(merchant.rating) }}</span>
                    </div>
                    <p class="shop-desc">{{ merchant.description || '这家店很懒，还没有写简介~' }}</p>
                </div>

                <div class="info-right">
                    <div class="contact-details">
                        <div class="contact-item">
                            <el-icon><Location /></el-icon>
                            <span>{{ merchant.address || '暂无地址' }}</span>
                        </div>
                        <div class="contact-item">
                            <el-icon><Phone /></el-icon>
                            <span>{{ merchant.phone || '暂无电话' }}</span>
                        </div>
                    </div>
                    <button class="cart-btn" @click="goCart">
                        <el-icon class="cart-icon"><ShoppingCart /></el-icon> 结算购物车
                    </button>
                </div>
            </div>
        </div>

        <el-tabs v-model="activeTab" class="merchant-tabs">
            <el-tab-pane label="点餐" name="menu">
                <div class="menu-layout">
                    <div class="menu-content">
                        <div v-for="group in groupedDishes" :key="group.name" class="dish-group">
                            <div class="category-header">
                                <span class="category-indicator"></span>
                                <h2 class="category-title">{{ group.name }}</h2>
                            </div>
                            
                            <div class="dish-grid">
                                <div class="dish-card" v-for="item in group.dishes" :key="item.id">
                                    <div class="dish-image-wrapper">
                                        <el-image 
                                            v-if="item.image" 
                                            :src="resolveUrl(item.image)" 
                                            fit="cover" 
                                            class="dish-image" 
                                        />
                                        <div v-else class="dish-placeholder">
                                            <el-icon :size="40" color="#c0c4cc"><Food /></el-icon>
                                            <span class="dish-pt-text">暂无图片</span>
                                        </div>
                                    </div>
                                    <div class="dish-content">
                                        <h3 class="dish-name" :title="item.name">{{ item.name }}</h3>
                                        <p class="dish-desc">{{ item.description || '营养美味，快来品尝！' }}</p>
                                        
                                        <div class="dish-stats">
                                            <span>月售 {{ item.sales || 0 }}</span>
                                            <span class="divider">|</span>
                                            <span>库存 {{ item.stock || 0 }}</span>
                                        </div>
                                        
                                        <div class="dish-bottom">
                                            <div class="price-wrap">
                                                <span class="price-symbol">¥</span>
                                                <span class="price">{{ item.price }}</span>
                                                <span class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</span>
                                            </div>
                                            <button 
                                                class="add-btn" 
                                                :class="{ 'add-btn-disabled': merchant.isOpen === 0 }"
                                                @click="openAttrDialog(item)"
                                            >
                                                <el-icon><Plus /></el-icon>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </el-tab-pane>

            <el-tab-pane label="评价" name="review">
                <div v-if="reviewList.length === 0" class="empty-review">
                    <el-empty description="暂无评价，快来抢沙发~" />
                </div>
                <div v-else class="review-list">
                    <div class="review-card" v-for="review in reviewList" :key="review.id">
                        <div class="review-header">
                            <div class="review-user-info">
                                <el-avatar :size="50" :src="checkAvatarUrl(review.avatar)" class="user-avatar">
                                    {{ (review.nickname || review.username || '匿').charAt(0) }}
                                </el-avatar>
                                <div class="user-meta">
                                    <span class="review-user">{{ review.nickname || review.username || '匿名用户' }}</span>
                                    <el-rate :model-value="review.rating" disabled show-score text-color="#FFC300" size="small" />
                                </div>
                            </div>
                            <span class="review-time">{{ review.createTime }}</span>
                        </div>
                        <div class="review-content">{{ review.content }}</div>
                        <div v-if="review.reply" class="merchant-reply">
                            <strong>商家回复：</strong>{{ review.reply }}
                        </div>
                    </div>
                </div>
            </el-tab-pane>
        </el-tabs>

        <!-- 高级规格选择弹窗 -->
        <el-dialog v-model="attrDialogVisible" width="480px" destroy-on-close class="spec-dialog" :show-close="false">
            <template #header="{ close }">
                <div class="spec-header">
                    <div class="spec-avatar">
                        <el-image v-if="currentDish?.image" :src="resolveUrl(currentDish.image)" fit="cover" />
                        <el-icon v-else size="40" color="#c0c4cc"><Food /></el-icon>
                    </div>
                    <div class="spec-title-box">
                        <div class="spec-title">{{ currentDish?.name || '' }}</div>
                        <div class="spec-desc">已选：{{ selectedTags.spec }}，{{ selectedTags.spiciness }}，{{ selectedTags.recommend }}</div>
                    </div>
                    <el-icon class="close-icon" @click="close"><Close /></el-icon>
                </div>
            </template>

            <div class="spec-scroll-wrap">
                <div class="spec-group">
                    <div class="spec-label">规格</div>
                    <div class="spec-opts">
                        <div class="spec-opt" :class="{active: selectedTags.spec === '大份'}" @click="selectedTags.spec = '大份'">大份 (+5元)</div>
                        <div class="spec-opt" :class="{active: selectedTags.spec === '中份'}" @click="selectedTags.spec = '中份'">中份 (原价)</div>
                        <div class="spec-opt" :class="{active: selectedTags.spec === '小份'}" @click="selectedTags.spec = '小份'">小份 (-5元)</div>
                    </div>
                </div>
                
                <div class="spec-group">
                    <div class="spec-label">辣度</div>
                    <div class="spec-opts">
                        <div v-for="tag in ['不辣', '微辣', '中辣', '特辣']" :key="tag" 
                             class="spec-opt" :class="{active: selectedTags.spiciness === tag}" 
                             @click="selectedTags.spiciness = tag">
                             {{ tag }}
                        </div>
                    </div>
                </div>
                
                <div class="spec-group">
                    <div class="spec-label">配料推荐</div>
                    <div class="spec-opts">
                         <div v-for="tag in ['正常', '加葱', '加香菜', '不要葱蒜']" :key="tag" 
                             class="spec-opt" :class="{active: selectedTags.recommend === tag}" 
                             @click="selectedTags.recommend = tag">
                             {{ tag }}
                        </div>
                    </div>
                </div>
            </div>
            
            <template #footer>
                <div class="spec-footer">
                    <div class="spec-price-wrap">
                        <span class="currency">¥</span>
                        <span class="price-val">{{ computedSpecPrice }}</span>
                    </div>
                    <div class="spec-actions">
                        <el-button @click="attrDialogVisible = false" round>再想想</el-button>
                        <button class="add-cart-confirm-btn" @click="confirmAddToCart">
                            <el-icon><ShoppingCart /></el-icon>&nbsp;加入购物车
                        </button>
                    </div>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserMerchantDetail, getUserMerchantDishList } from '@/api/userMerchant'
import { addToCart } from '@/api/cart'
import { getMerchantReviews } from '@/api/review'
import { useUserStore } from '@/stores/user'
import { PictureFilled, Location, Phone, ShoppingCart, Food, Plus, Close, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const resolveUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}
const merchant = ref({})
const dishList = ref([])
const reviewList = ref([])
const activeTab = ref('menu')

const attrDialogVisible = ref(false)
const currentDish = ref(null)
const selectedTags = ref({
    spec: '大份',
    spiciness: '不辣',
    recommend: '正常'
})

const groupedDishes = computed(() => {
    const map = new Map()
    for (const dish of dishList.value) {
        const catName = dish.categoryName || '默认分类'
        if (!map.has(catName)) {
            map.set(catName, { name: catName, dishes: [] })
        }
        map.get(catName).dishes.push(dish)
    }
    return Array.from(map.values())
})

// 计算加上附加费后的总价
const computedSpecPrice = computed(() => {
    if (!currentDish.value) return 0
    let base = Number(currentDish.value.price) || 0
    if (selectedTags.value.spec === '大份') base += 5
    if (selectedTags.value.spec === '小份') base -= 5
    return base.toFixed(2)
})

const loadData = async () => {
    const id = route.params.id

    const [merchantResult, dishResult, reviewResult] = await Promise.allSettled([
        getUserMerchantDetail(id),
        getUserMerchantDishList(id),
        getMerchantReviews(id)
    ])

    if (merchantResult.status === 'fulfilled' && merchantResult.value.code === 200) {
        merchant.value = merchantResult.value.data
    }

    if (dishResult.status === 'fulfilled' && dishResult.value.code === 200) {
        dishList.value = dishResult.value.data
    }

    if (reviewResult.status === 'fulfilled' && reviewResult.value.code === 200) {
        reviewList.value = reviewResult.value.data || []
    }
}

const formatRating = (rating) => {
    if (!rating || Number(rating) === 0) return '暂无评价'
    return Number(rating).toFixed(1)
}

const checkAvatarUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) {
        return 'http://localhost:8080' + url
    }
    if (url.startsWith('file://') || /^[A-Za-z]:/.test(url)) {
        return ''
    }
    return url
}

const openAttrDialog = (dish) => {
    // 游客用户无法加购物车，提示登录
    if (userStore.isGuest) {
        ElMessage.warning('请先登录再添加购物车')
        router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
        return
    }
    // 打烊中无法下单
    if (merchant.value.isOpen === 0) {
        ElMessage.warning('该店铺正在打烊，暂停接单，请稍后再来~')
        return
    }
    currentDish.value = dish
    selectedTags.value = {
        spec: '大份',
        spiciness: '不辣',
        recommend: '正常'
    }
    attrDialogVisible.value = true
}

const confirmAddToCart = async () => {
    if (!currentDish.value) return
    
    // 构建标签字符串
    const tagsArr = []
    if (selectedTags.value.spec && selectedTags.value.spec !== '正常') tagsArr.push(`规格:${selectedTags.value.spec}`)
    if (selectedTags.value.spiciness && selectedTags.value.spiciness !== '正常') tagsArr.push(`辣度:${selectedTags.value.spiciness}`)
    if (selectedTags.value.recommend && selectedTags.value.recommend !== '正常') tagsArr.push(`推荐:${selectedTags.value.recommend}`)
    
    const dishTagsStr = tagsArr.length > 0 ? tagsArr.join(', ') : null

    try {
        const res = await addToCart({
            dishId: currentDish.value.id,
            quantity: 1,
            dishTags: dishTagsStr
        })
        if (res.code === 200) {
            ElMessage.success(res.message || '加入购物车成功')
            attrDialogVisible.value = false
        } else {
            ElMessage.error(res.message || '加入购物车失败')
        }
    } catch (error) {
        ElMessage.error(error.message || '加入购物车失败')
    }
}

const goCart = () => {
    if (userStore.isGuest) {
        ElMessage.warning('请先登录再查看购物车')
        router.push(`/login?redirect=/user/cart`)
        return
    }
    router.push('/user/cart')
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.merchant-detail-container {
    padding: 24px;
    max-width: 1400px;
    margin: 0 auto;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.merchant-header-card {
    background: #fff;
    border-radius: 20px;
    margin-bottom: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
    overflow: hidden;
}

/* 全宽 Banner 区 */
.merchant-banner {
    width: 100%;
    height: 320px;
    position: relative;
    background: #1a1a1a;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}
.banner-image {
    width: 100%;
    height: 100%;
}
.banner-overlay {
    position: absolute;
    inset: 0;
    pointer-events: none;
    background: linear-gradient(to top, rgba(0,0,0,0.5), transparent);
}
.banner-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: rgba(255,255,255,0.4);
    gap: 12px;
}
.placeholder-text { font-size: 15px; letter-spacing: 1px; }

/* 悬浮返回按钮 */
.back-nav-btn {
    position: absolute;
    top: 24px;
    left: 24px;
    z-index: 10;
    background: rgba(0, 0, 0, 0.45);
    color: #ffffff;
    border: 1px solid rgba(255, 255, 255, 0.15);
    padding: 8px 16px;
    border-radius: 100px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    backdrop-filter: blur(8px);
    transition: all 0.3s ease;
}

.back-nav-btn:hover {
    background: #FFC300;
    color: #222222;
    border-color: #FFC300;
    transform: translateX(-2px);
    box-shadow: 0 4px 12px rgba(255, 195, 0, 0.3);
}

/* 信息行 */
.merchant-info-row {
    padding: 24px 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 24px;
}
.info-left { flex: 1; }
.info-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 16px;
    flex-shrink: 0;
}
.info-top { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
.shop-title { margin: 0; font-size: 28px; font-weight: 800; font-family: 'Plus Jakarta Sans', system-ui; color: #1e293b; }
.info-rating { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.score-text { font-size: 16px; color: #FFC300; font-weight: 800; }
.shop-desc {
    color: #64748b; line-height: 1.6; margin: 0; font-size: 14px;
    background: #f8fafc; padding: 12px 16px; border-radius: 12px;
    max-width: 600px; font-weight: 500;
}

.contact-details { display: flex; flex-direction: column; gap: 8px; }
.contact-item { display: flex; align-items: center; font-size: 14px; color: #64748b; font-weight: 500; gap: 8px; }
.contact-item .el-icon { color: #d97706; font-size: 16px; }

/* 结算购物车 按钮 */
.cart-btn {
    background: #FFC300;
    color: #222222;
    border: none;
    padding: 14px 32px;
    border-radius: 100px;
    font-weight: 800;
    font-size: 16px;
    cursor: pointer;
    box-shadow: 0 6px 16px rgba(255, 195, 0, 0.3);
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    display: flex;
    align-items: center;
}
.cart-icon { font-size: 20px; margin-right: 8px; }
.cart-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(255, 195, 0, 0.4);
    background: #ffce33;
}

/* 标签页主体 */
.merchant-tabs { 
    background: #fff; 
    padding: 24px 32px; 
    border-radius: 20px; 
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03); 
}
.merchant-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 600; }
.merchant-tabs :deep(.el-tabs__active-bar) { background-color: #FFC300; height: 3px; border-radius: 3px; }
.merchant-tabs :deep(.el-tabs__item.is-active) { color: #222222 !important; font-weight: 800; }
.merchant-tabs :deep(.el-tabs__item:hover) { color: #d97706 !important; }

.dish-group { margin-bottom: 40px; }
.dish-group:last-child { margin-bottom: 0; }
.category-header { display: flex; align-items: center; margin-bottom: 20px; }
.category-indicator { width: 5px; height: 20px; background-color: #FFC300; border-radius: 4px; margin-right: 12px; }
.category-title { font-size: 22px; font-weight: 800; color: #1e293b; margin: 0; }
.dish-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }

/* 菜品卡片 (独立封装，移除el-card) */
.dish-card { 
    background: #ffffff; 
    border-radius: 16px; 
    overflow: hidden; 
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1); 
    border: 1px solid #f1f5f9; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}
.dish-card:hover { 
    transform: translateY(-4px); 
    box-shadow: 0 16px 32px rgba(0, 0, 0, 0.08); 
    border-color: #e2e8f0;
}
.dish-image-wrapper { width: 100%; height: 220px; background: #f8fafc; position: relative; overflow: hidden; }
.dish-image { width: 100%; height: 100%; transition: transform 0.6s ease; }
.dish-card:hover .dish-image { transform: scale(1.05); }
.dish-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #94a3b8; }
.dish-pt-text { margin-top: 8px; font-size: 13px; font-weight: 500; }
.dish-content { padding: 20px; }
.dish-name { margin: 0 0 8px 0; font-size: 18px; font-weight: 700; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-family: 'Plus Jakarta Sans', system-ui; }
.dish-desc { font-size: 13px; color: #64748b; font-weight: 500; margin: 0 0 12px 0; line-height: 1.5; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

.dish-stats { display: flex; align-items: center; font-size: 12px; color: #94a3b8; font-weight: 600; margin-bottom: 16px; }
.divider { margin: 0 8px; color: #cbd5e1; }

.dish-bottom { display: flex; justify-content: space-between; align-items: center; }
.price-wrap { display: flex; align-items: baseline; }
.price-symbol { font-size: 14px; color: #e11d48; font-weight: 800; }
.price { font-size: 24px; color: #e11d48; font-weight: 800; margin-right: 8px; font-family: 'Plus Jakarta Sans', monospace; letter-spacing: -0.5px; }
.original-price { font-size: 13px; color: #94a3b8; text-decoration: line-through; font-weight: 600; }

.add-btn { 
    width: 36px; height: 36px; 
    border-radius: 50%; 
    background: #FFC300; 
    color: #222222; 
    border: none; 
    display: flex; align-items: center; justify-content: center; 
    font-size: 18px; 
    cursor: pointer; 
    transition: all 0.2s; 
    box-shadow: 0 4px 12px rgba(255, 195, 0, 0.25);
}
.add-btn:active { transform: scale(0.9); }
.add-btn:hover { background: #ffce33; }
.add-btn-disabled {
    background: #e2e8f0 !important;
    color: #94a3b8 !important;
    cursor: not-allowed;
    box-shadow: none;
}
.add-btn-disabled:hover { background: #e2e8f0 !important; transform: none !important; }

/* 评价列表 */
.review-list { display: flex; flex-direction: column; gap: 16px; }
.review-card { border-radius: 16px; transition: background-color 0.3s; padding: 20px; border: 1px solid #f1f5f9; background: #ffffff; }
.review-card:hover { background-color: #f8fafc; border-color: #e2e8f0; }
.review-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.review-user-info { display: flex; align-items: center; gap: 16px; }
.user-avatar { border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
.user-meta { display: flex; flex-direction: column; gap: 4px; }
.review-user { font-weight: 700; color: #1e293b; font-size: 15px; }
.review-time { font-size: 13px; color: #94a3b8; font-weight: 500; }
.review-content { color: #475569; line-height: 1.6; font-size: 14px; margin-left: 66px; font-weight: 500; }
.merchant-reply { 
    margin-left: 66px; margin-top: 14px; 
    background: #fffbef; padding: 14px 16px; 
    border-radius: 12px; font-size: 13px; 
    color: #b45309; line-height: 1.6; 
    border-left: 4px solid #f59e0b;
}

/* 规格弹窗高级优化 */
:deep(.spec-dialog) {
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
:deep(.spec-dialog .el-dialog__header) { margin:0; padding:0; }
:deep(.spec-dialog .el-dialog__body) { padding: 0; }
:deep(.spec-dialog .el-dialog__footer) { padding: 0; }

.spec-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 24px;
    background: #f8fafc;
    border-bottom: 1px solid #f1f5f9;
    position: relative;
}
.spec-avatar {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    overflow: hidden;
    background: #f1f5f9;
    display: flex;
    justify-content: center;
    align-items: center;
}
.spec-avatar .el-image { width: 100%; height: 100%; }
.spec-title-box { flex: 1; }
.spec-title { font-size: 18px; font-weight: 800; color: #1e293b; margin-bottom: 6px; }
.spec-desc { font-size: 13px; color: #64748b; line-height: 1.4; font-weight: 600; }
.close-icon {
    position: absolute;
    top: 24px;
    right: 24px;
    font-size: 20px;
    color: #94a3b8;
    cursor: pointer;
    transition: color 0.2s;
}
.close-icon:hover { color: #1e293b; }

.spec-scroll-wrap {
    max-height: 400px;
    overflow-y: auto;
    padding: 24px;
}
.spec-scroll-wrap::-webkit-scrollbar { width: 0; display: none; }

.spec-group { margin-bottom: 24px; }
.spec-group:last-child { margin-bottom: 0; }
.spec-label { font-size: 15px; font-weight: 800; color: #334155; margin-bottom: 14px; }
.spec-opts { display: flex; flex-wrap: wrap; gap: 12px; }
.spec-opt {
    padding: 10px 20px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    color: #475569;
    cursor: pointer;
    transition: all 0.2s ease;
    user-select: none;
}
.spec-opt:hover { border-color: #cbd5e1; color: #1e293b; }
.spec-opt.active {
    background: #fffbef;
    border-color: #FFC300;
    color: #d97706;
}

.spec-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: #ffffff;
    border-top: 1px solid #f1f5f9;
}
.spec-price-wrap { color: #e11d48; display: flex; align-items: baseline; }
.spec-price-wrap .currency { font-size: 16px; margin-right: 4px; font-weight: 800; }
.spec-price-wrap .price-val { font-size: 32px; font-weight: 800; font-family: 'Plus Jakarta Sans', monospace; letter-spacing: -1px; }

.spec-actions { display: flex; gap: 12px; }
.add-cart-confirm-btn {
    padding: 0 28px;
    height: 44px;
    border-radius: 100px;
    font-weight: 800;
    background: #FFC300;
    color: #222222;
    border: none;
    box-shadow: 0 6px 16px rgba(255, 195, 0, 0.3);
    transition: all 0.2s ease;
    cursor: pointer;
    display: flex;
    align-items: center;
    font-size: 15px;
}
.add-cart-confirm-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(255, 195, 0, 0.4);
    background: #ffce33;
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>