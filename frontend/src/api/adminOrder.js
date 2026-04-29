import request from './request'

export function getAdminOrderList(params) {
    return request({
        url: '/api/admin/order/page',
        method: 'get',
        params
    })
}

export function getAdminOrderDetail(id) {
    return request({
        url: `/api/admin/order/${id}`,
        method: 'get'
    })
}

export function resolveArbitration(id, data) {
    return request({
        url: `/api/admin/order/${id}/arbitration`,
        method: 'post',
        data
    })
}