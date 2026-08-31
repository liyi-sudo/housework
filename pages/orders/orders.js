const { get, post } = require('../../utils/request')

Page({
  data: {
    tabs: [
      { label: '全部', value: null },
      { label: '待付款', value: 0 },
      { label: '待发货', value: 1 },
      { label: '待收货', value: 2 },
      { label: '已完成', value: 3 },
      { label: '已取消', value: 4 },
      { label: '售后', value: 5 }
    ],
    active: null,
    list: [],
    page: 1,
    total: 0
  },

  onLoad(options) {
    if (options.status !== undefined) {
      this.setData({ active: Number(options.status) })
    }
  },

  onShow() {
    this.data.page = 1
    this.load()
  },

  async load() {
    try {
      const res = await get('/order', { status: this.data.active, page: this.data.page, size: 10 })
      const list = (res.list || []).map(o => ({ ...o, logisticsText: this.logisticsText(o.status) }))
      this.setData({
        list: this.data.page === 1 ? list : this.data.list.concat(list),
        total: res.total || 0
      })
    } catch (e) {}
  },

  logisticsText(status) {
    switch (status) {
      case 0: return '等待付款'
      case 1: return '商家备货中，等待发货'
      case 2: return '商品运输中，请等待收货'
      case 3: return '物流已完成，交易结束'
      case 4: return '订单已取消'
      case 5: return '售后退换处理中'
      default: return '订单处理中'
    }
  },

  switchTab(e) {
    let value = e.currentTarget.dataset.value
    if (value === 'null' || value === 'undefined' || value === '' || value === null || value === undefined) {
      value = null
    } else {
      value = Number(value)
    }
    this.setData({ active: value, page: 1 }, () => this.load())
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/order-detail/order-detail?id=' + e.currentTarget.dataset.id })
  },

  noop() {},

  async pay(e) {
    const { id } = e.currentTarget.dataset
    const res = await wx.showModal({ title: '提示', content: '确认使用模拟余额支付？' })
    if (!res.confirm) return
    try {
      await post('/order/' + id + '/pay')
      wx.showToast({ title: '支付成功', icon: 'success' })
      this.load()
    } catch (e) {}
  },

  async cancel(e) {
    const { id } = e.currentTarget.dataset
    const res = await wx.showModal({ title: '提示', content: '确定取消订单？' })
    if (!res.confirm) return
    await post('/order/' + id + '/cancel')
    this.load()
  },

  async confirm(e) {
    const { id } = e.currentTarget.dataset
    const res = await wx.showModal({ title: '提示', content: '确认已收到货？' })
    if (!res.confirm) return
    await post('/order/' + id + '/confirm')
    this.load()
  },

  onReachBottom() {
    if (this.data.list.length >= this.data.total) return
    this.setData({ page: this.data.page + 1 }, () => this.load())
  }
})