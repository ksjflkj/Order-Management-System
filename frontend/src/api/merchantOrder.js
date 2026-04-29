import request from './request'

export function getMerchantOrderList() {
    return request({
        url: '/api/merchant/order/list',
        method: 'get'
    })
}

export function getMerchantOrderDetail(id) {
    return request({
        url: `/api/merchant/order/${id}`,
        method: 'get'
    })
}

export function acceptMerchantOrder(id) {
    return request({
        url: `/api/merchant/order/${id}/accept`,
        method: 'post'
    })
}

export function completeMerchantOrder(id) {
    return request({
        url: `/api/merchant/order/${id}/complete`,
        method: 'post'
    })
}

export function agreeRefund(id) {
    return request({
        url: `/api/merchant/order/${id}/agree-refund`,
        method: 'post'
    })
}

export function rejectRefund(id, data) {
    return request({
        url: `/api/merchant/order/${id}/reject-refund`,
        method: 'post',
        data
    })
}