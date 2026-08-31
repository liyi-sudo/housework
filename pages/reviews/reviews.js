const { get, post } = require('../../utils/request')

Page({
  data: {
    list: [],
    showFollow: false,
    current: null,
    followContent: '',
    submitting: false
  },

  onShow() {
    this.load()
  },

  hasFollowUp(content) {
    return (content || '').indexOf('【追评】') !== -1
  },

  async load() {
    const list = await get('/review/mine')
    const enriched = (list || []).map(item => ({
      ...item,
      starText: '★'.repeat(item.rating || 1),
      followUp: this.hasFollowUp(item.content)
    }))
    this.setData({ list: enriched, showFollow: false })
  },

  openFollow(e) {
    const id = e.currentTarget.dataset.id
    const item = this.data.list.find(i => i.id === id)
    this.setData({ current: item, followContent: '', showFollow: true })
  },

  closeFollow() {
    this.setData({ showFollow: false, current: null })
  },

  stopTap() {
    // 阻止事件冒泡到遮罩层关闭弹窗
  },

  onFollowInput(e) {
    this.setData({ followContent: e.detail.value })
  },

  async submitFollow() {
    const text = this.data.followContent.trim()
    if (!text) {
      wx.showToast({ title: '请输入追评内容', icon: 'none' })
      return
    }
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      await post('/review', {
        orderItemId: this.data.current.orderItemId,
        rating: this.data.current.rating,
        content: text,
        images: '',
        followUp: true
      })
      wx.showToast({ title: '追评成功', icon: 'success' })
      this.load()
    } catch (e) {}
    this.setData({ submitting: false })
  }
})
