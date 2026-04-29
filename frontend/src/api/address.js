import request from './request'

export function saveAddress(data) {
    return request({
        url: '/api/address/save',
        method: 'post',
        data
    })
}

export function getAddressList(params) {
    return request({
        url: '/api/address/my',
        method: 'get',
        params
    })
}

export function deleteAddress(id) {
    return request({
        url: `/api/address/${id}`,
        method: 'delete'
    })
}