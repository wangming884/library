<template>
  <div class="home-container">
    <!-- 1. 全景 Hero 检索区 -->
    <section class="hero-section">
      <div class="hero-bg-orb orb-1"></div>
      <div class="hero-bg-orb orb-2"></div>
      <div class="hero-content">
        <!-- 实时问候与开馆状态条 -->
        <div class="hero-status-pill">
          <span class="status-pulse-dot"></span>
          <span class="greeting-text">{{ timeGreeting }}</span>
          <span class="status-divider">·</span>
          <span class="status-text">今日正常开馆：08:00 - 22:00</span>
          <span class="status-divider">|</span>
          <span class="status-highlight">
            <el-icon><Location /></el-icon> 智慧文献中心 · 数字化自习空间
          </span>
        </div>

        <h1 class="hero-title">探索知识之海，让每一次阅读都触手可及</h1>
        <p class="hero-subtitle">全馆纸电一体化文献检索 · 空间自习室在线选座 · 读者借还全周期智能服务</p>

        <!-- 聚合智能搜索栏 -->
        <div class="smart-search-wrapper">
          <div class="search-type-prefix">
            <el-select v-model="searchType" class="search-type-select" size="large">
              <el-option label="📚 全部馆藏" value="all" />
              <el-option label="📖 图书书名" value="title" />
              <el-option label="✍️ 著作作者" value="author" />
              <el-option label="🏷️ 标准 ISBN" value="isbn" />
            </el-select>
          </div>
          <el-input
            v-model="keyword"
            size="large"
            :placeholder="searchPlaceholder"
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
            class="hero-search-input"
          />
          <el-button type="primary" size="large" class="hero-search-button" @click="handleSearch">
            <el-icon><Search /></el-icon>
            <span>智能检索</span>
          </el-button>
        </div>

        <!-- 热门搜索推荐词 -->
        <div class="hot-search-tags">
          <span class="hot-title"><el-icon><Trophy /></el-icon> 热门检索：</span>
          <button
            v-for="tag in hotTags"
            :key="tag"
            type="button"
            class="hot-chip"
            @click="searchHotTag(tag)"
          >
            {{ tag }}
          </button>
        </div>

        <!-- 实时馆情数据概览栏 -->
        <div class="hero-metrics-bar">
          <div class="metric-item">
            <div class="metric-num-wrap">
              <span class="metric-num">50,000</span><span class="metric-unit">+</span>
            </div>
            <span class="metric-label">纸电馆藏图书</span>
          </div>
          <div class="metric-sep"></div>
          <div class="metric-item">
            <div class="metric-num-wrap">
              <span class="metric-num">356</span><span class="metric-unit">席</span>
            </div>
            <span class="metric-label">自习研讨座位</span>
          </div>
          <div class="metric-sep"></div>
          <div class="metric-item">
            <div class="metric-num-wrap">
              <span class="metric-num">12,800</span><span class="metric-unit">+</span>
            </div>
            <span class="metric-label">借阅流通人次</span>
          </div>
          <div class="metric-sep"></div>
          <div class="metric-item">
            <div class="metric-num-wrap">
              <span class="metric-num">99.2</span><span class="metric-unit">%</span>
            </div>
            <span class="metric-label">读者好评指数</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. 六大核心服务快捷导航网格（分流至各独立专页） -->
    <section class="services-section">
      <div class="section-title-wrap">
        <span class="section-badge">一站式服务</span>
        <h2 class="section-main-heading">智慧读者服务大厅</h2>
        <p class="section-sub-heading">各功能已拆分为独立专注页面，点击卡片即刻直达对应专属服务</p>
      </div>

      <div class="services-grid">
        <div class="service-box service-box-blue" @click="router.push('/portal/search')">
          <div class="service-icon-wrap icon-blue">
            <el-icon><Search /></el-icon>
          </div>
          <div class="service-text">
            <h3>馆藏图书检索</h3>
            <p>全馆图书分类快速查询，在馆副本与索书号即时定位</p>
          </div>
          <span class="service-arrow">→</span>
        </div>

        <div class="service-box service-box-amber" @click="router.push('/portal/rank')">
          <div class="service-icon-wrap icon-amber">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="service-text">
            <h3>文献榜单精选</h3>
            <p>热门借阅风云榜、金银铜榜首领奖台与最新入藏典籍</p>
          </div>
          <span class="service-arrow">→</span>
        </div>

        <div class="service-box service-box-emerald" @click="router.push('/portal/seat')">
          <div class="service-icon-wrap icon-emerald">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="service-text">
            <h3>自习选座空间</h3>
            <p>考研自习区与静音阅览室，平面图实时选座智能签到</p>
          </div>
          <span class="service-arrow">→</span>
        </div>

        <div class="service-box service-box-purple" @click="router.push('/portal/news')">
          <div class="service-icon-wrap icon-purple">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="service-text">
            <h3>馆务资讯中心</h3>
            <p>馆务动态、学术活动讲座通告与节假日开闭馆公告</p>
          </div>
          <span class="service-arrow">→</span>
        </div>

        <div class="service-box service-box-cyan" @click="router.push('/portal/guide')">
          <div class="service-icon-wrap icon-cyan">
            <el-icon><Guide /></el-icon>
          </div>
          <div class="service-text">
            <h3>入馆服务指南</h3>
            <p>读者权限、借期规则、超期违约金政策与1F~4F空间导览</p>
          </div>
          <span class="service-arrow">→</span>
        </div>

        <div class="service-box service-box-indigo" @click="router.push('/portal/center')">
          <div class="service-icon-wrap icon-indigo">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="service-text">
            <h3>个人借阅中心</h3>
            <p>当前在借图书到期提醒，逾期预警与一键极速线上续借</p>
          </div>
          <span class="service-arrow">→</span>
        </div>
      </div>
    </section>

    <!-- 3. 今日焦点速递：新书精选 & 馆务要闻（清爽双列，各链向独立专页） -->
    <section class="highlights-section">
      <el-row :gutter="24">
        <!-- 左侧：新书速递 -->
        <el-col :xs="24" :lg="14">
          <div class="panel-card">
            <div class="panel-header">
              <div class="panel-title">
                <el-icon color="#2563eb"><Reading /></el-icon>
                <span>最新入藏精选新书</span>
              </div>
              <router-link to="/portal/rank" class="more-link">
                查看完整榜单专页 →
              </router-link>
            </div>

            <div class="home-books-grid" v-if="newBooks.length">
              <div
                v-for="book in newBooks.slice(0, 4)"
                :key="book.id"
                class="mini-book-item"
                @click="goBookDetail(book.id)"
              >
                <div class="mini-cover-wrap">
                  <el-image :src="book.cover" fit="cover" class="mini-cover-img" lazy>
                    <template #error>
                      <div class="mini-mock"><el-icon><Reading /></el-icon></div>
                    </template>
                  </el-image>
                </div>
                <div class="mini-book-info">
                  <h4 class="mini-title" :title="book.title">{{ book.title }}</h4>
                  <p class="mini-author">{{ book.author || '未知作者' }}</p>
                  <el-tag size="small" effect="plain" type="info">{{ book.categoryName || '文献' }}</el-tag>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无新书入藏" :image-size="60" />
          </div>
        </el-col>

        <!-- 右侧：馆务快讯 -->
        <el-col :xs="24" :lg="10">
          <div class="panel-card">
            <div class="panel-header">
              <div class="panel-title">
                <el-icon color="#f59e0b"><Bell /></el-icon>
                <span>馆务动态与公告</span>
              </div>
              <router-link to="/portal/news" class="more-link">
                查看全部资讯专页 →
              </router-link>
            </div>

            <div class="home-news-list" v-if="announcements.length">
              <div
                v-for="item in announcements.slice(0, 4)"
                :key="item.id"
                class="home-news-row"
                @click="router.push('/portal/news')"
              >
                <el-tag :type="announcementTagType(item.type)" size="small" effect="light">
                  {{ announcementTypeLabel(item.type) }}
                </el-tag>
                <span class="home-news-title" :title="item.title">{{ item.title }}</span>
                <span class="home-news-date">{{ formatDate(item.publishTime || item.createTime) }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无公告动态" :image-size="60" />
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 4. 每日书香寄语小条 -->
    <section class="quote-strip-section">
      <div class="quote-strip">
        <div class="quote-content">
          <span class="quote-icon">“</span>
          <span class="quote-body-text">{{ currentQuote.text }}</span>
          <span class="quote-from">—— {{ currentQuote.author }} 《{{ currentQuote.source }}》</span>
        </div>
        <el-button link type="primary" size="small" @click="nextQuote">换一句 ↻</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  Reading,
  Bell,
  OfficeBuilding,
  Trophy,
  Location,
  Guide
} from '@element-plus/icons-vue'
import { getAnnouncements } from '../../api/modules/system'
import { getNewBooks } from '../../api/modules/book'

