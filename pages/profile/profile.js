const { get, put } = require('../../utils/request')

Page({
  data: {
    nickname: '',
    gender: 0,
    phone: '',
    avatar: '',
    genders: [
      { value: 0, label: '保密' },
      { value: 1, label: '男' },
      { value: 2, label: '女' }
    ]
  },

  onShow() {
    this.loadInfo()
  },

  async loadInfo() {
    try {
      const info = await get('/user/info')
      this.setData({
        nickname: info.nickname || '',
        gender: info.gender || 0,
        phone: info.phone || '',
        avatar: info.avatar || ''
      })
    } catch (e) {}
  },

  onNickname(e) {
    this.setData({ nickname: e.detail.value })
  },

  selectGender(e) {
    this.setData({ gender: Number(e.currentTarget.dataset.value) })
  },

  async save() {
    if (!this.data.nickname.trim()) {
      wx.showToast({ title: '昵称不能为空', icon: 'none' })
      return
    }
    try {
      await put('/user/info', {
        nickname: this.data.nickname,
        gender: this.data.gender,
        avatar: this.data.avatar
      })
      const userInfo = wx.getStorageSync('userInfo') || {}
      userInfo.nickname = this.data.nickname
      userInfo.gender = this.data.gender
      wx.setStorageSync('userInfo', userInfo)
      wx.showToast({ title: '保存成功', icon: 'success' })
    } catch (e) {}
  },

  goPassword() {
    wx.navigateTo({ url: '/pages/profile/password' })
  }
})
