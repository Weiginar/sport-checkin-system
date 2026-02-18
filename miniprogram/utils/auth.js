const app = getApp();

const checkLogin = () => {
  if (!app.globalData.token) {
    wx.redirectTo({ url: '/pages/login/login' });
    return false;
  }
  return true;
};

const logout = () => {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');
  app.globalData.token = '';
  app.globalData.userInfo = null;
  wx.redirectTo({ url: '/pages/login/login' });
};

module.exports = { checkLogin, logout };
