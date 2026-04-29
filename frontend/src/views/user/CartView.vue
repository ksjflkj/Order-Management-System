<template>
    <div class="cart-container">
        <div class="cart-main">
            <div class="page-header">
                <h2 class="page-title">
                    <div class="icon-wrapper">
                        <el-icon><ShoppingCart /></el-icon>
                    </div>
                    我的购物车
                </h2>
                <div class="header-actions" v-if="cartList.length > 0">
                    <button class="modern-btn btn-danger-soft" @click="handleClear">
                        <el-icon><Delete /></el-icon> 清空购物车
                    </button>
                </div>
            </div>

            <div v-if="cartList.length === 0" class="premium-card empty-cart">
                <el-empty 
                    description="您的购物车空空如也，快去发现心仪的美食吧" 
                    :image-size="200"
                >
                    <template #default>
                        <button class="modern-btn btn-primary" @click="router.push('/user/merchant-list')" style="min-width: 160px;">
                            去逛逛
                        </button>
                    </template>
                </el-empty>
            </div>

            <template v-else>
                <!-- 按商家分组展示 -->
                <div v-for="group in groupedCart" :key="group.merchantId" class="merchant-group"
                     :class="{ 'is-selected': selectedMerchantId === group.merchantId }">
                    <div class="premium-card order-info-card" @click="selectMerchant(group.merchantId)">
                        <div class="shop-header">
                            <div class="shop-name-wrap">
                                <div class="radio-indicator" :class="{ active: selectedMerchantId === group.merchantId }">
                                    <div class="radio-dot"></div>
                                </div>
                                <div class="shop-icon"><el-icon><Shop /></el-icon></div>
                                <span class="shop-name">{{ group.shopName }}</span>
                                <span class="item-count">{{ group.items.length }}件商品</span>
                            </div>
                        </div>

                        <div class="group-table-wrap">
                            <el-table :data="group.items" class="custom-table" empty-text="暂无菜品">
                                <el-table-column label="菜品信息" min-width="280">
                                    <template #default="{ row }">
                                        <div class="dish-info-cell">
                                            <div class="dish-image-box">
                                                <el-image v-if="row.dishImage" :src="resolveUrl(row.dishImage)" fit="cover" class="dish-img"/>
                                                <div v-else class="dish-img-placeholder">
                                                    <el-icon><Picture /></el-icon>
                                                </div>
                                            </div>
                                            <div class="dish-details">
                                                <div class="dish-name">{{ row.dishName }}</div>
                                                <div v-if="row.dishTags" class="dish-tags">{{ row.dishTags }}</div>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="price" label="单价" width="120" align="center">
                                    <template #default="{ row }">
                                        <span class="price-text">¥{{ row.price }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column label="数量" width="180" align="center">
                                    <template #default="{ row }">
                                        <div @click.stop>
                                            <el-input-number 
                                                :model-value="row.quantity" 
                                                :min="1" 
                                                :max="row.stock"
                                                class="premium-input-number"
                                                @change="(val) => changeQuantity(row, val)" 
                                            />
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="小计" width="140" align="center">
                                    <template #default="{ row }">
                                        <span class="subtotal-text">¥{{ (Number(row.price) * row.quantity).toFixed(2) }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" width="100" align="center">
                                    <template #default="{ row }">
                                        <button type="button" class="action-delete" @click.stop="handleDelete(row.id)">删除</button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>

                        <div class="group-subtotal">
                            <span>小计：</span>
                            <span class="group-amount">¥{{ group.subtotal }}</span>
                        </div>
                    </div>
                </div>

                <!-- 弹性占位符 -->
                <div class="flex-spacer"></div>

                <!-- 收货地址选择 (移到底部结算栏上方) -->
                <div class="premium-card address-card" v-if="selectedMerchantId">
                    <div class="address-box">
                        <div class="address-label">
                            <el-icon><Location /></el-icon> 收货地址
                        </div>
                        <div class="address-content">
                            <el-select 
                                v-model="selectedAddressId" 
                                placeholder="请选择收货地址" 
                                class="premium-input address-select"
                                size="large"
                            >
                                <el-option v-for="item in addressList" :key="item.id"
                                    :label="`${item.contactName} ${item.contactPhone} - ${item.province || ''}${item.city || ''}${item.district || ''}${item.detailAddress}`"
                                    :value="item.id" />
                            </el-select>
                            <button type="button" class="modern-btn btn-secondary-text" @click="goAddressManage">管理地址</button>
                        </div>
                    </div>
                </div>

                <!-- Floating checkout bar -->
                <div class="floating-checkout-bar" :class="{ 'no-selection': !selectedMerchantId }">
                    <div class="checkout-left">
                        <div class="coupon-section" v-if="selectedMerchantId">
                            <div class="coupon-label">
                                <el-icon><Ticket /></el-icon> 优惠券
                            </div>
                            <el-select 
                                v-model="selectedCouponId" 
                                placeholder="不使用优惠券" 
                                clearable 
                                class="premium-input coupon-select"
                            >
                                <el-option v-for="coupon in myCoupons" :key="coupon.id"
                                    :label="coupon.type === 'DISCOUNT' ? `${coupon.name} - ${parseFloat((coupon.value / 10).toFixed(1))}折` : `${coupon.name} - 减${coupon.value}元`"
                                    :value="coupon.id" />
                            </el-select>
                        </div>
                        <div v-else class="select-hint">
                            <el-icon><InfoFilled /></el-icon>
                            <span>请选择一个商家进行结算</span>
                        </div>
                    </div>
                    
                    <div class="checkout-right">
                        <div class="summary-info" v-if="selectedMerchantId">
                            <!-- 平滑过渡动画包裹 -->
                            <div class="discount-wrapper" :class="{ 'is-active': discountAmount > 0 }">
                                <div class="discount-content">
                                    <div class="original-total">
                                        订单总额：<span>¥{{ totalAmount }}</span>
                                    </div>
                                    <div class="discount-display">
                                        已优惠：<span>- ¥{{ discountAmount }}</span>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="final-total">
                                <span class="final-text-label">实付金额：</span>
                                <span class="highlight-amount">¥<span class="highlight-number">{{ finalAmount }}</span></span>
                            </div>
                        </div>
                        <button type="button" class="modern-btn btn-primary submit-btn" 
                                @click="handleCheckout" 
                                :disabled="!selectedMerchantId">
                            {{ selectedMerchantId ? '立即结算' : '请选择商家' }}
                        </button>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearCart, deleteCartItem, getCartList, updateCart } from '@/api/cart'
import { createOrder } from '@/api/order'
import { getAddressList } from '@/api/address'
import { getMyCoupons } from '@/api/coupon'
import { ShoppingCart, Delete, Shop, Location, Picture, Ticket, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const cartList = ref([])
const addressList = ref([])
const selectedAddressId = ref(null)
const selectedCouponId = ref(null)
const selectedMerchantId = ref(null)
const myCoupons = ref([])
const discountAmount = ref(0)

const resolveUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}

// 按商家分组
const groupedCart = computed(() => {
    const groups = {}
    cartList.value.forEach(item => {
        const mid = item.merchantId
        if (!groups[mid]) {
            groups[mid] = {
                merchantId: mid,
                shopName: item.shopName,
                items: [],
                subtotal: '0.00'
            }
        }
        groups[mid].items.push(item)
    })
    // 计算每组小计
    Object.values(groups).forEach(g => {
        g.subtotal = g.items
            .reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)
            .toFixed(2)
    })
    return Object.values(groups)
})

