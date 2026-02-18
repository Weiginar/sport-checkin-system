const { get } = require('../../utils/request');
const { formatDate } = require('../../utils/util');

Page({
  data: {
    achievements: [],
    unlockedCount: 0,
    totalCount: 0,
    showDetail: false,
    currentAchievement: {}
  },

  onLoad() {
    this.loadAchievements();
  },

  onPullDownRefresh() {
    this.loadAchievements().then(() => wx.stopPullDownRefresh());
  },

  loadAchievements() {
    wx.showLoading({ title: '加载中' });
    const userInfo = wx.getStorageSync('userInfo') || {};
    return get('/api/achievement/list').then(res => {
      if (res.code === 200) {
        const list = Array.isArray(res.data) ? res.data : (res.data.list || []);
        const achievements = list.map(item => {
          const unlocked = !!item.unlocked;
          const target = item.conditionValue || 1;
          let current = 0;
          if (item.conditionType === 'TOTAL_CHECKINS') {
            current = userInfo.totalCheckins || 0;
          } else if (item.conditionType === 'CONSECUTIVE') {
            current = userInfo.consecutiveDays || 0;
          } else if (item.conditionType === 'TOTAL_DURATION') {
            current = userInfo.totalDuration || 0;
          }
          if (unlocked) current = target;
          const progress = Math.min(Math.round((current / target) * 100), 100);
          return {
            ...item,
            unlocked,
            target,
            current,
            progress,
            unlockTime: item.unlockTime ? formatDate(item.unlockTime) : ''
          };
        });
        const unlockedCount = achievements.filter(a => a.unlocked).length;
        this.setData({
          achievements,
          unlockedCount,
          totalCount: achievements.length
        });
      }
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }).finally(() => {
      wx.hideLoading();
    });
  },

  showAchievementDetail(e) {
    const index = e.currentTarget.dataset.index;
    const currentAchievement = this.data.achievements[index];
    this.setData({ showDetail: true, currentAchievement });
  },

  closeDetail() {
    this.setData({ showDetail: false, currentAchievement: {} });
  }
});
