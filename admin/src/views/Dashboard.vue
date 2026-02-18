<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ overview.totalUsers ?? 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
            <el-icon class="stat-icon" style="color: #409eff; background: rgba(64,158,255,0.1)">
              <User />
            </el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ overview.todayCheckins ?? 0 }}</div>
              <div class="stat-label">今日打卡</div>
            </div>
            <el-icon class="stat-icon" style="color: #67c23a; background: rgba(103,194,58,0.1)">
              <Calendar />
            </el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ overview.totalCheckins ?? 0 }}</div>
              <div class="stat-label">总打卡次数</div>
            </div>
            <el-icon class="stat-icon" style="color: #e6a23c; background: rgba(230,162,60,0.1)">
              <Document />
            </el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ overview.activeUsers ?? 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
            <el-icon class="stat-icon" style="color: #a855f7; background: rgba(168,85,247,0.1)">
              <UserFilled />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域：打卡趋势 + 运动类型分布 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>近30天打卡趋势</template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>运动类型分布</template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户增长趋势 -->
    <el-row class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>用户增长趋势</template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { User, Calendar, Document, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getStatsOverview, getStatsTrend } from '../api'

const overview = reactive({
  totalUsers: 0,
  todayCheckins: 0,
  totalCheckins: 0,
  activeUsers: 0
})

const lineChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)

let lineChart = null
let pieChart = null
let barChart = null

const initLineChart = (data) => {
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.date),
      axisLabel: { rotate: 45 }
    },
    yAxis: { type: 'value', name: '打卡次数' },
    series: [
      {
        name: '打卡次数',
        type: 'line',
        smooth: true,
        data: data.map((item) => item.count),
        areaStyle: { color: 'rgba(64,158,255,0.15)' },
        lineStyle: { color: '#409eff' },
        itemStyle: { color: '#409eff' }
      }
    ],
    grid: { left: 50, right: 20, bottom: 60, top: 20 }
  })
}

const initPieChart = (data) => {
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        data: data.map((item) => ({ name: item.name, value: item.count })),
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
        },
        label: { formatter: '{b}\n{d}%' }
      }
    ]
  })
}

const initBarChart = (data) => {
  barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.date),
      axisLabel: { rotate: 45 }
    },
    yAxis: { type: 'value', name: '新增用户' },
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: data.map((item) => item.newUsers),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4facfe' },
            { offset: 1, color: '#00f2fe' }
          ])
        },
        barWidth: '50%'
      }
    ],
    grid: { left: 50, right: 20, bottom: 60, top: 20 }
  })
}

const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  try {
    const [overviewRes, trendRes] = await Promise.all([
      getStatsOverview(),
      getStatsTrend({ days: 30 })
    ])

    const overviewData = overviewRes.data || overviewRes
    Object.assign(overview, overviewData)

    const trendData = trendRes.data || trendRes
    const { dailyCheckins = [], sportDistribution = [], userGrowth = [] } = trendData

    initLineChart(dailyCheckins)
    initPieChart(sportDistribution)
    initBarChart(userGrowth)

    window.addEventListener('resize', handleResize)
  } catch (err) {
    console.error('加载统计数据失败', err)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card .stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info .stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-info .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 360px;
}
</style>