const router = useRouter()

// 动态问候语
const timeGreeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 11) return '早安，求知者 🌅'
  if (hour >= 11 && hour < 14) return '午安，阅读者 ☀️'
  if (hour >= 14 && hour < 18) return '下午好，学海泛舟人 ☕'
  if (hour >= 18 && hour < 23) return '晚上好，夜读者 🌙'
  return '夜深了，注意休息 ✨'
})

// 搜索状态
const keyword = ref('')
const searchType = ref('all')
const hotTags = ['人工智能', '计算机科学', '心理学', '三体', '经济学原理', '明朝那些事儿', '文学经典']

const searchPlaceholder = computed(() => {
  const map = {
    all: '输入书名、作者、ISBN 或主题关键词检索...',
    title: '请输入图书全称或书名关键词...',
    author: '请输入著者、编者或译者姓名...',
    isbn: '请输入 10 位或 13 位标准 ISBN 号...'
  }
  return map[searchType.value] || '搜索图书...'
})

// 核心数据
const announcements = ref([])
const newBooks = ref([])

// 读书名言轮换
const quotes = [
  { text: '书籍是人类进步的阶梯。', author: '高尔基', source: '童年' },
  { text: '读一本好书，就是在和许多高尚的人谈话。', author: '笛卡尔', source: '谈方法' },
  { text: '立身以立学为先，立学以读书为本。', author: '欧阳修', source: '示弟子' },
  { text: '读书破万卷，下笔如有神。', author: '杜甫', source: '奉赠韦左丞丈二十二韵' },
  { text: '天堂应该是图书馆的模样。', author: '博尔赫斯', source: '诗选' },
  { text: '黑夜给了我黑色的眼睛，我却用它寻找光明。', author: '顾城', source: '一代人' }
]
const quoteIndex = ref(0)
const currentQuote = computed(() => quotes[quoteIndex.value])
const nextQuote = () => {
  quoteIndex.value = (quoteIndex.value + 1) % quotes.length
}

