const { get } = require('../../utils/request');
const { formatDate, getSportIcon } = require('../../utils/util');

Page({
  data: {
    currentYear: 0,
    currentMonth: 0,
    calendarDays: [],
    checkinDates: [],
    records: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    const now = new Date();
    this.setData({
      currentYear: now.getFullYear(),
      currentMonth: now.getMonth() + 1
    });
    this.generateCalendar();
    this.loadCalendarData();
    this.loadHistory(true);
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
  },

  prevMonth() {
    let { currentYear, currentMonth } = this.data;
    currentMonth--;
    if (currentMonth < 1) {
      currentMonth = 12;
      currentYear--;
    }
    this.setData({ currentYear, currentMonth });
    this.generateCalendar();
    this.loadCalendarData();
    this.setData({ records: [], pageNum: 1, hasMore: true });
    this.loadHistory(true);
  },

  nextMonth() {
    let { currentYear, currentMonth } = this.data;
    currentMonth++;
    if (currentMonth > 12) {
      currentMonth = 1;
      currentYear++;
    }
    this.setData({ currentYear, currentMonth });
    this.generateCalendar();
    this.loadCalendarData();
    this.setData({ records: [], pageNum: 1, hasMore: true });
    this.loadHistory(true);
  },

  generateCalendar() {
    const { currentYear, currentMonth, checkinDates } = this.data;
    const today = new Date();
    const todayStr = formatDate(today);

    const firstDay = new Date(currentYear, currentMonth - 1, 1);
    const lastDay = new Date(currentYear, currentMonth, 0);
    const startWeekday = firstDay.getDay();
    const daysInMonth = lastDay.getDate();

    const prevLastDay = new Date(currentYear, currentMonth - 1, 0);
    const prevDays = prevLastDay.getDate();

    const days = [];

    // Previous month fill
    for (let i = startWeekday - 1; i >= 0; i--) {
      days.push({
        day: prevDays - i,
        isCurrentMonth: false,
        isToday: false,
        isChecked: false
      });
    }

    // Current month
    for (let d = 1; d <= daysInMonth; d++) {
      const dateStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      days.push({
        day: d,
        isCurrentMonth: true,
        isToday: dateStr === todayStr,
        isChecked: checkinDates.indexOf(dateStr) !== -1
      });
    }

    // Next month fill
    const remaining = 7 - (days.length % 7);
    if (remaining < 7) {
      for (let i = 1; i <= remaining; i++) {
        days.push({
          day: i,
          isCurrentMonth: false,
          isToday: false,
          isChecked: false
        });
      }
    }

    this.setData({ calendarDays: days });
  },

  loadCalendarData() {
    const { currentYear, currentMonth } = this.data;
    get('/api/checkin/calendar', {
      year: currentYear,
      month: currentMonth
    }).then(res => {
      if (res && res.data) {
        this.setData({ checkinDates: res.data.checkinDates || [] });
        this.generateCalendar();
      }
    }).catch(() => {});
  },

  loadHistory(reset) {
    if (this.data.loading) return;
    if (!reset && !this.data.hasMore) return;

    const pageNum = reset ? 1 : this.data.pageNum;
    this.setData({ loading: true });

    const { currentYear, currentMonth } = this.data;
    const startDate = `${currentYear}-${String(currentMonth).padStart(2, '0')}-01`;
    const lastDay = new Date(currentYear, currentMonth, 0).getDate();
    const endDate = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
    get('/api/checkin/history', {
      startDate,
      endDate,
      pageNum,
      pageSize: this.data.pageSize
    }).then(res => {
      if (res && res.data) {
        const list = (res.data.records || []).map(item => ({
          ...item,
          sportIcon: getSportIcon(item.sportTypeIcon || item.sportIcon),
          checkinDate: item.checkinDate || formatDate(item.createTime)
        }));
        const records = reset ? list : this.data.records.concat(list);
        this.setData({
          records,
          pageNum: pageNum + 1,
          hasMore: list.length >= this.data.pageSize,
          loading: false
        });
      }
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onReachBottom() {
    this.loadHistory(false);
  }
});
