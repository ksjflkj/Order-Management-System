import { defineStore } from 'pinia'
import { getCurrentUser } from '@/api/user'
import { getMyMerchants } from '@/api/merchant'
import { getToken, removeToken, setToken } from '@/utils/auth'

/** 解析 JWT payload，无需验签 */
function parseJwtPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64))
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    // 当前用户审核通过的店铺列表（用于判断是否拥有商家身份）
    myMerchants: []
  }),

  getters: {
    // 是否拥有至少一个已审核通过的店铺
    hasApprovedMerchant: (state) => state.myMerchants.length > 0,

    // 是否为游客身份（解析 token 中的 role）
    isGuest: () => {
      const token = getToken()
      if (!token) return false
      const payload = parseJwtPayload(token)
      return payload?.role === 'GUEST'
    }
  },

  actions: {
    async fetchUserInfo() {
      const res = await getCurrentUser()
      if (res.code === 200) {
        this.userInfo = res.data
        // 拉取用户信息后，同步获取关联的商家店铺
        await this.fetchMyMerchants()
      }
      return res
    },

    async fetchMyMerchants() {
      try {
        const res = await getMyMerchants()
        if (res.code === 200) {
          this.myMerchants = res.data || []
        }
      } catch (e) {
        this.myMerchants = []
      }
    },

    /** 设置游客 token，不拉取用户信息 */
    setGuestToken(token) {
      setToken(token)
      this.userInfo = null
      this.myMerchants = []
    },

    logout() {
      this.userInfo = null
      this.myMerchants = []
      removeToken()
    }
  }
})