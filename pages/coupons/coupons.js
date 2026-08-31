const { get, post } = require('../../utils/request')

Page({
  data: {
    tab: 0,
    list: [],
    claimable: []
  },

  onShow() {
    this.load()
    this.loadClaimable()
  },

  async load() {
    let res
    try {
      res = await get('/coupon/mine', { status: this.data.tab })
    } catch (e) {
      return
    }
    this.setData({ list: res || [] })
  },

  async loadClaimable() {
    let res
    try {
      res = await get('/coupon/claimable')
    } catch (e) {
      return
    }
    this.setData({ claimable: res || [] })
  },

  async claim(e) {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const id = e.currentTarget.dataset.id
    const item = this.data.claimable.find(c => c.id === id)
    if (item && item.claimed) return
    try {
      await post('/coupon/' + id + '/claim')
      wx.showToast({ title: '领取成功', icon: 'success' })
      this.loadClaimable()
      this.load()
    } catch (e) {}
  },

  switchTab(e) {
    this.setData({ tab: Number(e.currentTarget.dataset.tab) }, () => this.load())
  }
})