// 选中商家
const selectMerchant = (merchantId) => {
    selectedMerchantId.value = selectedMerchantId.value === merchantId ? null : merchantId
    // 切换商家时重置优惠券
    selectedCouponId.value = null
    discountAmount.value = 0
}

// 当前选中商家的总金额
const totalAmount = computed(() => {
    if (!selectedMerchantId.value) return '0.00'
    const group = groupedCart.value.find(g => g.merchantId === selectedMerchantId.value)
    return group ? group.subtotal : '0.00'
})

const finalAmount = computed(() => {
    const total = Number(totalAmount.value)
    return (total - discountAmount.value).toFixed(2)
})

const loadData = async () => {
    const res = await getCartList()
    if (res.code === 200) {
        cartList.value = res.data || []
        // 如果之前选中的商家已经没有购物车了，自动取消选择
        if (selectedMerchantId.value) {
            const still = cartList.value.some(item => item.merchantId === selectedMerchantId.value)
            if (!still) selectedMerchantId.value = null
        }
        // 如果只有一个商家，自动选中
        const merchants = [...new Set(cartList.value.map(i => i.merchantId))]
        if (merchants.length === 1 && !selectedMerchantId.value) {
            selectedMerchantId.value = merchants[0]
        }
    }
}

const loadAddressData = async () => {
    const res = await getAddressList()
    if (res.code === 200) {
        addressList.value = res.data.records || []
        const defaultAddress = addressList.value.find(item => item.isDefault === 1)
        selectedAddressId.value = defaultAddress ? defaultAddress.id : (addressList.value[0]?.id || null)
    }
}

