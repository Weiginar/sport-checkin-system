const { put } = require('../../utils/request')

const GENDER_MAP = { 1: '男', 2: '女', 0: '保密' }
const GENDER_REVERSE = { '男': 1, '女': 2, '保密': 0 }

Page({
  data: {
    nickname: '',
    avatar: '',
    phone: '',
    gender: '保密'
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo') || {}
    this.setData({
      nickname: userInfo.nickname || '',
      avatar: userInfo.avatar || '',
      phone: userInfo.phone || '',
      gender: GENDER_MAP[userInfo.gender] || '保密'
    })
  },

  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempPath = res.tempFiles[0].tempFilePath
        this.setData({ avatar: tempPath })
      }
    })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  onGenderChange(e) {
    this.setData({ gender: e.detail.value })
  },

  onSave() {
    const { nickname, avatar, phone, gender } = this.data

    if (!nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }

    if (phone && !/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }

    wx.showLoading({ title: '保存中...' })

    const genderInt = GENDER_REVERSE[gender] !== undefined ? GENDER_REVERSE[gender] : 0

    put('/api/user/profile', { nickname, avatar, phone, gender: genderInt }).then(res => {
      wx.hideLoading()
      // Update local storage
      const userInfo = wx.getStorageSync('userInfo') || {}
      Object.assign(userInfo, { nickname, avatar, phone, gender: genderInt })
      wx.setStorageSync('userInfo', userInfo)

      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1200)
    }).catch(err => {
      wx.hideLoading()
      console.error('保存失败', err)
      wx.showToast({ title: '保存失败，请重试', icon: 'none' })
    })
  }
})
