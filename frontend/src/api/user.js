import request from './request'

export function getCurrentUser() {
  return request({
    url: '/api/user/me',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/api/user/update',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/api/user/change-password',
    method: 'post',
    data
  })
}

export function sendPhoneCode(phone) {
  return request({
    url: '/api/user/send-code',
    method: 'post',
    data: { phone }
  })
}

export function updatePhone(data) {
  return request({
    url: '/api/user/update-phone',
    method: 'post',
    data
  })
}

export function uploadAvatar(formData) {
  return request({
    url: '/api/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}