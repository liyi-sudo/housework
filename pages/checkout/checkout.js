const { get, post } = require('../../utils/request')

Page({
  data: {
    items: [],
    addresses: [],
    selectedAddress: null,
    coupons: [],
    selectedCoupon: null,
    remark: '',
    submitting: false
  },

  async onLoad(options) {
    const ids = (options.ids || '').split(',').filter(Boolean).map(Number)
    this.setData({ ids })
    const [cart] = await Promise.all([get('/cart')])
    const items = cart.filter(i => ids.includes(i.id) && i.checked === 1)
    let coupons = []
    try {
      coupons = await get('/coupon/mine', { status: 0 })
    } catch (e) {}
    this.setData({ items, coupons }, () => this.recalc())
    await this.loadAddresses()
  },

  onShow() {
    if (this.data.ids) this.loadAddresses()
  },

  async loadAddresses() {
    try {
      const addresses = await get('/address')
      let selectedAddress = this.data.selectedAddress
      if (!selectedAddress) {
        selectedAddress = addresses.find(a => a.isDefault === 1) || addresses[0] || null
      }
      this.setData({ addresses, selectedAddress })
    } catch (e) {}
  },

  goAddress() {
    wx.navigateTo({ url: '/pages/address/address' })
  },

  recalc() {
    const total = this.data.items.reduce((s, i) => s + i.price * i.quantity, 0)
    let discount = 0
    if (this.data.selectedCoupon && total >= this.data.selectedCoupon.threshold) {
      discount = Math.min(this.data.selectedCoupon.amount, total)
    }
    const afterDiscount = total - discount
    const freight = afterDiscount >= 99 ? 0 : 8
    this.setData({
      totalAmount: total.toFixed(2),
      discountAmount: discount.toFixed(2),
      freightAmount: freight.toFixed(2),
      payAmount: (afterDiscount + freight).toFixed(2)
    })
  },

  selectAddress(e) {
    this.setData({ selectedAddress: this.data.addresses.find(a => a.id === e.currentTarget.dataset.id) })
  },

  selectCoupon(e) {
    this.setData({ selectedCoupon: this.data.coupons.find(c => c.id === e.currentTarget.dataset.id) }, () => this.recalc())
  },

  onRemark(e) {
    this.setData({ remark: e.detail.value })
  },

  async submit() {
    if (!this.data.selectedAddress) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await post('/order/submit', {
        addressId: this.data.selectedAddress.id,
        cartIds: this.data.items.map(i => i.id),
        userCouponId: this.data.selectedCoupon ? this.data.selectedCoupon.id : null,
        remark: this.data.remark
      })
      wx.showToast({ title: '下单成功', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/orders/orders?status=0' })
      }, 800)
    } catch (e) {
    } finally {
      this.setData({ submitting: false })
    }
  }
})