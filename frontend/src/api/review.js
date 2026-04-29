import request from './request'

export function saveReview(data) {
    return request({
        url: '/api/review/save',
        method: 'post',
        data
    })
}

export function getMyReviews(params) {
    return request({
        url: '/api/review/my',
        method: 'get',
        params
    })
}

export function getMerchantReviews(merchantId) {
    return request({
        url: `/api/review/merchant/${merchantId}`,
        method: 'get'
    })
}

/** 用户追评 */
export function followUpReview(reviewId, content) {
    return request({
        url: `/api/review/follow-up/${reviewId}`,
        method: 'post',
        data: { content }
    })
}

/** 商家查看评价 */
export function getMerchantReviewList(merchantId) {
    return request({
        url: `/api/merchant/review/list/${merchantId}`,
        method: 'get'
    })
}

/** 商家回复评价 */
export function merchantReplyReview(reviewId, reply) {
    return request({
        url: `/api/merchant/review/reply/${reviewId}`,
        method: 'post',
        data: { reply }
    })
}

/** 管理员查询所有评价 */
export function adminListReviews(params) {
    return request({
        url: '/api/admin/review/page',
        method: 'get',
        params
    })
}

/** 管理员屏蔽/取消屏蔽评价 */
export function adminBlockReview(reviewId, status) {
    return request({
        url: `/api/admin/review/block/${reviewId}`,
        method: 'post',
        data: { status }
    })
}