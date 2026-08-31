import { defineStore } from 'pinia'
import { login as apiLogin, getUserInfo, logout as apiLogout } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isLogin: state => !!state.token
  },
  actions: {
    async login(phone, password, code) {
      const data = await apiLogin({ phone, password, client: 'WEB', code })
      this.token = data.token
      this.userInfo = data.user
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data.user))
    },
    async fetchInfo() {
      this.userInfo = await getUserInfo()
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    async logout() {
      try {
        await apiLogout()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})