import request from './request'

export function addToCart(data) {
    return request({
        url: '/api/cart/add',
        method: 'post',
        data
    })
}

export function getCartList() {
    return request({
        url: '/api/cart/list',
        method: 'get'
    })
}

export function updateCart(data) {
    return request({
        url: '/api/cart/update',
        method: 'post',
        data
    })
}

export function deleteCartItem(id) {
    return request({
        url: `/api/cart/${id}`,
        method: 'delete'
    })
}

export function clearCart() {
    return request({
        url: '/api/cart/clear',
        method: 'delete'
    })
}