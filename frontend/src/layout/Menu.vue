<template>
    <div class="sidebar-header">
        <h2 class="sys-title" v-if="!isCollapse">{{ title }}</h2>
        <h2 class="sys-title" v-else>{{ shortTitle }}</h2>
    </div>
    <el-menu
        :default-active="route.path"
        class="el-menu-vertical-demo"
        :collapse="isCollapse"
        unique-opened
        router
        background-color="transparent"
        text-color="#b0b0b0"
        active-text-color="#222222"
    >
        <template v-if="role === 'ADMIN'">
            <el-menu-item index="/admin/home">
                <el-icon><HomeFilled /></el-icon>
                <template #title><span>后台首页</span></template>
            </el-menu-item>
            <el-menu-item index="/admin/merchant">
                <el-icon><Shop /></el-icon>
                <template #title><span>商家管理</span></template>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
                <el-icon><List /></el-icon>
                <template #title><span>订单管理</span></template>
            </el-menu-item>
            <el-menu-item index="/admin/coupon">
                <el-icon><Ticket /></el-icon>
                <template #title><span>优惠券管理</span></template>
            </el-menu-item>
            <el-menu-item index="/admin/activity">
                <el-icon><Calendar /></el-icon>
                <template #title><span>满减活动</span></template>
            </el-menu-item>
            <el-menu-item index="/admin/reviews">
                <el-icon><Comment /></el-icon>
                <template #title><span>评价管理</span></template>
            </el-menu-item>
            <el-menu-item index="/admin/users">
                <el-icon><UserFilled /></el-icon>
                <template #title><span>用户管理</span></template>
            </el-menu-item>
            <el-menu-item index="__logout__" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <template #title><span>退出登录</span></template>
            </el-menu-item>
        </template>

        <template v-else-if="role === 'MERCHANT'">
            <el-menu-item index="/merchant/home">
                <el-icon><HomeFilled /></el-icon>
                <template #title><span>商家首页</span></template>
            </el-menu-item>
            <el-menu-item index="/merchant/shop">
                <el-icon><Shop /></el-icon>
                <template #title><span>店铺管理</span></template>
            </el-menu-item>
            <el-menu-item index="/merchant/dish">
                <el-icon><Goods /></el-icon>
                <template #title><span>菜品管理</span></template>
            </el-menu-item>
            <el-menu-item index="/merchant/orders">
                <el-icon><List /></el-icon>
                <template #title><span>订单管理</span></template>
            </el-menu-item>
            <el-menu-item index="/merchant/statistics">
                <el-icon><PieChart /></el-icon>
                <template #title><span>数据统计</span></template>
            </el-menu-item>
            <el-menu-item index="/merchant/reviews">
                <el-icon><Comment /></el-icon>
                <template #title><span>评价管理</span></template>
            </el-menu-item>
            <el-menu-item index="/user/home">
                <el-icon><User /></el-icon>
                <template #title><span>返回用户端</span></template>
            </el-menu-item>
            <el-menu-item index="__logout__" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <template #title><span>退出登录</span></template>
            </el-menu-item>
        </template>

        <template v-else>
            <el-menu-item index="/user/home">
                <el-icon><HomeFilled /></el-icon>
                <template #title><span>用户首页</span></template>
            </el-menu-item>

            <el-sub-menu index="/user/settings">
                <template #title>
                    <el-icon><User /></el-icon>
                    <span>个人设置</span>
                </template>
                <el-menu-item index="/user/settings/profile">
                    <el-icon><Document /></el-icon>
                    <template #title><span>基本信息</span></template>
                </el-menu-item>
                <el-menu-item index="/user/settings/password">
                    <el-icon><Lock /></el-icon>
                    <template #title><span>修改密码</span></template>
                </el-menu-item>
                <el-menu-item index="/user/settings/phone">
                    <el-icon><Phone /></el-icon>
                    <template #title><span>手机绑定</span></template>
                </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/user/merchant-list">
                <el-icon><Goods /></el-icon>
                <template #title><span>商家列表</span></template>
            </el-menu-item>

            <el-menu-item index="/user/orders">
                <el-icon><List /></el-icon>
                <template #title><span>我的订单</span></template>
            </el-menu-item>

            <el-menu-item index="/user/cart">
                <el-icon><ShoppingCart /></el-icon>
                <template #title><span>购物车</span></template>
            </el-menu-item>

            <el-sub-menu index="/user/address">
                <template #title>
                    <el-icon><Location /></el-icon>
                    <span>地址管理</span>
                </template>
                <el-menu-item index="/user/address">
                    <el-icon><List /></el-icon>
                    <template #title><span>我的地址</span></template>
                </el-menu-item>
                <el-menu-item index="/user/address/add">
                    <el-icon><Plus /></el-icon>
                    <template #title><span>新增地址</span></template>
                </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/user/reviews">
                <el-icon><Comment /></el-icon>
                <template #title><span>我的评价</span></template>
            </el-menu-item>

            <el-sub-menu index="/user/coupon">
                <template #title>
                    <el-icon><Ticket /></el-icon>
                    <span>优惠券区</span>
                </template>
                <el-menu-item index="/user/coupon/available">
                    <el-icon><Discount /></el-icon>
                    <template #title><span>去领券</span></template>
                </el-menu-item>
                <el-menu-item index="/user/coupon/my">
                    <el-icon><Wallet /></el-icon>
                    <template #title><span>我的卡包</span></template>
                </el-menu-item>
                <el-menu-item index="/user/coupon/activities">
                    <el-icon><Calendar /></el-icon>
                    <template #title><span>进行中的活动</span></template>
                </el-menu-item>
            </el-sub-menu>

            <!-- 商家入驻 / 进入商家后台（仅真实用户显示状态区分） -->
            <el-menu-item v-if="hasApprovedMerchant && !isGuest" index="/merchant/home">
                <el-icon><Shop /></el-icon>
                <template #title><span>进入商家后台</span></template>
            </el-menu-item>
            <el-menu-item v-else index="/user/merchant-apply">
                <el-icon><Shop /></el-icon>
                <template #title><span>商家入驻</span></template>
            </el-menu-item>

            <!-- 退出登录 / 立即登录 -->
            <el-menu-item v-if="!isGuest" index="__logout__" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <template #title><span>退出登录</span></template>
            </el-menu-item>
            <el-menu-item v-else index="" @click="goLogin">
                <el-icon><SwitchButton /></el-icon>
                <template #title><span style="color:#409eff;font-weight:600">立即登录</span></template>
            </el-menu-item>
        </template>
    </el-menu>
