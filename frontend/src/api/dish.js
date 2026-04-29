import request from './request'

export function getDishPage(params) {
    return request({
        url: '/api/merchant/dish/page',
        method: 'get',
        params
    })
}

export function saveDish(data) {
    return request({
        url: '/api/merchant/dish/save',
        method: 'post',
        data
    })
}

export function updateDishStatus(data) {
    return request({
        url: '/api/merchant/dish/status',
        method: 'post',
        data
    })
}

export function getMyMerchants() {
    return request({
        url: '/api/merchant/dish/merchants',
        method: 'get'
    })
}

export function getDishCategoryList(params) {
    return request({
        url: '/api/merchant/dish-category/list',
        method: 'get',
        params
    })
}

export function saveDishCategory(data) {
    return request({
        url: '/api/merchant/dish-category/save',
        method: 'post',
        data
    })
}

export function deleteDishCategory(id) {
    return request({
        url: `/api/merchant/dish-category/${id}`,
        method: 'delete'
    })
}