const announcementTagType = (type) => {
  const map = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

const announcementTypeLabel = (type) => {
  const map = { 1: '通知', 2: '活动', 3: '闭馆' }
  return map[type] || '通知'
}

const formatDate = (val) => {
  if (!val) return ''
  return String(val).slice(0, 10)
}

const handleSearch = () => {
  const query = {}
  const text = keyword.value.trim()
  if (text) {
    if (searchType.value === 'title') query.title = text
    else if (searchType.value === 'author') query.author = text
    else if (searchType.value === 'isbn') query.isbn = text
    else query.keyword = text
  }
  router.push({ path: '/portal/search', query })
}

const searchHotTag = (tag) => {
  keyword.value = tag
  searchType.value = 'all'
  handleSearch()
}

const goBookDetail = (id) => {
  router.push(`/portal/book/${id}`)
}

const fallbackHomeBooks = [
  {
    id: 1,
    title: '深入理解计算机系统 (原书第3版)',
    author: 'Randal E. Bryant / David R. OHallaron',
    categoryName: '计算机科学',
    cover: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300&q=80'
  },
  {
    id: 2,
    title: '三体全集 (地球往事三部曲)',
    author: '刘慈欣',
    categoryName: '当代科幻',
    cover: 'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?w=300&q=80'
  },
  {
    id: 3,
    title: '算法导论 (原书第3版)',
    author: 'Thomas H. Cormen 等',
    categoryName: '计算机科学',
    cover: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300&q=80'
  },
  {
    id: 4,
    title: '经济学原理 (微观+宏观分册)',
    author: 'N. 格里高利·曼昆',
    categoryName: '经济管理',
    cover: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=300&q=80'
  }
]

const fallbackHomeAnnouncements = [
  {
    id: 101,
    title: '关于2026年春季学期图书馆开放时间及借还服务安排的通告',
    type: 1,
    publishTime: '2026-03-20'
  },
  {
    id: 102,
    title: '【新书上架】2026年第一批人文社科与人工智能外文图书已编目入库',
    type: 2,
    publishTime: '2026-03-18'
  },
  {
    id: 103,
    title: '关于期末自习室考研选座系统防占座签到规则的调整通知',
    type: 3,
    publishTime: '2026-03-15'
  },
  {
    id: 104,
    title: '关于万方数据知识服务平台及 Web of Science 数据库更新的通知',
    type: 1,
    publishTime: '2026-03-10'
  }
]

const fetchAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ size: 6 })
    const records = res.data?.records || res.data?.list || (Array.isArray(res.data) ? res.data : null)
    if (records && records.length > 0) {
      announcements.value = records
    } else {
      announcements.value = fallbackHomeAnnouncements
    }
  } catch {
    announcements.value = fallbackHomeAnnouncements
  }
}

