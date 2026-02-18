const { get } = require('../../utils/request');
const { getGreeting, getSportIcon } = require('../../utils/util');

Page({
  data: {
    userInfo: {},
    greeting: '',
    todayStatus: { checkedIn: false },
    stats: {},
    sportList: []
  },

  onShow() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }

    this.setData({ greeting: getGreeting() });
    this.loadUserInfo();
    this.loadTodayStatus();
    this.loadSportList();
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({ userInfo });
    }
  },

  async loadTodayStatus() {
    try {
      const res = await get('/api/checkin/today');
      if (res.code === 200 && res.data) {
        const d = res.data;
        const record = d.record || {};
        this.setData({
          todayStatus: {
            checkedIn: d.checkedIn,
            sportName: record.sportTypeName || '',
            duration: record.duration || 0,
            calories: record.calories || 0
          }
        });
      }
      // Load stats from userInfo (profile)
      const userInfo = wx.getStorageSync('userInfo') || {};
      this.setData({
        stats: {
          consecutiveDays: userInfo.consecutiveDays || 0,
          totalCheckins: userInfo.totalCheckins || 0,
          totalPoints: userInfo.points || 0
        }
      });
    } catch (e) {
      console.error('loadTodayStatus failed', e);
    }
  },

  async loadSportList() {
    try {
      const res = await get('/api/sport/list');
      if (res.code === 200 && res.data) {
        const sportList = res.data.map(item => ({
          id: item.id,
          name: item.name,
          icon: getSportIcon(item.icon),
          caloriesPerHour: item.caloriesPerHour || 300
        }));
        this.setData({ sportList });
      }
    } catch (e) {
      console.error('loadSportList failed', e);
    }
  },

  goCheckin(e) {
    const sportTypeId = e.currentTarget?.dataset?.id || '';
    wx.switchTab({
      url: '/pages/checkin/checkin',
      success() {
        if (sportTypeId) {
          const pages = getCurrentPages();
          const checkinPage = pages[pages.length - 1];
          if (checkinPage && checkinPage.preselectSport) {
            checkinPage.preselectSport(sportTypeId);
          }
        }
      }
    });
  }
});
