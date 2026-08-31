const { get } = require('../../utils/request')

Page({
  data: {
    keyword: '',
    categoryId: null,
    sort: 'default',
    list: [],
    page: 1,
    total: 0,
    size: 10,
    sorts: [
      { key: 'default', label: '综合' },
      { key: 'sales', label: '销量' },
      { key: 'price_asc', label: '价格↑' },
      { key: 'price_desc', label: '价格↓' }
    ]
  },

  onLoad(options) {
    const query = { categoryId: options.categoryId || null, keyword: options.keyword || '' }
    this.setData(query)
    this.load()
  },

  async load() {
    const { keyword, categoryId, sort, page, size } = this.data
    const res = await get('/goods', { keyword, categoryId, sort, page, size })
    this.setData({
      list: page === 1 ? (res.list || []) : this.data.list.concat(res.list || []),
      total: res.total || 0,
      pageIsFirst: page === 1
    })
  },

  changeSort(e) {
    this.setData({ sort: e.currentTarget.dataset.key, page: 1 }, () => this.load())
  },

  onGoodsTap(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  onReachBottom() {
    if (this.data.list.length >= this.data.total) {
      return
    }
    this.setData({ page: this.data.page + 1 }, () => this.load())
  }
})