<template>
  <div class="statistics-page">
    <el-card shadow="never" class="filter-card">
      <el-radio-group v-model="days" @change="handleRangeChange">
        <el-radio-button :value="7">近7天</el-radio-button>
        <el-radio-button :value="30">近30天</el-radio-button>
        <el-radio-button :value="90">近90天</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card shadow="never" class="chart-card">
      <template #header>每日打卡人数趋势</template>
      <div ref="lineChartRef" class="chart-container chart-large" />
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>运动类型分布</template>
          <div ref="pieChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>用户增长趋势</template>
          <div ref="barChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getStatsTrend } from '../api'

const days = ref(30)

const lineChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)

let lineChart = null
let pieChart = null
let barChart = null

const initCharts = () => {
  lineChart = echarts.init(lineChartRef.value)
  pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)
}

const updateLineChart = (data) => {
  const dates = data.map((d) => d.date)
  const values = data.map((d) => d.count)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { type: 'slider', start: 0, end: 100 },
    ],
    series: [
      {
        name: '打卡人数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        data: values,
      },
    ],
    grid: { left: 50, right: 30, top: 20, bottom: 70 },
  })
}

const updatePieChart = (data) => {
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [
      {
        name: '运动类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        label: { formatter: '{b}\n{d}%' },
        data: data.map((d) => ({ name: d.name, value: d.count })),
      },
    ],
  })
}

const updateBarChart = (data) => {
  const dates = data.map((d) => d.date)
  const values = data.map((d) => d.newUsers)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: values,
        itemStyle: { borderRadius: [4, 4, 0, 0] },
      },
    ],
    grid: { left: 50, right: 20, top: 20, bottom: 60 },
  })
}

const loadData = async () => {
  try {
    const res = await getStatsTrend({ days: days.value })
    const { dailyCheckins, sportDistribution, userGrowth } = res.data || {}
    updateLineChart(dailyCheckins || [])
    updatePieChart(sportDistribution || [])
    updateBarChart(userGrowth || [])
  } catch (err) {
    ElMessage.error('加载统计数据失败')
  }
}

const handleRangeChange = () => {
  loadData()
}

const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  await nextTick()
  initCharts()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}
.filter-card {
  margin-bottom: 16px;
}
.chart-card {
  margin-bottom: 16px;
}
.chart-container {
  height: 350px;
  width: 100%;
}
.chart-large {
  height: 400px;
}
</style>