const fetchNewBooks = async () => {
  try {
    const res = await getNewBooks(4)
    if (res.data && res.data.length > 0) {
      newBooks.value = res.data
    } else {
      newBooks.value = fallbackHomeBooks
    }
  } catch {
    newBooks.value = fallbackHomeBooks
  }
}

onMounted(() => {
  fetchAnnouncements()
  fetchNewBooks()
})
</script>

<style scoped>
.home-container {
  width: 100%;
}

/* ================= 1. Hero 检索区 ================= */
.hero-section {
  position: relative;
  background: linear-gradient(135deg, #091a32 0%, #112d56 45%, #1d4ed8 100%);
  border-radius: 20px;
  padding: 56px 32px 48px;
  color: #fff;
  text-align: center;
  overflow: hidden;
  box-shadow: 0 20px 40px -15px rgba(15, 23, 42, 0.25);
  margin-bottom: 32px;
}

.hero-bg-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(60px);
}

.orb-1 {
  top: -80px;
  right: -60px;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(96, 165, 250, 0.3) 0%, rgba(96, 165, 250, 0) 70%);
}

.orb-2 {
  bottom: -60px;
  left: -60px;
  width: 260px;
  height: 260px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.25) 0%, rgba(59, 130, 246, 0) 70%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
}

.hero-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 18px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  font-size: 13px;
  margin-bottom: 20px;
}

.status-pulse-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.35);
}

.greeting-text {
  font-weight: 600;
  color: #fef08a;
}

.status-divider {
  opacity: 0.4;
}

.status-highlight {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #bfdbfe;
  font-weight: 500;
}

.hero-title {
  font-size: 38px;
  font-weight: 800;
  letter-spacing: -0.5px;
  margin: 0 0 12px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.25);
}

.hero-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 30px;
  letter-spacing: 0.5px;
}

.smart-search-wrapper {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  padding: 5px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.22);
  margin-bottom: 18px;
  transition: box-shadow 0.2s;
}

.smart-search-wrapper:focus-within {
  box-shadow: 0 12px 36px rgba(37, 99, 235, 0.35);
}

.search-type-select {
  width: 135px;
}

.search-type-select :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border-right: 1px solid #e2e8f0;
  border-radius: 10px 0 0 10px;
}

.hero-search-input {
  flex: 1;
}

