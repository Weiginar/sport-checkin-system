const { post } = require('../../utils/request')

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    nickname: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value.trim() })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value.trim() })
  },

  onRegister() {
    const { username, password, confirmPassword, nickname } = this.data

    // Validation
    if (!username || username.length < 4 || username.length > 20) {
      wx.showToast({ title: '用户名需4-20个字符', icon: 'none' })
      return
    }
    if (!password || password.length < 6 || password.length > 20) {
      wx.showToast({ title: '密码需6-20个字符', icon: 'none' })
      return
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码输入不一致', icon: 'none' })
      return
    }

    wx.showLoading({ title: '注册中...' })

    const params = { username, password }
    if (nickname) params.nickname = nickname

    post('/api/user/register', params, { noAuth: true })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '注册成功', icon: 'success' })

        setTimeout(() => {
          wx.redirectTo({ url: '/pages/login/login' })
        }, 800)
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: (err && err.message) || '注册失败，请重试',
          icon: 'none'
        })
      })
  },

  goLogin() {
    wx.navigateBack({
      fail() {
        wx.redirectTo({ url: '/pages/login/login' })
      }
    })
  }
})
