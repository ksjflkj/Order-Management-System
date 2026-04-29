import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { getGuestToken } from '@/api/auth'

import LoginView from '@/views/login/LoginView.vue'
import RegisterView from '@/views/login/RegisterView.vue'
import ForgotPassword from '@/views/login/ForgotPassword.vue'

import Layout from '@/layout/index.vue'

import AdminHome from '@/views/admin/AdminHome.vue'
import MerchantManage from '@/views/admin/MerchantManage.vue'
import UserManage from '@/views/admin/UserManage.vue'

import MerchantHome from '@/views/merchant/MerchantHome.vue'
import DishManage from '@/views/merchant/DishManage.vue'
import ShopManage from '@/views/merchant/ShopManage.vue'

import UserHome from '@/views/user/UserHome.vue'
import UserSettings from '@/views/user/UserSettings.vue'
import MerchantList from '@/views/user/MerchantList.vue'
import OrderList from '@/views/user/OrderList.vue'
import GuestNoticePage from '@/views/user/GuestNoticePage.vue'

import MerchantApply from '@/views/user/MerchantApply.vue'

import MerchantDetail from '@/views/user/MerchantDetail.vue'

import CartView from '@/views/user/CartView.vue'

import OrderDetail from '@/views/user/OrderDetail.vue'

import OrderManage from '@/views/merchant/OrderManage.vue'
import MerchantOrderDetail from '@/views/merchant/OrderDetail.vue'

import AddressManage from '@/views/user/AddressManage.vue'

import ReviewCreate from '@/views/user/ReviewCreate.vue'
import ReviewList from '@/views/user/ReviewList.vue'

import CouponCenter from '@/views/user/CouponCenter.vue'

import AdminOrderManage from '@/views/admin/OrderManage.vue'
import AdminOrderDetail from '@/views/admin/OrderDetail.vue'
import CouponManage from '@/views/admin/CouponManage.vue'
import ActivityManage from '@/views/admin/ActivityManage.vue'
import AdminReviewManage from '@/views/admin/ReviewManage.vue'

