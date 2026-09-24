<template>
  <div class="news-page-container">
    <!-- 头部横幅 Banner -->
    <div class="news-banner">
      <div class="banner-tag">新闻动态与学术活动</div>
      <h1>馆务资讯与公告中心</h1>
      <p class="banner-desc">
        第一时间获取图书馆最新馆务动态、读者文化活动、学术讲座与开闭馆通知。
      </p>
    </div>

    <!-- 检索与分类工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-inner">
        <!-- 分类切换 -->
        <el-radio-group v-model="selectedType" size="default" @change="handleTypeChange">
          <el-radio-button label="all">全部资讯</el-radio-button>
          <el-radio-button label="1">📢 馆务通知</el-radio-button>
          <el-radio-button label="2">🎉 读者活动</el-radio-button>
          <el-radio-button label="3">⚠️ 闭馆通告</el-radio-button>
        </el-radio-group>

        <!-- 关键词搜索 -->
        <div class="search-input-wrap">
          <el-input
            v-model="keyword"
            placeholder="搜索公告标题或内容..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
            style="width: 260px;"
          />
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
      </div>
    </el-card>

    <!-- 资讯列表 -->
    <div class="news-list-container" v-loading="loading">
      <div v-if="filteredList.length === 0" class="empty-wrap">
        <el-empty description="暂无符合条件的馆务公告" />
      </div>

      <div v-else class="news-cards-grid">
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="news-card-item"
          @click="openDetail(item)"
        >
          <div class="card-top-meta">
            <el-tag :type="tagType(item.type)" size="small" effect="light">
              {{ tagLabel(item.type) }}
            </el-tag>
            <span class="news-date">{{ formatDate(item.publishTime || item.createTime) }}</span>
          </div>

          <h3 class="news-item-title" :title="item.title">{{ item.title }}</h3>
          <p class="news-item-summary">{{ item.content }}</p>

          <div class="news-card-footer">
            <span class="read-more-btn">阅读全文 →</span>
          </div>
        </div>
      </div>

      <!-- 分页条 -->
      <div class="pagination-bar" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[9, 18, 27]"
          layout="total, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="馆务公告详情"
      width="640px"
      append-to-body
      destroy-on-close
    >
      <div v-if="currentNotice" class="dialog-notice-body">
        <div class="dialog-header-row">
          <el-tag :type="tagType(currentNotice.type)" size="default">
            {{ tagLabel(currentNotice.type) }}
          </el-tag>
          <h2 class="dialog-notice-title">{{ currentNotice.title }}</h2>
        </div>
        <div class="dialog-notice-time">
          发布日期：{{ currentNotice.publishTime || currentNotice.createTime || '-' }}
        </div>
        <el-divider style="margin: 16px 0;" />
        <div class="dialog-notice-content">
          {{ currentNotice.content }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="dialogVisible = false">关闭详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getAnnouncements } from '../../api/modules/system'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedType = ref('all')
const keyword = ref('')

const dialogVisible = ref(false)
const currentNotice = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 9
})

const tagType = (t) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[t] || 'info'
}

const tagLabel = (t) => {
  const map = { 1: '馆务通知', 2: '读者活动', 3: '闭馆通告' }
  return map[t] || '通告'
}

const formatDate = (val) => {
  if (!val) return ''
  return String(val).slice(0, 10)
}

const filteredList = computed(() => {
  let res = list.value
  if (selectedType.value !== 'all') {
    res = res.filter(item => String(item.type) === selectedType.value)
  }
  if (keyword.value.trim()) {
    const k = keyword.value.trim().toLowerCase()
    res = res.filter(item => (item.title && item.title.toLowerCase().includes(k)) || (item.content && item.content.toLowerCase().includes(k)))
  }
  return res
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAnnouncements({
      page: pagination.page,
      size: pagination.pageSize
    })
    list.value = res.data.records || res.data.list || res.data || []
    total.value = res.data.total || list.value.length
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleTypeChange = () => {
  // filtered computed handles it
}

const handleSearch = () => {
  // filtered computed handles it
}

const openDetail = (item) => {
  currentNotice.value = item
  dialogVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.news-page-container {
  width: 100%;
}

.news-banner {
  background: linear-gradient(135deg, #091a32 0%, #173868 45%, #1d4ed8 100%);
  border-radius: 18px;
  padding: 44px 32px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.15);
}

.banner-tag {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #93c5fd;
  background: rgba(255, 255, 255, 0.14);
  padding: 3px 12px;
  border-radius: 20px;
  margin-bottom: 12px;
}

.news-banner h1 {
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 10px;
}

.banner-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  max-width: 720px;
  line-height: 1.6;
}

.toolbar-card {
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.news-cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.news-card-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
}

.news-card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.card-top-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.news-date {
  font-size: 12px;
  color: #94a3b8;
}

.news-item-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 10px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.news-item-summary {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.news-card-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.read-more-btn {
  font-size: 13px;
  color: #2563eb;
  font-weight: 600;
}

.pagination-bar {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.dialog-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.dialog-notice-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.dialog-notice-time {
  font-size: 13px;
  color: #94a3b8;
}

.dialog-notice-content {
  font-size: 15px;
  color: #334155;
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 8px 0;
}

@media (max-width: 1024px) {
  .news-cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .news-cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
