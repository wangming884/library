<template>
  <div class="rank-page-container">
    <!-- 头部横幅 Banner -->
    <div class="rank-banner">
      <div class="banner-tag">风云榜与馆藏精选</div>
      <h1>文献借阅风云榜</h1>
      <p class="banner-desc">
        汇聚全馆借阅热度最高、读者好评最多与最新入藏的精品图书，洞察校园阅读趋势。
      </p>
    </div>

    <!-- 前三甲金银铜领奖台 (Podium) -->
    <div class="podium-section" v-if="topThree.length >= 3">
      <!-- 第二名：银牌 -->
      <div class="podium-card podium-silver" @click="goDetail(topThree[1].id)">
        <div class="medal-crown silver-medal">🥈 NO.2</div>
        <div class="podium-cover-wrap">
          <el-image :src="topThree[1].cover" fit="cover" class="podium-img">
            <template #error>
              <div class="podium-mock"><el-icon><Reading /></el-icon></div>
            </template>
          </el-image>
        </div>
        <h4 class="podium-title" :title="topThree[1].title">{{ topThree[1].title }}</h4>
        <span class="podium-author">{{ topThree[1].author || '未知作者' }}</span>
        <el-tag type="info" size="small" effect="light" class="podium-tag">
          借阅 {{ topThree[1].borrowCount || topThree[1].count || 0 }} 次
        </el-tag>
      </div>

      <!-- 第一名：金牌 -->
      <div class="podium-card podium-gold" @click="goDetail(topThree[0].id)">
        <div class="medal-crown gold-medal">👑 NO.1 榜首</div>
        <div class="podium-cover-wrap gold-wrap">
          <el-image :src="topThree[0].cover" fit="cover" class="podium-img">
            <template #error>
              <div class="podium-mock"><el-icon><Reading /></el-icon></div>
            </template>
          </el-image>
        </div>
        <h4 class="podium-title gold-title" :title="topThree[0].title">{{ topThree[0].title }}</h4>
        <span class="podium-author">{{ topThree[0].author || '未知作者' }}</span>
        <el-tag type="warning" size="small" effect="dark" class="podium-tag">
          借阅 {{ topThree[0].borrowCount || topThree[0].count || 0 }} 次
        </el-tag>
      </div>

      <!-- 第三名：铜牌 -->
      <div class="podium-card podium-bronze" @click="goDetail(topThree[2].id)">
        <div class="medal-crown bronze-medal">🥉 NO.3</div>
        <div class="podium-cover-wrap">
          <el-image :src="topThree[2].cover" fit="cover" class="podium-img">
            <template #error>
              <div class="podium-mock"><el-icon><Reading /></el-icon></div>
            </template>
          </el-image>
        </div>
        <h4 class="podium-title" :title="topThree[2].title">{{ topThree[2].title }}</h4>
        <span class="podium-author">{{ topThree[2].author || '未知作者' }}</span>
        <el-tag type="info" size="small" effect="light" class="podium-tag">
          借阅 {{ topThree[2].borrowCount || topThree[2].count || 0 }} 次
        </el-tag>
      </div>
    </div>

    <!-- 榜单切换与模式控制 -->
    <el-card shadow="never" class="rank-control-card">
      <div class="control-header">
        <el-radio-group v-model="activeTab" size="default" @change="handleTabChange">
          <el-radio-button label="rank">🔥 借阅热度总榜 (Top 20)</el-radio-button>
          <el-radio-button label="new">✨ 最新入藏图书</el-radio-button>
        </el-radio-group>

        <div class="view-mode-toggle">
          <el-button
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            size="small"
            @click="viewMode = 'grid'"
          >
            卡片画廊
          </el-button>
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            size="small"
            @click="viewMode = 'list'"
          >
            排行榜表格
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 网格画廊模式 -->
    <div v-if="viewMode === 'grid'" class="rank-grid-view" v-loading="loading">
      <div class="books-grid">
        <div
          v-for="(book, idx) in currentList"
          :key="book.id"
          class="book-card"
          @click="goDetail(book.id)"
        >
          <div class="rank-badge" :class="'badge-' + (idx < 3 ? (idx + 1) : 'other')">
            {{ idx < 3 ? ('TOP ' + (idx + 1)) : (idx + 1) }}
          </div>

          <div class="book-cover-wrap">
            <div class="book-spine-effect"></div>
            <el-image :src="book.cover" fit="cover" class="book-cover-img" lazy>
              <template #error>
                <div class="mock-cover">
                  <el-icon size="32"><Reading /></el-icon>
                  <span>暂无封面</span>
                </div>
              </template>
            </el-image>
            <div class="hover-overlay">
              <span class="hover-btn">查看详情 →</span>
            </div>
          </div>

          <div class="book-info">
            <h4 class="book-title" :title="book.title">{{ book.title }}</h4>
            <p class="book-author" :title="book.author">{{ book.author || '未知作者' }}</p>
            <div class="book-bottom">
              <el-tag size="small" effect="plain" type="info">{{ book.categoryName || '文献' }}</el-tag>
              <span class="count-badge" v-if="book.borrowCount || book.count">
                已借 {{ book.borrowCount || book.count }} 次
              </span>
              <span class="stock-badge in-stock" v-else>在馆可借</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格列表模式 -->
    <div v-else class="rank-table-view" v-loading="loading">
      <el-card shadow="never">
        <el-table :data="currentList" stripe border @row-click="(row) => goDetail(row.id)" style="cursor: pointer;">
          <el-table-column label="名次" width="80" align="center">
            <template #default="{ $index }">
              <span class="rank-order-box" :class="{ 'top-1': $index === 0, 'top-2': $index === 1, 'top-3': $index === 2 }">
                {{ $index + 1 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="cover" label="封面" width="80" align="center">
            <template #default="{ row }">
              <el-image :src="row.cover" style="width: 44px; height: 60px; border-radius: 4px;" fit="cover">
                <template #error>
                  <div class="table-mock"><el-icon><Reading /></el-icon></div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="图书题名" min-width="180" show-overflow-tooltip />
          <el-table-column prop="author" label="著者" width="140" show-overflow-tooltip />
          <el-table-column prop="isbn" label="标准 ISBN" width="140" />
          <el-table-column prop="categoryName" label="文献分类" width="120" />
          <el-table-column label="借阅频次" width="110" align="center">
            <template #default="{ row }">
              <el-tag type="primary" effect="light" size="small">
                {{ row.borrowCount || row.count || 0 }} 次
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click.stop="goDetail(row.id)">借阅详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading } from '@element-plus/icons-vue'
import { getBorrowRank, getNewBooks } from '../../api/modules/book'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('rank')
const viewMode = ref('grid')

const rankList = ref([])
const newBooksList = ref([])

const currentList = computed(() => {
  return activeTab.value === 'rank' ? rankList.value : newBooksList.value
})

const topThree = computed(() => {
  return rankList.value.slice(0, 3)
})

const goDetail = (id) => {
  router.push(`/portal/book/${id}`)
}

const fetchData = async () => {
  loading.value = true
  try {
    const [rankRes, newRes] = await Promise.all([
      getBorrowRank(20),
      getNewBooks(20)
    ])
    rankList.value = rankRes.data || []
    newBooksList.value = newRes.data || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // computed handles list
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.rank-page-container {
  width: 100%;
}

.rank-banner {
  background: linear-gradient(135deg, #091a32 0%, #173868 45%, #1d4ed8 100%);
  border-radius: 18px;
  padding: 44px 32px;
  color: #fff;
  margin-bottom: 28px;
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

.rank-banner h1 {
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

/* 领奖台 (Podium) */
.podium-section {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 10px 0;
}

.podium-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  padding: 24px 20px;
  width: 240px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.podium-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
}

.podium-gold {
  border-color: #fde68a;
  background: linear-gradient(180deg, #fffdf0 0%, #ffffff 100%);
  box-shadow: 0 12px 28px rgba(245, 158, 11, 0.15);
  padding: 32px 20px 28px;
  width: 270px;
}

.podium-silver {
  border-color: #e2e8f0;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.podium-bronze {
  border-color: #fed7aa;
  background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
}

.medal-crown {
  font-size: 13px;
  font-weight: 800;
  padding: 4px 12px;
  border-radius: 20px;
  margin-bottom: 14px;
}

.gold-medal { background: #fef3c7; color: #b45309; }
.silver-medal { background: #f1f5f9; color: #475569; }
.bronze-medal { background: #ffedd5; color: #c2410c; }

.podium-cover-wrap {
  width: 100px;
  height: 140px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.12);
  margin-bottom: 12px;
}

.gold-wrap {
  width: 114px;
  height: 160px;
}

.podium-img {
  width: 100%;
  height: 100%;
}

.podium-mock {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.podium-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.gold-title {
  font-size: 16px;
  color: #92400e;
}

.podium-author {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.podium-tag {
  font-weight: 600;
}

/* 控制栏 */
.rank-control-card {
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.control-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

/* 网格画廊 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.book-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}

.rank-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 3;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 800;
  color: #fff;
}

.badge-1 { background: linear-gradient(135deg, #f59e0b, #d97706); }
.badge-2 { background: linear-gradient(135deg, #94a3b8, #64748b); }
.badge-3 { background: linear-gradient(135deg, #b45309, #78350f); }
.badge-other { background: rgba(15, 23, 42, 0.7); }

.book-cover-wrap {
  height: 204px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.book-spine-effect {
  position: absolute;
  left: calc(50% - 60px);
  top: 18px;
  width: 4px;
  height: 168px;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.2), rgba(255, 255, 255, 0.2));
  z-index: 2;
  pointer-events: none;
}

.book-cover-img {
  width: 120px;
  height: 168px;
  border-radius: 6px;
  box-shadow: -3px 0 8px rgba(0, 0, 0, 0.1), 4px 6px 14px rgba(0, 0, 0, 0.12);
  transition: transform 0.3s;
}

.book-card:hover .book-cover-img {
  transform: scale(1.05);
}

.mock-cover {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 11px;
  gap: 4px;
}

.hover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
}

.book-card:hover .hover-overlay {
  opacity: 1;
}

.hover-btn {
  background: #fff;
  color: #1e293b;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 20px;
}

.book-info {
  padding: 14px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-bottom {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.count-badge {
  font-size: 12px;
  color: #2563eb;
  font-weight: 600;
}

.stock-badge.in-stock {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
}

/* 表格视图 */
.rank-order-box {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 6px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.top-1 { background: #f59e0b; color: #fff; }
.top-2 { background: #94a3b8; color: #fff; }
.top-3 { background: #d97706; color: #fff; }

.table-mock {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

@media (max-width: 1024px) {
  .books-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .podium-section {
    flex-direction: column;
    align-items: center;
  }
  .podium-card, .podium-gold {
    width: 100%;
    max-width: 320px;
  }
  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