const loadCoupons = async () => {
    try {
        const res = await getMyCoupons({ status: 'UNUSED', current: 1, size: 100 })
        if (res.code === 200) {
            myCoupons.value = res.data.records || []
        }
    } catch (e) {
        console.error('加载优惠券失败', e)
    }
}

watch(selectedCouponId, async (newVal) => {
    if (newVal) {
        const coupon = myCoupons.value.find(c => c.id === newVal)
        if (coupon && Number(totalAmount.value) >= Number(coupon.minAmount)) {
            if (coupon.type === 'DISCOUNT') {
                // 折扣券：value 存折扣百分比（如 90 = 九折），优惠金额 = 总额 × (100 - value) / 100
                const foldValue = Number(coupon.value)
                discountAmount.value = parseFloat((Number(totalAmount.value) * (100 - foldValue) / 100).toFixed(2))
            } else {
                // 满减券 / 代金券：直接减免固定金额
                discountAmount.value = Number(coupon.value)
            }
        } else {
            discountAmount.value = 0
            if (coupon) {
                ElMessage.warning(`订单金额未达到优惠券使用条件（满${coupon.minAmount}元）`)
            }
        }
    } else {
        discountAmount.value = 0
    }
})

const changeQuantity = async (item, quantity) => {
    if (!quantity || quantity < 1) return
    if (quantity > item.stock) {
        ElMessage.error('数量不能超过库存')
        return
    }

    try {
        const res = await updateCart({
            id: item.id,
            quantity
        })
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        ElMessage.error(error.message || '更新失败')
    }
}

const handleDelete = async (id) => {
    try {
        await ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
        const res = await deleteCartItem(id)
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '删除失败')
        }
    }
}

const handleClear = async () => {
    try {
        await ElMessageBox.confirm('确认清空购物车吗？', '提示', { type: 'warning' })
        const res = await clearCart()
        ElMessage.success(res.message)
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '清空失败')
        }
    }
}

const handleCheckout = async () => {
    if (!selectedMerchantId.value) {
        ElMessage.warning('请先选择要结算的商家')
        return
    }
    if (!selectedAddressId.value) {
        ElMessage.warning('请先新增收货地址')
        return
    }

    try {
        const res = await createOrder({
            addressId: selectedAddressId.value,
            merchantId: selectedMerchantId.value,
            remark: '用户提交订单',
            couponId: selectedCouponId.value
        })
        ElMessage.success(res.message)
        // 结算后刷新购物车（其他商家菜品仍保留）
        await loadData()
        if (cartList.value.length === 0) {
            router.push('/user/orders')
        }
    } catch (error) {
        ElMessage.error(error.message || '下单失败')
    }
}

const goAddressManage = () => {
    router.push('/user/address')
}

onMounted(() => {
    loadData()
    loadAddressData()
    loadCoupons()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');

.cart-container {
    padding: 40px 48px;
    max-width: 1300px;
    margin: 0 auto;
    background-color: #f5f7fa;
    border-radius: 24px;
    min-height: calc(100vh - 108px);
    animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
    display: flex;
    flex-direction: column;
}

.cart-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    position: relative;
    padding-bottom: 24px;
}

.flex-spacer {
    flex: 1;
    min-height: 24px;
}

/* Page Header */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
}

.page-title {
    font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
    font-size: 28px;
    font-weight: 700;
    color: #0f172a;
    display: flex;
    align-items: center;
    gap: 16px;
    margin: 0;
    letter-spacing: -0.02em;
}

