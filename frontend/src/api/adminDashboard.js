import request from './request'

export function getAdminDashboard() {
    return request({
        url: '/api/admin/dashboard',
        method: 'get'
    })
}