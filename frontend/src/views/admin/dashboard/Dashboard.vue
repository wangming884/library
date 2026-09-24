<template>
  <div class="dashboard">
    <!-- 统计指标卡片 -->
    <el-row :gutter="18" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-borrow">
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-title">今日借阅量</span>
              <div class="stat-value">{{ stats.todayBorrows }}</div>
              <span class="stat-tip">流通借还实时统计</span>
            </div>
            <div class="stat-icon-wrapper borrow-bg">
              <el-icon :size="28"><Document /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-readers">
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-title">注册读者总数</span>
              <div class="stat-value">{{ stats.totalReaders }}</div>
              <span class="stat-tip">学生/教职工/校外</span>
            </div>
            <div class="stat-icon-wrapper readers-bg">
              <el-icon :size="28"><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-books">
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-title">馆藏图书总量</span>
              <div class="stat-value">{{ stats.totalBooks }}</div>
              <span class="stat-tip">中图分类馆藏复本</span>
            </div>
            <div class="stat-icon-wrapper books-bg">
              <el-icon :size="28"><Reading /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-overdue">
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-title">当前逾期待还</span>
              <div class="stat-value" :class="{ 'has-overdue': stats.overdueCount > 0 }">{{ stats.overdueCount }}</div>
              <span class="stat-tip">需催还或缴纳罚金</span>
            </div>
            <div class="stat-icon-wrapper overdue-bg">
              <el-icon :size="28"><WarningFilled /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 常用功能快捷入口 -->
    <el-card shadow="never" class="quick-nav-card">
      <div class="quick-nav-title">常用业务快捷通道</div>
      <div class="quick-nav-grid">
        <div class="quick-item" @click="router.push('/admin/borrow')">
          <div class="quick-icon-box q-borrow"><el-icon><DocumentAdd /></el-icon></div>
          <span>借还办理</span>
        </div>
        <div class="quick-item" @click="router.push('/admin/books')">
          <div class="quick-icon-box q-book"><el-icon><Reading /></el-icon></div>
          <span>图书管理</span>
        </div>
        <div class="quick-item" @click="router.push('/admin/readers')">
          <div class="quick-icon-box q-reader"><el-icon><User /></el-icon></div>
          <span>读者列表</span>
        </div>
        <div class="quick-item" @click="router.push('/admin/seat-reservations')">
          <div class="quick-icon-box q-seat"><el-icon><OfficeBuilding /></el-icon></div>
          <span>座位预约</span>
        </div>
        <div class="quick-item" @click="router.push('/admin/reports')">
          <div class="quick-icon-box q-report"><el-icon><DataAnalysis /></el-icon></div>
          <span>统计报表</span>
        </div>
        <div class="quick-item" @click="router.push('/admin/announcements')">
          <div class="quick-icon-box q-notice"><el-icon><Bell /></el-icon></div>
          <span>发布公告</span>
        </div>
      </div>
    </el-card>

    <!-- 图表与排行榜区域 -->
    <el-row :gutter="18" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header-row">
              <span class="section-title">借阅趋势分析</span>
              <el-tag size="small" type="info" effect="plain">近7天流通数据</el-tag>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header-row">
              <span class="section-title">热门借阅排行榜</span>
              <span class="sub-hint">Top 10</span>
            </div>
          </template>
          <el-table :data="rankList" stripe size="small" max-height="350">
            <el-table-column type="index" label="#" width="45" align="center">
              <template #default="{ $index }">
                <span class="rank-badge" :class="{ 'top-three': $index < 3 }">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="图书名称" show-overflow-tooltip />
            <el-table-column prop="borrowCount" label="借阅频次" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="primary" effect="plain">{{ row.borrowCount || row.count || 0 }} 次</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告区域 -->
    <el-row :gutter="18" class="announcement-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-row">
              <span class="section-title">馆务通知公告</span>
              <el-button link type="primary" size="small" @click="router.push('/admin/announcements')">
                管理公告 →
              </el-button>
            </div>
          </template>
          <el-timeline v-if="announcements.length" class="modern-timeline">
            <el-timeline-item
              v-for="item in announcements"
              :key="item.id"
              :timestamp="item.createTime || item.publishTime"
              placement="top"
              type="primary"
            >
              <div class="announcement-item-box">
                <div class="item-title-row">
                  <h4>{{ item.title }}</h4>
                  <el-tag size="small" :type="item.type === 3 ? 'warning' : 'info'">
                    {{ item.type === 3 ? '闭馆/维修' : (item.type === 2 ? '活动' : '通知') }}
                  </el-tag>
                </div>
                <p class="announcement-content">{{ item.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无公告信息" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Reading, WarningFilled, DocumentAdd, OfficeBuilding, DataAnalysis, Bell } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getBorrowStats, getReaderStats, getCollectionStats, getOverdueStats } from '../../../api/modules/report'
import { getAnnouncements } from '../../../api/modules/system'
import { getBorrowRank } from '../../../api/modules/book'
import { useUserStore } from '../../../stores/user'

const router = useRouter()
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
    // handled
  }
}

