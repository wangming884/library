<template>
  <div class="home-container">
    <!-- 头部搜索 Hero Banner -->
    <div class="search-banner">
      <div class="banner-badge">智慧文献与空间服务平台</div>
      <h1>让每一次阅读，都触手可及</h1>
      <p class="banner-sub">海量馆藏图书检索 · 在线自习室选座 · 便捷借阅与续借</p>
      
      <div class="search-box">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="搜索图书名称、作者、ISBN 或主题关键词..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          class="banner-search-input"
        >
          <template #append>
            <el-button type="primary" class="banner-search-btn" @click="handleSearch">
              <el-icon><Search /></el-icon>
              <span>立即检索</span>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 热门搜索推荐词 -->
      <div class="hot-search-row">
        <span class="hot-label">热门搜索：</span>
        <span
          v-for="item in hotTags"
          :key="item"
          class="hot-tag"
          @click="searchHotTag(item)"
        >
          {{ item }}
        </span>
      </div>
    </div>

    <!-- 快捷服务捷径卡片 -->
    <div class="service-shortcuts">
      <div class="shortcut-card" @click="router.push('/portal/search')">
        <div class="shortcut-icon search-bg"><el-icon><Search /></el-icon></div>
        <div class="shortcut-info">
          <h4>馆藏检索</h4>
          <p>全馆图书分类精准查询</p>
        </div>
      </div>

      <div class="shortcut-card" @click="router.push('/portal/seat')">
        <div class="shortcut-icon seat-bg"><el-icon><OfficeBuilding /></el-icon></div>
        <div class="shortcut-info">
          <h4>座位预约</h4>
          <p>自习室/研讨室在线预订</p>
        </div>
      </div>

      <div class="shortcut-card" @click="router.push('/portal/center')">
        <div class="shortcut-icon borrow-bg"><el-icon><Reading /></el-icon></div>
        <div class="shortcut-info">
          <h4>我的借还</h4>
          <p>查看借阅进度与一键续借</p>
        </div>
      </div>

      <div class="shortcut-card" @click="router.push('/portal/feedback')">
        <div class="shortcut-icon feedback-bg"><el-icon><ChatDotRound /></el-icon></div>
        <div class="shortcut-info">
          <h4>留言反馈</h4>
          <p>读者心声与荐购需求</p>
        </div>
      </div>
    </div>

    <!-- 公告轮播 -->
    <el-card shadow="never" class="announcement-banner-card" v-if="announcements.length">
      <div class="announcement-flex">
        <div class="announcement-lead">
          <el-icon size="18" color="#409EFF"><Bell /></el-icon>
          <span>馆务动态：</span>
        </div>
        <el-carousel height="38px" direction="vertical" :autoplay="true" indicator-position="none" class="announcement-slider">
          <el-carousel-item v-for="item in announcements" :key="item.id">
            <div class="announcement-item" @click="openAnnouncement(item)" style="cursor: pointer;">
              <el-tag :type="announcementTagType(item.type)" size="small" effect="light" style="margin-right: 10px;">
                {{ announcementTypeLabel(item.type) }}
              </el-tag>
              <span class="announcement-title" :title="item.title">{{ item.title }}</span>
              <span class="announcement-time">{{ item.publishTime || item.createTime }}</span>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </el-card>

    <!-- 核心板块：新书推荐与借阅排行榜 -->
    <el-row :gutter="20">
      <!-- 新书推荐 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-header">
              <div class="section-title">
                <el-icon color="#409EFF"><Reading /></el-icon>
                <span>新书推荐入藏</span>
              </div>
              <el-button link type="primary" size="small" @click="router.push('/portal/search')">
                查看更多图书 →
              </el-button>
            </div>
          </template>

          <el-carousel :interval="4500" type="card" height="270px" class="books-carousel" v-if="newBooks.length">
            <el-carousel-item v-for="book in newBooks" :key="book.id">
              <div class="book-card" @click="goBookDetail(book.id)">
                <div class="book-cover">
                  <el-image
                    :src="book.cover"
                    fit="cover"
                    class="book-img"
                  >
                    <template #error>
                      <div class="cover-placeholder">
                        <el-icon size="36"><Picture /></el-icon>
                        <span>暂无封面</span>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="book-info">
                  <h4 class="book-title" :title="book.title">{{ book.title }}</h4>
                  <p class="book-author" :title="book.author">{{ book.author || '未知作者' }}</p>
                  <el-tag size="small" type="info" effect="plain" class="book-category">{{ book.categoryName || '图书' }}</el-tag>
                  <div class="book-action-row">
                    <span class="view-detail-btn">借阅详情 →</span>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="暂无新书上架" />
        </el-card>
      </el-col>

      <!-- 借阅排行 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-header">
              <div class="section-title">
                <el-icon color="#E6A23C"><Trophy /></el-icon>
                <span>借阅风云榜</span>
              </div>
              <span class="sub-hint">热门图书排行</span>
            </div>
          </template>

          <div class="rank-list" v-if="borrowRank.length">
            <div
              v-for="(item, index) in borrowRank"
              :key="item.id"
              class="rank-item"
              @click="goBookDetail(item.id)"
            >
              <span class="rank-num" :class="{ 'top-first': index === 0, 'top-second': index === 1, 'top-third': index === 2 }">
                {{ index + 1 }}
              </span>
              <div class="rank-info">
                <span class="rank-title" :title="item.title">{{ item.title }}</span>
                <span class="rank-author">{{ item.author || '' }}</span>
              </div>
              <el-tag size="small" type="primary" effect="light" class="rank-badge">
                {{ item.borrowCount || item.count || 0 }} 次
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无排行数据" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告详情查看弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      title="馆务公告详情"
      width="560px"
      append-to-body
      destroy-on-close
    >
      <div v-if="currentAnnouncement" class="announcement-modal-body">
        <div class="modal-title-row">
          <el-tag :type="announcementTagType(currentAnnouncement.type)" size="small">
            {{ announcementTypeLabel(currentAnnouncement.type) }}
          </el-tag>
          <h3 class="modal-announcement-title">{{ currentAnnouncement.title }}</h3>
        </div>
        <div class="modal-announcement-time">
          发布时间：{{ currentAnnouncement.publishTime || currentAnnouncement.createTime || '-' }}
        </div>
        <el-divider style="margin: 14px 0;" />
        <div class="modal-announcement-content">
          {{ currentAnnouncement.content }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="announcementDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Reading, Bell, OfficeBuilding, ChatDotRound, Trophy, Picture } from '@element-plus/icons-vue'
import { getAnnouncements } from '../../api/modules/system'
import { getNewBooks, getBorrowRank } from '../../api/modules/book'

const router = useRouter()
const keyword = ref('')
const announcements = ref([])
const newBooks = ref([])
const borrowRank = ref([])


const announcementDialogVisible = ref(false)
const currentAnnouncement = ref(null)

const openAnnouncement = (item) => {
  currentAnnouncement.value = item
  announcementDialogVisible.value = true
}

const hotTags = ['人工智能', '计算机科学', '心理学', '三国演义', '经济学', '文学经典']

const announcementTagType = (type) => {
  const map = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

const announcementTypeLabel = (type) => {
  const map = { 1: '通知', 2: '活动', 3: '闭馆' }
  return map[type] || '通知'
}

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/portal/search', query: { keyword: keyword.value.trim() } })
  } else {
    router.push('/portal/search')
  }
}

