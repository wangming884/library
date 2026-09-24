<template>
  <div class="book-detail-container" v-loading="loading">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-bar" v-if="book">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/portal/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/portal/search' }">图书检索</el-breadcrumb-item>
        <el-breadcrumb-item>{{ book.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 图书主体卡片 -->
    <el-card shadow="never" class="main-book-card" v-if="book">
      <el-row :gutter="36">
        <!-- 封面与交互按钮 -->
        <el-col :xs="24" :md="8" :lg="7" class="cover-col">
          <div class="detail-cover-box">
            <el-image
              :src="book.cover"
              fit="cover"
              class="detail-cover-img"
              :preview-src-list="book.cover ? [book.cover] : []"
              preview-teleported
            >
              <template #error>
                <div class="cover-placeholder-large">
                  <el-icon size="64"><Picture /></el-icon>
                  <span>暂无封面图片</span>
                </div>
              </template>
            </el-image>
            <div class="rare-badge-tag" v-if="book.isRare">
              <el-tag type="danger" effect="dark" size="small">珍贵古籍 / 特藏</el-tag>
            </div>
          </div>

          <!-- 收藏与分享小工具 -->
          <div class="action-tools">
            <el-button
              :type="isFavorited ? 'danger' : 'default'"
              :icon="isFavorited ? StarFilled : Star"
              plain
              class="tool-act-btn"
              @click="toggleFavorite"
            >
              {{ isFavorited ? '已加入心愿单' : '加入心愿单' }}
            </el-button>
            <el-button
              :icon="Share"
              plain
              class="tool-act-btn"
              @click="handleShare"
            >
              分享图书
            </el-button>
          </div>
        </el-col>

        <!-- 详细信息与借阅面板 -->
        <el-col :xs="24" :md="16" :lg="17" class="info-col">
          <div class="book-header-title">
            <h1 class="book-title">{{ book.title }}</h1>
            <p class="book-subtitle" v-if="book.author">著作者：{{ book.author }}</p>
          </div>

          <!-- 评分指示牌 -->
          <div class="rating-strip">
            <el-rate :model-value="averageRating" disabled allow-half score-template="{value}" />
            <span class="rating-score">{{ averageRating.toFixed(1) }} 分</span>
            <span class="divider">•</span>
            <span class="review-count">{{ reviewPagination.total }} 条读者书评</span>
          </div>

          <!-- 元数据详情表格 -->
          <el-descriptions :column="2" border size="default" class="meta-desc">
            <el-descriptions-item label="国际标准书号 (ISBN)">{{ book.isbn || '-' }}</el-descriptions-item>
            <el-descriptions-item label="图书分类">{{ book.categoryName || '普通类' }}</el-descriptions-item>
            <el-descriptions-item label="出版单位">{{ book.publisher || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ book.pubDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="馆藏参考定价">
              <span class="price-val">{{ book.price ? `¥ ${Number(book.price).toFixed(2)}` : '免费借阅' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="馆藏在架状态">
              <div class="inventory-status">
                <el-tag :type="book.availableCount > 0 ? 'success' : 'danger'" size="default" effect="light">
                  {{ book.availableCount > 0 ? `可借 ${book.availableCount} 册` : '全部借出' }}
                </el-tag>
                <span class="total-span">（全馆共有 {{ book.totalCount || 0 }} 册复本）</span>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 内容简介 -->
          <div class="book-desc-section" v-if="book.description">
            <h3 class="section-subheading">内容提要与简介</h3>
            <p class="desc-paragraph">{{ book.description }}</p>
          </div>

          <!-- 核心操作按键 -->
          <div class="borrow-action-card">
            <div class="action-note">
              <span v-if="book.availableCount > 0" class="note-green">
                ✓ 当前馆藏充足，支持读者在线登记或凭证到馆自助机借还。
              </span>
              <span v-else class="note-orange">
                ⚠ 当前复本已全数借出，您可以提交预约，到书后系统将优先为您保留 3 天。
              </span>
            </div>

            <div class="action-btn-group">
              <el-button
                v-if="book.availableCount > 0"
                type="primary"
                size="large"
                class="main-act-btn"
                :loading="borrowing"
                @click="handleBorrow"
              >
                <el-icon><Collection /></el-icon> 立即借阅此书
              </el-button>
              <el-button
                v-else
                type="warning"
                size="large"
                class="main-act-btn"
                :loading="reserving"
                @click="handleReserve"
              >
                <el-icon><Clock /></el-icon> 预约排队
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 可借复本在架位置指引 -->
    <el-card shadow="never" class="sub-card copies-card" v-if="book && book.availableCount > 0">
      <template #header>
        <div class="card-header-line">
          <div class="card-header-title">
            <el-icon color="#409EFF"><Location /></el-icon>
            <span>在馆在架复本位置指引</span>
          </div>
          <el-tag size="small" type="success">按条码与架位索书</el-tag>
        </div>
      </template>

      <el-table :data="availableCopies" v-loading="copyLoading" stripe border>
        <el-table-column prop="id" label="复本编号" width="95" align="center" />
        <el-table-column prop="barcode" label="条码号 (Barcode)" width="160" />
        <el-table-column prop="location" label="所属书库/阅览室" min-width="160" />
        <el-table-column prop="floor" label="楼层" width="100" align="center" />
        <el-table-column prop="shelf" label="具体排架号" width="140" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.shelf || '标准架位' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default>
            <el-tag size="small" type="success">可借出</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!copyLoading && availableCopies.length === 0" description="暂无可借复本明细" />
    </el-card>

    <!-- 读者书评与交流区 -->
    <el-card shadow="never" class="sub-card reviews-card" v-if="book">
      <template #header>
        <div class="card-header-line">
          <div class="card-header-title">
            <el-icon color="#E6A23C"><ChatDotRound /></el-icon>
            <span>读者书评与心得 ({{ reviewPagination.total }})</span>
          </div>
        </div>
      </template>

      <!-- 发表新书评 Form -->
      <div class="review-compose-box">
        <el-form class="review-form" :model="reviewForm">
          <div class="compose-header">
            <span class="rate-prompt">给这本书打个分：</span>
            <el-rate v-model="reviewForm.rating" show-text />
          </div>
          <el-form-item>
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="分享您的读书感悟，或给其他读者提供借阅参考..."
            />
          </el-form-item>
          <div class="compose-actions">
            <el-button type="primary" :loading="reviewSubmitting" @click="handleSubmitReview">
              发布书评
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 书评列表 -->
      <div class="review-stream" v-loading="reviewLoading">
        <div v-for="item in reviews" :key="item.id" class="review-card-item">
          <div class="review-user-avatar">
            <el-avatar :size="38" style="background: #409EFF;">
              {{ (item.readerName || '读')[0] }}
            </el-avatar>
          </div>
          <div class="review-body">
            <div class="review-meta-line">
              <span class="review-reader-name">{{ item.readerName || '读者用户' }}</span>
              <el-rate :model-value="item.rating || 5" disabled size="small" />
              <span class="review-timestamp">{{ item.createTime }}</span>
            </div>
            <p class="review-text-content">{{ item.content }}</p>
          </div>
        </div>
        <el-empty v-if="!reviewLoading && reviews.length === 0" description="暂无书评，欢迎成为第一位点评人！" />
      </div>

      <el-pagination
        v-if="reviewPagination.total > reviewPagination.pageSize"
        class="pagination"
        v-model:current-page="reviewPagination.page"
        v-model:page-size="reviewPagination.pageSize"
        :total="reviewPagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchReviews"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Collection, Clock, Location, ChatDotRound, Star, StarFilled, Share } from '@element-plus/icons-vue'
import { addBookReview, getAvailableCopies, getBook, getBookReviews } from '../../api/modules/book'
import { readerBorrowBook, reserveBook } from '../../api/modules/borrow'

const route = useRoute()
const loading = ref(false)
const copyLoading = ref(false)
const borrowing = ref(false)
const reserving = ref(false)
const isFavorited = ref(false)

const book = ref(null)
const availableCopies = ref([])
const reviewLoading = ref(false)
const reviewSubmitting = ref(false)
const reviews = ref([])
const reviewPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const reviewForm = reactive({ rating: 5, content: '' })

// 计算平均评分
const averageRating = computed(() => {
  if (!reviews.value.length) return 5.0
  const sum = reviews.value.reduce((acc, cur) => acc + (cur.rating || 5), 0)
  return sum / reviews.value.length
})

const fetchBook = async () => {
  loading.value = true
  try {
    const res = await getBook(route.params.id)
    book.value = res.data
    checkFavoriteStatus()
    fetchAvailableCopies()
    fetchReviews()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const fetchReviews = async () => {
  reviewLoading.value = true
  try {
    const res = await getBookReviews(route.params.id, {
      page: reviewPagination.page,
      size: reviewPagination.pageSize
    })
    reviews.value = res.data?.records || res.data?.list || res.data || []
    reviewPagination.total = res.data?.total || reviews.value.length
  } catch {
    reviews.value = []
  } finally {
    reviewLoading.value = false
  }
}

const fetchAvailableCopies = async () => {
  copyLoading.value = true
  try {
    const res = await getAvailableCopies(route.params.id)
    availableCopies.value = res.data || []
  } catch {
    availableCopies.value = []
  } finally {
    copyLoading.value = false
  }
}

const handleBorrow = async () => {
  try {
    await ElMessageBox.confirm(`确认借阅《${book.value.title}》？借出后请在借阅期限内归还。`, '借阅办理确认', {
      type: 'info',
      confirmButtonText: '确认借阅',
      cancelButtonText: '取消'
    })
    borrowing.value = true
    await readerBorrowBook({ bookId: book.value.id })
    ElMessage.success('借阅申请已办理成功！')
    fetchBook()
  } catch {
    // handled
  } finally {
    borrowing.value = false
  }
}

const handleReserve = async () => {
  try {
    await ElMessageBox.confirm(`当前图书无在架复本，是否预约排队借阅《${book.value.title}》？`, '预约确认', {
      type: 'warning',
      confirmButtonText: '确认预约',
      cancelButtonText: '取消'
    })
    reserving.value = true
    await reserveBook({ bookId: book.value.id })
    ElMessage.success('预约登记成功！到书后将通知您')
    fetchBook()
  } catch {
    // handled
  } finally {
    reserving.value = false
  }
}

const handleSubmitReview = async () => {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请写下您的评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await addBookReview(route.params.id, {
      rating: reviewForm.rating,
      content: reviewForm.content.trim()
    })
    ElMessage.success('书评发表成功')
    reviewForm.content = ''
    fetchReviews()
  } catch {
    // handled
  } finally {
    reviewSubmitting.value = false
  }
}

// 收藏到心愿单
const checkFavoriteStatus = () => {
  try {
    const list = JSON.parse(localStorage.getItem('my_favorite_books') || '[]')
    isFavorited.value = list.includes(Number(route.params.id))
  } catch {
    isFavorited.value = false
  }
}

const toggleFavorite = () => {
  try {
    let list = JSON.parse(localStorage.getItem('my_favorite_books') || '[]')
    const bookId = Number(route.params.id)
    if (isFavorited.value) {
      list = list.filter(id => id !== bookId)
      isFavorited.value = false
      ElMessage.info('已从心愿单移出')
    } else {
      list.push(bookId)
      isFavorited.value = true
      ElMessage.success('已加入我的心愿单')
    }
    localStorage.setItem('my_favorite_books', JSON.stringify(list))
  } catch {
    // handled
  }
}

// 分享图书
const handleShare = async () => {
  const shareText = `【图书馆藏书推荐】《${book.value.title}》 作者：${book.value.author || '未知'}，ISBN：${book.value.isbn || '无'}。详情可登录图书馆系统查看：${window.location.href}`
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(shareText)
      ElMessage.success('图书信息与链接已复制到剪贴板，快去分享给同学吧！')
    } else {
      ElMessage.success('图书信息已准备：' + book.value.title)
    }
  } catch {
    ElMessage.info('图书信息：' + book.value.title)
  }
}

onMounted(() => {
  fetchBook()
})
</script>

<style scoped>
.book-detail-container {
  width: 100%;
}

.breadcrumb-bar {
  margin-bottom: 16px;
  font-size: 14px;
}

.main-book-card {
  border-radius: 14px;
  padding: 10px;
  margin-bottom: 24px;
}

.cover-col {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.detail-cover-box {
  position: relative;
  width: 100%;
  max-width: 280px;
  height: 380px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
  margin-bottom: 16px;
}

.detail-cover-img {
  width: 100%;
  height: 100%;
}

.rare-badge-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.cover-placeholder-large {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 10px;
}

.action-tools {
  display: flex;
  gap: 12px;
  width: 100%;
  max-width: 280px;
}

.tool-act-btn {
  flex: 1;
}

.info-col {
  display: flex;
  flex-direction: column;
}

.book-header-title {
  margin-bottom: 12px;
}

.book-title {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
}

.book-subtitle {
  margin: 0;
  font-size: 15px;
  color: #6b7280;
}

.rating-strip {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.rating-score {
  font-weight: 700;
  color: #f59e0b;
  font-size: 15px;
}

.divider {
  color: #d1d5db;
}

.review-count {
  font-size: 13px;
  color: #9ca3af;
}

.meta-desc {
  margin-bottom: 20px;
}

.price-val {
  font-weight: 700;
  color: #ef4444;
}

.inventory-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.total-span {
  font-size: 12px;
  color: #9ca3af;
}

.book-desc-section {
  background: #f9fafb;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.section-subheading {
  margin: 0 0 8px;
  font-size: 15px;
  color: #374151;
}

.desc-paragraph {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
}

.borrow-action-card {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.action-note {
  margin-bottom: 14px;
  font-size: 13px;
}

.note-green { color: #16a34a; }
.note-orange { color: #d97706; }

.main-act-btn {
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 600;
}

/* 子卡片通用 */
.sub-card {
  border-radius: 12px;
  margin-bottom: 24px;
}

.card-header-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

/* 书评区 */
.review-compose-box {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  padding: 18px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.compose-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.rate-prompt {
  font-size: 14px;
  color: #4b5563;
}

.compose-actions {
  display: flex;
  justify-content: flex-end;
}

.review-stream {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: #fff;
  border: 1px solid #f0f2f5;
  border-radius: 10px;
  transition: all 0.2s;
}

.review-card-item:hover {
  background: #fafafa;
}

.review-body {
  flex: 1;
}

.review-meta-line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.review-reader-name {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.review-timestamp {
  font-size: 12px;
  color: #9ca3af;
  margin-left: auto;
}

.review-text-content {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
