const { get, post } = require('../../utils/request');
const { getSportIcon } = require('../../utils/util');

Page({
  data: {
    sportList: [],
    selectedSport: null,
    duration: 30,
    distance: '',
    calories: 0,
    remark: '',
    todayChecked: false,
    showSuccess: false,
    checkinResult: {}
  },

  onLoad(options) {
    this._preselectedId = options.sportTypeId || '';
    this.loadSportList();
    this.checkTodayStatus();
  },

  onShow() {
    this.checkTodayStatus();
  },

  preselectSport(sportTypeId) {
    this._preselectedId = sportTypeId;
    const { sportList } = this.data;
    if (sportList.length) {
      this._applyPreselect();
    }
  },

  _applyPreselect() {
    if (!this._preselectedId) return;
    const idx = this.data.sportList.findIndex(s => s.id === this._preselectedId);
    if (idx !== -1) {
      this.setData({ selectedSport: this.data.sportList[idx] });
      this.calculateCalories();
    }
    this._preselectedId = '';
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

        if (this._preselectedId) {
          this._applyPreselect();
        } else if (sportList.length && !this.data.selectedSport) {
          this.setData({ selectedSport: sportList[0] });
          this.calculateCalories();
        }
      }
    } catch (e) {
      console.error('loadSportList failed', e);
    }
  },

  async checkTodayStatus() {
    try {
      const res = await get('/api/checkin/today');
      if (res.code === 200 && res.data) {
        this.setData({ todayChecked: !!res.data.checkedIn });
      }
    } catch (e) {
      console.error('checkTodayStatus failed', e);
    }
  },

  selectSport(e) {
    const index = e.currentTarget.dataset.index;
    const sport = this.data.sportList[index];
    this.setData({ selectedSport: sport });
    this.calculateCalories();
  },

  onDurationChange(e) {
    this.setData({ duration: e.detail.value });
    this.calculateCalories();
  },

  onDistanceInput(e) {
    this.setData({ distance: e.detail.value });
  },

  onCaloriesInput(e) {
    this.setData({ calories: Number(e.detail.value) || 0 });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  calculateCalories() {
    const { selectedSport, duration } = this.data;
    if (!selectedSport) return;
    const cal = Math.round(selectedSport.caloriesPerHour * duration / 60);
    this.setData({ calories: cal });
  },

  async doCheckin() {
    if (this.data.todayChecked) return;

    const { selectedSport, duration, distance, calories, remark } = this.data;
    if (!selectedSport) {
      wx.showToast({ title: '请选择运动类型', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '打卡中...' });
    try {
      const res = await post('/api/checkin/do', {
        sportTypeId: selectedSport.id,
        duration,
        distance: distance ? Number(distance) : 0,
        calories,
        remark
      });
      wx.hideLoading();

      if (res.code === 200) {
        this.setData({
          todayChecked: true,
          showSuccess: true,
          checkinResult: res.data || {}
        });
      } else {
        wx.showToast({ title: res.message || '打卡失败', icon: 'none' });
      }
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: '打卡失败，请重试', icon: 'none' });
    }
  },

  closeSuccess() {
    this.setData({ showSuccess: false });
  }
});
