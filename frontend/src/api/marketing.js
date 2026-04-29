import request from "./request";

// 管理员优惠券接口
export function getCouponList(params) {
    return request({
        url: "/api/admin/coupon/page",
        method: "get",
        params
    });
}

export function createCoupon(data) {
    return request({
        url: "/api/admin/coupon",
        method: "post",
        data,
    });
}

export function updateCoupon(id, data) {
    return request({
        url: `/api/admin/coupon/${id}`,
        method: "put",
        data,
    });
}

export function deleteCoupon(id) {
    return request({
        url: `/api/admin/coupon/${id}`,
        method: "delete",
    });
}

// 管理员活动接口
export function getActivityList(params) {
    return request({
        url: "/api/admin/activity/page",
        method: "get",
        params
    });
}

export function createActivity(data) {
    return request({
        url: "/api/admin/activity",
        method: "post",
        data,
    });
}

export function updateActivity(id, data) {
    return request({
        url: `/api/admin/activity/${id}`,
        method: "put",
        data,
    });
}

export function deleteActivity(id) {
    return request({
        url: `/api/admin/activity/${id}`,
        method: "delete",
    });
}
