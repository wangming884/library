<template>
  <div class="home-container">
    <!-- 搜索栏 -->
    <div class="search-banner">
      <h1>欢迎使用图书馆管理系统</h1>
      <div class="search-box">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="搜索图书名称、作者、ISBN..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 公告轮播 -->
    <el-card class="section-card" v-if="announcements.length">
      <template #header>
        <div class="section-header">
          <el-icon><Bell /></el-icon>
          <span>公告通知</span>
        </div>
      </template>
      <el-carousel height="80px" direction="vertical" :autoplay="true" indicator-position="none">
        <el-carousel-item v-for="item in announcements" :key="item.id">
          <div class="announcement-item">
            <el-tag :type="announcementTagType(item.type)" size="small" style="margin-right: 8px;">
              {{ item.type }}
            </el-tag>
            <span class="title">{{ item.title }}</span>
            <span class="time">{{ item.publishTime }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <el-row :gutter="20">
      <!-- 新书上架 -->
      <el-col :span="16">
        <el-card class="section-card">
          <template #header>
            <div class="section-header">
              <el-icon><Reading /></el-icon>
              <span>新书推荐</span>
            </div>
          </template>
          <el-carousel :interval="4000" type="card" height="260px">
            <el-carousel-item v-for="book in newBooks" :key="book.id">
              <div class="book-card" @click="goBookDetail(book.id)">
                <div class="book-cover">
                  <el-image
                    :src="book.coverUrl"
                    fit="cover"
                    style="width: 120px; height: 160px; border-radius: 4px;"
                  >
                    <template #error>
                      <div class="cover-placeholder">
                        <el-icon size="40"><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="book-info">
                  <div class="book-title">{{ book.title }}</div>
                  <div class="book-author">{{ book.author }}</div>
                  <div class="book-category">{{ book.categoryName }}</div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </el-card>
      </el-col>

      <!-- 借阅排行 -->
      <el-col :span="8">
        <el-card class="section-card">
          <template #header>
            <div class="section-header">
              <el-icon><Trophy /></el-icon>
              <span>借阅排行榜</span>
            </div>
          </template>
          <div class="rank-list">
            <div
              v-for="(item, index) in borrowRank"
              :key="item.id"
              class="rank-item"
              @click="goBookDetail(item.id)"
            >
              <span class="rank-num" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="rank-title" :title="item.title">{{ item.title }}</span>
              <span class="rank-count">{{ item.borrowCount || item.count }}次</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getAnnouncements } from '../../api/modules/system'
import { getNewBooks, getBorrowRank } from '../../api/modules/book'

const router = useRouter()
const keyword = ref('')
const announcements = ref([])
const newBooks = ref([])
const borrowRank = ref([])

const announcementTagType = (type) => {
  const map = { '通知': '', '活动': 'success', '闭馆': 'warning' }
  return map[type] || ''
}

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/portal/search', query: { keyword: keyword.value.trim() } })
  }
}

const goBookDetail = (id) => {
  router.push(`/portal/book/${id}`)
}

const fetchAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ pageSize: 10 })
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
  max-width: 1200px;
  margin: 0 auto;
}
.search-banner {
  text-align: center;
  padding: 40px 0 30px;
  background: linear-gradient(135deg, #409EFF 0%, #79bbff 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  color: #fff;
}
.search-banner h1 {
  font-size: 28px;
  margin-bottom: 20px;
  font-weight: 500;
}
.search-box {
  max-width: 600px;
  margin: 0 auto;
}
.section-card {
  margin-bottom: 20px;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}
.announcement-item {
  display: flex;
  align-items: center;
  height: 80px;
  padding: 0 20px;
}
.announcement-item .title {
  flex: 1;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.announcement-item .time {
  color: #909399;
  font-size: 13px;
  margin-left: 12px;
}
.book-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  height: 100%;
  box-sizing: border-box;
}
.book-card:hover {
  background: #ecf5ff;
}
.cover-placeholder {
  width: 120px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  border-radius: 4px;
  color: #c0c4cc;
}
.book-info {
  flex: 1;
}
.book-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.book-author {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}
.book-category {
  font-size: 13px;
  color: #909399;
}
.rank-list {
  max-height: 400px;
  overflow-y: auto;
}
.rank-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.rank-item:hover {
  background: #f5f7fa;
}
.rank-item:last-child {
  border-bottom: none;
}
.rank-num {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: #f0f2f5;
  color: #909399;
  font-size: 13px;
  font-weight: bold;
  margin-right: 12px;
  flex-shrink: 0;
}
.rank-num.top {
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
  color: #fff;
}
.rank-title {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-count {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
}
</style>