const searchHotTag = (tag) => {
  keyword.value = tag
  handleSearch()
}

const goBookDetail = (id) => {
  router.push(`/portal/book/${id}`)
}

const fetchAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ size: 10 })
    announcements.value = res.data.records || res.data.list || res.data || []
  } catch {
    // handled
  }
}

const fetchNewBooks = async () => {
  try {
    const res = await getNewBooks(8)
    newBooks.value = res.data || []
  } catch {
    // handled
  }
}

const fetchBorrowRank = async () => {
  try {
    const res = await getBorrowRank(10)
    borrowRank.value = res.data || []
  } catch {
    // handled
  }
}

onMounted(() => {
  fetchAnnouncements()
  fetchNewBooks()
  fetchBorrowRank()
})
</script>

<style scoped>
.home-container {
  width: 100%;
}

.search-banner {
  text-align: center;
  padding: 48px 24px 38px;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  color: #fff;
  box-shadow: 0 10px 25px rgba(37, 99, 235, 0.2);
}

.banner-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 1px;
  margin-bottom: 14px;
}

.search-banner h1 {
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.banner-sub {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 26px;
}

.search-box {
  max-width: 660px;
  margin: 0 auto 16px;
}

.banner-search-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 6px 14px;
}

.banner-search-btn {
  border-radius: 0 10px 10px 0 !important;
  font-weight: 600;
  padding: 0 20px;
}