</template>

<script setup lang="ts">
import { inject, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
    HomeFilled, Shop, List, SwitchButton, Goods, ShoppingCart, Location, Comment, User, UserFilled, Ticket, Calendar, PieChart,
    Document, Lock, Phone, Wallet, Discount, Plus
} from '@element-plus/icons-vue'

const isCollapse = inject('isCollapse')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 是否拥有已审核通过的店铺（用于切换"商家入驻" vs "进入商家后台"）
const hasApprovedMerchant = computed(() => userStore.hasApprovedMerchant)
const isGuest = computed(() => userStore.isGuest)

const handleLogout = () => {
    userStore.logout()
    window.location.href = '/login'
}

const goLogin = () => {
    router.push('/login')
}

const role = computed(() => {
    if (route.path.startsWith('/admin')) return 'ADMIN'
    if (route.path.startsWith('/merchant')) return 'MERCHANT'
    return 'USER'
})

const title = computed(() => {
    if (role.value === 'ADMIN') return '管理端'
    if (role.value === 'MERCHANT') return '商家端'
    return '用户端'
})

const shortTitle = computed(() => {
    if (role.value === 'ADMIN') return '管理'
    if (role.value === 'MERCHANT') return '商家'
    return '用户'
})
</script>

<style lang="scss" scoped>
.sidebar-header {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #FFC300;
    background-color: #1a1a1a;
    box-shadow: 0 1px 4px rgba(0,0,0,0.2);
}
.sys-title {
    margin: 0;
    font-size: 20px;
    font-weight: 800;
    letter-spacing: 1px;
    white-space: nowrap;
    overflow: hidden;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 230px;
    min-height: 400px;
}
.el-menu {
    border-right: none;
    flex: 1;
}

:deep(.el-sub-menu .el-sub-menu__title) {
    color: #b0b0b0 !important;
}
:deep(.el-sub-menu .el-sub-menu__title:hover) {
    background-color: #333333 !important;
    color: #ffffff !important;
}

/* 所有子菜单中文字的颜色 */
:deep(.el-menu .el-menu-item) {
    color: #b0b0b0;
}
/* 当前打开菜单的所有子菜单颜色 (非活跃状态) */
:deep(.is-opened .el-menu-item:not(.is-active)) {
    background-color: #1f1f1f !important;
}
/* 鼠标移动菜单的颜色 */
:deep(.el-menu-item:not(.is-active):hover) {
    background-color: #333333 !important;
    color: #ffffff !important;
}
/* 菜单点中文字的颜色 (最高优先级) */
:deep(.el-menu-item.is-active),
:deep(.is-opened .el-menu-item.is-active) {
    color: #222222 !important;
    background-color: #FFC300 !important;
    font-weight: 700 !important;
}
</style>
