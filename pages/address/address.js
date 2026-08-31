const { get, post, put, del } = require('../../utils/request')

Page({
  data: {
    list: [],
    showForm: false,
    editingId: null,
    form: {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      isDefault: 0
    }
  },

  onShow() {
    this.load()
  },

  async load() {
    const list = await get('/address')
    this.setData({ list })
  },

  openAdd() {
    this.setData({
      showForm: true,
      editingId: null,
      form: { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 }
    })
  },

  openEdit(e) {
    const item = this.data.list.find(a => a.id === e.currentTarget.dataset.id)
    if (!item) return
    this.setData({
      showForm: true,
      editingId: item.id,
      form: {
        receiverName: item.receiverName,
        receiverPhone: item.receiverPhone,
        province: item.province,
        city: item.city,
        district: item.district,
        detail: item.detail,
        isDefault: item.isDefault === 1 ? 1 : 0
      }
    })
  },

  hideForm() {
    this.setData({ showForm: false })
  },

  onField(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  toggleDefault(e) {
    this.setData({ 'form.isDefault': e.detail.value ? 1 : 0 })
  },

  async save() {
    const f = this.data.form
    if (!f.receiverName || !f.receiverPhone || !f.detail) {
      wx.showToast({ title: '请填写完整', icon: 'none' })
      return
    }
    if (this.data.editingId) {
      await put('/address', { ...f, id: this.data.editingId })
    } else {
      await post('/address', f)
    }
    wx.showToast({ title: '保存成功', icon: 'success' })
    this.setData({ showForm: false })
    this.load()
  },

  async remove(e) {
    const id = e.currentTarget.dataset.id
    const res = await wx.showModal({ title: '提示', content: '确定删除该地址？' })
    if (!res.confirm) return
    await del('/address/' + id)
    this.load()
  },

  async setDefault(e) {
    const id = e.currentTarget.dataset.id
    await put('/address/' + id + '/default')
    this.load()
  },

  stopProp() {}
})