.hot-search-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  flex-wrap: wrap;
}

.hot-label {
  color: rgba(255, 255, 255, 0.75);
}

.hot-tag {
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  padding: 2px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.hot-tag:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-1px);
}

/* 快捷卡片 */
.service-shortcuts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.shortcut-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.22s ease;
}

.shortcut-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.06);
  border-color: #dcdfe6;
}

.shortcut-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.search-bg { background: #e0f2fe; color: #0284c7; }
.seat-bg { background: #fef3c7; color: #d97706; }
.borrow-bg { background: #dcfce7; color: #16a34a; }
.feedback-bg { background: #f3e8ff; color: #9333ea; }

.shortcut-info h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #1f2937;
}

.shortcut-info p {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

/* 公告轮播条 */
.announcement-banner-card {
  border-radius: 10px;
  margin-bottom: 24px;
  padding: 4px 14px;
}

.announcement-flex {
  display: flex;
  align-items: center;
}

.announcement-lead {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}

.announcement-slider {
  flex: 1;
}

.announcement-item {
  display: flex;
  align-items: center;
  height: 38px;
}

.announcement-title {
  flex: 1;
  font-size: 13px;
  color: #4b5563;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-time {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 12px;
}

/* 核心板块 */
.section-card {
  border-radius: 12px !important;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.sub-hint {
  font-size: 12px;
  color: #9ca3af;
}

.books-carousel {
  margin-top: 8px;
}

.book-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  height: 100%;
  box-sizing: border-box;
  transition: all 0.22s;
}

.book-card:hover {
  background: #f0f7ff;
  border-color: #bfdbfe;
}

.book-cover {
  width: 110px;
  height: 150px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.book-img {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: #f3f4f6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 11px;
  gap: 4px;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 8px;
}

.book-category {
  align-self: flex-start;
  margin-bottom: 12px;
}

.view-detail-btn {
  font-size: 12px;
  color: #2563eb;
  font-weight: 500;
}

/* 排行榜 */
.rank-list {
  padding-top: 4px;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.rank-item:hover {
  background: #f3f4f6;
}

.rank-num {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  background: #f3f4f6;
  color: #6b7280;
  margin-right: 12px;
  flex-shrink: 0;
}

.rank-num.top-first { background: #f59e0b; color: #fff; }
.rank-num.top-second { background: #94a3b8; color: #fff; }
.rank-num.top-third { background: #d97706; color: #fff; }

.rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-right: 10px;
}

.rank-title {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-author {
  font-size: 11px;
  color: #9ca3af;
}

@media (max-width: 900px) {
  .service-shortcuts {
    grid-template-columns: repeat(2, 1fr);
  }
}

.announcement-modal-body {
  padding: 4px;
}
.modal-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.modal-announcement-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
}
.modal-announcement-time {
  font-size: 12px;
  color: #9ca3af;
}
.modal-announcement-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
