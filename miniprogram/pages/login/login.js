const { post } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    username: '',
    password: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value.trim() })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onLogin() {
    const { username, password } = this.data

    if (!username) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    wx.showLoading({ title: '登录中...' })

    post('/api/user/login', { username, password }, { noAuth: true })
      .then(res => {
        wx.hideLoading()
        if (res.code !== 200) {
          wx.showToast({ title: res.message || '登录失败', icon: 'none' })
          return
        }
        const data = res.data || {}
        const token = data.token
        const userInfo = data.user || {}

        // Persist to storage
        wx.setStorageSync('token', token)
        wx.setStorageSync('userInfo', userInfo)

        // Update globalData
        app.globalData.token = token
        app.globalData.userInfo = userInfo

        wx.showToast({ title: '登录成功', icon: 'success' })

        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' })
        }, 800)
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: (err && err.message) || '登录失败，请重试',
          icon: 'none'
        })
      })
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
