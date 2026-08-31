const { post } = require('../../utils/request')

Page({
  data: {
    mode: 'password',
    phone: '',
    password: '',
    code: '',
    countdown: 0,
    loading: false
  },
  timer: null,

  onUnload() {
    clearInterval(this.timer)
  },

  switchMode(e) {
    const mode = e.currentTarget.dataset.mode
    if (mode === this.data.mode) return
    this.setData({ mode, error: '' })
  },

  onPhone(e) {
    this.setData({ phone: e.detail.value })
  },
  onPassword(e) {
    this.setData({ password: e.detail.value })
  },
  onCode(e) {
    this.setData({ code: e.detail.value })
  },

  isPhone() {
    return /^1\d{10}$/.test(this.data.phone)
  },

  async sendCode() {
    if (!this.isPhone()) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (this.data.countdown > 0) return
    try {
      const res = await post('/auth/sms-code', { phone: this.data.phone })
      wx.showModal({
        title: '模拟验证码',
        content: '验证码：' + res + '（演示环境，直接填写即可）',
        showCancel: false
      })
      this.setData({ countdown: 60 })
      clearInterval(this.timer)
      this.timer = setInterval(() => {
        if (this.data.countdown <= 1) {
          clearInterval(this.timer)
          this.setData({ countdown: 0 })
          return
        }
        this.setData({ countdown: this.data.countdown - 1 })
      }, 1000)
    } catch (e) {}
  },

  async doLogin() {
    if (!this.isPhone()) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (this.data.mode === 'password' && !this.data.password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }
    if (this.data.mode === 'code' && !this.data.code) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
    if (this.data.loading) return
    this.setData({ loading: true })
    try {
      const data = await post('/auth/login', {
        phone: this.data.phone,
        password: this.data.mode === 'password' ? this.data.password : '',
        code: this.data.mode === 'code' ? this.data.code : '',
        client: 'MINI'
      })
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('userInfo', data.user)
      wx.showToast({ title: '登录成功' })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (e) {
    } finally {
      this.setData({ loading: false })
    }
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
