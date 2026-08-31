const { get } = require('../../utils/request')

Page({
  data: {
    userInfo: null,
    stats: {}
  },

  onShow() {
    const userInfo = wx.getStorageSync('userInfo')
    this.setData({ userInfo })
    if (userInfo) this.loadStats()
  },

  async loadStats() {
    const orders = await get('/order', { page: 1, size: 50 })
    const list = orders.list || []
    this.setData({
      stats: {
        waitPay: list.filter(o => o.status === 0).length,
        waitShip: list.filter(o => o.status === 1).length,
        waitReceive: list.filter(o => o.status === 2).length,
        aftersale: list.filter(o => o.status === 5).length
      }
    })
  },

  onLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录？',
      success: res => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          this.setData({ userInfo: null, stats: {} })
          wx.showToast({ title: '已退出', icon: 'none' })
        }
      }
    })
  },

  go(e) {
    const url = e.currentTarget.dataset.url
    wx.navigateTo({ url })
  }
})