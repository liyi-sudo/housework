const { get, post, put } = require('../../utils/request')

Page({
  data: {
    goods: null,
    imagesArray: [],
    skuList: [],
    selectedSku: null,
    quantity: 1,
    reviews: [],
    isFav: false,
    sheetVisible: false,
    intent: 'cart',
    reviewVisible: false,
    reviewRating: 5,
    reviewContent: '',
    reviewTarget: null
  },

  async onLoad(options) {
    const id = options.id
    const goods = await get('/goods/' + id)
    const skuList = goods.skuList || []
    this.setData({
      goods,
      imagesArray: goods.mainImage ? [goods.mainImage] : [],
      skuList,
      selectedSku: skuList.length ? skuList[0] : null
    })
    this.loadReviews()
    const token = wx.getStorageSync('token')
    if (token) {
      try {
        this.setData({ isFav: await get('/favorites/' + id + '/status') })
        post('/goods/' + id + '/browse')
      } catch (e) {}
    }
  },

  async loadReviews() {
    const res = await get('/goods/' + this.data.goods.id + '/reviews', { page: 1, size: 10 })
    const reviews = (res.list || []).map(r => ({ ...r, starText: '★'.repeat(r.rating || 1) }))
    this.setData({ reviews })
  },

  selectSku(e) {
    const id = e.currentTarget.dataset.id
    const sku = this.data.skuList.find(s => s.id === id)
    if (sku) this.setData({ selectedSku: sku })
  },

  changeQty(e) {
    const delta = Number(e.currentTarget.dataset.delta)
    const quantity = Math.max(1, this.data.quantity + delta)
    this.setData({ quantity })
  },

  showSheet(e) {
    if (!this.data.selectedSku) {
      wx.showToast({ title: '请选择规格', icon: 'none' })
      return
    }
    const intent = e.currentTarget.dataset.mode || 'cart'
    this.setData({ sheetVisible: true, intent })
  },

  hideSheet() {
    this.setData({ sheetVisible: false })
  },

  confirmSheet() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    post('/cart', { skuId: this.data.selectedSku.id, quantity: this.data.quantity }).then(() => {
      if (this.data.intent === 'buy') {
        this.buyNow()
      } else {
        this.setData({ sheetVisible: false })
        wx.showToast({ title: '已加入购物车', icon: 'success' })
      }
    })
  },

  async buyNow() {
    const cart = await get('/cart')
    const item = cart.find(i => i.skuId === this.data.selectedSku.id)
    if (!item) {
      this.setData({ sheetVisible: false })
      wx.showToast({ title: '加入购物车失败', icon: 'none' })
      return
    }
    if (item.checked !== 1) {
      await put('/cart/check', { checked: 1, ids: [item.id] })
    }
    this.setData({ sheetVisible: false })
    wx.navigateTo({ url: '/pages/checkout/checkout?ids=' + item.id })
  },

  async openReview() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const target = await get('/review/goods/' + this.data.goods.id + '/target')
    if (!target || !target.canReview) {
      wx.showToast({ title: target && target.alreadyReviewed ? '您已评价过该商品' : '请先完成该商品的订单后再评价', icon: 'none' })
      return
    }
    this.setData({ reviewTarget: target, reviewRating: 5, reviewContent: '', reviewVisible: true })
  },

  closeReview() {
    this.setData({ reviewVisible: false })
  },

  setRating(e) {
    this.setData({ reviewRating: Number(e.currentTarget.dataset.value) })
  },

  onReviewContent(e) {
    this.setData({ reviewContent: e.detail.value })
  },

  async submitReview() {
    if (!this.data.reviewContent.trim()) {
      wx.showToast({ title: '请填写评价内容', icon: 'none' })
      return
    }
    await post('/review', {
      orderId: this.data.reviewTarget.orderId,
      orderItemId: this.data.reviewTarget.orderItemId,
      rating: this.data.reviewRating,
      content: this.data.reviewContent,
      images: ''
    })
    this.setData({ reviewVisible: false })
    wx.showToast({ title: '评价成功', icon: 'success' })
    this.loadReviews()
  },

  stopProp() {},

  async toggleFav() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    if (this.data.isFav) {
      const { del } = require('../../utils/request')
      await del('/favorites/' + this.data.goods.id)
      this.setData({ isFav: false })
    } else {
      await post('/favorites/' + this.data.goods.id)
      this.setData({ isFav: true })
    }
  }
})