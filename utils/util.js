function fmt(price) {
  return Number(price).toFixed(2)
}

function toast(title, icon = 'none') {
  wx.showToast({ title, icon })
}

function confirm(content) {
  return new Promise(resolve => {
    wx.showModal({
      title: '提示',
      content,
      success(res) {
        resolve(res.confirm)
      }
    })
  })
}

module.exports = { fmt, toast, confirm }