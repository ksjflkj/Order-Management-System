import request from './request'

export function ping() {
  return request({
    url: '/api/auth/ping',
    method: 'get'
  })
}

/**
 * 获取游客 Token（无需登录，有效期 2 小时）
 */
export function getGuestToken() {
  return request({
    url: '/api/auth/guest',
    method: 'post'
  })
}

/**
 * 注册时发送手机验证码（无需登录）
 */
export function sendRegisterCode(phone) {
  return request({
    url: '/api/auth/send-code',
    method: 'post',
    data: { phone }
  })
}

export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

/**
 * 找回密码：向已注册手机号发送验证码
 */
export function sendResetCode(phone) {
  return request({
    url: '/api/auth/reset-send-code',
    method: 'post',
    data: { phone }
  })
}

/**
 * 找回密码：通过手机验证码设置新密码
 */
export function resetPassword(data) {
  return request({
    url: '/api/auth/reset-password',
    method: 'post',
    data
  })
}