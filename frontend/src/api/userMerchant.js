import request from './request'

export function getUserMerchantPage(params) {
    return request({
        url: '/api/user/merchant/page',
        method: 'get',
        params
    })
}

export function getUserMerchantDetail(id) {
    return request({
        url: `/api/user/merchant/${id}`,
        method: 'get'
    })
}

export function getUserMerchantDishList(id) {
    return request({
        url: `/api/user/merchant/${id}/dish/list`,
        method: 'get'
    })
}