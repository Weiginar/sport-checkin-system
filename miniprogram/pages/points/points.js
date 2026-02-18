const { get } = require('../../utils/request');

const TYPE_ICONS = {
  CHECKIN: '🏃',
  CONSECUTIVE: '🔥',
  ACHIEVEMENT: '🏆'
};

Page({
  data: {
    totalPoints: 0,
    records: [],
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    loading: false
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.points !== undefined) {
      this.setData({ totalPoints: userInfo.points });
    }
    this.loadRecords();
  },

  onPullDownRefresh() {
    this.setData({ records: [], pageNum: 1, hasMore: true });
    this.loadRecords().then(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadRecords();
    }
  },

  loadRecords() {
    if (this.data.loading) return Promise.resolve();
    this.setData({ loading: true });

    const { pageNum, pageSize } = this.data;
    return get('/api/points/list', { pageNum, pageSize }).then(res => {
      if (res.code === 200) {
        const newRecords = (res.data.records || res.data.list || []).map(item => ({
          ...item,
          typeIcon: TYPE_ICONS[item.type] || '🏅'
        }));
        const records = this.data.records.concat(newRecords);
        const hasMore = newRecords.length >= pageSize;
        if (res.data.totalPoints !== undefined) {
          this.setData({ totalPoints: res.data.totalPoints });
        }
        this.setData({
          records,
          hasMore,
          pageNum: pageNum + 1
        });
      }
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }).finally(() => {
      this.setData({ loading: false });
    });
  }
});
