const { get, post } = require('../../utils/request')

Page({
  data: {
    orderId: null,
    types: ['退货退款', '退款', '换货'],
    typeIndex: 0,
    reason: '',
    submitting: false
  },

  onLoad(options) {
    this.setData({ orderId: options.orderId })
  },

  onTypeChange(e) {
    this.setData({ typeIndex: Number(e.detail.value) })
  },

  onReason(e) {
    this.setData({ reason: e.detail.value })
  },

  async submit() {
    if (!this.data.reason.trim()) {
      wx.showToast({ title: '请填写申请原因', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await post('/aftersale', {
        orderId: Number(this.data.orderId),
        type: this.data.typeIndex + 1,
        reason: this.data.reason
      })
      wx.showToast({ title: '申请成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (e) {
    } finally {
      this.setData({ submitting: false })
    }
  }
})