const app = getApp();

const request = (options) => {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (!options.noAuth && app.globalData.token) {
      header['Authorization'] = 'Bearer ' + app.globalData.token;
    }
    wx.request({
      url: app.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success(res) {
        if (res.data.code === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          app.globalData.token = '';
          app.globalData.userInfo = null;
          wx.redirectTo({ url: '/pages/login/login' });
          reject(res.data);
          return;
        }
        resolve(res.data);
      },
      fail(err) {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

const get = (url, data, options) => request({ url, method: 'GET', data, ...options });
const post = (url, data, options) => request({ url, method: 'POST', data, ...options });
const put = (url, data, options) => request({ url, method: 'PUT', data, ...options });

module.exports = { request, get, post, put };
