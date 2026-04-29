import request from './request'

export function createOrder(data) {
    return request({
        url: '/api/order/create',
        method: 'post',
        data
    })
}

export function getOrderList(params) {
    return request({
        url: '/api/order/my',
        method: 'get',
        params
    })
}

export function getOrderDetail(id) {
    return request({
        url: `/api/order/${id}`,
        method: 'get'
    })
}

export function cancelOrder(id) {
    return request({
        url: `/api/order/${id}/cancel`,
        method: 'post'
    })
}

export function payOrder(id) {
    return request({
        url: `/api/order/${id}/pay`,
        method: 'post'
    })
}

export function confirmReceived(id) {
    return request({
        url: `/api/order/${id}/confirm`,
        method: 'post'
    })
}

export function applyRefund(id, data) {
    return request({
        url: `/api/order/${id}/refund`,
        method: 'post',
        data
    })
}

export function escalateToArbitration(id) {
    return request({
        url: `/api/order/${id}/escalate`,
        method: 'post'
    })
}
