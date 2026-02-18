const { get } = require('../../utils/request')
const { checkLogin, logout: authLogout } = require('../../utils/auth')

Page({
  data: {
    userInfo: {}
  },

  onShow() {
    if (!checkLogin()) return

    this.fetchProfile()
  },

  fetchProfile() {
    get('/api/user/profile').then(res => {
      if (res && res.data) {
        this.setData({ userInfo: res.data })
        wx.setStorageSync('userInfo', res.data)
      }
    }).catch(err => {
      console.error('获取用户信息失败', err)
      // Fall back to cached data
      const cached = wx.getStorageSync('userInfo')
      if (cached) {
        this.setData({ userInfo: cached })
      }
    })
  },

  goPage(e) {
    const url = e.currentTarget.dataset.url
    wx.navigateTo({ url })
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          authLogout()
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
