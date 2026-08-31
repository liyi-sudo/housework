const { get } = require('../../utils/request')

Page({
  data: {
    parents: [],
    goodsList: [],
    activeId: null,
    total: 0,
    page: 1,
    size: 10
  },

  async onLoad() {
    const categories = await get('/category/tree')
    const parents = categories.filter(c => c.parentId === 0)
    this.setData({ parents })
    if (parents.length) {
      this.setData({ activeId: parents[0].id })
      this.loadGoods()
    }
  },

  async loadGoods() {
    if (!this.data.activeId) return
    const res = await get('/goods', { categoryId: this.data.activeId, page: this.data.page, size: this.data.size })
    const list = this.data.page === 1 ? (res.list || []) : this.data.goodsList.concat(res.list || [])
    this.setData({ goodsList: list, total: res.total || 0 })
  },

  onSelect(e) {
    const id = e.currentTarget.dataset.id
    if (id === this.data.activeId) return
    this.setData({ activeId: id, page: 1 })
    this.loadGoods()
  },

  onGoodsTap(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  onLoadMore() {
    if (this.data.goodsList.length >= this.data.total) return
    this.setData({ page: this.data.page + 1 }, () => this.loadGoods())
  }
})
