const { get } = require('../../utils/request');
const { getSportIcon } = require('../../utils/util');

const DIST_COLORS = ['#43cea2', '#185a9d', '#f5a623', '#e74c3c', '#9b59b6', '#3498db', '#1abc9c', '#e67e22'];
const WEEK_LABELS = ['日', '一', '二', '三', '四', '五', '六'];

Page({
  data: {
    stats: {},
    weeklyData: [],
    sportDistribution: [],
    recentTrend: []
  },

  onLoad() {
    this.loadStats();
  },

  loadStats() {
    get('/api/stats/personal').then(res => {
      if (res && res.code === 200 && res.data) {
        const data = res.data;
        this.setData({
          stats: {
            totalCheckins: data.totalCheckins || 0,
            consecutiveDays: data.consecutiveDays || 0,
            maxConsecutive: data.maxConsecutive || 0,
            totalDuration: data.totalDuration || 0,
            totalCalories: data.totalCalories || 0,
            points: data.points || 0
          }
        });
        this.processWeeklyData(data.weeklyDuration || []);
        this.processSportDistribution(data.sportDistribution || []);
        this.processRecentTrend(data.recentTrend || []);
      }
    }).catch(() => {});
  },

  processWeeklyData(raw) {
    const days = [];
    const today = new Date();

    // Build map from raw data
    const map = {};
    raw.forEach(item => {
      map[item.date] = item.duration || 0;
    });

    // Fill past 7 days
    let maxDuration = 0;
    for (let i = 6; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      const dateStr = `${y}-${m}-${day}`;
      const duration = map[dateStr] || 0;
      if (duration > maxDuration) maxDuration = duration;
      days.push({
        date: dateStr,
        duration,
        label: WEEK_LABELS[d.getDay()]
      });
    }

    // Calculate percentages
    const weeklyData = days.map(item => ({
      ...item,
      percent: maxDuration > 0 ? Math.round((item.duration / maxDuration) * 100) : 0
    }));

    this.setData({ weeklyData });
  },

  processSportDistribution(raw) {
    if (!raw.length) {
      this.setData({ sportDistribution: [] });
      return;
    }

    const maxCount = Math.max(...raw.map(item => item.count || 0));

    const sportDistribution = raw.map((item, index) => ({
      name: item.sportName || item.name,
      icon: getSportIcon(item.sportIcon || item.icon),
      count: item.count || 0,
      percent: maxCount > 0 ? Math.round(((item.count || 0) / maxCount) * 100) : 0,
      color: DIST_COLORS[index % DIST_COLORS.length]
    }));

    this.setData({ sportDistribution });
  },

  processRecentTrend(raw) {
    if (!raw.length) {
      this.setData({ recentTrend: [] });
      return;
    }

    // Group by week (show ~4 weeks)
    const weeks = [];
    const today = new Date();

    // Build daily map
    const map = {};
    raw.forEach(item => {
      map[item.date] = item.duration || 0;
    });

    // Aggregate into 4-week buckets (recent 30 days, ~4 rows)
    for (let w = 3; w >= 0; w--) {
      let count = 0;
      const endOffset = w * 7;
      const startOffset = endOffset + 6;
      const endDate = new Date(today);
      endDate.setDate(endDate.getDate() - endOffset);
      const startDate = new Date(today);
      startDate.setDate(startDate.getDate() - startOffset);

      for (let i = 0; i <= 6; i++) {
        const d = new Date(startDate);
        d.setDate(d.getDate() + i);
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        count += map[`${y}-${m}-${day}`] || 0;
      }

      const sm = String(startDate.getMonth() + 1) + '/' + String(startDate.getDate());
      weeks.push({ label: sm, count });
    }

    const maxCount = Math.max(...weeks.map(w => w.count));
    const recentTrend = weeks.map(item => ({
      ...item,
      percent: maxCount > 0 ? Math.round((item.count / maxCount) * 100) : 0
    }));

    this.setData({ recentTrend });
  }
});