import MerchantReviewManage from '@/views/merchant/MerchantReviewManage.vue'
import MerchantStatistics from '@/views/merchant/MerchantStatistics.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: ForgotPassword
  },

  {
    path: '/admin',
    component: Layout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: '',
        redirect: '/admin/home'
      },
      {
        path: 'home',
        name: 'admin-home',
        component: AdminHome
      },
      {
        path: 'merchant',
        name: 'merchant-manage',
        component: MerchantManage
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: AdminOrderManage
      },
      {
        path: 'orders/:id',
        name: 'admin-order-detail',
        component: AdminOrderDetail
      },
      {
        path: 'coupon',
        name: 'coupon-manage',
        component: CouponManage
      },
      {
        path: 'activity',
        name: 'activity-manage',
        component: ActivityManage
      },
      {
        path: 'reviews',
        name: 'admin-review-manage',
        component: AdminReviewManage
      },
      {
        path: 'users',
        name: 'admin-user-manage',
        component: UserManage
      }
    ]
  },

  {
    path: '/merchant',
    component: Layout,
    meta: { requiresAuth: true, role: 'MERCHANT' },
    children: [
      {
        path: '',
        redirect: '/merchant/home'
      },
      {
        path: 'home',
        name: 'merchant-home',
        component: MerchantHome
      },
      {
        path: 'dish',
        name: 'dish-manage',
        component: DishManage
      },
      {
        path: 'shop',
        name: 'shop-manage',
        component: ShopManage
      },
      {
        path: 'orders',
        name: 'merchant-orders',
        component: OrderManage
      },
      {
        path: 'orders/:id',
        name: 'merchant-order-detail',
        component: MerchantOrderDetail
      },
      {
        path: 'reviews',
        name: 'merchant-review-manage',
        component: MerchantReviewManage
      },
      {
        path: 'statistics',
        name: 'merchant-statistics',
        component: MerchantStatistics
      }
    ]
  },

  {
    path: '/user',
    component: Layout,
    meta: { requiresAuth: true, role: 'USER' },
    children: [
      {
        path: '',
        redirect: '/user/home'
      },
      {
        path: 'home',
        name: 'user-home',
        component: UserHome
      },
      {
        path: 'guest-notice',
        name: 'guest-notice',
        component: GuestNoticePage
        // 注意：不加 requiresRealAuth，游客可以访问此页
      },
      {
        path: '/user/settings',
        component: UserSettings,
        name: 'user-settings',
        meta: {
          title: '个人设置',
          icon: 'Setting',
          roles: ['USER'],
          requiresRealAuth: true
        },
        redirect: '/user/settings/profile',
        children: [
          {
            path: '/user/settings/profile',
            component: () => import('@/views/user/settings/Profile.vue'),
            name: 'user-settings-profile',
            meta: { title: '基本信息', icon: 'User', roles: ['USER'], requiresRealAuth: true }
          },
          {
            path: '/user/settings/password',
            component: () => import('@/views/user/settings/Password.vue'),
            name: 'user-settings-password',
            meta: { title: '修改密码', icon: 'Lock', roles: ['USER'], requiresRealAuth: true }
          },
          {
            path: '/user/settings/phone',
            component: () => import('@/views/user/settings/Phone.vue'),
            name: 'user-settings-phone',
            meta: { title: '手机绑定', icon: 'Mobile', roles: ['USER'], requiresRealAuth: true }
          }
        ]
      },
      {
        path: 'merchant-list',
        name: 'merchant-list',
        component: MerchantList
      },
      {
        path: 'orders',
        name: 'order-list',
        component: OrderList,
        meta: { requiresRealAuth: true }
      },
      {
        path: 'merchant-apply',
        name: 'merchant-apply',
        component: MerchantApply,
        meta: { requiresRealAuth: true }
      },
      {
        path: 'merchant/:id',
        name: 'merchant-detail',
        component: MerchantDetail
      },
      {
        path: 'cart',
        name: 'cart',
        component: CartView,
        meta: { requiresRealAuth: true }
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: OrderDetail,
        meta: { requiresRealAuth: true }
      },
      {
        path: 'address',
        component: AddressManage,
        name: 'address-manage',
        meta: { requiresRealAuth: true },
        children: [
          {
            path: '',
            component: () => import('@/views/user/address/AddressList.vue'),
            name: 'address-list'
          },
          {
            path: 'add',
            component: () => import('@/views/user/address/AddressForm.vue'),
            name: 'address-add'
          },
          {
            path: 'edit/:id',
            component: () => import('@/views/user/address/AddressForm.vue'),
            name: 'address-edit'
          }
        ]
      },
      {
        path: 'orders/:id/review',
        name: 'order-review',
        component: ReviewCreate,
        meta: { requiresRealAuth: true }
      },
      {
        path: 'reviews',
        name: 'review-list',
        component: ReviewList,
        meta: { requiresRealAuth: true }
      },
      {
        path: '/user/coupon',
        name: 'coupon-center',
        component: CouponCenter,
        meta: {
          title: '领券中心',
          icon: 'Ticket',
          roles: ['USER'],
          requiresRealAuth: true
        },
        redirect: '/user/coupon/available',
        children: [
          {
            path: '/user/coupon/available',
            component: () => import('@/views/user/coupon/Available.vue'),
            name: 'coupon-available',
            meta: { title: '去领券', icon: 'Discount', roles: ['USER'], requiresRealAuth: true }
          },
          {
            path: '/user/coupon/my',
            component: () => import('@/views/user/coupon/MyCoupons.vue'),
            name: 'coupon-my',
            meta: { title: '我的卡包', icon: 'Wallet', roles: ['USER'], requiresRealAuth: true }
          },
          {
            path: '/user/coupon/activities',
            component: () => import('@/views/user/coupon/Activities.vue'),
            name: 'coupon-activities',
            meta: { title: '进行中的活动', icon: 'Calendar', roles: ['USER'], requiresRealAuth: true }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function getDefaultRouteByRole(role) {
  if (role === 'ADMIN') return '/admin/home'
  return '/user/home'
}

router.beforeEach(async (to, from, next) => {
  let token = getToken()
  const userStore = useUserStore()

  // ① 没有任何 Token：静默申请游客 Token，无感知进入页面
  if (!token) {
    try {
      const res = await getGuestToken()
      if (res.code === 200 && res.data) {
        userStore.setGuestToken(res.data)
        token = res.data
      }
    } catch (e) {
      // 游客 token 申请失败（网络问题等），降级跳登录页
      if (to.meta.requiresAuth) {
        next('/login')
        return
      }
    }
  }

  if (to.meta.requiresAuth) {
    // ② requiresRealAuth：游客不允许访问，跳转到提示页（而非登录页）
    if (to.meta.requiresRealAuth && userStore.isGuest) {
      next({ name: 'guest-notice', query: { from: to.fullPath } })
      return
    }

    // 游客 token 满足 requiresAuth，继续加载
    // 注意：isGuest 在 token 为空时也会返回 false，需额外判断 token 是否真实存在
    const currentToken = getToken()
    if (!userStore.isGuest && currentToken && !userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        userStore.logout()
        if (to.path !== '/login') {
          next('/login')
        } else {
          next()
        }
        return
      }
    }

    const userRole = userStore.userInfo?.role
    const requiredRole = to.meta.role

    // ADMIN 路由：必须是 ADMIN role
    if (requiredRole === 'ADMIN' && userRole !== 'ADMIN') {
      next(getDefaultRouteByRole(userRole))
      return
    }

    // MERCHANT 路由：游客跳转到提示页
    if (requiredRole === 'MERCHANT') {
      if (userStore.isGuest) {
        next({ name: 'guest-notice', query: { from: to.fullPath } })
        return
      }
      if (userRole === 'ADMIN') {
        next(getDefaultRouteByRole(userRole))
        return
      }
      if (userRole !== 'MERCHANT' && !userStore.hasApprovedMerchant) {
        next('/user/home')
        return
      }
    }

    // USER 路由：ADMIN 跳转到自己的首页
    if (requiredRole === 'USER' && userRole === 'ADMIN') {
      next(getDefaultRouteByRole(userRole))
      return
    }
  }

  // ③ 已登录（非游客）访问 /login：自动跳转到首页
    if (to.path === '/login' && token && !userStore.isGuest) {
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        // fetchUserInfo 失败（token 失效 / 403）：清理状态并留在登录页
        userStore.logout()
        next()
        return
      }
    }
    const role = userStore.userInfo?.role
    next(getDefaultRouteByRole(role))
    return
  }

  next()
})


export default router