.icon-wrapper {
    background: #ffffff;
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14px;
    font-size: 24px;
    color: #222222;
    box-shadow: 0 4px 16px rgba(255, 195, 0, 0.15);
}

/* Merchant Group */
.merchant-group {
    margin-bottom: 24px;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.merchant-group .order-info-card {
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    border: 2px solid transparent;
}

.merchant-group.is-selected .order-info-card {
    border-color: #FFC300;
    box-shadow: 0 4px 24px rgba(255, 195, 0, 0.15), 0 1px 2px rgba(0, 0, 0, 0.02);
}

.merchant-group:not(.is-selected) .order-info-card:hover {
    border-color: #e2e8f0;
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}

/* Radio indicator */
.radio-indicator {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    border: 2px solid #cbd5e1;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    flex-shrink: 0;
}

.radio-indicator.active {
    border-color: #FFC300;
    background: #FFC300;
}

.radio-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: transparent;
    transition: all 0.2s ease;
}

.radio-indicator.active .radio-dot {
    background: #ffffff;
}

/* Premium Main Cards */
.premium-card {
    background: #ffffff;
    border-radius: 24px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
    padding: 32px 40px;
    border: 1px solid #f1f5f9;
    transition: transform 0.2s ease;
}

.empty-cart {
    padding: 80px 0;
}

/* Shop Header */
.shop-header {
    padding-bottom: 20px;
    border-bottom: 1px dashed #e2e8f0;
    margin-bottom: 20px;
}

.shop-name-wrap {
    display: flex;
    align-items: center;
    gap: 14px;
}

.shop-icon {
    font-size: 24px;
    color: #FFC300;
    background: #FFF8E1;
    padding: 10px;
    border-radius: 12px;
    display: flex;
}

.shop-name {
    font-family: 'Plus Jakarta Sans', system-ui;
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
}

.item-count {
    font-size: 13px;
    color: #94a3b8;
    background: #f1f5f9;
    padding: 4px 12px;
    border-radius: 20px;
    font-weight: 600;
    margin-left: auto;
}

/* Group subtotal */
.group-subtotal {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px dashed #e2e8f0;
    margin-top: 16px;
    font-size: 15px;
    color: #475569;
    font-weight: 600;
}

.group-amount {
    font-family: 'Plus Jakarta Sans', monospace;
    font-size: 20px;
    font-weight: 800;
    color: #FF4A26;
}

/* Address Card */
.address-card {
    margin-bottom: 24px;
    padding: 24px 40px;
}

.address-box {
    display: flex;
    align-items: center;
}

.address-label {
    width: 140px;
    color: #475569;
    font-weight: 600;
    font-size: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.address-content {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 16px;
}

.address-select {
    flex: 1;
    max-width: 600px;
}

/* Select hint */
.select-hint {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #94a3b8;
    font-size: 15px;
    font-weight: 500;
}

.select-hint .el-icon {
    font-size: 18px;
}

/* Premium Form Elements */
.premium-input :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 8px 16px;
    background-color: #f8fafc;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    transition: all 0.2s ease;
}

.premium-input :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #cbd5e1 inset;
    background-color: #ffffff;
}

.premium-input :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 3px rgba(255, 195, 0, 0.15), 0 0 0 1px #FFC300 inset;
    background-color: #ffffff;
}

/* Table */
.group-table-wrap {
    padding: 0;
}

.custom-table {
    border-radius: 16px;
    overflow: hidden;
}

.custom-table :deep(th.el-table__cell) {
    background-color: #f8fafc !important;
    color: #475569 !important;
    font-weight: 600 !important;
    font-size: 14px;
    border-bottom: 2px solid #e2e8f0;
    padding: 16px 0;
}

.custom-table :deep(td.el-table__cell) {
    border-bottom: 1px solid #f1f5f9;
    padding: 20px 0;
}

.dish-info-cell {
    display: flex;
    align-items: center;
    gap: 20px;
}

.dish-image-box {
    width: 72px;
    height: 72px;
    border-radius: 12px;
    background: #f1f5f9;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.dish-img {
    width: 100%;
    height: 100%;
}

.dish-img-placeholder {
    font-size: 28px;
    color: #cbd5e1;
}

