import request from '@/api/request'

export function getMerchantStatistics() {
    return request({
        url: '/api/merchant/statistics',
        method: 'get'
    })
}
