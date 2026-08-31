const { get, post } = require('../../utils/request')

Page({
  data: {
    order: null,
    logistics: null
  },

  async onLoad(options) {
    const id = options.id
    const order = await get('/order/' + id)
    this.setData({ order })

    if (order.status === 2) {
      try {
        const logistics = await get('/order/' + id + '/logistics')
        this.setData({
          logistics: {
            ...logistics,
            traces: (logistics.trace || '').split(';').filter(Boolean)
          }
        })
      } catch (e) {}
    }
  },

  async pay() {
    const res = await wx.showModal({ title: '提示', content: '确认使用模拟余额支付？' })
    if (!res.confirm) return
    try {
      await post('/order/' + this.data.order.id + '/pay')
      wx.showToast({ title: '支付成功', icon: 'success' })
      setTimeout(() => this.onLoad({ id: this.data.order.id }), 600)
    } catch (e) {}
  },

  async cancel() {
    const res = await wx.showModal({ title: '提示', content: '确定取消订单？' })
    if (!res.confirm) return
    await post('/order/' + this.data.order.id + '/cancel')
    wx.showToast({ title: '已取消', icon: 'success' })
    setTimeout(() => this.onLoad({ id: this.data.order.id }), 600)
  },

  async confirm() {
    const res = await wx.showModal({ title: '提示', content: '确认已收到货' })
    if (!res.confirm) return
    await post('/order/' + this.data.order.id + '/confirm')
    wx.showToast({ title: '已确认', icon: 'success' })
    setTimeout(() => this.onLoad({ id: this.data.order.id }), 600)
  },

  goAftersale() {
    wx.navigateTo({ url: '/pages/aftersale/aftersale?orderId=' + this.data.order.id })
  }
})