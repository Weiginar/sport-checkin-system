const { get } = require('../../utils/request');

Page({
  data: {
    currentTab: 'week',
    rankList: [],
    myRank: 0,
    loading: false
  },

  onLoad() {
    this.loadRanking();
  },

  onPullDownRefresh() {
    this.loadRanking().then(() => wx.stopPullDownRefresh());
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.currentTab) return;
    this.setData({ currentTab: tab, rankList: [], myRank: 0 });
    this.loadRanking();
  },

  loadRanking() {
    if (this.data.loading) return Promise.resolve();
    this.setData({ loading: true });
    wx.showLoading({ title: '加载中' });

    const userInfo = wx.getStorageSync('userInfo');
    const myUserId = userInfo ? userInfo.id : '';

    return get('/api/stats/ranking', { type: this.data.currentTab }).then(res => {
      if (res.code === 200 && res.data) {
        const list = res.data.rankList || [];
        let myRank = 0;
        const rankList = list.map((item, index) => {
          const uid = item.userId ? Number(item.userId) : 0;
          const isMe = uid === Number(myUserId);
          if (isMe) myRank = index + 1;
          return { ...item, isMe };
        });
        if (res.data.myRank && res.data.myRank.rank) {
          myRank = res.data.myRank.rank;
        }
        this.setData({ rankList, myRank });
      }
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }).finally(() => {
      this.setData({ loading: false });
      wx.hideLoading();
    });
  }
});
