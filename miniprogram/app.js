App({
  globalData: {
    baseUrl: 'http://192.168.0.125:8080',
    token: '',
    userInfo: null
  },
  onLaunch() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
  }
})
