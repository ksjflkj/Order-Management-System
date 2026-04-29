import request from './request'

/**
 * 分页查询用户列表（管理员）
 */
export function getAdminUserPage(params) {
    return request({
        url: '/api/admin/users/page',
        method: 'get',
        params
    })
}

/**
 * 禁用 / 启用用户
 * @param {number} id 用户ID
 * @param {number} status 0=正常 1=禁用
 */
export function updateUserStatus(id, status) {
    return request({
        url: `/api/admin/users/${id}/status`,
        method: 'post',
        params: { status }
    })
}
