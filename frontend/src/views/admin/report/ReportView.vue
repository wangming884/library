<template>
  <div class="report-container">
    <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
      <!-- 借阅统计 -->
      <el-tab-pane label="借阅统计" name="borrow">
        <el-row :gutter="20" class="stat-cards">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">总借阅量</div>
                <div class="stat-value">{{ borrowStats.totalBorrows || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">今日借阅</div>
                <div class="stat-value today">{{ borrowStats.todayBorrows || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">本月借阅</div>
                <div class="stat-value">{{ borrowStats.monthBorrows || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">平均借阅天数</div>
                <div class="stat-value">{{ borrowStats.avgDays?.toFixed(1) || 0 }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-card>
          <template #header><span>近7日借阅趋势</span></template>
          <div ref="borrowChartRef" class="chart-box"></div>
        </el-card>
      </el-tab-pane>

      <!-- 读者统计 -->
      <el-tab-pane label="读者统计" name="reader">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header><span>读者类型分布</span></template>
              <div ref="readerPieRef" class="chart-box"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header><span>院系借阅占比</span></template>
              <div ref="deptChartRef" class="chart-box"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 馆藏统计 -->
      <el-tab-pane label="馆藏统计" name="collection">
        <el-card>
          <template #header><span>各分类藏书数量</span></template>
          <div ref="categoryBarRef" class="chart-box"></div>
        </el-card>
      </el-tab-pane>

      <!-- 逾期统计 -->
      <el-tab-pane label="逾期统计" name="overdue">
        <el-row :gutter="20" class="stat-cards">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">当前逾期</div>
                <div class="stat-value danger">{{ overdueStats.currentOverdue || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">本月逾期</div>
                <div class="stat-value">{{ overdueStats.monthOverdue || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">逾期罚款总额</div>
                <div class="stat-value">{{ overdueStats.totalFine?.toFixed(2) || '0.00' }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">逾期率</div>
                <div class="stat-value">{{ overdueStats.overdueRate?.toFixed(1) || 0 }}%</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import {
  getBorrowStats,
  getReaderStats,
  getCollectionStats,
  getOverdueStats,
  getDeptRatio,
  getCategoryCount
} from '../../../api/modules/report'

const activeTab = ref('borrow')
const borrowStats = ref({})
const overdueStats = ref({})

const borrowChartRef = ref(null)
const readerPieRef = ref(null)
const deptChartRef = ref(null)
const categoryBarRef = ref(null)

let borrowChart = null
let readerPie = null
let deptChart = null
let categoryBar = null

const onTabChange = (tab) => {
  nextTick(() => {
    if (tab === 'borrow') {
      renderBorrowChart()
    } else if (tab === 'reader') {
      renderReaderPie()
      renderDeptChart()
    } else if (tab === 'collection') {
      renderCategoryBar()
    } else if (tab === 'overdue') {
      fetchOverdueStats()
    }
  })
}

const fetchBorrowStats = async () => {
  try {
    const res = await getBorrowStats()
    const data = res.data || {}
    borrowStats.value = data.summary || data
    await nextTick()
    renderBorrowChart(data.trend || data.daily || [])
  } catch {
    // handled
  }
}

const renderBorrowChart = (trend) => {
  if (!borrowChartRef.value) return
  if (borrowChart) borrowChart.dispose()
  borrowChart = echarts.init(borrowChartRef.value)
  const dates = trend?.map(i => i.date) || []
  const counts = trend?.map(i => i.count) || []
  borrowChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '借阅量' },
    series: [{
      data: counts,
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409EFF' }
    }],
    grid: { left: 60, right: 30, bottom: 30, top: 30 }
  })
}

const renderReaderPie = async () => {
  if (!readerPieRef.value) return
  try {
    const res = await getReaderStats()
    const data = res.data || []
    if (readerPie) readerPie.dispose()
    readerPie = echarts.init(readerPieRef.value)
    readerPie.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        label: { formatter: '{b}: {c} ({d}%)' },
        data: (Array.isArray(data) ? data : data.list || []).map(i => ({
          name: i.typeName || i.name,
          value: i.count || i.value
        }))
      }]
    })
  } catch {
    // handled
  }
}

const renderDeptChart = async () => {
  if (!deptChartRef.value) return
  try {
    const res = await getDeptRatio()
    const data = res.data || []
    if (deptChart) deptChart.dispose()
    deptChart = echarts.init(deptChartRef.value)
    deptChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: '65%',
        data: (Array.isArray(data) ? data : data.list || []).map(i => ({
          name: i.deptName || i.name,
          value: i.count || i.value
        }))
      }]
    })
  } catch {
    // handled
  }
}

const renderCategoryBar = async () => {
  if (!categoryBarRef.value) return
  try {
    const res = await getCategoryCount()
    const data = res.data || []
    if (categoryBar) categoryBar.dispose()
    categoryBar = echarts.init(categoryBarRef.value)
    const list = Array.isArray(data) ? data : data.list || []
    categoryBar.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: list.map(i => i.categoryName || i.name),
        axisLabel: { rotate: 30 }
      },
      yAxis: { type: 'value', name: '数量' },
      series: [{
        type: 'bar',
        data: list.map(i => i.count || i.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#79bbff' }
          ])
        },
        barMaxWidth: 40
      }],
      grid: { left: 60, right: 30, bottom: 60, top: 30 }
    })
  } catch {
    // handled
  }
}

const fetchOverdueStats = async () => {
  try {
    const res = await getOverdueStats()
    overdueStats.value = res.data || {}
  } catch {
    // handled
  }
}

const handleResize = () => {
  borrowChart?.resize()
  readerPie?.resize()
  deptChart?.resize()
  categoryBar?.resize()
}

onMounted(() => {
  fetchBorrowStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  borrowChart?.dispose()
  readerPie?.dispose()
  deptChart?.dispose()
  categoryBar?.dispose()
})
</script>

<style scoped>
.report-container {
  padding: 0;
}
.stat-cards {
  margin-bottom: 20px;
}
.stat-item {
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-value.today {
  color: #409EFF;
}
.stat-value.danger {
  color: #F56C6C;
}
.chart-box {
  width: 100%;
  height: 400px;
}
</style>
