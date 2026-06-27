<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-borrow">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">今日借阅</p>
              <p class="stat-value">{{ stats.todayBorrows }}</p>
            </div>
            <el-icon class="stat-icon" :size="48"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-readers">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">读者总数</p>
              <p class="stat-value">{{ stats.totalReaders }}</p>
            </div>
            <el-icon class="stat-icon" :size="48"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-books">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">馆藏总数</p>
              <p class="stat-value">{{ stats.totalBooks }}</p>
            </div>
            <el-icon class="stat-icon" :size="48"><Reading /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-overdue">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">逾期数量</p>
              <p class="stat-value">{{ stats.overdueCount }}</p>
            </div>
            <el-icon class="stat-icon" :size="48"><WarningFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">借阅趋势</span>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">借阅排行榜</span>
          </template>
          <el-table :data="rankList" stripe size="small" max-height="360">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="title" label="书名" show-overflow-tooltip />
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告区域 -->
    <el-row :gutter="20" class="announcement-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">最新公告</span>
          </template>
          <el-timeline v-if="announcements.length">
            <el-timeline-item
              v-for="item in announcements"
              :key="item.id"
              :timestamp="item.createTime"
              placement="top"
            >
              <el-card shadow="never" class="announcement-item">
                <h4>{{ item.title }}</h4>
                <p class="announcement-content">{{ item.content }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无公告" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Document, User, Reading, WarningFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getBorrowStats, getReaderStats, getCollectionStats, getOverdueStats } from '../../../api/modules/report'
import { getAnnouncements } from '../../../api/modules/system'
import { getBorrowRank } from '../../../api/modules/book'
import { useUserStore } from '../../../stores/user'

const chartRef = ref(null)
let chartInstance = null
const userStore = useUserStore()
const canViewReports = () => userStore.roleKey && userStore.roleKey !== 'reader'

const stats = reactive({
  todayBorrows: 0,
  totalReaders: 0,
  totalBooks: 0,
  overdueCount: 0
})

const rankList = ref([])
const announcements = ref([])

const fetchStats = async () => {
  if (!canViewReports()) return
  try {
    const [borrowRes, readerRes, collectionRes, overdueRes] = await Promise.all([
      getBorrowStats(),
      getReaderStats(),
      getCollectionStats(),
      getOverdueStats()
    ])
    stats.todayBorrows = borrowRes.data?.todayBorrows ?? borrowRes.data?.todayCount ?? 0
    stats.totalReaders = readerRes.data?.totalReaders ?? readerRes.data?.total ?? 0
    stats.totalBooks = collectionRes.data?.totalBooks ?? collectionRes.data?.total ?? 0
    stats.overdueCount = overdueRes.data?.overdueCount ?? 0
  } catch {
    // error handled by interceptor
  }
}

const fetchRank = async () => {
  try {
    const res = await getBorrowRank(10)
    rankList.value = res.data || []
  } catch {
    // error handled by interceptor
  }
}

const fetchAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ page: 1, size: 5 })
    announcements.value = res.data?.records || res.data || []
  } catch {
    // error handled by interceptor
  }
}

const initChart = async () => {
  // fetch borrow trend data
  let dates = []
  let counts = []
  try {
    if (!canViewReports()) {
      throw new Error('report permission required')
    }
    const res = await getBorrowStats({ type: 'trend' })
    const trendData = res.data?.dailyTrend || res.data?.trend || res.data || []
    dates = trendData.map(d => d.date)
    counts = trendData.map(d => d.count)
  } catch {
    // fallback: generate mock dates for last 7 days
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      dates.push(`${d.getMonth() + 1}/${d.getDate()}`)
      counts.push(0)
    }
  }

  await nextTick()
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '借阅量',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' },
        data: counts
      }
    ]
  })
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(async () => {
  fetchStats()
  fetchRank()
  fetchAnnouncements()
  await initChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  border-left: 4px solid;
}

.stat-borrow {
  border-left-color: #409eff;
}

.stat-readers {
  border-left-color: #67c23a;
}

.stat-books {
  border-left-color: #e6a23c;
}

.stat-overdue {
  border-left-color: #f56c6c;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-title {
  margin: 0 0 8px;
  font-size: 14px;
  color: #909399;
}

.stat-value {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-icon {
  color: #c0c4cc;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.announcement-row {
  margin-bottom: 20px;
}

.announcement-item {
  border: none;
}

.announcement-item h4 {
  margin: 0 0 4px;
  color: #303133;
}

.announcement-content {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
</style>
