import request from "./request";

// 获取可领取的优惠券
export function getAvailableCoupons(params) {
    return request({
        url: "/api/coupon/available",
        method: "get",
        params
    });
}

// 领取优惠券
export function receiveCoupon(id) {
    return request({
        url: `/api/coupon/receive/${id}`,
        method: "post",
    });
}

// 获取我的优惠券
export function getMyCoupons(params) {
    return request({
        url: "/api/coupon/my",
        method: "get",
        params
    });
}

// 获取进行中的活动
export function getActiveActivities(merchantId) {
    return request({
        url: "/api/activity/active",
        method: "get",
        params: { merchantId },
    });
}

// 计算优惠金额
export function calculateDiscount(amount, merchantId) {
    return request({
        url: "/api/activity/calculate",
        method: "get",
        params: { amount, merchantId },
    });
}