.hero-search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  font-size: 15px;
}

.hero-search-button {
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  height: 44px;
}

.hot-search-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
  margin-bottom: 36px;
}

.hot-title {
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.hot-chip {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
}

.hot-chip:hover {
  background: rgba(255, 255, 255, 0.32);
  transform: translateY(-2px);
}

.hero-metrics-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.metric-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.metric-num-wrap {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.metric-num {
  font-size: 26px;
  font-weight: 800;
  color: #60a5fa;
  font-family: 'DIN Alternate', Inter, sans-serif;
}

.metric-unit {
  font-size: 13px;
  color: #93c5fd;
  font-weight: 600;
}

.metric-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.metric-sep {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.15);
}

/* ================= 2. 六大核心服务网格 ================= */
.services-section {
  margin-bottom: 36px;
}

.section-title-wrap {
  margin-bottom: 20px;
}

.section-badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
  background: #eff6ff;
  padding: 3px 10px;
  border-radius: 6px;
  margin-bottom: 6px;
}

.section-main-heading {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px;
}

.section-sub-heading {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.service-box {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.service-box:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: #cbd5e1;
}

.service-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.icon-blue { background: #eff6ff; color: #2563eb; }
.icon-amber { background: #fffbeb; color: #d97706; }
.icon-emerald { background: #ecfdf5; color: #059669; }
.icon-purple { background: #faf5ff; color: #9333ea; }
.icon-cyan { background: #ecfeff; color: #0891b2; }
.icon-indigo { background: #eef2ff; color: #4f46e5; }

.service-text {
  flex: 1;
}

.service-text h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px;
}

.service-text p {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.service-arrow {
  color: #94a3b8;
  font-size: 16px;
  font-weight: bold;
  transition: transform 0.2s;
}

.service-box:hover .service-arrow {
  transform: translateX(4px);
  color: #2563eb;
}

/* ================= 3. 今日焦点速递 ================= */
.highlights-section {
  margin-bottom: 28px;
}

.panel-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 22px 24px;
  height: 100%;
  box-sizing: border-box;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.more-link {
  font-size: 13px;
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.more-link:hover {
  color: #1d4ed8;
}

/* 新书 4 宫格 */
.home-books-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.mini-book-item {
  display: flex;
  gap: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  align-items: center;
}

.mini-book-item:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
  transform: translateY(-2px);
}

.mini-cover-wrap {
  width: 52px;
  height: 72px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.mini-cover-img {
  width: 100%;
  height: 100%;
}

.mini-mock {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.mini-book-info {
  flex: 1;
  overflow: hidden;
}

.mini-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-author {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 动态列表 */
.home-news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-news-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.18s;
}

.home-news-row:hover {
  background: #f8fafc;
}

.home-news-title {
  flex: 1;
  font-size: 13px;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-news-row:hover .home-news-title {
  color: #2563eb;
}

.home-news-date {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

/* ================= 4. 每日书香条 ================= */
.quote-strip-section {
  margin-bottom: 24px;
}

.quote-strip {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.quote-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  flex-wrap: wrap;
}

.quote-icon {
  font-size: 20px;
  font-family: Georgia, serif;
  color: #f59e0b;
  font-weight: bold;
}

.quote-body-text {
  font-style: italic;
  color: #1e293b;
  font-weight: 500;
}

.quote-from {
  color: #64748b;
}

/* ================= 响应式 ================= */
@media (max-width: 1100px) {
  .services-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 36px 18px 30px;
  }
  .hero-title {
    font-size: 26px;
  }
  .smart-search-wrapper {
    flex-direction: column;
    padding: 6px;
    gap: 6px;
  }
  .search-type-select {
    width: 100%;
  }
  .search-type-select :deep(.el-input__wrapper) {
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }
  .hero-search-button {
    width: 100%;
  }
  .hero-metrics-bar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }
  .metric-sep {
    display: none;
  }
  .services-grid {
    grid-template-columns: 1fr;
  }
  .home-books-grid {
    grid-template-columns: 1fr;
  }
}
</style>