.dish-details {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.dish-name {
    font-family: 'Plus Jakarta Sans', system-ui;
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
}

.dish-tags {
    font-size: 13px;
    color: #64748b;
    background: #f1f5f9;
    padding: 4px 10px;
    border-radius: 6px;
    display: inline-block;
    align-self: flex-start;
    font-weight: 500;
}

.price-text {
    font-family: 'Plus Jakarta Sans', monospace;
    font-weight: 700;
    color: #475569;
    font-size: 16px;
}

.subtotal-text {
    font-family: 'Plus Jakarta Sans', monospace;
    font-weight: 800;
    color: #FF4A26;
    font-size: 18px;
}

.premium-input-number :deep(.el-input-number__decrease),
.premium-input-number :deep(.el-input-number__increase) {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    transition: all 0.2s ease;
}
.premium-input-number :deep(.el-input-number__decrease:hover),
.premium-input-number :deep(.el-input-number__increase:hover) {
    color: #FFC300;
}
.premium-input-number :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e2e8f0 inset;
}

/* Floating Checkout Bar */
.floating-checkout-bar {
    position: sticky;
    bottom: 32px;
    margin: 0 auto;
    width: 90%;
    max-width: 900px;
    background: rgba(255, 255, 255, 0.82);
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
    border-radius: 28px;
    padding: 20px 36px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 20px 48px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(255, 255, 255, 0.6) inset, 0 0 0 1px rgba(0, 0, 0, 0.04);
    z-index: 100;
    transition: all 0.3s ease;
}

.floating-checkout-bar.no-selection {
    background: rgba(248, 250, 252, 0.9);
}

.checkout-left {
    display: flex;
    align-items: center;
}

.coupon-section {
    display: flex;
    align-items: center;
    gap: 16px;
}

.coupon-label {
    font-size: 15px;
    color: #334155;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
}
.coupon-label .el-icon {
    color: #eab308;
    font-size: 18px;
}

.coupon-select {
    width: 280px;
}

.checkout-right {
    display: flex;
    align-items: center;
    gap: 40px;
}

.summary-info {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
}

/* 核心技巧：利用 0fr 到 1fr 的 Grid 动画 */
.discount-wrapper {
    display: grid;
    grid-template-rows: 0fr;
    opacity: 0;
    transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.discount-wrapper.is-active {
    grid-template-rows: 1fr;
    opacity: 1;
    margin-bottom: 8px;
}

.discount-content {
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
}

.original-total {
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
}

.discount-display {
    font-size: 14px;
    color: #10b981;
    font-weight: 600;
    background: #d1fae5;
    padding: 2px 10px;
    border-radius: 6px;
}

.final-total {
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.final-text-label {
    font-size: 15px;
    font-weight: 600;
    color: #334155;
}

.highlight-amount {
    font-family: 'Plus Jakarta Sans', monospace;
    font-weight: 800;
    color: #FF4A26;
    font-size: 20px;
}

.highlight-number {
    font-size: 32px;
    letter-spacing: -0.02em;
}

/* Modern Buttons Core */
.modern-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    border: none;
    font-family: inherit;
    gap: 8px;
}

.btn-primary {
    background: #FFC300;
    color: #222222;
    box-shadow: 0 4px 14px rgba(255, 195, 0, 0.3);
    padding: 14px 40px;
    height: 52px;
    font-size: 16px;
}

.btn-primary:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 195, 0, 0.4);
    background: #E5AF00;
}

.btn-primary:active:not(:disabled) {
    transform: translateY(0);
}

.btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    background: #94a3b8;
    box-shadow: none;
}

.btn-secondary-text {
    background: transparent;
    color: #FFC300;
    padding: 10px 16px;
}
.btn-secondary-text:hover {
    background: #FFF8E1;
}

.btn-danger-soft {
    background: #fef2f2;
    color: #ef4444;
    padding: 10px 20px;
    border: 1px solid #fee2e2;
}
.btn-danger-soft:hover {
    background: #fee2e2;
    transform: translateY(-1px);
}

.action-delete {
    background: transparent;
    border: none;
    color: #ef4444;
    font-weight: 600;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 6px;
    transition: all 0.2s ease;
}
.action-delete:hover {
    background: #fef2f2;
}

/* Animation */
@keyframes slideUp {
    from { opacity: 0; transform: translateY(15px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>