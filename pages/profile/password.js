const { put } = require('../../utils/request')

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirm: '',
    submitting: false
  },

  onOld(e) {
    this.setData({ oldPassword: e.detail.value })
  },
  onNew(e) {
    this.setData({ newPassword: e.detail.value })
  },
  onConfirm(e) {
    this.setData({ confirm: e.detail.value })
  },

  async save() {
    if (!this.data.oldPassword) {
      wx.showToast({ title: '请输入原密码', icon: 'none' })
      return
    }
    if (this.data.newPassword.length < 6) {
      wx.showToast({ title: '新密码至少 6 位', icon: 'none' })
      return
    }
    if (this.data.newPassword !== this.data.confirm) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      await put('/user/password', {
        oldPassword: this.data.oldPassword,
        newPassword: this.data.newPassword
      })
      wx.showModal({
        title: '提示',
        content: '密码修改成功，请重新登录',
        showCancel: false,
        success: () => {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.navigateTo({ url: '/pages/login/login' })
        }
      })
    } catch (e) {}
    this.setData({ submitting: false })
  }
})
