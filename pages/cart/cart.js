const { get, post, put, del } = require('../../utils/request')

Page({
  data: {
    items: [],
    total: 0,
    allChecked: false
  },

  onShow() {
    this.load()
  },

  async load() {
    const list = await get('/cart')
    const total = list.filter(i => i.checked === 1).reduce((s, i) => s + i.price * i.quantity, 0)
    const valid = list.filter(i => i.goodsStatus === 1)
    this.setData({
      items: list,
      total: total.toFixed(2),
      allChecked: valid.length > 0 && valid.every(i => i.checked === 1)
    })
  },

  async toggle(e) {
    const { id, checked } = e.currentTarget.dataset
    await put('/cart/check', { checked: checked ? 0 : 1, ids: [id] })
    this.recalc(id)
  },

  async toggleAll() {
    const target = this.data.allChecked ? 0 : 1
    await put('/cart/check', { checked: target, ids: [] })
    this.setData({ allChecked: !this.data.allChecked })
    this.recalc()
  },

  recalc(onlyId) {
    let items = this.data.items
    if (onlyId) {
      items = items.map(i => (i.id === onlyId ? { ...i, checked: i.checked === 1 ? 0 : 1 } : i))
    } else {
      items = items.map(i => ({ ...i, checked: this.data.allChecked ? 1 : 0 }))
    }
    const total = items.filter(i => i.checked === 1).reduce((s, i) => s + i.price * i.quantity, 0)
    this.setData({ items, total: total.toFixed(2) })
  },

  async changeQty(e) {
    const { id, delta } = e.currentTarget.dataset
    const item = this.data.items.find(i => i.id === id)
    const quantity = Math.max(1, item.quantity + Number(delta))
    if (quantity > item.stock) {
      wx.showToast({ title: '库存不足', icon: 'none' })
      return
    }
    await put('/cart/' + id + '/quantity', { quantity })
    const items = this.data.items.map(i => (i.id === id ? { ...i, quantity } : i))
    const total = items.filter(i => i.checked === 1).reduce((s, i) => s + i.price * i.quantity, 0)
    this.setData({ items, total: total.toFixed(2) })
  },

  async onDelete(e) {
    const { id } = e.currentTarget.dataset
    const res = await wx.showModal({ title: '提示', content: '确定删除该商品？' })
    if (!res.confirm) return
    await del('/cart/' + id)
    this.load()
  },

  onCheckout() {
    const ids = this.data.items.filter(i => i.checked === 1 && i.goodsStatus === 1).map(i => i.id)
    if (!ids.length) {
      wx.showToast({ title: '请先选择商品', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/checkout/checkout?ids=' + ids.join(',') })
  }
})