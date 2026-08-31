const { post } = require('../../utils/request')

Page({
  data: {
    phone: '',
    password: '',
    confirm: '',
    nickname: '',
    loading: false
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value })
  },

  async doRegister() {
    const { phone, password, confirm, nickname } = this.data
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' })
      return
    }
    if (password.length < 6) {
      wx.showToast({ title: '密码至少 6 位', icon: 'none' })
      return
    }
    if (password !== confirm) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      await post('/auth/register', { phone, password, nickname })
      wx.showToast({ title: '注册成功' })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (e) {
    } finally {
      this.setData({ loading: false })
    }
  }
})