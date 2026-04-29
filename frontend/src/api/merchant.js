import request from './request'

export function applyMerchant(data) {
    return request({
        url: '/api/merchant/apply',
        method: 'post',
        data
    })
}

/**
 * 获取当前用户审核通过的店铺（USER role 也可调用）
 */
export function getMyMerchants() {
    return request({
        url: '/api/merchant/my',
        method: 'get'
    })
}

/**
 * 获取当前用户所有申请记录（包括待审核 / 已拒绝）
 */
export function getAllMyMerchants() {
    return request({
        url: '/api/merchant/my/all',
        method: 'get'
    })
}

export function updateMyMerchant(data) {
    return request({
        url: '/api/merchant/dish/update-merchant',
        method: 'put',
        data
    })
}

/**
 * 切换店铺营业状态
 * @param {number} merchantId 店铺ID
 * @param {boolean} open true=开始营业 false=打烊
 */
export function toggleShopOpen(merchantId, open) {
    return request({
        url: `/api/merchant/dish/${merchantId}/toggle-open`,
        method: 'post',
        params: { open }
    })
}