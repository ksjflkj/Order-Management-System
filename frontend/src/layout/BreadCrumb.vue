<template>
    <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: homePath }">首页</el-breadcrumb-item>
        
        <template v-for="(item, index) in matchedRoutes" :key="index">
            <el-breadcrumb-item :to="item.path && index < matchedRoutes.length - 1 ? { path: item.path } : undefined">
                {{ item.title }}
            </el-breadcrumb-item>
        </template>
    </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const homePath = computed(() => {
    if (route.path.startsWith('/admin')) return '/admin/home'
    if (route.path.startsWith('/merchant')) return '/merchant/home'
    return '/user/home'
})

const map: Record<string, string> = {
    'admin-home': '后台首页',
    'merchant-manage': '商家管理',
    'admin-orders': '订单管理',
    'admin-order-detail': '订单详情',
    'admin-review-manage': '评价管理',
    'coupon-manage': '优惠券管理',
    'activity-manage': '活动管理',
    'merchant-home': '商家首页',
    'dish-manage': '菜品管理',
    'shop-manage': '店铺管理',
    'merchant-orders': '订单管理',
    'merchant-order-detail': '订单详情',
    'merchant-review-manage': '评价管理',
    'merchant-statistics': '数据统计',
    'user-home': '用户首页',
    'merchant-list': '商家列表',
    'merchant-detail': '商家详情',
    'order-list': '我的订单',
    'order-detail': '订单详情',
    'order-review': '订单评价',
    'cart': '购物车',
    'address-manage': '地址管理',
    'address-list': '我的地址',
    'address-add': '新增地址',
    'address-edit': '编辑地址',
    'review-list': '我的评价',
    'merchant-apply': '商家入驻',
    'coupon-center': '领券中心'
}

const matchedRoutes = computed(() => {
    const list: Array<{title: string, path: string}> = []
    
    route.matched.forEach(item => {
        // Skip root layouts
        if (!item.path || item.path === '/' || item.path === '/user' || item.path === '/admin' || item.path === '/merchant') {
            return
        }
        
        let t = item.meta?.title || map[item.name as string]
        if (t && item.path !== homePath.value) {
            // Avoid duplicate text with next level if redirect matches
            list.push({ title: t as string, path: item.redirect as string || item.path })
        }
    })

    if (list.length === 0) {
        let t = map[route.name as string] || route.meta?.title
        if (t && route.path !== homePath.value) {
            list.push({ title: t as string, path: '' })
        }
    }
    
    return list
})
</script>

<style scoped>
:deep(.el-breadcrumb__inner) {
    color: #222222 !important;
}

:deep(.el-breadcrumb__inner.is-link) {
    color: #555555 !important;
}

:deep(.el-breadcrumb__inner.is-link:hover) {
    color: #000000 !important;
}
</style>