const fetchRank = async () => {
  try {
    const res = await getBorrowRank(10)
    rankList.value = res.data || []
  } catch {
    // handled
  }
}

const fetchAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ page: 1, size: 5 })
    announcements.value = res.data?.records || res.data || []
  } catch {
    // handled
  }
}

const initChart = async () => {
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
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 13 },
      shadowColor: 'rgba(0, 0, 0, 0.08)',
      shadowBlur: 10
    },
    grid: {
      left: '2%',
      right: '3%',
      bottom: '3%',
      top: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f0f2f5' } },
      axisLabel: { color: '#909399' }
    },
    series: [
      {
        name: '借阅量',
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbolSize: 6,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.35)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
          ])
        },
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff', borderWidth: 2 },
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
  padding: 4px;
}

.stat-row {
  margin-bottom: 18px;
}

.stat-card {
  border-radius: 12px !important;
  border: 1px solid #ebeef5;
  transition: all 0.25s ease;
  margin-bottom: 12px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08) !important;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 4px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 6px;
}

.stat-value.has-overdue {
  color: #f56c6c;
}

.stat-tip {
  font-size: 12px;
  color: #c0c4cc;
}

.stat-icon-wrapper {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.borrow-bg { background: #ecf5ff; color: #409eff; }
.readers-bg { background: #f0f9eb; color: #67c23a; }
.books-bg { background: #fdf6ec; color: #e6a23c; }
.overdue-bg { background: #fef0f0; color: #f56c6c; }

/* 快捷导航 */
.quick-nav-card {
  border-radius: 12px !important;
  margin-bottom: 18px;
}

.quick-nav-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
}

.quick-nav-grid {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e9edf2;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.quick-item:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
  transform: translateY(-2px);
}

.quick-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.q-borrow { background: #e0edff; color: #409eff; }
.q-book { background: #e8f7e6; color: #67c23a; }
.q-reader { background: #fef3e6; color: #e6a23c; }
.q-seat { background: #f3e8ff; color: #9c27b0; }
.q-report { background: #e6f7ff; color: #00bcd4; }
.q-notice { background: #ffeef0; color: #f56c6c; }

/* 图表与排行 */
.chart-row {
  margin-bottom: 18px;
}

.chart-card {
  border-radius: 12px !important;
  margin-bottom: 12px;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.sub-hint {
  font-size: 12px;
  color: #909399;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.rank-badge {
  display: inline-block;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  background: #f0f2f5;
  color: #909399;
}

.rank-badge.top-three {
  background: #409eff;
  color: #fff;
}

/* 通知时间轴 */
.announcement-row {
  margin-bottom: 12px;
}

.modern-timeline {
  padding-top: 10px;
}

.announcement-item-box {
  background: #f8fafc;
  border: 1px solid #ebeef5;
  padding: 12px 16px;
  border-radius: 8px;
}

.item-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.item-title-row h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.announcement-content {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
</style>
