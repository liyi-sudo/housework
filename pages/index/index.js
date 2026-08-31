const { get } = require('../../utils/request')

Page({
  data: {
    banners: [],
    notices: [],
    categories: [],
    goodsList: [],
    current: 0,
    noticeIdx: 0,
    showNotice: true
  },
  timer: null,
  _noticeTimer: null,
  _noticeAnimating: false,

  onShow() {
    this.loadData()
  },
  onHide() {
    clearInterval(this.timer)
    clearTimeout(this._noticeTimer)
    this._noticeAnimating = false
  },

  async loadData() {
    try {
      const [banners, notices, categories, goods] = await Promise.all([
        get('/banner'),
        get('/notice'),
        get('/category/tree'),
        get('/goods', { page: 1, size: 10 })
      ])
      const parents = (categories || []).filter(c => c.parentId === 0)
      this.setData({ banners: banners || [], notices: notices || [], categories: parents, goodsList: goods.list || [], noticeIdx: 0, showNotice: true })
      clearInterval(this.timer)
      if (this.data.banners.length > 1 || this.data.notices.length > 1) {
        this.timer = setInterval(() => {
          if (this.data.banners.length > 1) {
            this.setData({ current: (this.data.current + 1) % this.data.banners.length })
          }
          if (this.data.notices.length > 1) {
            this.advanceNotice()
          }
        }, 4000)
      }
    } catch (e) {}
  },

  advanceNotice() {
    const total = this.data.notices.length
    if (total <= 1 || this._noticeAnimating) return
    this._noticeAnimating = true
    this.setData({ showNotice: false })
    const next = (this.data.noticeIdx + 1) % total
    this._noticeTimer = setTimeout(() => {
      this.setData({ noticeIdx: next, showNotice: true })
      this._noticeAnimating = false
    }, 500)
  },

  onNoticeTap() {
    this.advanceNotice()
  },

  onBannerTap(e) {
    const link = e.currentTarget.dataset.link
    if (link) wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + link })
  },

  onCategoryTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/goods-list/goods-list?categoryId=' + id })
  },

  goCoupons() {
    wx.navigateTo({ url: '/pages/coupons/coupons' })
  },

  onGoodsTap(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  onSearchInput(e) {
    this.searchWord = e.detail.value
  },

  onSearch() {
    wx.navigateTo({ url: '/pages/goods-list/goods-list?keyword=' + (this.searchWord || '') })
  }
})