import axios from 'axios'
import { getToken } from '@/utils/auth'

// 生产环境（Docker）由 Nginx 反向代理 /api/ → 后端:8080
// 本地开发直连后端（import.meta.env.DEV 为 true 时）
const baseURL = import.meta.env.DEV ? 'http://localhost:8080' : '/api'

const service = axios.create({
  baseURL,
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    // 后端统一返回 { code, message, data }，code !== 200 视为业务失败
    if (res && typeof res === 'object' && 'code' in res && res.code !== 200) {
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  async error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '请求失败'

    // 账号被禁用：403 + 含"禁用"关键词 → 弹窗提示后强制退出
    if (status === 403 && message.includes('禁用')) {
      const { ElMessageBox } = await import('element-plus')
      const { useUserStore } = await import('@/stores/user')

      try {
        await ElMessageBox.alert(
          message,
          '账号已禁用',
          {
            type: 'error',
            confirmButtonText: '知道了',
            showClose: false
          }
        )
      } finally {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(message))
    }

    console.error('请求错误:', error)
    return Promise.reject(new Error(message))
  }
)

export default service