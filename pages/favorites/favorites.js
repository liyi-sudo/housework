const { get, post, del } = require('../../utils/request')

Page({
  data: {
    list: [],
    auth: false
  },

  onShow() {
    const token = wx.getStorageSync('token')
    if (!token) {
      this.setData({ auth: false })
      return
    }
    this.setData({ auth: true })
    this.load()
  },

  async load() {
    const res = await get('/favorites', { page: 1, size: 50 })
    this.setData({ list: res.list || [] })
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  async unfav(e) {
    const id = e.currentTarget.dataset.id
    await del('/favorites/' + id)
    wx.showToast({ title: '已取消收藏', icon: 'success' })
    this.load()
  }
})